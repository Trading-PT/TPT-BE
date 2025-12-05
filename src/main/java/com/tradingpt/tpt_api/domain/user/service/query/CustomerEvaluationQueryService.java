package com.tradingpt.tpt_api.domain.user.service.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.user.dto.response.PendingEvaluationItemDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.PendingEvaluationListResponseDTO;

/**
 * 고객 평가 관리 Query Service
 */
public interface CustomerEvaluationQueryService {

	/**
	 * 미작성 평가 목록 조회
	 *
	 * 역할별 동작:
	 * - ADMIN: 모든 고객의 미작성 평가 목록 조회
	 * - TRAINER: 담당 고객의 미작성 평가 목록만 조회
	 *
	 * 비즈니스 규칙:
	 * - 완강 후(AFTER_COMPLETION) 고객만 조회
	 * - 완강 월부터 현재 월까지의 모든 미작성 평가
	 * - 월간 평가: 완강 후 DAY/SWING 모두
	 * - 주간 평가: 완강 후 DAY만, 현재 주차까지만
	 * - 고객 이름순 정렬
	 * - 무한 스크롤 (Slice 페이징)
	 *
	 * @param userId   현재 로그인한 사용자 ID (ADMIN 또는 TRAINER)
	 * @param pageable 페이징 정보
	 * @return 미작성 평가 목록 응답 DTO
	 */
	PendingEvaluationListResponseDTO getPendingEvaluations(Long userId, Pageable pageable);
}
