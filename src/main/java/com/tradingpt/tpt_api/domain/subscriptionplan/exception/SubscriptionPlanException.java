package com.tradingpt.tpt_api.domain.subscriptionplan.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * SubscriptionPlan 도메인 예외
 */
public class SubscriptionPlanException extends BaseException {

	public SubscriptionPlanException(BaseCodeInterface errorCode) {
		super(errorCode);
	}
}
