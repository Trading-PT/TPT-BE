package com.tradingpt.tpt_api.domain.subscription.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구독 고객 목록 응답 DTO
 * 관리자용 구독 고객 목록 조회 시 사용
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "구독 고객 정보 응답")
public class SubscriptionCustomerResponseDTO {

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객명")
	private String name;

	@Schema(description = "전화번호")
	private String phoneNumber;

	@Schema(description = "고객 UID (미등록 시 null)", example = "123456789")
	private String uid;

	@Schema(description = "배정된 트레이너 이름 (미배정 시 null)")
	private String trainerName;

	@Schema(description = "작성한 피드백 요청 총 개수")
	private Long feedbackRequestCount;
}
