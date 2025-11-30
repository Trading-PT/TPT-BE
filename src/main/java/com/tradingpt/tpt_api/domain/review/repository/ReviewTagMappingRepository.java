package com.tradingpt.tpt_api.domain.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tradingpt.tpt_api.domain.review.entity.ReviewTagMapping;

public interface ReviewTagMappingRepository extends JpaRepository<ReviewTagMapping, Long> {

	/**
	 * 태그별 리뷰 개수를 조회합니다.
	 * 통계 화면에서 각 태그가 선택된 횟수를 표시할 때 사용합니다.
	 */
	@Query("SELECT rtm.reviewTag.id, rtm.reviewTag.name, COUNT(rtm) " +
		"FROM ReviewTagMapping rtm " +
		"GROUP BY rtm.reviewTag.id, rtm.reviewTag.name " +
		"ORDER BY COUNT(rtm) DESC")
	List<Object[]> countReviewsByTag();

	/**
	 * 특정 리뷰의 태그 매핑들을 조회합니다.
	 */
	List<ReviewTagMapping> findByReviewId(Long reviewId);
}
