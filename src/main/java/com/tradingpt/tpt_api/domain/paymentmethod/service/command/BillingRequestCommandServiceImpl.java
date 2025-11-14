package com.tradingpt.tpt_api.domain.paymentmethod.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.BillingRequest;
import com.tradingpt.tpt_api.domain.paymentmethod.enums.Status;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodErrorStatus;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodException;
import com.tradingpt.tpt_api.domain.paymentmethod.repository.BillingRequestRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BillingRequestCommandServiceImpl implements BillingRequestCommandService {

	private final BillingRequestRepository billingRequestRepository;
	private final CustomerRepository customerRepository;

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void createBillingRequest(Long customerId, String moid, String resultCode, String resultMsg) {
		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		BillingRequest billingRequest = BillingRequest.of(customer, moid, resultCode, resultMsg);

		billingRequestRepository.save(billingRequest);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateBillingRequestStatus(Long billingRequestId, Status status, String resultCode, String resultMsg) {
		BillingRequest billingRequest = billingRequestRepository.findById(billingRequestId)
			.orElseThrow(() -> new PaymentMethodException(PaymentMethodErrorStatus.BILLING_KEY_REGISTRATION_FAILED));

		billingRequest.setStatus(status);
		billingRequest.setResultCode(resultCode);
		billingRequest.setResultMsg(resultMsg);

	}
}
