package com.tradingpt.tpt_api.domain.investmenttypehistory.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response.InvestmentTypeHistoryResponseDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

/**
 * 투자유형 이력 조회 서비스 구현체
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InvestmentTypeHistoryQueryServiceImpl implements InvestmentTypeHistoryQueryService {

	private final InvestmentTypeHistoryRepository investmentTypeHistoryRepository;
	private final CustomerRepository customerRepository;

	@Override
	public List<InvestmentTypeHistoryResponseDTO> getCustomerInvestmentTypeHistories(Long customerId) {
		// 고객 존재 여부 검증
		if (!customerRepository.existsById(customerId)) {
			throw new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND);
		}

		// 투자유형 이력 조회 (startDate 오름차순)
		return investmentTypeHistoryRepository.findByCustomer_IdOrderByStartDateAsc(customerId)
			.stream()
			.map(InvestmentTypeHistoryResponseDTO::from)
			.toList();
	}
}
