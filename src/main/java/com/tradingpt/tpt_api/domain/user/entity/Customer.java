package com.tradingpt.tpt_api.domain.user.entity;

import com.tradingpt.tpt_api.domain.user.enums.LeveltestStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeHistory;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureErrorStatus;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureException;
import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.user.enums.AccountStatus;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.enums.Role;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "customer")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@DiscriminatorValue(value = "ROLE_CUSTOMER")
@PrimaryKeyJoinColumn(name = "user_id")
@DynamicUpdate  // ⭐ 변경된 필드만 UPDATE 쿼리에 포함
@DynamicInsert
public class Customer extends User {

	/**
	 * 연관 관계 매핑
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trainer_id", nullable = true)
	private User assignedTrainer;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<PaymentMethod> paymentMethods = new ArrayList<>();

	@OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	private Uid uid;

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<FeedbackRequest> feedbackRequests = new ArrayList<>();

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<InvestmentTypeHistory> investmentHistories = new ArrayList<>();

	/**
	 * 필드
	 */

	@Column(name = "phone_number")
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "primary_investment_type")
	private InvestmentType primaryInvestmentType;

	@Enumerated(EnumType.STRING)
	@Builder.Default
	private AccountStatus status = AccountStatus.PENDING;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private UserStatus userStatus = UserStatus.UID_REVIEW_PENDING;

	@Enumerated(EnumType.STRING)
	@Column(name = "membership_level")
	private MembershipLevel membershipLevel;

	@Column(name = "membership_expired_at")
	private LocalDateTime membershipExpiredAt;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	private CourseStatus courseStatus = CourseStatus.BEFORE_COMPLETION;

	/**
	 * 완강 시점 (AFTER_COMPLETION으로 상태 변경된 시각)
	 * 완강 월/연도 확인 및 평가 대상 기간 판별에 사용
	 */
	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private LeveltestStatus leveltestStatus = LeveltestStatus.AVAILABLE;

	@Column(name = "open_chapter_number")
	private Integer openChapterNumber;

	@Builder.Default
	private Integer token = 0; // 토큰의 개수

	/**
	 * 피드백 요청 누적 작성 횟수
	 * 매매일지(피드백 요청) 작성 시 자동 증가 (단조증가)
	 * 삭제 시에는 감소하지 않음 (누적 개념)
	 * 토큰 보상 기준으로 사용됨 (N개마다 M개 토큰 지급)
	 */
	@Column(name = "feedback_request_count")
	@Builder.Default
	private Integer feedbackRequestCount = 0;

	// ⭐ getRole() 구현
	@Override
	public Role getRole() {
		return Role.ROLE_CUSTOMER;
	}

	/**
	 * uid 값 통째로 교체/설정
	 */
	public void setUid(Uid uid) {
		// 기존 연관 끊기
		if (this.uid != null) {
			this.uid.setCustomer(null);
		}
		this.uid = uid;
		if (uid != null) {
			uid.setCustomer(this);
		}
	}

	public void setAssignedTrainer(User trainer) {
		this.assignedTrainer = trainer;
	}

	/**
	 * 값으로 신규 생성(없으면 생성, 있으면 값만 변경)
	 */
	public void upsertUid(String exchangeName, String uidValue) {
		if (this.uid == null) {
			Uid newUid = Uid.builder()
				.exchangeName(exchangeName)
				.uid(uidValue)
				.customer(this)
				.build();
			this.uid = newUid;
		} else {
			this.uid.setExchangeName(exchangeName);
			this.uid.setUid(uidValue);
		}
	}

	/**
	 * uid 제거
	 */
	public void removeUid() {
		if (this.uid != null) {
			this.uid.setCustomer(null);
			this.uid = null;
		}
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setMembershipLevel(MembershipLevel membershipLevel) {
		this.membershipLevel = membershipLevel;
	}

	public void setMembershipExpiredAt(LocalDateTime time) {
		this.membershipExpiredAt = time;
	}

	public void setOpenChapterNumber(Integer chapterNumber) {
		this.openChapterNumber = openChapterNumber;
	}

	public void changeInvestmentType(InvestmentType investmentType, LocalDate effectiveDate) {
		// 변경 일자가 비어 있으면 잘못된 요청으로 간주
		if (effectiveDate == null) {
			throw new UserException(UserErrorStatus.INVALID_INVESTMENT_HISTORY_REQUEST);
		}

		// 과거 데이터 대비 컬렉션이 비어있을 수 있으므로 방어적으로 초기화
		if (investmentHistories == null) {
			investmentHistories = new ArrayList<>();
		}

		// 현재 진행 중인 마지막 투자 유형 이력을 찾음
		InvestmentTypeHistory latestOngoing = investmentHistories.stream()
			.filter(InvestmentTypeHistory::isOngoing)
			.max(Comparator.comparing(InvestmentTypeHistory::getStartDate,
				Comparator.nullsLast(Comparator.naturalOrder())))
			.orElse(null);

		// 동일한 유형으로 변경 요청이 오면 새 이력 없이 현재 상태만 동기화
		if (latestOngoing != null && latestOngoing.getInvestmentType() == investmentType) {
			this.primaryInvestmentType = investmentType;
			return;
		}

		// 기존에 열려 있던 이력은 종료일을 변경 전날로 설정
		if (latestOngoing != null) {
			latestOngoing.closeAt(effectiveDate.minusDays(1));
		}

		// 새로운 투자 유형이 지정되면 해당 일자부터 시작하는 이력을 추가
		if (investmentType != null) {
			InvestmentTypeHistory history = InvestmentTypeHistory.builder()
				.customer(this)
				.investmentType(investmentType)
				.startDate(effectiveDate)
				.build();
			history.assignCustomer(this);
			investmentHistories.add(history);
		}

		// 현재 고객의 주 투자 유형 값을 최신 상태로 반영
		this.primaryInvestmentType = investmentType;
	}

	public InvestmentType getInvestmentTypeOn(LocalDate date) {
		// 조회 날짜가 없으면 현재 저장된 주 투자 유형으로 응답
		if (date == null) {
			return primaryInvestmentType;
		}

		// 해당 날짜에 유효한 이력 중 가장 최근 시작분을 찾아 투자 유형을 결정
		return investmentHistories.stream()
			.filter(history -> history.isActiveOn(date))
			.max(Comparator.comparing(InvestmentTypeHistory::getStartDate,
				Comparator.nullsLast(Comparator.naturalOrder())))
			.map(InvestmentTypeHistory::getInvestmentType)
			.orElse(primaryInvestmentType);
	}

	// 사용자의 트레이딩 타입이 일치하지 않을 경우
	public void checkTradingType(InvestmentType tradingType) {
		if (primaryInvestmentType != null && !primaryInvestmentType.equals(tradingType)) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_INVESTMENT_TYPE_MISMATCH);
		}
	}

	// 사용자의 멤버쉽 여부에 대해서 일치 여부 확인
	public void checkMembership() {
	}

	public void setUserStatus(UserStatus status) {
		this.userStatus = status;
	}

	public void setCourseStatus(CourseStatus status) {
		this.courseStatus = status;
	}

	public void updatePrimaryInvestmentType(InvestmentType requestedType) {
		primaryInvestmentType = requestedType;
	}

	public void updateToken(Integer token) {
		this.token = token;
	}

	public void useTokens(int tokens) {
		if (this.token < tokens) {
			throw new LectureException(LectureErrorStatus.NOT_ENOUGH_TOKENS);
		}
		this.token -= tokens;
	}

	/**
	 * 멤버십 레벨 및 만료일 업데이트
	 * JPA dirty checking을 활용하여 변경 사항 자동 반영
	 */
	public void updateMembership(MembershipLevel membershipLevel, LocalDateTime expiredAt) {
		this.membershipLevel = membershipLevel;
		this.membershipExpiredAt = expiredAt;
	}

	/**
	 * 멤버십 활성 여부 확인
	 */
	public boolean isMembershipActive() {
		if (membershipLevel != MembershipLevel.PREMIUM) {
			return false;
		}
		if (membershipExpiredAt == null) {
			return false;
		}
		return LocalDateTime.now().isBefore(membershipExpiredAt);
	}

	public void updateOpenChapterNumber(int chapterNumber) {
		this.openChapterNumber = chapterNumber;
	}

	public void updateCourseStatus(CourseStatus status) {
		this.courseStatus = status;
	}

	/**
	 * 완강 처리 (AFTER_COMPLETION 상태로 변경 + 완강 시점 기록)
	 * JPA Dirty Checking을 활용하여 자동 UPDATE
	 *
	 * 비즈니스 규칙:
	 * - PENDING_COMPLETION -> AFTER_COMPLETION 전환 시 호출
	 * - 완강 시점을 기록하여 평가 대상 기간 판별에 활용
	 *
	 * @param completedAt 완강 시각 (보통 스케줄러 실행 시점)
	 */
	public void completeTraining(LocalDateTime completedAt) {
		this.courseStatus = CourseStatus.AFTER_COMPLETION;
		this.completedAt = completedAt;
	}

	// ========================================
	// 피드백 카운트 및 토큰 보상 비즈니스 메서드
	// ========================================

	/**
	 * 피드백 요청 누적 카운트 증가 (단조증가)
	 * 매매일지 작성 시 호출
	 * 삭제 시에는 감소하지 않음 (누적 작성 횟수 개념)
	 * JPA Dirty Checking을 활용하여 자동 UPDATE
	 */
	public void incrementFeedbackCount() {
		this.feedbackRequestCount++;
	}

	/**
	 * 조건 충족 시 토큰 보상
	 * N개마다 M개 토큰을 자동 발급
	 *
	 * @param threshold    몇 개마다 보상할지 (예: 5)
	 * @param rewardAmount 보상 토큰 개수 (예: 3)
	 * @return 보상 여부 (true: 보상 받음, false: 조건 미충족)
	 */
	public boolean rewardTokensIfEligible(int threshold, int rewardAmount) {
		// 카운트가 임계값의 배수인지 확인
		if (this.feedbackRequestCount > 0 && this.feedbackRequestCount % threshold == 0) {
			this.token += rewardAmount;
			return true;
		}
		return false;
	}

	/**
	 * 현재 다음 보상까지 남은 피드백 개수 계산
	 * UI에 표시하거나 로깅 용도로 사용 가능
	 *
	 * @param threshold 보상 임계값 (예: 5)
	 * @return 다음 보상까지 남은 피드백 개수
	 */
	public int getRemainingFeedbacksForNextReward(int threshold) {
		int remainder = this.feedbackRequestCount % threshold;
		return threshold - remainder;
	}
	// 토큰 더하는 메서드
	public void addToken(int amount) {
		if (amount <= 0) {
			return;
		}
		if (this.token == null) {
			this.token = 0;
		}
		this.token += amount;
	}

	// ========================================
	// 피드백 요청 검증 비즈니스 메서드
	// ========================================

	/**
	 * 요청한 완강 상태가 현재 고객의 완강 상태와 호환되는지 검증
	 * 불일치 시 FeedbackRequestException 발생
	 *
	 * @param requestCourseStatus 요청한 완강 상태
	 * @throws FeedbackRequestException courseStatus가 호환되지 않는 경우
	 */
	public void validateCourseStatusCompatibility(CourseStatus requestCourseStatus) {
		if (!this.courseStatus.isCompatibleWith(requestCourseStatus)) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.COURSE_STATUS_MISMATCH);
		}
	}

	/**
	 * 피드백 요청 시 토큰 검증 및 차감 (멤버십에 따른 분기 처리)
	 *
	 * 변경된 로직:
	 * - BASIC 멤버십: 토큰 사용 선택 가능
	 *   - useToken=true → 토큰 차감 후 트레이너가 볼 수 있음
	 *   - useToken=false → 기록용으로만 생성 (트레이너가 볼 수 없음)
	 * - PREMIUM 멤버십: 토큰 사용 불가
	 *
	 * @param useToken       토큰 사용 여부
	 * @param requiredTokens 차감할 토큰 개수 (서버에서 고정된 값)
	 * @return 실제로 토큰이 차감되었는지 여부
	 * @throws FeedbackRequestException PREMIUM이 토큰 사용 시도 또는 토큰 부족 시
	 */
	public boolean validateAndConsumeTokenForFeedback(Boolean useToken, int requiredTokens) {
		// PREMIUM 멤버십인 경우
		if (this.membershipLevel == MembershipLevel.PREMIUM) {
			if (Boolean.TRUE.equals(useToken)) {
				throw new FeedbackRequestException(
					FeedbackRequestErrorStatus.TOKEN_NOT_ALLOWED_FOR_PREMIUM_MEMBERSHIP);
			}
			// PREMIUM은 토큰 없이 자유롭게 생성 가능
			return false;
		}

		// BASIC 멤버십인 경우
		if (this.membershipLevel == MembershipLevel.BASIC) {
			if (Boolean.TRUE.equals(useToken)) {
				// 토큰 부족 체크
				if (this.token < requiredTokens) {
					throw new FeedbackRequestException(FeedbackRequestErrorStatus.INSUFFICIENT_TOKEN);
				}
				// 토큰 차감
				this.token -= requiredTokens;
				return true;
			}
			// 토큰 사용 안 함 → 기록용으로만 생성
			return false;
		}

		return false;
	}

	public void setLeveltestStatus(LeveltestStatus leveltestStatus) {
		this.leveltestStatus = leveltestStatus;
	}
}
