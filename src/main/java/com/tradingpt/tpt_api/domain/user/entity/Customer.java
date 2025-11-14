package com.tradingpt.tpt_api.domain.user.entity;

import com.tradingpt.tpt_api.domain.lecture.exception.LectureErrorStatus;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.tradingpt.tpt_api.domain.customermembershiphistory.entity.CustomerMembershipHistory;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeHistory;
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

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<CustomerMembershipHistory> customerMembershipHistories = new ArrayList<>();

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

	@Column(name = "open_chapter_number")
	private Integer openChapterNumber;

	@Builder.Default
	private Integer token = 0; // 토큰의 개수

	// ⭐ getRole() 구현
	@Override
	public Role getRole() {
		return Role.ROLE_CUSTOMER;
	}

	/** uid 값 통째로 교체/설정 */
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

	/** 값으로 신규 생성(없으면 생성, 있으면 값만 변경) */
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

	/** uid 제거 */
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
		this.membershipExpiredAt = membershipExpiredAt;
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

	public void setCourseStatus(CourseStatus status){this.courseStatus = status;}

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
}
