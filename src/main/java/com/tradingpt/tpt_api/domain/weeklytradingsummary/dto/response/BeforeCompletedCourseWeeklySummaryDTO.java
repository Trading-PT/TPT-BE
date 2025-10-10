package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.PerformanceComparison;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "완강 전 고객 주간 매매 일지")
public class BeforeCompletedCourseWeeklySummaryDTO extends WeeklySummaryResponseDTO {

	@Schema(description = "주별 통계 DTO")
	private WeeklyFeedbackSummaryResponseDTO weeklyFeedbackSummaryResponseDTO;

	@Schema(description = "주별 성과 비교")
	private PerformanceComparison<PerformanceComparison.WeekSnapshot> performanceComparison;

	@Schema(description = "이번 주 나의 매매 중 가장 큰 문제점 메모하기")
	private String memo;

	public static BeforeCompletedCourseWeeklySummaryDTO of(
		CourseStatus courseStatus,
		InvestmentType investmentType,
		Integer year,
		Integer month,
		Integer week,
		WeeklyFeedbackSummaryResponseDTO weeklyFeedbackSummaryResponseDTO,
		PerformanceComparison<PerformanceComparison.WeekSnapshot> performanceComparison,
		String memo
	) {
		return BeforeCompletedCourseWeeklySummaryDTO.builder()
			.courseStatus(courseStatus)
			.investmentType(investmentType)
			.year(year)
			.month(month)
			.week(week)
			.weeklyFeedbackSummaryResponseDTO(weeklyFeedbackSummaryResponseDTO)
			.performanceComparison(performanceComparison)
			.memo(memo)
			.build();
	}

}
