package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.MonthlyFeedbackSummary;

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
@Schema(description = "특정 연도의 월별 트레이딩 피드백 요약 응답 DTO")
public class YearlySummaryResponseDTO {

	@Schema(description = "요약 연도")
	private Integer feedbackYear;

	@Schema(description = "해당 연도에 피드백이 존재하는 월 리스트")
	private List<MonthlyFeedbackSummaryDTO> months;

	public static YearlySummaryResponseDTO of(Integer year, List<MonthlyFeedbackSummaryDTO> months) {
		return YearlySummaryResponseDTO.builder()
			.feedbackYear(year)
			.months(months)
			.build();
	}

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor
	@Schema(description = "월별 트레이딩 피드백 요약 DTO")
	public static class MonthlyFeedbackSummaryDTO {

		@Schema(description = "월")
		private Integer month;

		@Schema(description = "해당 월의 피드백 요청 수")
		private Integer totalCount;

		@Schema(description = "피드백 답변 여부, 피드백 답변 읽음 여부 상태")
		private Status status;

		public static MonthlyFeedbackSummaryDTO of(MonthlyFeedbackSummary summary) {
			return MonthlyFeedbackSummaryDTO.builder()
				.month(summary.getMonth())
				.totalCount(summary.getTotalCount().intValue())
				.status(summary.getStatus())
				.build();
		}
	}
}
