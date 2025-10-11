package com.tradingpt.tpt_api.global.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * 기본 예외 클래스 - 모든 커스텀 예외의 부모
 */
public class BaseException extends RuntimeException {

	private final BaseCodeInterface errorCode;   // 에러 코드 규격(코드/기본메시지 보유)

	/**
	 * BaseException 생성자
	 * RuntimeException에 에러 메시지를 전달하여 로그에서 의미 있는 메시지 출력
	 */
	public BaseException(BaseCodeInterface errorCode) {
		super(errorCode.getCode().getMessage());  // RuntimeException에 메시지 전달
		this.errorCode = errorCode;
	}

	/** ApiResponse 등에서 코드 객체 꺼낼 때 사용 */
	public BaseCode getErrorCode() {
		return errorCode.getCode();
	}

	/** BaseCodeInterface 직접 반환 (GlobalExceptionHandler에서 사용) */
	public BaseCodeInterface getErrorCodeInterface() {
		return errorCode;
	}

}
