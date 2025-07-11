#!/bin/bash
# 스크립트 해석기 지정 (bash 사용)

# 애플리케이션 디렉토리로 이동
cd /home/ubuntu/app

# 실행 중인 모든 컨테이너 중지 및 제거
# || true: 명령이 실패해도 스크립트 계속 실행 (컨테이너가 없을 수 있음)
docker-compose down || true

# 사용하지 않는 Docker 리소스 정리
# -f: 확인 없이 강제 실행
# 중지된 컨테이너, 사용하지 않는 네트워크, 댕글링 이미지 제거
docker system prune -f || true