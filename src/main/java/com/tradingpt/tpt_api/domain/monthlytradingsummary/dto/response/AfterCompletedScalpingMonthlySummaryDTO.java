package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "완강 후 고객 월별 요약 - 스캘핑")
public class AfterCompletedScalpingMonthlySummaryDTO extends MonthlySummaryResponseDTO {

	@Schema(description = "주별 트레이딩 피드백")
	private List<WeeklyFeedbackSummaryDTO> weeklyFeedbackSummaryDTOS;

}
