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
@Schema(description = "주간 매매일지 메모 응답 DTO")
public class WeeklyMemoResponseDTO {

	@Schema(description = "주간 매매일지 ID")
	private Long id;

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객 닉네임")
	private String customerNickname;

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

	@Schema(description = "나의 문제점 메모")
	private String memo;

	@Schema(description = "생성일시")
	private LocalDateTime createdAt;

	@Schema(description = "수정일시")
	private LocalDateTime updatedAt;

	/**
	 * WeeklyTradingSummary 엔티티로부터 DTO 생성
	 */
	public static WeeklyMemoResponseDTO from(WeeklyTradingSummary entity) {
		return WeeklyMemoResponseDTO.builder()
			.id(entity.getId())
			.customerId(entity.getCustomer().getId())
			.customerNickname(entity.getCustomer().getNickname())
			.year(entity.getPeriod().getYear())
			.month(entity.getPeriod().getMonth())
			.week(entity.getPeriod().getWeek())
			.courseStatus(entity.getCourseStatus())
			.investmentType(entity.getInvestmentType())
			.memo(entity.getMemo())
			.createdAt(entity.getCreatedAt())
			.updatedAt(entity.getUpdatedAt())
			.build();
	}
}
