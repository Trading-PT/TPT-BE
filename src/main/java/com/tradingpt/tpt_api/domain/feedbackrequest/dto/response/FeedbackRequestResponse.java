package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

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
@Schema(description = "피드백 요청 응답 DTO")
public class FeedbackRequestResponse {

	@Schema(description = "피드백 요청 ID")
	private Long id;

	@Schema(description = "고객 ID")
	private Long customerId;

	@Schema(description = "고객 이름")
	private String customerName;

	@Schema(description = "피드백 타입")
	private FeedbackType feedbackType;

	@Schema(description = "피드백 상태")
	private Status status;

	@Schema(description = "피드백 요청 일자")
	private LocalDate feedbackRequestedAt;

	@Schema(description = "완강 여부")
	private Boolean isCourseCompleted;

	@Schema(description = "피드백 연도")
	private Integer feedbackYear;

	@Schema(description = "피드백 월")
	private Integer feedbackMonth;

	@Schema(description = "피드백 주차")
	private Integer feedbackWeek;

	@Schema(description = "베스트 피드백 여부")
	private Boolean isBestFeedback;

	@Schema(description = "생성일시")
	private LocalDateTime createdAt;

	@Schema(description = "수정일시")
	private LocalDateTime updatedAt;

	@Schema(description = "피드백 응답 존재 여부")
	private Boolean hasFeedbackResponse;

	public static FeedbackRequestResponse of(FeedbackRequest feedbackRequest) {
		return FeedbackRequestResponse.builder()
				.id(feedbackRequest.getId())
				.customerId(feedbackRequest.getCustomer().getId())
				.customerName(feedbackRequest.getCustomer().getName())
				.feedbackType(feedbackRequest.getFeedbackType())
				.status(feedbackRequest.getStatus())
				.feedbackRequestedAt(feedbackRequest.getFeedbackRequestedAt())
				.isCourseCompleted(feedbackRequest.getIsCourseCompleted())
				.feedbackYear(feedbackRequest.getFeedbackYear())
				.feedbackMonth(feedbackRequest.getFeedbackMonth())
				.feedbackWeek(feedbackRequest.getFeedbackWeek())
				.isBestFeedback(feedbackRequest.getIsBestFeedback())
				.createdAt(feedbackRequest.getCreatedAt())
				.updatedAt(feedbackRequest.getUpdatedAt())
				.hasFeedbackResponse(feedbackRequest.getFeedbackResponse() != null)
				.build();
	}
}