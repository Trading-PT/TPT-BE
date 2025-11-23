package com.tradingpt.tpt_api.domain.event.service;

import com.tradingpt.tpt_api.domain.user.entity.Customer;

public interface EventTokenService {
    void grantSignupTokens(Customer customer);
}
