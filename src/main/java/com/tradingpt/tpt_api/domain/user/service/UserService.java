package com.tradingpt.tpt_api.domain.user.service;

public interface UserService {
    void ensureUnique(String username, String email, String phone);
}