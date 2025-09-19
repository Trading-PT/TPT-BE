package com.tradingpt.tpt_api.domain.feedbackrequest.service.command;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateDayRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateScalpingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateSwingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DayRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestResponseDTO;

public interface FeedbackRequestCommandService {

	/**
	 * 데이 트레이딩 피드백 요청 생성
	 */
	DayRequestDetailResponseDTO createDayRequest(CreateDayRequestDetailRequestDTO request, Long customerId);

	/**
	 * 스켈핑 피드백 요청 생성
	 */
	FeedbackRequestResponseDTO createScalpingRequest(CreateScalpingRequestDetailRequestDTO request, Long customerId);

	/**
	 * 스윙 피드백 요청 생성
	 */
	FeedbackRequestResponseDTO createSwingRequest(CreateSwingRequestDetailRequestDTO request, Long customerId);

	/**
	 * 피드백 요청 삭제
	 *
	 * @return
	 */
	Void deleteFeedbackRequest(Long feedbackRequestId, Long customerId);
}
