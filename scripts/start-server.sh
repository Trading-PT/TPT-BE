#!/bin/bash
set -e

echo "============= 서버 배포 시작 ============="

# 배포 정보 로드 (먼저 환경 확인을 위해)
# CodeDeploy agent의 작업 디렉토리가 아닌 appspec.yml에 정의된 배포 경로 사용
DEPLOY_DIR="/home/ubuntu/tpt-server"
echo "배포 디렉토리: $DEPLOY_DIR"

if [ -f "$DEPLOY_DIR/deployment-info.env" ]; then
    source "$DEPLOY_DIR/deployment-info.env"
    echo "✅ 배포 정보 로드 완료"
    echo "ECR Registry: $ECR_REGISTRY"
    echo "Repository: $ECR_REPOSITORY"
    echo "Image Tag: $IMAGE_TAG"
    echo "AWS Region: $AWS_REGION"
    echo "Spring Profile: ${SPRING_PROFILES_ACTIVE:-dev}"
else
    echo "❌ deployment-info.env 파일을 찾을 수 없습니다!"
    exit 1
fi

# 환경 감지 (SPRING_PROFILES_ACTIVE로 결정)
PROFILE="${SPRING_PROFILES_ACTIVE:-dev}"
echo "🔧 환경: $PROFILE"

# 환경별 설정
if [ "$PROFILE" = "prod" ]; then
    SSM_PATH="/tpt-api/prod/"
    LOG_GROUP="/tpt/prod/application"
else
    SSM_PATH="/tpt-api/dev/"
    LOG_GROUP="/tpt/dev/application"
fi

echo "SSM 경로: $SSM_PATH"
echo "로그 그룹: $LOG_GROUP"

# ECR 로그인
echo "ECR 로그인 중..."
aws ecr get-login-password --region $AWS_REGION | \
  docker login --username AWS --password-stdin $ECR_REGISTRY

# Parameter Store에서 환경변수 가져오기
echo "Parameter Store에서 환경변수 가져오는 중..."

ENV_FILE="/tmp/app.env"
rm -f $ENV_FILE

# Parameter Store에서 환경변수 추출 (환경별 경로 사용)
# Process Substitution 사용 (서브쉘 문제 방지)
while IFS=$'\t' read -r name value; do
  # /tpt-api/{env}/VARIABLE_NAME -> VARIABLE_NAME 형태로 변환
  env_name=$(echo "$name" | sed "s|^$SSM_PATH||")
  echo "$env_name=$value" >> $ENV_FILE
done < <(aws ssm get-parameters-by-path \
  --path "$SSM_PATH" \
  --recursive \
  --with-decryption \
  --region $AWS_REGION \
  --query 'Parameters[*].[Name,Value]' \
  --output text)

# Spring Boot 프로파일 설정 추가
echo "SPRING_PROFILES_ACTIVE=$PROFILE" >> $ENV_FILE

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

# 컨테이너 실행 (환경별 로그 그룹 사용)
docker run -d \
  --name tpt-spring-app \
  --env-file $ENV_FILE \
  -p 8080:8080 \
  --memory="700m" \
  --memory-swap="1g" \
  --restart unless-stopped \
  --log-driver awslogs \
  --log-opt awslogs-group="$LOG_GROUP" \
  --log-opt awslogs-stream="tpt-spring-app-$(date +%Y%m%d)" \
  --log-opt awslogs-region="ap-northeast-2" \
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