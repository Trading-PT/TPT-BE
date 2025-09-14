package com.tradingpt.tpt_api.domain.feedbackresponse.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackresponse.entity.FeedbackResponse;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 피드백 답변 Command Service 구현체
 * 피드백 답변 생성, 수정과 관련된 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackResponseCommandServiceImpl implements FeedbackResponseCommandService {

    private final FeedbackRequestRepository feedbackRequestRepository;
    private final UserRepository userRepository;

    @Override
    public void createFeedbackResponse(Long feedbackRequestId, String responseContent, Long trainerId) {
        FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
            .orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

        if (feedbackRequest.getFeedbackResponse() != null) {
            throw new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_RESPONSE_ALREADY_EXISTS);
        }

        Trainer trainer = getTrainerById(trainerId);

        FeedbackResponse feedbackResponse = FeedbackResponse.createResponse(feedbackRequest, trainer, responseContent);

        feedbackRequest.setStatus(Status.DONE);
        // FeedbackResponse는 cascade로 저장됨
        feedbackRequestRepository.save(feedbackRequest);
    }

    @Override
    public void updateFeedbackResponse(Long feedbackRequestId, String responseContent, Long trainerId) {
        FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
            .orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

        FeedbackResponse feedbackResponse = feedbackRequest.getFeedbackResponse();
        if (feedbackResponse == null) {
            throw new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_RESPONSE_NOT_FOUND);
        }

        // 답변 작성자만 수정 가능
        if (!feedbackResponse.getTrainer().getId().equals(trainerId)) {
            throw new FeedbackRequestException(FeedbackRequestErrorStatus.RESPONSE_UPDATE_PERMISSION_DENIED);
        }

        feedbackResponse.updateContent(responseContent);
        feedbackRequestRepository.save(feedbackRequest);
    }

    private Trainer getTrainerById(Long trainerId) {
        return (Trainer) userRepository.findById(trainerId)
            .orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));
    }
}