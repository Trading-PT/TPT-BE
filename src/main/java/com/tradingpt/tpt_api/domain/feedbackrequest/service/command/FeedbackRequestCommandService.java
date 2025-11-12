package com.tradingpt.tpt_api.domain.feedbackrequest.service.command;

import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateDayRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateScalpingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateSwingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DayFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.ScalpingFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.SwingFeedbackRequestDetailResponseDTO;

public interface FeedbackRequestCommandService {

	/**
	 * 데이 트레이딩 피드백 요청 생성
	 */
	DayFeedbackRequestDetailResponseDTO createDayRequest(CreateDayRequestDetailRequestDTO request, Long customerId);

	/**
	 * 스켈핑 피드백 요청 생성
	 */
	ScalpingFeedbackRequestDetailResponseDTO createScalpingRequest(CreateScalpingRequestDetailRequestDTO request,
		Long customerId);

	/**
	 * 스윙 피드백 요청 생성
	 */
	SwingFeedbackRequestDetailResponseDTO createSwingRequest(CreateSwingRequestDetailRequestDTO request,
		Long customerId);

	/**
	 * 피드백 요청 삭제
	 *
	 * @return
	 */
	Void deleteFeedbackRequest(Long feedbackRequestId, Long customerId);

	/**
	 * 베스트 피드백 일괄 업데이트
	 * - 기존 베스트 피드백을 모두 해제하고
	 * - 새로운 피드백들을 베스트로 지정
	 *
	 * @param feedbackIds 베스트로 지정할 피드백 ID 목록
	 *                    (최대 {@link com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest#MAX_BEST_FEEDBACK_COUNT}개)
	 * @throws FeedbackRequestException 피드백을 찾을 수 없거나, 최대 개수 초과 시
	 */
	Void updateBestFeedbacks(List<Long> feedbackIds);
}
