package com.tradingpt.tpt_api.domain.auth.support;

import static com.tradingpt.tpt_api.domain.auth.util.AuthUtil.normalizeEmail;
import static com.tradingpt.tpt_api.domain.auth.util.AuthUtil.normalizePhone;

import com.tradingpt.tpt_api.global.exception.AuthException;
import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import jakarta.servlet.http.HttpSession;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final JavaMailSender mailSender;
    private final SensSmsClient solapi; // 솔라피 클라이언트 사용

    // 메일 전용 스레드풀
    @Qualifier("mailExecutor")
    private final Executor mailExecutor;

    @Value("${app.mail.from:no-reply@tpt.ai}")
    private String mailFrom;

    // ===== 인메모리 OTP 저장 =====
    private static final long CODE_TTL_MS = 180_000L; // 3분
    private record Entry(String code, long expMs) {}

    private final Map<String, Entry> phoneCodes = new ConcurrentHashMap<>();
    private final Map<String, Entry> emailCodes = new ConcurrentHashMap<>();
    private final Set<String> phoneVerified = ConcurrentHashMap.newKeySet();
    private final Set<String> emailVerified = ConcurrentHashMap.newKeySet();

    private static final SecureRandom RND = new SecureRandom();
    private static String code6() { return String.format("%06d", RND.nextInt(1_000_000)); }
    private static long now() { return System.currentTimeMillis(); }
    private static boolean expired(Entry e){ return e == null || e.expMs() < now(); }

    @Override
    public void sendPhoneCode(String rawPhone, HttpSession session) {
        final String phone = normalizePhone(rawPhone);
        final String code  = code6();
        phoneCodes.put(phone, new Entry(code, now() + CODE_TTL_MS));

        try {
            solapi.send(phone, "[TradingPT] 인증번호는 " + code + " 입니다.");
            log.info("SMS 전송 성공: {}", phone);
        } catch (Exception ex) {
            // 발송 실패 시 코드 제거 후 예외
            phoneCodes.remove(phone);
            log.warn("SMS 전송 실패({}): {}", phone, ex.getMessage(), ex);
            throw new AuthException(AuthErrorStatus.SMS_SEND_FAILED, "인증 문자 발송에 실패했습니다.");
        }
    }

    @Override
    public void verifyPhone(String rawPhone, String code, HttpSession session) {
        final String phone = normalizePhone(rawPhone);

        if (code == null || !code.matches("\\d{6}")) {
            throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_INVALID, "잘못된 인증번호 형식입니다.");
        }

        final Entry e = phoneCodes.get(phone);
        if (expired(e)) {
            phoneCodes.remove(phone);
            throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_EXPIRED, "인증코드가 만료되었습니다.");
        }
        if (!e.code().equals(code)) {
            throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_INVALID, "휴대폰 인증 실패(코드 불일치)");
        }

        phoneCodes.remove(phone);
        phoneVerified.add(phone);
    }

    @Override
    public void sendEmailCode(String rawEmail, HttpSession session) {
        final String email = normalizeEmail(rawEmail);
        final String code  = code6();

        // 1) 코드 저장 (동기)
        emailCodes.put(email, new Entry(code, now() + CODE_TTL_MS));

        // 2) 메일 전송 (비동기)
        mailExecutor.execute(() -> {
            try {
                SimpleMailMessage m = new SimpleMailMessage();
                m.setFrom(mailFrom);
                m.setTo(email);
                m.setSubject("[TradingPT] 이메일 인증번호");
                m.setText("[TradingPT] 인증번호는 " + code + " 입니다.");
                mailSender.send(m);
            } catch (Exception ex) {
                // 전송 실패 시 코드 제거
                emailCodes.remove(email);
            }
        });
    }

    @Override
    public void verifyEmail(String rawEmail, String code, HttpSession session) {
        final String email = normalizeEmail(rawEmail);

        if (code == null || !code.matches("\\d{6}")) {
            throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_INVALID, "잘못된 인증번호 형식입니다.");
        }

        final Entry e = emailCodes.get(email);
        if (expired(e)) {
            emailCodes.remove(email);
            throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_EXPIRED, "인증코드가 만료되었습니다.");
        }
        if (!e.code().equals(code)) {
            throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_INVALID, "이메일 인증 실패(코드 불일치)");
        }

        emailCodes.remove(email);
        emailVerified.add(email);
    }

    @Override
    public void requireVerified(String phone, String email, HttpSession session) {
        // phone/email 인자는 UI에서 재입력 없이도 오기 때문에 normalize 재사용
        if (phone == null || !phoneVerified.contains(normalizePhone(phone))) {
            throw new AuthException(AuthErrorStatus.PHONE_NOT_VERIFIED, "휴대폰 인증이 필요합니다.");
        }
        if (email == null || !emailVerified.contains(normalizeEmail(email))) {
            throw new AuthException(AuthErrorStatus.ACCOUNT_NOT_VERIFIED);
        }
    }

    @Override
    public void clearPhoneTrace(String rawPhone) {
        final String phone = normalizePhone(rawPhone);
        phoneCodes.remove(phone);
        phoneVerified.remove(phone);
    }

    @Override
    public void clearEmailTrace(String rawEmail) {
        final String email = normalizeEmail(rawEmail);
        emailCodes.remove(email);
        emailVerified.remove(email);
    }
}
