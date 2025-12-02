package com.tradingpt.tpt_api.domain.auth.infrastructure;

import jakarta.servlet.http.HttpSession;

public interface VerificationService {
    void sendPhoneCode(String phone, HttpSession session);
    void verifyPhone(String phone, String code, HttpSession session);

    void sendEmailCode(String email, HttpSession session);
    void verifyEmail(String email, String code, HttpSession session);


    void clearPhoneTrace(String phoneNumber);
    void clearEmailTrace(String email);

    void requireVerified(String phone, String email, HttpSession session);

    void markEmailVerifiedWithoutCode(String email, HttpSession session);
}
