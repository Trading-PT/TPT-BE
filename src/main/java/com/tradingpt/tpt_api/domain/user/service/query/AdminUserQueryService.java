package com.tradingpt.tpt_api.domain.user.service.query;

import java.util.List;

import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.tradingpt.tpt_api.domain.user.dto.response.PendingUserApprovalRowResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserQueryService {

	private final CustomerRepository customerRepository;

	public List<PendingUserApprovalRowResponseDTO> getPendingApprovalRows() {

		List<UserStatus> targetStatuses = List.of(
				UserStatus.UID_REVIEW_PENDING,
				UserStatus.UID_REJECTED
		);

		List<Customer> customers =
				customerRepository.findByUserStatusIn(targetStatuses);

		return customers.stream()
				.map(PendingUserApprovalRowResponseDTO::from)
				.toList();
	}

	public Page<PendingUserApprovalRowResponseDTO> searchUsersByUidPrefix(String uidPrefix, Pageable pageable) {

		Page<Customer> customers =
				customerRepository.findByUidUidStartingWithIgnoreCase(uidPrefix, pageable);

		return customers.map(PendingUserApprovalRowResponseDTO::from);
	}

	public Page<PendingUserApprovalRowResponseDTO> searchUsersByName(String nameKeyword, Pageable pageable) {

		Page<Customer> customers =
				customerRepository.findByNameContainingIgnoreCase(nameKeyword, pageable);

		return customers.map(PendingUserApprovalRowResponseDTO::from);
	}
}
