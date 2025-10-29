package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * 특정 주의 피드백이 존재하는 날짜 목록 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
@Schema(description = "특정 주의 피드백이 존재하는 날짜 목록")
public class WeeklyDayFeedbackResponseDTO {

	@Schema(description = "연도", example = "2025")
	private Integer year;

	@Schema(description = "월", example = "7")
	private Integer month;

	@Schema(description = "주차", example = "3")
	private Integer week;

	@Schema(description = "피드백이 존재하는 날짜 목록 (일)", example = "[17, 19, 21, 22]")
	private List<Integer> days;

	public static WeeklyDayFeedbackResponseDTO of(
		Integer year,
		Integer month,
		Integer week,
		List<Integer> days
	) {
		return WeeklyDayFeedbackResponseDTO.builder()
			.year(year)
			.month(month)
			.week(week)
			.days(days)
			.build();
	}
}