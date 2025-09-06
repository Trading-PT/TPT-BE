package com.tradingpt.tpt_api.global.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.Getter;

/**
 * 기본 예외 클래스 - 모든 커스텀 예외의 부모
 */
@Getter
public class BaseException extends RuntimeException {

	private final BaseCodeInterface errorCode;   // 에러 코드 규격(코드/기본메시지 보유)
	private final String customMessage;          // 선택: 커스텀 메시지(없으면 null)

	/** (기본) 코드만 전달 */
	public BaseException(BaseCodeInterface errorCode) {
		super(errorCode.getCode().getMessage());
		this.errorCode = errorCode;
		this.customMessage = null;
	}

	/** 코드 + 커스텀 메시지 */
	public BaseException(BaseCodeInterface errorCode, String customMessage) {
		super(customMessage != null ? customMessage : errorCode.getCode().getMessage());
		this.errorCode = errorCode;
		this.customMessage = customMessage;
	}

	/** 코드 + 원인 예외 */
	public BaseException(BaseCodeInterface errorCode, Throwable cause) {
		super(errorCode.getCode().getMessage(), cause);
		this.errorCode = errorCode;
		this.customMessage = null;
	}

	/** 코드 + 커스텀 메시지 + 원인 예외 */
	public BaseException(BaseCodeInterface errorCode, String customMessage, Throwable cause) {
		super(customMessage != null ? customMessage : errorCode.getCode().getMessage(), cause);
		this.errorCode = errorCode;
		this.customMessage = customMessage;
	}

	/** ApiResponse 등에서 코드 객체 꺼낼 때 사용 */
	public BaseCode getErrorCode() {
		return errorCode.getCode();
	}

	/** 커스텀 메시지가 있으면 그걸 우선 반환 */
	@Override
	public String getMessage() {
		return customMessage != null ? customMessage : errorCode.getCode().getMessage();
	}
}
