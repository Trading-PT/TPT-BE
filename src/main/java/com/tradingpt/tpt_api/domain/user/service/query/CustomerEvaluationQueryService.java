package com.tradingpt.tpt_api.domain.user.service.query;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.user.dto.response.PendingEvaluationItemDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.PendingEvaluationListResponseDTO;

/**
 * 트레이너의 담당 고객 평가 관리 Query Service
 */
public interface CustomerEvaluationQueryService {

	/**
	 * 트레이너의 담당 고객 미작성 평가 목록 조회
	 *
	 * 비즈니스 규칙:
	 * - 완강 후(AFTER_COMPLETION) 고객만 조회
	 * - 완강 월부터 현재 월까지의 모든 미작성 평가
	 * - 월간 평가: 완강 후 DAY/SWING 모두
	 * - 주간 평가: 완강 후 DAY만, 현재 주차까지만
	 * - 고객 이름순 정렬
	 * - 무한 스크롤 (Slice 페이징)
	 *
	 * @param trainerId 트레이너 ID
	 * @param pageable  페이징 정보
	 * @return 미작성 평가 목록 응답 DTO
	 */
	PendingEvaluationListResponseDTO getPendingEvaluations(Long trainerId, Pageable pageable);
}
