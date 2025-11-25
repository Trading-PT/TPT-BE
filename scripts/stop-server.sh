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

# 중지된 컨테이너 삭제 (재배포를 위해 필수)
if docker ps -a | grep -q tpt-spring-app; then
    echo "기존 컨테이너 삭제 중..."
    docker rm tpt-spring-app
    echo "✅ 컨테이너 삭제 완료"
fi

echo "✅ 서버 중지 완료"