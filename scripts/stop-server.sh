#!/bin/bash

echo "============= 서버 중지 시작 ============="

# 컨테이너 graceful 종료
if docker ps | grep -q tpt-spring-app; then
    echo "Spring Boot 애플리케이션 종료 중..."
    docker stop tpt-spring-app
    echo "✅ 애플리케이션 종료 완료"
else
    echo "ℹ️ 실행 중인 컨테이너가 없습니다."
fi

echo "✅ 서버 중지 완료"