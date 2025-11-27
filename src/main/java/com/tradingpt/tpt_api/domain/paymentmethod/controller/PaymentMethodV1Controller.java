package com.tradingpt.tpt_api.domain.paymentmethod.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.paymentmethod.dto.request.BillingKeyCompleteRequestDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.dto.request.CardInfoRequestDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.dto.response.BillingKeyInitResponseDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.dto.response.BillingKeyRegisterResponseDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.dto.response.PaymentMethodResponse;
import com.tradingpt.tpt_api.domain.paymentmethod.service.command.PaymentMethodCommandService;
import com.tradingpt.tpt_api.domain.paymentmethod.service.query.PaymentMethodQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 결제수단 REST Controller (고객용)
 */
@RestController
@RequestMapping("/api/v1/payment-methods")
@RequiredArgsConstructor
@Tag(name = "결제수단", description = "결제수단 관리 API (고객용)")
public class PaymentMethodV1Controller {

	private final PaymentMethodCommandService paymentMethodCommandService;
	private final PaymentMethodQueryService paymentMethodQueryService;

	@Operation(
		summary = "빌키 등록 초기화",
		description = """
			프론트엔드에서 NicePay 인증창을 띄우는데 필요한 정보를 생성합니다.
			- Moid (주문번호) 생성
			- SignData 생성
			- NicePay 설정 정보 반환
			"""
	)
	@PostMapping("/billing-key/init")
	public BaseResponse<BillingKeyInitResponseDTO> initBillingKey(
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {

		return BaseResponse.onSuccess(paymentMethodCommandService.initBillingKeyRegistration(customerId));
	}

	@Operation(
		summary = "빌키 등록 완료 (인증 방식)",
		description = """
			NicePay 인증 완료 후 빌링키를 발급받고 결제수단을 등록합니다.
			- NicePay API 호출하여 빌링키(BID) 발급
			- 결제수단 정보 저장
			- 첫 번째 결제수단인 경우 자동으로 주 결제수단으로 설정
			"""
	)
	@PostMapping("/billing-key/register")
	public BaseResponse<BillingKeyRegisterResponseDTO> registerBillingKey(
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long customerId,
		@Valid @RequestBody BillingKeyCompleteRequestDTO request
	) {

		return BaseResponse.onSuccess(paymentMethodCommandService.completeBillingKeyRegistration(customerId, request));
	}

	/**
	 * 절대 사용되어선 안됨!! 테스트 용도임
	 */
	@Operation(
		summary = "빌키 등록 (비인증 방식)",
		description = """
			카드 정보를 직접 전달하여 빌링키를 발급받고 결제수단을 등록합니다.
			⚠️ 보안 주의: 프론트엔드에서도 HTTPS 사용 필수
			- 카드 정보를 암호화하여 NicePay API 호출
			- 빌링키(BID) 발급 및 결제수단 저장
			- 자동으로 주 결제수단으로 설정
			- 기존 활성 결제수단이 있으면 에러 반환
			"""
	)
	@Deprecated
	@PostMapping("/billing-key/direct")
	public BaseResponse<BillingKeyRegisterResponseDTO> registerBillingKeyDirect(
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long customerId,
		@Valid @RequestBody CardInfoRequestDTO cardInfoRequest
	) {

		return BaseResponse.onSuccess(
			paymentMethodCommandService.registerBillingKeyDirect(customerId, cardInfoRequest));
	}

	@Operation(
		summary = "주 결제수단 조회",
		description = "고객의 주 결제수단을 조회합니다. 등록된 주 결제수단이 없으면 null을 반환합니다."
	)
	@GetMapping
	public BaseResponse<PaymentMethodResponse> getPrimaryPaymentMethod(
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long customerId
	) {

		return BaseResponse.onSuccess(paymentMethodQueryService.getPrimaryPaymentMethod(customerId));
	}

	@Operation(
		summary = "결제수단 상세 조회",
		description = "특정 결제수단의 상세 정보를 조회합니다."
	)
	@GetMapping("/{paymentMethodId}")
	public BaseResponse<PaymentMethodResponse> getPaymentMethod(
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long customerId,
		@Parameter(description = "결제수단 ID", example = "1")
		@PathVariable Long paymentMethodId
	) {
		return BaseResponse.onSuccess(paymentMethodQueryService.getPaymentMethod(customerId, paymentMethodId));
	}

	@Operation(
		summary = "결제수단 삭제",
		description = """
			결제수단을 삭제합니다. (소프트 삭제)
			- NicePay API를 호출하여 빌링키도 함께 삭제됩니다.
			"""
	)
	@DeleteMapping("/{paymentMethodId}")
	public BaseResponse<Void> deletePaymentMethod(
		@Parameter(hidden = true)
		@AuthenticationPrincipal(expression = "id") Long customerId,
		@Parameter(description = "결제수단 ID", example = "1")
		@PathVariable Long paymentMethodId
	) {

		paymentMethodCommandService.deletePaymentMethod(customerId, paymentMethodId);

		return BaseResponse.onSuccessDelete(null);
	}
}
