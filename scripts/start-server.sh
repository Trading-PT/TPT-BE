#!/bin/bash
set -e

echo "============= 서버 배포 시작 ============="

# 작업 디렉토리 이동
cd /home/ubuntu/tpt-server-dev

# 배포 정보 로드
if [ -f deployment-info.env ]; then
    source deployment-info.env
    echo "✅ 배포 정보 로드 완료"
    echo "ECR Registry: $ECR_REGISTRY"
    echo "Repository: $ECR_REPOSITORY"
    echo "Image Tag: $IMAGE_TAG"
    echo "AWS Region: $AWS_REGION"
else
    echo "❌ deployment-info.env 파일을 찾을 수 없습니다!"
    exit 1
fi

# ECR 로그인
echo "ECR 로그인 중..."
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $ECR_REGISTRY

# Parameter Store에서 환경변수 가져오기
echo "Parameter Store에서 환경변수 가져오는 중..."

ENV_FILE="/tmp/app.env"
rm -f $ENV_FILE

# Parameter Store에서 환경변수 추출
aws ssm get-parameters-by-path \
  --path "/tpt-api/dev/" \
  --recursive \
  --with-decryption \
  --region $AWS_REGION \
  --query 'Parameters[*].[Name,Value]' \
  --output text | \
while IFS=$'\t' read -r name value; do
  # /tpt-api/dev/VARIABLE_NAME -> VARIABLE_NAME 형태로 변환
  env_name=$(echo "$name" | sed 's|^/tpt-api/dev/||')
  echo "$env_name=$value" >> $ENV_FILE
done

# Spring Boot 기본 설정 추가
echo "SPRING_PROFILES_ACTIVE=dev" >> $ENV_FILE

# Parameter Store에서 가져온 환경변수 개수 확인
if [ -f $ENV_FILE ]; then
    param_count=$(wc -l < $ENV_FILE)
    echo "✅ Parameter Store에서 $param_count 개의 환경변수를 가져왔습니다."

    # 환경변수 파일 내용 확인 (민감정보 제외)
    echo "📋 로드된 환경변수 목록:"
    grep -v -E "(PASSWORD|SECRET|KEY)" $ENV_FILE | cut -d'=' -f1 | sed 's/^/  - /' || true
else
    echo "❌ 환경변수 파일 생성에 실패했습니다!"
    exit 1
fi

# 최신 Docker 이미지 Pull
echo "최신 Docker 이미지 가져오는 중..."
docker pull $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

# Spring Boot 애플리케이션 실행
echo "Spring Boot 애플리케이션 시작 중..."
docker run -d \
  --name tpt-spring-app \
  --env-file $ENV_FILE \
  -p 8080:8080 \
  --memory="700m" \
  --memory-swap="1g" \
  --restart unless-stopped \
  --log-driver json-file \
  --log-opt max-size=100m \
  --log-opt max-file=3 \
  $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG

# 환경변수 파일 보안 삭제
rm -f $ENV_FILE

echo "✅ 컨테이너 시작 완료"

# 컨테이너 상태 확인
sleep 5
if docker ps | grep -q tpt-spring-app; then
    echo "✅ 컨테이너가 정상적으로 실행 중입니다."
    docker ps | grep tpt-spring-app
else
    echo "❌ 컨테이너 시작에 실패했습니다."
    echo "컨테이너 로그:"
    docker logs tpt-spring-app || true
    exit 1
fi

echo "============= 서버 배포 완료 ============="