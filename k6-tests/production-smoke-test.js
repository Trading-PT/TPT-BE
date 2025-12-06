/**
 * TPT-API 운영 서버 스모크 테스트 (k6)
 *
 * 본 부하 테스트 전 기본 동작 확인용
 * 적은 수의 사용자로 빠르게 테스트
 *
 * 실행 방법 (EC2에서):
 *   BASE_URL=https://api.tradingpt.kr k6 run production-smoke-test.js
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// =====================================================
// 설정
// =====================================================

const BASE_URL = __ENV.BASE_URL || 'https://api.tradingpt.kr';

const TEST_USER_PREFIX = 'loadtest_user_';
const TEST_PASSWORD = 'loadtest123!';
const START_USER_NUM = 900001;

// =====================================================
// 커스텀 메트릭
// =====================================================
const loginSuccessRate = new Rate('login_success_rate');
const loginDuration = new Trend('login_duration');
const apiErrorRate = new Rate('api_error_rate');

// =====================================================
// 테스트 옵션 (스모크 테스트)
// =====================================================
export const options = {
    vus: 5,           // 5명의 가상 사용자
    duration: '1m',   // 1분 동안 실행
    thresholds: {
        http_req_duration: ['p(95)<2000'],     // 95%가 2초 이내
        // 404는 데이터 없음으로 정상이므로, api_error_rate로 실패 판단
        api_error_rate: ['rate<0.1'],          // API 에러율 10% 미만
        login_success_rate: ['rate>0.9'],      // 로그인 성공률 90% 이상
    },
};

// =====================================================
// 헬퍼 함수
// =====================================================

function getTestUser(vuId) {
    const userNum = START_USER_NUM + (vuId % 1000);
    return {
        username: `${TEST_USER_PREFIX}${userNum}`,
        password: TEST_PASSWORD,
    };
}

/**
 * 쿠키에서 CSRF 토큰 추출
 */
function extractCsrfTokenFromCookies(jar, url) {
    const cookies = jar.cookiesForURL(url);
    if (cookies && cookies['XSRF-TOKEN']) {
        return cookies['XSRF-TOKEN'][0];
    }
    return '';
}

/**
 * 응답 헤더에서 CSRF 토큰 추출
 */
function extractCsrfTokenFromHeaders(response) {
    return response.headers['Xsrf-Token'] ||
           response.headers['XSRF-TOKEN'] ||
           response.headers['xsrf-token'] ||
           '';
}

/**
 * 로그인 수행
 * /api/v1/auth/** 경로는 CSRF 면제됨
 */
function login(user, jar) {
    const startTime = Date.now();

    const loginPayload = JSON.stringify({
        username: user.username,
        password: user.password,
        rememberMe: false,  // JSON 필드명: rememberMe (not remember-me)
    });

    const response = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, {
        headers: {
            'Content-Type': 'application/json',
        },
        jar: jar,
    });

    const duration = Date.now() - startTime;
    loginDuration.add(duration);

    const success = check(response, {
        'login status is 200': (r) => r.status === 200,
    });

    loginSuccessRate.add(success);

    if (!success) {
        console.log(`Login failed: ${response.status} - ${response.body?.substring(0, 200)}`);
    }

    // CSRF 토큰 추출
    let csrfToken = extractCsrfTokenFromHeaders(response);
    if (!csrfToken) {
        csrfToken = extractCsrfTokenFromCookies(jar, BASE_URL);
    }

    return { success, csrfToken };
}

// =====================================================
// 메인 테스트
// =====================================================
export default function () {
    const vuId = __VU;
    const user = getTestUser(vuId);
    const jar = http.cookieJar();

    let csrfToken = '';
    let loggedIn = false;

    // 1. 로그인
    group('Login', function () {
        const result = login(user, jar);
        loggedIn = result.success;
        csrfToken = result.csrfToken;

        if (!loggedIn) {
            apiErrorRate.add(1);
            return;
        }
    });

    if (!loggedIn) {
        sleep(1);
        return;
    }

    sleep(0.5);

    // 2. 피드백 요청 목록 조회 API 테스트
    group('Feedback Request List API', function () {
        const headers = {
            'Content-Type': 'application/json',
        };
        if (csrfToken) {
            headers['X-XSRF-TOKEN'] = csrfToken;
        }

        // 피드백 요청 목록 조회 (size=50)
        let response = http.get(`${BASE_URL}/api/v1/feedback-requests?page=0&size=50`, {
            headers: headers,
            jar: jar,
        });
        let success = check(response, {
            'feedback list is 200': (r) => r.status === 200,
        });
        apiErrorRate.add(!success);
        if (!success) {
            console.log(`Feedback list failed: ${response.status} - ${response.body?.substring(0, 200)}`);
        }
    });

    sleep(1);
}

// =====================================================
// 결과 요약
// =====================================================
export function handleSummary(data) {
    console.log('\n=== 🔥 스모크 테스트 결과 ===');
    console.log(`테스트 대상: ${BASE_URL}`);
    console.log(`총 요청: ${data.metrics.http_reqs?.values?.count || 0}`);
    console.log(`평균 응답시간: ${(data.metrics.http_req_duration?.values?.avg || 0).toFixed(2)}ms`);
    console.log(`API 에러율: ${((data.metrics.api_error_rate?.values?.rate || 0) * 100).toFixed(2)}%`);
    console.log(`로그인 성공률: ${((data.metrics.login_success_rate?.values?.rate || 0) * 100).toFixed(2)}%`);

    const passed = (data.metrics.api_error_rate?.values?.rate || 0) < 0.1 &&
        (data.metrics.login_success_rate?.values?.rate || 0) > 0.9;

    console.log(`상태: ${passed ? '✅ PASS' : '❌ FAIL'}`);

    if (passed) {
        console.log('\n→ 스모크 테스트 통과! 본 부하 테스트 진행 가능');
        console.log('  실행: k6 run production-load-test.js');
    } else {
        console.log('\n→ 스모크 테스트 실패. 문제 확인 필요');
    }

    return {};
}
