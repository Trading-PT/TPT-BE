package com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 특정 날짜의 피드백 목록 아이템 DTO
 */
@Getter
@Builder
@Schema(description = "특정 날짜의 피드백 정보")
public class DailyFeedbackListItemDTO {

	@Schema(description = "피드백 요청 ID")
	private Long feedbackId;

	@Schema(description = "피드백 제목")
	private String title;

	@Schema(description = "투자 타입", example = "DAY")
	private InvestmentType investmentType;

	@Schema(description = "코스 상태", example = "AFTER_COMPLETION")
	private CourseStatus courseStatus;

	@Schema(description = "피드백 상태", example = "FR")
	private Status status;

	@Schema(description = "작성 시간")
	private LocalDateTime createdAt;

	@Schema(description = "피드백 응답 여부")
	private Boolean hasResponse;

	public static DailyFeedbackListItemDTO from(FeedbackRequest feedbackRequest) {
		return DailyFeedbackListItemDTO.builder()
			.feedbackId(feedbackRequest.getId())
			.title(feedbackRequest.getTitle())
			.investmentType(feedbackRequest.getInvestmentType())
			.courseStatus(feedbackRequest.getCourseStatus())
			.status(feedbackRequest.getStatus())
			.createdAt(feedbackRequest.getCreatedAt())
			.hasResponse(feedbackRequest.getFeedbackResponse() != null)
			.build();
	}
}