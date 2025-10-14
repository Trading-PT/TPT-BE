package com.tradingpt.tpt_api.domain.investmenttypehistory.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response.ChangeRequestResponseDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeChangeRequest;
import com.tradingpt.tpt_api.domain.investmenttypehistory.enums.ChangeRequestStatus;
import com.tradingpt.tpt_api.domain.investmenttypehistory.exception.InvestmentHistoryErrorStatus;
import com.tradingpt.tpt_api.domain.investmenttypehistory.exception.InvestmentHistoryException;
import com.tradingpt.tpt_api.domain.investmenttypehistory.repository.InvestmentTypeChangeRequestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestmentTypeChangeQueryServiceImpl implements InvestmentTypeChangeQueryService {

	private final InvestmentTypeChangeRequestRepository investmentTypeChangeRequestRepository;

	@Override
	public List<ChangeRequestResponseDTO> getMyChangeRequests(Long customerId) {
		List<InvestmentTypeChangeRequest> requests = investmentTypeChangeRequestRepository
			.findByCustomerIdOrderByRequestedDateDesc(customerId);

		return requests.stream()
			.map(ChangeRequestResponseDTO::from)
			.toList();
	}

	@Override
	public ChangeRequestResponseDTO getChangeRequest(Long customerId, Long requestId) {
		InvestmentTypeChangeRequest request = investmentTypeChangeRequestRepository.findById(requestId)
			.orElseThrow(() -> new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.CHANGE_REQUEST_NOT_FOUND));

		// 본인 확인
		if (!request.getCustomer().getId().equals(customerId)) {
			throw new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.UNAUTHORIZED_ACCESS);
		}

		return ChangeRequestResponseDTO.from(request);
	}

	@Override
	public List<ChangeRequestResponseDTO> getPendingChangeRequests() {
		List<InvestmentTypeChangeRequest> requests = investmentTypeChangeRequestRepository
			.findByStatusOrderByRequestedDateAsc(ChangeRequestStatus.PENDING);

		return requests.stream()
			.map(ChangeRequestResponseDTO::from)
			.toList();
	}

	@Override
	public List<ChangeRequestResponseDTO> getAllChangeRequests() {
		List<InvestmentTypeChangeRequest> requests = investmentTypeChangeRequestRepository
			.findAll();

		return requests.stream()
			.map(ChangeRequestResponseDTO::from)
			.toList();
	}

	@Override
	public ChangeRequestResponseDTO getChangeRequestDetail(Long requestId) {
		InvestmentTypeChangeRequest request = investmentTypeChangeRequestRepository.findById(requestId)
			.orElseThrow(() -> new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.CHANGE_REQUEST_NOT_FOUND));

		return ChangeRequestResponseDTO.from(request);
	}
}