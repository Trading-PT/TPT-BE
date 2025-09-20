package com.tradingpt.tpt_api.domain.feedbackresponse.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class FeedbackResponseException extends BaseException {
	public FeedbackResponseException(BaseCodeInterface errorCode) {
		super(errorCode);
	}
}

