package com.tradingpt.tpt_api.global.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * 기본 예외 클래스 - 모든 커스텀 예외의 부모
 */
public class BaseException extends RuntimeException {

	private final BaseCodeInterface errorCode;   // 에러 코드 규격(코드/기본메시지 보유)
	private final String customMessage;           // 커스텀 메시지 (null이면 errorCode의 기본 메시지 사용)

	/**
	 * BaseException 생성자
	 * RuntimeException에 에러 메시지를 전달하여 로그에서 의미 있는 메시지 출력
	 */
	public BaseException(BaseCodeInterface errorCode) {
		super(errorCode.getCode().getMessage());  // RuntimeException에 메시지 전달
		this.errorCode = errorCode;
		this.customMessage = null;
	}

	/**
	 * 커스텀 메시지를 포함하는 생성자
	 * PG사 에러 등 외부 시스템의 구체적인 에러 메시지를 사용자에게 전달하기 위해 사용
	 *
	 * @param errorCode     에러 코드
	 * @param customMessage 커스텀 에러 메시지 (사용자에게 표시될 메시지)
	 */
	public BaseException(BaseCodeInterface errorCode, String customMessage) {
		super(customMessage != null ? customMessage : errorCode.getCode().getMessage());
		this.errorCode = errorCode;
		this.customMessage = customMessage;
	}

	/** ApiResponse 등에서 코드 객체 꺼낼 때 사용 */
	public BaseCode getErrorCode() {
		return errorCode.getCode();
	}

	/** BaseCodeInterface 직접 반환 (GlobalExceptionHandler에서 사용) */
	public BaseCodeInterface getErrorCodeInterface() {
		return errorCode;
	}

	/**
	 * 커스텀 메시지 반환
	 * GlobalExceptionHandler에서 customMessage가 있으면 이를 우선 사용
	 *
	 * @return 커스텀 메시지 (없으면 null)
	 */
	public String getCustomMessage() {
		return customMessage;
	}

	/**
	 * 커스텀 메시지가 있는지 확인
	 *
	 * @return 커스텀 메시지 존재 여부
	 */
	public boolean hasCustomMessage() {
		return customMessage != null && !customMessage.isEmpty();
	}

}
