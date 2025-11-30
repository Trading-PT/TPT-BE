package com.tradingpt.tpt_api.domain.review.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tradingpt.tpt_api.domain.review.entity.ReviewTag;

public interface ReviewTagRepository extends JpaRepository<ReviewTag, Long> {

	/**
	 * 특정 ID 목록에 해당하는 태그들을 조회합니다.
	 * 리뷰 생성 시 선택된 태그들을 가져올 때 사용합니다.
	 */
	List<ReviewTag> findAllByIdIn(List<Long> ids);

	/**
	 * 모든 태그를 이름 순으로 조회합니다.
	 */
	List<ReviewTag> findAllByOrderByNameAsc();
}
