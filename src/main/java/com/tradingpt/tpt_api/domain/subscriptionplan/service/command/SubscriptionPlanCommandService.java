package com.tradingpt.tpt_api.domain.subscriptionplan.service.command;

import com.tradingpt.tpt_api.domain.subscriptionplan.dto.request.SubscriptionPlanCreateRequestDTO;

/**
 * SubscriptionPlan Command Service 인터페이스
 * 구독 플랜 생성, 수정, 삭제 등 CUD 작업을 담당
 */
public interface SubscriptionPlanCommandService {

	/**
	 * 새 구독 플랜 등록
	 * - 기존 활성 플랜이 있다면 자동으로 비활성화
	 * - 새 플랜을 활성화하여 등록
	 *
	 * @param request 플랜 생성 요청 DTO
	 * @return 생성된 플랜 ID
	 */
	Long createSubscriptionPlan(SubscriptionPlanCreateRequestDTO request);
}
