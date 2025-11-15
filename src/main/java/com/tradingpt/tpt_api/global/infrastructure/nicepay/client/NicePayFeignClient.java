package com.tradingpt.tpt_api.global.infrastructure.nicepay.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.tradingpt.tpt_api.global.infrastructure.nicepay.config.NicePayFeignConfig;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyDeleteRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyDirectRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.BillingKeyRegisterRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.request.RecurringPaymentRequestDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyDeleteResponseDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyRegisterResponse;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.RecurringPaymentResponseDTO;

/**
 * NicePay API Feign Client
 * 빌키 발급/삭제 및 빌링 결제 API 호출 (인증/비인증 방식 모두 지원)
 */
@FeignClient(
	name = "nicepay-api",
	url = "${nicepay.api.base-url}",
	configuration = NicePayFeignConfig.class
)
public interface NicePayFeignClient {

	/**
	 * 빌키 발급 API 호출 (인증 방식)
	 *
	 * @param request 빌키 발급 요청 데이터
	 * @return 빌키 발급 응답 (BID 포함)
	 */
	@PostMapping(
		value = "${nicepay.api.billing-register-path}",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		produces = "text/plain;charset=EUC-KR"
	)
	BillingKeyRegisterResponse registerBillingKey(
		@RequestBody BillingKeyRegisterRequestDTO request
	);

	/**
	 * 빌키 발급 API 호출 (비인증 방식)
	 * 카드 정보를 직접 전달하여 빌키를 발급받습니다.
	 *
	 * @param request 빌키 발급 요청 데이터 (암호화된 카드 정보 포함)
	 * @return 빌키 발급 응답 (BID 포함)
	 */
	@PostMapping(
		value = "${nicepay.api.billing-direct-register-path}",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		produces = "text/plain;charset=EUC-KR"
	)
	BillingKeyRegisterResponse registerBillingKeyDirect(
		@RequestBody BillingKeyDirectRequestDTO request
	);

	/**
	 * 빌키 삭제 API 호출
	 *
	 * @param request 빌키 삭제 요청 데이터
	 * @return 빌키 삭제 응답
	 */
	@PostMapping(
		value = "${nicepay.api.billing-delete-path}",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		produces = "text/plain;charset=EUC-KR"
	)
	BillingKeyDeleteResponseDTO deleteBillingKey(
		@RequestBody BillingKeyDeleteRequestDTO request
	);

	/**
	 * 빌링 결제(승인) API 호출
	 * 발급받은 빌링키(BID)를 사용하여 정기 결제를 실행합니다.
	 *
	 * @param request 빌링 결제 요청 데이터
	 * @return 빌링 결제 응답 (승인번호, 거래번호 등)
	 */
	@PostMapping(
		value = "${nicepay.api.billing-approve-path}",
		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
		produces = "text/plain;charset=EUC-KR"
	)
	RecurringPaymentResponseDTO executeRecurringPayment(
		@RequestBody RecurringPaymentRequestDTO request
	);
}
