package com.tradingpt.tpt_api.domain.subscriptionplan.controller;

import com.tradingpt.tpt_api.domain.subscriptionplan.dto.response.SubscriptionPlanPriceResponseDTO;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.subscriptionplan.dto.response.SubscriptionPlanResponseDTO;
import com.tradingpt.tpt_api.domain.subscriptionplan.service.query.SubscriptionPlanQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 구독 플랜 조회 REST Controller (유저용)
 */
@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
@Tag(name = "결제 플랜 조회", description = "유저용 결제 플랜 조회 API")
public class SubscriptionPlanV1Controller {

    private final SubscriptionPlanQueryService subscriptionPlanQueryService;

    @Operation(
            summary = "현재 활성 구독 플랜 조회",
            description = """
            현재 활성화(is_active = true)된 구독 플랜 정보를 조회합니다.
            - 단일 활성 플랜만 존재한다고 가정
            - name, price 만 반환
            """
    )
    @GetMapping("/active")
    public BaseResponse<List<SubscriptionPlanPriceResponseDTO>> getActiveSubscriptionPlan() {
        return BaseResponse.onSuccess(subscriptionPlanQueryService.getActivePlans());
    }
}
