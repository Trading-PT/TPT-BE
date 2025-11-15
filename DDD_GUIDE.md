# Domain-Driven Design (DDD) Entity 설계 원칙

> **프로젝트 컨텍스트**: TPT-API (Trading Platform API)
> **최종 수정일**: 2025-01-15

## 📋 목차
1. [핵심 원칙](#핵심-원칙)
2. [Anti-Patterns (절대 금지)](#anti-patterns-절대-금지)
3. [Best Practices (권장)](#best-practices-권장)
4. [실전 예시](#실전-예시)
5. [체크리스트](#체크리스트)
6. [마이그레이션 가이드](#마이그레이션-가이드)

---

## 핵심 원칙

### 1. Rich Domain Model (풍부한 도메인 모델)

**Entity는 단순한 데이터 홀더가 아닙니다.**

```java
// ❌ BAD: Anemic Domain Model (빈약한 도메인 모델)
@Entity
public class Subscription {
    private Long id;
    private Status status;
    private LocalDate nextBillingDate;
    private Integer paymentFailureCount;
    // Getter/Setter만 있음 - 비즈니스 로직 없음
}

// ✅ GOOD: Rich Domain Model (풍부한 도메인 모델)
@Entity
public class Subscription {
    private Long id;
    private Status status;
    private LocalDate nextBillingDate;
    private Integer paymentFailureCount;

    // 비즈니스 로직이 Entity 안에 있음
    public void activate(LocalDate startDate) {
        if (this.status == Status.CANCELLED) {
            throw new IllegalStateException("취소된 구독은 활성화할 수 없습니다");
        }
        this.status = Status.ACTIVE;
        this.nextBillingDate = startDate.plusMonths(1);
    }

    public boolean canBeBilled() {
        return status == Status.ACTIVE
            && nextBillingDate != null
            && !nextBillingDate.isAfter(LocalDate.now());
    }

    public void recordPaymentFailure() {
        this.paymentFailureCount++;
        this.lastPaymentFailedAt = LocalDateTime.now();

        if (this.paymentFailureCount >= 3) {
            this.suspend("3회 결제 실패");
        }
    }
}
```

---

### 2. Tell, Don't Ask (묻지 말고 시켜라)

**Service가 Entity의 데이터를 꺼내서 판단하지 말고, Entity에게 행동을 시키세요.**

```java
// ❌ BAD: Service에서 Entity 데이터를 꺼내서 비즈니스 로직 수행
@Service
public class SubscriptionService {
    public void processPayment(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).get();

        // ❌ Entity의 상태를 getter로 꺼내서 Service에서 판단
        if (subscription.getStatus() == Status.ACTIVE
            && subscription.getNextBillingDate() != null
            && !subscription.getNextBillingDate().isAfter(LocalDate.now())) {

            // ❌ Service에서 비즈니스 규칙 구현
            subscription.setPaymentFailureCount(subscription.getPaymentFailureCount() + 1);
            subscription.setLastPaymentFailedAt(LocalDateTime.now());

            if (subscription.getPaymentFailureCount() >= 3) {
                subscription.setStatus(Status.SUSPENDED);
            }
        }
    }
}

// ✅ GOOD: Entity에게 행동을 시킴
@Service
public class SubscriptionService {
    public void processPayment(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId).get();

        // ✅ Entity에게 판단과 행동을 위임
        if (subscription.canBeBilled()) {
            subscription.recordPaymentFailure();
        }
        // JPA Dirty Checking이 자동으로 UPDATE
    }
}
```

---

### 3. 비즈니스 규칙은 Entity에 캡슐화

**도메인 규칙, 유효성 검증, 상태 전이 로직은 모두 Entity 안에 있어야 합니다.**

```java
@Entity
public class Customer {
    private Long id;
    private MembershipLevel membershipLevel;
    private LocalDateTime membershipExpiredAt;
    private LocalDateTime lastLoginAt;

    // ✅ 도메인 규칙 캡슐화
    public void upgradeMembership(MembershipLevel newLevel, int months) {
        // 유효성 검증
        if (newLevel.ordinal() <= this.membershipLevel.ordinal()) {
            throw new IllegalArgumentException("더 높은 등급으로만 업그레이드 가능합니다");
        }

        // 비즈니스 규칙
        this.membershipLevel = newLevel;
        this.membershipExpiredAt = calculateExpiredDate(months);
    }

    public boolean isMembershipActive() {
        return membershipExpiredAt != null
            && membershipExpiredAt.isAfter(LocalDateTime.now());
    }

    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public boolean isInactive() {
        return lastLoginAt == null
            || lastLoginAt.isBefore(LocalDateTime.now().minusMonths(3));
    }

    private LocalDateTime calculateExpiredDate(int months) {
        LocalDateTime baseDate = isMembershipActive()
            ? membershipExpiredAt  // 기존 만료일에 추가
            : LocalDateTime.now(); // 새로 시작
        return baseDate.plusMonths(months);
    }
}
```

---

### 4. Service는 얇게, Entity는 두껍게

**Service Layer의 역할:**
- 트랜잭션 관리
- 여러 Entity 간 협력 조율
- 외부 시스템과의 통합
- Use Case 흐름 제어

**Entity의 역할:**
- 비즈니스 규칙 구현
- 데이터 무결성 보장
- 상태 변경 로직
- 도메인 계산

```java
// ✅ GOOD: Service는 조율자 역할
@Service
@RequiredArgsConstructor
public class SubscriptionCommandServiceImpl {
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Transactional
    public void processMonthlyBilling(Long subscriptionId) {
        // 1. Entity 조회
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException(SubscriptionErrorStatus.SUBSCRIPTION_NOT_FOUND));

        // 2. Entity에게 판단 위임
        if (!subscription.canBeBilled()) {
            return;
        }

        // 3. 외부 시스템 호출 (Service 책임)
        PaymentResult result = paymentService.processPayment(
            subscription.getCustomer().getPaymentMethod(),
            subscription.getPlan().getPrice()
        );

        // 4. Entity에게 결과 처리 위임
        if (result.isSuccess()) {
            subscription.recordSuccessfulPayment(result.getTransactionId());
        } else {
            subscription.recordPaymentFailure();
        }

        // 5. 부가 작업 (Service 책임)
        notificationService.sendPaymentResult(subscription.getCustomer(), result);

        // JPA가 자동으로 변경사항 감지 및 UPDATE
    }
}
```

---

## Anti-Patterns (절대 금지)

### 🚫 1. Service에서 비즈니스 로직 구현

```java
// ❌ 이렇게 작성하지 마세요
@Service
public class TradingJournalService {
    public void submitForReview(Long journalId) {
        TradingJournal journal = repository.findById(journalId).get();

        // ❌ Service에서 비즈니스 규칙 검증
        if (journal.getEntries().size() < 5) {
            throw new InvalidOperationException("최소 5개 이상의 거래 기록이 필요합니다");
        }

        // ❌ Service에서 상태 변경
        journal.setStatus(JournalStatus.PENDING_REVIEW);
        journal.setSubmittedAt(LocalDateTime.now());
    }
}

// ✅ 이렇게 작성하세요
@Entity
public class TradingJournal {
    public void submitForReview() {
        // ✅ Entity 안에서 비즈니스 규칙 검증
        validateMinimumEntries();

        // ✅ Entity 안에서 상태 변경
        this.status = JournalStatus.PENDING_REVIEW;
        this.submittedAt = LocalDateTime.now();
    }

    private void validateMinimumEntries() {
        if (entries.size() < 5) {
            throw new InvalidOperationException("최소 5개 이상의 거래 기록이 필요합니다");
        }
    }
}
```

---

### 🚫 2. Getter/Setter 남용

```java
// ❌ BAD: Setter를 통한 직접 상태 변경
@Service
public class CustomerService {
    public void extendMembership(Long customerId, int months) {
        Customer customer = repository.findById(customerId).get();

        // ❌ Setter 남용 - 비즈니스 규칙 없이 직접 수정
        customer.setMembershipExpiredAt(
            customer.getMembershipExpiredAt().plusMonths(months)
        );
    }
}

// ✅ GOOD: 의미 있는 비즈니스 메서드
@Entity
public class Customer {
    // ✅ Setter 대신 의미 있는 메서드
    public void extendMembership(int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("연장 기간은 양수여야 합니다");
        }

        // 비즈니스 규칙: 기존 만료일에 추가
        this.membershipExpiredAt = isMembershipActive()
            ? membershipExpiredAt.plusMonths(months)
            : LocalDateTime.now().plusMonths(months);
    }
}
```

---

### 🚫 3. 도메인 로직을 외부에 노출

```java
// ❌ BAD: 도메인 계산을 Service에서 수행
@Service
public class FeedbackService {
    public void createFeedback(CreateFeedbackRequest request) {
        TradingJournal journal = journalRepository.findById(request.getJournalId()).get();

        // ❌ 평가 점수 계산을 Service에서 수행
        int totalScore = request.getAnalysisScore()
            + request.getRiskManagementScore()
            + request.getPsychologyScore();
        double averageScore = totalScore / 3.0;

        Feedback feedback = Feedback.builder()
            .averageScore(averageScore)
            .build();
    }
}

// ✅ GOOD: 도메인 계산은 Entity 내부에
@Entity
public class Feedback {
    private Integer analysisScore;
    private Integer riskManagementScore;
    private Integer psychologyScore;

    // ✅ 계산 로직을 Entity 내부에 캡슐화
    public double getAverageScore() {
        return (analysisScore + riskManagementScore + psychologyScore) / 3.0;
    }

    public boolean isExcellent() {
        return getAverageScore() >= 4.5;
    }

    public FeedbackGrade getGrade() {
        double avg = getAverageScore();
        if (avg >= 4.5) return FeedbackGrade.EXCELLENT;
        if (avg >= 3.5) return FeedbackGrade.GOOD;
        if (avg >= 2.5) return FeedbackGrade.AVERAGE;
        return FeedbackGrade.NEEDS_IMPROVEMENT;
    }
}
```

---

## Best Practices (권장)

### ✅ 1. 팩토리 메서드 사용

```java
@Entity
public class Payment {
    // ✅ 정적 팩토리 메서드로 생성 로직 캡슐화
    public static Payment createSubscriptionPayment(
        Subscription subscription,
        PaymentMethod paymentMethod
    ) {
        validatePaymentMethod(paymentMethod);

        return Payment.builder()
            .customer(subscription.getCustomer())
            .paymentMethod(paymentMethod)
            .amount(subscription.getPlan().getPrice())
            .paymentType(PaymentType.SUBSCRIPTION)
            .status(PaymentStatus.PENDING)
            .build();
    }

    private static void validatePaymentMethod(PaymentMethod method) {
        if (method == null || !method.isValid()) {
            throw new InvalidPaymentMethodException("유효하지 않은 결제 수단입니다");
        }
    }
}

// Service에서 사용
@Service
public class PaymentService {
    public Payment initiateSubscriptionPayment(Subscription subscription) {
        PaymentMethod method = subscription.getCustomer().getPaymentMethod();

        // ✅ 팩토리 메서드 사용 - 생성 로직이 Entity에 캡슐화됨
        Payment payment = Payment.createSubscriptionPayment(subscription, method);

        return paymentRepository.save(payment);
    }
}
```

---

### ✅ 2. 상태 전이 메서드

```java
@Entity
public class Subscription {
    private Status status;

    // ✅ 상태 전이 로직을 명확한 메서드로 표현
    public void activate() {
        if (status == Status.CANCELLED) {
            throw new IllegalStateException("취소된 구독은 활성화할 수 없습니다");
        }

        this.status = Status.ACTIVE;
        this.activatedAt = LocalDateTime.now();
    }

    public void suspend(String reason) {
        if (status != Status.ACTIVE) {
            throw new IllegalStateException("활성 상태의 구독만 정지할 수 있습니다");
        }

        this.status = Status.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.suspensionReason = reason;
    }

    public void resume() {
        if (status != Status.SUSPENDED) {
            throw new IllegalStateException("정지된 구독만 재개할 수 있습니다");
        }

        this.status = Status.ACTIVE;
        this.suspendedAt = null;
        this.suspensionReason = null;
    }

    public void cancel() {
        // 환불 가능 기간 확인
        if (isRefundable()) {
            this.refundStatus = RefundStatus.ELIGIBLE;
        }

        this.status = Status.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    private boolean isRefundable() {
        return activatedAt != null
            && activatedAt.isAfter(LocalDateTime.now().minusDays(7));
    }
}
```

---

### ✅ 3. Value Object 활용

```java
// ✅ 값 객체로 복잡한 개념 캡슐화
@Embeddable
public class Money {
    private BigDecimal amount;
    private Currency currency;

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("통화가 다릅니다");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        return this.amount.compareTo(other.amount) > 0;
    }
}

@Entity
public class SubscriptionPlan {
    @Embedded
    private Money price;

    // ✅ Value Object를 활용한 비즈니스 로직
    public Money calculateAnnualPrice() {
        return price.multiply(12);
    }

    public boolean isMoreExpensiveThan(SubscriptionPlan other) {
        return this.price.isGreaterThan(other.getPrice());
    }
}
```

---

### ✅ 4. 불변성 보장

```java
@Entity
public class TradingEntry {
    private final Long id;
    private final TradingJournal journal;  // final로 불변성 보장
    private final LocalDateTime tradedAt;

    private TradeType tradeType;
    private String symbol;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;

    // ✅ 생성 후 핵심 데이터는 변경 불가
    protected TradingEntry() {}  // JPA용

    private TradingEntry(TradingJournal journal, LocalDateTime tradedAt) {
        this.journal = journal;
        this.tradedAt = tradedAt;
    }

    // ✅ 수정은 새로운 비즈니스 메서드로만
    public void updateTradeDetails(
        String symbol,
        BigDecimal entryPrice,
        BigDecimal exitPrice
    ) {
        validatePrices(entryPrice, exitPrice);

        this.symbol = symbol;
        this.entryPrice = entryPrice;
        this.exitPrice = exitPrice;
    }

    // ✅ 계산 로직 캡슐화
    public BigDecimal calculateProfit() {
        return exitPrice.subtract(entryPrice);
    }

    public boolean isProfitable() {
        return calculateProfit().compareTo(BigDecimal.ZERO) > 0;
    }

    private void validatePrices(BigDecimal entry, BigDecimal exit) {
        if (entry.compareTo(BigDecimal.ZERO) <= 0 || exit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("가격은 양수여야 합니다");
        }
    }
}
```

---

## 실전 예시

이 섹션에서는 TPT-API 프로젝트의 실제 도메인을 기반으로 한 예시를 제공합니다.

### 예시 1: Subscription (구독)

```java
@Entity
@DynamicUpdate
public class Subscription extends BaseEntity {
    private Long id;
    private Customer customer;
    private SubscriptionPlan plan;
    private PaymentMethod paymentMethod;
    private Status status;
    private LocalDate nextBillingDate;
    private Integer paymentFailureCount;

    // ✅ 비즈니스 메서드: 결제일 도래 여부 판단
    public boolean canBeBilled() {
        return status == Status.ACTIVE
            && nextBillingDate != null
            && !nextBillingDate.isAfter(LocalDate.now());
    }

    // ✅ 비즈니스 메서드: 결제 실패 처리
    public void recordPaymentFailure() {
        this.paymentFailureCount++;
        this.lastPaymentFailedAt = LocalDateTime.now();

        // 비즈니스 규칙: 3회 실패 시 자동 정지
        if (this.paymentFailureCount >= 3) {
            suspend("3회 연속 결제 실패");
        }
    }

    // ✅ 비즈니스 메서드: 결제 성공 처리
    public void recordSuccessfulPayment(String transactionId) {
        this.paymentFailureCount = 0;
        this.lastPaymentFailedAt = null;
        this.lastBillingDate = LocalDate.now();
        this.nextBillingDate = LocalDate.now().plusMonths(1);
    }

    // ✅ 상태 전이: 정지
    public void suspend(String reason) {
        if (status != Status.ACTIVE) {
            throw new IllegalStateException("활성 상태의 구독만 정지할 수 있습니다");
        }

        this.status = Status.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.suspensionReason = reason;
    }
}
```

### 예시 2: Customer (고객)

```java
@Entity
@DiscriminatorValue("ROLE_CUSTOMER")
public class Customer extends User {
    private MembershipLevel membershipLevel;
    private LocalDateTime membershipExpiredAt;
    private Integer token;

    // ✅ 비즈니스 메서드: 멤버십 업데이트
    public void updateMembership(MembershipLevel membershipLevel, LocalDateTime expiredAt) {
        this.membershipLevel = membershipLevel;
        this.membershipExpiredAt = expiredAt;
    }

    // ✅ 비즈니스 판단: 멤버십 활성 여부
    public boolean isMembershipActive() {
        if (membershipLevel != MembershipLevel.PREMIUM) {
            return false;
        }
        if (membershipExpiredAt == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(membershipExpiredAt);
    }

    // ✅ 비즈니스 메서드: 토큰 사용
    public void useTokens(int tokens) {
        if (this.token < tokens) {
            throw new LectureException(LectureErrorStatus.NOT_ENOUGH_TOKENS);
        }
        this.token -= tokens;
    }

    // ✅ 비즈니스 판단: 활성 구독 여부
    public boolean hasActiveSubscription() {
        // Subscription과의 연관관계를 통해 판단
        return subscriptions.stream()
            .anyMatch(sub -> sub.getStatus() == Status.ACTIVE);
    }
}
```

### 예시 3: FeedbackRequest (피드백 요청)

```java
@Entity
public class FeedbackRequest extends BaseEntity {
    private Long id;
    private Customer customer;
    private Trainer assignedTrainer;
    private RequestStatus status;
    private Priority priority;
    private LocalDateTime requestedAt;

    // ✅ 팩토리 메서드: 생성 로직 캡슐화
    public static FeedbackRequest createFrom(Customer customer, InvestmentType investmentType) {
        validateCustomerEligibility(customer);

        return FeedbackRequest.builder()
            .customer(customer)
            .status(RequestStatus.PENDING)
            .priority(determinePriority(customer))
            .investmentType(investmentType)
            .requestedAt(LocalDateTime.now())
            .build();
    }

    private static void validateCustomerEligibility(Customer customer) {
        if (!customer.hasActiveSubscription()) {
            throw new IllegalStateException("활성 구독이 있는 고객만 피드백을 요청할 수 있습니다");
        }
    }

    private static Priority determinePriority(Customer customer) {
        // 비즈니스 규칙: PREMIUM 멤버는 높은 우선순위
        return customer.getMembershipLevel() == MembershipLevel.PREMIUM
            ? Priority.HIGH
            : Priority.NORMAL;
    }

    // ✅ 상태 전이: 트레이너 배정
    public void assignTo(Trainer trainer) {
        if (status != RequestStatus.PENDING) {
            throw new IllegalStateException("대기 중인 요청만 배정할 수 있습니다");
        }

        if (!trainer.canHandleInvestmentType(this.investmentType)) {
            throw new IllegalArgumentException("해당 트레이너는 이 투자 유형을 담당할 수 없습니다");
        }

        this.assignedTrainer = trainer;
        this.status = RequestStatus.ASSIGNED;
        this.assignedAt = LocalDateTime.now();
    }

    // ✅ 도메인 판단: SLA 위반 여부
    public boolean isSlaViolated() {
        if (status == RequestStatus.COMPLETED) {
            return false;
        }

        int slaHours = priority == Priority.HIGH ? 24 : 48;
        return requestedAt.plusHours(slaHours).isBefore(LocalDateTime.now());
    }
}
```

---

## 체크리스트

코드 작성/리뷰 시 반드시 확인하세요:

### Entity 설계 체크리스트

- [ ] **비즈니스 로직이 Service가 아닌 Entity 안에 있는가?**
  - getter로 값을 꺼내서 Service에서 계산하고 있지 않은가?

- [ ] **의미 있는 도메인 메서드가 있는가?**
  - 단순 setter 대신 `activate()`, `suspend()`, `complete()` 같은 메서드를 사용하는가?

- [ ] **도메인 규칙을 검증하는가?**
  - 상태 전이 시 유효성을 검증하는가?
  - 비즈니스 제약사항을 Entity가 보장하는가?

- [ ] **Tell, Don't Ask 원칙을 따르는가?**
  - `if (entity.getStatus() == ...)` 대신 `entity.isActive()` 같은 메서드를 사용하는가?

- [ ] **불변성을 적절히 보장하는가?**
  - 핵심 도메인 개념은 final로 선언했는가?
  - setter를 무분별하게 열어두지 않았는가?

- [ ] **복잡한 생성 로직은 팩토리 메서드로 캡슐화했는가?**
  - `createFrom()`, `of()` 같은 정적 팩토리 메서드를 활용하는가?

### Service 설계 체크리스트

- [ ] **Service는 얇은가?**
  - 비즈니스 로직이 아닌 조율 역할만 하는가?

- [ ] **Service에서 Entity의 데이터를 직접 조작하고 있지 않은가?**
  - `entity.setXxx()` 호출이 있다면 Entity 메서드로 옮길 수 있는가?

- [ ] **Service의 메서드 이름이 유스케이스를 표현하는가?**
  - `update()` 대신 `processMonthlyBilling()` 같은 명확한 이름을 사용하는가?

### 코드 품질 체크리스트

- [ ] **JPA Dirty Checking을 활용하는가?**
  - 불필요한 `repository.save()` 호출이 없는가?

- [ ] **Builder 패턴으로 Entity를 재생성하고 있지 않은가?**
  - Managed Entity를 복사하여 새로 만들고 있지 않은가?

- [ ] **@DynamicUpdate를 활용하는가?**
  - 변경된 필드만 UPDATE하도록 최적화했는가?

---

## 마이그레이션 가이드

기존 코드를 DDD 스타일로 마이그레이션하는 단계:

### Step 1: Entity에 비즈니스 메서드 추가

```java
// Before
@Entity
public class Subscription {
    private Status status;
    // getter/setter만 있음
}

// After
@Entity
public class Subscription {
    private Status status;

    public void activate() {
        this.status = Status.ACTIVE;
        this.activatedAt = LocalDateTime.now();
    }

    public void suspend(String reason) {
        this.status = Status.SUSPENDED;
        this.suspendedAt = LocalDateTime.now();
        this.suspensionReason = reason;
    }
}
```

### Step 2: Service 코드 간소화

```java
// Before
subscription.setStatus(Status.ACTIVE);
subscription.setActivatedAt(LocalDateTime.now());

// After
subscription.activate();
```

### Step 3: 복잡한 로직을 Entity로 이동

```java
// Before (Service에 있던 로직)
if (subscription.getPaymentFailureCount() >= 3) {
    subscription.setStatus(Status.SUSPENDED);
    subscription.setSuspensionReason("3회 연속 결제 실패");
}

// After (Entity 안으로 이동)
@Entity
public class Subscription {
    public void recordPaymentFailure() {
        this.paymentFailureCount++;
        if (this.paymentFailureCount >= 3) {
            this.suspend("3회 연속 결제 실패");
        }
    }
}
```

### Step 4: 유효성 검증 추가

```java
@Entity
public class Subscription {
    public void activate() {
        if (this.status == Status.CANCELLED) {
            throw new IllegalStateException("취소된 구독은 활성화할 수 없습니다");
        }
        this.status = Status.ACTIVE;
        this.activatedAt = LocalDateTime.now();
    }
}
```

### Step 5: 팩토리 메서드 도입

```java
@Entity
public class FeedbackRequest {
    public static FeedbackRequest createFrom(Customer customer, InvestmentType type) {
        validateCustomerEligibility(customer);

        return FeedbackRequest.builder()
            .customer(customer)
            .status(RequestStatus.PENDING)
            .priority(determinePriority(customer))
            .investmentType(type)
            .requestedAt(LocalDateTime.now())
            .build();
    }
}
```

---

## 요약

### 8가지 핵심 원칙

1. **Entity는 데이터 + 행동**을 함께 가진 Rich Domain Model
2. **비즈니스 로직은 Entity 안에** 캡슐화
3. **Service는 얇게**, Entity 간 협력만 조율
4. **Tell, Don't Ask**: Entity에게 시키고, 꺼내서 판단하지 말 것
5. **의미 있는 도메인 메서드** 사용 (setter 지양)
6. **팩토리 메서드**로 복잡한 생성 로직 캡슐화
7. **상태 전이는 명확한 메서드**로 표현
8. **JPA Dirty Checking** 적극 활용

### 기대 효과

이 원칙들을 따르면:
- ✅ **코드 가독성 향상**: 비즈니스 로직이 명확하게 표현됨
- ✅ **유지보수성 향상**: 변경 사항이 한 곳에 집중됨
- ✅ **테스트 용이성 향상**: Entity 단위 테스트가 쉬워짐
- ✅ **버그 감소**: 비즈니스 규칙이 Entity에서 보장됨
- ✅ **재사용성 향상**: 도메인 로직이 독립적으로 재사용 가능

---

## 참고 자료

- **CLAUDE.md**: 프로젝트 전체 가이드 및 JPA Best Practices
- **Eric Evans - Domain-Driven Design**: DDD 원조 서적
- **Vaughn Vernon - Implementing Domain-Driven Design**: DDD 실전 가이드
