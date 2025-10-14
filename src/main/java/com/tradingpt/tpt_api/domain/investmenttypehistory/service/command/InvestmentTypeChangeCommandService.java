package com.tradingpt.tpt_api.domain.investmenttypehistory.service.command;

import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.request.ApproveChangeRequestDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.request.CreateChangeRequestDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response.ChangeRequestResponseDTO;

import jakarta.validation.Valid;

public interface InvestmentTypeChangeCommandService {

	/**
	 * 투자 유형 변경 신청 (고객)
	 */
	ChangeRequestResponseDTO createChangeRequest(Long customerId, CreateChangeRequestDTO request);

	/**
	 * 변경 신청 승인/거부 (어드민)
	 */
	ChangeRequestResponseDTO processChangeRequest(
		Long requestId,
		Long trainerId,
		@Valid ApproveChangeRequestDTO request
	);

	/**
	 * 변경 신청 취소 (고객)
	 */
	Void cancelChangeRequest(Long customerId, Long requestId);

}
