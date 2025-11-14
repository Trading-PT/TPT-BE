package com.tradingpt.tpt_api.domain.subscriptionplan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tradingpt.tpt_api.domain.subscriptionplan.entity.SubscriptionPlan;

/**
 * SubscriptionPlan 리포지토리
 */
@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

	/**
	 * 현재 활성화된 플랜 조회 (단일)
	 *
	 * @return 활성 플랜 (없으면 empty)
	 */
	Optional<SubscriptionPlan> findByIsActiveTrue();

}
