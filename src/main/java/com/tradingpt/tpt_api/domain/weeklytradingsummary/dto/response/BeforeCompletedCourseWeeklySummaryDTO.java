package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "완강 전 고객 주간 매매 일지")
public class BeforeCompletedCourseWeeklySummaryDTO extends WeeklySummaryResponseDTO {
	private Long id;
}
