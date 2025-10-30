package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 특정 날짜의 피드백 목록 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "특정 날짜의 피드백 목록")
public class DailyFeedbackListResponseDTO {

	@Schema(description = "연도", example = "2025")
	private Integer year;

	@Schema(description = "월", example = "7")
	private Integer month;

	@Schema(description = "주차", example = "3")
	private Integer week;

	@Schema(description = "일", example = "22")
	private Integer day;

	@Schema(description = "피드백 목록")
	private List<DailyFeedbackListItemDTO> feedbacks;

	public static DailyFeedbackListResponseDTO of(
		Integer year,
		Integer month,
		Integer week,
		Integer day,
		List<DailyFeedbackListItemDTO> feedbacks
	) {
		return DailyFeedbackListResponseDTO.builder()
			.year(year)
			.month(month)
			.week(week)
			.day(day)
			.feedbacks(feedbacks)
			.build();
	}
}