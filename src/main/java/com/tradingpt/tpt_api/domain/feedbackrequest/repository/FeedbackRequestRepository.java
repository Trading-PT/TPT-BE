package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;

public interface FeedbackRequestRepository
	extends JpaRepository<FeedbackRequest, Long>, FeedbackRequestRepositoryCustom {

	Optional<FeedbackRequest> findByCustomer_IdAndFeedbackYearAndFeedbackMonth(
		Long customerId,
		Integer year,
		Integer month
	);
}
