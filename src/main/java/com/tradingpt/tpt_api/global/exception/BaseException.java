package com.tradingpt.tpt_api.global.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;

/**
 * 기본 예외 클래스 - 모든 커스텀 예외의 부모
 */
@AllArgsConstructor
public class BaseException extends RuntimeException {

	private final BaseCodeInterface errorCode;   // 에러 코드 규격(코드/기본메시지 보유)

	/** ApiResponse 등에서 코드 객체 꺼낼 때 사용 */
	public BaseCode getErrorCode() {
		return errorCode.getCode();
	}

}
