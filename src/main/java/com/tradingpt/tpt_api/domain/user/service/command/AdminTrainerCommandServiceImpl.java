package com.tradingpt.tpt_api.domain.user.service.command;

import com.tradingpt.tpt_api.domain.user.dto.request.TrainerRequestDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.TrainerResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.Admin;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.Provider;
import com.tradingpt.tpt_api.domain.user.enums.Role;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminTrainerCommandServiceImpl implements AdminTrainerCommandService {

	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;
	private final S3FileService s3FileService;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	@Override
	public TrainerResponseDTO createTrainer(TrainerRequestDTO req, MultipartFile profileImage) {
		// 1) 비밀번호 확인
		if (!req.getPassword().equals(req.getPasswordCheck())) {
			throw new UserException(UserErrorStatus.PASSWORD_CONFIRM_NOT_MATCH);
		}

		// 2) username 중복 체크
		if (userRepository.existsByUsername(req.getUsername())) {
			throw new UserException(UserErrorStatus.LOGIN_ID_DUPLICATED);
		}

		// 3) 이미지 처리 (파일 우선, 없으면 URL, 둘 다 없으면 null 유지)
		String imageKey = null;
		String imageUrl = null;

		if (profileImage != null && !profileImage.isEmpty()) {
			var uploaded = s3FileService.upload(profileImage, "profiles/trainers/");
			imageKey = uploaded.key();
			imageUrl = uploaded.url();
		}

		// 4) 역할에 따라 타입 생성
		User saved;
		if (req.getGrantRole() == Role.ROLE_ADMIN) {
			Admin admin = Admin.builder()
					.name(req.getName())
					.username(req.getUsername())
					.password(passwordEncoder.encode(req.getPassword()))
					.provider(Provider.LOCAL)
					.phoneNumber(req.getPhone())
					.onelineIntroduction(req.getOnelineIntroduction())
					.profileImageKey(imageKey)
					.profileImageUrl(imageUrl)
					.build();

			saved = userRepository.save(admin);
		} else {
			Trainer trainer = Trainer.builder()
					.name(req.getName())
					.username(req.getUsername())
					.password(passwordEncoder.encode(req.getPassword()))
					.provider(Provider.LOCAL)
					.phoneNumber(req.getPhone())
					.onelineIntroduction(req.getOnelineIntroduction())
					.profileImageKey(imageKey)
					.profileImageUrl(imageUrl)
					.build();

			saved = userRepository.save(trainer);
		}

		return TrainerResponseDTO.builder()
				.trainerId(saved.getId()) // 상속 PK -> getId 사용
				.build();
	}

	@Transactional
	@Override
	public TrainerResponseDTO updateTrainer(Long userId, TrainerRequestDTO req, MultipartFile profileImage) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 1) 비밀번호 확인
		if (!req.getPassword().equals(req.getPasswordCheck())) {
			throw new UserException(UserErrorStatus.PASSWORD_CONFIRM_NOT_MATCH);
		}

		// 2) username 중복 체크 (본인 제외)
		if (!user.getUsername().equals(req.getUsername()) && userRepository.existsByUsername(req.getUsername())) {
			throw new UserException(UserErrorStatus.LOGIN_ID_DUPLICATED);
		}

		String imageKey = user.getProfileImageKey();
		String imageUrl = user.getProfileImageUrl();

		if (profileImage != null && !profileImage.isEmpty()) {
			// 새 이미지 업로드 → 기존 키 있으면 삭제 후 교체
			if (imageKey != null && !imageKey.isEmpty()) {
				s3FileService.delete(imageKey);
			}
			var uploaded = s3FileService.upload(profileImage, "profiles/users/");
			imageKey = uploaded.key();
			imageUrl = uploaded.url();
		} else {
			// 업로드가 없으면 기존 이미지 제거
			if (imageKey != null && !imageKey.isEmpty()) {
				s3FileService.delete(imageKey);
			}
			imageKey = null;
			imageUrl = null;
		}


		//  Role 변경 감지 (Trainer → Admin or Admin → Trainer)
		Role currentRole = user.getRole();
		Role requestedRole = req.getGrantRole();

		if (currentRole != requestedRole) {
			// 기존 엔티티 삭제
			userRepository.delete(user);
			userRepository.flush();

			// 새 엔티티 생성
			User newUser;
			if (requestedRole == Role.ROLE_ADMIN) {
				newUser = Admin.builder()
						.name(req.getName())
						.username(req.getUsername())
						.password(passwordEncoder.encode(req.getPassword()))
						.provider(Provider.LOCAL)
						.phoneNumber(req.getPhone())
						.onelineIntroduction(req.getOnelineIntroduction())
						.profileImageKey(imageKey)
						.profileImageUrl(imageUrl)
						.build();
			} else {
				newUser = Trainer.builder()
						.name(req.getName())
						.username(req.getUsername())
						.password(passwordEncoder.encode(req.getPassword()))
						.provider(Provider.LOCAL)
						.phoneNumber(req.getPhone())
						.onelineIntroduction(req.getOnelineIntroduction())
						.profileImageKey(imageKey)
						.profileImageUrl(imageUrl)
						.build();
			}

			userRepository.save(newUser);
			return TrainerResponseDTO.builder()
					.trainerId(newUser.getId())
					.build();
		}

		//  Role 그대로 유지 (Admin ↔ Trainer 전환 X)
		user.changeName(req.getName());
		user.changeUsername(req.getUsername());
		user.changePassword(passwordEncoder.encode(req.getPassword()));
		user.changeProfileImage(imageKey, imageUrl);

		if (user instanceof Trainer trainer) {
			trainer.changePhoneNumber(req.getPhone());
			if (req.getOnelineIntroduction() != null) trainer.changeOnelineIntroduction(req.getOnelineIntroduction());
		} else if (user instanceof Admin admin) {
			admin.changePhoneNumber(req.getPhone());
			if (req.getOnelineIntroduction() != null) admin.changeOnelineIntroduction(req.getOnelineIntroduction());
		}

		return TrainerResponseDTO.builder()
				.trainerId(user.getId())
				.build();
	}

	@Transactional
	@Override
	public void deleteTrainer(Long trainerId) {
		User user = userRepository.findById(trainerId)
				.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 어드민 삭제 금지
		if (user instanceof Admin) {
			throw new UserException(UserErrorStatus.INVALID_STATUS_CHANGE);
		}

		Trainer trainer = (Trainer) user;

		// 트레이너에게 배정된 고객이 있는지 확인
		boolean hasAssignedCustomers = customerRepository. existsByAssignedTrainer_Id(trainerId);
		if (hasAssignedCustomers) {
			throw new UserException(UserErrorStatus.HAS_ASSIGNED_CUSTOMERS);
		}


		// S3 정리
		if ((trainer.getProfileImageKey() != null)) {
			s3FileService.delete(trainer.getProfileImageKey());
		}

		userRepository.deleteById(trainerId);
	}

	@Transactional
	@Override
	public Long reassignCustomerToTrainer(Long trainerId, Long customerId) {
		// 1) 트레이너 존재 및 타입 검증
		User trainer = userRepository.findById(trainerId)
				.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

		if (trainer.getRole() == null ||
				trainer.getRole().equals("ROLE_CUSTOMER")) {
			throw new UserException(UserErrorStatus.INVALID_USER_TYPE);
		}

		// 2) 고객 존재 검증
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 3) 트레이너 재배정
		customer.setAssignedTrainer(trainer);

		customer.setUserStatus(UserStatus.TRAINER_ASSIGNED);

		// 5) 반환: 성공적으로 변경된 고객 ID
		return customer.getId();
	}
}
