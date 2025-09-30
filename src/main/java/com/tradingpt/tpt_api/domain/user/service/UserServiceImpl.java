package com.tradingpt.tpt_api.domain.user.service;


import com.tradingpt.tpt_api.domain.user.dto.request.ChangePasswordRequest;
import com.tradingpt.tpt_api.domain.user.dto.response.FindIdResponseDTO;
import com.tradingpt.tpt_api.domain.auth.dto.response.MeResponse;
import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.PasswordHistory;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.enums.Provider;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.PasswordHistoryRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.exception.AuthException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private static final int PASSWORD_HISTORY_CHECK_SIZE = 5;
	private final FindByIndexNameSessionRepository<? extends Session> sessionRepository;
	private final ObjectProvider<PersistentTokenRepository> persistentTokenRepositoryProvider;


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
	public void changePassword(Long userId, ChangePasswordRequest req) {
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
		user.setPassword(passwordEncoder.encode(req.getNewPassword()));
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
		;
	}

}
