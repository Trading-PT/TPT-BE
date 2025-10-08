package com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response;

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
@Schema(description = "완강 전 고객 월별 요약")
public class BeforeCompletedCourseMonthlySummaryDTO extends MonthlySummaryResponseDTO {

	@Schema(description = "월별 통계 DTO")
	private MonthlyFeedbackSummaryResponseDTO monthlyFeedbackSummaryResponseDTO;

	@Schema(description = "월별 성과 비교")
	private PerformanceComparison<PerformanceComparison.MonthSnapshot> performanceComparison;

	public static BeforeCompletedCourseMonthlySummaryDTO of(
		CourseStatus courseStatus,
		InvestmentType investmentType, Integer year, Integer month,
		MonthlyFeedbackSummaryResponseDTO monthlyFeedbackSummaryResponseDTO,
		PerformanceComparison<PerformanceComparison.MonthSnapshot> performanceComparison) {

		return BeforeCompletedCourseMonthlySummaryDTO.builder()
			.courseStatus(courseStatus)
			.investmentType(investmentType)
			.year(year)
			.month(month)
			.monthlyFeedbackSummaryResponseDTO(monthlyFeedbackSummaryResponseDTO)
			.performanceComparison(performanceComparison)
			.build();
	}
}
