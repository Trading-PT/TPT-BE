package com.tradingpt.tpt_api.global.infrastructure.s3.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum S3ErrorStatus implements BaseCodeInterface {

	EMPTY_FILE(HttpStatus.BAD_REQUEST, "S3_4001", "업로드할 파일이 존재하지 않습니다."),
	INVALID_FILENAME(HttpStatus.BAD_REQUEST, "S3_4002", "파일 이름이 유효하지 않습니다."),
	UNSUPPORTED_EXTENSION(HttpStatus.BAD_REQUEST, "S3_4003", "지원하지 않는 파일 확장자입니다."),
	INVALID_CONTENT(HttpStatus.BAD_REQUEST, "S3_4004", "업로드할 데이터가 올바르지 않습니다."),
	INVALID_OBJECT_KEY(HttpStatus.BAD_REQUEST, "S3_4005", "객체 키 값이 잘못되었습니다."),
	UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_5001", "파일 업로드에 실패했습니다."),
	DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_5002", "파일 삭제에 실패했습니다.");

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
