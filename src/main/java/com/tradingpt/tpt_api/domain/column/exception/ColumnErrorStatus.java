package com.tradingpt.tpt_api.domain.column.exception;

import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.BaseCodeInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 칼럼 도메인 에러 상태 코드 정의
 *
 * 에러 코드 형식: COLUMN_{HTTP_STATUS}_{SEQUENCE}
 * - HTTP_STATUS: 3자리 HTTP 상태 코드 (400, 404, 500 등)
 * - SEQUENCE: 같은 HTTP 상태 내 순번 (0-9)
 */
@Getter
@AllArgsConstructor
public enum ColumnErrorStatus implements BaseCodeInterface {

	// 404 Not Found
	NOT_FOUND(HttpStatus.NOT_FOUND, "COLUMN_404_0", "해당 칼럼을 찾을 수 없습니다."),
	WRITER_NOT_FOUND(HttpStatus.NOT_FOUND, "COLUMN_404_1", "작성자를 찾을 수 없습니다."),
	CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "COLUMN_404_2", "해당 카테고리를 찾을 수 없습니다"),
	COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COLUMN_404_3", "해당 댓글을 찾을 수 없습니다"),

	// 403 Forbidden
	UNAUTHORIZED(HttpStatus.FORBIDDEN, "COLUMN_403_0", "작성자 본인만 수정할 수 있습니다."),
	DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "COLUMN_403_1", "칼럼 삭제 권한이 없습니다."),

	// 409 Conflict
	DUPLICATE_TITLE(HttpStatus.CONFLICT, "COLUMN_409_0", "동일한 제목의 칼럼이 이미 존재합니다."),

	// 400 Bad Request
	INVALID_ROLE(HttpStatus.BAD_REQUEST, "COLUMN_400_0", "유효하지 않은 사용자 역할입니다."),
	LIKE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "COLUMN_400_1", "좋아요 수는 10,000을 초과할 수 없습니다."),
	BEST_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "COLUMN_400_2", "해당 카테고리의 베스트는 최대 3개까지 설정할 수 있습니다."),
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
