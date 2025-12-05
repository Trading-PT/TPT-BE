/**
 * TPT-API 운영 서버 부하 테스트 스크립트 (k6)
 *
 * 테스트 대상 API:
 * 1. GET /api/v1/feedback-requests?page=0&size=50 - 피드백 요청 목록
 * 2. GET /api/v1/weekly-trading-summary/customers/me/years/{year}/months/{month}/weeks/{week}
 * 3. GET /api/v1/monthly-trading-summaries/customers/me/years/{year}/months/{month}
 *
 * 실행 방법 (EC2에서):
 *   BASE_URL=https://api.tradingpt.kr k6 run production-load-test.js
 */

import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

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
const weeklyApiTrend = new Trend('weekly_api_duration');
const monthlyApiTrend = new Trend('monthly_api_duration');
const totalRequests = new Counter('total_requests');

// =====================================================
// 테스트 시나리오 옵션
// =====================================================
export const options = {
    scenarios: {
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
    thresholds: {
        http_req_duration: ['p(95)<1000'],     // 95%의 요청이 1초 이내
        // 404는 데이터 없음으로 정상이므로, api_error_rate로 실패 판단
        api_error_rate: ['rate<0.05'],          // API 에러율 5% 미만
        login_success_rate: ['rate>0.95'],      // 로그인 성공률 95% 이상
    },
    http2: true,
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
 * Spring Security CookieCsrfTokenRepository는 XSRF-TOKEN 쿠키 사용
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

    // 로그인 성공 후 CSRF 토큰 추출
    let csrfToken = extractCsrfTokenFromHeaders(response);
    if (!csrfToken) {
        csrfToken = extractCsrfTokenFromCookies(jar, BASE_URL);
    }

    return { success, csrfToken };
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

/**
 * 테스트용 날짜 파라미터 생성
 */
function getTestDateParams() {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth() + 1;
    const firstDayOfMonth = new Date(year, month - 1, 1);
    const dayOfMonth = now.getDate();
    const week = Math.ceil((dayOfMonth + firstDayOfMonth.getDay()) / 7);
    return { year, month, week: Math.min(week, 5) };
}

// =====================================================
// 메인 테스트 시나리오
// =====================================================
export default function () {
    const vuId = __VU;
    const user = getTestUser(vuId);
    const jar = http.cookieJar();
    const dateParams = getTestDateParams();

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

    sleep(Math.random() * 0.5 + 0.5);

    // 2. 피드백 요청 목록 조회 (size=50)
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

    sleep(Math.random() * 0.5 + 0.5);

    // 3. 주간 매매 요약 조회
    group('Weekly Trading Summary API', function () {
        const startTime = Date.now();
        const url = `${BASE_URL}/api/v1/weekly-trading-summary/customers/me/years/${dateParams.year}/months/${dateParams.month}/weeks/${dateParams.week}`;
        const response = authenticatedGet(url, jar, csrfToken);
        const duration = Date.now() - startTime;
        weeklyApiTrend.add(duration);

        // 404는 데이터 없음이므로 정상 처리
        const success = check(response, {
            'weekly summary status is 200 or 404': (r) => r.status === 200 || r.status === 404,
        });

        apiErrorRate.add(!success);

        if (!success && response.status !== 404) {
            console.log(`Weekly summary failed: ${response.status} - ${response.body?.substring(0, 200)}`);
        }
    });

    sleep(Math.random() * 0.5 + 0.5);

    // 4. 월간 매매 요약 조회
    group('Monthly Trading Summary API', function () {
        const startTime = Date.now();
        const url = `${BASE_URL}/api/v1/monthly-trading-summaries/customers/me/years/${dateParams.year}/months/${dateParams.month}`;
        const response = authenticatedGet(url, jar, csrfToken);
        const duration = Date.now() - startTime;
        monthlyApiTrend.add(duration);

        // 404는 데이터 없음이므로 정상 처리
        const success = check(response, {
            'monthly summary status is 200 or 404': (r) => r.status === 200 || r.status === 404,
        });

        apiErrorRate.add(!success);

        if (!success && response.status !== 404) {
            console.log(`Monthly summary failed: ${response.status} - ${response.body?.substring(0, 200)}`);
        }
    });

    // 요청 간 랜덤 대기 (실제 사용자 행동 시뮬레이션)
    sleep(Math.random() * 2 + 1);
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
    console.log(`로그인 성공률: ${((data.metrics.login_success_rate?.values?.rate || 0) * 100).toFixed(2)}%`);
    console.log(`API 에러율: ${((data.metrics.api_error_rate?.values?.rate || 0) * 100).toFixed(2)}%`);
    console.log('----------------------------------------');
    console.log('API별 평균 응답 시간:');
    console.log(`  - 피드백 목록 (size=50): ${(data.metrics.feedback_api_duration?.values?.avg || 0).toFixed(2)}ms`);
    console.log(`  - 주간 요약: ${(data.metrics.weekly_api_duration?.values?.avg || 0).toFixed(2)}ms`);
    console.log(`  - 월간 요약: ${(data.metrics.monthly_api_duration?.values?.avg || 0).toFixed(2)}ms`);
    console.log('========================================');

    const thresholdsPassed = Object.entries(data.metrics)
        .filter(([key, value]) => value.thresholds)
        .every(([key, value]) => Object.values(value.thresholds).every(t => t.ok));

    if (thresholdsPassed) {
        console.log('✅ 모든 성능 임계값 통과!');
    } else {
        console.log('❌ 일부 성능 임계값 미달');
    }

    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
        [`summary-${timestamp}.json`]: JSON.stringify(data, null, 2),
    };
}

import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';
