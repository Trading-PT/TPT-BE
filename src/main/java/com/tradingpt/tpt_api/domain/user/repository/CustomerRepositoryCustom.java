package com.tradingpt.tpt_api.domain.user.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import java.util.Set;

public interface CustomerRepositoryCustom {
	/**
	 * 주어진 상태(UserStatus)에 해당하는 Customer를
	 * UID 목록과 함께 fetch join으로 조회
	 */
	List<Customer> findCustomersWithUidByStatus(UserStatus status);

	Optional<Customer> findWithBasicsAndPaymentMethodsById(Long id);

	Map<Long, List<AssignedCustomerInfo>> findAssignedMapByTrainerIds(Set<Long> trainerIds);

	Map<Long, List<AssignedCustomerInfo>> findAssignedMapByAdminIds(Set<Long> adminIds);
	@lombok.Getter
	@lombok.AllArgsConstructor
	class AssignedCustomerInfo {
		private Long customerId;
		private String name;
	}
}
