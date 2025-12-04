package com.tradingpt.tpt_api.domain.auth.infrastructure;

import static com.tradingpt.tpt_api.domain.auth.util.AuthUtil.normalizeEmail;
import static com.tradingpt.tpt_api.domain.auth.util.AuthUtil.normalizePhone;

import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.domain.auth.infrastructure.sms.SensSmsClient;
import com.tradingpt.tpt_api.domain.auth.util.AuthUtil;
import com.tradingpt.tpt_api.global.exception.AuthException;
import jakarta.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

	// ===== 설정 값 =====
	private static final long CODE_TTL_MS = 300_000L; // 5분 (기존 유지)
	private static final long VERIFIED_TTL_SECONDS = 600L; // 10분 동안 "인증됨" 상태 유지 (필요시 조절)

	private static final SecureRandom RND = new SecureRandom();

	private final JavaMailSender mailSender;
	private final SensSmsClient solapi; // SMS 클라이언트
	private final StringRedisTemplate redisTemplate; // ✅ 인메모리 대신 Redis 사용

	// 메일 전용 스레드풀
	@Qualifier("mailExecutor")
	private final Executor mailExecutor;

	@Value("${app.mail.from}")
	private String mailFrom;

	// ===== Redis Key 헬퍼 =====
	private String phoneCodeKey(String phone) {
		return "verif:phone:code:" + phone;
	}

	private String emailCodeKey(String email) {
		return "verif:email:code:" + email;
	}

	private String phoneVerifiedKey(String phone) {
		return "verif:phone:ok:" + phone;
	}

	private String emailVerifiedKey(String email) {
		return "verif:email:ok:" + email;
	}

	// ===== 공통 유틸 =====

	private static String code6() {
		return String.format("%06d", RND.nextInt(1_000_000));
	}

	private static long now() {
		return System.currentTimeMillis();
	}

	// ===== 휴대폰 인증 =====

	@Override
	public void sendPhoneCode(String rawPhone, HttpSession session) {
		final String phone = normalizePhone(rawPhone);
		final String code = code6();

		// Redis에 코드 + TTL 저장
		String key = phoneCodeKey(phone);
		redisTemplate.opsForValue().set(
				key,
				code,
				Duration.ofMillis(CODE_TTL_MS)
		);

		try {
			solapi.send(phone, "[TradingPT] 인증번호는 " + code + " 입니다.");
			log.info("SMS 전송 성공: {} (key={})", phone, key);
		} catch (Exception ex) {
			// 발송 실패 시 코드 제거 후 예외
			redisTemplate.delete(key);
			log.warn("SMS 전송 실패({}): {}", phone, ex.getMessage(), ex);
			throw new AuthException(AuthErrorStatus.SMS_SEND_FAILED);
		}
	}

	@Override
	public void verifyPhone(String rawPhone, String code, HttpSession session) {
		final String phone = normalizePhone(rawPhone);

		if (code == null || !code.matches("\\d{6}")) {
			throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_INVALID);
		}

		String codeKey = phoneCodeKey(phone);
		String stored = redisTemplate.opsForValue().get(codeKey);

		if (stored == null) {
			// TTL 만료 또는 아예 발급 안 됨
			throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_EXPIRED);
		}
		if (!stored.equals(code)) {
			throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_INVALID);
		}

		// 코드 삭제 + "인증됨" 플래그 설정
		redisTemplate.delete(codeKey);
		redisTemplate.opsForValue().set(
				phoneVerifiedKey(phone),
				"true",
				Duration.ofSeconds(VERIFIED_TTL_SECONDS)
		);
	}

	// ===== 이메일 인증 =====

	@Override
	public void sendEmailCode(String rawEmail, HttpSession session) {
		final String email = normalizeEmail(rawEmail);
		final String code = code6();

		String key = emailCodeKey(email);
		// 1) 코드 저장 (동기)
		redisTemplate.opsForValue().set(
				key,
				code,
				Duration.ofMillis(CODE_TTL_MS)
		);

		// 2) 메일 전송 (비동기)
		mailExecutor.execute(() -> {
			try {
				// MIME 메일 생성
				var message = mailSender.createMimeMessage();
				var helper = new MimeMessageHelper(message, false, "UTF-8");

				/**
				 * Envelope-From + Header-From 둘 다 Gmail Relay가 인정하는 형식으로 들어감
				 * (SimpleMailMessage는 Envelope-From을 설정하지 못하기 때문에 550 에러 발생)
				 */
				helper.setFrom(mailFrom, "TradingPT"); // mailFrom = no-reply@YOUR_DOMAIN

				helper.setTo(email);
				helper.setSubject("[TradingPT] 이메일 인증번호");
				helper.setText("[TradingPT] 인증번호는 " + code + " 입니다.", false);

				mailSender.send(message);

				log.info("이메일 인증번호 전송 성공: {} (key={})", email, key);

			} catch (Exception ex) {
				redisTemplate.delete(key);
				log.info("mailFrom='{}'", mailFrom);
				log.warn("이메일 전송 실패({}): {}", email, ex.getMessage(), ex);
			}
		});

	}

	@Override
	public void verifyEmail(String rawEmail, String code, HttpSession session) {
		final String email = normalizeEmail(rawEmail);

		if (code == null || !code.matches("\\d{6}")) {
			throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_INVALID);
		}

		String codeKey = emailCodeKey(email);
		String stored = redisTemplate.opsForValue().get(codeKey);

		if (stored == null) {
			throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_EXPIRED);
		}
		if (!stored.equals(code)) {
			throw new AuthException(AuthErrorStatus.VERIFICATION_CODE_INVALID);
		}

		// 코드 삭제 + "이메일 인증됨" 플래그 설정
		redisTemplate.delete(codeKey);
		redisTemplate.opsForValue().set(
				emailVerifiedKey(email),
				"true",
				Duration.ofSeconds(VERIFIED_TTL_SECONDS)
		);
	}

	// ===== 가입 시 "인증 완료 여부" 체크 =====

	@Override
	public void requireVerified(String phone, String email, HttpSession session) {
		// phone / email 인자는 이미 UI에서 입력된 값이라 normalize 재사용
		if (phone == null) {
			throw new AuthException(AuthErrorStatus.PHONE_NOT_VERIFIED);
		}
		if (email == null) {
			throw new AuthException(AuthErrorStatus.ACCOUNT_NOT_VERIFIED);
		}

		String normalizedPhone = normalizePhone(phone);
		String normalizedEmail = normalizeEmail(email);

		Boolean phoneOk = redisTemplate.hasKey(phoneVerifiedKey(normalizedPhone));
		if (phoneOk == null || !phoneOk) {
			throw new AuthException(AuthErrorStatus.PHONE_NOT_VERIFIED);
		}

		Boolean emailOk = redisTemplate.hasKey(emailVerifiedKey(normalizedEmail));
		if (emailOk == null || !emailOk) {
			throw new AuthException(AuthErrorStatus.ACCOUNT_NOT_VERIFIED);
		}
	}

	// ===== 인증 흔적 초기화 =====

	@Override
	public void clearPhoneTrace(String rawPhone) {
		final String phone = normalizePhone(rawPhone);
		redisTemplate.delete(phoneCodeKey(phone));
		redisTemplate.delete(phoneVerifiedKey(phone));
	}

	@Override
	public void clearEmailTrace(String rawEmail) {
		final String email = normalizeEmail(rawEmail);
		redisTemplate.delete(emailCodeKey(email));
		redisTemplate.delete(emailVerifiedKey(email));
	}

	// ===== 소셜 가입에서 "이메일 인증 우회"용 =====

	/**
	 * 소셜 로그인 / 가입 시, 별도의 인증코드 없이
	 * "이 이메일은 이미 인증되었다"라고 간주하고 싶을 때 사용.
	 * (멀티 인스턴스 환경을 위해 HttpSession 대신 Redis에 저장)
	 */
	@Override
	public void markEmailVerifiedWithoutCode(String email, HttpSession session) {
		String normalized = AuthUtil.normalizeEmail(email);
		redisTemplate.opsForValue().set(
				emailVerifiedKey(normalized),
				"true",
				Duration.ofSeconds(VERIFIED_TTL_SECONDS)
		);
	}
}
