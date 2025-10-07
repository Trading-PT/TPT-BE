package com.tradingpt.tpt_api.domain.user.repository;

import java.util.List;
import java.util.Optional;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;

public interface CustomerRepositoryCustom {
	/**
	 * 주어진 상태(UserStatus)에 해당하는 Customer를
	 * UID 목록과 함께 fetch join으로 조회
	 */
	List<Customer> findCustomersWithUidByStatus(UserStatus status);

	Optional<Customer> findWithBasicsAndPaymentMethodsById(Long id);
}
