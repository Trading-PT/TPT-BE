package com.tradingpt.tpt_api.domain.review.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tradingpt.tpt_api.domain.review.entity.Review;
import com.tradingpt.tpt_api.domain.review.enums.Status;

public interface ReviewRepository extends JpaRepository<Review, Long> {

	List<Review> findByCustomerIdOrderBySubmittedAtDesc(Long customerId);

	// 전체 공개 리뷰 조회 - Slice (무한 스크롤)
	@Query("SELECT r " +
		"FROM Review r " +
		"LEFT JOIN FETCH r.customer " +
		"LEFT JOIN FETCH r.trainer " +
		"WHERE r.status = :status")
	Slice<Review> findByStatusSlice(@Param("status") Status status, Pageable pageable);
}
