package com.tradingpt.tpt_api.domain.auth.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MeResponse {
	private String name;
	private String username;
	private String email;
	private String phoneNumber;
	private String uid;
	private String exchangeName;
	private String paymentMethod;
	private UserStatus userStatus;
	private Long trainerId;
	private String trainerName;
	private String profileImage;
	private String investmentType;
	private Boolean isCourseCompleted;
	private Boolean isPremium;

	public static MeResponse from(Customer c) {
		if (c == null)
			return null;

		boolean isPremium = MembershipLevel.PREMIUM.equals(c.getMembershipLevel())
			&& c.getMembershipExpiredAt() != null
			&& c.getMembershipExpiredAt().isAfter(LocalDateTime.now());

		String investmentType = (c.getPrimaryInvestmentType() != null)
			? c.getPrimaryInvestmentType().name()
			: null;

		Boolean isCourseCompleted = CourseStatus.AFTER_COMPLETION.equals(c.getCourseStatus());

		// UID 정보
		String uidValue = (c.getUid() != null) ? c.getUid().getUid() : null;
		String exchangeName = (c.getUid() != null) ? c.getUid().getExchangeName() : null;

		// 트레이너 정보
		String trainerName = (c.getTrainer() != null) ? c.getTrainer().getName() : null;
		Long trainerId = (c.getTrainer() != null) ? c.getTrainer().getId() : null;

		// 결제수단 (활성 중인 것 1개)
		String paymentMethod = null;
		if (c.getPaymentMethods() != null && !c.getPaymentMethods().isEmpty()) {
			paymentMethod = c.getPaymentMethods().stream()
				.filter(pm -> pm.isActive() && !pm.isExpired())
				.findFirst()
				.map(pm -> pm.getDisplayName())
				.orElse(null);
		}

		return MeResponse.builder()
			.name(c.getName())
			.username(c.getUsername())
			.email(c.getEmail())
			.phoneNumber(c.getPhoneNumber())
			.uid(uidValue)
			.exchangeName(exchangeName)
			.paymentMethod(paymentMethod)
			.userStatus(c.getUserStatus())
			.trainerId(trainerId)
			.trainerName(trainerName)
			.profileImage((c.getTrainer() != null) ? c.getTrainer().getProfileImageUrl() : null)
			.investmentType(investmentType)
			.isCourseCompleted(isCourseCompleted)
			.isPremium(isPremium)
			.build();
	}

}

