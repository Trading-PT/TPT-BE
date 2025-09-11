package com.tradingpt.tpt_api.domain.auth.service;

import com.tradingpt.tpt_api.domain.auth.dto.request.SendEmailCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SendPhoneCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SignUpRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.VerifyCodeRequest;
import com.tradingpt.tpt_api.domain.auth.support.VerificationService;
import com.tradingpt.tpt_api.domain.auth.util.AuthUtil;
import com.tradingpt.tpt_api.global.exception.AuthException;
import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.domain.user.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.user.entity.Uid;
import com.tradingpt.tpt_api.domain.user.user.entity.User;
import com.tradingpt.tpt_api.domain.user.user.enums.AccountStatus;
import com.tradingpt.tpt_api.domain.user.user.enums.Provider;
import com.tradingpt.tpt_api.domain.user.user.enums.Role;
import com.tradingpt.tpt_api.domain.user.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final VerificationService verificationService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /* === 휴대폰 인증 === */
    @Override
    public void sendPhoneCode(SendPhoneCodeRequest req, HttpSession session) {
        final String phone = AuthUtil.normalizePhone(req.getPhone());
        verificationService.sendPhoneCode(phone, session);
    }

    @Override
    public void verifyPhoneCode(VerifyCodeRequest req, HttpSession session) {
        final String phone = AuthUtil.normalizePhone(req.getValue());
        verificationService.verifyPhone(phone, req.getCode(), session);
    }

    /* === 이메일 인증 === */
    @Override
    public void sendEmailCode(SendEmailCodeRequest req, HttpSession session) {
        final String email = AuthUtil.normalizeEmail(req.getEmail());
        verificationService.sendEmailCode(email, session);
    }

    @Override
    public void verifyEmailCode(VerifyCodeRequest req, HttpSession session) {
        final String email = AuthUtil.normalizeEmail(req.getValue());
        verificationService.verifyEmail(email, req.getCode(), session);
    }

    /* === 회원가입 === */
    @Override
    @Transactional
    public void signUp(SignUpRequest req, HttpSession session) {
        // 1) 기본 검증
        if (!req.getPassword().equals(req.getPasswordCheck())) {
            throw new AuthException(AuthErrorStatus.PASSWORD_MISMATCH);
        }
        if (!Boolean.TRUE.equals(req.getTermsService()) || !Boolean.TRUE.equals(req.getTermsPrivacy())) {
            throw new AuthException(AuthErrorStatus.TERMS_AGREEMENT_REQUIRED, "필수 약관에 동의해 주세요.");
        }

        // 2) 정규화
        final String phone = AuthUtil.normalizePhone(req.getPhone());
        final String email = AuthUtil.normalizeEmail(req.getEmail());

        // 3) 인증 요구(휴대폰/이메일)
        verificationService.requireVerified(phone, email, session);

        // 4) 중복 체크
        userService.ensureUnique(req.getUsername(), email, phone);

        // 5) User 저장
        User user = User.builder()
                .role(Role.ROLE_CUSTOMER)
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .email(email)
                .name(req.getName())
                .build();
        user = userService.saveUser(user);

        // 6) Customer 조립 (투자유형 포함)
        Customer customer = Customer.builder()
                .user(user)
                .provider(Provider.LOCAL)
                .providerId(null)
                .phoneNumber(phone)
                .status(AccountStatus.PENDING)
                .primaryInvestmentType(req.getInvestmentType()) // 엔티티 필드명에 맞춤
                .build();

        // 7) UID 처리 (최대 5개, 중복 금지, 길이 검증)
        if (req.getUids() != null && !req.getUids().isEmpty()) {
            if (req.getUids().size() > 5) {
                throw new AuthException(AuthErrorStatus.INVALID_INPUT_FORMAT, "UID는 최대 5개까지 등록 가능합니다.");
            }
            Set<String> seen = new HashSet<>();
            for (SignUpRequest.UidRequest u : req.getUids()) {
                String exchange = u.getExchangeName() == null ? "" : u.getExchangeName().trim();
                String uidValue  = u.getUid() == null ? "" : u.getUid().trim();

                if (exchange.isEmpty()) {
                    throw new AuthException(AuthErrorStatus.REQUIRED_FIELD_MISSING, "거래소명을 입력해 주세요.");
                }
                if (uidValue.isEmpty()) {
                    throw new AuthException(AuthErrorStatus.REQUIRED_FIELD_MISSING, "UID를 입력해 주세요.");
                }
                if (!seen.add(uidValue)) {
                    throw new AuthException(AuthErrorStatus.INVALID_INPUT_FORMAT, "중복된 UID가 포함되어 있습니다: " + uidValue);
                }

                Uid uid = Uid.builder()
                        .exchangeName(exchange)
                        .uid(uidValue)
                        .build();
                // 양방향 편의 메서드 (customer.addUid가 customer 필드 세팅해줌)
                customer.addUid(uid);
            }
        }

        // 8) 저장 (cascade로 UID까지 함께 저장)
        userService.saveCustomer(customer);

        // 9) 인증 흔적 정리
        verificationService.clearPhoneTrace(phone);
        verificationService.clearEmailTrace(email);
    }

    @Override
    public boolean isUsernameAvailable(String username) {
        return !userService.existsByUsername(username);
    }
}
