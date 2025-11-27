package com.tradingpt.tpt_api.global.infrastructure.s3.exception;

import org.springframework.http.HttpStatus;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * S3 인프라 에러 상태 코드 정의
 *
 * 에러 코드 형식: S3_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum S3ErrorStatus implements BaseCodeInterface {

	// 500 Internal Server Error
	UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_0", "파일 업로드에 실패했습니다."),
	DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_1", "파일 삭제에 실패했습니다."),
	PRESIGN_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S3_500_2", "사전 서명 URL 생성에 실패했습니다."),

	// 400 Bad Request
	EMPTY_FILE(HttpStatus.BAD_REQUEST, "S3_400_0", "업로드할 파일이 존재하지 않습니다."),
	INVALID_FILENAME(HttpStatus.BAD_REQUEST, "S3_400_1", "파일 이름이 유효하지 않습니다."),
	UNSUPPORTED_EXTENSION(HttpStatus.BAD_REQUEST, "S3_400_2", "지원하지 않는 파일 확장자입니다."),
	INVALID_CONTENT(HttpStatus.BAD_REQUEST, "S3_400_3", "업로드할 데이터가 올바르지 않습니다."),
	INVALID_OBJECT_KEY(HttpStatus.BAD_REQUEST, "S3_400_4", "객체 키 값이 잘못되었습니다."),
	;

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
