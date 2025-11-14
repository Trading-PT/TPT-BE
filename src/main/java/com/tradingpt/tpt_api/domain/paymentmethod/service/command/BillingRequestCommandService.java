package com.tradingpt.tpt_api.domain.paymentmethod.service.command;

import com.tradingpt.tpt_api.domain.paymentmethod.enums.Status;

public interface BillingRequestCommandService {
	void createBillingRequest(Long customerId, String moid, String resultCode, String resultMsg);

	void updateBillingRequestStatus(Long billingRequestId, Status status, String resultCode, String resultMsg);
}
