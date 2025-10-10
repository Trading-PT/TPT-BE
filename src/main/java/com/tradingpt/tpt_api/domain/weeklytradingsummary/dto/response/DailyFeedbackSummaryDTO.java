package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import java.time.LocalDate;

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
@Schema(description = "피드백 요청 일별")
public class DailyFeedbackSummaryDTO {

	@Schema(description = "날짜")
	private LocalDate date;

	@Schema(description = "해당 주의 피드백 요청 수")
	private Integer totalCount;

	@Schema(description = "피드백 답변 읽음 상태")
	private Status status;

	public static DailyFeedbackSummaryDTO of(LocalDate date, Integer totalCount, Status status) {
		return DailyFeedbackSummaryDTO.builder()
			.date(date)
			.totalCount(totalCount)
			.status(status)
			.build();
	}

}
