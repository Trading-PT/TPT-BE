package com.tradingpt.tpt_api.domain.feedbackresponse.service.command;

/**
 * 피드백 답변 Command Service 인터페이스
 * 피드백 답변 생성, 수정과 관련된 비즈니스 로직 처리
 */
public interface FeedbackResponseCommandService {

    /**
     * 피드백 답변 생성
     *
     * @param feedbackRequestId 피드백 요청 ID
     * @param responseContent 답변 내용
     * @param trainerId 트레이너 ID
     */
    void createFeedbackResponse(Long feedbackRequestId, String responseContent, Long trainerId);

    /**
     * 피드백 답변 수정
     *
     * @param feedbackRequestId 피드백 요청 ID
     * @param responseContent 수정할 답변 내용
     * @param trainerId 트레이너 ID
     */
    void updateFeedbackResponse(Long feedbackRequestId, String responseContent, Long trainerId);
}