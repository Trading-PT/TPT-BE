package com.tradingpt.tpt_api.domain.auth.service;


import com.tradingpt.tpt_api.domain.auth.dto.request.SendEmailCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SendPhoneCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SignUpRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.VerifyCodeRequest;
import jakarta.servlet.http.HttpSession;

public interface AuthService {
    void sendPhoneCode(SendPhoneCodeRequest req, HttpSession session);
    void verifyPhoneCode(VerifyCodeRequest req, HttpSession session);

    void sendEmailCode(SendEmailCodeRequest req, HttpSession session);
    void verifyEmailCode(VerifyCodeRequest req, HttpSession session);

    void signUp(SignUpRequest req, HttpSession session);

    boolean isUsernameAvailable(String username);
}
