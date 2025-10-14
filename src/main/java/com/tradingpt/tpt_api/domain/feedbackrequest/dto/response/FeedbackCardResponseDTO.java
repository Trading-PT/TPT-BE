package com.tradingpt.tpt_api.domain.feedbackrequest.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.DayRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.SwingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

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
public class FeedbackCardResponseDTO {

	@Schema(description = "피드백 요청 ID", example = "123")
	private Long feedbackRequestId;

	@Schema(description = "제목", example = "8/24 (I) 작성 완료")
	private String title;

	@Schema(description = "내용 미리보기", example = "안녕하세요 트레이너님! 저번에 조언해주신 대로...")
	private String contentPreview;

	@Schema(description = "작성일시", example = "2025-08-03T21:54:00")
	private LocalDateTime createdAt;

	@Schema(description = "투자 유형", example = "DAY")
	private InvestmentType investmentType;

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
	public static FeedbackCardResponseDTO from(FeedbackRequest feedbackRequest) {
		return FeedbackCardResponseDTO.builder()
			.feedbackRequestId(feedbackRequest.getId())
			.title(feedbackRequest.getTitle())
			.contentPreview(generatePreview(feedbackRequest))
			.createdAt(feedbackRequest.getCreatedAt())
			.investmentType(feedbackRequest.getInvestmentType())
			.courseStatus(feedbackRequest.getCourseStatus())
			.status(feedbackRequest.getStatus())
			.isBestFeedback(feedbackRequest.getIsBestFeedback())
			.customerName(feedbackRequest.getCustomer().getName())
			.build();
	}

	/**
	 * 조건에 따라 적절한 내용을 미리보기로 생성
	 *
	 * 규칙:
	 * 1. BEFORE_COMPLETION: tradingReview
	 * 2. AFTER_COMPLETION + SCALPING: tradingReview
	 * 3. AFTER_COMPLETION + (DAY or SWING): trainerFeedbackRequestContent
	 */
	private static String generatePreview(FeedbackRequest feedbackRequest) {
		String contentToPreview = null;

		// BEFORE_COMPLETION이면 무조건 tradingReview
		if (feedbackRequest.getCourseStatus() == CourseStatus.BEFORE_COMPLETION
			|| feedbackRequest.getCourseStatus() == CourseStatus.PENDING_COMPLETION) {
			contentToPreview = feedbackRequest.getTradingReview();
		}
		// AFTER_COMPLETION
		else if (feedbackRequest.getCourseStatus() == CourseStatus.AFTER_COMPLETION) {
			// SCALPING이면 tradingReview
			if (feedbackRequest.getInvestmentType() == InvestmentType.SCALPING) {
				contentToPreview = feedbackRequest.getTradingReview();
			}
			// DAY or SWING이면 trainerFeedbackRequestContent
			else if (feedbackRequest.getInvestmentType() == InvestmentType.DAY) {
				DayRequestDetail dayRequest = (DayRequestDetail)feedbackRequest;
				contentToPreview = dayRequest.getTrainerFeedbackRequestContent();
			} else if (feedbackRequest.getInvestmentType() == InvestmentType.SWING) {
				SwingRequestDetail swingRequest = (SwingRequestDetail)feedbackRequest;
				contentToPreview = swingRequest.getTrainerFeedbackRequestContent();
			}
		}

		// 내용이 없으면 기본 메시지
		if (contentToPreview == null || contentToPreview.isBlank()) {
			return "내용 없음";
		}

		// 최대 50자로 자르기
		String preview = contentToPreview.trim();
		if (preview.length() > 50) {
			return preview.substring(0, 50) + "...";
		}
		return preview;
	}
}