package com.tradingpt.tpt_api.domain.user.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserCommandService {

	private final CustomerRepository customerRepository;

	@Transactional
	public void updateUserStatus(Long userId, UserStatus newStatus) {
		Customer customer = customerRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 승인 또는 거절 상태만 변경 허용
		if (newStatus != UserStatus.UID_APPROVED && newStatus != UserStatus.UID_REJECTED) {
			throw new UserException(UserErrorStatus.INVALID_STATUS_CHANGE);
		}

		customer.setUserStatus(newStatus);

	}
}
