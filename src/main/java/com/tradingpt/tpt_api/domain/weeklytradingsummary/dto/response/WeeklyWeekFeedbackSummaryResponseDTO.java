package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import java.time.LocalDate;

import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

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
@Schema(description = "각 일별 통계 DTO")
public class WeeklyWeekFeedbackSummaryResponseDTO {

	@Schema(description = "날짜")
	private LocalDate date;

	@Schema(description = "매매 횟수")
	private Integer tradingCount;

	@Schema(description = "수익 횟수")
	private Integer winCount;

	@Schema(description = "손실 횟수")
	private Integer lossCount;

	@Schema(description = "일간 P&L")
	private Double dailyPnl;

	@Schema(description = "피드백 답변 읽음 상태")
	private Status status;

}
