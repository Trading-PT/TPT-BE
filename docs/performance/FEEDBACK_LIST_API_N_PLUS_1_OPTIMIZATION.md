# FeedbackRequest 목록 API N+1 쿼리 최적화 계획

> **Version**: 1.0.0
> **Last Updated**: 2025-11-27
> **Author**: TPT Development Team

---

## 📌 기술 키워드 (Technical Keywords)

| 카테고리 | 키워드 |
|---------|--------|
| **최적화 영역** | `API Performance`, `Database Tuning`, `JPA Optimization` |
| **측정 도구** | `JMeter`, `Gatling`, `k6`, `Hibernate Statistics`, `P6Spy` |
| **최적화 기법** | `N+1 Query Resolution`, `Batch Fetching`, `Fetch Join`, `IN Query Optimization` |
| **관련 기술** | `Spring Boot`, `JPA/Hibernate`, `QueryDSL`, `MySQL` |
| **핵심 지표** | `Query Count`, `Response Time`, `TPS`, `DB Connection Pool Usage` |

---

> **작성일**: 2025년 11월
> **프로젝트**: TPT-API (Trading Platform)
> **도메인**: FeedbackRequest (매매일지)
> **최적화 대상**: GET /api/v1/feedback-requests API
> **상태**: 🟡 **계획 단계** (부하 테스트 후 필요시 구현 예정)

## 📋 목차

1. [성능 문제 발견](#1-성능-문제-발견)
2. [현재 상태 분석](#2-현재-상태-분석)
3. [병목 지점 분석](#3-병목-지점-분석)
4. [최적화 목표 설정](#4-최적화-목표-설정)
5. [최적화 전략 및 실행 계획](#5-최적화-전략-및-실행-계획)
6. [예상 성과](#6-예상-성과)
7. [실행 트리거 조건](#7-실행-트리거-조건)

---

## 1. 성능 문제 발견

### 발견 경위
- **트리거**: Feature #169 구현 중 코드 리뷰에서 잠재적 N+1 문제 발견
- **발견 시점**: 2025-11-27 (개발 단계)
- **영향 범위**: GET /api/v1/feedback-requests API (공개용 피드백 목록 조회)

### 변경 내역
`FeedbackCardResponseDTO.java`에서 첨부파일 이미지 URL 목록을 추가:

```java
// Line 69-73: 새로 추가된 코드
.imageUrls(
    feedbackRequest.getFeedbackRequestAttachments()
        .stream().map(FeedbackRequestAttachment::getFileUrl)
        .toList()
)
```

### 관찰된 잠재적 증상
- **N+1 쿼리 발생**: 각 FeedbackRequest마다 Attachment 조회 쿼리 추가 발생
- **LAZY 로딩 트리거**: `getFeedbackRequestAttachments()` 호출 시 추가 SELECT 실행
- **Customer 조회**: `getCustomer().getName()` 호출로 추가 N+1 발생 가능

### 영향받는 API 엔드포인트
| API | 메서드 | 영향도 |
|-----|--------|--------|
| `/api/v1/feedback-requests` | GET | 🔴 High |
| `/api/v1/admin/feedback-requests` | GET | 🟠 Medium |
| `/api/v1/trainer-written-feedbacks` | GET | 🟡 Low (이미 Fetch Join 적용) |

---

## 2. 현재 상태 분석

### 관련 엔티티 구조

```
FeedbackRequest (부모, abstract)
├── DayRequestDetail (자식)
├── ScalpingRequestDetail (자식)
└── SwingRequestDetail (자식)

FeedbackRequest 1 ──────< N FeedbackRequestAttachment
FeedbackRequest N >────── 1 Customer
```

### 현재 Repository 구현 (문제 코드)

**파일**: `FeedbackRequestRepositoryImpl.java` (Line 63-84)

```java
@Override
public Slice<FeedbackRequest> findAllFeedbackRequestsSlice(Pageable pageable) {
    List<FeedbackRequest> allResults = new ArrayList<>();

    // ❌ 문제 1: 전체 데이터를 메모리에 로드 (페이징 무효화)
    List<DayRequestDetail> dayRequests = queryFactory
        .selectFrom(dayRequestDetail)
        .fetch();  // 전체 조회
    allResults.addAll(dayRequests);

    List<ScalpingRequestDetail> scalpingRequests = queryFactory
        .selectFrom(scalpingRequestDetail)
        .fetch();  // 전체 조회
    allResults.addAll(scalpingRequests);

    List<SwingRequestDetail> swingRequests = queryFactory
        .selectFrom(swingRequestDetail)
        .fetch();  // 전체 조회
    allResults.addAll(swingRequests);

    // 메모리에서 정렬 및 페이징 처리
    return createSlice(allResults, pageable);
}
```

### 현재 DTO 변환 (N+1 발생 지점)

**파일**: `FeedbackCardResponseDTO.java` (Line 65-83)

```java
public static FeedbackCardResponseDTO from(FeedbackRequest feedbackRequest) {
    return FeedbackCardResponseDTO.builder()
        .feedbackRequestId(feedbackRequest.getId())
        .title(feedbackRequest.getTitle())
        .imageUrls(
            // ❌ N+1 발생: LAZY 컬렉션 접근
            feedbackRequest.getFeedbackRequestAttachments()
                .stream().map(FeedbackRequestAttachment::getFileUrl)
                .toList()
        )
        .totalAssetPnl(feedbackRequest.getTotalAssetPnl())
        .contentPreview(generatePreview(feedbackRequest))
        .createdAt(feedbackRequest.getCreatedAt())
        .investmentType(feedbackRequest.getInvestmentType())
        .courseStatus(feedbackRequest.getCourseStatus())
        .status(feedbackRequest.getStatus())
        .isBestFeedback(feedbackRequest.getIsBestFeedback())
        // ❌ N+1 발생: LAZY 엔티티 접근
        .customerName(feedbackRequest.getCustomer().getName())
        .build();
}
```

### Hibernate 설정 현황

**파일**: `application.yml`

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 20  # ✅ Batch Fetching 설정됨
```

### Batch Fetching 영향 분석

현재 `default_batch_fetch_size: 20` 설정으로 인해:
- 순수 N+1이 아닌 `ceil(N/20)` 쿼리로 완화됨
- 예: 100개 FeedbackRequest → 5개 Attachment 배치 쿼리 + 5개 Customer 배치 쿼리

**예상 쿼리 수 (페이지당 20건 기준)**:

| 구분 | Batch 없을 때 | Batch 적용 시 (현재) |
|------|--------------|---------------------|
| FeedbackRequest 조회 | 3개 | 3개 |
| Attachment 조회 | +20개 | +1개 (IN 쿼리) |
| Customer 조회 | +20개 | +1개 (IN 쿼리) |
| **총 쿼리 수** | **43개** | **5개** |

---

## 3. 병목 지점 분석

### 시스템 계층별 분석

#### Application Layer
| 문제 | 심각도 | 설명 |
|------|--------|------|
| 메모리 기반 페이징 | 🔴 Critical | 전체 데이터 로드 후 메모리에서 페이징 |
| LAZY 컬렉션 접근 | 🟠 High | DTO 변환 시 N+1 쿼리 트리거 |
| 스트림 내 DB 호출 | 🟡 Medium | map() 내부에서 LAZY 로딩 발생 |

#### Database Layer
| 문제 | 심각도 | 설명 |
|------|--------|------|
| 인덱스 부재 확인 필요 | 🟡 Medium | feedbackRequestAttachments FK 인덱스 |
| 상속 구조 쿼리 | 🟠 High | JOINED 상속으로 3개 테이블 조인 필요 |
| 전체 테이블 스캔 | 🔴 Critical | 페이징 없이 전체 데이터 조회 |

### 병목 지점 우선순위

| 순위 | 병목 지점 | 영향도 | 개선 난이도 | 예상 효과 |
|------|-----------|--------|-------------|-----------|
| **1** | 메모리 기반 페이징 | 🔴 Critical | ⭐⭐⭐ | 메모리 사용량 90% 감소 |
| **2** | N+1 쿼리 (Attachment) | 🟠 High | ⭐⭐ | 쿼리 수 80% 감소 |
| **3** | N+1 쿼리 (Customer) | 🟠 High | ⭐⭐ | 쿼리 수 추가 감소 |

---

## 4. 최적화 목표 설정

### 성능 목표

#### Primary Goals (필수 달성)
- **쿼리 수**: 페이지당 43개 → 3-5개 (90% 감소)
- **응답 시간**: 목표 P95 < 200ms
- **메모리 사용**: DB 페이징으로 메모리 부하 제거

#### Secondary Goals (추가 목표)
- **확장성**: 데이터 증가 시에도 일정한 성능 유지
- **유지보수성**: 복잡도를 최소화하면서 최적화

### 제약 조건
- **기술적 제약**: JPA 상속 구조 (JOINED) 유지 필요
- **비즈니스 제약**: 베스트 피드백 우선 정렬 로직 유지 필요
- **시간 제약**: 부하 테스트 결과에 따라 우선순위 조정

### 성공 기준
- ✅ **부하 테스트에서 목표 TPS 달성**
- ✅ **P95 응답 시간 200ms 이하**
- ✅ **쿼리 수 페이지당 10개 이하**

---

## 5. 최적화 전략 및 실행 계획

### 최적화 로드맵

```
Phase 1: Quick Wins (Batch Fetching 활용)
    ↓
Phase 2: 별도 쿼리로 Attachment 조회 (IN 쿼리)
    ↓
Phase 3: DB 레벨 페이징 적용
    ↓
Phase 4: 캐싱 전략 (필요시)
```

---

### Phase 1: Quick Wins - 현재 Batch Fetching 활용

**목표**: 기존 설정 확인 및 최적 활용

#### 현재 상태 (이미 적용됨)
```yaml
# application.yml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 20
```

#### 효과
- N+1 → N/20 쿼리로 자동 완화
- 추가 코드 변경 없이 적용됨

#### 예상 결과 (페이지 20건 기준)
| 지표 | Before (Batch 없음) | After (Batch 적용) | 개선율 |
|------|---------------------|-------------------|--------|
| 총 쿼리 수 | 43개 | 5개 | **↓ 88%** |
| Attachment 쿼리 | 20개 | 1개 | **↓ 95%** |
| Customer 쿼리 | 20개 | 1개 | **↓ 95%** |

---

### Phase 2: 별도 쿼리로 Attachment 조회 (권장)

**목표**: 명시적 IN 쿼리로 Attachment 한 번에 조회

#### Before (현재 - 암묵적 Batch)
```java
// DTO 변환 시 LAZY 로딩 발생 (Batch로 완화되지만 여전히 추가 쿼리)
.imageUrls(
    feedbackRequest.getFeedbackRequestAttachments()
        .stream().map(FeedbackRequestAttachment::getFileUrl)
        .toList()
)
```

#### After (개선안 - 명시적 IN 쿼리)
```java
// Service Layer
public FeedbackListResponseDTO getFeedbackListSlice(Pageable pageable) {
    // 1단계: FeedbackRequest 조회
    Slice<FeedbackRequest> feedbackSlice = feedbackRequestRepository
        .findAllFeedbackRequestsSlice(pageable);

    List<FeedbackRequest> feedbacks = feedbackSlice.getContent();

    // 2단계: ID 목록 추출
    List<Long> feedbackIds = feedbacks.stream()
        .map(FeedbackRequest::getId)
        .toList();

    // 3단계: Attachment를 IN 쿼리로 한 번에 조회
    Map<Long, List<String>> attachmentMap = feedbackRequestRepository
        .findAttachmentUrlsByFeedbackIds(feedbackIds);

    // 4단계: Customer를 IN 쿼리로 한 번에 조회 (이미 LAZY 로딩 시 Batch 적용됨)
    // 또는 Fetch Join으로 1단계에서 함께 조회

    // 5단계: DTO 변환 (추가 쿼리 없음)
    Slice<FeedbackCardResponseDTO> cardSlice = feedbackSlice
        .map(fr -> FeedbackCardResponseDTO.from(fr, attachmentMap.get(fr.getId())));

    return FeedbackListResponseDTO.of(cardSlice.getContent(), SliceInfo.of(cardSlice));
}
```

#### Repository 추가 메서드
```java
// FeedbackRequestRepositoryCustom.java
Map<Long, List<String>> findAttachmentUrlsByFeedbackIds(List<Long> feedbackIds);

// FeedbackRequestRepositoryImpl.java
@Override
public Map<Long, List<String>> findAttachmentUrlsByFeedbackIds(List<Long> feedbackIds) {
    if (feedbackIds.isEmpty()) {
        return Collections.emptyMap();
    }

    return queryFactory
        .select(
            feedbackRequestAttachment.feedbackRequest.id,
            feedbackRequestAttachment.fileUrl
        )
        .from(feedbackRequestAttachment)
        .where(feedbackRequestAttachment.feedbackRequest.id.in(feedbackIds))
        .fetch()
        .stream()
        .collect(Collectors.groupingBy(
            tuple -> tuple.get(0, Long.class),
            Collectors.mapping(
                tuple -> tuple.get(1, String.class),
                Collectors.toList()
            )
        ));
}
```

#### DTO 수정
```java
// FeedbackCardResponseDTO.java
public static FeedbackCardResponseDTO from(
    FeedbackRequest feedbackRequest,
    List<String> imageUrls  // 외부에서 주입
) {
    return FeedbackCardResponseDTO.builder()
        .feedbackRequestId(feedbackRequest.getId())
        .title(feedbackRequest.getTitle())
        .imageUrls(imageUrls != null ? imageUrls : Collections.emptyList())
        // ... 나머지 필드
        .build();
}
```

#### 예상 결과
| 지표 | Before (Batch) | After (IN 쿼리) | 개선율 |
|------|----------------|-----------------|--------|
| 총 쿼리 수 | 5개 | 4개 | **↓ 20%** |
| 쿼리 예측 가능성 | 낮음 | 높음 | **↑ 명확** |
| 코드 복잡도 | 낮음 | 중간 | **↑ 약간** |

---

### Phase 3: DB 레벨 페이징 적용

**목표**: 메모리 기반 페이징을 DB 레벨 페이징으로 전환

#### 현재 문제
```java
// 전체 데이터를 메모리에 로드 후 페이징
List<DayRequestDetail> dayRequests = queryFactory
    .selectFrom(dayRequestDetail)
    .fetch();  // ❌ 전체 조회
```

#### 개선안: 2단계 쿼리 전략
```java
@Override
public Slice<FeedbackRequest> findAllFeedbackRequestsSlice(Pageable pageable) {
    // 1단계: ID만 먼저 조회 (DB 페이징 적용)
    List<Long> ids = queryFactory
        .select(feedbackRequest.id)
        .from(feedbackRequest)
        .orderBy(
            feedbackRequest.isBestFeedback.desc(),  // 베스트 우선
            feedbackRequest.createdAt.desc()         // 최신순
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize() + 1)  // hasNext 판단용
        .fetch();

    boolean hasNext = ids.size() > pageable.getPageSize();
    if (hasNext) {
        ids = ids.subList(0, pageable.getPageSize());
    }

    if (ids.isEmpty()) {
        return new SliceImpl<>(Collections.emptyList(), pageable, false);
    }

    // 2단계: 실제 데이터 조회 (Fetch Join으로 연관 엔티티 함께)
    List<FeedbackRequest> results = queryFactory
        .selectFrom(feedbackRequest)
        .leftJoin(feedbackRequest.customer).fetchJoin()
        .where(feedbackRequest.id.in(ids))
        .orderBy(
            feedbackRequest.isBestFeedback.desc(),
            feedbackRequest.createdAt.desc()
        )
        .fetch();

    return new SliceImpl<>(results, pageable, hasNext);
}
```

#### 주의사항
- **1:N 컬렉션 Fetch Join 제한**: `feedbackRequestAttachments`는 Fetch Join 시 페이징 문제 발생
- **해결책**: Phase 2의 별도 IN 쿼리 방식 권장

---

### Phase 4: 캐싱 전략 (필요시)

**목표**: 자주 조회되는 데이터 캐싱으로 DB 부하 감소

#### Redis 캐싱 적용 대상
| 대상 | TTL | 무효화 조건 |
|------|-----|-------------|
| 베스트 피드백 목록 | 5분 | 베스트 피드백 변경 시 |
| 첫 페이지 결과 | 1분 | 새 피드백 등록 시 |

#### 구현 예시 (Spring Cache)
```java
@Cacheable(value = "feedbackList", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
public FeedbackListResponseDTO getFeedbackListSlice(Pageable pageable) {
    // ...
}

@CacheEvict(value = "feedbackList", allEntries = true)
public void createFeedback(FeedbackRequest request) {
    // ...
}
```

---

## 6. 예상 성과

### 단계별 예상 개선 효과

| Phase | 쿼리 수 | 응답 시간 | 메모리 | 구현 난이도 |
|-------|---------|----------|--------|-------------|
| 현재 (Batch 적용) | 5개 | ~100ms | 높음 | - |
| Phase 2 (IN 쿼리) | 4개 | ~80ms | 높음 | ⭐⭐ |
| Phase 3 (DB 페이징) | 4개 | ~50ms | **낮음** | ⭐⭐⭐ |
| Phase 4 (캐싱) | 0-4개 | ~10ms | 낮음 | ⭐⭐ |

### 최종 목표 대비 예상 결과

| 지표 | 현재 | 목표 | 예상 달성 | 상태 |
|------|------|------|----------|------|
| 쿼리 수/페이지 | 5개 | <10개 | 4개 | ✅ 달성 예상 |
| P95 응답 시간 | ~100ms | <200ms | ~50ms | ✅ 달성 예상 |
| 메모리 사용 | 전체 로드 | 페이지만 | DB 페이징 | ✅ 달성 예상 |

---

## 7. 실행 트리거 조건

### 부하 테스트 후 실행 기준

| 조건 | 임계값 | 실행 Phase |
|------|--------|-----------|
| P95 응답 시간 | > 500ms | Phase 2 + 3 |
| TPS 저하 | < 100 TPS | Phase 2 + 3 |
| 에러율 | > 1% | 즉시 조사 |
| DB 커넥션 풀 고갈 | > 80% 사용 | Phase 3 + 4 |

### 실행 판단 프로세스

```
1. 부하 테스트 수행 (JMeter/k6)
   ↓
2. 성능 지표 측정
   - 응답 시간 (평균, P95, P99)
   - TPS
   - 에러율
   - DB 쿼리 수 (Hibernate Statistics)
   ↓
3. 임계값 초과 여부 판단
   ↓
4. 초과 시: Phase별 최적화 순차 적용
   미초과 시: 현재 상태 유지 (모니터링 지속)
```

### 모니터링 체크리스트

부하 테스트 시 확인 사항:
- [ ] Hibernate Statistics 활성화 (`spring.jpa.properties.hibernate.generate_statistics=true`)
- [ ] P6Spy 또는 쿼리 로깅으로 실제 쿼리 수 확인
- [ ] DB 커넥션 풀 사용량 모니터링
- [ ] GC 로그 확인 (메모리 기반 페이징 영향)
- [ ] 응답 시간 분포 확인 (평균 vs P95 vs P99)

---

## 📌 핵심 요약

### 현재 상태
- **Batch Fetching (batch_size: 20)** 이 이미 적용되어 N+1 문제가 크게 완화됨
- 순수 N+1 (43개 쿼리) → Batch 적용 후 (5개 쿼리)

### 잠재적 문제
1. **메모리 기반 페이징**: 데이터 증가 시 OOM 위험
2. **암묵적 Batch**: 쿼리 수 예측 어려움

### 권장 접근법
1. **당장은 현재 상태 유지** (Batch Fetching으로 충분히 완화됨)
2. **부하 테스트 수행** 후 성능 저하 시 Phase 2, 3 적용
3. **트래픽 증가 시** Phase 4 (캐싱) 검토

---

## 🔗 관련 문서

- [JPA Development Guidelines](../../CLAUDE.md#jpa-development)
- [DDD Guide](../../DDD_GUIDE.md)
- [Performance Optimization Template](../templates/TEMPLATE_PERFORMANCE_OPTIMIZATION.md)

---

## 📸 참고: 예상 쿼리 흐름

### 현재 (Batch Fetching 적용)
```
1. SELECT * FROM day_request_detail         -- 전체 조회
2. SELECT * FROM scalping_request_detail    -- 전체 조회
3. SELECT * FROM swing_request_detail       -- 전체 조회
4. SELECT * FROM feedback_request_attachment
   WHERE feedback_request_id IN (?, ?, ..., ?)  -- Batch (최대 20개씩)
5. SELECT * FROM customer
   WHERE id IN (?, ?, ..., ?)                   -- Batch (최대 20개씩)
```

### 최적화 후 (Phase 3 적용)
```
1. SELECT id FROM feedback_request
   ORDER BY is_best_feedback DESC, created_at DESC
   LIMIT 21 OFFSET 0                            -- ID만 페이징 조회
2. SELECT fr.*, c.* FROM feedback_request fr
   LEFT JOIN customer c ON fr.customer_id = c.id
   WHERE fr.id IN (?, ?, ..., ?)                -- 실제 데이터 + Customer
3. SELECT * FROM feedback_request_attachment
   WHERE feedback_request_id IN (?, ?, ..., ?)  -- Attachment 별도 조회
```

---

**작성자**: TPT Development Team
**최종 수정일**: 2025년 11월 27일
**버전**: 1.0.0
