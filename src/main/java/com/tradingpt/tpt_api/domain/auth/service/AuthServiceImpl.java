package com.tradingpt.tpt_api.domain.auth.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;
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
	private final CustomerRepository customerRepository;
	private final TrainerRepository trainerRepository;

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
			throw new AuthException(AuthErrorStatus.TERMS_AGREEMENT_REQUIRED);
		}

		// 2) 정규화
		final String phone = AuthUtil.normalizePhone(req.getPhone());
		final String email = AuthUtil.normalizeEmail(req.getEmail());

		// 3) 인증 요구(휴대폰/이메일)
		verificationService.requireVerified(phone, email, session);

		// 4) 중복 체크
		userService.ensureUnique(req.getUsername(), email, phone);

		// 5) Customer 직접 생성 (User 필드들 포함)
		// ⭐ 핵심 변경: User 따로 생성하지 않고 Customer에서 모든 필드 설정
		Customer customer = Customer.builder()
			.username(req.getUsername())
			.password(passwordEncoder.encode(req.getPassword()))
			.email(email)
			.name(req.getName())
			.phoneNumber(phone)
			.membershipLevel(MembershipLevel.BASIC)
			.build();

		// 6) UID 처리 - 빌더에서 직접 customer 설정
		if (req.getUids() != null && !req.getUids().isEmpty()) {
			if (req.getUids().size() > 5) {
				throw new AuthException(AuthErrorStatus.INVALID_INPUT_FORMAT);
			}

			Set<String> seen = new HashSet<>();
			List<Uid> uidList = new ArrayList<>();

			for (SignUpRequest.UidRequest u : req.getUids()) {
				String exchange = u.getExchangeName() == null ? "" : u.getExchangeName().trim();
				String uidValue = u.getUid() == null ? "" : u.getUid().trim();

				if (exchange.isEmpty() || uidValue.isEmpty()) {
					throw new AuthException(AuthErrorStatus.REQUIRED_FIELD_MISSING);
				}
				if (!seen.add(uidValue)) {
					throw new AuthException(AuthErrorStatus.INVALID_INPUT_FORMAT);
				}

				// ⭐ 빌더에서 customer 직접 설정
				Uid uid = Uid.builder()
					.exchangeName(exchange)
					.uid(uidValue)
					.customer(customer)  // 빌더에서 customer 설정
					.build();

				uidList.add(uid);
			}

			// Customer의 uids 리스트에 직접 설정
			customer.getUids().addAll(uidList);
		}

		// 7) Customer 저장
		customerRepository.save(customer);

		// 8) 인증 흔적 정리
		verificationService.clearPhoneTrace(phone);
		verificationService.clearEmailTrace(email);
	}

	@Override
	public boolean isUsernameAvailable(String username) {
		return !userRepository.existsByUsername(username);
	}
}
