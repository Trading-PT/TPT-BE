#!/bin/bash
# -e: 명령 실패 시 즉시 스크립트 종료
set -e

# 진행 상황 출력
echo "Loading environment variables from Parameter Store..."

# Parameter Store에서 파라미터 값을 가져오는 함수
# $1: 파라미터 이름 (예: /dev/db/name)
get_parameter() {
    # aws ssm get-parameter: Parameter Store에서 값 조회
    # --with-decryption: SecureString 타입 파라미터 복호화
    # --query: JMESPath로 필요한 값만 추출
    # --output text: JSON이 아닌 일반 텍스트로 출력
    aws ssm get-parameter --name "$1" --with-decryption --query 'Parameter.Value' --output text
}

# .env 파일 생성 (Docker Compose에서 사용)
# EOF까지의 내용을 .env 파일로 저장
cat > /home/ubuntu/app/.env << EOF
# 데이터베이스 설정
DB_NAME=$(get_parameter "/dev/db/name")
DB_USER=$(get_parameter "/dev/db/user")
DB_PASSWORD=$(get_parameter "/dev/db/password")

# Redis 설정
REDIS_PASSWORD=$(get_parameter "/dev/redis/password")

# 보안 설정
JWT_SECRET=$(get_parameter "/dev/jwt/secret")

# ECR 설정
ECR_REGISTRY=$(get_parameter "/dev/ecr/registry")
ECR_REPOSITORY=$(get_parameter "/dev/ecr/repository")
IMAGE_TAG=$(get_parameter "/dev/image/tag")
EOF

# 완료 메시지
echo "Environment variables loaded successfully"