package com.tradingpt.tpt_api.domain.user.service;

import com.tradingpt.tpt_api.domain.auth.dto.response.FindIdResponse;
import com.tradingpt.tpt_api.domain.auth.dto.response.MeResponse;

public interface UserService {
    void ensureUnique(String username, String email, String phone);
    FindIdResponse findUserId(String email);
    MeResponse getMe(Long userId);
}