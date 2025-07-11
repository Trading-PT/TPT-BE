#!/bin/bash
# -e: 명령 실패 시 즉시 스크립트 종료
set -e

# 애플리케이션 디렉토리로 이동
cd /home/ubuntu/app

# .env 파일이 존재하는 경우 환경변수로 로드
# -f: 파일 존재 여부 확인
if [ -f .env ]; then
    # export: 환경변수로 설정
    # cat .env | xargs: .env 파일의 각 라인을 환경변수로 변환
    export $(cat .env | xargs)
fi

# ECR 로그인 과정
echo "Logging into ECR..."
# aws ecr get-login-password: ECR 로그인용 토큰 생성
# --region: AWS 리전 지정
# docker login: Docker Hub가 아닌 ECR에 로그인
# --username AWS: ECR은 항상 AWS를 사용자명으로 사용
# --password-stdin: 비밀번호를 표준 입력으로 받음 (보안)
aws ecr get-login-password --region ap-northeast-2 | docker login --username AWS --password-stdin $ECR_REGISTRY

# Docker 이미지 다운로드
echo "Pulling Docker image..."
# ECR에서 지정된 태그의 이미지 다운로드
docker pull $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

# Docker Compose로 컨테이너 시작
echo "Starting containers..."
# -f: 사용할 compose 파일 지정
# up: 컨테이너 생성 및 시작
# -d: 백그라운드 실행 (detached mode)
docker-compose -f docker-compose-dev.yml up -d

# 컨테이너 로그 출력 (디버깅용)
echo "Container logs:"
# timeout 30: 30초 동안만 로그 출력
# logs -f: 실시간 로그 스트리밍
# || true: 타임아웃이 발생해도 스크립트 계속 실행
timeout 30 docker-compose -f docker-compose-dev.yml logs -f || true

# 완료 메시지
echo "Application started successfully"