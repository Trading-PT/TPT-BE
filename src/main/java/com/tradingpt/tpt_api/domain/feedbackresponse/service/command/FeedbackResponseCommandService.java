package com.tradingpt.tpt_api.domain.feedbackresponse.service.command;

import com.tradingpt.tpt_api.domain.feedbackresponse.dto.request.CreateFeedbackResponseRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.request.UpdateFeedbackResponseRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.response.FeedbackResponseDTO;

/**
 * 피드백 답변 Command Service 인터페이스
 * 피드백 답변 생성, 수정과 관련된 비즈니스 로직 처리
 */
public interface FeedbackResponseCommandService {

	/**
	 * 피드백 답변 생성
	 *
	 * @param feedbackRequestId 피드백 요청 ID
	 * @param request
	 * @param trainerId         트레이너 ID
	 * @return
	 */
	FeedbackResponseDTO createFeedbackResponse(Long feedbackRequestId, CreateFeedbackResponseRequestDTO request,
		Long trainerId);

	/**
	 * 피드백 답변 수정
	 *
	 * @param feedbackRequestId 피드백 요청 ID
	 * @param request
	 * @param trainerId         트레이너 ID
	 * @return
	 */
	FeedbackResponseDTO updateFeedbackResponse(Long feedbackRequestId, UpdateFeedbackResponseRequestDTO request,
		Long trainerId);
}