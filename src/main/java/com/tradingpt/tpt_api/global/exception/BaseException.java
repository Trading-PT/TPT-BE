package com.tradingpt.tpt_api.global.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 기본 예외 클래스
 * 모든 커스텀 예외의 부모 클래스
 */
@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException {

	private final BaseCodeInterface errorCode;

	public BaseCode getErrorCode() {
		return errorCode.getCode();
	}

}
