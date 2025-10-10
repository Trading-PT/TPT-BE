package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;

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
@Schema(description = "피드백 카드 DTO (목록용)")
public class FeedbackCardDTO {

	@Schema(description = "피드백 요청 ID", example = "123")
	private Long feedbackRequestId;

	@Schema(description = "제목", example = "8/24 (I) 작성 완료")
	private String title;

	@Schema(description = "내용 미리보기", example = "안녕하세요 트레이너님! 저번에 조언해주신 대로...")
	private String contentPreview;

	@Schema(description = "작성일시", example = "2025-08-03T21:54:00")
	private LocalDateTime createdAt;

	@Schema(description = "피드백 타입", example = "DAY")
	private FeedbackType feedbackType;

	@Schema(description = "완강 여부", example = "BEFORE_COMPLETION")
	private CourseStatus courseStatus;

	@Schema(description = "읽음 상태", example = "N")
	private Status status;

	@Schema(description = "베스트 피드백 여부 (왕관 표시)", example = "true")
	private Boolean isBestFeedback;

	@Schema(description = "작성자 이름", example = "홍길동")
	private String customerName;

	/**
	 * FeedbackRequest 엔티티로부터 카드 DTO 생성
	 */
	public static FeedbackCardDTO from(FeedbackRequest feedbackRequest) {
		return FeedbackCardDTO.builder()
			.feedbackRequestId(feedbackRequest.getId())
			.title(feedbackRequest.getTitle())
			.contentPreview(generatePreview(feedbackRequest.getTradingReview()))
			.createdAt(feedbackRequest.getCreatedAt())
			.feedbackType(feedbackRequest.getFeedbackType())
			.courseStatus(feedbackRequest.getCourseStatus())
			.status(feedbackRequest.getStatus())
			.isBestFeedback(feedbackRequest.getIsBestFeedback())
			.customerName(feedbackRequest.getCustomer().getName())
			.build();
	}

	/**
	 * 매매 복기 내용에서 미리보기 텍스트 생성 (최대 50자)
	 */
	private static String generatePreview(String tradingReview) {
		if (tradingReview == null || tradingReview.isBlank()) {
			return "내용 없음";
		}

		String preview = tradingReview.trim();
		if (preview.length() > 50) {
			return preview.substring(0, 50) + "...";
		}
		return preview;
	}
}