package com.tradingpt.tpt_api.domain.user.service.query;

import java.util.List;

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
		List<Customer> customers =
			customerRepository.findCustomersWithUidByStatus(UserStatus.UID_REVIEW_PENDING);

		return customers.stream()
			.map(PendingUserApprovalRowResponseDTO::from)
			.toList();
	}
}
