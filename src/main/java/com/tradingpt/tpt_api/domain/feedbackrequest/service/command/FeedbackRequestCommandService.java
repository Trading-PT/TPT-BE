package com.tradingpt.tpt_api.domain.feedbackrequest.service.command;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateDayRequestDetailRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateScalpingRequestDetailRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateSwingRequestDetailRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestResponse;

public interface FeedbackRequestCommandService {

    /**
     * 데이 트레이딩 피드백 요청 생성
     */
    FeedbackRequestResponse createDayRequest(CreateDayRequestDetailRequest request, Long customerId);

    /**
     * 스켈핑 피드백 요청 생성
     */
    FeedbackRequestResponse createScalpingRequest(CreateScalpingRequestDetailRequest request, Long customerId);

    /**
     * 스윙 피드백 요청 생성
     */
    FeedbackRequestResponse createSwingRequest(CreateSwingRequestDetailRequest request, Long customerId);

    /**
     * 피드백 요청 삭제
     */
    void deleteFeedbackRequest(Long feedbackRequestId, Long customerId);
}
