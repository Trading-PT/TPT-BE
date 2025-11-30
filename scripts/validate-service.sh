#!/bin/bash
set -e

echo "============= 서비스 검증 시작 ============="

# 컨테이너 실행 상태 확인
echo "컨테이너 상태 확인 중..."
if ! docker ps | grep -q tpt-spring-app; then
    echo "❌ 컨테이너가 실행되지 않았습니다."
    docker logs tpt-spring-app || true
    exit 1
fi

# 애플리케이션 시작 대기 (JVM 워밍업 고려)
echo "애플리케이션 시작 대기 중..."
sleep 60

# Health Check
echo "애플리케이션 Health Check 시작..."
MAX_RETRIES=45  # 45회 * 10초 = 7분 30초
for i in $(seq 1 $MAX_RETRIES); do
    # Health Check 응답 상세 로깅
    http_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>&1)

    if [ "$http_code" = "200" ]; then
        echo "✅ 애플리케이션이 정상적으로 실행 중입니다!"

        # Health Check 상세 정보
        health_response=$(curl -s http://localhost:8080/actuator/health 2>/dev/null || echo '{"status":"UNKNOWN"}')
        echo "Health Status: $health_response"

        exit 0
    fi

    echo "⏳ Health Check 대기 중... ($i/$MAX_RETRIES, HTTP: $http_code)"
    sleep 10
done

echo "❌ Health Check 실패!"
echo "최근 애플리케이션 로그:"
docker logs --tail 50 tpt-spring-app || true

exit 1