package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 주간 이익 매매 피드백 목록 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "주간 이익 매매 피드백 목록")
public class WeeklyProfitFeedbackListResponseDTO {

	@Schema(description = "연도", example = "2025")
	private Integer year;

	@Schema(description = "월", example = "7")
	private Integer month;

	@Schema(description = "주차", example = "3")
	private Integer week;

	@Schema(description = "이익 매매 피드백 목록")
	private List<WeeklyFeedbackListItemDTO> profitFeedbacks;

	public static WeeklyProfitFeedbackListResponseDTO of(
		Integer year,
		Integer month,
		Integer week,
		List<FeedbackRequest> feedbackRequests
	) {
		return WeeklyProfitFeedbackListResponseDTO.builder()
			.year(year)
			.month(month)
			.week(week)
			.profitFeedbacks(
				feedbackRequests.stream()
					.map(WeeklyFeedbackListItemDTO::from)
					.collect(Collectors.toList())
			)
			.build();
	}
}
