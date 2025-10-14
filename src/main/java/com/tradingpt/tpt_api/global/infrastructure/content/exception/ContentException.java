package com.tradingpt.tpt_api.global.infrastructure.content.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

public class ContentException extends BaseException {
	/**
	 * BaseException 생성자
	 * RuntimeException에 에러 메시지를 전달하여 로그에서 의미 있는 메시지 출력
	 *
	 * @param errorCode
	 */
	public ContentException(BaseCodeInterface errorCode) {
		super(errorCode);
	}
}
