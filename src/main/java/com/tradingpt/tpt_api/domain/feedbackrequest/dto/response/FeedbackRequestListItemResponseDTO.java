package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 일별 피드백 요청 목록 조회용 DTO
 * 간단한 정보만 포함 (목록 표시용)
 */
@Getter
@Builder
@Schema(description = "피드백 요청 목록 아이템")
public class FeedbackRequestListItemResponseDTO {

	@Schema(description = "피드백 요청 ID")
	private Long id;

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객 이름")
	private String customerName;

	@Schema(description = "투자 타입", example = "DAY")
	private InvestmentType investmentType;

	@Schema(description = "코스 상태", example = "BEFORE_COMPLETION")
	private CourseStatus courseStatus;

	@Schema(description = "피드백 상태", example = "FN")
	private Status status;

	@Schema(description = "작성 시간")
	private LocalDateTime createdAt;

	@Schema(description = "피드백 작성 날짜")
	private LocalDate feedbackRequestDate;

	@Schema(description = "피드백 연도")
	private Integer feedbackYear;

	@Schema(description = "피드백 월")
	private Integer feedbackMonth;

	@Schema(description = "피드백 주")
	private Integer feedbackWeek;

	@Schema(description = "피드백 일")
	private Integer feedbackDay;

	@Schema(description = "베스트 피드백 여부")
	private Boolean isBestFeedback;

	/**
	 * FeedbackRequest 엔티티에서 DTO 생성
	 */
	public static FeedbackRequestListItemResponseDTO from(FeedbackRequest request) {
		return FeedbackRequestListItemResponseDTO.builder()
			.id(request.getId())
			.customerId(request.getCustomer().getId())
			.customerName(request.getCustomer().getUsername())
			.investmentType(request.getInvestmentType())
			.courseStatus(request.getCourseStatus())
			.status(request.getStatus())
			.createdAt(request.getCreatedAt())
			.feedbackRequestDate(request.getFeedbackRequestDate())
			.feedbackYear(request.getFeedbackYear())
			.feedbackMonth(request.getFeedbackMonth())
			.feedbackWeek(request.getFeedbackWeek())
			.feedbackDay(request.getFeedbackRequestDate().getDayOfMonth())
			.isBestFeedback(request.getIsBestFeedback())
			.build();
	}
}