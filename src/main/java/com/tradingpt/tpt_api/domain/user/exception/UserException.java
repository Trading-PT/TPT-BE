package com.tradingpt.tpt_api.domain.user.exception;

import com.tradingpt.tpt_api.global.exception.BaseException;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

/**
 * 사용자 도메인 전용 예외 클래스
 * 사용자 관련 비즈니스 로직에서 발생하는 예외를 처리
 */
public class UserException extends BaseException {
	public UserException(BaseCodeInterface errorCode) {
		super(errorCode);
	}
}