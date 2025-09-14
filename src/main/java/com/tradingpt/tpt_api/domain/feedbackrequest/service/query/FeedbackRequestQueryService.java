package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestResponse;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FeedbackRequestQueryService {

    /**
     * 피드백 요청 목록 조회 (페이징)
     */
    Page<FeedbackRequestResponse> getFeedbackRequests(Pageable pageable, FeedbackType feedbackType, Status status, Long customerId);

    /**
     * 피드백 요청 상세 조회
     */
    Object getFeedbackRequestById(Long feedbackRequestId, Long currentUserId);

    /**
     * 내 피드백 요청 목록 조회
     */
    List<FeedbackRequestResponse> getMyFeedbackRequests(Long customerId, FeedbackType feedbackType, Status status);

    /**
     * 고객 피드백 요청 권한 확인
     */
    boolean hasAccessPermission(Long feedbackRequestId, Long currentUserId);
}