#!/bin/bash
# -e: 명령 실패 시 즉시 스크립트 종료
set -e

# 진행 상황 출력
echo "Validating application health..."

# 헬스체크 설정
# 최대 시도 횟수 (30번 = 약 5분)
MAX_ATTEMPTS=30
# 현재 시도 횟수
ATTEMPT=0

# 헬스체크 루프
while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
    # 시도 횟수 증가
    ATTEMPT=$((ATTEMPT + 1))

    # 헬스체크 엔드포인트 호출
    # curl -f: 실패 시 에러 코드 반환
    # 2>/dev/null: 에러 메시지 숨김
    if curl -f http://localhost:8080/actuator/health 2>/dev/null; then
        # 헬스체크 성공
        echo ""
        echo "Application is healthy!"

        # 추가 정보 확인 (애플리케이션 버전 등)
        echo "Checking application info..."
        # -s: 진행 상황 표시 안 함
        # || true: 실패해도 계속 진행
        curl -s http://localhost:8080/actuator/info || true

        # 성공적으로 종료
        exit 0
    fi

    # 실패 시 대기 후 재시도
    echo "Health check attempt $ATTEMPT/$MAX_ATTEMPTS..."
    # 10초 대기
    sleep 10
done

# 모든 시도가 실패한 경우
echo "Health check failed after $MAX_ATTEMPTS attempts!"
# 실패 코드로 종료
exit 1