package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "월별 트레이딩 피드백 DTO")
public class MonthlySummaryResponseDTO {

	@Schema(description = "월별 트레이딩 피드백 ID")
	private Long id;

}
