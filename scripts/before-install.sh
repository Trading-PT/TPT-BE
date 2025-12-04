#!/bin/bash

echo "============= Before Install 시작 ============="

# 기존 컨테이너 정리
echo "기존 컨테이너 정리 중..."
docker stop tpt-spring-app || true
docker rm tpt-spring-app || true

# 사용하지 않는 Docker 이미지 정리 (-a: 태그 있는 미사용 이미지도 삭제)
echo "Docker 이미지 정리 중..."
docker image prune -af || true

echo "✅ Before Install 완료"