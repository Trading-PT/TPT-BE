package com.tradingpt.tpt_api.domain.paymentmethod.service.command;

import com.tradingpt.tpt_api.domain.paymentmethod.enums.Status;

public interface BillingRequestCommandService {
	void createBillingRequest(Long customerId, String moid, String resultCode, String resultMsg);

	void updateBillingRequestStatus(Long billingRequestId, Status status, String resultCode, String resultMsg);

	/**
	 * 빌링 요청 완료 처리 (별도 트랜잭션)
	 * 첫 결제 실패 시에도 빌링키 발급 성공 정보가 롤백되지 않도록 REQUIRES_NEW 사용
	 *
	 * @param billingRequestId 빌링 요청 ID
	 * @param resultCode 결과 코드
	 * @param resultMsg 결과 메시지
	 */
	void completeBillingRequestInNewTransaction(Long billingRequestId, String resultCode, String resultMsg);

	/**
	 * 빌링 요청 실패 처리 (별도 트랜잭션)
	 * 첫 결제 실패 시 빌링 요청 상태를 FAILED로 변경
	 *
	 * @param billingRequestId 빌링 요청 ID
	 * @param resultCode 실패 코드
	 * @param resultMsg 실패 메시지
	 */
	void failBillingRequestInNewTransaction(Long billingRequestId, String resultCode, String resultMsg);
}
