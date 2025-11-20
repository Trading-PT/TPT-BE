package com.tradingpt.tpt_api.domain.user.dto.response;

import com.tradingpt.tpt_api.domain.user.entity.Customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 신규 구독 고객 조회 응답 DTO
 * 구독 시작한지 24시간 이내인 고객 정보
 */
@Getter
@Builder
@Schema(description = "신규 구독 고객 응답 DTO")
public class NewSubscriptionCustomerResponseDTO {

	@Schema(description = "고객 ID", example = "123")
	private Long customerId;

	@Schema(description = "고객 이름", example = "김개똥")
	private String name;

	@Schema(description = "전화번호", example = "010-1234-5678")
	private String phoneNumber;

	@Schema(description = "레벨테스트 응시 여부", example = "true")
	private Boolean hasAttemptedLevelTest;

	@Schema(description = "레벨테스트 정보 (미응시 시 null)")
	private LevelTestInfo levelTestInfo;

	@Schema(description = "상담 여부", example = "true")
	private Boolean hasConsultation;

	@Schema(description = "배정된 트레이너 이름 (미배정 시 null)", example = "이지원")
	private String assignedTrainerName;

	/**
	 * Customer 엔티티로부터 NewSubscriptionCustomerResponseDTO 생성
	 *
	 * @param customer Customer 엔티티
	 * @param hasAttemptedLevelTest 레벨테스트 응시 여부
	 * @param levelTestInfo 레벨테스트 정보
	 * @param hasConsultation 상담 여부
	 * @return NewSubscriptionCustomerResponseDTO
	 */
	public static NewSubscriptionCustomerResponseDTO from(
		Customer customer,
		boolean hasAttemptedLevelTest,
		LevelTestInfo levelTestInfo,
		boolean hasConsultation
	) {
		return NewSubscriptionCustomerResponseDTO.builder()
			.customerId(customer.getId())
			.name(customer.getName())
			.phoneNumber(customer.getPhoneNumber())
			.hasAttemptedLevelTest(hasAttemptedLevelTest)
			.levelTestInfo(levelTestInfo)
			.hasConsultation(hasConsultation)
			.assignedTrainerName(customer.getAssignedTrainer() != null
				? customer.getAssignedTrainer().getName()
				: null)
			.build();
	}

	/**
	 * 레벨테스트 상세 정보
	 */
	@Getter
	@Builder
	@Schema(description = "레벨테스트 정보")
	public static class LevelTestInfo {

		@Schema(description = "레벨테스트 상태", example = "GRADED", allowableValues = {"SUBMITTED", "GRADING", "GRADED"})
		private String status;

		@Schema(description = "채점 결과 등급 (채점 완료 시)", example = "C")
		private String grade;

		@Schema(description = "채점 트레이너 이름 (채점 완료 시)", example = "이조교")
		private String gradingTrainerName;
	}
}
