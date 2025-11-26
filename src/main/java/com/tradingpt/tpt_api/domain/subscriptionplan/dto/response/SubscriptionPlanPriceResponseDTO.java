package com.tradingpt.tpt_api.domain.subscriptionplan.dto.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SubscriptionPlanPriceResponseDTO {

    @Schema(description = "구독 상품명", example = "프리미엄 멤버십")
    private String name;

    @Schema(description = "구독 가격", example = "59000.00")
    private BigDecimal price;
}
