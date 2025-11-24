# 매매일지 토큰 보상 시스템 설계 및 보안 취약점 해결

> **작성일**: 2025년 11월
> **프로젝트**: Trading Platform API (Spring Boot 3.x)
> **도메인**: 사용자 리워드 시스템, 데이터 정합성, 보안

## 📋 목차

1. [프로젝트 배경](#1-프로젝트-배경)
2. [요구사항 분석](#2-요구사항-분석)
3. [설계 의사결정](#3-설계-의사결정)
4. [보안 취약점 발견 및 분석](#4-보안-취약점-발견-및-분석)
5. [해결 방안 비교 분석](#5-해결-방안-비교-분석)
6. [최종 구현](#6-최종-구현)
7. [기술적 성과](#7-기술적-성과)

---

## 1. 프로젝트 배경

### 비즈니스 요구사항

- **목표**: 사용자가 매매일지를 n개 작성할 때마다 토큰을 m개 발급하여 서비스 참여도 향상
- **핵심 가치**: 사용자 인게이지먼트 증대, 콘텐츠 생산 촉진

### 기술 스택

- **Backend**: Spring Boot 3.5.5, Java 17
- **ORM**: JPA/Hibernate with QueryDSL
- **Database**: MySQL 8.0
- **아키텍처**: Domain-Driven Design (DDD)

---

## 2. 요구사항 분석

### 기능 요구사항

1. 매매일지(FeedbackRequest) **n개 작성 시마다** 자동으로 토큰 **m개 발급**
2. n, m 값은 **상수로 관리**하여 쉽게 변경 가능해야 함
3. **데이터 정합성 보장**: 카운트와 실제 피드백 개수 불일치 방지

### 비기능 요구사항

- **성능**: 매번 DB 조회 없이 효율적 처리
- **확장성**: 멀티 인스턴스 환경 대응
- **보안**: 토큰 부정 획득 방지
- **유지보수성**: 명확한 비즈니스 로직, 간결한 코드

---

## 3. 설계 의사결정

### 3.1 구현 방식 선택

**방법 1 (선택됨): 엔티티 필드 추가**

```java

@Entity
public class Customer extends User {
	@Column(name = "feedback_request_count")
	private Integer feedbackRequestCount = 0;
}
```

**장점**:

- ✅ **성능 우수**: 매번 `COUNT(*)` 쿼리 불필요
- ✅ **DDD 원칙**: 비즈니스 로직이 엔티티에 캡슐화됨
- ✅ **JPA Dirty Checking 활용**: 자동 UPDATE, `save()` 호출 불필요
- ✅ **확장성**: 향후 UI에서 "다음 보상까지 N개 남음" 표시 용이

**방법 2 (기각됨): 매번 레포지토리 조회**

```java
long count = feedbackRequestRepository.countByCustomer_Id(customerId);
```

**단점**:

- ❌ **성능 저하**: 매 작성/삭제마다 DB 조회 발생
- ❌ **DDD 위반**: 도메인 로직이 서비스 레이어에 분산
- ❌ **유지보수**: 여러 곳에서 동일한 조회 로직 중복

### 3.2 상수 관리 전략

```java
// global/common/RewardConstants.java
public final class RewardConstants {
    public static final int FEEDBACK_THRESHOLD = 5;  // n = 5
    public static final int TOKEN_REWARD_AMOUNT = 3;  // m = 3

    private RewardConstants() {}  // 인스턴스화 방지
}
```

**설계 근거**:

- ✅ **단일 책임 원칙**: 보상 정책을 한 곳에서 관리
- ✅ **타입 안전성**: final class + private constructor로 불변성 보장
- ✅ **가독성**: 명확한 이름 (`THRESHOLD`, `REWARD_AMOUNT`)

### 3.3 초기 데이터 정합성 전략

**스케줄러 기반 동기화 (나중에 제거됨)**

```java
@Scheduled(cron = "0 0 3 * * *")  // 매일 새벽 3시
@SchedulerLock(name = "feedbackCountSyncScheduler",
               lockAtMostFor = "PT30M",
               lockAtLeastFor = "PT23H")
public void syncAllFeedbackCounts() {
    // 전체 고객의 카운트를 실제 값으로 동기화
}
```

**목적**:

- 애플리케이션 버그, 동시성 이슈로 인한 카운트 불일치 복구
- **ShedLock**: 멀티 인스턴스 환경에서 중복 실행 방지 (DB 기반 분산 락)

**왜 새벽 3시인가?**

- 사용자 활동 최소 시간대
- 멤버십 만료 스케줄러(자정), 정기 결제 스케줄러(새벽 2시)와 시간대 분리

---

## 4. 보안 취약점 발견 및 분석

### 4.1 초기 구현 로직

```java
// 작성 시
customer.incrementFeedbackCount();  // count++
if(customer.

getFeedbackRequestCount() %threshold ==0){
	customer.

addToken(rewardAmount);  // 토큰 지급
}

	// 삭제 시
	customer.

decrementFeedbackCount();  // count--
```

### 4.2 보안 취약점 시나리오 (Critical)

**악용 패턴**:

1. 피드백 5개 작성 → 카운트 5 → **토큰 3개 지급** ✅
2. 피드백 1개 삭제 → 카운트 4 ⚠️
3. 피드백 1개 작성 → 카운트 5 → **토큰 3개 또 지급!** 🚨
4. 2-3번 무한 반복 → **무제한 토큰 획득 가능** 💣

**영향 분석**:

- **심각도**: Critical (비즈니스 로직 우회, 경제적 손실)
- **공격 난이도**: 낮음 (일반 사용자도 쉽게 발견 가능)
- **탐지 가능성**: 낮음 (정상적인 API 호출로 보임)

---

## 5. 해결 방안 비교 분석

### 5.1 제안된 해결책들

| 방법                                  | 설명               | 장점                                 | 단점                             | 복잡도   |
|-------------------------------------|------------------|------------------------------------|--------------------------------|-------|
| **1. lastRewardedFeedbackCount 추가** | 마지막 보상받은 카운트 추적  | ✅ 멱등성 보장<br>✅ 정확한 정합성              | ⚠️ 필드 추가<br>⚠️ 로직 복잡           | ⭐⭐⭐   |
| **2. Never Decrement (최종 선택)**      | 삭제 시 카운트 감소 안 함  | ✅ 간단<br>✅ 스케줄러 불필요<br>✅ Exploit 차단 | ⚠️ "누적 작성 횟수" 의미 변경            | ⭐⭐    |
| **3. 토큰 회수**                        | 삭제 시 지급된 토큰도 회수  | ✅ 직관적                              | ❌ 사용자 경험 나쁨<br>❌ 이미 사용한 토큰 처리? | ⭐⭐⭐⭐  |
| **4. TokenRewardHistory 테이블**       | 별도 테이블로 보상 기록 관리 | ✅ 완벽한 추적<br>✅ 감사 기능                | ❌ 오버엔지니어링<br>❌ 성능 저하           | ⭐⭐⭐⭐⭐ |

### 5.2 최종 선택: Never Decrement

**의사결정 근거**:

1. **복잡성 최소화**: 가장 간단한 구현
2. **비즈니스 정책 명확화**: "누적 작성 횟수" 개념으로 재정의
3. **보안 강화**: Exploit 완전 차단
4. **성능 향상**: 동기화 스케줄러 제거 (복잡도 대폭 감소)

**비즈니스 정책 변경**:

- **변경 전**: `feedbackRequestCount` = 현재 보유한 피드백 개수
- **변경 후**: `feedbackRequestCount` = 누적 작성 횟수 (단조증가)

**예시**:

```
작성: 1 → 2 → 3 → 4 → 5 (토큰 지급) → 6 → 7 → ...
삭제: 카운트 변화 없음 (누적 개념이므로)
```

---

## 6. 최종 구현

### 6.1 Entity (DDD 패턴)

```java

@Entity
@DynamicUpdate  // ⚡ 변경된 필드만 UPDATE
public class Customer extends User {

	/**
	 * 피드백 요청 누적 작성 횟수 (단조증가)
	 * - 작성 시: 증가
	 * - 삭제 시: 감소 안 함 (누적 개념)
	 */
	@Column(name = "feedback_request_count")
	@Builder.Default
	private Integer feedbackRequestCount = 0;

	/**
	 * 피드백 카운트 증가 (단조증가)
	 * JPA Dirty Checking 활용 → 자동 UPDATE
	 */
	public void incrementFeedbackCount() {
		this.feedbackRequestCount++;
	}

	/**
	 * 조건 충족 시 토큰 보상
	 * @return 보상 여부
	 */
	public boolean rewardTokensIfEligible(int threshold, int rewardAmount) {
		if (this.feedbackRequestCount > 0
			&& this.feedbackRequestCount % threshold == 0) {
			this.token += rewardAmount;
			return true;
		}
		return false;
	}
}
```

**핵심 설계 원칙**:

- ✅ **Tell, Don't Ask**: 엔티티가 스스로 상태 변경
- ✅ **JPA Dirty Checking**: `save()` 호출 불필요
- ✅ **@DynamicUpdate**: 변경된 필드만 UPDATE (성능 최적화)

### 6.2 Service Layer (얇은 서비스)

```java

@Transactional
public DayFeedbackRequestDetailResponseDTO createDayRequest(
	CreateDayRequestDetailRequestDTO request,
	Long customerId
) {
	Customer customer = getCustomerById(customerId);

	// Entity에 비즈니스 로직 위임
	customer.incrementFeedbackCount();
	boolean rewarded = customer.rewardTokensIfEligible(
		RewardConstants.FEEDBACK_THRESHOLD,
		RewardConstants.TOKEN_REWARD_AMOUNT
	);

	if (rewarded) {
		log.info("🎉 Token reward milestone! customerId={}, count={}, earned={}",
			customerId, customer.getFeedbackRequestCount(),
			RewardConstants.TOKEN_REWARD_AMOUNT);
	}

	// JPA Dirty Checking이 자동으로 UPDATE!
	return DayFeedbackRequestDetailResponseDTO.of(saved);
}
```

**서비스 레이어 역할**:

- ❌ **하지 않는 것**: 비즈니스 로직 구현, 엔티티 상태 직접 조작
- ✅ **하는 것**: 트랜잭션 관리, 엔티티 간 협력 조율, 외부 시스템 통합

### 6.3 삭제 로직 (보안 강화)

```java
@Transactional
public Void deleteFeedbackRequest(Long feedbackRequestId, Long customerId) {
    FeedbackRequest feedbackRequest = feedbackRequestRepository
        .findById(feedbackRequestId)
        .orElseThrow(() -> new FeedbackRequestException(
            FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND
        ));

    // 권한 확인
    if (!feedbackRequest.getCustomer().getId().equals(customerId)) {
        throw new FeedbackRequestException(
            FeedbackRequestErrorStatus.DELETE_PERMISSION_DENIED
        );
    }

    // ✅ 누적 작성 횟수는 삭제 시에도 감소하지 않음!
    // decrementFeedbackCount() 호출 제거 → Exploit 차단

    feedbackRequestRepository.delete(feedbackRequest);
    log.info("Feedback deleted: feedbackRequestId={}, customerId={}",
        feedbackRequestId, customerId);

    return null;
}
```

### 6.4 데이터베이스 마이그레이션

```sql
-- 피드백 카운트 필드 추가
ALTER TABLE customer
    ADD COLUMN feedback_request_count INT DEFAULT 0 COMMENT '피드백 요청 누적 작성 횟수 (단조증가, 토큰 보상 기준)';

-- 기존 데이터 동기화
UPDATE customer c
SET c.feedback_request_count = (SELECT COUNT(*)
                                FROM feedback_request fr
                                WHERE fr.customer_id = c.user_id);

-- NOT NULL 제약 조건 추가
ALTER TABLE customer
    MODIFY COLUMN feedback_request_count INT NOT NULL DEFAULT 0;

-- 인덱스 추가 (통계 조회 성능 향상)
CREATE INDEX idx_customer_feedback_count
    ON customer (feedback_request_count);
```

---

## 7. 기술적 성과

### 7.1 보안 개선

- ✅ **Exploit 완전 차단**: 삭제-재작성 패턴으로 토큰 무한 획득 불가능
- ✅ **비즈니스 정책 명확화**: "누적 작성 횟수" 개념으로 재정의

### 7.2 코드 품질 향상

- ✅ **복잡도 감소**: 스케줄러, sync 메서드 제거 (약 150줄 감소)
- ✅ **DDD 원칙 준수**: Rich Domain Model, Tell Don't Ask
- ✅ **JPA 최적화**: Dirty Checking 활용, 불필요한 `save()` 제거

### 7.3 성능 최적화

- ✅ **쿼리 최적화**: 매번 `COUNT(*)` 불필요 → 단순 필드 조회
- ✅ **@DynamicUpdate**: 변경된 필드만 UPDATE (30-50% 성능 향상)
- ✅ **스케줄러 제거**: 매일 새벽 전체 고객 조회 및 동기화 불필요

### 7.4 유지보수성 강화

- ✅ **명확한 의미**: `feedbackRequestCount`의 의미가 명확함
- ✅ **간결한 코드**: 동기화 로직, 스케줄러 제거로 복잡도 감소
- ✅ **쉬운 테스트**: 단순한 로직으로 테스트 작성 용이

### 7.5 확장성

- ✅ **UI 통합 가능**: "다음 보상까지 N개 남음" 계산 메서드 제공
  ```java
  public int getRemainingFeedbacksForNextReward(int threshold) {
      int remainder = this.feedbackRequestCount % threshold;
      return threshold - remainder;
  }
  ```

---

## 📌 핵심 교훈 (Key Takeaways)

### 1. 보안은 초기 설계부터 고려해야 한다

- **문제**: 기능 구현 후 보안 취약점 발견 → 대규모 리팩토링
- **교훈**: 요구사항 분석 단계에서 악용 시나리오 검토 필수

### 2. 단순함이 최고의 해결책이다

- **오버엔지니어링 유혹**: TokenRewardHistory 테이블, 복잡한 동기화 로직
- **최종 선택**: Never Decrement (가장 간단하지만 효과적)

### 3. DDD는 유지보수성의 핵심이다

- **엔티티에 비즈니스 로직 캡슐화** → 서비스 레이어 얇아짐
- **Tell, Don't Ask** → 명확한 책임 분리

### 4. JPA를 제대로 활용하자

- **Dirty Checking**: 불필요한 `save()` 호출 제거
- **@DynamicUpdate**: 성능 최적화
- **엔티티 재생성 금지**: Builder로 재구성하지 말 것

### 5. 비즈니스 정책은 명확해야 한다

- **모호한 정책**: "현재 피드백 개수" → 삭제 시 어떻게?
- **명확한 정책**: "누적 작성 횟수" → 삭제 시 감소 안 함 (명확)

---

## 🔗 관련 문서

- [DDD_GUIDE.md](../DDD_GUIDE.md) - Domain-Driven Design 가이드
- [CLAUDE.md](../CLAUDE.md) - JPA Development 섹션
- [RewardConstants.java](../src/main/java/com/tradingpt/tpt_api/global/common/RewardConstants.java)
- [Customer.java](../src/main/java/com/tradingpt/tpt_api/domain/user/entity/Customer.java)
- [FeedbackRequestCommandServiceImpl.java](../src/main/java/com/tradingpt/tpt_api/domain/feedbackrequest/service/command/FeedbackRequestCommandServiceImpl.java)

---

**작성자**: 박동규
**최종 수정일**: 2025년 1월
**버전**: 1.0.0
