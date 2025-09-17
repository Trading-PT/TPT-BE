package com.tradingpt.tpt_api.global.infrastructure.s3.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class S3Exception extends BaseException {

	public S3Exception(BaseCodeInterface errorCode) {
		super(errorCode);
	}
}
