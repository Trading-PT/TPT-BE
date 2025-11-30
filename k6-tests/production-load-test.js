/**
 * TPT-API 운영 서버 부하 테스트 스크립트 (k6)
 *
 * 실제 로그인 플로우를 사용하여 운영 서버에서 부하 테스트 실행
 *
 * 사전 준비:
 * 1. create-test-accounts.sql 실행하여 운영 DB에 테스트 계정 생성
 * 2. 테스트 계정 비밀번호: loadtest123!
 *
 * 실행 방법 (EC2에서):
 *   BASE_URL=https://api.tradingpt.kr k6 run production-load-test.js
 *
 * 결과 리포트 생성:
 *   BASE_URL=https://api.tradingpt.kr k6 run --out json=results.json production-load-test.js
 *
 * 테스트 완료 후:
 *   cleanup-test-accounts.sql 실행하여 테스트 데이터 정리
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// =====================================================
// 설정
// =====================================================

// 운영 서버 URL
const BASE_URL = __ENV.BASE_URL || 'https://api.tradingpt.kr';

// 테스트 계정 설정
const TEST_USER_PREFIX = 'loadtest_user_';
const TEST_PASSWORD = 'loadtest123!';
const START_USER_NUM = 900001;

// =====================================================
// 커스텀 메트릭
// =====================================================
const loginSuccessRate = new Rate('login_success_rate');
const loginDuration = new Trend('login_duration');
const apiErrorRate = new Rate('api_error_rate');
const memoApiTrend = new Trend('memo_api_duration');
const feedbackApiTrend = new Trend('feedback_api_duration');
const userMeApiTrend = new Trend('user_me_api_duration');
const totalRequests = new Counter('total_requests');

// =====================================================
// 테스트 시나리오 옵션
// =====================================================
export const options = {
    scenarios: {
        // 점진적으로 사용자 수 증가
        ramping_users: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '1m', target: 100 },    // 1분 동안 100명까지 증가
                { duration: '2m', target: 500 },    // 2분 동안 500명까지 증가
                { duration: '3m', target: 1000 },   // 3분 동안 1000명 유지
                { duration: '2m', target: 500 },    // 2분 동안 500명으로 감소
                { duration: '1m', target: 0 },      // 1분 동안 0명으로 감소
            ],
            gracefulRampDown: '30s',
        },
    },

    // 성능 임계값 설정
    thresholds: {
        http_req_duration: ['p(95)<1000'],     // 95%의 요청이 1초 이내
        http_req_failed: ['rate<0.05'],         // 실패율 5% 미만
        login_success_rate: ['rate>0.95'],      // 로그인 성공률 95% 이상
        api_error_rate: ['rate<0.05'],          // API 에러율 5% 미만
    },

    // HTTP 2.0 사용
    http2: true,
};

// =====================================================
// 세션 쿠키 저장소 (VU별로 격리)
// =====================================================
let sessionCookies = {};

// =====================================================
// 헬퍼 함수
// =====================================================

/**
 * 테스트 사용자 정보 생성
 */
function getTestUser(vuId) {
    const userNum = START_USER_NUM + (vuId % 1000);
    return {
        username: `${TEST_USER_PREFIX}${userNum}`,
        password: TEST_PASSWORD,
    };
}

/**
 * CSRF 토큰 가져오기
 */
function getCsrfToken(jar) {
    // CSRF 토큰은 보통 첫 요청에서 쿠키로 설정됨
    const response = http.get(`${BASE_URL}/api/v1/auth/csrf`, {
        jar: jar,
        redirects: 0,
    });

    // X-CSRF-TOKEN 헤더에서 토큰 추출
    const csrfToken = response.headers['X-Csrf-Token'] || response.headers['x-csrf-token'];
    return csrfToken;
}

/**
 * 로그인 수행
 */
function login(user, jar) {
    const startTime = Date.now();

    // CSRF 토큰 가져오기 (필요한 경우)
    let csrfToken = '';
    try {
        csrfToken = getCsrfToken(jar) || '';
    } catch (e) {
        // CSRF가 없어도 진행 시도
    }

    const loginPayload = JSON.stringify({
        username: user.username,
        password: user.password,
        'remember-me': false,
    });

    const headers = {
        'Content-Type': 'application/json',
    };

    if (csrfToken) {
        headers['X-CSRF-TOKEN'] = csrfToken;
    }

    const response = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, {
        headers: headers,
        jar: jar,
        redirects: 0,
    });

    const duration = Date.now() - startTime;
    loginDuration.add(duration);
    totalRequests.add(1);

    const success = check(response, {
        'login status is 200': (r) => r.status === 200,
        'login has session cookie': (r) => {
            const cookies = r.cookies;
            return cookies && (cookies['SESSION'] || cookies['JSESSIONID']);
        },
    });

    loginSuccessRate.add(success);

    if (!success) {
        console.log(`Login failed for ${user.username}: ${response.status} - ${response.body}`);
    }

    return success;
}

/**
 * 인증된 GET 요청
 */
function authenticatedGet(url, jar, csrfToken) {
    const headers = {
        'Content-Type': 'application/json',
    };

    if (csrfToken) {
        headers['X-CSRF-TOKEN'] = csrfToken;
    }

    const response = http.get(url, {
        headers: headers,
        jar: jar,
    });

    totalRequests.add(1);
    return response;
}

// =====================================================
// 메인 테스트 시나리오
// =====================================================
export default function () {
    const vuId = __VU;
    const user = getTestUser(vuId);

    // 쿠키 jar 생성 (세션 유지용)
    const jar = http.cookieJar();

    // 1. 로그인
    group('Login', function () {
        const loggedIn = login(user, jar);

        if (!loggedIn) {
            apiErrorRate.add(1);
            sleep(1);
            return; // 로그인 실패 시 이번 반복 스킵
        }
    });

    // CSRF 토큰 가져오기
    let csrfToken = '';
    try {
        csrfToken = getCsrfToken(jar) || '';
    } catch (e) {
        // 무시
    }

    // 2. 사용자 정보 조회
    group('User Me API', function () {
        const startTime = Date.now();
        const response = authenticatedGet(`${BASE_URL}/api/v1/auth/me`, jar, csrfToken);
        const duration = Date.now() - startTime;

        userMeApiTrend.add(duration);

        const success = check(response, {
            'user me status is 200': (r) => r.status === 200,
            'user me has result': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body.result !== null;
                } catch (e) {
                    return false;
                }
            },
        });

        apiErrorRate.add(!success);
    });

    sleep(Math.random() * 0.5 + 0.5); // 0.5~1초 대기

    // 3. 메모 조회
    group('Memo API', function () {
        const startTime = Date.now();
        const response = authenticatedGet(`${BASE_URL}/api/v1/memo`, jar, csrfToken);
        const duration = Date.now() - startTime;

        memoApiTrend.add(duration);

        const success = check(response, {
            'memo status is 200 or 404': (r) => r.status === 200 || r.status === 404,
        });

        apiErrorRate.add(!success);
    });

    sleep(Math.random() * 0.5 + 0.5);

    // 4. 피드백 요청 목록 조회
    group('Feedback Request API', function () {
        const startTime = Date.now();
        const response = authenticatedGet(
            `${BASE_URL}/api/v1/feedback-request?page=0&size=10`,
            jar,
            csrfToken
        );
        const duration = Date.now() - startTime;

        feedbackApiTrend.add(duration);

        const success = check(response, {
            'feedback list status is 200': (r) => r.status === 200,
        });

        apiErrorRate.add(!success);
    });

    // 요청 간 랜덤 대기 (실제 사용자 행동 시뮬레이션)
    sleep(Math.random() * 2 + 1); // 1~3초 랜덤 대기
}

// =====================================================
// 테스트 완료 후 요약
// =====================================================
export function handleSummary(data) {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');

    console.log('========================================');
    console.log('🚀 TPT-API 운영 서버 부하 테스트 결과');
    console.log('========================================');
    console.log(`테스트 대상: ${BASE_URL}`);
    console.log(`총 요청 수: ${data.metrics.http_reqs?.values?.count || 0}`);
    console.log(`평균 응답 시간: ${(data.metrics.http_req_duration?.values?.avg || 0).toFixed(2)}ms`);
    console.log(`95 백분위 응답 시간: ${(data.metrics.http_req_duration?.values?.['p(95)'] || 0).toFixed(2)}ms`);
    console.log(`HTTP 실패율: ${((data.metrics.http_req_failed?.values?.rate || 0) * 100).toFixed(2)}%`);
    console.log(`로그인 성공률: ${((data.metrics.login_success_rate?.values?.rate || 0) * 100).toFixed(2)}%`);
    console.log(`API 에러율: ${((data.metrics.api_error_rate?.values?.rate || 0) * 100).toFixed(2)}%`);
    console.log('========================================');

    // 임계값 통과 여부 확인
    const thresholdsPassed = Object.entries(data.metrics)
        .filter(([key, value]) => value.thresholds)
        .every(([key, value]) => Object.values(value.thresholds).every(t => t.ok));

    if (thresholdsPassed) {
        console.log('✅ 모든 성능 임계값 통과!');
    } else {
        console.log('❌ 일부 성능 임계값 미달');
    }

    // 결과 파일 생성
    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
        [`summary-${timestamp}.json`]: JSON.stringify(data, null, 2),
    };
}

// k6 내장 텍스트 요약 함수
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';
