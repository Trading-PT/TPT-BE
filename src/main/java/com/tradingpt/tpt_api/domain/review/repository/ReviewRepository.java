package com.tradingpt.tpt_api.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
