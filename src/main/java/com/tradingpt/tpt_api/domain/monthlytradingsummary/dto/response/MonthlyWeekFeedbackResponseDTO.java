package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response;

import java.util.List;

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
@Schema(description = "피드백이 존재하는 주 DTO")
public class MonthlyWeekFeedbackResponseDTO {

	@Schema(description = "연도", example = "2025")
	private Integer year;

	@Schema(description = "월", example = "7")
	private Integer month;

	@Schema(description = "피드백이 존재하는 주차 목록", example = "[1, 3, 4]")
	private List<Integer> weeks;

	public static MonthlyWeekFeedbackResponseDTO of(
		Integer year,
		Integer month,
		List<Integer> weeks
	) {
		return MonthlyWeekFeedbackResponseDTO.builder()
			.year(year)
			.month(month)
			.weeks(weeks)
			.build();
	}
}
