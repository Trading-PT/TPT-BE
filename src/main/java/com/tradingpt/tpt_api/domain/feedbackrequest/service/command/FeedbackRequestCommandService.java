package com.tradingpt.tpt_api.domain.feedbackrequest.service.command;

import java.util.List;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateFeedbackRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.UpdateFeedbackRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestDetailResponseDTO;

public interface FeedbackRequestCommandService {

	/**
	 * 피드백 요청 생성 (DAY/SWING 통합)
	 *
	 * @param request 피드백 요청 생성 DTO
	 * @param customerId 고객 ID
	 * @return 생성된 피드백 요청 상세 응답 DTO
	 */
	FeedbackRequestDetailResponseDTO createFeedbackRequest(CreateFeedbackRequestDTO request, Long customerId);

	/**
	 * 피드백 요청 삭제
	 *
	 * @param feedbackRequestId 피드백 요청 ID
	 * @param customerId 고객 ID
	 * @return null
	 */
	Void deleteFeedbackRequest(Long feedbackRequestId, Long customerId);

	/**
	 * 피드백 요청 수정
	 *
	 * @param feedbackRequestId 피드백 요청 ID
	 * @param request 수정 요청 DTO
	 * @param customerId 고객 ID (소유권 검증용)
	 * @return 수정된 피드백 요청 상세 응답 DTO
	 * @throws FeedbackRequestException 권한이 없거나, 이미 답변 완료된 경우
	 */
	FeedbackRequestDetailResponseDTO updateFeedbackRequest(
		Long feedbackRequestId,
		UpdateFeedbackRequestDTO request,
		Long customerId
	);

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
