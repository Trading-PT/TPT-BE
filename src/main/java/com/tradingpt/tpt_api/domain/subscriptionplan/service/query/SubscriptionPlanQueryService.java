package com.tradingpt.tpt_api.domain.subscriptionplan.service.query;

import com.tradingpt.tpt_api.domain.subscriptionplan.dto.response.SubscriptionPlanPriceResponseDTO;
import java.util.List;

public interface SubscriptionPlanQueryService {
    List<SubscriptionPlanPriceResponseDTO> getActivePlans();
}
