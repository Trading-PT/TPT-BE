package com.tradingpt.tpt_api.domain.user.service;

import com.tradingpt.tpt_api.domain.auth.dto.response.AdminMeResponse;
import com.tradingpt.tpt_api.domain.user.dto.response.ProfileImageResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.Admin;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.enums.Role;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.AdminRepository;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;
import com.tradingpt.tpt_api.global.infrastructure.s3.response.S3UploadResult;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;
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
	private final TrainerRepository trainerRepository;
	private final AdminRepository adminRepository;
	private final PasswordEncoder passwordEncoder;
	private final PasswordHistoryRepository passwordHistoryRepository;

	@Override
	public void ensureUnique(String u, String e, String p) { /* 중복 검사 */ }

	@Override
	public FindIdResponseDTO findUserId(String email) {

		// 1) email 기준으로 전체 유저 조회
		List<User> users = userRepository.findAllByEmail(email);

		// 2) LOCAL 계정만 필터링
		List<User> localUsers = users.stream()
				.filter(u -> u.getProvider() == Provider.LOCAL)
				.toList();

		// 3) LOCAL 계정이 하나도 없으면 null 또는 빈 DTO 반환
		if (localUsers.isEmpty()) {
			return null;
		}

		// 4) LOCAL 계정들의 username 리스트로 반환
		List<String> usernames = localUsers.stream()
				.map(User::getUsername)
				.toList();

		return FindIdResponseDTO.builder()
				.usernames(usernames)   // ← 변경
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
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new AuthException(AuthErrorStatus.USER_NOT_FOUND));

		// 이미 탈퇴 처리된 계정이면 그냥 리턴하거나 예외 던질지 선택
		if (user.isDeleted()) {
			return;
		}

		// 실제 삭제 대신 "탈퇴 상태 + 삭제 예정일 기록"
		user.delete();
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

	@Override
	@Transactional
	public Long changeNickname(Long userId, String nickname) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		user.changeNickname(nickname);

		return userId;
	}

	@Transactional(readOnly = true)
	public AdminMeResponse getAdminMe(Long userId) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new AuthException(AuthErrorStatus.USER_NOT_FOUND));

		Role role = user.getRole();

		if (role == Role.ROLE_TRAINER) {
			Trainer trainer = trainerRepository.findById(userId)
					.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

			return AdminMeResponse.from(user, trainer);
		}

		if (role == Role.ROLE_ADMIN) {
			Admin admin = adminRepository.findById(userId)
					.orElseThrow(() -> new UserException(UserErrorStatus.ADMIN_NOT_FOUND));

			return AdminMeResponse.from(user, admin);
		}

		return null;
	}

}
