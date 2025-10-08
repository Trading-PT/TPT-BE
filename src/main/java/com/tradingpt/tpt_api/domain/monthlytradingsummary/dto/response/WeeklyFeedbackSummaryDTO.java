package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response;

import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

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

	@Schema(description = "해당 주의 피드백 요청 수")
	private Integer totalCount;

	@Schema(description = "해당 주에 피드백 답변의 상태 여부")
	private Status status;

	public static WeeklyFeedbackSummaryDTO of(Integer week, Integer totalCount, Status status) {
		return WeeklyFeedbackSummaryDTO.builder()
			.week(week)
			.totalCount(totalCount)
			.status(status)
			.build();
	}
}
