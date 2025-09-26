package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.repository.MonthlyFeedbackSummaryResult;

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

		@Schema(description = "해당 월에 읽지 않은 피드백 답변이 존재하는지 여부")
		private Boolean hasUnreadFeedbackResponse;

		@Schema(description = "해당 월에 트레이너 답변 대기 상태의 요청이 존재하는지 여부")
		private Boolean hasPendingTrainerResponse;

		public static MonthlyFeedbackSummaryDTO of(MonthlyFeedbackSummaryResult summary) {
			return YearlySummaryResponseDTO.MonthlyFeedbackSummaryDTO.builder()
				.month(summary.month())
				.totalCount(summary.totalCount().intValue())
				.hasUnreadFeedbackResponse(summary.unreadCount() != null && summary.unreadCount() > 0)
				.hasPendingTrainerResponse(summary.pendingCount() != null && summary.pendingCount() > 0)
				.build();
		}
	}
}
