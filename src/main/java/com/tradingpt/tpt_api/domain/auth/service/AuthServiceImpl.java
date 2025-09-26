package com.tradingpt.tpt_api.domain.auth.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.auth.dto.request.SendEmailCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SendPhoneCodeRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.SignUpRequest;
import com.tradingpt.tpt_api.domain.auth.dto.request.VerifyCodeRequest;
import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.domain.auth.infrastructure.VerificationService;
import com.tradingpt.tpt_api.domain.auth.util.AuthUtil;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Uid;
import com.tradingpt.tpt_api.domain.user.enums.AccountStatus;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.enums.Provider;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.domain.user.service.UserService;
import com.tradingpt.tpt_api.global.exception.AuthException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

	private final VerificationService verificationService;
	private final UserService userService;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

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

	@Override
	@Transactional
	public void signUp(SignUpRequest req, HttpSession session) {
		// 필수 동의 검증
		if (!Boolean.TRUE.equals(req.getTermsService()) || !Boolean.TRUE.equals(req.getTermsPrivacy())) {
			throw new AuthException(AuthErrorStatus.TERMS_AGREEMENT_REQUIRED);
		}

		final String phone = AuthUtil.normalizePhone(req.getPhone());
		final String email = AuthUtil.normalizeEmail(req.getEmail());

		// 소셜 임시계정 여부
		boolean isSocialUsername =
			req.getUsername() != null &&
				(req.getUsername().startsWith("KAKAO_") || req.getUsername().startsWith("NAVER_"));

		if (isSocialUsername) { //소셜일 경우
			updateSocialCustomer(req, session, phone, email);
			return;
		}

		verificationService.requireVerified(phone, email, session);

		// 일반 회원가입
		if (!req.getPassword().equals(req.getPasswordCheck())) {
			throw new AuthException(AuthErrorStatus.PASSWORD_MISMATCH);
		}
		userService.ensureUnique(req.getUsername(), email, phone);

		Customer customer = Customer.builder()
			.username(req.getUsername())
			.password(passwordEncoder.encode(req.getPassword()))
			.email(email)
			.name(req.getName())
			.provider(Provider.LOCAL)
			.phoneNumber(phone)
			.membershipLevel(MembershipLevel.BASIC)
			.membershipExpiredAt(null)
			.openChapterNumber(null)
			.status(AccountStatus.ACTIVE)
			.build();

		customer.changeInvestmentType(req.getInvestmentType(), LocalDate.now());

		attachUids(customer, req);     // 자식 먼저 붙이고
		userRepository.save(customer); // 저장은 한 번 (cascade로 Uid 함께 INSERT)

		verificationService.clearPhoneTrace(phone);
		verificationService.clearEmailTrace(email);
	}

	private void updateSocialCustomer(SignUpRequest req, HttpSession session, String phone, String email) {
		var userOpt = userRepository.findByUsername(req.getUsername());
		if (userOpt.isEmpty() || !(userOpt.get() instanceof Customer)) {
			throw new AuthException(AuthErrorStatus.INVALID_INPUT_FORMAT);
		}
		Customer customer = (Customer)userOpt.get();

		// 이메일/휴대폰 변경 시에만 중복 체크
		if (!email.equalsIgnoreCase(customer.getEmail()) || !phone.equals(customer.getPhoneNumber())) {
			userService.ensureUnique(customer.getUsername(), email, phone);
		}

		// 기본 정보 업데이트
		customer.setPhoneNumber(phone);
		if (customer.getMembershipLevel() == null) {
			customer.setMembershipLevel(MembershipLevel.BASIC);
		}
		customer.setMembershipExpiredAt(null);
		customer.setOpenChapterNumber(null);
		customer.changeInvestmentType(req.getInvestmentType(), LocalDate.now());

		// UID 병합/설정
		attachUids(customer, req);

		// 기존 엔티티 저장 (변경 감지 + cascade)
		userRepository.save(customer);

		verificationService.clearPhoneTrace(phone);
		verificationService.clearEmailTrace(email);
	}

	private void attachUids(Customer customer, SignUpRequest req) {
		if (req.getUids() == null || req.getUids().isEmpty())
			return;

		int existing = customer.getUids() == null ? 0 : customer.getUids().size();
		if (existing + req.getUids().size() > 5) {
			throw new AuthException(AuthErrorStatus.INVALID_INPUT_FORMAT);
		}

		Set<String> seen = new HashSet<>();
		if (customer.getUids() != null) {
			for (Uid u : customer.getUids())
				seen.add(u.getUid());
		}

		for (SignUpRequest.UidRequest ur : req.getUids()) {
			String exchange = ur.getExchangeName() == null ? "" : ur.getExchangeName().trim();
			String uidValue = ur.getUid() == null ? "" : ur.getUid().trim();
			if (exchange.isEmpty() || uidValue.isEmpty()) {
				throw new AuthException(AuthErrorStatus.REQUIRED_FIELD_MISSING);
			}
			if (!seen.add(uidValue)) {
				throw new AuthException(AuthErrorStatus.INVALID_INPUT_FORMAT);
			}
			customer.addUid(Uid.builder()
				.exchangeName(exchange)
				.uid(uidValue)
				.build());
		}
	}

	@Override
	public boolean isUsernameAvailable(String username) {
		return !userRepository.existsByUsername(username);
	}
}
