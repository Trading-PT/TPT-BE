# 매매일지 피드백 시스템 (Trading Feedback System)

## 목차
- [1. 배경](#1-배경)
- [2. 요구사항](#2-요구사항)
- [3. 기술적 과제](#3-기술적-과제)
- [4. 아키텍처 설계](#4-아키텍처-설계)
- [5. 구현 상세](#5-구현-상세)
- [6. 코드 품질 및 개선사항](#6-코드-품질-및-개선사항)
- [7. 결론](#7-결론)

---

## 1. 배경

### 1.1 비즈니스 요구사항

트레이딩 플랫폼에서 사용자가 자신의 매매 내역을 기록하고 트레이너로부터 피드백을 받을 수 있는 시스템이 필요했습니다.

**핵심 비즈니스 로직:**
- 3가지 투자 유형별 매매일지 작성 (DAY/SCALPING/SWING)
- 완강 전/후 구분하여 다른 필드 요구
- 토큰 시스템: 기록용 vs 피드백 요청
- Best 피드백 선정 (최대 4개)
- 트레이너 배정 및 응답 권한 관리

---

## 2. 요구사항

### 2.1 기능 요구사항

#### 2.1.1 매매일지 작성 (Feedback Request)
- **투자 유형별 다른 입력 폼**: DAY, SCALPING, SWING
- **완강 전/후 구분**:
  - 완강 전: 기본 정보 + 포지션 시작/종료 사유
  - 완강 후: 기본 정보 + 프레임 분석, 추세 분석, 등급, 트레이너 요청사항 등
- **토큰 시스템**:
  - 기록용 (토큰 미사용): 피드백 없이 매매 기록만 저장
  - 피드백 요청 (토큰 사용): 트레이너 피드백 받기
- **스크린샷 업로드**: 매매 차트 이미지 첨부 (S3 저장)
- **토큰 보상**: 피드백 요청 10회마다 토큰 3개 지급

#### 2.1.2 피드백 응답 (Feedback Response)
- **트레이너 응답 작성**: HTML 에디터 (이미지 업로드 지원)
- **권한 관리**:
  - 토큰 사용 피드백: 모든 트레이너 응답 가능
  - 일반 피드백: 배정된 트레이너만 응답 가능
- **상태 관리**: 응답 완료 시 상태 변경 (FN: 응답 완료, 아직 읽지 않음)

#### 2.1.3 Best 피드백 관리
- 관리자가 우수 피드백 최대 4개 선정
- `FeedbackRequest.MAX_BEST_FEEDBACK_COUNT = 4` (상수 관리)
- 선정된 피드백은 `isBest = true` 플래그

### 2.2 비기능 요구사항

- **Entity 상속 구조**: JOINED 전략으로 3가지 투자 유형 관리
- **팩토리 메서드 패턴**: 복잡한 생성 로직 캡슐화
- **파일 업로드**: S3 연동 (스크린샷, HTML 이미지)
- **코드 품질**: 78/100 (개선 필요 - setter 노출 문제)

---

## 3. 기술적 과제

### 3.1 Entity 상속 구조 설계

#### 과제: 3가지 투자 유형을 효율적으로 관리

**요구사항:**
- DAY, SCALPING, SWING 각각 다른 필드 필요
- 공통 필드는 중복 제거
- 다형성 쿼리 지원 (투자 유형 무관하게 조회)

**해결 방안:**
- **JOINED 상속 전략** 채택
- `FeedbackRequest` 추상 부모 엔티티
- `DayRequestDetail`, `ScalpingRequestDetail`, `SwingRequestDetail` 자식 엔티티
- `@DiscriminatorColumn(name = "investment_type")` 구분

**장단점:**
- ✅ 정규화된 테이블 구조 (중복 최소화)
- ✅ 타입별 특화 필드 관리 용이
- ⚠️ JOIN 쿼리 발생 (성능 고려 필요)

### 3.2 토큰 시스템 통합

#### 과제: 피드백 요청 vs 기록용 구분

**문제 상황:**
- 모든 매매일지를 피드백 요청으로 처리하면 토큰 부족
- 기록용 매매일지는 통계에만 사용

**해결 방안:**
```java
// 1. 토큰 검증 및 차감 (선택적)
validateAndConsumeTokenIfNeeded(customer, request.getUseToken(), request.getTokenAmount());

// 2. 토큰 사용 여부 설정
if (Boolean.TRUE.equals(request.getUseToken())) {
    Integer tokenAmount = request.getTokenAmount() != null ? request.getTokenAmount() : 1;
    dayRequest.useToken(tokenAmount);  // isTokenUsed = true, tokenUsedAmount = N
} else {
    // 기록용: isTokenUsed = false (기본값)
}

// 3. 피드백 카운트 증가 및 토큰 보상 (DDD 패턴)
customer.incrementFeedbackCount();
boolean rewarded = customer.rewardTokensIfEligible(
    RewardConstants.FEEDBACK_THRESHOLD,   // 10회
    RewardConstants.TOKEN_REWARD_AMOUNT    // 3개
);
```

### 3.3 Setter 노출 문제 (코드 품질 78/100)

#### 문제: FeedbackRequest에 setter 메서드 노출

**Code-Reviewer 지적사항:**
```java
// ❌ BAD: setter 메서드 노출 (DDD 위반)
@Entity
public abstract class FeedbackRequest extends BaseEntity {
    // ...

    public void setStatus(Status status) {  // ❌ setter 노출
        this.status = status;
    }

    public void setFeedbackResponse(FeedbackResponse feedbackResponse) {  // ❌ setter 노출
        this.feedbackResponse = feedbackResponse;
    }
}
```

**문제점:**
- **캡슐화 위반**: 외부에서 Entity 내부 상태 직접 조작
- **비즈니스 로직 부재**: setter는 단순 값 변경만 수행
- **DDD 원칙 위배**: Tell, Don't Ask 원칙 미준수

**개선 방안:**
```java
// ✅ GOOD: 비즈니스 메서드로 캡슐화
@Entity
public abstract class FeedbackRequest extends BaseEntity {
    // ...

    /**
     * 피드백 응답 완료 처리
     * @param feedbackResponse 작성된 피드백 응답
     */
    public void completeWithResponse(FeedbackResponse feedbackResponse) {
        this.feedbackResponse = feedbackResponse;
        this.status = Status.FN;  // 응답 완료, 아직 읽지 않음
        this.isTrainerWritten = Boolean.TRUE;
    }

    /**
     * 피드백 읽음 처리
     */
    public void markAsRead() {
        if (this.status == Status.FN) {
            this.status = Status.R;  // 읽음
        }
    }

    /**
     * 토큰 사용 설정
     */
    public void useToken(Integer amount) {
        this.isTokenUsed = Boolean.TRUE;
        this.tokenUsedAmount = amount;
    }
}
```

---

## 4. 아키텍처 설계

### 4.1 Entity 상속 구조 (JOINED Strategy)

```
FeedbackRequest (abstract)
    ↓ JOINED (investment_type)
    ├── DayRequestDetail (DAY)
    ├── ScalpingRequestDetail (SCALPING)
    └── SwingRequestDetail (SWING)

FeedbackRequest (1) ← (1) FeedbackResponse
FeedbackRequest (N) → (1) Customer
FeedbackResponse (N) → (1) Trainer
```

#### 4.1.1 FeedbackRequest (Abstract Parent)

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "investment_type")
@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Getter
public abstract class FeedbackRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_request_id")
    private Long id;

    // 연관 관계
    @ManyToOne(fetch = FetchType.LAZY)
    private Customer customer;

    @OneToOne(mappedBy = "feedbackRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private FeedbackResponse feedbackResponse;

    @OneToMany(mappedBy = "feedbackRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackRequestAttachment> attachments = new ArrayList<>();

    // 공통 필드
    private String title;

    private Integer feedbackYear;
    private Integer feedbackMonth;
    private Integer feedbackWeek;
    private LocalDate feedbackRequestDate;

    @Enumerated(EnumType.STRING)
    private FeedbackCategory category;  // 매매, 실전, 계좌 점검, 기타

    @Enumerated(EnumType.STRING)
    private Position position;  // LONG, SHORT

    private String positionHoldingTime;

    @Enumerated(EnumType.STRING)
    private CourseStatus courseStatus;  // BEFORE_COMPLETION, AFTER_COMPLETION

    @Enumerated(EnumType.STRING)
    private MembershipLevel membershipLevel;

    private Boolean riskTaking;
    private BigDecimal leverage;

    private BigDecimal pnl;  // 손익
    private BigDecimal totalAssetPnl;  // 총 자산 대비 손익
    private BigDecimal rnr;  // Risk to Reward Ratio

    @Lob
    @Column(columnDefinition = "TEXT")
    private String tradingReview;  // 매매 회고

    private BigDecimal operatingFundsRatio;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private BigDecimal settingStopLoss;
    private BigDecimal settingTakeProfit;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.N;  // N, FN, R

    @Builder.Default
    private Boolean isTokenUsed = Boolean.FALSE;

    private Integer tokenUsedAmount;

    @Builder.Default
    private Boolean isBest = Boolean.FALSE;

    @Builder.Default
    private Boolean isTrainerWritten = Boolean.FALSE;

    /**
     * 상수: Best 피드백 최대 선정 개수
     */
    public static final int MAX_BEST_FEEDBACK_COUNT = 4;

    /**
     * 투자 유형 반환 (자식 클래스에서 구현)
     */
    public abstract InvestmentType getInvestmentType();

    /**
     * ❌ 문제: setter 메서드 노출 (개선 필요)
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    public void setFeedbackResponse(FeedbackResponse feedbackResponse) {
        this.feedbackResponse = feedbackResponse;
    }

    /**
     * ✅ 개선: 비즈니스 메서드 추가 권장
     */
    public void useToken(Integer amount) {
        this.isTokenUsed = Boolean.TRUE;
        this.tokenUsedAmount = amount;
    }
}
```

#### 4.1.2 DayRequestDetail (Child Entity)

```java
@Entity
@Table(name = "day_request_detail")
@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Getter
@DiscriminatorValue(value = "DAY")
public class DayRequestDetail extends FeedbackRequest {

    // 완강 전 필드
    private String positionStartReason;  // 포지션 시작 이유
    private String positionEndReason;    // 포지션 종료 이유

    // 완강 후 전용 필드
    private Boolean directionFrameExists;
    private String directionFrame;  // 방향 프레임
    private String mainFrame;       // 메인 프레임
    private String subFrame;        // 서브 프레임

    @Lob
    @Column(columnDefinition = "TEXT")
    private String trendAnalysis;  // 추세 분석

    @Lob
    @Column(columnDefinition = "TEXT")
    private String trainerFeedbackRequestContent;  // 트레이너 요청사항

    @Enumerated(EnumType.STRING)
    private EntryPoint entryPoint;  // 진입 타점

    @Enumerated(EnumType.STRING)
    private Grade grade;  // 등급 (A, B, C, D, F)

    private Integer additionalBuyCount;  // 추가 매수 횟수
    private Integer splitSellCount;      // 분할 매도 횟수

    /**
     * ✅ 정적 팩토리 메서드: 복잡한 생성 로직 캡슐화
     */
    public static DayRequestDetail createFrom(
        CreateDayRequestDetailRequestDTO request,
        Customer customer,
        FeedbackPeriodUtil.FeedbackPeriod period,
        String title
    ) {
        DayRequestDetailBuilder<?, ?> builder = DayRequestDetail.builder()
            .customer(customer)
            .title(title)
            .feedbackYear(period.year())
            .feedbackMonth(period.month())
            .feedbackWeek(period.week())
            .feedbackRequestDate(request.getFeedbackRequestDate())
            .category(request.getCategory())
            // ... 공통 필드 설정

        // 완강 전/후 조건부 필드 설정
        if (request.getCourseStatus() == CourseStatus.BEFORE_COMPLETION) {
            builder
                .positionStartReason(request.getPositionStartReason())
                .positionEndReason(request.getPositionEndReason());
        } else if (request.getCourseStatus() == CourseStatus.AFTER_COMPLETION) {
            builder
                .directionFrameExists(request.getDirectionFrameExists())
                .directionFrame(request.getDirectionFrame())
                .mainFrame(request.getMainFrame())
                // ... 완강 후 필드 설정
        }

        DayRequestDetail dayRequestDetail = builder.build();

        // 양방향 연관관계 설정
        customer.getFeedbackRequests().add(dayRequestDetail);

        return dayRequestDetail;
    }

    @Override
    public InvestmentType getInvestmentType() {
        return InvestmentType.DAY;
    }
}
```

#### 4.1.3 FeedbackResponse (Factory Method Pattern)

```java
@Entity
@Table(name = "feedback_response")
@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Getter
public class FeedbackResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_response_id")
    private Long id;

    // 연관 관계
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_request_id")
    private FeedbackRequest feedbackRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id")
    private Trainer trainer;

    @OneToMany(mappedBy = "feedbackResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedbackResponseAttachment> attachments = new ArrayList<>();

    // 필드
    private String title;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String content;  // HTML 콘텐츠 (이미지 포함)

    /**
     * ✅ 정적 팩토리 메서드: FeedbackResponse 생성
     * FeedbackRequest와 양방향 연관관계 자동 설정
     */
    public static FeedbackResponse createFrom(
        FeedbackRequest feedbackRequest,
        Trainer trainer,
        String title,
        String responseContent
    ) {
        FeedbackResponse newFeedbackResponse = FeedbackResponse.builder()
            .feedbackRequest(feedbackRequest)
            .trainer(trainer)
            .title(title)
            .content(responseContent)
            .build();

        // ❌ 문제: setter 사용 (개선 필요)
        feedbackRequest.setFeedbackResponse(newFeedbackResponse);

        return newFeedbackResponse;
    }

    /**
     * ✅ 비즈니스 메서드: 콘텐츠 수정
     */
    public void updateContent(String newContent) {
        this.content = newContent;
    }
}
```

### 4.2 Service Layer (CQRS)

#### 4.2.1 FeedbackRequestCommandService

```java
@Service
@Transactional
public class FeedbackRequestCommandServiceImpl implements FeedbackRequestCommandService {

    @Override
    public DayFeedbackRequestDetailResponseDTO createDayRequest(
        CreateDayRequestDetailRequestDTO request,
        Long customerId
    ) {
        Customer customer = getCustomerById(customerId);

        // 1. 트레이딩 타입 검증
        customer.checkTradingType(InvestmentType.DAY);

        // 2. 토큰 검증 및 차감 (선택적)
        validateAndConsumeTokenIfNeeded(customer, request.getUseToken(), request.getTokenAmount());

        // 3. 피드백 기간 자동 계산 (년, 월, 주차)
        FeedbackPeriodUtil.FeedbackPeriod period =
            FeedbackPeriodUtil.resolveFrom(request.getFeedbackRequestDate());

        // 4. 제목 자동 생성
        String title = buildFeedbackTitle(
            request.getFeedbackRequestDate(),
            request.getCategory(),
            request.getTotalAssetPnl()
        );

        // 5. DayRequestDetail 생성 (팩토리 메서드 사용)
        DayRequestDetail dayRequest = DayRequestDetail.createFrom(request, customer, period, title);

        // 6. 스크린샷 업로드 (S3)
        uploadScreenshots(request.getScreenshotFiles(), dayRequest);

        // 7. 토큰 사용 여부 설정
        if (Boolean.TRUE.equals(request.getUseToken())) {
            Integer tokenAmount = request.getTokenAmount() != null ? request.getTokenAmount() : 1;
            dayRequest.useToken(tokenAmount);  // ✅ 비즈니스 메서드 사용
        }

        // 8. 저장 (CASCADE로 attachment도 자동 저장)
        DayRequestDetail saved = (DayRequestDetail) feedbackRequestRepository.save(dayRequest);

        // 9. 피드백 카운트 증가 및 토큰 보상 (DDD 패턴)
        customer.incrementFeedbackCount();
        boolean rewarded = customer.rewardTokensIfEligible(
            RewardConstants.FEEDBACK_THRESHOLD,   // 10회
            RewardConstants.TOKEN_REWARD_AMOUNT    // 3개
        );

        if (rewarded) {
            log.info("🎉 Token reward milestone reached! customerId={}, tokensEarned={}",
                customerId, RewardConstants.TOKEN_REWARD_AMOUNT);
        }

        // JPA Dirty Checking이 자동으로 Customer UPDATE (save() 불필요)

        return DayFeedbackRequestDetailResponseDTO.of(saved);
    }
}
```

#### 4.2.2 FeedbackResponseCommandService

```java
@Service
@Transactional
public class FeedbackResponseCommandServiceImpl implements FeedbackResponseCommandService {

    @Override
    public FeedbackResponseDTO createFeedbackResponse(
        Long feedbackRequestId,
        CreateFeedbackResponseRequestDTO request,
        Long trainerId
    ) {
        // 1. 피드백 요청 조회
        FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
            .orElseThrow(() -> new FeedbackRequestException(...));

        // 2. 이미 응답이 존재하는지 확인
        if (feedbackRequest.getFeedbackResponse() != null) {
            throw new FeedbackRequestException(
                FeedbackRequestErrorStatus.FEEDBACK_RESPONSE_ALREADY_EXISTS);
        }

        // 3. 트레이너 조회 및 권한 검증
        Trainer trainer = getTrainerById(trainerId);
        validateTrainerPermission(feedbackRequest, trainer);

        // 4. HTML 콘텐츠 처리 (Base64 이미지 → S3 URL 변환)
        String processedContent = contentImageUploader.processContent(
            request.getContent(),
            "feedback-responses"
        );

        // 5. 피드백 응답 생성 (팩토리 메서드 사용)
        FeedbackResponse feedbackResponse = FeedbackResponse.createFrom(
            feedbackRequest,
            trainer,
            request.getTitle(),
            processedContent
        );

        // 6. ❌ 문제: setter 직접 호출 (개선 필요)
        feedbackRequest.setStatus(Status.FN);  // 응답 완료, 아직 읽지 않음

        // ✅ 개선 권장: 비즈니스 메서드 사용
        // feedbackRequest.completeWithResponse(feedbackResponse);

        // 7. 저장 (cascade로 FeedbackResponse도 함께 저장됨)
        feedbackRequestRepository.save(feedbackRequest);

        return FeedbackResponseDTO.of(feedbackResponse, trainer);
    }

    /**
     * ✅ 트레이너 권한 검증
     * - 토큰 사용 피드백: 모든 트레이너 응답 가능
     * - 일반 피드백: 배정된 트레이너만 응답 가능
     */
    private void validateTrainerPermission(FeedbackRequest feedbackRequest, Trainer trainer) {
        if (Boolean.TRUE.equals(feedbackRequest.getIsTokenUsed())) {
            return;  // 토큰 사용 피드백이면 모든 트레이너 응답 가능
        }

        // 일반 피드백이면 배정된 트레이너만 응답 가능
        Customer customer = feedbackRequest.getCustomer();
        if (customer.getAssignedTrainer() == null
            || !customer.getAssignedTrainer().getId().equals(trainer.getId())) {
            throw new FeedbackRequestException(
                FeedbackRequestErrorStatus.CANNOT_RESPOND_TO_NON_TOKEN_FEEDBACK_AS_UNASSIGNED_TRAINER);
        }
    }
}
```

---

## 5. 구현 상세

### 5.1 투자 유형별 엔티티 생성 (Factory Method)

**공통 패턴:**
1. DTO 검증
2. 피드백 기간 자동 계산 (년, 월, 주차)
3. 제목 자동 생성
4. 정적 팩토리 메서드로 엔티티 생성
5. 완강 전/후 조건부 필드 설정
6. 스크린샷 업로드 (S3)
7. 토큰 사용 여부 설정

**차이점:**
- **DAY**: 주차 계산 필요, 완강 후 프레임 분석 필드
- **SCALPING**: 주차 계산 필요, 빠른 매매 특성 반영
- **SWING**: 주차 계산 불필요, 장기 보유 특성 반영

### 5.2 토큰 시스템 통합

```java
/**
 * 토큰 검증 및 차감 (선택적)
 */
private void validateAndConsumeTokenIfNeeded(
    Customer customer,
    Boolean useToken,
    Integer tokenAmount
) {
    if (Boolean.TRUE.equals(useToken)) {
        // 토큰 사용 요청
        Integer requestedAmount = tokenAmount != null ? tokenAmount : 1;

        // PREMIUM 회원 검증
        if (customer.getMembershipLevel() != MembershipLevel.PREMIUM) {
            throw new FeedbackRequestException(
                FeedbackRequestErrorStatus.TOKEN_ONLY_FOR_PREMIUM_MEMBERS);
        }

        // 토큰 잔액 확인
        if (customer.getToken() < requestedAmount) {
            throw new FeedbackRequestException(
                FeedbackRequestErrorStatus.NOT_ENOUGH_TOKEN);
        }

        // ✅ 토큰 차감 (DDD 패턴: Entity 비즈니스 메서드 사용)
        customer.consumeToken(requestedAmount);
    }
}

/**
 * 피드백 카운트 증가 및 토큰 보상
 */
customer.incrementFeedbackCount();
boolean rewarded = customer.rewardTokensIfEligible(
    RewardConstants.FEEDBACK_THRESHOLD,   // 10회
    RewardConstants.TOKEN_REWARD_AMOUNT    // 3개
);
```

### 5.3 파일 업로드 (S3)

```java
/**
 * 스크린샷 업로드 (S3)
 */
private void uploadScreenshots(
    List<MultipartFile> screenshotFiles,
    FeedbackRequest feedbackRequest
) {
    if (screenshotFiles == null || screenshotFiles.isEmpty()) {
        return;
    }

    for (MultipartFile file : screenshotFiles) {
        // S3 업로드
        S3UploadResult s3Result = s3FileService.uploadFile(file, "feedback-screenshots");

        // FeedbackRequestAttachment 생성
        FeedbackRequestAttachment attachment = FeedbackRequestAttachment.builder()
            .feedbackRequest(feedbackRequest)
            .originalFileName(file.getOriginalFilename())
            .s3Key(s3Result.getS3Key())
            .s3Url(s3Result.getFileUrl())
            .fileSize(file.getSize())
            .contentType(file.getContentType())
            .build();

        feedbackRequest.getAttachments().add(attachment);
    }
}
```

### 5.4 HTML 콘텐츠 처리 (이미지 업로드)

```java
/**
 * HTML 콘텐츠 처리: Base64 이미지 → S3 URL 변환
 */
String processedContent = contentImageUploader.processContent(
    request.getContent(),
    "feedback-responses"
);

// 내부 동작:
// 1. HTML 파싱 (JSoup)
// 2. Base64 이미지 추출
// 3. S3 업로드
// 4. <img> 태그의 src를 S3 URL로 변경
// 5. 변경된 HTML 반환
```

---

## 6. 코드 품질 및 개선사항

### 6.1 Code-Reviewer 평가: 78/100

**장점:**
- ✅ 팩토리 메서드 패턴 활용
- ✅ JOINED 상속 전략 적절
- ✅ 토큰 시스템 DDD 패턴 적용 (Customer Entity)
- ✅ 파일 업로드 분리 (S3Service)

**개선 필요 사항:**
- ❌ **setter 메서드 노출** (DDD 위반)
- ❌ **도메인 로직 일부가 Service에 존재** (Entity로 이동 권장)
- ⚠️ **1799줄 Repository** (코드 중복, 리팩토링 필요 - 통계 기능 참조)

### 6.2 개선 권장사항

#### 6.2.1 Setter 제거 및 비즈니스 메서드 추가

**Before (78/100):**
```java
@Entity
public abstract class FeedbackRequest extends BaseEntity {
    public void setStatus(Status status) {  // ❌
        this.status = status;
    }

    public void setFeedbackResponse(FeedbackResponse feedbackResponse) {  // ❌
        this.feedbackResponse = feedbackResponse;
    }
}

// Service에서 직접 setter 호출
feedbackRequest.setStatus(Status.FN);  // ❌
```

**After (100/100 목표):**
```java
@Entity
public abstract class FeedbackRequest extends BaseEntity {

    /**
     * ✅ 비즈니스 메서드: 피드백 응답 완료 처리
     */
    public void completeWithResponse(FeedbackResponse feedbackResponse) {
        this.feedbackResponse = feedbackResponse;
        this.status = Status.FN;  // 응답 완료, 아직 읽지 않음
        this.isTrainerWritten = Boolean.TRUE;
    }

    /**
     * ✅ 비즈니스 메서드: 피드백 읽음 처리
     */
    public void markAsRead() {
        if (this.status == Status.FN) {
            this.status = Status.R;  // 읽음
        }
    }

    /**
     * ✅ 비즈니스 메서드: Best 피드백 선정
     */
    public void selectAsBest() {
        this.isBest = Boolean.TRUE;
    }

    /**
     * ✅ 비즈니스 메서드: Best 피드백 해제
     */
    public void deselectAsBest() {
        this.isBest = Boolean.FALSE;
    }
}

// Service에서 비즈니스 메서드 호출
feedbackRequest.completeWithResponse(feedbackResponse);  // ✅
```

#### 6.2.2 Service 로직을 Entity로 이동

**Before:**
```java
// Service에서 비즈니스 로직 직접 처리 (❌)
FeedbackResponse feedbackResponse = FeedbackResponse.createFrom(...);
feedbackRequest.setFeedbackResponse(feedbackResponse);
feedbackRequest.setStatus(Status.FN);
feedbackRequestRepository.save(feedbackRequest);
```

**After:**
```java
// Entity에 비즈니스 로직 캡슐화 (✅)
FeedbackResponse feedbackResponse = FeedbackResponse.createFrom(...);
feedbackRequest.completeWithResponse(feedbackResponse);  // 상태 변경 + 연관관계 설정

// JPA Dirty Checking이 자동으로 UPDATE (save() 불필요)
```

### 6.3 예상 개선 효과

**코드 품질:**
- 78/100 → 100/100 (Subscription/Payment 수준)
- DDD 원칙 완전 준수
- Entity가 자신의 상태 관리

**유지보수성:**
- 비즈니스 로직 변경 시 Entity만 수정
- Service는 얇게 유지 (조율 역할만)
- 상태 전이 로직 명확화

---

## 7. 결론

매매일지 피드백 시스템은 **JOINED 상속 전략**과 **팩토리 메서드 패턴**을 활용한 복잡한 도메인 모델입니다.

**핵심 성과:**
1. **3가지 투자 유형 관리**: JOINED 상속으로 효율적 구조
2. **토큰 시스템 통합**: DDD 패턴 적용 (Customer Entity)
3. **팩토리 메서드 패턴**: 복잡한 생성 로직 캡슐화
4. **파일 업로드**: S3 연동 (스크린샷, HTML 이미지)

**개선 필요사항:**
- **Setter 제거**: 비즈니스 메서드로 대체 (78/100 → 100/100 목표)
- **Service 얇게**: Entity로 비즈니스 로직 이동
- **Repository 리팩토링**: 1799줄 코드 중복 제거 (통계 기능 참조)

**학습 포인트:**
- JOINED 상속 전략의 장단점
- 팩토리 메서드 패턴 활용
- DDD 원칙: Tell, Don't Ask
- JPA Dirty Checking 활용 (save() 제거)

**다음 단계:**
- Setter를 비즈니스 메서드로 대체 (리팩토링)
- Repository 코드 중복 제거 (Strategy Pattern 도입 검토)
- 정기 결제 시스템 수준의 코드 품질 달성 (100/100)
