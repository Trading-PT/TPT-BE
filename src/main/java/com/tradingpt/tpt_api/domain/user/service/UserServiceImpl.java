package com.tradingpt.tpt_api.domain.user.service;

import com.tradingpt.tpt_api.domain.user.dto.response.ProfileImageResponseDTO;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.global.infrastructure.s3.S3FileService;
import com.tradingpt.tpt_api.global.infrastructure.s3.S3UploadResult;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.auth.dto.response.MeResponse;
import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.domain.user.dto.request.ChangePasswordRequestDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.FindIdResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.PasswordHistory;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.Provider;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.PasswordHistoryRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.exception.AuthException;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private static final int PASSWORD_HISTORY_CHECK_SIZE = 5;
	private final S3FileService s3FileService;

	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordHistoryRepository passwordHistoryRepository;

	@Override
	public void ensureUnique(String u, String e, String p) { /* 중복 검사 */ }

	@Override
	public FindIdResponseDTO findUserId(String email) {
		// email 기준으로 여러 User 가져오기
		List<User> users = userRepository.findAllByEmail(email);

		// LOCAL 계정만 필터링
		User localUser = users.stream()
			.filter(u -> u.getProvider() == Provider.LOCAL)
			.findFirst()
			.orElse(null); // 없으면 null 반환

		// LOCAL 계정이 없을 경우 → null 반환
		if (localUser == null) {
			return null;
		}

		// LOCAL 유저 존재 시 DTO 반환
		return FindIdResponseDTO.builder()
			.userName(localUser.getUsername())
			.build();
	}

	@Override
	@Transactional(readOnly = true)
	public MeResponse getMe(Long userId) {
		Customer c = customerRepository.findWithBasicsAndPaymentMethodsById(userId)
			.orElseThrow(() -> new AuthException(AuthErrorStatus.USER_NOT_FOUND));
		return MeResponse.from(c);
	}

	@Transactional
	public void changePassword(Long userId, ChangePasswordRequestDTO req) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new AuthException(AuthErrorStatus.USER_NOT_FOUND));

		// 1) 현재 비번 일치 확인
		if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
			throw new AuthException(AuthErrorStatus.BAD_CREDENTIALS);
		}

		// 2) 새 비번이 현재 비번과 동일 금지
		if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
			throw new AuthException(AuthErrorStatus.PASSWORD_REUSED);
		}

		// 3) 최근 N개 재사용 금지 (해시만 조회: QueryDSL 커스텀)
		List<String> recentHashes =
			passwordHistoryRepository.findRecentHashesByUserId(userId, PASSWORD_HISTORY_CHECK_SIZE);

		boolean reused = recentHashes.stream()
			.anyMatch(h -> passwordEncoder.matches(req.getNewPassword(), h));
		if (reused) {
			throw new AuthException(AuthErrorStatus.PASSWORD_REUSED);
		}

		// 4) 현재 해시를 히스토리에 백업
		passwordHistoryRepository.save(
			PasswordHistory.builder()
				.user(user)
				.passwordHash(user.getPassword())
				.build()
		);

		// 5) 비밀번호 변경
		user.changePassword(passwordEncoder.encode(req.getNewPassword()));
		userRepository.save(user); // 명시 저장 (안전)

		// 6) 초과분 정리 (최근 N개만 유지)
		passwordHistoryRepository.deleteOlderThanNByUserId(userId, PASSWORD_HISTORY_CHECK_SIZE);
	}

	@Transactional
	@Override
	public void deleteAccount(Long userId) {
		Customer customer = customerRepository.findById(userId)
			.orElseThrow(() -> new AuthException(AuthErrorStatus.USER_NOT_FOUND));

		customerRepository.delete(customer);

	}

	@Transactional
	@Override
	public ProfileImageResponseDTO updateProfileImage(Long customerId, MultipartFile file) {
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 기존 이미지가 있으면 삭제 (실패해도 업로드는 계속)
		String oldKey = customer.getProfileImageKey();
		if (oldKey != null && !oldKey.isBlank()) {
			try {
				s3FileService.delete(oldKey);
			} catch (Exception ignored) {

			}
		}

		// 신규 업로드
		S3UploadResult uploaded = s3FileService.upload(file, "mypage/image");

		// 엔티티 반영 (DB에는 key+url 저장)
		customer.changeProfileImage(uploaded.key(), uploaded.url());

		return ProfileImageResponseDTO.builder()
				.url(uploaded.url())
				.build();
	}
}
