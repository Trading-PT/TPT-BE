package com.tradingpt.tpt_api.global.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * 인증/인가 관련 예외 클래스
 * Trading PT 인증 시스템에서 발생하는 모든 인증 관련 오류를 처리
 */
public class AuthException extends BaseException {

	public AuthException(BaseCodeInterface baseCodeInterface) {
		super(baseCodeInterface);
	}
}
