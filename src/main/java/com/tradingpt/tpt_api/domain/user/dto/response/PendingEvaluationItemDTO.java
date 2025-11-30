package com.tradingpt.tpt_api.domain.user.dto.response;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.EvaluationType;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 미작성 평가 항목 DTO
 * 고객별 × 평가 대상별 = 행 분리 방식
 *
 * 예:
 * - 홍길동 | DAY | 11월 월간 평가
 * - 홍길동 | DAY | 11월 1주차 주간 평가
 * - 홍길동 | DAY | 11월 2주차 주간 평가
 */
@Getter
@Builder
@Schema(description = "미작성 평가 항목 DTO")
public class PendingEvaluationItemDTO {

	@Schema(description = "고객 ID", example = "123")
	private Long customerId;

	@Schema(description = "고객 이름", example = "홍길동")
	private String customerName;

	@Schema(description = "고객 전화번호", example = "010-1234-5678")
	private String phoneNumber;

	@Schema(description = "투자 유형", example = "DAY")
	private InvestmentType investmentType;

	@Schema(description = "평가 유형", example = "MONTHLY")
	private EvaluationType evaluationType;

	@Schema(description = "평가 대상 연도", example = "2025")
	private Integer targetYear;

	@Schema(description = "평가 대상 월", example = "11")
	private Integer targetMonth;

	@Schema(description = "평가 대상 주차 (월간 평가의 경우 null)", example = "3")
	private Integer targetWeek;

	@Schema(description = "평가 대상 기간 표시", example = "2025년 11월 3주차 주간 평가")
	private String targetPeriodDisplay;

	/**
	 * 월간 평가 항목 생성
	 */
	public static PendingEvaluationItemDTO monthly(
		Customer customer,
		int year,
		int month
	) {
		return PendingEvaluationItemDTO.builder()
			.customerId(customer.getId())
			.customerName(customer.getName())
			.phoneNumber(customer.getPhoneNumber())
			.investmentType(customer.getPrimaryInvestmentType())
			.evaluationType(EvaluationType.MONTHLY)
			.targetYear(year)
			.targetMonth(month)
			.targetWeek(null)
			.targetPeriodDisplay(String.format("%d년 %d월 월간 평가", year, month))
			.build();
	}

	/**
	 * 주간 평가 항목 생성
	 */
	public static PendingEvaluationItemDTO weekly(
		Customer customer,
		int year,
		int month,
		int week
	) {
		return PendingEvaluationItemDTO.builder()
			.customerId(customer.getId())
			.customerName(customer.getName())
			.phoneNumber(customer.getPhoneNumber())
			.investmentType(customer.getPrimaryInvestmentType())
			.evaluationType(EvaluationType.WEEKLY)
			.targetYear(year)
			.targetMonth(month)
			.targetWeek(week)
			.targetPeriodDisplay(String.format("%d년 %d월 %d주차 주간 평가", year, month, week))
			.build();
	}
}
