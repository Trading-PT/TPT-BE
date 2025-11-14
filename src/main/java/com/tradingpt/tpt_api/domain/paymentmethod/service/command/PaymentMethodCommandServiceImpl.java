package com.tradingpt.tpt_api.domain.paymentmethod.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.paymentmethod.dto.request.BillingKeyCompleteRequestDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.dto.request.CardInfoRequestDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.dto.response.BillingKeyInitResponseDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.dto.response.BillingKeyRegisterResponseDTO;
import com.tradingpt.tpt_api.domain.paymentmethod.entity.BillingRequest;
import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.paymentmethod.enums.CardType;
import com.tradingpt.tpt_api.domain.paymentmethod.enums.Status;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodErrorStatus;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodException;
import com.tradingpt.tpt_api.domain.paymentmethod.repository.BillingRequestRepository;
import com.tradingpt.tpt_api.domain.paymentmethod.repository.PaymentMethodRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.config.NicePayConfig;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyDeleteResponseDTO;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.dto.response.BillingKeyRegisterResponse;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.service.NicePayService;
import com.tradingpt.tpt_api.global.infrastructure.nicepay.util.NicePayCryptoUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PaymentMethod Command Service 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentMethodCommandServiceImpl implements PaymentMethodCommandService {

	private final CustomerRepository customerRepository;
	private final PaymentMethodRepository paymentMethodRepository;
	private final NicePayService nicePayService;
	private final NicePayConfig nicePayConfig;
	private final BillingRequestRepository billingRequestRepository;
	private final BillingRequestCommandService billingRequestCommandService;

	@Override
	public BillingKeyInitResponseDTO initBillingKeyRegistration(Long customerId) {
		// 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// Moid 생성 (UUID 기반)
		String moid = NicePayCryptoUtil.generateMoid();

		// EdiDate 생성
		String ediDate = NicePayCryptoUtil.generateEdiDate();

		// SignData 생성: SHA256(MID + EdiDate + Moid + MerchantKey)
		String mid = nicePayConfig.getMid();
		String merchantKey = nicePayConfig.getMerchantKey();
		String signData = NicePayCryptoUtil.generateSignData(mid, ediDate, moid, merchantKey);

		// 빌링 요청 등록
		BillingRequest newBillingRequest = BillingRequest.of(customer, moid);

		// 빌링 요청 등록
		billingRequestRepository.save(newBillingRequest);

		return BillingKeyInitResponseDTO.of(moid, ediDate, signData, mid, nicePayConfig.getGoodsName(),
			nicePayConfig.getAmt());
	}

	@Override
	public BillingKeyRegisterResponseDTO completeBillingKeyRegistration(
		Long customerId,
		BillingKeyCompleteRequestDTO request
	) {
		log.info("빌링키 등록 시작: customerId={}, moid={}", customerId, request.getMoid());

		// 고객 검색
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 빌링 요청 조회
		BillingRequest billingRequest = billingRequestRepository.findByCustomer_IdAndMoid(customerId, request.getMoid())
			.orElseThrow(() -> new PaymentMethodException(PaymentMethodErrorStatus.BILLING_REQUEST_NOT_FOUND));

		// 기존 활성 결제수단 확인 (이미 있으면 에러)
		paymentMethodRepository.findByCustomerAndIsPrimaryTrueAndIsDeletedFalse(customer)
			.ifPresent(existing -> {
				log.error("이미 등록된 결제수단이 존재: customerId={}, existingPaymentMethodId={}",
					customerId, existing.getId());
				throw new PaymentMethodException(PaymentMethodErrorStatus.PAYMENT_METHOD_ALREADY_EXISTS);
			});

		// NicePay API 호출하여 빌링키 발급
		BillingKeyRegisterResponse nicePayResponse;
		try {
			nicePayResponse = nicePayService.registerBillingKey(
				request.getTxTid(),
				request.getAuthToken(),
				request.getMoid()
			);
		} catch (Exception e) {
			log.error("빌키 발급 실패", e);

			billingRequestCommandService.updateBillingRequestStatus(
				billingRequest.getId(),
				Status.FAILED,
				"통신 에러",
				e.getMessage()
			);

			throw new PaymentMethodException(
				PaymentMethodErrorStatus.BILLING_KEY_REGISTRATION_FAILED
			);
		}

		// 화면 표시명 생성: {카드사명} ****{뒤 4자리}
		String displayName = String.format("%s %s",
			nicePayResponse.getCardName(),
			nicePayResponse.getCardNo()
		);

		// 빌링 요청의 상태를 '완료'로 변경
		billingRequest.complete();

		PaymentMethod paymentMethod = PaymentMethod.of(customer, billingRequest, request.getMoid(),
			nicePayResponse.getBID(),
			nicePayResponse.getAuthDate(), nicePayResponse.getCardCode(), nicePayResponse.getCardName(),
			nicePayResponse.getCardCl().equals("0") ? CardType.CREDIT : CardType.DEBIT,
			nicePayResponse.getCardNo(), displayName, nicePayResponse.getResultCode(), nicePayResponse.getResultMsg()
		);

		paymentMethodRepository.save(paymentMethod);

		log.info("빌링키 등록 완료: customerId={}, paymentMethodId={}", customerId, paymentMethod.getId());

		// 응답 생성
		return BillingKeyRegisterResponseDTO.from(paymentMethod);
	}

	@Override
	public BillingKeyRegisterResponseDTO registerBillingKeyDirect(
		Long customerId,
		CardInfoRequestDTO cardInfoRequest
	) {
		log.info("비인증 빌링키 등록 시작: customerId={}", customerId);

		// 고객 검색
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 기존 활성 결제수단 확인 (이미 있으면 에러)
		paymentMethodRepository.findByCustomerAndIsPrimaryTrueAndIsDeletedFalse(customer)
			.ifPresent(existing -> {
				log.error("이미 등록된 결제수단이 존재: customerId={}, existingPaymentMethodId={}",
					customerId, existing.getId());
				throw new PaymentMethodException(PaymentMethodErrorStatus.PAYMENT_METHOD_ALREADY_EXISTS);
			});

		// Moid 생성
		String moid = NicePayCryptoUtil.generateMoid();

		// 카드 정보를 평문으로 변환
		String cardInfoPlainText = cardInfoRequest.toEncDataPlainText();

		// NicePay API 호출하여 빌링키 발급 (비인증 방식)
		BillingKeyRegisterResponse nicePayResponse;
		try {
			nicePayResponse = nicePayService.registerBillingKeyDirect(
				cardInfoPlainText,
				moid,
				customer.getEmail(),
				customer.getPhoneNumber(),
				customer.getName()
			);
		} catch (Exception e) {
			log.error("비인증 빌키 발급 실패", e);

			billingRequestCommandService.createBillingRequest(customerId, moid, "통신 에러", e.getMessage());

			throw new PaymentMethodException(
				PaymentMethodErrorStatus.BILLING_KEY_REGISTRATION_FAILED
			);
		}

		// 카드번호 처리: NicePay 응답의 마스킹된 번호 사용, 없으면 원본에서 마스킹 처리
		String maskedCardNo = nicePayResponse.getCardNo();
		if (maskedCardNo == null || maskedCardNo.isEmpty()) {
			// 응답에 카드번호가 없는 경우, 원본 카드번호를 마스킹 처리
			String originalCardNo = cardInfoRequest.getCardNo();
			maskedCardNo = maskCardNumber(originalCardNo);
		}

		// 화면 표시명 생성: {카드사명} {마스킹된 카드번호}
		String displayName = String.format("%s %s",
			nicePayResponse.getCardName(),
			maskedCardNo
		);

		BillingRequest billingRequest = BillingRequest.of(customer, moid);
		billingRequest.setStatus(Status.COMPLETED);

		billingRequestRepository.save(billingRequest);

		PaymentMethod paymentMethod = PaymentMethod.of(customer, billingRequest, moid, nicePayResponse.getBID(),
			nicePayResponse.getAuthDate(), nicePayResponse.getCardCode(), nicePayResponse.getCardName(),
			nicePayResponse.getCardCl().equals("0") ? CardType.CREDIT : CardType.DEBIT,
			maskedCardNo, displayName, nicePayResponse.getResultCode(), nicePayResponse.getResultMsg()
		);

		paymentMethodRepository.save(paymentMethod);

		log.info("비인증 빌링키 등록 완료: customerId={}, paymentMethodId={}", customerId, paymentMethod.getId());

		// 응답 생성
		return BillingKeyRegisterResponseDTO.from(paymentMethod);
	}

	@Override
	public void deletePaymentMethod(Long customerId, Long paymentMethodId) {
		// 고객 검색
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 대상 결제수단 조회 및 검증
		PaymentMethod paymentMethod = paymentMethodRepository
			.findByIdAndCustomerAndIsDeletedFalse(paymentMethodId, customer)
			.orElseThrow(() -> new PaymentMethodException(
				PaymentMethodErrorStatus.PAYMENT_METHOD_NOT_FOUND
			));

		// NicePay API 호출하여 빌링키 삭제
		BillingKeyDeleteResponseDTO response;
		try {
			response = nicePayService.deleteBillingKey(paymentMethod.getOrderId(), paymentMethod.getBillingKey());
		} catch (Exception e) {
			log.error("빌키 삭제 API 호출 실패", e);

			paymentMethod.setPgResponseCode("통신 실패", e.getMessage());

			throw new PaymentMethodException(
				PaymentMethodErrorStatus.BILLING_KEY_DELETION_FAILED
			);
		}

		// 소프트 삭제
		paymentMethod.delete();

		paymentMethod.setPgResponseCode(response.getResultCode(), response.getResultMsg());

		log.info("결제수단 삭제 완료: customerId={}, paymentMethodId={}", customerId, paymentMethodId);
	}

	/**
	 * 카드번호 마스킹 처리
	 * 앞 6자리와 뒤 4자리만 보이고 나머지는 *로 처리
	 * 예: 1234567890123456 → 123456******3456
	 *
	 * @param cardNo 원본 카드번호
	 * @return 마스킹된 카드번호
	 */
	private String maskCardNumber(String cardNo) {
		if (cardNo == null || cardNo.length() < 10) {
			return cardNo;
		}

		int length = cardNo.length();
		String prefix = cardNo.substring(0, 6);
		String suffix = cardNo.substring(length - 4);
		int maskedLength = length - 10;

		return prefix + "*".repeat(maskedLength) + suffix;
	}
}
