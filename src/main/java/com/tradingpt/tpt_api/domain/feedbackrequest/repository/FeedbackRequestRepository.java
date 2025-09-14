package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;

public interface FeedbackRequestRepository extends JpaRepository<FeedbackRequest, Long>, FeedbackRequestRepositoryCustom {
}
