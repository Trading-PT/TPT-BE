package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "피드백 요청 주간")
public class WeeklyFeedbackSummaryDTO {
	@Schema(description = "주")
	private Integer week;

	@Schema(description = "해당 월의 피드백 요청 수")
	private Integer totalCount;

	@Schema(description = "해당 월에 읽지 않은 피드백 답변이 존재하는지 여부")
	private Boolean hasUnreadFeedbackResponse;

	@Schema(description = "해당 월에 트레이너 답변 대기 상태의 요청이 존재하는지 여부")
	private Boolean hasPendingTrainerResponse;

	public static WeeklyFeedbackSummaryDTO of(
		Integer week, Integer totalCount, Boolean hasUnreadFeedbackResponse, Boolean hasPendingTrainerResponse
	) {
		return WeeklyFeedbackSummaryDTO.builder()
			.week(week)
			.totalCount(totalCount)
			.hasUnreadFeedbackResponse(hasUnreadFeedbackResponse)
			.hasPendingTrainerResponse(hasPendingTrainerResponse)
			.build();
	}

}
