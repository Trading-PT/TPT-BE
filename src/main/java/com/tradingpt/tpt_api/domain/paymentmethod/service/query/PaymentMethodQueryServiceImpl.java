package com.tradingpt.tpt_api.domain.paymentmethod.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.paymentmethod.dto.response.PaymentMethodResponse;
import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodErrorStatus;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodException;
import com.tradingpt.tpt_api.domain.paymentmethod.repository.PaymentMethodRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PaymentMethod Query Service 구현체
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PaymentMethodQueryServiceImpl implements PaymentMethodQueryService {

	private final PaymentMethodRepository paymentMethodRepository;
	private final CustomerRepository customerRepository;

	@Override
	public PaymentMethodResponse getPrimaryPaymentMethod(Long customerId) {

		// 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 주 결제수단 조회 (없으면 null 반환)
		return paymentMethodRepository
			.findByCustomerAndIsPrimaryTrueAndIsDeletedFalse(customer)
			.map(PaymentMethodResponse::from)
			.orElse(null);
	}

	@Override
	public PaymentMethodResponse getPaymentMethod(Long customerId, Long paymentMethodId) {

		// 고객 조회
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		PaymentMethod paymentMethod = paymentMethodRepository
			.findByIdAndCustomerAndIsDeletedFalse(paymentMethodId, customer)
			.orElseThrow(() -> new PaymentMethodException(
				PaymentMethodErrorStatus.PAYMENT_METHOD_NOT_FOUND
			));

		return PaymentMethodResponse.from(paymentMethod);
	}
}
