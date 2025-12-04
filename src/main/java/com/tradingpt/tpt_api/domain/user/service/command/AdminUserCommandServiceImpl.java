package com.tradingpt.tpt_api.domain.user.service.command;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import com.tradingpt.tpt_api.domain.lecture.enums.ChapterType;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureProgressRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.user.dto.request.GiveUserTokenRequestDTO;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Uid;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UidRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserCommandServiceImpl implements AdminUserCommandService {

	private final CustomerRepository customerRepository;
	private final UidRepository uidRepository;
	private final LectureRepository lectureRepository;
	private final LectureProgressRepository lectureProgressRepository;

	@Transactional
	@Override
	public void updateUserStatus(Long userId, UserStatus newStatus) {
		Customer customer = customerRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 승인 또는 거절 상태만 변경 허용
		if (newStatus != UserStatus.UID_APPROVED && newStatus != UserStatus.UID_REJECTED) {
			throw new UserException(UserErrorStatus.INVALID_STATUS_CHANGE);
		}

		customer.setUserStatus(newStatus);

		if (newStatus == UserStatus.UID_APPROVED) {
			createFreeOTLectures(customer);
		}
	}

	@Override
	public void giveUserTokens(Long userId, GiveUserTokenRequestDTO request) {
		Customer customer = customerRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// ✅ 기존 토큰에 누적하여 부여 (교체가 아닌 추가)
		customer.addToken(request.getTokenCount());
		log.info("Tokens granted: userId={}, amount={}, totalTokens={}",
			userId, request.getTokenCount(), customer.getToken());
	}

	@Override
	public void updateUserUid(Long userId, String uidValue) {
		// 1) 고객 존재 확인
		Customer customer = customerRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 2) 해당 고객의 Uid 엔티티 조회 (없으면 예외 또는 생성)
		Uid uid = uidRepository.findByCustomerId(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.UID_NOT_FOUND));

		// 3) 값 변경
		uid.setUid(uidValue);   // or uid.updateUid(uidValue);

		// JPA 변경감지로 자동 flush
	}

	private void createFreeOTLectures(Customer customer) {
		Long customerId = customer.getId();

		// 1) OT 무료 강의 조회 (ChapterType.FREE + requiredToken = 0)
		List<Lecture> otLectures = lectureRepository
				.findFreeLecturesByChapterType(ChapterType.REGULAR);

		if (otLectures.isEmpty()) {
			return;
		}

		// 2) 각 강의별 LectureProgress가 없으면 생성
		for (Lecture lecture : otLectures) {
			boolean exists = lectureProgressRepository
					.existsByLectureIdAndCustomerId(lecture.getId(), customerId);

			if (!exists) {
				lectureProgressRepository.save(
						LectureProgress.builder()
								.lecture(lecture)
								.customer(customer)
								.watchedSeconds(0)
								.isCompleted(false)
								.build()
				);
			}
		}
	}

}
