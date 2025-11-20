package com.tradingpt.tpt_api.domain.user.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

public interface CustomerRepositoryCustom {
	/**
	 * 주어진 상태(UserStatus)에 해당하는 Customer를
	 * UID 목록과 함께 fetch join으로 조회
	 */
	List<Customer> findCustomersWithUidByStatus(UserStatus status);

	Optional<Customer> findWithBasicsAndPaymentMethodsById(Long id);

	Map<Long, List<AssignedCustomerInfo>> findAssignedMapByTrainerIds(Set<Long> trainerIds);

	Map<Long, List<AssignedCustomerInfo>> findAssignedMapByAdminIds(Set<Long> adminIds);

	/**
	 * 미구독(무료) 고객 목록 조회
	 *
	 * 조건:
	 * - ACTIVE 상태의 Subscription이 없음
	 * - membershipLevel이 BASIC
	 * - 담당 트레이너가 없음 (assignedTrainer IS NULL)
	 *
	 * @param pageable 페이징 정보
	 * @return 미구독 고객 Slice
	 */
	Slice<Customer> findFreeCustomers(Pageable pageable);

	@Getter
	@AllArgsConstructor
	class AssignedCustomerInfo {
		private Long customerId;
		private String name;
	}
}
