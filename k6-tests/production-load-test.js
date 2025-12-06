/**
 * TPT-API 운영 서버 부하 테스트 스크립트 (k6)
 *
 * 버전: v2 - 최대 부하 10분 유지
 *
 * 테스트 대상 API:
 * 1. POST /api/v1/auth/login - 로그인
 * 2. GET /api/v1/feedback-requests?page=0&size=50 - 피드백 요청 목록
 *
 * 실행 방법:
 *   BASE_URL=https://api.tradingpt.kr k6 run production-load-test-v2.js
 *
 * 테스트 총 시간: 약 20분
 * - 웜업: 2분 (0 → 100 VU)
 * - 점진적 증가: 3분 (100 → 500 VU)
 * - 최대 부하 유지: 10분 (1000 VU) ← 핵심 구간
 * - 점진적 감소: 3분 (1000 → 500 VU)
 * - 쿨다운: 2분 (500 → 0 VU)
 */

import http from 'k6/http';
import {check, group, sleep} from 'k6';
import {Counter, Rate, Trend} from 'k6/metrics';
import {textSummary} from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

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
const feedbackApiTrend = new Trend('feedback_api_duration');
const totalRequests = new Counter('total_requests');

// =====================================================
// 테스트 시나리오 옵션 (v2 - 10분 유지)
// =====================================================
export const options = {
    scenarios: {
        ramping_users: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                // === 웜업 단계 ===
                {duration: '2m', target: 100},     // 2분: 0 → 100 VU (서버 웜업)

                // === 점진적 증가 ===
                {duration: '3m', target: 500},     // 3분: 100 → 500 VU

                // === 최대 부하 유지 (핵심!) ===
                {duration: '10m', target: 1000},   // 10분: 1000 VU 유지

                // === 점진적 감소 ===
                {duration: '3m', target: 500},     // 3분: 1000 → 500 VU

                // === 쿨다운 ===
                {duration: '2m', target: 0},       // 2분: 500 → 0 VU
            ],
            gracefulRampDown: '30s',
        },
    },
    thresholds: {
        // 응답 시간 기준
        http_req_duration: ['p(95)<500', 'p(99)<1000'],  // p95 < 500ms, p99 < 1초

        // 에러율 기준
        api_error_rate: ['rate<0.05'],           // API 에러율 5% 미만

        // 로그인 성공률
        login_success_rate: ['rate>0.95'],       // 로그인 성공률 95% 이상

        // 피드백 API 응답 시간
        feedback_api_duration: ['p(95)<500'],    // 피드백 API p95 < 500ms
    },

    // HTTP/2 사용
    http2: true,

    // 타임아웃 설정
    httpTimeout: '30s',
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
 */
function login(user, jar) {
    const startTime = Date.now();

    const loginPayload = JSON.stringify({
        username: user.username,
        password: user.password,
        rememberMe: false,
    });

    const response = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, {
        headers: {
            'Content-Type': 'application/json',
        },
        jar: jar,
        redirects: 0,
    });

    const duration = Date.now() - startTime;
    loginDuration.add(duration);
    totalRequests.add(1);

    const success = check(response, {
        'login status is 200': (r) => r.status === 200,
        'login has session cookie': (r) => {
            const cookies = jar.cookiesForURL(BASE_URL);
            return cookies && cookies['SESSION'];
        },
    });

    loginSuccessRate.add(success);

    if (!success) {
        console.log(`Login failed for ${user.username}: ${response.status} - ${response.body?.substring(0, 200)}`);
    }

    let csrfToken = extractCsrfTokenFromHeaders(response);
    if (!csrfToken) {
        csrfToken = extractCsrfTokenFromCookies(jar, BASE_URL);
    }

    return {success, csrfToken};
}

/**
 * 인증된 GET 요청
 */
function authenticatedGet(url, jar, csrfToken) {
    const headers = {
        'Content-Type': 'application/json',
    };

    if (csrfToken) {
        headers['X-XSRF-TOKEN'] = csrfToken;
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
            sleep(1);
            return;
        }
    });

    if (!loggedIn) {
        return;
    }

    // 로그인 후 잠시 대기 (실제 사용자 행동)
    sleep(Math.random() * 0.5 + 0.5);

    // 2. 피드백 요청 목록 조회
    group('Feedback Request List API', function () {
        const startTime = Date.now();
        const response = authenticatedGet(
            `${BASE_URL}/api/v1/feedback-requests?page=0&size=50`,
            jar,
            csrfToken
        );
        const duration = Date.now() - startTime;
        feedbackApiTrend.add(duration);

        const success = check(response, {
            'feedback list status is 200': (r) => r.status === 200,
            'feedback list has result': (r) => {
                try {
                    const body = JSON.parse(r.body);
                    return body.result !== null && body.result !== undefined;
                } catch (e) {
                    return false;
                }
            },
        });

        apiErrorRate.add(!success);

        if (!success) {
            console.log(`Feedback list failed: ${response.status} - ${response.body?.substring(0, 200)}`);
        }
    });

    // 요청 간 랜덤 대기 (실제 사용자 행동 시뮬레이션)
    // 평균 2초 대기 → 1000 VU 기준 약 500 TPS
    sleep(Math.random() * 2 + 1);
}

// =====================================================
// 테스트 완료 후 요약
// =====================================================
export function handleSummary(data) {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');

    console.log('\n');
    console.log('════════════════════════════════════════════════════════════════');
    console.log('🚀 TPT-API 운영 서버 부하 테스트 결과 (v2 - 10분 유지)');
    console.log('════════════════════════════════════════════════════════════════');
    console.log(`테스트 대상: ${BASE_URL}`);
    console.log(`테스트 시간: 약 20분 (최대 부하 10분 유지)`);
    console.log('────────────────────────────────────────────────────────────────');

    console.log('\n📊 전체 요약');
    console.log(`  총 요청 수: ${data.metrics.http_reqs?.values?.count || 0}`);
    console.log(`  평균 응답 시간: ${(data.metrics.http_req_duration?.values?.avg || 0).toFixed(2)}ms`);
    console.log(`  P95 응답 시간: ${(data.metrics.http_req_duration?.values?.['p(95)'] || 0).toFixed(2)}ms`);
    console.log(`  P99 응답 시간: ${(data.metrics.http_req_duration?.values?.['p(99)'] || 0).toFixed(2)}ms`);

    console.log('\n✅ 성공률');
    console.log(`  로그인 성공률: ${((data.metrics.login_success_rate?.values?.rate || 0) * 100).toFixed(2)}%`);
    console.log(`  API 에러율: ${((data.metrics.api_error_rate?.values?.rate || 0) * 100).toFixed(2)}%`);

    console.log('\n⏱️ API별 응답 시간');
    console.log(`  로그인 API: ${(data.metrics.login_duration?.values?.avg || 0).toFixed(2)}ms (avg)`);
    console.log(`  피드백 목록 API: ${(data.metrics.feedback_api_duration?.values?.avg || 0).toFixed(2)}ms (avg)`);
    console.log(`  피드백 목록 P95: ${(data.metrics.feedback_api_duration?.values?.['p(95)'] || 0).toFixed(2)}ms`);

    console.log('\n════════════════════════════════════════════════════════════════');

    // 임계값 통과 여부 확인
    const thresholdResults = {};
    let allPassed = true;

    Object.entries(data.metrics).forEach(([key, value]) => {
        if (value.thresholds) {
            Object.entries(value.thresholds).forEach(([threshold, result]) => {
                thresholdResults[`${key}: ${threshold}`] = result.ok;
                if (!result.ok) allPassed = false;
            });
        }
    });

    console.log('\n📋 임계값 검사 결과');
    Object.entries(thresholdResults).forEach(([name, passed]) => {
        console.log(`  ${passed ? '✅' : '❌'} ${name}`);
    });

    console.log('\n════════════════════════════════════════════════════════════════');
    if (allPassed) {
        console.log('🎉 모든 성능 임계값 통과! 서버가 1000 VU를 10분간 안정적으로 처리했습니다.');
    } else {
        console.log('⚠️ 일부 성능 임계값 미달 - 서버 스펙 조정이 필요할 수 있습니다.');
    }
    console.log('════════════════════════════════════════════════════════════════\n');

    return {
        'stdout': textSummary(data, {indent: ' ', enableColors: true}),
        [`summary-${timestamp}.json`]: JSON.stringify(data, null, 2),
    };
}

