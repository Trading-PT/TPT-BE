package com.tradingpt.tpt_api.domain.user.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.user.dto.request.GiveUserTokenRequestDTO;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserCommandServiceImpl implements AdminUserCommandService {

	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;

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
	public Void giveUserTokens(Long userId, GiveUserTokenRequestDTO request) {
		Customer customer = (Customer)userRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		customer.updateToken(request.getTokenCount());

		return null;
	}
}
