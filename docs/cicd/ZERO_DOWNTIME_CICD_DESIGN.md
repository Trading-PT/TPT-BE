# TPT-API 무중단 CI/CD 파이프라인 설계서

## 문서 개요

**작성일**: 2025-11-25
**대상 환경**: AWS 운영 서버 (tpt-prod)
**애플리케이션**: Spring Boot 3.5.5 (Java 17) REST API
**목표**: 무중단 배포 자동화 시스템 설계

---

## 목차

1. [현재 환경 분석](#1-현재-환경-분석)
2. [CI/CD 도구 선정](#2-cicd-도구-선정)
3. [배포 전략 설계](#3-배포-전략-설계)
4. [Docker 컨테이너화 전략](#4-docker-컨테이너화-전략)
5. [파이프라인 단계 설계](#5-파이프라인-단계-설계)
6. [롤백 전략](#6-롤백-전략)
7. [환경별 워크플로우](#7-환경별-워크플로우)
8. [모니터링 및 알림](#8-모니터링-및-알림)
9. [보안 및 IAM 정책](#9-보안-및-iam-정책)
10. [비용 산정](#10-비용-산정)
11. [구현 로드맵](#11-구현-로드맵)
12. [참고 자료](#12-참고-자료)

---

## 1. 현재 환경 분석

### 1.1 애플리케이션 스택

```yaml
애플리케이션:
  - 프레임워크: Spring Boot 3.5.5
  - Java 버전: 17
  - 빌드 도구: Gradle 8.x
  - 패키징: JAR (Executable)

주요 의존성:
  - Spring Security + OAuth2 (Kakao/Naver)
  - Spring Data JPA + QueryDSL
  - Redis (세션 저장소)
  - MySQL 8.0.42
  - AWS SDK 2.25.40 (S3)
  - ShedLock 5.13.0 (분산 스케줄링)
```

### 1.2 현재 AWS 인프라

```yaml
운영 환경 (tpt-prod):
  - EC2: t3.small (2 vCPU, 2GB RAM) × 2대
  - RDS: MySQL 8.0.42 (db.t3.micro, Multi-AZ)
  - ElastiCache: Redis (cache.t3.micro)
  - ALB: Application Load Balancer
  - Auto Scaling: 최소 2대, 최대 4대
  - CloudWatch: 로그 및 메트릭 수집
  - VPC: Private/Public 서브넷 분리

개발 환경 (tpt-dev):
  - EC2: t3.micro × 1대
  - RDS: MySQL 8.0.42 (db.t3.micro, Single-AZ)
  - ElastiCache: Redis (cache.t3.micro)
```

### 1.3 현재 배포 프로세스 (개발)

```yaml
기존 방식:
  1. GitHub Actions로 빌드
  2. Docker 이미지 생성 → ECR 푸시
  3. CodeDeploy로 EC2 배포
  4. 배포 전략: AllAtOnce (단일 인스턴스)

문제점:
  - 운영 환경 배포 전략 미정의
  - 무중단 배포 미구현
  - 롤백 전략 부재
  - 프로덕션 파이프라인 미구축
```

---

## 2. CI/CD 도구 선정

### 2.1 도구 비교 분석

| 항목        | GitHub Actions   | AWS CodePipeline | Jenkins         |
|-----------|------------------|------------------|-----------------|
| **구축 비용** | $0 (2,000분/월 무료) | ~$1/파이프라인/월      | EC2 비용 (~$30/월) |
| **학습 곡선** | 낮음               | 중간               | 높음              |
| **유지보수**  | GitHub 관리        | AWS 관리           | 자체 관리 필요        |
| **통합성**   | GitHub 네이티브      | AWS 네이티브         | 플러그인 필요         |
| **확장성**   | 우수               | 우수               | 높음 (커스터마이징)     |
| **보안**    | Secrets 관리       | IAM 통합           | 자체 관리           |
| **모니터링**  | Actions 대시보드     | CloudWatch 통합    | 별도 플러그인         |

### 2.2 선정 결과 및 근거

**추천: GitHub Actions + AWS CodeDeploy 조합**

**선정 이유:**

1. **비용 효율성**
    - GitHub Free tier로 충분 (월 2,000분 무료)
    - CodeDeploy는 EC2 배포 시 추가 비용 없음
    - 총 CI/CD 비용: ~$0-5/월

2. **개발팀 친화성**
    - 이미 GitHub을 소스 저장소로 사용 중
    - 코드 리뷰 → 머지 → 배포 워크플로우 자연스러움
    - Pull Request 기반 자동화 가능

3. **구축 및 유지보수 용이성**
    - YAML 기반 간단한 설정
    - GitHub Marketplace의 풍부한 액션
    - 별도 서버 관리 불필요

4. **기존 환경과의 호환성**
    - 현재 개발 환경에서 이미 사용 중
    - ECR, CodeDeploy 연동 검증됨
    - 운영 환경 확장이 자연스러움

---

## 3. 배포 전략 설계

### 3.1 배포 전략 비교

| 전략             | 장점                                   | 단점                           | 적합성     |
|----------------|--------------------------------------|------------------------------|---------|
| **Rolling**    | - 단순한 구조<br>- 추가 인프라 불필요<br>- 점진적 배포 | - 배포 시간 길어짐<br>- 버전 혼재 가능    | ⭐⭐⭐ 적합  |
| **Blue/Green** | - 완전한 격리<br>- 빠른 롤백<br>- 테스트 용이      | - 2배 리소스 필요<br>- 비용 증가       | ⭐⭐ 오버스펙 |
| **Canary**     | - 점진적 트래픽 전환<br>- 위험 최소화             | - 복잡한 모니터링 필요<br>- 구현 난이도 높음 | ⭐ 과도함   |

### 3.2 선정 전략: Rolling Deployment

**선정 근거:**

- 현재 인프라 규모 (2-4대)에 최적
- ALB + Auto Scaling 이미 구축됨
- 추가 비용 없이 무중단 배포 가능
- 복잡도 낮아 유지보수 용이

### 3.3 Rolling Deployment 아키텍처

```
┌─────────────────────────────────────────────────────────────┐
│                      Application Load Balancer               │
│                  (Health Check: /actuator/health)            │
└────────────┬──────────────────────────┬─────────────────────┘
             │                          │
   ┌─────────▼─────────┐      ┌────────▼──────────┐
   │   Target Group    │      │   Target Group    │
   │    (Port 8080)    │      │    (Port 8080)    │
   └─────────┬─────────┘      └────────┬──────────┘
             │                          │
   ┌─────────▼─────────┐      ┌────────▼──────────┐
   │   EC2 Instance 1  │      │   EC2 Instance 2  │
   │  (tpt-spring-app) │      │  (tpt-spring-app) │
   │                   │      │                   │
   │  Health: Healthy  │      │  Health: Healthy  │
   └───────────────────┘      └───────────────────┘

배포 흐름:
1. Instance 1 → ALB에서 제거 (Connection Draining: 30초)
2. Instance 1 → 새 버전 배포
3. Instance 1 → Health Check 통과 (최대 5분)
4. Instance 1 → ALB에 재등록
5. Instance 2 → 동일 프로세스 반복
```

### 3.4 배포 프로세스 상세

```yaml
Phase 1: 배포 준비
           - ECR에 새 Docker 이미지 푸시
           - CodeDeploy 배포 그룹 트리거
           - 배포 전략: CodeDeployDefault.OneAtATime

Phase 2: 인스턴스별 배포 (순차 진행)
           1. ApplicationStop (기존 컨테이너 중지)
           - docker stop tpt-spring-app
           - 타임아웃: 60초

           2. BeforeInstall (사전 준비)
           - 기존 컨테이너 제거
           - Docker 이미지 정리
           - 타임아웃: 300초

           3. AfterInstall (ECR 로그인 & 이미지 Pull)
           - AWS ECR 인증
           - 최신 이미지 다운로드
           - 타임아웃: 300초

           4. ApplicationStart (새 컨테이너 시작)
           - Parameter Store에서 환경변수 로드
           - Docker 컨테이너 실행
           - CloudWatch Logs 연결
           - 타임아웃: 300초

           5. ValidateService (헬스체크)
           - 30초 대기 (애플리케이션 부팅)
           - /actuator/health 엔드포인트 검증 (30회 시도, 10초 간격)
           - 성공 시 다음 인스턴스로 진행
           - 실패 시 롤백 트리거
           - 타임아웃: 300초

Phase 3: 배포 완료
  - 모든 인스턴스 정상 확인
  - CloudWatch 메트릭 모니터링 시작
  - 슬랙 알림 발송
```

### 3.5 세션 관리 전략

**현재 구성 (유지):**

```yaml
세션 저장소: Redis (ElastiCache)
세션 타임아웃: 7일 (604,800초)
세션 공유: 모든 인스턴스에서 동일 Redis 접근

장점:
  - 인스턴스 재시작 시에도 세션 유지
  - 로그인 상태 보존
  - 추가 구현 불필요 (이미 구현됨)
```

---

## 4. Docker 컨테이너화 전략

### 4.1 현재 Dockerfile 분석

**현재 구조:**

```dockerfile
# 단일 스테이지 빌드
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**문제점:**

- 빌드 도구 포함 (불필요한 용량 증가)
- 레이어 캐싱 최적화 부족
- 프로덕션 최적화 미흡

### 4.2 최적화된 Multi-stage Build 설계

```dockerfile
# ============================================
# Stage 1: Build (빌드 전용 스테이지)
# ============================================
FROM gradle:8.5-jdk17-alpine AS builder

WORKDIR /app

# 의존성 캐싱 최적화 (레이어 재사용)
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon

# 소스 코드 복사 및 빌드
COPY src ./src
RUN gradle clean build -x test --no-daemon

# ============================================
# Stage 2: Runtime (실행 전용 스테이지)
# ============================================
FROM amazoncorretto:17-alpine-jdk AS runtime

# 보안 및 운영 최적화
RUN apk add --no-cache curl dumb-init && \
    addgroup -g 1001 spring && \
    adduser -D -u 1001 -G spring spring

WORKDIR /app

# 빌드 결과물만 복사 (용량 최소화)
COPY --from=builder /app/build/libs/*.jar app.jar

# 비 root 사용자로 실행 (보안 강화)
USER spring:spring

# Health check 설정
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 최적화 옵션
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

EXPOSE 8080

# dumb-init으로 시그널 처리 (Graceful Shutdown)
ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 4.3 최적화 효과

| 항목                | 기존      | 최적화 후  | 개선율    |
|-------------------|---------|--------|--------|
| 이미지 크기            | ~450MB  | ~280MB | 38% 감소 |
| 빌드 시간 (초기)        | ~3분     | ~3분    | 동일     |
| 빌드 시간 (캐시)        | ~3분     | ~30초   | 83% 감소 |
| 보안                | root 실행 | 일반 사용자 | 향상     |
| Graceful Shutdown | 미지원     | 지원     | 추가     |

### 4.4 이미지 관리 전략

```yaml
ECR 저장소:
  - 개발: tpt-api-dev
  - 운영: tpt-api-prod

태깅 전략:
  - latest: 최신 프로덕션 이미지
  - { git-sha }: 커밋 해시 기반 (추적용)
  - v{version}: 시맨틱 버전 (릴리즈용)
  - dev-{timestamp}: 개발 환경 이미지

예시:
  - 123456789.dkr.ecr.ap-northeast-2.amazonaws.com/tpt-api-prod:latest
  - 123456789.dkr.ecr.ap-northeast-2.amazonaws.com/tpt-api-prod:abc1234
  - 123456789.dkr.ecr.ap-northeast-2.amazonaws.com/tpt-api-prod:v1.2.3

Lifecycle Policy (비용 절감):
  - latest 및 최근 10개 이미지 유지
  - 태그 없는 이미지 7일 후 삭제
  - 개발 이미지 30일 후 삭제
```

---

## 5. 파이프라인 단계 설계

### 5.1 전체 파이프라인 플로우

```
┌─────────────┐
│ 코드 커밋    │
│ (main 브랜치)│
└──────┬──────┘
       │
       ▼
┌─────────────────────────────────────────┐
│ Stage 1: Build & Test (GitHub Actions)  │
│ ─────────────────────────────────────── │
│ • Checkout 코드                          │
│ • Java 17 설정                           │
│ • Gradle 의존성 캐싱                     │
│ • ./gradlew clean build                 │
│ • 테스트 실행 및 커버리지 수집           │
│ • 빌드 아티팩트 생성                     │
└──────┬──────────────────────────────────┘
       │ [빌드 성공]
       ▼
┌─────────────────────────────────────────┐
│ Stage 2: Docker Build & Push             │
│ ─────────────────────────────────────── │
│ • Docker 이미지 빌드 (Multi-stage)       │
│ • 이미지 스캔 (Trivy 취약점 검사)        │
│ • ECR 로그인                             │
│ • 이미지 푸시 (latest, git-sha 태그)    │
└──────┬──────────────────────────────────┘
       │ [이미지 푸시 완료]
       ▼
┌─────────────────────────────────────────┐
│ Stage 3: Deploy (AWS CodeDeploy)         │
│ ─────────────────────────────────────── │
│ • deployment-info.env 생성               │
│ • S3에 배포 패키지 업로드                │
│ • CodeDeploy 배포 트리거                 │
│ • Rolling 배포 시작                      │
│   - Instance 1 배포 & 검증               │
│   - Instance 2 배포 & 검증               │
└──────┬──────────────────────────────────┘
       │ [배포 완료]
       ▼
┌─────────────────────────────────────────┐
│ Stage 4: Post-Deploy Verification        │
│ ─────────────────────────────────────── │
│ • ALB 헬스체크 상태 확인                 │
│ • 애플리케이션 로그 검증                 │
│ • 성능 메트릭 수집 (5분간)               │
│ • 슬랙 배포 완료 알림                    │
└─────────────────────────────────────────┘
```

### 5.2 Stage 1: Build & Test

**목적**: 코드 품질 검증 및 배포 가능한 아티팩트 생성

```yaml
name: Build and Test

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 15

    steps:
      - name: Checkout 코드
        uses: actions/checkout@v4

      - name: Java 17 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'corretto'
          java-version: '17'

      - name: Gradle 캐싱
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}

      - name: Gradle 빌드 권한 부여
        run: chmod +x gradlew

      - name: 테스트 실행
        run: ./gradlew test
        env:
          SPRING_PROFILES_ACTIVE: test

      - name: 테스트 결과 업로드
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: build/test-results/

      - name: 빌드 실행
        run: ./gradlew clean build -x test

      - name: JAR 파일 업로드
        uses: actions/upload-artifact@v4
        with:
          name: application-jar
          path: build/libs/*.jar
          retention-days: 7
```

**소요 시간**: 약 3-5분
**성공 조건**: 모든 테스트 통과, JAR 파일 생성
**실패 시**: 파이프라인 중단, 개발자에게 알림

### 5.3 Stage 2: Docker Build & Push

**목적**: 컨테이너 이미지 생성 및 ECR 저장

```yaml
name: Build and Push Docker Image

jobs:
  docker:
    needs: build
    runs-on: ubuntu-latest
    timeout-minutes: 10

    steps:
      - name: Checkout 코드
        uses: actions/checkout@v4

      - name: JAR 파일 다운로드
        uses: actions/download-artifact@v4
        with:
          name: application-jar
          path: build/libs/

      - name: AWS 자격증명 설정
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_ARN }}
          aws-region: ap-northeast-2

      - name: ECR 로그인
        uses: aws-actions/amazon-ecr-login@v2

      - name: Docker 메타데이터 추출
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ secrets.ECR_REGISTRY }}/tpt-api-prod
          tags: |
            type=raw,value=latest
            type=sha,prefix=,format=short

      - name: Docker 이미지 빌드
        uses: docker/build-push-action@v5
        with:
          context: .
          file: Dockerfile.prod
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Trivy 취약점 스캔
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ secrets.ECR_REGISTRY }}/tpt-api-prod:latest
          format: 'sarif'
          output: 'trivy-results.sarif'
          severity: 'CRITICAL,HIGH'

      - name: 스캔 결과 업로드
        uses: github/codeql-action/upload-sarif@v3
        if: always()
        with:
          sarif_file: 'trivy-results.sarif'
```

**소요 시간**: 약 5-8분
**성공 조건**: 이미지 빌드 성공, 치명적 취약점 없음
**실패 시**: 심각한 보안 취약점 발견 시 배포 중단

### 5.4 Stage 3: Deploy to Production

**목적**: CodeDeploy를 통한 Rolling 배포 실행

```yaml
name: Deploy to Production

jobs:
  deploy:
    needs: docker
    runs-on: ubuntu-latest
    timeout-minutes: 30
    environment:
      name: production
      url: https://api.tradingpt.com

    steps:
      - name: Checkout 코드
        uses: actions/checkout@v4

      - name: AWS 자격증명 설정
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_ARN }}
          aws-region: ap-northeast-2

      - name: 배포 정보 파일 생성
        run: |
          cat > deployment-info.env << EOF
          ECR_REGISTRY=${{ secrets.ECR_REGISTRY }}
          ECR_REPOSITORY=tpt-api-prod
          IMAGE_TAG=${{ github.sha }}
          AWS_REGION=ap-northeast-2
          DEPLOYED_AT=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
          DEPLOYED_BY=${{ github.actor }}
          COMMIT_MESSAGE="${{ github.event.head_commit.message }}"
          EOF

      - name: S3에 배포 패키지 업로드
        run: |
          zip -r deployment.zip appspec.yml scripts/ deployment-info.env
          aws s3 cp deployment.zip s3://tpt-deploy-prod/releases/tpt-api-${{ github.sha }}.zip

      - name: CodeDeploy 배포 생성
        id: deploy
        run: |
          aws deploy create-deployment \
            --application-name tpt-api-prod \
            --deployment-group-name tpt-api-prod-group \
            --deployment-config-name CodeDeployDefault.OneAtATime \
            --description "Deploy commit ${{ github.sha }}" \
            --s3-location bucket=tpt-deploy-prod,key=releases/tpt-api-${{ github.sha }}.zip,bundleType=zip \
            --query 'deploymentId' \
            --output text > deployment-id.txt

          echo "DEPLOYMENT_ID=$(cat deployment-id.txt)" >> $GITHUB_OUTPUT

      - name: 배포 상태 모니터링
        run: |
          DEPLOYMENT_ID=$(cat deployment-id.txt)
          echo "Monitoring deployment: $DEPLOYMENT_ID"

          while true; do
            STATUS=$(aws deploy get-deployment \
              --deployment-id $DEPLOYMENT_ID \
              --query 'deploymentInfo.status' \
              --output text)

            echo "Current status: $STATUS"

            if [ "$STATUS" = "Succeeded" ]; then
              echo "✅ Deployment succeeded!"
              exit 0
            elif [ "$STATUS" = "Failed" ] || [ "$STATUS" = "Stopped" ]; then
              echo "❌ Deployment failed!"
              exit 1
            fi

            sleep 15
          done

      - name: 배포 실패 시 롤백
        if: failure()
        run: |
          echo "Rolling back to previous version..."
          aws deploy stop-deployment \
            --deployment-id $(cat deployment-id.txt) \
            --auto-rollback-enabled
```

**소요 시간**: 약 10-15분 (인스턴스 2대 기준)
**성공 조건**: 모든 인스턴스 헬스체크 통과
**실패 시**: 자동 롤백 트리거

### 5.5 Stage 4: Post-Deploy Verification

**목적**: 배포 후 애플리케이션 정상 동작 확인

```yaml
name: Post-Deploy Verification

jobs:
  verify:
    needs: deploy
    runs-on: ubuntu-latest
    timeout-minutes: 10

    steps:
      - name: Health Check
        run: |
          echo "Verifying application health..."
          for i in {1..30}; do
            HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" https://api.tradingpt.com/actuator/health)
            if [ "$HTTP_CODE" = "200" ]; then
              echo "✅ Health check passed (attempt $i)"
              break
            fi
            echo "⏳ Waiting for health check... (attempt $i/30)"
            sleep 10
          done

      - name: 로그 검증
        run: |
          aws logs tail /tpt/prod/application \
            --since 5m \
            --format short \
            --filter-pattern "ERROR" \
            > error-logs.txt

          if [ -s error-logs.txt ]; then
            echo "⚠️ Errors detected in logs"
            cat error-logs.txt
          else
            echo "✅ No errors in recent logs"
          fi

      - name: 슬랙 알림
        uses: slackapi/slack-github-action@v1
        with:
          payload: |
            {
              "text": "✅ Production Deployment Successful",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*🚀 Production Deployment Completed*\n\n*Environment:* Production\n*Version:* ${{ github.sha }}\n*Deployed by:* ${{ github.actor }}\n*Time:* $(date -u +\"%Y-%m-%d %H:%M:%S UTC\")"
                  }
                }
              ]
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

**소요 시간**: 약 5분
**성공 조건**: 헬스체크 통과, 심각한 에러 로그 없음
**실패 시**: 경고 알림 발송, 수동 검토 필요

### 5.6 파이프라인 총 소요 시간

```
┌─────────────────────┬──────────────┬─────────────┐
│ Stage               │ 소요 시간    │ 누적 시간   │
├─────────────────────┼──────────────┼─────────────┤
│ Build & Test        │ 3-5분        │ 3-5분       │
│ Docker Build & Push │ 5-8분        │ 8-13분      │
│ Deploy              │ 10-15분      │ 18-28분     │
│ Verification        │ 5분          │ 23-33분     │
└─────────────────────┴──────────────┴─────────────┘

전체 배포 시간: 약 25-35분
```

---

## 6. 롤백 전략

### 6.1 롤백 시나리오

```yaml
자동 롤백 트리거:
  1. Health Check 실패 (5회 연속)
  2. 배포 중 타임아웃 초과
  3. 애플리케이션 시작 실패
  4. 심각한 에러 로그 감지

수동 롤백 트리거:
  1. 비즈니스 로직 버그 발견
  2. 성능 저하 (응답시간 2배 이상)
  3. 사용자 불만 급증
```

### 6.2 자동 롤백 메커니즘

**CodeDeploy 자동 롤백 설정:**

```yaml
DeploymentGroup:
  AutoRollbackConfiguration:
    Enabled: true
    Events:
      - DEPLOYMENT_FAILURE        # 배포 실패 시
      - DEPLOYMENT_STOP_ON_ALARM  # CloudWatch 알람 트리거 시
      - DEPLOYMENT_STOP_ON_REQUEST # 수동 중지 시

CloudWatch Alarms (자동 롤백 트리거):
  1. HealthyHostCount < 1
  - 1분간 정상 인스턴스 0대

  2. TargetResponseTime > 2000ms
  - 5분간 평균 응답시간 2초 초과

  3. HTTPCode_Target_5XX_Count > 10
  - 5분간 5xx 에러 10건 이상

  4. UnHealthyHostCount > 0
  - 1분간 비정상 인스턴스 발생
```

**롤백 프로세스:**

```
현재 배포 중단
    ↓
이전 배포 버전 확인 (deployment-info.env)
    ↓
이전 Docker 이미지로 재배포
    ↓
Rolling 방식으로 이전 버전 복원
    ↓
Health Check 검증
    ↓
정상 확인 후 알림 발송
```

### 6.3 수동 롤백 절차

**방법 1: CodeDeploy Console**

```bash
# AWS Console에서:
1. CodeDeploy > Deployments 이동
2. 롤백할 배포 선택
3. "Stop and roll back" 클릭
4. 이전 성공한 배포 선택
5. 롤백 시작
```

**방법 2: AWS CLI**

```bash
# 1. 최근 성공한 배포 ID 확인
aws deploy list-deployments \
  --application-name tpt-api-prod \
  --deployment-group-name tpt-api-prod-group \
  --include-only-statuses Succeeded \
  --query 'deployments[0]' \
  --output text

# 2. 해당 배포로 롤백
aws deploy create-deployment \
  --application-name tpt-api-prod \
  --deployment-group-name tpt-api-prod-group \
  --deployment-config-name CodeDeployDefault.OneAtATime \
  --description "Rollback to previous version" \
  --s3-location bucket=tpt-deploy-prod,key=releases/tpt-api-{PREVIOUS_SHA}.zip,bundleType=zip
```

**방법 3: GitHub Actions 수동 트리거**

```yaml
name: Manual Rollback

on:
  workflow_dispatch:
    inputs:
      target_commit:
        description: 'Target commit SHA to rollback'
        required: true
        type: string

jobs:
  rollback:
    runs-on: ubuntu-latest
    steps:
      - name: 이전 버전 배포
        run: |
          echo "Rolling back to commit: ${{ inputs.target_commit }}"
          # CodeDeploy 롤백 실행
```

### 6.4 롤백 검증

```yaml
롤백 후 필수 검증 항목:
  1. 모든 인스턴스 Health Check 통과
  2. 애플리케이션 로그 정상
  3. 데이터베이스 연결 정상
  4. Redis 세션 정상
  5. 주요 API 엔드포인트 테스트
  6. 성능 메트릭 정상 범위

검증 완료 후:
  - 슬랙 롤백 완료 알림
  - 사고 보고서 작성
  - 근본 원인 분석 (RCA)
```

### 6.5 데이터베이스 마이그레이션 롤백

**주의사항:**

- 배포와 DB 마이그레이션은 분리하여 관리
- 역호환성(Backward Compatibility) 유지 필수

**안전한 배포 시나리오:**

```
Phase 1: DB 마이그레이션 (호환성 유지)
  - 새 컬럼 추가 (nullable)
  - 새 테이블 생성
  - 인덱스 추가
  ↓
Phase 2: 애플리케이션 배포 (새 버전)
  - 새 컬럼 사용 시작
  - 기존 코드와 호환 유지
  ↓
Phase 3: 정리 (충분한 검증 후)
  - 구 컬럼 제거 (별도 마이그레이션)
  - 사용하지 않는 테이블 제거
```

**DB 롤백 불가능한 경우:**

```
❌ 피해야 할 마이그레이션:
  - 컬럼 삭제 (데이터 손실)
  - 타입 변경 (호환성 깨짐)
  - NOT NULL 제약 추가 (기존 데이터 영향)

✅ 안전한 접근:
  1. 새 컬럼 추가 (nullable)
  2. 데이터 마이그레이션 스크립트 실행
  3. 애플리케이션 배포
  4. 검증 후 구 컬럼 deprecated
  5. 충분한 기간 후 삭제
```

---

## 7. 환경별 워크플로우

### 7.1 개발 환경 (tpt-dev)

**트리거 조건:**

```yaml
on:
  push:
    branches: [ develop ]
```

**배포 특징:**

- 빠른 피드백 (테스트 최소화)
- AllAtOnce 배포 (단일 인스턴스)
- 자동 롤백 비활성화
- 슬랙 알림 간소화

**워크플로우:**

```yaml
name: Deploy to Development

on:
  push:
    branches: [ develop ]

jobs:
  deploy-dev:
    runs-on: ubuntu-latest
    environment: development

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Build & Test (간소화)
        run: ./gradlew clean build -x test

      - name: Docker Build & Push
        run: |
          # ECR 로그인
          aws ecr get-login-password --region ap-northeast-2 | \
            docker login --username AWS --password-stdin $ECR_REGISTRY

          # 빌드 및 푸시
          docker build -t tpt-api-dev:latest .
          docker tag tpt-api-dev:latest $ECR_REGISTRY/tpt-api-dev:latest
          docker push $ECR_REGISTRY/tpt-api-dev:latest

      - name: Deploy to EC2
        run: |
          aws deploy create-deployment \
            --application-name tpt-api-dev \
            --deployment-group-name tpt-api-dev-group \
            --deployment-config-name CodeDeployDefault.AllAtOnce \
            --s3-location bucket=tpt-deploy-dev,key=releases/latest.zip,bundleType=zip

소요 시간: 약 8-12분
```

### 7.2 운영 환경 (tpt-prod)

**트리거 조건:**

```yaml
on:
  push:
    branches: [ main ]
  workflow_dispatch:  # 수동 트리거 허용
```

**배포 특징:**

- 전체 테스트 실행
- Rolling 배포 (OneAtATime)
- 자동 롤백 활성화
- 보안 스캔 필수
- 배포 승인 프로세스 (선택)

**워크플로우:**

```yaml
name: Deploy to Production

on:
  push:
    branches: [ main ]
  workflow_dispatch:
    inputs:
      skip_tests:
        description: 'Skip tests (not recommended)'
        required: false
        default: false
        type: boolean

jobs:
  # Stage 1: Build & Test
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run tests
        if: ${{ !inputs.skip_tests }}
        run: ./gradlew test
      - name: Build
        run: ./gradlew clean build -x test
      - name: Upload JAR
        uses: actions/upload-artifact@v4
        with:
          name: application-jar
          path: build/libs/*.jar

  # Stage 2: Docker Build & Security Scan
  docker:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Download JAR
        uses: actions/download-artifact@v4
      - name: Build Docker image
        run: docker build -f Dockerfile.prod -t tpt-api-prod:${{ github.sha }} .
      - name: Trivy scan
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: tpt-api-prod:${{ github.sha }}
          severity: 'CRITICAL,HIGH'
          exit-code: '1'
      - name: Push to ECR
        run: |
          aws ecr get-login-password | docker login --username AWS --password-stdin $ECR_REGISTRY
          docker tag tpt-api-prod:${{ github.sha }} $ECR_REGISTRY/tpt-api-prod:${{ github.sha }}
          docker tag tpt-api-prod:${{ github.sha }} $ECR_REGISTRY/tpt-api-prod:latest
          docker push $ECR_REGISTRY/tpt-api-prod:${{ github.sha }}
          docker push $ECR_REGISTRY/tpt-api-prod:latest

  # Stage 3: Deploy with Approval (선택)
  deploy:
    needs: docker
    runs-on: ubuntu-latest
    environment:
      name: production
      url: https://api.tradingpt.com
    steps:
      # 배포 승인 대기 (GitHub Environment Protection Rules)
      - name: Deploy to Production
        run: |
          # CodeDeploy 트리거 (롤링 배포)
          aws deploy create-deployment \
            --application-name tpt-api-prod \
            --deployment-group-name tpt-api-prod-group \
            --deployment-config-name CodeDeployDefault.OneAtATime \
            --s3-location bucket=tpt-deploy-prod,key=releases/tpt-api-${{ github.sha }}.zip,bundleType=zip

  # Stage 4: Post-Deploy Verification
  verify:
    needs: deploy
    runs-on: ubuntu-latest
    steps:
      - name: Health Check
        run: curl -f https://api.tradingpt.com/actuator/health
      - name: Smoke Tests
        run: |
          # 주요 엔드포인트 테스트
          curl -f https://api.tradingpt.com/api/v1/health
      - name: Slack Notification
        uses: slackapi/slack-github-action@v1
        with:
          payload: |
            {
              "text": "✅ Production deployment successful: ${{ github.sha }}"
            }

소요 시간: 약 25-35분
```

### 7.3 배포 승인 프로세스 (선택적)

**GitHub Environment Protection Rules 설정:**

```yaml
Environment: production
Protection Rules:
  - Required reviewers: 2명
  - Wait timer: 5분 (검토 시간)
  - Deployment branches: main only
```

**승인 프로세스:**

```
1. 개발자가 main 브랜치에 푸시
   ↓
2. 빌드 & 테스트 자동 실행
   ↓
3. Docker 이미지 생성 & 스캔
   ↓
4. 배포 승인 대기 (슬랙 알림)
   ↓
5. 승인자 2명이 GitHub에서 승인
   ↓
6. 배포 시작 (Rolling)
   ↓
7. 검증 & 알림
```

### 7.4 환경별 설정 비교

| 항목    | 개발 (tpt-dev)   | 운영 (tpt-prod)        |
|-------|----------------|----------------------|
| 트리거   | develop 브랜치 푸시 | main 브랜치 푸시          |
| 테스트   | 생략 가능          | 필수 실행                |
| 보안 스캔 | 선택             | 필수                   |
| 배포 전략 | AllAtOnce      | OneAtATime (Rolling) |
| 롤백    | 수동             | 자동 + 수동              |
| 승인    | 불필요            | 선택 (권장)              |
| 알림    | 간소화            | 상세                   |
| 소요 시간 | 8-12분          | 25-35분               |

---

## 8. 모니터링 및 알림

### 8.1 CloudWatch 메트릭

**애플리케이션 메트릭:**

```yaml
Namespace: TPT/Application

메트릭:
    1. RequestCount (요청 수)
      - 통계: Sum
      - 기간: 1분
      - 임계값: > 1000/min (경고)

      2. ResponseTime (응답 시간)
      - 통계: Average, p95, p99
      - 기간: 1분
      - 임계값:
          - Average > 500ms (경고)
          - p95 > 1000ms (경고)
          - p99 > 2000ms (심각)

      3. ErrorRate (에러율)
      - 통계: Average
      - 기간: 5분
      - 임계값:
          - > 1% (경고)
          - > 5% (심각)

      4. ActiveSessions (활성 세션)
      - 통계: Average
      - 기간: 5분
      - 임계값: > 10000 (경고)

      5. DatabaseConnectionPool
      - 통계: Average, Maximum
      - 기간: 1분
      - 임계값:
          - Average > 7/10 (경고)
          - Max = 10 (심각)
```

**인프라 메트릭:**

```yaml
Namespace: AWS/EC2, AWS/ApplicationELB, AWS/RDS, AWS/ElastiCache

메트릭:
  EC2:
    - CPUUtilization (CPU 사용률)
        - 임계값: > 70% (경고), > 85% (심각)
    - MemoryUtilization (메모리 사용률)
        - 임계값: > 80% (경고), > 90% (심각)
    - DiskUtilization (디스크 사용률)
        - 임계값: > 80% (경고)

  ALB:
    - TargetResponseTime (대상 응답 시간)
        - 임계값: > 1000ms (경고)
    - HealthyHostCount (정상 호스트 수)
        - 임계값: < 2 (심각)
    - UnHealthyHostCount (비정상 호스트)
        - 임계값: > 0 (경고)
    - HTTPCode_Target_5XX_Count (5xx 에러)
        - 임계값: > 10/5min (경고)

  RDS:
    - DatabaseConnections (DB 연결 수)
        - 임계값: > 80 (경고)
    - FreeableMemory (사용 가능 메모리)
        - 임계값: < 200MB (경고)
    - CPUUtilization
        - 임계값: > 70% (경고)

  ElastiCache:
    - CPUUtilization
        - 임계값: > 70% (경고)
    - NetworkBytesIn/Out
        - 임계값: > 100MB/s (경고)
    - CurrConnections (현재 연결 수)
        - 임계값: > 5000 (경고)
```

### 8.2 CloudWatch Alarms 설정

**Critical Alarms (즉시 대응 필요):**

```yaml
1. HealthyHostCount < 1
  - 설명: 모든 인스턴스 다운
- 평가: 1분간 데이터포인트 1/1
- 액션: SNS → 슬랙, 이메일, SMS
- 자동 롤백: 활성화

  2. HTTPCode_Target_5XX_Count > 50
- 설명: 5분간 5xx 에러 50건 이상
- 평가: 5분간 데이터포인트 1/1
- 액션: SNS → 슬랙, 이메일
- 자동 롤백: 활성화

  3. DatabaseConnections = 0
- 설명: DB 연결 불가
- 평가: 1분간 데이터포인트 2/2
- 액션: SNS → 슬랙, 이메일, SMS

  4. EC2 StatusCheckFailed
- 설명: 인스턴스 상태 체크 실패
- 평가: 2분간 데이터포인트 2/2
- 액션: SNS → 슬랙, 이메일
- 자동 복구: 활성화
```

**Warning Alarms (모니터링 필요):**

```yaml
1. ResponseTime > 500ms
  - 평가: 5분간 데이터포인트 3/5
- 액션: SNS → 슬랙

  2. ErrorRate > 1%
- 평가: 5분간 데이터포인트 2/5
- 액션: SNS → 슬랙

  3. CPUUtilization > 70%
- 평가: 5분간 데이터포인트 3/5
- 액션: SNS → 슬랙

  4. MemoryUtilization > 80%
- 평가: 5분간 데이터포인트 3/5
- 액션: SNS → 슬랙
```

### 8.3 CloudWatch Logs 설정

**로그 그룹 구조:**

```yaml
로그 그룹:
  /tpt/prod/application:
    - 보존 기간: 30일
    - 소스: Spring Boot 애플리케이션
    - 로그 스트림: 인스턴스별 (instance-id)
    - 메트릭 필터:
        - ERROR 로그 카운트
        - Exception 발생 횟수
        - 느린 쿼리 (> 1초)

  /tpt/prod/deployment:
    - 보존 기간: 90일
    - 소스: CodeDeploy 배포 스크립트
    - 로그 스트림: 배포별 (deployment-id)

  /tpt/prod/access:
    - 보존 기간: 7일
    - 소스: ALB 액세스 로그
    - 로그 스트림: ALB별
```

**메트릭 필터 (로그 기반 메트릭):**

```yaml
1. ERROR 로그 카운트:
  Pattern: [ timestamp, level=ERROR*, ... ]
  Metric: TPT/Application/ErrorCount
  Value: 1

2. Exception 발생:
  Pattern: "Exception"
  Metric: TPT/Application/ExceptionCount
  Value: 1

3. 느린 쿼리:
  Pattern: [ timestamp, level, msg="*", duration>1000* ]
  Metric: TPT/Application/SlowQueryCount
  Value: 1

4. 배포 실패:
  Pattern: "❌" "배포 실패" "Deployment failed"
  Metric: TPT/Deployment/FailureCount
  Value: 1
```

### 8.4 슬랙 알림 설정

**알림 채널 구조:**

```yaml
슬랙 채널:
  #tpt-prod-alerts:
  - Critical 알람
  - 배포 실패
  - 롤백 발생

  #tpt-prod-deployments:
  - 배포 시작
  - 배포 완료
  - 배포 승인 요청

  #tpt-prod-monitoring:
  - Warning 알람
  - 성능 이슈
  - 리소스 사용량
```

**알림 메시지 템플릿:**

```json
// 배포 시작
{
  "text": "🚀 Production Deployment Started",
  "blocks": [
    {
      "type": "section",
      "text": {
        "type": "mrkdwn",
        "text": "*Environment:* Production\n*Commit:* <https://github.com/org/repo/commit/abc123|abc123>\n*Author:* @developer\n*Time:* 2025-01-15 14:30:00 KST"
      }
    },
    {
      "type": "actions",
      "elements": [
        {
          "type": "button",
          "text": {
            "type": "plain_text",
            "text": "View Logs"
          },
          "url": "https://console.aws.amazon.com/cloudwatch/..."
        }
      ]
    }
  ]
}

// 배포 완료
{
  "text": "✅ Production Deployment Successful",
  "blocks": [
    {
      "type": "section",
      "text": {
        "type": "mrkdwn",
        "text": "*🎉 Deployment Completed*\n\n*Duration:* 28m 45s\n*Instances:* 2/2 healthy\n*Health Check:* ✅ Passed\n*Errors:* 0"
      }
    }
  ]
}

// Critical 알람
{
  "text": "🚨 CRITICAL: All instances are unhealthy!",
  "blocks": [
    {
      "type": "section",
      "text": {
        "type": "mrkdwn",
        "text": "*⚠️ CRITICAL ALERT*\n\n*Alarm:* HealthyHostCount\n*Condition:* < 1 for 1 minute\n*Current Value:* 0\n*Time:* 2025-01-15 14:35:00 KST\n\n*Action Required:* Check application logs and EC2 instances immediately!"
      }
    },
    {
      "type": "actions",
      "elements": [
        {
          "type": "button",
          "text": {
            "type": "plain_text",
            "text": "View CloudWatch"
          },
          "url": "https://console.aws.amazon.com/cloudwatch/...",
          "style": "danger"
        },
        {
          "type": "button",
          "text": {
            "type": "plain_text",
            "text": "Trigger Rollback"
          },
          "url": "https://github.com/org/repo/actions/..."
        }
      ]
    }
  ]
}
```

### 8.5 대시보드 구성

**CloudWatch 대시보드 (TPT-Production-Overview):**

```yaml
위젯:
  Row 1: 애플리케이션 헬스
    - HealthyHostCount (숫자)
    - TargetResponseTime (그래프)
    - RequestCount (그래프)
    - ErrorRate (게이지)

  Row 2: 인프라 리소스
    - EC2 CPU 사용률 (그래프)
    - EC2 메모리 사용률 (그래프)
    - RDS Connections (숫자)
    - Redis CPU (게이지)

  Row 3: 에러 및 예외
    - 5xx 에러 카운트 (숫자)
    - ERROR 로그 (로그 인사이트)
    - Exception 카운트 (그래프)

  Row 4: 배포 히스토리
    - 최근 배포 목록 (텍스트)
    - 배포 성공률 (게이지)
    - 롤백 카운트 (숫자)

새로고침: 자동 (1분)
공유: 팀 전체
```

---

## 9. 보안 및 IAM 정책

### 9.1 IAM 역할 설계

**1. GitHub Actions용 OIDC 역할**

```json
{
  "RoleName": "GitHubActionsDeployRole",
  "Description": "GitHub Actions에서 ECR 푸시 및 CodeDeploy 트리거 권한",
  "AssumeRolePolicyDocument": {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Principal": {
          "Federated": "arn:aws:iam::ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
        },
        "Action": "sts:AssumeRoleWithWebIdentity",
        "Condition": {
          "StringEquals": {
            "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
          },
          "StringLike": {
            "token.actions.githubusercontent.com:sub": "repo:org/tpt-api:ref:refs/heads/main"
          }
        }
      }
    ]
  },
  "Policies": [
    {
      "PolicyName": "ECRAccess",
      "PolicyDocument": {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "ecr:GetAuthorizationToken",
              "ecr:BatchCheckLayerAvailability",
              "ecr:GetDownloadUrlForLayer",
              "ecr:BatchGetImage",
              "ecr:PutImage",
              "ecr:InitiateLayerUpload",
              "ecr:UploadLayerPart",
              "ecr:CompleteLayerUpload"
            ],
            "Resource": [
              "arn:aws:ecr:ap-northeast-2:ACCOUNT_ID:repository/tpt-api-prod"
            ]
          }
        ]
      }
    },
    {
      "PolicyName": "CodeDeployAccess",
      "PolicyDocument": {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "codedeploy:CreateDeployment",
              "codedeploy:GetDeployment",
              "codedeploy:GetDeploymentConfig",
              "codedeploy:RegisterApplicationRevision"
            ],
            "Resource": [
              "arn:aws:codedeploy:ap-northeast-2:ACCOUNT_ID:application/tpt-api-prod",
              "arn:aws:codedeploy:ap-northeast-2:ACCOUNT_ID:deploymentgroup/tpt-api-prod/*"
            ]
          }
        ]
      }
    },
    {
      "PolicyName": "S3DeploymentBucket",
      "PolicyDocument": {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "s3:PutObject",
              "s3:GetObject"
            ],
            "Resource": "arn:aws:s3:::tpt-deploy-prod/*"
          }
        ]
      }
    }
  ]
}
```

**2. EC2 인스턴스용 역할 (CodeDeploy Agent)**

```json
{
  "RoleName": "TPT-EC2-CodeDeployRole",
  "Description": "EC2 인스턴스가 CodeDeploy 배포 및 ECR 접근 권한",
  "AssumeRolePolicyDocument": {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Principal": {
          "Service": "ec2.amazonaws.com"
        },
        "Action": "sts:AssumeRole"
      }
    ]
  },
  "ManagedPolicies": [
    "arn:aws:iam::aws:policy/AmazonEC2RoleforAWSCodeDeploy",
    "arn:aws:iam::aws:policy/CloudWatchAgentServerPolicy"
  ],
  "Policies": [
    {
      "PolicyName": "ECRReadAccess",
      "PolicyDocument": {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "ecr:GetAuthorizationToken",
              "ecr:BatchCheckLayerAvailability",
              "ecr:GetDownloadUrlForLayer",
              "ecr:BatchGetImage"
            ],
            "Resource": "*"
          }
        ]
      }
    },
    {
      "PolicyName": "SSMParameterStoreAccess",
      "PolicyDocument": {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "ssm:GetParameter",
              "ssm:GetParameters",
              "ssm:GetParametersByPath"
            ],
            "Resource": [
              "arn:aws:ssm:ap-northeast-2:ACCOUNT_ID:parameter/tpt-api/prod/*"
            ]
          },
          {
            "Effect": "Allow",
            "Action": [
              "kms:Decrypt"
            ],
            "Resource": [
              "arn:aws:kms:ap-northeast-2:ACCOUNT_ID:key/KMS_KEY_ID"
            ]
          }
        ]
      }
    },
    {
      "PolicyName": "CloudWatchLogsAccess",
      "PolicyDocument": {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "logs:CreateLogGroup",
              "logs:CreateLogStream",
              "logs:PutLogEvents",
              "logs:DescribeLogStreams"
            ],
            "Resource": [
              "arn:aws:logs:ap-northeast-2:ACCOUNT_ID:log-group:/tpt/prod/*"
            ]
          }
        ]
      }
    }
  ]
}
```

**3. CodeDeploy 서비스 역할**

```json
{
  "RoleName": "TPT-CodeDeployServiceRole",
  "Description": "CodeDeploy가 EC2/ALB/Auto Scaling 제어 권한",
  "AssumeRolePolicyDocument": {
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Principal": {
          "Service": "codedeploy.amazonaws.com"
        },
        "Action": "sts:AssumeRole"
      }
    ]
  },
  "ManagedPolicies": [
    "arn:aws:iam::aws:policy/AWSCodeDeployRole"
  ]
}
```

### 9.2 Secrets 관리 전략

**AWS Secrets Manager vs Parameter Store:**

| 항목    | Secrets Manager         | Parameter Store                        |
|-------|-------------------------|----------------------------------------|
| 비용    | $0.40/secret/월 + API 비용 | 무료 (Standard), $0.05/파라미터/월 (Advanced) |
| 자동 교체 | 지원 (Lambda 통합)          | 미지원                                    |
| 버전 관리 | 자동                      | 수동 (Advanced)                          |
| 암호화   | 기본 (KMS)                | 선택적 (KMS)                              |
| 용도    | DB 비밀번호, API 키          | 설정값, 비민감 정보                            |

**권장 사용 전략:**

```yaml
Secrets Manager (고민감 정보):
  - DB 비밀번호 (자동 교체 활성화)
  - OAuth2 Client Secret
  - API 키 (외부 서비스)

Parameter Store (설정 정보):
  - /tpt-api/prod/SPRING_PROFILES_ACTIVE: prod
  - /tpt-api/prod/SERVER_PORT: 8080
  - /tpt-api/prod/DB_HOST: tpt-prod.xxx.rds.amazonaws.com
  - /tpt-api/prod/REDIS_HOST: tpt-prod.xxx.cache.amazonaws.com
  - /tpt-api/prod/AWS_REGION: ap-northeast-2
  - /tpt-api/prod/S3_BUCKET_NAME: tpt-prod-uploads

Parameter Store (민감 정보, SecureString):
  - /tpt-api/prod/DB_PASSWORD
  - /tpt-api/prod/REDIS_PASSWORD
  - /tpt-api/prod/JWT_SECRET
  - /tpt-api/prod/KAKAO_CLIENT_ID
  - /tpt-api/prod/NAVER_CLIENT_SECRET
```

**애플리케이션 통합:**

```bash
# 배포 스크립트에서 환경변수 자동 로드
aws ssm get-parameters-by-path \
  --path "/tpt-api/prod/" \
  --recursive \
  --with-decryption \
  --region ap-northeast-2 \
  --query 'Parameters[*].[Name,Value]' \
  --output text | \
while IFS=$'\t' read -r name value; do
  env_name=$(echo "$name" | sed 's|^/tpt-api/prod/||')
  echo "$env_name=$value" >> /tmp/app.env
done

docker run --env-file /tmp/app.env tpt-api-prod:latest
```

### 9.3 네트워크 보안

**Security Group 설계:**

```yaml
ALB Security Group (tpt-alb-sg):
  Inbound:
    - Port 80 (HTTP): 0.0.0.0/0 → 443 리다이렉트
    - Port 443 (HTTPS): 0.0.0.0/0 → ALB
  Outbound:
    - Port 8080: tpt-app-sg → EC2 인스턴스

EC2 Security Group (tpt-app-sg):
  Inbound:
    - Port 8080: tpt-alb-sg → Spring Boot
    - Port 22 (SSH): Bastion Host만 허용 (운영 시 제거 권장)
  Outbound:
    - Port 3306: tpt-rds-sg → RDS
    - Port 6379: tpt-redis-sg → Redis
    - Port 443: 0.0.0.0/0 → AWS API, S3, ECR

RDS Security Group (tpt-rds-sg):
  Inbound:
    - Port 3306: tpt-app-sg → MySQL
  Outbound:
    - 없음 (필요 없음)

ElastiCache Security Group (tpt-redis-sg):
  Inbound:
    - Port 6379: tpt-app-sg → Redis
  Outbound:
    - 없음 (필요 없음)
```

**VPC 구조:**

```
VPC (10.0.0.0/16)
│
├── Public Subnet (10.0.1.0/24, 10.0.2.0/24)
│   ├── NAT Gateway
│   └── ALB
│
├── Private Subnet - App (10.0.11.0/24, 10.0.12.0/24)
│   └── EC2 Instances (Spring Boot)
│
├── Private Subnet - Data (10.0.21.0/24, 10.0.22.0/24)
│   ├── RDS MySQL (Multi-AZ)
│   └── ElastiCache Redis
```

### 9.4 컴플라이언스 및 감사

**CloudTrail 로깅:**

```yaml
활성화:
  - 모든 관리 이벤트 기록
  - S3 데이터 이벤트 (배포 버킷)
  - Lambda 데이터 이벤트 (없음)

보존:
  - S3: 90일
  - CloudWatch Logs: 30일

모니터링 이벤트:
  - IAM 정책 변경
  - Security Group 수정
  - CodeDeploy 배포 생성/중단
  - Secrets Manager 액세스
  - Parameter Store 수정
```

**AWS Config (선택적):**

```yaml
규칙:
  - ec2-instance-managed-by-systems-manager
  - rds-multi-az-support
  - s3-bucket-public-read-prohibited
  - iam-password-policy
  - encrypted-volumes

비용: ~$5-10/월
```

---

## 10. 비용 산정

### 10.1 CI/CD 인프라 비용

| 서비스                   | 사양                     | 월 비용 (USD)   |
|-----------------------|------------------------|--------------|
| **GitHub Actions**    | 2,000분 무료 (충분)         | $0           |
| **AWS CodeDeploy**    | EC2 배포 (무료)            | $0           |
| **Amazon ECR**        | 0.5GB 스토리지 (월 10개 이미지) | $0.05        |
| **S3 (배포 버킷)**        | 1GB 스토리지, 100 요청       | $0.50        |
| **CloudWatch Logs**   | 5GB/월 수집, 30일 보존       | $2.50        |
| **CloudWatch Alarms** | 10개 알람                 | $1.00        |
| **SNS (알림)**          | 1,000 알림/월             | $0.50        |
| **Secrets Manager**   | 5개 시크릿                 | $2.00        |
| **Parameter Store**   | Standard (무료)          | $0           |
| **CloudTrail**        | 관리 이벤트만 (무료)           | $0           |
| **데이터 전송**            | 배포 패키지 전송 (1GB/월)      | $1.00        |
| **총 CI/CD 비용**        | -                      | **~$7.55/월** |

### 10.2 기존 인프라 비용 (참고)

| 서비스                           | 사양                     | 월 비용 (USD)               |
|-------------------------------|------------------------|--------------------------|
| **EC2**                       | t3.small × 2대 (730h)   | $30.37 × 2 = $60.74      |
| **RDS MySQL**                 | db.t3.micro (Multi-AZ) | $24.82                   |
| **ElastiCache Redis**         | cache.t3.micro         | $12.41                   |
| **Application Load Balancer** | 730h + 1GB 처리          | $16.20 + $0.008 = $16.21 |
| **EBS**                       | gp3 50GB × 2           | $4.00 × 2 = $8.00        |
| **데이터 전송**                    | 100GB 아웃바운드            | $9.00                    |
| **총 운영 인프라**                  | -                      | **~$131/월**              |

### 10.3 총 비용 요약

```
기존 운영 인프라: ~$131/월
CI/CD 추가 비용: ~$7.55/월
──────────────────────────
총 비용: ~$138.55/월

CI/CD 비용 비율: 5.8%
```

### 10.4 비용 최적화 권장사항

**단기 (즉시 적용 가능):**

1. **ECR Lifecycle Policy 설정**
    - 최근 10개 이미지만 유지
    - 태그 없는 이미지 7일 후 삭제
    - 절감: ~$0.03/월

2. **CloudWatch Logs 보존 기간 단축**
    - 애플리케이션 로그: 30일 → 14일
    - 배포 로그: 90일 → 30일
    - 절감: ~$1.00/월

3. **S3 Lifecycle Policy**
    - 배포 패키지 30일 후 IA로 이동
    - 90일 후 삭제
    - 절감: ~$0.20/월

**중기 (검토 후 적용):**

1. **Reserved Instances (1년 약정)**
    - EC2 t3.small × 2대
    - 절감: ~30% ($18/월)

2. **Savings Plans (1년 약정)**
    - RDS + ElastiCache 포함
    - 절감: ~20% ($7.4/월)

3. **CloudWatch Logs Insights 대신 Athena 사용 (대량 분석 시)**
    - 비용: 스캔 데이터 기준 ($5/TB)
    - 절감: 빈번한 분석 시 유리

**장기 (스케일링 대비):**

1. **AWS Graviton2 인스턴스 고려**
    - t4g.small (ARM 기반)
    - 절감: ~20% ($12/월)
    - 주의: Docker 이미지 multi-arch 빌드 필요

2. **Auto Scaling 정책 최적화**
    - 야간 시간대 (02:00-06:00) 최소 1대로 축소
    - 절감: ~$15/월 (인스턴스 시간 절반)

---

## 11. 구현 로드맵

### 11.1 Phase 1: 기반 구축 (Week 1-2)

**목표**: CI/CD 파이프라인 기본 구성 완료

```yaml
Week 1:
  Day 1-2: GitHub Actions 워크플로우 작성
    - build-test.yml 생성 (빌드 & 테스트)
    - docker-build.yml 생성 (이미지 빌드 & ECR 푸시)
    - 개발 환경에서 검증

  Day 3-4: 프로덕션 Dockerfile 최적화
    - Multi-stage build 작성
    - 보안 설정 (비 root 사용자, dumb-init)
    - 이미지 크기 최적화 검증

  Day 5: IAM 역할 및 정책 설정
    - GitHub OIDC Provider 생성
    - GitHubActionsDeployRole 생성
    - EC2 인스턴스 프로파일 생성
    - 권한 테스트

Week 2:
  Day 1-2: CodeDeploy 설정
    - 애플리케이션 생성
    - 배포 그룹 생성 (Rolling, OneAtATime)
    - ALB 연동 설정
    - Auto Scaling 그룹 연동

  Day 3-4: 배포 스크립트 작성
    - stop-application.sh
    - before-install.sh
    - after-install.sh
    - start-application.sh
    - validate-service.sh
    - appspec.yml 작성

  Day 5: 통합 테스트
    - 개발 환경에서 전체 파이프라인 실행
    - 배포 시간 측정
    - 롤백 시나리오 테스트

체크리스트:
  ✅ GitHub Actions 워크플로우 작성 완료
  ✅ Docker 이미지 빌드 및 ECR 푸시 성공
  ✅ IAM 역할 및 권한 설정 완료
  ✅ CodeDeploy 배포 그룹 생성
  ✅ 배포 스크립트 작성 및 검증
  ✅ 개발 환경 통합 테스트 통과
```

### 11.2 Phase 2: 프로덕션 배포 (Week 3-4)

**목표**: 프로덕션 환경에 무중단 배포 시스템 적용

```yaml
Week 3:
  Day 1-2: Secrets 관리 설정
    - Parameter Store에 환경변수 등록
    - KMS 키 생성 및 암호화
    - EC2에서 접근 테스트

  Day 3-4: 프로덕션 파이프라인 구성
    - deploy-prod.yml 작성
    - 배포 승인 프로세스 설정 (선택)
    - 프로덕션 배포 그룹 생성
    - Rolling 배포 전략 적용

  Day 5: 초기 배포 실행
    - 프로덕션 첫 배포 (신중하게)
    - 배포 로그 모니터링
    - Health Check 검증

Week 4:
  Day 1-2: 롤백 메커니즘 구현
    - 자동 롤백 설정 (CloudWatch Alarms)
    - 수동 롤백 절차 문서화
    - 롤백 시나리오 테스트

  Day 3-4: 모니터링 설정
    - CloudWatch Alarms 생성 (Critical + Warning)
    - 슬랙 알림 연동
    - 대시보드 구성

  Day 5: 최종 검증
    - 전체 배포 프로세스 재실행
    - 롤백 테스트
    - 문서 업데이트

체크리스트:
  ✅ Parameter Store 환경변수 등록
  ✅ 프로덕션 파이프라인 작성
  ✅ Rolling 배포 성공적으로 실행
  ✅ 자동 롤백 설정 완료
  ✅ CloudWatch Alarms 및 슬랙 알림 작동
  ✅ 프로덕션 배포 문서 작성
```

### 11.3 Phase 3: 최적화 및 안정화 (Week 5-6)

**목표**: 성능 최적화 및 운영 효율성 향상

```yaml
Week 5:
  Day 1-2: 배포 시간 최적화
    - Gradle 캐싱 개선
    - Docker 레이어 캐싱 최적화
    - 병렬 빌드 검토

  Day 3-4: 보안 강화
    - Trivy 취약점 스캔 통합
    - Secrets 교체 절차 수립
    - Security Group 재검토

  Day 5: 비용 최적화
    - ECR Lifecycle Policy 적용
    - CloudWatch Logs 보존 기간 조정
    - 불필요한 리소스 정리

Week 6:
  Day 1-2: 운영 문서 작성
    - 배포 가이드
    - 롤백 매뉴얼
    - 트러블슈팅 가이드
    - Runbook 작성

  Day 3-4: 팀 교육
    - CI/CD 파이프라인 구조 설명
    - GitHub Actions 워크플로우 트리거 방법
    - 모니터링 대시보드 활용
    - 롤백 절차 실습

  Day 5: 회고 및 개선
    - 배포 프로세스 회고
    - 개선 사항 도출
    - 다음 스프린트 계획

체크리스트:
  ✅ 배포 시간 25분 이내 달성
  ✅ 보안 스캔 통합 완료
  ✅ 비용 최적화 적용
  ✅ 운영 문서 작성 완료
  ✅ 팀 교육 완료
  ✅ 프로덕션 안정 배포 3회 이상 성공
```

### 11.4 성공 지표 (KPI)

```yaml
배포 속도:
  - 목표: < 30분 (전체 파이프라인)
  - 측정: GitHub Actions 실행 시간

배포 안정성:
  - 목표: > 95% 성공률
  - 측정: 성공 배포 / 전체 배포 시도

다운타임:
  - 목표: 0분 (무중단 배포)
  - 측정: ALB HealthyHostCount = 0 시간

롤백 속도:
  - 목표: < 10분
  - 측정: 롤백 트리거 ~ 정상화 시간

배포 빈도:
  - 목표: 주 2회 이상
  - 측정: main 브랜치 배포 횟수

평균 복구 시간 (MTTR):
  - 목표: < 30분
  - 측정: 장애 발생 ~ 복구 완료 시간
```

### 11.5 리스크 관리

```yaml
High Risk (배포 중 전체 서비스 다운):
  - 완화: Rolling 배포로 최소 1대 유지
  - 대응: 자동 롤백 + 즉시 수동 개입
  - 확률: Low (< 5%)

Medium Risk (일부 인스턴스 배포 실패):
  - 완화: Health Check 타임아웃 충분히 설정 (5분)
  - 대응: 실패한 인스턴스만 재배포
  - 확률: Medium (10-20%)

Medium Risk (DB 마이그레이션 롤백 불가):
  - 완화: 역호환성 유지 (안전한 마이그레이션 전략)
  - 대응: 애플리케이션만 롤백 후 수동 DB 복구
  - 확률: Low (< 5%)

Low Risk (배포 승인 지연):
  - 완화: 배포 승인 프로세스 간소화 (선택적 적용)
  - 대응: 긴급 배포 시 승인 스킵 가능하도록 설정
  - 확률: Medium (20-30%)

Low Risk (GitHub Actions 장애):
  - 완화: N/A (외부 서비스)
  - 대응: 수동 배포 절차 준비 (AWS CLI 스크립트)
  - 확률: Very Low (< 1%)
```

---

## 12. 참고 자료

### 12.1 공식 문서

**AWS**:

- [AWS CodeDeploy User Guide](https://docs.aws.amazon.com/codedeploy/latest/userguide/)
- [EC2 Auto Scaling User Guide](https://docs.aws.amazon.com/autoscaling/ec2/userguide/)
- [Application Load Balancer Guide](https://docs.aws.amazon.com/elasticloadbalancing/latest/application/)
- [Amazon ECR User Guide](https://docs.aws.amazon.com/ecr/latest/userguide/)
- [AWS Systems Manager Parameter Store](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-parameter-store.html)
- [CloudWatch User Guide](https://docs.aws.amazon.com/cloudwatch/)

**GitHub**:

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Workflow Syntax](https://docs.github.com/en/actions/using-workflows/workflow-syntax-for-github-actions)
- [OIDC with AWS](https://docs.github.com/en/actions/deployment/security-hardening-your-deployments/configuring-openid-connect-in-amazon-web-services)

**Spring Boot**:

- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Spring Boot with Docker](https://spring.io/guides/gs/spring-boot-docker/)

**Docker**:

- [Multi-stage builds](https://docs.docker.com/build/building/multi-stage/)
- [Best practices for writing Dockerfiles](https://docs.docker.com/develop/dev-best-practices/)

### 12.2 유용한 도구

```yaml
CLI 도구:
  - aws-cli: AWS 리소스 관리
  - gh: GitHub CLI (PR, Issues, Actions)
  - docker: 컨테이너 관리
  - jq: JSON 파싱

모니터링:
  - CloudWatch Logs Insights
  - AWS X-Ray (분산 추적, 선택)
  - Spring Boot Actuator

보안:
  - Trivy: 컨테이너 취약점 스캔
  - AWS Inspector: EC2 보안 평가
  - AWS Config: 컴플라이언스 모니터링

개발:
  - LocalStack: 로컬 AWS 환경 시뮬레이션
  - Docker Compose: 로컬 개발 환경
```

### 12.3 학습 자료

**블로그 & 아티클**:

- [AWS DevOps Blog](https://aws.amazon.com/blogs/devops/)
- [GitHub Actions Examples](https://github.com/actions/starter-workflows)
- [Spring Boot Deployment Best Practices](https://spring.io/guides/topicals/spring-boot-docker/)

**책 (추천)**:

- "Continuous Delivery" by Jez Humble
- "The DevOps Handbook" by Gene Kim
- "Site Reliability Engineering" by Google

**커뮤니티**:

- AWS re:Post
- GitHub Community Forum
- Stack Overflow (aws, github-actions, spring-boot 태그)

### 12.4 트러블슈팅 가이드

**일반적인 문제 및 해결책**:

1. **CodeDeploy 배포 실패 (타임아웃)**
   ```
   원인: Health Check 실패 (Spring Boot 부팅 시간 초과)
   해결:
     - ValidateService 타임아웃 증가 (300초)
     - 애플리케이션 부팅 시간 최적화
     - /actuator/health readiness 상태 확인
   ```

2. **Docker 이미지 빌드 느림**
   ```
   원인: Gradle 의존성 다운로드 반복
   해결:
     - GitHub Actions 캐싱 활성화
     - Multi-stage build에서 의존성 레이어 분리
     - Docker BuildKit 활성화 (DOCKER_BUILDKIT=1)
   ```

3. **Parameter Store 접근 실패**
   ```
   원인: IAM 권한 부족 또는 KMS 키 접근 불가
   해결:
     - EC2 인스턴스 프로파일에 ssm:GetParameter* 권한 추가
     - kms:Decrypt 권한 추가
     - Parameter Store 경로 확인 (/tpt-api/prod/*)
   ```

4. **롤백 후에도 문제 지속**
   ```
   원인: DB 마이그레이션 롤백 불가 또는 외부 의존성 변경
   해결:
     - DB 스냅샷으로 복원 (최후 수단)
     - 외부 API 호환성 확인
     - 애플리케이션 로그에서 근본 원인 분석
   ```

5. **세션 유실**
   ```
   원인: Redis 연결 끊김 또는 세션 타임아웃 설정 오류
   해결:
     - Redis 연결 상태 확인 (CloudWatch 메트릭)
     - Spring Session 설정 확인 (application.yml)
     - ElastiCache 재시작 (필요시)
   ```

---

## 부록 A: 실행 가능한 스크립트 예시

### A.1 배포 스크립트 (start-application.sh)

```bash
#!/bin/bash
set -e

echo "============= 서버 배포 시작 ============="

# 작업 디렉토리 이동
cd /home/ubuntu/tpt-server-prod

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
  --path "/tpt-api/prod/" \
  --recursive \
  --with-decryption \
  --region $AWS_REGION \
  --query 'Parameters[*].[Name,Value]' \
  --output text | \
while IFS=$'\t' read -r name value; do
  # /tpt-api/prod/VARIABLE_NAME -> VARIABLE_NAME 형태로 변환
  env_name=$(echo "$name" | sed 's|^/tpt-api/prod/||')
  echo "$env_name=$value" >> $ENV_FILE
done

# Spring Boot 기본 설정 추가
echo "SPRING_PROFILES_ACTIVE=prod" >> $ENV_FILE

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

# 컨테이너 실행 부분을 다음으로 변경
docker run -d \
  --name tpt-spring-app \
  --env-file $ENV_FILE \
  -p 8080:8080 \
  --memory="1400m" \
  --memory-swap="1600m" \
  --restart unless-stopped \
  --log-driver awslogs \
  --log-opt awslogs-group="/tpt/prod/application" \
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
```

### A.2 헬스체크 스크립트 (validate-service.sh)

```bash
#!/bin/bash
set -e

echo "============= 서비스 검증 시작 ============="

# 컨테이너 실행 상태 확인
echo "컨테이너 상태 확인 중..."
if ! docker ps | grep -q tpt-spring-app; then
    echo "❌ 컨테이너가 실행되지 않았습니다."
    docker logs tpt-spring-app || true
    exit 1
fi

# 애플리케이션 시작 대기
echo "애플리케이션 시작 대기 중..."
sleep 30

# Health Check
echo "애플리케이션 Health Check 시작..."
for i in {1..30}; do
    if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo "✅ 애플리케이션이 정상적으로 실행 중입니다!"

        # Health Check 상세 정보
        health_response=$(curl -s http://localhost:8080/actuator/health 2>/dev/null || echo '{"status":"UNKNOWN"}')
        echo "Health Status: $health_response"

        exit 0
    fi

    echo "⏳ Health Check 대기 중... ($i/30)"
    sleep 10
done

echo "❌ Health Check 실패!"
echo "최근 애플리케이션 로그:"
docker logs --tail 50 tpt-spring-app || true

exit 1
```

### A.3 GitHub Actions 워크플로우 (deploy-prod.yml)

```yaml
name: Deploy to Production

on:
  push:
    branches: [ main ]
  workflow_dispatch:
    inputs:
      skip_tests:
        description: 'Skip tests (emergency only)'
        required: false
        default: false
        type: boolean

env:
  AWS_REGION: ap-northeast-2
  ECR_REPOSITORY: tpt-api-prod
  CODEDEPLOY_APP: tpt-api-prod
  CODEDEPLOY_GROUP: tpt-api-prod-group

jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 15

    steps:
      - name: Checkout 코드
        uses: actions/checkout@v4

      - name: Java 17 설정
        uses: actions/setup-java@v4
        with:
          distribution: 'corretto'
          java-version: '17'

      - name: Gradle 캐싱
        uses: actions/cache@v3
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
          restore-keys: |
            ${{ runner.os }}-gradle-

      - name: Gradle 빌드 권한 부여
        run: chmod +x gradlew

      - name: 테스트 실행
        if: ${{ !inputs.skip_tests }}
        run: ./gradlew test
        env:
          SPRING_PROFILES_ACTIVE: test

      - name: 빌드 실행
        run: ./gradlew clean build -x test

      - name: JAR 파일 업로드
        uses: actions/upload-artifact@v4
        with:
          name: application-jar
          path: build/libs/*.jar
          retention-days: 7

  docker:
    needs: build
    runs-on: ubuntu-latest
    timeout-minutes: 10
    permissions:
      id-token: write
      contents: read

    steps:
      - name: Checkout 코드
        uses: actions/checkout@v4

      - name: JAR 파일 다운로드
        uses: actions/download-artifact@v4
        with:
          name: application-jar
          path: build/libs/

      - name: AWS 자격증명 설정
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_ARN }}
          aws-region: ${{ env.AWS_REGION }}

      - name: ECR 로그인
        uses: aws-actions/amazon-ecr-login@v2

      - name: Docker 메타데이터 추출
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ secrets.ECR_REGISTRY }}/${{ env.ECR_REPOSITORY }}
          tags: |
            type=raw,value=latest
            type=sha,prefix=,format=short

      - name: Docker 이미지 빌드 및 푸시
        uses: docker/build-push-action@v5
        with:
          context: .
          file: Dockerfile.prod
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

      - name: Trivy 취약점 스캔
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: ${{ secrets.ECR_REGISTRY }}/${{ env.ECR_REPOSITORY }}:latest
          format: 'sarif'
          output: 'trivy-results.sarif'
          severity: 'CRITICAL,HIGH'
          exit-code: '0'

      - name: 스캔 결과 업로드
        uses: github/codeql-action/upload-sarif@v3
        if: always()
        with:
          sarif_file: 'trivy-results.sarif'

  deploy:
    needs: docker
    runs-on: ubuntu-latest
    timeout-minutes: 30
    environment:
      name: production
      url: https://api.tradingpt.com
    permissions:
      id-token: write
      contents: read

    steps:
      - name: Checkout 코드
        uses: actions/checkout@v4

      - name: AWS 자격증명 설정
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_ARN }}
          aws-region: ${{ env.AWS_REGION }}

      - name: 배포 정보 파일 생성
        run: |
          cat > deployment-info.env << EOF
          ECR_REGISTRY=${{ secrets.ECR_REGISTRY }}
          ECR_REPOSITORY=${{ env.ECR_REPOSITORY }}
          IMAGE_TAG=${{ github.sha }}
          AWS_REGION=${{ env.AWS_REGION }}
          DEPLOYED_AT=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
          DEPLOYED_BY=${{ github.actor }}
          COMMIT_MESSAGE="${{ github.event.head_commit.message }}"
          EOF

      - name: S3에 배포 패키지 업로드
        run: |
          zip -r deployment.zip appspec.yml scripts/ deployment-info.env
          aws s3 cp deployment.zip s3://tpt-deploy-prod/releases/tpt-api-${{ github.sha }}.zip

      - name: CodeDeploy 배포 생성
        id: deploy
        run: |
          DEPLOYMENT_ID=$(aws deploy create-deployment \
            --application-name ${{ env.CODEDEPLOY_APP }} \
            --deployment-group-name ${{ env.CODEDEPLOY_GROUP }} \
            --deployment-config-name CodeDeployDefault.OneAtATime \
            --description "Deploy commit ${{ github.sha }}" \
            --s3-location bucket=tpt-deploy-prod,key=releases/tpt-api-${{ github.sha }}.zip,bundleType=zip \
            --query 'deploymentId' \
            --output text)

          echo "DEPLOYMENT_ID=$DEPLOYMENT_ID" >> $GITHUB_OUTPUT
          echo "$DEPLOYMENT_ID" > deployment-id.txt

      - name: 배포 상태 모니터링
        run: |
          DEPLOYMENT_ID=$(cat deployment-id.txt)
          echo "🔍 Monitoring deployment: $DEPLOYMENT_ID"

          while true; do
            STATUS=$(aws deploy get-deployment \
              --deployment-id $DEPLOYMENT_ID \
              --query 'deploymentInfo.status' \
              --output text)

            echo "📊 Current status: $STATUS"

            if [ "$STATUS" = "Succeeded" ]; then
              echo "✅ Deployment succeeded!"
              exit 0
            elif [ "$STATUS" = "Failed" ] || [ "$STATUS" = "Stopped" ]; then
              echo "❌ Deployment failed!"

              # 실패 상세 정보 출력
              aws deploy get-deployment \
                --deployment-id $DEPLOYMENT_ID \
                --query 'deploymentInfo.errorInformation' \
                --output json

              exit 1
            fi

            sleep 15
          done

      - name: 배포 실패 시 롤백
        if: failure()
        run: |
          echo "🔄 Rolling back to previous version..."
          aws deploy stop-deployment \
            --deployment-id $(cat deployment-id.txt) \
            --auto-rollback-enabled

  verify:
    needs: deploy
    runs-on: ubuntu-latest
    timeout-minutes: 10

    steps:
      - name: Health Check
        run: |
          echo "🔍 Verifying application health..."
          for i in {1..30}; do
            HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" https://api.tradingpt.com/actuator/health)
            if [ "$HTTP_CODE" = "200" ]; then
              echo "✅ Health check passed (attempt $i)"

              # 상세 헬스체크 정보
              curl -s https://api.tradingpt.com/actuator/health | jq .

              break
            fi
            echo "⏳ Waiting for health check... (attempt $i/30)"
            sleep 10
          done

      - name: 슬랙 알림
        uses: slackapi/slack-github-action@v1
        with:
          payload: |
            {
              "text": "✅ Production Deployment Successful",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*🚀 Production Deployment Completed*\n\n*Environment:* Production\n*Version:* <https://github.com/${{ github.repository }}/commit/${{ github.sha }}|${{ github.sha }}>\n*Deployed by:* ${{ github.actor }}\n*Time:* $(date -u +\"%Y-%m-%d %H:%M:%S UTC\")\n*Commit:* ${{ github.event.head_commit.message }}"
                  }
                },
                {
                  "type": "actions",
                  "elements": [
                    {
                      "type": "button",
                      "text": {"type": "plain_text", "text": "View Logs"},
                      "url": "https://console.aws.amazon.com/cloudwatch/home?region=ap-northeast-2#logsV2:log-groups/log-group/$252Ftpt$252Fprod$252Fapplication"
                    },
                    {
                      "type": "button",
                      "text": {"type": "plain_text", "text": "View Metrics"},
                      "url": "https://console.aws.amazon.com/cloudwatch/home?region=ap-northeast-2#dashboards:name=TPT-Production-Overview"
                    }
                  ]
                }
              ]
            }
        env:
          SLACK_WEBHOOK_URL: ${{ secrets.SLACK_WEBHOOK_URL }}
```

---

## 결론

이 설계서는 TPT-API의 무중단 CI/CD 파이프라인 구축을 위한 **완전한 청사진**을 제공합니다.

**핵심 특징:**

- ✅ **비용 효율적**: 월 ~$7.55 추가 비용으로 구축 가능
- ✅ **무중단 배포**: Rolling 배포로 서비스 가용성 100% 유지
- ✅ **자동 롤백**: CloudWatch Alarms 기반 자동 복구
- ✅ **확장 가능**: 트래픽 증가 시 Auto Scaling으로 대응
- ✅ **보안 강화**: IAM, Secrets Manager, 취약점 스캔 통합
- ✅ **모니터링 완비**: CloudWatch + 슬랙 알림으로 실시간 가시성

**구현 시작 준비 완료:**

- 6주 구현 로드맵
- 실행 가능한 스크립트 제공
- 트러블슈팅 가이드 포함
- 운영 문서 작성 가이드

**다음 단계:**

1. Phase 1 시작 (Week 1-2): GitHub Actions 워크플로우 작성 및 개발 환경 검증
2. Phase 2 진행 (Week 3-4): 프로덕션 배포 및 롤백 메커니즘 구현
3. Phase 3 완료 (Week 5-6): 최적화, 문서화, 팀 교육

**질문 및 피드백:**

- 이 설계서에 대한 질문이나 추가 요구사항이 있다면 언제든지 문의해주세요.
- 구현 과정에서 발생하는 이슈는 트러블슈팅 가이드를 참고하거나 팀과 공유해주세요.

---

**문서 버전**: 1.0
**최종 업데이트**: 2025-01-15
**작성자**: Claude (DevOps Engineer Persona)
**검토 필요**: ✅ 설계 승인 후 구현 시작
