package com.tradingpt.tpt_api.domain.subscriptionplan.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.subscriptionplan.dto.request.SubscriptionPlanCreateRequestDTO;
import com.tradingpt.tpt_api.domain.subscriptionplan.entity.SubscriptionPlan;
import com.tradingpt.tpt_api.domain.subscriptionplan.repository.SubscriptionPlanRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SubscriptionPlan Command Service 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubscriptionPlanCommandServiceImpl implements SubscriptionPlanCommandService {

	private final SubscriptionPlanRepository subscriptionPlanRepository;

	@Override
	public Long createSubscriptionPlan(SubscriptionPlanCreateRequestDTO request) {
		log.info("구독 플랜 등록 시작: name={}, price={}", request.getName(), request.getPrice());

		// 1. 기존 활성 플랜 비활성화 (단일 활성 플랜 보장)
		subscriptionPlanRepository.findByIsActiveTrue().ifPresent(existingPlan -> {
			log.info("기존 활성 플랜 비활성화: planId={}, name={}", existingPlan.getId(), existingPlan.getName());
			existingPlan.deactivate();
		});

		// 3. 새 플랜 생성 (무조건 활성 상태)
		SubscriptionPlan newPlan = SubscriptionPlan.builder()
			.name(request.getName())
			.price(request.getPrice())
			.build();

		// 4. 저장
		subscriptionPlanRepository.save(newPlan);

		return newPlan.getId();
	}
}
