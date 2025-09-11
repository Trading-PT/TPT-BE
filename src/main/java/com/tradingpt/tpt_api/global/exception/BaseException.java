package com.tradingpt.tpt_api.global.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.Getter;

/**
 * 기본 예외 클래스 - 모든 커스텀 예외의 부모
 */
@Getter
public class BaseException extends RuntimeException {

	private final BaseCodeInterface errorCode;

	/** (기본) 코드만 전달 */
	public BaseException(BaseCodeInterface errorCode) {
		super(errorCode.getCode().getMessage());
		this.errorCode = errorCode;
	}

	/** 코드 + 커스텀 메시지 */
	public BaseException(BaseCodeInterface errorCode, String customMessage) {
		super(customMessage);
		this.errorCode = errorCode;
	}

	/** ApiResponse 등에서 코드 객체 꺼낼 때 사용 */
	public BaseCode getErrorCode() {
		return this.errorCode.getCode();
	}
}
