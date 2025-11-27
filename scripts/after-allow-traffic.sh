#!/bin/bash

echo "============= [Blue/Green] 트래픽 전환 완료 ============="

# 이 스크립트는 트래픽이 Green 환경으로 전환된 후 실행됩니다.
# 배포 성공 로깅 및 모니터링 목적입니다.

# 현재 시간 기록
DEPLOY_TIME=$(date '+%Y-%m-%d %H:%M:%S')
echo "배포 완료 시간: $DEPLOY_TIME"

# 컨테이너 상태 출력
echo ""
echo "=== 현재 컨테이너 상태 ==="
docker ps --filter "name=tpt-spring-app" --format "table {{.ID}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"

# 메모리 사용량 출력
echo ""
echo "=== 리소스 사용량 ==="
docker stats --no-stream tpt-spring-app --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}" 2>/dev/null || echo "통계 조회 실패"

# Health Check 상태
echo ""
echo "=== Health Check 상태 ==="
curl -s http://localhost:8080/actuator/health 2>/dev/null || echo '{"status":"UNKNOWN"}'

echo ""
echo "============= ✅ Blue/Green 배포 성공 ============="
echo "트래픽이 새 환경(Green)으로 전환되었습니다."
echo "기존 환경(Blue)은 설정된 시간 후 자동 종료됩니다."
