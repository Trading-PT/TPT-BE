package com.tradingpt.tpt_api.domain.feedbackrequest.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class FeedbackRequestException extends BaseException {
	public FeedbackRequestException(BaseCodeInterface errorCode) {
		super(errorCode);
	}
}
