package com.tradingpt.tpt_api.domain.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.user.enums.AccountStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.enums.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
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
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<Uid> uids = new ArrayList<>();

	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<FeedbackRequest> feedbackRequests = new ArrayList<>();

	/**
	 * 필드
	 */

	@Column(name = "phone_number")
	private String phoneNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "primary_investment_type")
	private InvestmentType primaryInvestmentType;

	@Enumerated(EnumType.STRING)
	private AccountStatus status = AccountStatus.PENDING;

	@Enumerated(EnumType.STRING)
	@Column(name = "membership_level")
	private MembershipLevel membershipLevel;

	@Column(name = "membership_expired_at")
	private LocalDateTime membershipExpiredAt;

	@Column(name = "is_course_completed")
	private Boolean isCourseCompleted = Boolean.FALSE;

	// ⭐ getRole() 구현
	@Override
	public Role getRole() {
		return Role.ROLE_CUSTOMER;
	}

	// ⭐ 수정된 편의 메서드 - 새로운 Uid 객체를 생성하여 추가
	public void addUid(String exchangeName, String uidValue) {
		Uid uid = Uid.builder()
			.exchangeName(exchangeName)
			.uid(uidValue)
			.customer(this)
			.build();
		uids.add(uid);
	}

	public void addUid(Uid uid) {
		if (uids == null)
			uids = new ArrayList<>(); // 과거 데이터 대비 가드
		uids.add(uid);
		uid.assignCustomer(this);
	}

	public void removeUid(Uid uid) {
		uids.remove(uid);
		// customer 참조는 제거하지 않음 (불변성 유지)
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setMembershipLevel(MembershipLevel membershipLevel) {
		this.membershipLevel = membershipLevel;
	}

	public void setPrimaryInvestmentType(InvestmentType investmentType) {
		this.primaryInvestmentType = investmentType;
	}

}
