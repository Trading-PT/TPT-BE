package com.tradingpt.tpt_api.domain.paymentmethod.service.command;

import com.tradingpt.tpt_api.domain.paymentmethod.dto.request.BillingKeyCompleteRequestDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.dto.request.CardInfoRequestDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.dto.response.BillingKeyInitResponseDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.dto.response.BillingKeyRegisterResponseDTO;

/**
 * PaymentMethod Command Service 인터페이스
 * 결제수단 생성, 수정, 삭제 등 CUD 작업을 담당
 */
public interface PaymentMethodCommandService {

	/**
	 * 빌키 등록 초기화
	 * 프론트엔드에서 NicePay 인증창을 띄우는데 필요한 정보를 생성합니다.
	 *
	 * @param customerId 고객
	 * @return 초기화 응답 (Moid, SignData 등)
	 */
	BillingKeyInitResponseDTO initBillingKeyRegistration(Long customerId);

	/**
	 * 빌키 등록 완료 (인증 방식)
	 * NicePay 인증 완료 후 빌링키를 발급받고 결제수단을 등록합니다.
	 * - 기존 활성 결제수단이 있으면 에러 발생
	 * - 새로 등록하는 결제수단은 무조건 주 결제수단(isPrimary=true)으로 설정
	 *
	 * @param customerId 고객
	 * @param request    빌키 등록 요청 (TxTid, AuthToken, Moid)
	 * @return 빌키 등록 응답
	 */
	BillingKeyRegisterResponseDTO completeBillingKeyRegistration(
		Long customerId,
		BillingKeyCompleteRequestDTO request
	);

	/**
	 * 빌키 등록 (비인증 방식)
	 * 카드 정보를 직접 전달하여 빌링키를 발급받고 결제수단을 등록합니다.
	 * ⚠️ 보안 주의: 카드 정보는 절대 로깅하거나 DB에 저장하면 안 됩니다.
	 * - 기존 활성 결제수단이 있으면 에러 발생
	 * - 새로 등록하는 결제수단은 무조건 주 결제수단(isPrimary=true)으로 설정
	 *
	 * @param customerId      고객 ID
	 * @param cardInfoRequest 카드 정보 (평문)
	 * @return 빌키 등록 응답
	 */
	BillingKeyRegisterResponseDTO registerBillingKeyDirect(
		Long customerId,
		CardInfoRequestDTO cardInfoRequest
	);

	/**
	 * 결제수단 삭제 (소프트 삭제)
	 * NicePay API를 호출하여 빌링키도 함께 삭제합니다.
	 *
	 * @param customerId      고객
	 * @param paymentMethodId 결제수단 ID
	 */
	void deletePaymentMethod(Long customerId, Long paymentMethodId);
}
