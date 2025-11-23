package com.tradingpt.tpt_api.domain.user.service.command;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Customer Command Service 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CustomerCommandServiceImpl implements CustomerCommandService {

	private final CustomerRepository customerRepository;

	@Override
	public void updateMembershipFromSubscription(
		Long customerId,
		MembershipLevel membershipLevel,
		LocalDateTime expiredAt
	) {
		log.info("멤버십 업데이트: customerId={}, level={}, expiredAt={}",
			customerId, membershipLevel, expiredAt);

		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 멤버십 업데이트 (JPA dirty checking으로 자동 반영)
		customer.updateMembership(membershipLevel, expiredAt);

		log.info("멤버십 업데이트 완료: customerId={}, newLevel={}, expiredAt={}",
			customerId, membershipLevel, expiredAt);
	}
}
