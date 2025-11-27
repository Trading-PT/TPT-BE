#!/bin/bash
set -e

echo "============= [Blue/Green] 트래픽 허용 전 최종 검증 ============="

# 이 스크립트는 Green 환경에서 트래픽을 받기 직전에 실행됩니다.
# ValidateService가 성공한 후, ALB가 트래픽을 전환하기 전 마지막 검증입니다.

# 1. 컨테이너 실행 상태 재확인
echo "1. 컨테이너 상태 최종 확인..."
if ! docker ps | grep -q tpt-spring-app; then
    echo "❌ 컨테이너가 실행되지 않았습니다."
    exit 1
fi
echo "✅ 컨테이너 실행 중"

# 2. Health Check 재확인
echo "2. Health Check 최종 확인..."
HEALTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health 2>/dev/null || echo "000")

if [ "$HEALTH_RESPONSE" != "200" ]; then
    echo "❌ Health Check 실패! HTTP 상태: $HEALTH_RESPONSE"
    exit 1
fi
echo "✅ Health Check 통과 (HTTP 200)"

# 3. 메모리 상태 확인
echo "3. 컨테이너 리소스 상태 확인..."
docker stats --no-stream tpt-spring-app --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}" || true

# 4. 애플리케이션 정보 출력
echo "4. 애플리케이션 정보..."
curl -s http://localhost:8080/actuator/info 2>/dev/null || echo "{}"

echo ""
echo "============= ✅ 트래픽 허용 준비 완료 ============="
echo "Green 환경이 트래픽을 받을 준비가 되었습니다."
