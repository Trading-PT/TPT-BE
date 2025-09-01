package com.tradingpt.tpt_api.global.exception;

import java.nio.file.AccessDeniedException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.tradingpt.tpt_api.global.common.ApiResponse;
import com.tradingpt.tpt_api.global.exception.code.BaseCode;
import com.tradingpt.tpt_api.global.exception.code.GlobalErrorStatus;

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
	public ResponseEntity<ApiResponse<String>> handleRestApiException(BaseException e) {
		BaseCode errorCode = e.getErrorCode();
		log.error("[handleRestApiException] {}", e.getMessage(), e);
		return handleExceptionInternal(errorCode);
	}

	/**
	 * 2) 예상치 못한 모든 서버 예외 처리
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<String>> handleException(Exception e) {
		log.error("[handleException] {}", e.getMessage(), e);

		// _INTERNAL_SERVER_ERROR 코드와 함께, 실제 발생한 메시지를 추가
		return handleExceptionInternalFalse(GlobalErrorStatus.INTERNAL_SERVER_ERROR.getCode(), e.getMessage());
	}

	/**
	 * 3) ConstraintViolationException (파라미터 검증, 메서드 검증 실패)
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<String>> handleConstraintViolationException(ConstraintViolationException e) {
		log.error("[handleConstraintViolationException] {}", e.getMessage(), e);

		return handleExceptionInternal(GlobalErrorStatus.INVALID_PARAMETER_FORMAT.getCode());
	}

	/**
	 * 4) MethodArgumentTypeMismatchException
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatch(
		MethodArgumentTypeMismatchException e) {
		log.error("[handleMethodArgumentTypeMismatch] {}", e.getMessage(), e);
		return handleExceptionInternal(GlobalErrorStatus.INVALID_PARAMETER_FORMAT.getCode());
	}

	/**
	 * 5) MethodArgumentNotValidException
	 */
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
		MethodArgumentNotValidException e,
		HttpHeaders headers,
		HttpStatusCode statusCode,
		WebRequest request
	) {
		Map<String, String> errors = new LinkedHashMap<>();
		e.getBindingResult().getFieldErrors().forEach(fieldError -> {
			String fieldName = fieldError.getField();
			String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
			errors.merge(fieldName, errorMessage, (oldVal, newVal) -> oldVal + ", " + newVal);
		});

		log.error("[handleMethodArgumentNotValid] {}", e.getMessage(), e);
		return handleExceptionInternalArgs(GlobalErrorStatus.INVALID_PARAMETER_FORMAT.getCode(), errors);
	}

	/**
	 * 6) Spring Security 인증/인가 예외 처리
	 */
	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiResponse<String>> handleAuthenticationException(AuthenticationException e) {
		log.error("[handleAuthenticationException] {}", e.getMessage(), e);
		return handleExceptionInternal(GlobalErrorStatus.UNAUTHORIZED.getCode());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException e) {
		log.error("[handleAccessDeniedException] {}", e.getMessage(), e);
		return handleExceptionInternal(GlobalErrorStatus.PORTFOLIO_ACCESS_DENIED.getCode());
	}

	/**
	 * 7) NoHandlerFoundException (404 Not Found)
	 */
	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
		NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.error("No handler found for {} {}", ex.getHttpMethod(), ex.getRequestURL());
		return ResponseEntity
			.status(HttpStatus.NOT_FOUND)
			.body(ApiResponse.onFailure("COMMON404", "요청하신 URL을 찾을 수 없습니다.", null));
	}

	/**
	 * 8) HttpRequestMethodNotSupportedException (405 Method Not Allowed)
	 */
	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
		HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		String supportedMethods = ex.getSupportedHttpMethods().stream()
			.map(method -> method.name())
			.collect(Collectors.joining(", "));

		log.error("Method not supported: {}", ex.getMessage());
		return ResponseEntity
			.status(HttpStatus.METHOD_NOT_ALLOWED)
			.body(ApiResponse.onFailure("COMMON405",
				String.format("지원하지 않는 HTTP 메서드입니다. 지원되는 메서드: %s", supportedMethods),
				null));
	}

	/**
	 * ==============
	 * 내부 메서드
	 * ==============
	 */
	private ResponseEntity<ApiResponse<String>> handleExceptionInternal(BaseCode errorCode) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), null));
	}

	private ResponseEntity<Object> handleExceptionInternalArgs(BaseCode errorCode, Map<String, String> errorArgs) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), errorArgs));
	}

	private ResponseEntity<ApiResponse<String>> handleExceptionInternalFalse(BaseCode errorCode,
		String errorPoint) {
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ApiResponse.onFailure(errorCode.getCode(), errorCode.getMessage(), errorPoint));
	}
}
