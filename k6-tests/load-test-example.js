/**
 * TPT-API 부하 테스트 스크립트 (k6)
 *
 * 실행 방법:
 * 1. 서버를 loadtest 프로필로 실행:
 *    SPRING_PROFILES_ACTIVE=loadtest ./gradlew bootRun
 *
 * 2. k6 실행 (EC2에서):
 *    k6 run load-test-example.js
 *
 * 3. 결과 리포트 생성:
 *    k6 run --out json=results.json load-test-example.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// =====================================================
// 설정
// =====================================================

// API 서버 URL (운영 서버 또는 Dev 서버)
const BASE_URL = __ENV.BASE_URL || 'https://dev.tradingpt.kr';

// 부하테스트 시크릿 키 (application-loadtest.yml과 일치해야 함)
const LOADTEST_SECRET = __ENV.LOADTEST_SECRET || 'tpt-loadtest-secret-2024';

// =====================================================
// 커스텀 메트릭
// =====================================================
const errorRate = new Rate('errors');
const memoApiTrend = new Trend('memo_api_duration');
const feedbackApiTrend = new Trend('feedback_api_duration');

// =====================================================
// 테스트 시나리오 옵션
// =====================================================
export const options = {
    // 시나리오 1: 점진적 부하 증가 (Ramp-up)
    scenarios: {
        // 점진적으로 사용자 수 증가
        ramping_users: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 100 },   // 30초 동안 100명까지 증가
                { duration: '1m', target: 500 },    // 1분 동안 500명까지 증가
                { duration: '2m', target: 1000 },   // 2분 동안 1000명 유지
                { duration: '1m', target: 500 },    // 1분 동안 500명으로 감소
                { duration: '30s', target: 0 },     // 30초 동안 0명으로 감소
            ],
            gracefulRampDown: '30s',
        },
    },

    // 성능 임계값 설정
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95%의 요청이 500ms 이내
        http_req_failed: ['rate<0.01'],     // 실패율 1% 미만
        errors: ['rate<0.05'],              // 에러율 5% 미만
    },
};

// =====================================================
// 부하테스트용 인증 헤더 생성
// =====================================================
function getAuthHeaders(userId) {
    return {
        'Content-Type': 'application/json',
        'X-Load-Test-Auth': LOADTEST_SECRET,
        'X-Load-Test-User-Id': String(userId),
        'X-Load-Test-Username': `loadtest_user_${userId}`,
        'X-Load-Test-Role': 'ROLE_CUSTOMER',
    };
}

// =====================================================
// 테스트 시나리오
// =====================================================
export default function () {
    // 각 VU(가상 사용자)에게 고유한 ID 부여
    const userId = __VU * 1000 + __ITER;
    const headers = getAuthHeaders(userId);

    // 시나리오 1: 메모 조회 API 테스트
    testMemoApi(headers);

    // 시나리오 2: 피드백 요청 목록 조회 테스트
    testFeedbackListApi(headers);

    // 시나리오 3: 사용자 정보 조회 테스트
    testUserMeApi(headers);

    // 요청 간 짧은 대기
    sleep(Math.random() * 2 + 1); // 1~3초 랜덤 대기
}

// =====================================================
// API 테스트 함수들
// =====================================================

function testMemoApi(headers) {
    const startTime = Date.now();

    const response = http.get(`${BASE_URL}/api/v1/memo`, { headers });

    const duration = Date.now() - startTime;
    memoApiTrend.add(duration);

    const success = check(response, {
        'memo API status is 200 or 404': (r) => r.status === 200 || r.status === 404,
        'memo API response time < 500ms': (r) => r.timings.duration < 500,
    });

    errorRate.add(!success);
}

function testFeedbackListApi(headers) {
    const startTime = Date.now();

    // 페이징 파라미터 추가
    const params = {
        headers,
    };

    const response = http.get(
        `${BASE_URL}/api/v1/feedback-request?page=0&size=10`,
        params
    );

    const duration = Date.now() - startTime;
    feedbackApiTrend.add(duration);

    const success = check(response, {
        'feedback list API status is 200': (r) => r.status === 200,
        'feedback list API response time < 500ms': (r) => r.timings.duration < 500,
    });

    errorRate.add(!success);
}

function testUserMeApi(headers) {
    const response = http.get(`${BASE_URL}/api/v1/auth/me`, { headers });

    const success = check(response, {
        'user me API status is 200': (r) => r.status === 200,
    });

    errorRate.add(!success);
}

// =====================================================
// 테스트 완료 후 요약
// =====================================================
export function handleSummary(data) {
    console.log('========================================');
    console.log('부하 테스트 결과 요약');
    console.log('========================================');
    console.log(`총 요청 수: ${data.metrics.http_reqs.values.count}`);
    console.log(`평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
    console.log(`95 백분위 응답 시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
    console.log(`실패율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);
    console.log('========================================');

    return {
        'stdout': textSummary(data, { indent: ' ', enableColors: true }),
        'summary.json': JSON.stringify(data),
    };
}

// k6 내장 텍스트 요약 함수 (import가 안되면 주석 처리)
import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';
