package com.tradingpt.tpt_api.domain.investmenthistory.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class InvestmentHistoryException extends BaseException {
	public InvestmentHistoryException(BaseCodeInterface errorCode) {
		super(errorCode);
	}
}
