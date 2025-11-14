package com.tradingpt.tpt_api.domain.subscriptionplan.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SubscriptionPlan 도메인 에러 상태
 */
@Getter
@AllArgsConstructor
public enum SubscriptionPlanErrorStatus implements BaseCodeInterface {

	// 404 NOT FOUND
	PLAN_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAN404", "해당 구독 플랜을 찾을 수 없습니다."),

	// 400 BAD REQUEST
	ALREADY_ACTIVE_PLAN_EXISTS(HttpStatus.BAD_REQUEST, "PLAN400", "이미 활성화된 구독 플랜이 존재합니다."),
	CANNOT_DELETE_ACTIVE_PLAN(HttpStatus.BAD_REQUEST, "PLAN401", "현재 활성 중인 플랜은 삭제할 수 없습니다."),
	INVALID_PRICE(HttpStatus.BAD_REQUEST, "PLAN402", "구독료는 0보다 커야 합니다."),
	INVALID_EFFECTIVE_DATE(HttpStatus.BAD_REQUEST, "PLAN403", "시행 종료일은 시작일보다 이후여야 합니다.");

	private final HttpStatus httpStatus;
	private final boolean isSuccess = false;
	private final String code;
	private final String message;

	@Override
	public BaseCode getCode() {
		return BaseCode.builder()
			.httpStatus(httpStatus)
			.isSuccess(isSuccess)
			.code(code)
			.message(message)
			.build();
	}
}
