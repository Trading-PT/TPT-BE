/**
 * TPT-API 스모크 테스트 (k6)
 *
 * 부하테스트 전 기본 동작 확인용
 * 적은 수의 사용자로 빠르게 테스트
 *
 * 실행:
 *   k6 run smoke-test.js
 *
 * 환경변수 지정:
 *   BASE_URL=https://your-api.com LOADTEST_SECRET=your-secret k6 run smoke-test.js
 */

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'https://dev.tradingpt.kr';
const LOADTEST_SECRET = __ENV.LOADTEST_SECRET || 'tpt-loadtest-secret-2024';

export const options = {
    vus: 5,           // 5명의 가상 사용자
    duration: '30s',  // 30초 동안 실행
    thresholds: {
        http_req_duration: ['p(95)<1000'], // 95%가 1초 이내
        http_req_failed: ['rate<0.1'],      // 실패율 10% 미만
    },
};

function getAuthHeaders(userId) {
    return {
        'Content-Type': 'application/json',
        'X-Load-Test-Auth': LOADTEST_SECRET,
        'X-Load-Test-User-Id': String(userId),
        'X-Load-Test-Username': `smoke_test_user_${userId}`,
        'X-Load-Test-Role': 'ROLE_CUSTOMER',
    };
}

export default function () {
    const userId = __VU;
    const headers = getAuthHeaders(userId);

    // 1. Health Check
    let res = http.get(`${BASE_URL}/actuator/health`);
    check(res, {
        'health check is 200': (r) => r.status === 200,
    });

    // 2. 인증된 API 테스트 (메모 조회)
    res = http.get(`${BASE_URL}/api/v1/memo`, { headers });
    check(res, {
        'memo API accessible': (r) => r.status === 200 || r.status === 404,
    });

    // 3. 사용자 정보 조회
    res = http.get(`${BASE_URL}/api/v1/auth/me`, { headers });
    check(res, {
        'user me API accessible': (r) => r.status === 200,
    });

    sleep(1);
}

export function handleSummary(data) {
    console.log('\n=== 스모크 테스트 결과 ===');
    console.log(`상태: ${data.metrics.http_req_failed.values.rate < 0.1 ? '✅ PASS' : '❌ FAIL'}`);
    console.log(`총 요청: ${data.metrics.http_reqs.values.count}`);
    console.log(`평균 응답시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
    console.log(`실패율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);

    return {};
}
