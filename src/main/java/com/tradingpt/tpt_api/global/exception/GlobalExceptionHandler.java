package com.tradingpt.tpt_api.global.exception;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tradingpt.tpt_api.global.common.BaseResponse;
import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.GlobalErrorStatus;

import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	/**
	 * 1) 직접 정의한 비즈니스/도메인 예외 처리
	 */
	@ExceptionHandler(value = BaseException.class)
	public ResponseEntity<BaseResponse<String>> handleRestApiException(BaseException e) {
		BaseCode errorCode = e.getErrorCode();
		log.error("[handleRestApiException] Domain Exception: {}", e.getMessage(), e);
		return handleExceptionInternal(errorCode);
	}

	/**
	 * 2) HTTP 메시지 읽기 불가 (JSON 파싱 오류 등)
	 */
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable(
		HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.error("[handleHttpMessageNotReadable] JSON parsing error: {}", ex.getMessage());

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(BaseResponse.of(GlobalErrorStatus.INVALID_REQUEST_BODY, null));
	}

	/**
	 * 3) 지원하지 않는 미디어 타입
	 */
	@Override
	protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
		HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String supportedTypes = ex.getSupportedMediaTypes().stream()
			.map(type -> type.toString())
			.collect(Collectors.joining(", "));

		log.error("[handleHttpMediaTypeNotSupported] Unsupported media type: {}", ex.getContentType());

		BaseResponse<String> response = BaseResponse.onFailure(
			GlobalErrorStatus.UNSUPPORTED_MEDIA_TYPE.getCode().getCode(),
			String.format("지원하지 않는 미디어 타입입니다. 지원되는 타입: %s", supportedTypes),
			null
		);

		return ResponseEntity
			.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
			.body(response);
	}

	/**
	 * 4) 필수 파라미터 누락
	 */
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestParameter(
		MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.error("[handleMissingServletRequestParameter] Missing parameter: {}", ex.getParameterName());

		BaseResponse<String> response = BaseResponse.onFailure(
			GlobalErrorStatus.MISSING_PARAMETER.getCode().getCode(),
			String.format("필수 파라미터가 누락되었습니다: %s", ex.getParameterName()),
			null
		);

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}

	/**
	 * 5) 파일 업로드 크기 초과
	 */
	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<BaseResponse<String>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException e) {
		log.error("[handleMaxUploadSizeExceeded] File size exceeded: {}", e.getMessage());

		BaseResponse<String> response = BaseResponse.onFailure(
			GlobalErrorStatus._BAD_REQUEST.getCode().getCode(), // ✅ 수정
			"업로드 파일 크기가 제한을 초과했습니다.",
			null
		);

		return ResponseEntity
			.status(HttpStatus.BAD_REQUEST)
			.body(response);
	}

	/**
	 * 6) 데이터베이스 제약조건 위반
	 */
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<BaseResponse<String>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
		log.error("[handleDataIntegrityViolation] Database constraint violation: {}", e.getMessage());

		// 민감한 DB 정보 노출 방지
		String message = "데이터 무결성 제약조건을 위반했습니다.";
		if (e.getMessage() != null && e.getMessage().contains("Duplicate entry")) {
			message = "중복된 데이터가 존재합니다.";
		}

		return handleExceptionInternal(GlobalErrorStatus.CONFLICT.getCode(), message);
	}

	/**
	 * 7) SQL 예외
	 */
	@ExceptionHandler(SQLException.class)
	public ResponseEntity<BaseResponse<String>> handleSQLException(SQLException e) {
		log.error("[handleSQLException] Database error: {}", e.getMessage(), e);

		// 프로덕션에서는 민감한 정보 숨김
		return handleExceptionInternal(GlobalErrorStatus.DATABASE_ERROR.getCode());
	}

	/**
	 * 8) Bean Validation 제약조건 위반
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<BaseResponse<String>> handleConstraintViolationException(ConstraintViolationException e) {
		log.error("[handleConstraintViolationException] Validation failed: {}", e.getMessage());

		String errorMessage = e.getConstraintViolations().stream()
			.map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
			.collect(Collectors.joining(", "));

		return handleExceptionInternal(GlobalErrorStatus.VALIDATION_ERROR.getCode(), errorMessage);
	}

	/**
	 * 9) 메서드 인자 타입 불일치
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<BaseResponse<String>> handleMethodArgumentTypeMismatch(
		MethodArgumentTypeMismatchException e) {
		log.error("[handleMethodArgumentTypeMismatch] Type mismatch for parameter '{}': {}",
			e.getName(), e.getMessage());

		String message = String.format("파라미터 '%s'의 타입이 올바르지 않습니다. 예상 타입: %s",
			e.getName(), e.getRequiredType().getSimpleName());

		return handleExceptionInternal(GlobalErrorStatus.INVALID_PARAMETER_TYPE.getCode(), message);
	}

	/**
	 * 10) Bean Validation 실패 (상세 처리)
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		MethodArgumentNotValidException e,
		HttpHeaders headers,
		HttpStatusCode statusCode,
		WebRequest request
	) {
		Map<String, String> errors = new LinkedHashMap<>();

		// 필드 에러 처리
		e.getBindingResult().getFieldErrors().forEach(fieldError -> {
			String fieldName = fieldError.getField();
			String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("유효하지 않은 값입니다.");
			errors.merge(fieldName, errorMessage, (oldVal, newVal) -> oldVal + ", " + newVal);
		});

		// 글로벌 에러 처리
		e.getBindingResult().getGlobalErrors().forEach(globalError -> {
			String objectName = globalError.getObjectName();
			String errorMessage = Optional.ofNullable(globalError.getDefaultMessage()).orElse("유효하지 않은 객체입니다.");
			errors.put(objectName, errorMessage);
		});

		log.error("[handleMethodArgumentNotValid] Validation errors: {}", errors);

		return handleExceptionInternalArgs(GlobalErrorStatus.VALIDATION_ERROR.getCode(), errors);
	}

	/**
	 * 11) Spring Security 기본 인증 예외 (일반적인 인증 실패)
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<BaseResponse<String>> handleAuthenticationException(AuthenticationException e) {
		log.warn("[handleAuthenticationException] Authentication failed: {}", e.getMessage());
		// ✅ GlobalErrorStatusCode의 _UNAUTHORIZED 사용 (Spring Security 기본 예외)
		return handleExceptionInternal(GlobalErrorStatus._UNAUTHORIZED.getCode());
	}

	/**
	 * 12) Spring Security 기본 인가 예외 (권한 부족)
	 */
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<BaseResponse<String>> handleAccessDeniedException(AccessDeniedException e) {
		log.warn("[handleAccessDeniedException] Access denied: {}", e.getMessage());
		// ✅ GlobalErrorStatusCode의 _FORBIDDEN 사용 (Spring Security 기본 예외)
		return handleExceptionInternal(GlobalErrorStatus._FORBIDDEN.getCode());
	}

	/**
	 * 13) 핸들러 없음 (404)
	 */
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
		NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.warn("[handleNoHandlerFoundException] No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());

		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.body(BaseResponse.of(GlobalErrorStatus.RESOURCE_NOT_FOUND, null));
	}

	/**
	 * 14) 지원하지 않는 HTTP 메서드 (405)
	 */
	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
		HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String supportedMethods = ex.getSupportedHttpMethods().stream()
			.map(method -> method.name())
			.collect(Collectors.joining(", "));

		log.warn("[handleHttpRequestMethodNotSupported] Method not supported: {}", ex.getMessage());

		BaseResponse<String> response = BaseResponse.onFailure(
			GlobalErrorStatus.METHOD_NOT_ALLOWED.getCode().getCode(), // ✅ 기존 코드 유지 (올바름)
			String.format("지원하지 않는 HTTP 메서드입니다. 지원되는 메서드: %s", supportedMethods),
			null
		);

		return ResponseEntity
			.status(HttpStatus.METHOD_NOT_ALLOWED)
			.body(response);
	}

	/**
	 * 15) 모든 예상치 못한 예외 (최종 안전망)
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<BaseResponse<String>> handleException(Exception e) {
		log.error("[handleException] Unexpected error occurred: {}", e.getMessage(), e);

		// 프로덕션에서는 내부 에러 메시지 숨김
		String message = "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.";

		return handleExceptionInternalFalse(
			GlobalErrorStatus._INTERNAL_SERVER_ERROR.getCode(), // ✅ 수정
			message
		);
	}

	/**
	 * ==============
	 * 내부 메서드
	 * ==============
	 */
	private ResponseEntity<BaseResponse<String>> handleExceptionInternal(BaseCode errorCode) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
	}

	private ResponseEntity<BaseResponse<String>> handleExceptionInternal(BaseCode errorCode, String customMessage) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(BaseResponse.onFailure(errorCode.getCode(), customMessage, null));
	}

	private ResponseEntity<Object> handleExceptionInternalArgs(BaseCode errorCode, Map<String, String> errorArgs) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), errorArgs));
	}

	private ResponseEntity<BaseResponse<String>> handleExceptionInternalFalse(BaseCode errorCode, String errorPoint) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(BaseResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), errorPoint));
	}
}