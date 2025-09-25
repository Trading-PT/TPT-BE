package com.tradingpt.tpt_api.domain.user.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary;

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
@Schema(description = "트레이너가 담당하는 고객들의 월말/주말 평가 관리 리스트 DTO")
public class CustomerEvaluationResponseDTO {

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객 이름")
	private String name;

	@Schema(description = "고객 전화번호")
	private String phoneNumber;

	@Schema(description = "주요 투자 유형")
	private InvestmentType primaryInvestmentType;

	@Schema(description = "강의 완강 여부")
	private Boolean courseCompleted;

	@Schema(description = "월간 평가 상태")
	private EvaluationStatus monthlyEvaluation;

	@Schema(description = "주간 평가 상태")
	private EvaluationStatus weeklyEvaluation;

	public static CustomerEvaluationResponseDTO of(
		Customer customer,
		MonthlyTradingSummary monthlyTradingSummary,
		WeeklyTradingSummary weeklyTradingSummary
	) {
		return CustomerEvaluationResponseDTO.builder()
			.customerId(customer.getId())
			.name(customer.getName())
			.phoneNumber(customer.getPhoneNumber())
			.primaryInvestmentType(customer.getPrimaryInvestmentType())
			.courseCompleted(Boolean.TRUE.equals(customer.getIsCourseCompleted()))
			.monthlyEvaluation(EvaluationStatus.fromMonthly(monthlyTradingSummary))
			.weeklyEvaluation(EvaluationStatus.fromWeekly(weeklyTradingSummary))
			.build();
	}

	@Getter
	@Builder
	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor
	@Schema(description = "평가 상태 DTO")
	public static class EvaluationStatus {

		@Schema(description = "평가 요약 ID")
		private Long summaryId;

		@Schema(description = "평가 연도")
		private Integer year;

		@Schema(description = "평가 월")
		private Integer month;

		@Schema(description = "평가 주차 (주간 평가인 경우)")
		private Integer week;

		@Schema(description = "평가 작성 여부")
		private boolean evaluated;

		@Schema(description = "평가 작성 시각")
		private LocalDateTime evaluatedAt;

		@Schema(description = "화면 표시용 라벨")
		private String label;

		public static EvaluationStatus fromMonthly(MonthlyTradingSummary summary) {
			if (summary == null) {
				return EvaluationStatus.builder()
					.evaluated(false)
					.build();
			}

			Integer year = summary.getPeriod() != null ? summary.getPeriod().getYear() : null;
			Integer month = summary.getPeriod() != null ? summary.getPeriod().getMonth() : null;

			return EvaluationStatus.builder()
				.summaryId(summary.getId())
				.year(year)
				.month(month)
				.label(month != null ? String.format("%d-월말 평가", month) : null)
				.evaluated(true)
				.evaluatedAt(summary.getEvaluatedAt())
				.build();
		}

		public static EvaluationStatus fromWeekly(WeeklyTradingSummary summary) {
			if (summary == null) {
				return EvaluationStatus.builder()
					.evaluated(false)
					.build();
			}

			Integer year = summary.getPeriod() != null ? summary.getPeriod().getYear() : null;
			Integer month = summary.getPeriod() != null ? summary.getPeriod().getMonth() : null;
			Integer week = summary.getPeriod() != null ? summary.getPeriod().getWeek() : null;

			return EvaluationStatus.builder()
				.summaryId(summary.getId())
				.year(year)
				.month(month)
				.week(week)
				.label(month != null ? String.format("%d-주말 평가", month) : null)
				.evaluated(true)
				.evaluatedAt(summary.getEvaluatedAt())
				.build();
		}
	}
}
