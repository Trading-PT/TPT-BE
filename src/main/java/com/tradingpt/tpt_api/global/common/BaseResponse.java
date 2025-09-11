package com.tradingpt.tpt_api.global.common;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"timestamp", "code", "message", "result"}) // JSON 응답 시 순서를 정의
@Schema(description = "공통 응답 DTO")
public class BaseResponse<T> {

	@Schema(description = "응답 시간", example = "2021-07-01T00:00:00")
	private final LocalDateTime timestamp = LocalDateTime.now();

	@Schema(description = "응답 코드", example = "200")
	private final String code;

	@Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
	private final String message;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	@Schema(description = "응답 데이터", example = "사용 가능한 이메일입니다.")
	private T result;

	//성공한 경우 응답 생성
	public static <T> BaseResponse<T> onSuccess(T result) {
		return new BaseResponse<>("COMMON200", "요청에 성공하였습니다.", result);
	}

	// 생성, 수정, 삭제 요청 성공 시 응답 생성
	public static <T> BaseResponse<T> onSuccessCreate(T result) {
		return new BaseResponse<>("COMMON201", "요청에 성공하였습니다.", result);
	}

	// 생성, 수정, 삭제 요청 성공 시 응답 생성
	public static <T> BaseResponse<T> onSuccessDelete(T result) {
		return new BaseResponse<>("COMMON202", "삭제 요청에 성공하였습니다.", result);
	}

	// 공통 코드를 사용하여 응답 생성
	public static <T> BaseResponse<T> of(BaseCodeInterface code, T result) {
		return new BaseResponse<>(code.getCode().getCode(), code.getCode().getMessage(), result);
	}

	// 실패한 경우 응답 생성
	public static <T> BaseResponse<T> onFailure(String code, String message, T result) {
		return new BaseResponse<>(code, message, result);
	}

}
