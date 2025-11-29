package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
@JsonTypeInfo(
	use = JsonTypeInfo.Id.NAME,
	include = JsonTypeInfo.As.EXISTING_PROPERTY,
	property = "courseStatus",  // courseStatus 값으로 자식 타입 구분
	visible = true
)
@JsonSubTypes({
	@JsonSubTypes.Type(value = BeforeCompletedCourseWeeklySummaryDTO.class, name = "BEFORE_COMPLETION"),
	@JsonSubTypes.Type(value = AfterCompletedDayWeeklySummaryDTO.class, name = "AFTER_COMPLETION_DAY"),
	@JsonSubTypes.Type(value = AfterCompletedSwingWeeklySummaryDTO.class, name = "AFTER_COMPLETION_GENERAL"),
})
@Schema(description = "주차별 트레이딩 피드백 기본 DTO")
public abstract class WeeklySummaryResponseDTO {

	@Schema(description = "고객의 완강 여부")
	private CourseStatus courseStatus;

	@Schema(description = "트레이딩 타입")
	private InvestmentType investmentType;

	@Schema(description = "연도")
	private Integer year;

	@Schema(description = "월")
	private Integer month;

	@Schema(description = "주차")
	private Integer week;
}