package com.tradingpt.tpt_api.domain.investmenttypehistory.service.query;

import java.util.List;

import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response.ChangeRequestResponseDTO;

public interface InvestmentTypeChangeQueryService {

	/**
	 * 내 변경 신청 목록 조회 (고객)
	 */
	List<ChangeRequestResponseDTO> getMyChangeRequests(Long customerId);

	/**
	 * 변경 신청 상세 조회 (고객)
	 */
	ChangeRequestResponseDTO getChangeRequest(Long customerId, Long requestId);

	/**
	 * 모든 대기 중인 신청 조회 (어드민)
	 */
	List<ChangeRequestResponseDTO> getPendingChangeRequests();

	/**
	 * 모든 변경 신청 조회 (어드민)
	 */
	List<ChangeRequestResponseDTO> getAllChangeRequests();

	/**
	 * 변경 신청 상세 조회 (어드민)
	 */
	ChangeRequestResponseDTO getChangeRequestDetail(Long requestId);

}
