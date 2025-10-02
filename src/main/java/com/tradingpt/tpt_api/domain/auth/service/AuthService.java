package com.tradingpt.tpt_api.domain.auth.service;


import com.tradingpt.tpt_api.domain.auth.dto.request.PasswordUpdateRequestDTO;
import com.tradingpt.tpt_api.domain.auth.dto.request.SendEmailCodeRequestDTO;
import com.tradingpt.tpt_api.domain.auth.dto.request.SendPhoneCodeRequestDTO;
import com.tradingpt.tpt_api.domain.auth.dto.request.SignUpRequestDTO;
import com.tradingpt.tpt_api.domain.auth.dto.request.VerifyCodeRequestDTO;
import com.tradingpt.tpt_api.domain.auth.dto.response.PasswordUpdateResponseDTO;
import jakarta.servlet.http.HttpSession;

public interface AuthService {
    void sendPhoneCode(SendPhoneCodeRequestDTO req, HttpSession session);
    void verifyPhoneCode(VerifyCodeRequestDTO req, HttpSession session);

    void sendEmailCode(SendEmailCodeRequestDTO req, HttpSession session);
    void verifyEmailCode(VerifyCodeRequestDTO req, HttpSession session);

    void signUp(SignUpRequestDTO req, HttpSession session);

    boolean isUsernameAvailable(String username);

    PasswordUpdateResponseDTO resetPasswordAndInvalidateDevices(PasswordUpdateRequestDTO dto);
}
