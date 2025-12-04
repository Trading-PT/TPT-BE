package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "주간 매매일지 트레이너 평가 응답 DTO")
public class WeeklyEvaluationResponseDTO {

	@Schema(description = "주간 매매일지 ID")
	private Long id;

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객 닉네임")
	private String customerNickname;

	@Schema(description = "평가 작성자 ID (ADMIN 또는 TRAINER)")
	private Long evaluatorId;

	@Schema(description = "평가 작성자 닉네임")
	private String evaluatorNickname;

	@Schema(description = "연도")
	private Integer year;

	@Schema(description = "월")
	private Integer month;

	@Schema(description = "주차")
	private Integer week;

	@Schema(description = "코스 상태")
	private CourseStatus courseStatus;

	@Schema(description = "투자 유형")
	private InvestmentType investmentType;

	@Schema(description = "주간 회원 매매 평가")
	private String weeklyEvaluation;

	@Schema(description = "수익난 매매 분석")
	private String weeklyProfitableTradingAnalysis;

	@Schema(description = "손실난 매매 분석")
	private String weeklyLossTradingAnalysis;

	@Schema(description = "트레이너 평가 시각")
	private LocalDateTime evaluatedAt;

	@Schema(description = "생성일시")
	private LocalDateTime createdAt;

	@Schema(description = "수정일시")
	private LocalDateTime updatedAt;

	/**
	 * WeeklyTradingSummary 엔티티로부터 DTO 생성
	 */
	public static WeeklyEvaluationResponseDTO from(WeeklyTradingSummary entity) {
		return WeeklyEvaluationResponseDTO.builder()
			.id(entity.getId())
			.customerId(entity.getCustomer().getId())
			.customerNickname(entity.getCustomer().getNickname())
			.evaluatorId(entity.getEvaluator() != null ? entity.getEvaluator().getId() : null)
			.evaluatorNickname(entity.getEvaluator() != null ? entity.getEvaluator().getNickname() : null)
			.year(entity.getPeriod().getYear())
			.month(entity.getPeriod().getMonth())
			.week(entity.getPeriod().getWeek())
			.courseStatus(entity.getCourseStatus())
			.investmentType(entity.getInvestmentType())
			.weeklyEvaluation(entity.getWeeklyEvaluation())
			.weeklyProfitableTradingAnalysis(entity.getWeeklyProfitableTradingAnalysis())
			.weeklyLossTradingAnalysis(entity.getWeeklyLossTradingAnalysis())
			.evaluatedAt(entity.getEvaluatedAt())
			.createdAt(entity.getCreatedAt())
			.updatedAt(entity.getUpdatedAt())
			.build();
	}
}
