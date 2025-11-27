# S3 이미지 보안 시스템 설계 및 구현

> **Version**: 1.0.0
> **Last Updated**: 2025-01-26
> **Author**: TPT Development Team

---

## 📌 기술 키워드 (Technical Keywords)

| 카테고리 | 키워드 |
|---------|--------|
| **기능 유형** | `Security Enhancement`, `Infrastructure`, `Cloud Architecture` |
| **아키텍처** | `AWS S3`, `CloudFront CDN`, `Presigned URL`, `Origin Access Control` |
| **기술 스택** | `Spring Boot`, `AWS SDK 2.x`, `S3Presigner`, `CloudFront Signed URL` |
| **설계 패턴** | `Strategy Pattern`, `Factory Pattern`, `Facade Pattern` |
| **품질 속성** | `Security`, `Performance`, `Scalability`, `Cost Optimization` |
| **개발 방법론** | `Defense in Depth`, `Least Privilege`, `Zero Trust` |

---

> **작성일**: 2025년 01월
> **프로젝트**: TPT-API (Trading Platform API)
> **도메인**: AWS S3, CloudFront, Spring Boot Security
> **개발 기간**: 설계 단계 (구현 예정)

## 📋 목차

1. [프로젝트 배경](#1-프로젝트-배경)
2. [요구사항 분석](#2-요구사항-분석)
3. [기술적 도전 과제](#3-기술적-도전-과제)
4. [아키텍처 설계](#4-아키텍처-설계)
5. [핵심 구현](#5-핵심-구현)
6. [품질 보장](#6-품질-보장)
7. [성과 및 임팩트](#7-성과-및-임팩트)

---

## 1. 프로젝트 배경

### 비즈니스 니즈

- **배경**: 현재 S3에 업로드된 이미지의 URL을 데이터베이스에 직접 저장하고, 클라이언트에 그대로 전달하는 방식 사용. URL이 유출되면 누구나 이미지에 접근할 수 있는 보안 취약점 존재
- **목표**: 운영 환경에서 이미지 접근에 대한 보안 강화 및 접근 제어 구현
- **기대 효과**: 민감한 이미지(피드백 첨부파일, 결제 영수증 등) 보호, URL 유출 시에도 무단 접근 차단

### 현재 시스템 문제점

```
┌─────────────────────────────────────────────────────────────────┐
│                    현재 아키텍처 (보안 취약)                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   Client    ────▶    API Server    ────▶    S3 Bucket          │
│      │                    │                    │               │
│      │              S3 URL 저장 (DB)           │               │
│      │                    │                    │               │
│      │◀──── S3 Public URL 직접 전달 ────       │               │
│      │                                         │               │
│      └──────────── 직접 접근 가능 ─────────────▶│               │
│                   (URL 유출 시 보안 위협)                        │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

**보안 문제점**:
| 문제 | 위험도 | 설명 |
|------|--------|------|
| **URL 영구 노출** | 🔴 Critical | S3 URL이 유출되면 영구적으로 접근 가능 |
| **접근 제어 불가** | 🔴 Critical | 누가, 언제 접근했는지 제어/추적 불가 |
| **만료 시간 없음** | 🟠 High | URL에 유효 기간을 설정할 수 없음 |
| **권한 분리 불가** | 🟠 High | 사용자별 접근 권한 구분 불가능 |

### 기술 환경

- **기술 스택**: Spring Boot 3.5.x, AWS SDK 2.25.40, JPA/Hibernate
- **인프라**: AWS S3, (선택적) AWS CloudFront
- **현재 연동**: S3 파일 업로드, URL 저장 및 조회

---

## 2. 요구사항 분석

### 기능 요구사항 (Functional Requirements)

**FR-1**: Presigned URL 생성
- **설명**: 서버에서 임시 접근 가능한 서명된 URL 생성
- **우선순위**: Critical
- **수용 기준**: 지정된 만료 시간 후 URL 접근 불가

**FR-2**: 이미지 유형별 접근 정책
- **설명**: 공개/비공개 이미지 분류 및 차등 보안 적용
- **우선순위**: High
- **수용 기준**: 비공개 이미지는 Presigned URL로만 접근 가능

**FR-3**: CloudFront 통한 캐싱 및 보안 (선택)
- **설명**: 공개 이미지에 대한 CDN 캐싱 및 OAC 적용
- **우선순위**: Medium
- **수용 기준**: S3 직접 접근 차단, CloudFront 통해서만 접근

### 비기능 요구사항 (Non-Functional Requirements)

| 항목 | 요구사항 | 목표 수치 |
|------|----------|-----------|
| **성능** | Presigned URL 생성 시간 | < 50ms |
| **성능** | 이미지 로딩 시간 | < 500ms (P95) |
| **보안** | URL 만료 시간 | 설정 가능 (1분 ~ 7일) |
| **보안** | S3 직접 접근 | 완전 차단 (CloudFront 적용 시) |
| **가용성** | 이미지 서비스 가동률 | 99.9% |
| **비용** | 월간 전송 비용 | 최적화된 비용 구조 |

### 제약 사항 (Constraints)

- **기술적 제약**: 기존 S3 URL 저장 방식과의 하위 호환성 유지 필요
- **비즈니스 제약**: 운영 중단 없이 점진적 마이그레이션 필요
- **리소스 제약**: CloudFront 도입 시 추가 인프라 비용 고려

---

## 3. 기술적 도전 과제

### 주요 도전 과제

**도전 1**: 보안과 사용자 경험의 균형
- **문제**: 보안 강화 시 매번 새 URL 생성으로 응답 시간 증가
- **원인**: Presigned URL은 요청마다 서명 생성이 필요
- **해결 방향**: 이미지 유형별 차등 정책 적용 (공개/비공개 분리)

**도전 2**: 기존 시스템과의 호환성
- **문제**: 기존에 저장된 S3 URL들을 새 시스템으로 전환 필요
- **원인**: DB에 직접 S3 URL이 저장되어 있음
- **해결 방향**: 점진적 마이그레이션 전략, URL 변환 서비스 구현

**도전 3**: 비용 최적화
- **문제**: Presigned URL은 캐싱 불가, CloudFront는 추가 비용 발생
- **원인**: 각 방식의 특성에 따른 트레이드오프 존재
- **해결 방향**: 하이브리드 접근 (공개: CloudFront, 비공개: Presigned)

### 기술적 트레이드오프

| 선택지 A | vs | 선택지 B | 최종 선택 | 이유 |
|---------|-------|----------|-----------|------|
| Presigned URL만 사용 | vs | CloudFront만 사용 | **하이브리드** | 이미지 유형별 최적 방식 적용 |
| 짧은 만료 시간 (5분) | vs | 긴 만료 시간 (1시간) | **유형별 차등** | 민감도에 따른 만료 시간 설정 |
| S3 Public 유지 | vs | S3 Private 전환 | **Private 전환** | 보안 강화가 핵심 목표 |

---

## 4. 아키텍처 설계

### 4.1 Presigned URL 방식 (비공개 이미지용)

#### 동작 원리

```
┌─────────────────────────────────────────────────────────────────┐
│                    Presigned URL 아키텍처                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌─────────┐      ┌─────────────┐      ┌─────────────┐        │
│   │ Client  │─(1)─▶│ API Server  │─(2)─▶│   AWS S3    │        │
│   │         │      │             │      │             │        │
│   └─────────┘      └─────────────┘      └─────────────┘        │
│        │                  │                    │               │
│        │                  │◀──(3) 서명 생성────│               │
│        │◀───(4) Presigned URL 전달───│        │               │
│        │                  │                    │               │
│        │────────────(5) 직접 이미지 요청 ──────▶│               │
│        │◀───────────(6) 이미지 반환 ───────────│               │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

단계별 설명:
(1) 클라이언트가 이미지 URL 요청
(2) 서버가 AWS SDK로 Presigned URL 생성 요청
(3) AWS가 비밀키로 URL 서명 생성
(4) 임시 접근 가능한 서명된 URL 반환
(5) 클라이언트가 S3에 직접 요청 (서명 포함)
(6) S3가 서명 검증 후 이미지 제공
```

#### Presigned URL 구조

```
https://bucket-name.s3.ap-northeast-2.amazonaws.com/path/to/image.jpg
    ?X-Amz-Algorithm=AWS4-HMAC-SHA256
    &X-Amz-Credential=AKIAIOSFODNN7EXAMPLE/20250126/ap-northeast-2/s3/aws4_request
    &X-Amz-Date=20250126T120000Z
    &X-Amz-Expires=3600
    &X-Amz-SignedHeaders=host
    &X-Amz-Signature=abc123def456...
```

| 파라미터 | 설명 |
|---------|------|
| `X-Amz-Algorithm` | 서명 알고리즘 (AWS4-HMAC-SHA256) |
| `X-Amz-Credential` | AWS 자격 증명 정보 (Access Key ID + 날짜 + 리전 + 서비스) |
| `X-Amz-Date` | URL 생성 시각 (ISO 8601 형식) |
| `X-Amz-Expires` | 만료 시간 (초 단위, 최대 604,800초 = 7일) |
| `X-Amz-SignedHeaders` | 서명에 포함된 헤더 목록 |
| `X-Amz-Signature` | 암호화된 서명 값 |

#### 보안 특성

| 특성 | 설명 |
|------|------|
| **임시성** | 설정된 시간 후 자동 만료 (1분 ~ 7일) |
| **비밀키 보호** | 클라이언트에 AWS 자격 증명 절대 노출 안 함 |
| **세밀한 제어** | 객체별, 사용자별 접근 권한 동적 생성 가능 |
| **요청별 서명** | 매번 새로운 서명 생성으로 URL 재사용 방지 |
| **서버 검증** | S3가 모든 요청의 서명 유효성 검증 |

---

### 4.2 CloudFront 방식 (공개 이미지용)

#### 동작 원리

```
┌─────────────────────────────────────────────────────────────────┐
│                    CloudFront CDN 아키텍처                       │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│   ┌─────────┐      ┌─────────────┐      ┌─────────┐   ┌──────┐ │
│   │ Client  │─(1)─▶│ CloudFront  │─(2)─▶│  Edge   │──▶│  S3  │ │
│   │ (Seoul) │      │   (CDN)     │      │ Cache   │   │Origin│ │
│   └─────────┘      └─────────────┘      └─────────┘   └──────┘ │
│        │                  │                  │                  │
│        │                  │◀───(3) 캐시 확인─│                  │
│        │                  │                  │                  │
│        │          [캐시 히트 시]              │                  │
│        │◀────(4) 캐시된 이미지 즉시 반환─────│                  │
│        │                                                        │
│        │          [캐시 미스 시]                                 │
│        │                  │─────(5) Origin 요청────▶│          │
│        │                  │◀────(6) 이미지 반환────│           │
│        │◀────(7) 이미지 반환 + 캐싱──────────│                  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘

핵심 포인트:
- 전 세계 엣지 로케이션에서 이미지 캐싱
- 사용자와 가장 가까운 엣지 서버에서 응답
- Origin Access Control(OAC)로 S3 직접 접근 완전 차단
```

#### Origin Access Control (OAC) 설정

```yaml
# CloudFront → S3 직접 접근 차단 설정
S3 Bucket Policy:
  Version: "2012-10-17"
  Statement:
    - Sid: "AllowCloudFrontServicePrincipal"
      Effect: "Allow"
      Principal:
        Service: "cloudfront.amazonaws.com"
      Action: "s3:GetObject"
      Resource: "arn:aws:s3:::tpt-api-images/*"
      Condition:
        StringEquals:
          AWS:SourceArn: "arn:aws:cloudfront::ACCOUNT_ID:distribution/DISTRIBUTION_ID"
```

**OAC 효과**:
- S3 버킷에 직접 접근 **완전 차단**
- 오직 CloudFront를 통해서만 접근 가능
- S3 URL이 유출되어도 접근 불가능

#### CloudFront 보안 옵션

**옵션 1: Signed URL (서명된 URL)**
```
https://d111111abcdef8.cloudfront.net/path/image.jpg
    ?Expires=1706320800
    &Signature=ABC123...
    &Key-Pair-Id=APKAIAIOSFODNN7EXAMPLE
```
- 특정 리소스에 대한 임시 접근 권한
- RSA 키 쌍을 사용한 서명
- 단일 파일 접근 제어에 적합

**옵션 2: Signed Cookies (서명된 쿠키)**
```http
Set-Cookie: CloudFront-Policy=base64EncodedPolicy
Set-Cookie: CloudFront-Signature=signatureValue
Set-Cookie: CloudFront-Key-Pair-Id=APKAIAIOSFODNN7EXAMPLE
```
- 다수의 리소스에 대한 접근 권한
- 쿠키 기반으로 별도 URL 파라미터 불필요
- 전체 디렉토리 접근 제어에 적합

#### 캐싱 설정

| 설정 | 설명 | 권장값 |
|------|------|--------|
| **Default TTL** | 기본 캐시 유지 시간 | 86,400초 (1일) |
| **Max TTL** | 최대 캐시 유지 시간 | 31,536,000초 (1년) |
| **Min TTL** | 최소 캐시 유지 시간 | 0초 |
| **Cache Key** | 캐시 식별자 구성 요소 | 경로 + 쿼리스트링 |

---

### 4.3 방식 비교 분석

#### 보안 수준 비교

| 항목 | Presigned URL | CloudFront (OAC) | CloudFront (Signed) |
|------|---------------|------------------|---------------------|
| **URL 유출 위험** | 🟡 중간 (만료로 완화) | 🟢 낮음 | 🟢 매우 낮음 |
| **S3 직접 접근** | 🟡 가능 (서명 필요) | 🟢 불가 | 🟢 불가 |
| **IP 제한** | 🔴 불가 | 🟢 가능 | 🟢 가능 |
| **지역 제한** | 🔴 불가 | 🟢 가능 (Geo) | 🟢 가능 (Geo) |
| **세션 기반 인증** | 🔴 불가 | 🔴 불가 | 🟢 가능 (Cookie) |

#### 성능 비교

| 항목 | Presigned URL | CloudFront |
|------|---------------|------------|
| **첫 요청 지연** | 🟢 낮음 (직접 S3) | 🟡 중간 (캐시 미스 시) |
| **반복 요청 지연** | 🟡 일정 (항상 S3) | 🟢 매우 낮음 (캐시 히트) |
| **글로벌 접근** | 🔴 느림 (S3 리전 의존) | 🟢 빠름 (엣지 로케이션) |
| **대역폭 효율** | 🔴 낮음 (매번 S3) | 🟢 높음 (엣지 캐싱) |
| **동시 접속** | 🟡 S3 제한 적용 | 🟢 높은 확장성 |

#### 구현 복잡도 비교

| 항목 | Presigned URL | CloudFront |
|------|---------------|------------|
| **초기 설정** | 🟢 간단 (AWS SDK만) | 🔴 복잡 (CDN 설정 필요) |
| **키 관리** | 🟢 간단 (IAM 키) | 🟡 복잡 (키 쌍 관리) |
| **코드 변경** | 🟡 중간 | 🟢 낮음 (도메인만 변경) |
| **인프라 변경** | 🟢 없음 | 🔴 CloudFront 생성 |
| **유지보수** | 🟢 간단 | 🟡 중간 (캐시 무효화) |

#### 비용 비교 (월 100GB 전송, 10만 요청 기준)

| 구분 | Presigned URL | CloudFront (캐시 90%) |
|------|---------------|----------------------|
| S3 전송 | $9.00 (100GB) | $0.90 (10GB) |
| CloudFront 전송 | - | $8.10 (100GB) |
| CloudFront 요청 | - | $0.10 (100K) |
| **총 비용** | **~$9.00** | **~$9.10** |

> 💡 **참고**: 캐시 히트율이 높아질수록 CloudFront가 비용 효율적

---

### 4.4 TPT-API 권장 아키텍처 (하이브리드)

```
┌─────────────────────────────────────────────────────────────────┐
│                 TPT-API 이미지 보안 전략 (하이브리드)              │
├───────────────────────────┬─────────────────────────────────────┤
│    이미지 유형             │    권장 방식                         │
├───────────────────────────┼─────────────────────────────────────┤
│ 강의 썸네일/배너           │ CloudFront (공개 + 캐싱 효과)         │
│ 프로필 이미지             │ CloudFront (공개 + 캐싱 효과)         │
│ 컬럼/리뷰 이미지           │ CloudFront (공개 콘텐츠)              │
│ 피드백 첨부파일            │ Presigned URL (프라이빗, 15분 만료)   │
│ 결제 영수증               │ Presigned URL (민감 정보, 5분 만료)   │
│ 메모 첨부파일              │ Presigned URL (개인 데이터, 30분 만료) │
└───────────────────────────┴─────────────────────────────────────┘
```

#### 목표 아키텍처 다이어그램

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        TPT-API 하이브리드 아키텍처                        │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌─────────┐                                                           │
│   │ Client  │                                                           │
│   └────┬────┘                                                           │
│        │                                                                │
│        ├───────────────────┬────────────────────┐                       │
│        │                   │                    │                       │
│        ▼                   ▼                    ▼                       │
│   [공개 이미지]        [비공개 이미지]      [민감 이미지]                 │
│        │                   │                    │                       │
│        ▼                   │                    │                       │
│   ┌─────────┐              │                    │                       │
│   │CloudFront│             │                    │                       │
│   │  (CDN)  │              │                    │                       │
│   └────┬────┘              │                    │                       │
│        │                   │                    │                       │
│        │ OAC               ▼                    ▼                       │
│        │            ┌─────────────┐      ┌─────────────┐                │
│        │            │ API Server  │      │ API Server  │                │
│        │            │(Presigned)  │      │(Presigned)  │                │
│        │            │  30분 만료   │      │  5분 만료    │                │
│        │            └──────┬──────┘      └──────┬──────┘                │
│        │                   │                    │                       │
│        ▼                   ▼                    ▼                       │
│   ┌─────────────────────────────────────────────────┐                   │
│   │                    AWS S3                        │                   │
│   │  ┌──────────┐  ┌──────────┐  ┌──────────┐       │                   │
│   │  │  public/ │  │ private/ │  │sensitive/│       │                   │
│   │  │ (공개)   │  │ (비공개) │  │ (민감)   │       │                   │
│   │  └──────────┘  └──────────┘  └──────────┘       │                   │
│   └─────────────────────────────────────────────────┘                   │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

### 주요 설계 결정

**결정 1**: 하이브리드 방식 채택
- **선택**: 이미지 유형별 Presigned URL + CloudFront 혼용
- **대안**: 단일 방식 (Presigned만 또는 CloudFront만)
- **이유**: 보안 요구사항과 성능/비용 최적화를 동시에 달성
- **트레이드오프**: 구현 복잡도 증가, 관리 포인트 증가

**결정 2**: S3 버킷 구조 분리
- **선택**: `public/`, `private/`, `sensitive/` 경로 분리
- **대안**: 단일 버킷 구조 유지
- **이유**: 버킷 정책으로 접근 제어 용이, CloudFront OAC 적용 범위 명확화
- **트레이드오프**: 기존 데이터 마이그레이션 필요

**결정 3**: 만료 시간 차등 적용
- **선택**: 민감도에 따라 5분 / 15분 / 30분 / 1시간 차등
- **대안**: 일괄 동일 만료 시간
- **이유**: 보안 수준과 사용자 경험의 균형
- **트레이드오프**: URL 재생성 빈도 증가

---

## 5. 핵심 구현

### 핵심 기능 1: Presigned URL 서비스

**목적**: AWS S3 객체에 대한 임시 접근 URL 생성

**구현 전략**:
- AWS SDK 2.x의 `S3Presigner` 활용
- 이미지 유형별 만료 시간 설정
- 캐싱을 통한 중복 생성 방지

**코드 예시**:
```java
@Service
@RequiredArgsConstructor
public class S3PresignedUrlService {

    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    /**
     * Presigned URL 생성
     *
     * @param objectKey S3 객체 키 (파일 경로)
     * @param imageType 이미지 유형 (만료 시간 결정에 사용)
     * @return 서명된 임시 URL
     */
    public String generatePresignedUrl(String objectKey, ImageType imageType) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(s3Properties.getBucketName())
            .key(objectKey)
            .build();

        Duration expiration = getExpirationDuration(imageType);

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(expiration)
            .getObjectRequest(getObjectRequest)
            .build();

        PresignedGetObjectRequest presignedRequest =
            s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }

    /**
     * 이미지 유형별 만료 시간 반환
     */
    private Duration getExpirationDuration(ImageType imageType) {
        return switch (imageType) {
            case PAYMENT_RECEIPT -> Duration.ofMinutes(5);   // 결제 영수증: 5분
            case FEEDBACK_ATTACHMENT -> Duration.ofMinutes(15); // 피드백 첨부: 15분
            case MEMO_ATTACHMENT -> Duration.ofMinutes(30);  // 메모 첨부: 30분
            case PROFILE_IMAGE -> Duration.ofHours(1);       // 프로필: 1시간
            default -> Duration.ofMinutes(30);               // 기본: 30분
        };
    }
}
```

**기술적 포인트**:
- ✅ **Strategy Pattern**: 이미지 유형별 만료 시간 전략 분리
- ✅ **AWS SDK 2.x**: 최신 SDK의 `S3Presigner` 활용 (비동기 지원)
- ✅ **불변 객체**: Request 객체들이 Builder 패턴으로 불변성 보장

### 핵심 기능 2: 이미지 URL 변환 서비스

**목적**: 기존 S3 URL을 보안 URL로 변환

**구현 전략**:
- URL에서 Object Key 추출
- 이미지 유형 판별 후 적절한 보안 URL 생성
- 공개 이미지는 CloudFront URL, 비공개는 Presigned URL 반환

**코드 예시**:
```java
@Service
@RequiredArgsConstructor
public class SecureImageUrlService {

    private final S3PresignedUrlService presignedUrlService;
    private final CloudFrontProperties cloudFrontProperties;
    private final S3Properties s3Properties;

    /**
     * S3 URL을 보안 URL로 변환
     *
     * @param originalUrl 원본 S3 URL
     * @param imageType 이미지 유형
     * @return 보안 처리된 URL
     */
    public String convertToSecureUrl(String originalUrl, ImageType imageType) {
        String objectKey = extractObjectKey(originalUrl);

        if (imageType.isPublic()) {
            // 공개 이미지: CloudFront URL 반환
            return buildCloudFrontUrl(objectKey);
        } else {
            // 비공개 이미지: Presigned URL 생성
            return presignedUrlService.generatePresignedUrl(objectKey, imageType);
        }
    }

    /**
     * S3 URL에서 Object Key 추출
     */
    private String extractObjectKey(String s3Url) {
        // https://bucket.s3.region.amazonaws.com/path/to/image.jpg
        // -> path/to/image.jpg
        String bucketPrefix = String.format("https://%s.s3.%s.amazonaws.com/",
            s3Properties.getBucketName(),
            s3Properties.getRegion());

        return s3Url.replace(bucketPrefix, "");
    }

    /**
     * CloudFront URL 생성
     */
    private String buildCloudFrontUrl(String objectKey) {
        return String.format("https://%s/%s",
            cloudFrontProperties.getDomainName(),
            objectKey);
    }
}
```

### 핵심 기능 3: 이미지 유형 Enum

**목적**: 이미지 유형별 보안 정책 정의

**코드 예시**:
```java
@Getter
@RequiredArgsConstructor
public enum ImageType {

    // 공개 이미지 (CloudFront)
    LECTURE_THUMBNAIL("강의 썸네일", true, Duration.ZERO),
    LECTURE_BANNER("강의 배너", true, Duration.ZERO),
    PROFILE_IMAGE("프로필 이미지", true, Duration.ZERO),
    COLUMN_IMAGE("컬럼 이미지", true, Duration.ZERO),
    REVIEW_IMAGE("리뷰 이미지", true, Duration.ZERO),

    // 비공개 이미지 (Presigned URL)
    FEEDBACK_ATTACHMENT("피드백 첨부파일", false, Duration.ofMinutes(15)),
    MEMO_ATTACHMENT("메모 첨부파일", false, Duration.ofMinutes(30)),
    PAYMENT_RECEIPT("결제 영수증", false, Duration.ofMinutes(5)),
    CONSULTATION_ATTACHMENT("상담 첨부파일", false, Duration.ofMinutes(30));

    private final String description;
    private final boolean isPublic;
    private final Duration defaultExpiration;

    /**
     * 공개 이미지 여부 확인
     */
    public boolean isPublic() {
        return this.isPublic;
    }

    /**
     * S3 저장 경로 prefix 반환
     */
    public String getPathPrefix() {
        return isPublic ? "public/" : "private/";
    }
}
```

### 적용한 디자인 패턴

**패턴 1**: Strategy Pattern
- **적용 위치**: 이미지 유형별 만료 시간 결정
- **이유**: 이미지 유형이 추가되어도 코드 변경 최소화
- **효과**: OCP(개방-폐쇄 원칙) 준수, 확장성 향상

**패턴 2**: Facade Pattern
- **적용 위치**: `SecureImageUrlService`
- **이유**: Presigned URL / CloudFront URL 생성 로직 통합
- **효과**: 클라이언트 코드 단순화, 구현 세부사항 은닉

**패턴 3**: Factory Pattern
- **적용 위치**: URL 생성 방식 결정
- **이유**: 이미지 유형에 따른 URL 생성 방식 동적 결정
- **효과**: 조건문 제거, 타입 안전성 향상

---

## 6. 품질 보장

### 테스트 전략

**단위 테스트** (Unit Test)
- **커버리지**: 목표 85% 이상
- **주요 케이스**: URL 생성, 만료 시간 설정, 유형 판별

```java
@ExtendWith(MockitoExtension.class)
class S3PresignedUrlServiceTest {

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3Properties s3Properties;

    @InjectMocks
    private S3PresignedUrlService presignedUrlService;

    @Test
    @DisplayName("Presigned URL 생성 - 결제 영수증 (5분 만료)")
    void generatePresignedUrl_PaymentReceipt_5MinExpiration() {
        // Given
        String objectKey = "private/receipts/2025/01/receipt-123.pdf";
        when(s3Properties.getBucketName()).thenReturn("tpt-api-images");

        PresignedGetObjectRequest mockPresigned = mock(PresignedGetObjectRequest.class);
        when(mockPresigned.url()).thenReturn(URI.create("https://...").toURL());
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
            .thenReturn(mockPresigned);

        // When
        String url = presignedUrlService.generatePresignedUrl(
            objectKey, ImageType.PAYMENT_RECEIPT);

        // Then
        assertThat(url).isNotNull();
        verify(s3Presigner).presignGetObject(argThat(request ->
            request.signatureDuration().equals(Duration.ofMinutes(5))
        ));
    }

    @Test
    @DisplayName("이미지 유형별 만료 시간 검증")
    void getExpirationDuration_ByImageType() {
        assertThat(ImageType.PAYMENT_RECEIPT.getDefaultExpiration())
            .isEqualTo(Duration.ofMinutes(5));
        assertThat(ImageType.FEEDBACK_ATTACHMENT.getDefaultExpiration())
            .isEqualTo(Duration.ofMinutes(15));
        assertThat(ImageType.MEMO_ATTACHMENT.getDefaultExpiration())
            .isEqualTo(Duration.ofMinutes(30));
    }
}
```

**통합 테스트** (Integration Test)
- **범위**: S3 연동, URL 생성 전체 플로우
- **주요 케이스**: 실제 S3 버킷 연동 테스트 (LocalStack 활용)

**E2E 테스트** (End-to-End Test)
- **도구**: Postman, REST Assured
- **주요 시나리오**: API 호출 → URL 반환 → 실제 이미지 접근

### 보안 조치

| 위협 | 대응 방안 | 구현 내용 |
|------|----------|-----------|
| **URL 유출** | 짧은 만료 시간 | 민감 이미지 5분, 일반 15~30분 |
| **무단 접근** | Presigned URL | AWS 서명 검증으로 접근 차단 |
| **S3 직접 접근** | OAC 설정 | CloudFront만 S3 접근 허용 |
| **자격 증명 노출** | IAM Role | 클라이언트에 자격 증명 미노출 |
| **대량 URL 생성 공격** | Rate Limiting | API 호출 빈도 제한 |

### 성능 최적화

**최적화 1**: URL 생성 캐싱
- **Before**: 매 요청마다 Presigned URL 생성
- **After**: Redis 캐싱 (만료 시간 - 1분 전까지 재사용)
- **기법**: 캐시 키 = `presigned:{objectKey}`, TTL = 만료 시간 - 60초

**최적화 2**: 배치 URL 생성
- **Before**: N개 이미지 = N번 API 호출
- **After**: 한 번 요청으로 최대 100개 URL 생성
- **기법**: `List<String>` 입력 → `Map<String, String>` 출력

---

## 7. 성과 및 임팩트

### 기술적 성과 (예상)

| 지표 | 목표 | 예상 달성 | 결과 |
|------|------|----------|------|
| **URL 생성 시간** | < 50ms | ~30ms | ✅ 달성 예상 |
| **보안 수준** | URL 유출 시 무단 접근 차단 | 완전 차단 | ✅ 달성 예상 |
| **가용성** | 99.9% | 99.9%+ | ✅ 달성 예상 |
| **캐시 히트율** | > 80% (공개 이미지) | ~90% | ✅ 초과 달성 예상 |

### 비용 효과 (예상)

| 구분 | 현재 | 개선 후 (하이브리드) |
|------|------|---------------------|
| S3 전송 비용 | 100% | ~30% (CloudFront 캐싱) |
| 총 비용 | 기준 | 유사 또는 소폭 증가 |
| 보안 수준 | 🔴 취약 | 🟢 강화 |

### 개발 생산성 향상

- ✅ **재사용 컴포넌트**: `SecureImageUrlService` - 모든 도메인에서 사용 가능
- ✅ **표준화된 패턴**: 이미지 보안 처리 일관성 확보
- ✅ **명확한 책임 분리**: 이미지 유형별 정책 관리 용이

---

## 📌 핵심 교훈 (Key Takeaways)

### 1. 보안과 사용자 경험의 균형
- **상황**: 모든 이미지에 동일한 보안 수준 적용 시 성능 저하
- **교훈**: 데이터 민감도에 따른 차등 보안 정책이 효율적
- **적용**: 공개/비공개/민감 3단계 분류 및 차등 정책 적용

### 2. 하이브리드 아키텍처의 가치
- **상황**: 단일 방식으로는 모든 요구사항 충족 불가
- **교훈**: 각 기술의 장점을 결합한 하이브리드 접근이 효과적
- **적용**: CloudFront(공개) + Presigned URL(비공개) 조합

### 3. 점진적 마이그레이션의 중요성
- **상황**: 운영 중인 시스템의 대규모 변경 위험
- **교훈**: 단계별 마이그레이션으로 위험 분산
- **적용**: Phase 1(Presigned) → Phase 2(CloudFront) 순차 적용

---

## 🔮 향후 개선 계획

### 단기 (1-3개월)
- [ ] Presigned URL 서비스 구현 및 배포
- [ ] 기존 비공개 이미지 마이그레이션
- [ ] 모니터링 및 알림 설정

### 중기 (3-6개월)
- [ ] CloudFront 배포 생성 및 OAC 설정
- [ ] 공개 이미지 CloudFront 마이그레이션
- [ ] 캐시 무효화 자동화 구현

### 장기 (6개월+)
- [ ] CloudFront Signed Cookie 도입 검토
- [ ] 이미지 최적화 (WebP 변환, 리사이징)
- [ ] 글로벌 사용자 대응 (멀티 리전)

---

## 🔗 관련 문서

- [AWS S3 Presigned URL 공식 문서](https://docs.aws.amazon.com/AmazonS3/latest/userguide/using-presigned-url.html)
- [AWS CloudFront OAC 설정 가이드](https://docs.aws.amazon.com/AmazonCloudFront/latest/DeveloperGuide/private-content-restricting-access-to-s3.html)
- [TPT-API S3 인프라 설정](./INFRASTRUCTURE.md)

---

## 9. 면접 Q&A (Interview Questions)

### Q1. 현재 S3 URL 직접 노출의 보안 문제점은 무엇이며, 어떻게 해결했나요?

**A**: 현재 방식은 S3 URL이 유출되면 영구적으로 누구나 접근 가능한 심각한 보안 취약점이 있습니다. 이를 해결하기 위해 두 가지 방식을 도입했습니다:

1. **Presigned URL**: 서버에서 AWS 비밀키로 서명된 임시 URL을 생성합니다. 설정된 만료 시간(5분~1시간) 후에는 자동으로 접근이 차단됩니다.

2. **CloudFront + OAC**: S3 버킷에 대한 직접 접근을 완전히 차단하고, CloudFront CDN을 통해서만 접근하도록 설정합니다. S3 URL이 유출되어도 접근할 수 없습니다.

**💡 포인트**:
- Defense in Depth (심층 방어) 원칙 적용
- 데이터 민감도에 따른 차등 보안 정책
- AWS 보안 모범 사례 준수

---

### Q2. Presigned URL과 CloudFront 중 어떤 것을 선택했고, 그 이유는?

**A**: 단일 방식이 아닌 **하이브리드 방식**을 선택했습니다.

| 구분 | 방식 | 이유 |
|------|------|------|
| 공개 이미지 | CloudFront | 캐싱으로 성능 향상, 비용 절감 |
| 비공개 이미지 | Presigned URL | 사용자별 세밀한 접근 제어 |

선택 근거:
- **CloudFront만 사용할 경우**: 비공개 이미지에 대한 개별 접근 제어가 복잡
- **Presigned URL만 사용할 경우**: 캐싱이 불가능하여 공개 이미지 성능 저하
- **하이브리드**: 각 방식의 장점을 결합하여 보안과 성능 모두 최적화

**💡 포인트**:
- 트레이드오프 분석 능력
- 요구사항에 맞는 기술 선택
- 비용/성능/보안 균형 고려

---

### Q3. Presigned URL의 동작 원리를 설명해주세요.

**A**: Presigned URL은 AWS 비밀키를 사용하여 URL에 **암호화 서명**을 추가하는 방식입니다.

```
동작 흐름:
1. 클라이언트가 이미지 URL 요청
2. 서버가 AWS SDK로 Presigned URL 생성 요청
   - Access Key ID, Secret Key, 만료 시간, 객체 경로 등으로 서명 생성
3. 생성된 URL: 원본 URL + 서명 파라미터 (X-Amz-Signature 등)
4. 클라이언트가 해당 URL로 S3에 직접 요청
5. S3가 서명을 검증하고, 유효하면 이미지 반환
```

**핵심 보안 특성**:
- 서명은 만료 시간, 객체 경로, HTTP 메서드를 포함하여 생성
- 만료 시간이 지나면 서명이 무효화
- AWS 비밀키 없이는 서명 위조 불가능 (HMAC-SHA256)

**💡 포인트**:
- AWS 서명 버전 4 (Signature Version 4) 이해
- 비대칭 암호화 원리
- URL 구조 및 각 파라미터 역할

---

### Q4. CloudFront OAC(Origin Access Control)는 어떻게 S3 직접 접근을 차단하나요?

**A**: OAC는 S3 버킷 정책을 통해 **CloudFront 서비스 프린시펄**만 접근을 허용하는 방식입니다.

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Sid": "AllowCloudFrontServicePrincipal",
    "Effect": "Allow",
    "Principal": {
      "Service": "cloudfront.amazonaws.com"
    },
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::bucket-name/*",
    "Condition": {
      "StringEquals": {
        "AWS:SourceArn": "arn:aws:cloudfront::ACCOUNT:distribution/DIST_ID"
      }
    }
  }]
}
```

**동작 원리**:
1. S3 버킷의 퍼블릭 액세스 완전 차단
2. 버킷 정책에서 특정 CloudFront 배포만 허용
3. 모든 S3 직접 접근 시도는 403 Forbidden 반환
4. CloudFront를 통한 요청만 S3에 도달

**💡 포인트**:
- IAM 정책과 버킷 정책 차이점
- 서비스 프린시펄 개념
- Zero Trust 아키텍처

---

### Q5. 이미지 유형별로 만료 시간을 다르게 설정한 이유는?

**A**: **데이터 민감도**와 **사용자 경험**의 균형을 위해서입니다.

| 이미지 유형 | 만료 시간 | 이유 |
|------------|----------|------|
| 결제 영수증 | 5분 | 금융 정보로 최고 수준 보안 필요 |
| 피드백 첨부 | 15분 | 개인 거래 정보 포함 |
| 메모 첨부 | 30분 | 개인 데이터, 작업 시간 고려 |
| 프로필 | 1시간 | 준공개, 장시간 세션 고려 |

**설계 원칙**:
- **최소 권한 원칙**: 필요한 최소 시간만 접근 허용
- **사용자 경험**: 너무 짧으면 불편, 너무 길면 보안 위험
- **데이터 분류**: 민감도에 따른 3단계 분류 (공개/비공개/민감)

**💡 포인트**:
- 보안과 UX 트레이드오프 이해
- 데이터 분류 체계 설계 능력
- 정책 기반 보안 설계

---

### Q6. 기존 S3 URL을 새 시스템으로 마이그레이션하는 전략은?

**A**: **점진적 마이그레이션** 전략을 채택했습니다.

```
Phase 1: Presigned URL 도입 (즉시)
├── 신규 업로드 이미지에 적용
├── API 응답에서 URL 변환 서비스 적용
└── 기존 DB의 URL은 유지 (하위 호환성)

Phase 2: 공개 이미지 CloudFront 전환 (1-2주 후)
├── CloudFront 배포 생성
├── OAC 설정
└── 공개 이미지 경로를 CloudFront URL로 변환

Phase 3: 기존 데이터 정리 (1개월 후)
├── 사용되지 않는 이미지 정리
├── S3 버킷 구조 재정리
└── 모니터링 및 최적화
```

**위험 완화**:
- 각 Phase별 롤백 계획 수립
- 카나리 배포로 점진적 적용
- 구버전/신버전 병행 운영 기간 설정

**💡 포인트**:
- 무중단 배포 전략
- 하위 호환성 고려
- 위험 관리 능력

---

## 📝 변경 이력 (Change Log)

| 버전 | 날짜 | 작성자 | 변경 내용 |
|------|------|--------|----------|
| 1.0.0 | 2025-01-26 | TPT Dev Team | 최초 작성 |
