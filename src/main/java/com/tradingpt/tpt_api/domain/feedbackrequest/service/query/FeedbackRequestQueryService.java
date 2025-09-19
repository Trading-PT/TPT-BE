package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

public interface FeedbackRequestQueryService {

	/**
	 * 피드백 요청 목록 조회 (페이징)
	 */
	Page<FeedbackRequestResponseDTO> getFeedbackRequests(Pageable pageable, FeedbackType feedbackType, Status status,
		Long customerId);

	/**
	 * 피드백 요청 상세 조회
	 */
	FeedbackRequestResponseDTO getFeedbackRequestById(Long feedbackRequestId, Long currentUserId);

	/**
	 * 내 피드백 요청 목록 조회
	 */
	List<FeedbackRequestResponseDTO> getMyFeedbackRequests(Long customerId, FeedbackType feedbackType, Status status);

	/**
	 * 고객 피드백 요청 권한 확인
	 */
	boolean hasAccessPermission(Long feedbackRequestId, Long currentUserId);
}