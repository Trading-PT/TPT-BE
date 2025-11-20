package com.tradingpt.tpt_api.domain.user.service.command;

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

	}

	@Override
	public void giveUserTokens(Long userId, GiveUserTokenRequestDTO request) {
		Customer customer = customerRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		customer.updateToken(request.getTokenCount());
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
}
