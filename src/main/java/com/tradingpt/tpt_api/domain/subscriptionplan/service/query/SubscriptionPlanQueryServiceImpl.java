package com.tradingpt.tpt_api.domain.subscriptionplan.service.query;

import com.tradingpt.tpt_api.domain.subscriptionplan.dto.response.SubscriptionPlanPriceResponseDTO;
import com.tradingpt.tpt_api.domain.subscriptionplan.entity.SubscriptionPlan;
import com.tradingpt.tpt_api.domain.subscriptionplan.repository.SubscriptionPlanRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SubscriptionPlanQueryServiceImpl implements SubscriptionPlanQueryService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionPlanPriceResponseDTO> getActivePlans() {

        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAllByIsActiveTrue();

        return plans.stream()
                .map(plan -> SubscriptionPlanPriceResponseDTO.builder()
                        .name(plan.getName())
                        .price(plan.getPrice())
                        .build())
                .toList();
    }
}
