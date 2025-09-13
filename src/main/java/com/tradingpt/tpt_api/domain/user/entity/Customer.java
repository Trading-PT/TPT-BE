package com.tradingpt.tpt_api.domain.user.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.tradingpt.tpt_api.domain.user.enums.AccountStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.enums.Provider;
import com.tradingpt.tpt_api.domain.user.enums.Role;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
public class Customer extends User {

	/**
	 * 연관 관계 매핑
	 */
	@OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Uid> uids = new ArrayList<>();

	/**
	 * 필드
	 */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Provider provider;      // LOCAL, KAKAO, NAVER

	@Column(name = "provider_id")
	private String providerId; // 소셜 id

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

	// ⭐ 기존 Uid 객체를 추가하는 메서드 (패키지 접근 메서드 사용)
	public void addUid(Uid uid) {
		uids.add(uid);
		uid.assignCustomer(this); // 패키지 접근 메서드 사용
	}

	public void removeUid(Uid uid) {
		uids.remove(uid);
		// customer 참조는 제거하지 않음 (불변성 유지)
	}

}
