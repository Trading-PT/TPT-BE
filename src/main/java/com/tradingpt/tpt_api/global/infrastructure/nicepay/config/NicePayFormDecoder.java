package com.tradingpt.tpt_api.global.infrastructure.nicepay.config;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;

/**
 * NicePay 응답을 DTO로 변환하는 커스텀 Decoder
 *
 * NicePay API는 두 가지 형식으로 응답합니다:
 * 1. URL-encoded: ResultCode=F100&ResultMsg=정상처리&BID=BK_xxx&TID=xxx... (인증 방식)
 * 2. JSON: {"ResultCode":"F100","ResultMsg":"정상처리","BID":"BK_xxx",...} (비인증 방식)
 *
 * 이 Decoder는 응답 형식을 자동 감지하여 DTO 객체에 매핑합니다.
 * 특정 날짜 필드는 자동으로 LocalDateTime으로 변환됩니다.
 */
@Slf4j
public class NicePayFormDecoder implements Decoder {

	private static final Charset EUC_KR = Charset.forName("EUC-KR");
	private static final ObjectMapper objectMapper = createObjectMapper();

	/**
	 * Jackson ObjectMapper 생성 및 설정
	 * NicePay 날짜 형식을 처리하기 위한 커스텀 Deserializer 등록
	 */
	private static ObjectMapper createObjectMapper() {
		ObjectMapper mapper = new ObjectMapper();

		// 대소문자 구분 없이 필드 매핑 (ResultCode → resultCode 허용)
		mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

		// 알 수 없는 JSON 필드 무시 (DTO에 없는 필드가 있어도 에러 없이 진행)
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		// NicePay 날짜 형식을 위한 커스텀 Deserializer 등록
		SimpleModule module = new SimpleModule();
		module.addDeserializer(LocalDateTime.class, new NicePayDateTimeDeserializer());
		mapper.registerModule(module);

		return mapper;
	}

	/**
	 * NicePay 날짜 형식을 LocalDateTime으로 변환하는 커스텀 Deserializer
	 * YYYYMMDD (8자리) 또는 YYMMDDHHMISS (12자리) 형식 지원
	 */
	private static class NicePayDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
		@Override
		public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
			String dateStr = parser.getText();
			if (dateStr == null || dateStr.isEmpty()) {
				return null;
			}

			try {
				// YYMMDDHHMISS (12자리) 형식
				if (dateStr.length() == 12) {
					return LocalDateTime.parse(dateStr, NICEPAY_DATETIME_FORMATTER);
				}

				// YYYYMMDD (8자리) 형식 - 시간을 00:00:00으로 설정
				if (dateStr.length() == 8) {
					String dateTimeStr = dateStr + "000000";
					return LocalDateTime.parse(dateTimeStr, NICEPAY_DATE_TO_DATETIME_FORMATTER);
				}

				log.warn("예상치 못한 날짜 형식: {} (길이: {})", dateStr, dateStr.length());
				return null;

			} catch (Exception e) {
				log.error("날짜 파싱 실패: {}", dateStr, e);
				return null;
			}
		}
	}

	/**
	 * NicePay 날짜 형식: YYMMDDHHMISS (12자리)
	 * 예: "250114123045" → 2025-01-14T12:30:45
	 */
	private static final DateTimeFormatter NICEPAY_DATETIME_FORMATTER =
		DateTimeFormatter.ofPattern("yyMMddHHmmss");

	/**
	 * NicePay 날짜 형식: YYYYMMDD (8자리) - 비인증 빌링키 API 응답
	 * 예: "20250114" → 2025-01-14T00:00:00
	 */
	private static final DateTimeFormatter NICEPAY_DATE_FORMATTER =
		DateTimeFormatter.ofPattern("yyyyMMdd");

	/**
	 * NicePay 날짜+시간 형식: YYYYMMDD000000 (14자리) - 날짜를 LocalDateTime으로 변환
	 */
	private static final DateTimeFormatter NICEPAY_DATE_TO_DATETIME_FORMATTER =
		DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	/**
	 * LocalDateTime으로 변환할 필드명 목록
	 * NicePay API의 날짜/시간 필드명을 여기에 추가
	 */
	private static final Set<String> DATETIME_FIELDS = Set.of(
		"AuthDate",      // 빌키 발급일시 (YYMMDDHHMISS)
		"TrDate",        // 거래일시
		"CancelDate"     // 취소일시
		// 필요시 추가
	);

	@Override
	public Object decode(Response response, Type type) throws IOException, FeignException {
		log.info("=== NicePayFormDecoder.decode() 호출됨 ===");
		log.info("응답 상태 코드: {}", response.status());
		log.info("응답 헤더: {}", response.headers());
		log.info("응답 DTO 타입: {}", type);

		if (response.body() == null) {
			log.error("응답 본문(body)이 NULL입니다!");
			throw new IOException("Response body is null");
		}

		// 응답 본문을 EUC-KR로 읽기
		String body = new String(
			response.body().asInputStream().readAllBytes(),
			EUC_KR
		);

		log.info("=== 원본 응답 본문 (길이={}): {} ===", body.length(), body);
		log.debug("NicePay 응답 원문: {}", body);

		// 응답 형식 감지: JSON인지 URL-encoded인지 판단
		String trimmedBody = body.trim();
		boolean isJson = trimmedBody.startsWith("{") && trimmedBody.endsWith("}");

		if (isJson) {
			log.info("JSON 형식 응답 감지 - Jackson으로 파싱");
			return parseJsonResponse(body, type);
		} else {
			log.info("URL-encoded 형식 응답 감지 - 커스텀 파서로 파싱");
			return parseUrlEncodedResponseToDto(body, type);
		}
	}

	/**
	 * JSON 형식의 응답을 DTO로 변환
	 * 비인증 빌링키 API가 JSON으로 응답하는 경우 사용
	 *
	 * @param body JSON 문자열
	 * @param type 변환할 DTO 클래스 타입
	 * @return 변환된 DTO 객체
	 * @throws IOException JSON 파싱 실패 시
	 */
	private Object parseJsonResponse(String body, Type type) throws IOException {
		try {
			Class<?> responseClass = (Class<?>)type;

			// Jackson으로 JSON 파싱
			Object responseDto = objectMapper.readValue(body, responseClass);

			log.info("JSON 파싱 성공: {}", responseDto);
			return responseDto;

		} catch (Exception e) {
			log.error("JSON 파싱 실패", e);
			throw new IOException("Failed to parse JSON response", e);
		}
	}

	/**
	 * URL-encoded 형식의 응답을 DTO로 변환
	 * 인증 빌링키 API가 URL-encoded로 응답하는 경우 사용
	 *
	 * @param body URL-encoded 문자열 (Key=Value&Key=Value...)
	 * @param type 변환할 DTO 클래스 타입
	 * @return 변환된 DTO 객체
	 * @throws IOException 파싱 실패 시
	 */
	private Object parseUrlEncodedResponseToDto(String body, Type type) throws IOException {
		// Key=Value& 형식 파싱
		Map<String, String> params = parseUrlEncodedResponse(body);

		log.debug("파싱된 파라미터: {}", params);

		// DTO 객체 생성 및 매핑
		try {
			Class<?> responseClass = (Class<?>)type;
			Object responseDto = responseClass.getDeclaredConstructor().newInstance();

			// BeanWrapper를 사용하여 필드 자동 매핑
			BeanWrapper beanWrapper = new BeanWrapperImpl(responseDto);

			for (Map.Entry<String, String> entry : params.entrySet()) {
				String fieldName = entry.getKey();
				String fieldValue = entry.getValue();

				// 필드가 존재하고 쓰기 가능한 경우에만 설정
				if (beanWrapper.isWritableProperty(fieldName)) {
					try {
						// 날짜/시간 필드인 경우 LocalDateTime으로 변환
						if (DATETIME_FIELDS.contains(fieldName) &&
							beanWrapper.getPropertyType(fieldName) == LocalDateTime.class) {

							LocalDateTime dateTime = parseNicePayDateTime(fieldValue);
							beanWrapper.setPropertyValue(fieldName, dateTime);
							log.debug("날짜 변환 성공: {}={} → {}",
								fieldName, fieldValue, dateTime);
						} else {
							// 일반 필드는 그대로 설정 (BeanWrapper가 자동 타입 변환)
							beanWrapper.setPropertyValue(fieldName, fieldValue);
						}
					} catch (Exception e) {
						log.warn("필드 매핑 실패: {}={}, error={}",
							fieldName, fieldValue, e.getMessage());
					}
				}
			}

			return responseDto;

		} catch (Exception e) {
			log.error("DTO 변환 실패", e);
			throw new IOException("Failed to convert response to DTO", e);
		}
	}

	/**
	 * NicePay 날짜 형식(YYMMDDHHMISS 또는 YYYYMMDD)을 LocalDateTime으로 변환
	 *
	 * @param dateTimeStr YYMMDDHHMISS (12자리) 또는 YYYYMMDD (8자리) 형식의 문자열
	 * @return 변환된 LocalDateTime
	 * @throws DateTimeParseException 변환 실패 시
	 */
	private LocalDateTime parseNicePayDateTime(String dateTimeStr) {
		if (dateTimeStr == null || dateTimeStr.isEmpty()) {
			return null;
		}

		try {
			// YYMMDDHHMISS (12자리) 형식 파싱 - 인증 빌링키 API
			if (dateTimeStr.length() == 12) {
				return LocalDateTime.parse(dateTimeStr, NICEPAY_DATETIME_FORMATTER);
			}

			// YYYYMMDD (8자리) 형식 파싱 - 비인증 빌링키 API
			// 날짜에 "000000"을 추가하여 14자리로 만든 후 파싱
			if (dateTimeStr.length() == 8) {
				String dateTimeWithZeroTime = dateTimeStr + "000000";
				return LocalDateTime.parse(dateTimeWithZeroTime, NICEPAY_DATE_TO_DATETIME_FORMATTER);
			}

			log.warn("예상치 못한 날짜 형식: {} (길이: {})", dateTimeStr, dateTimeStr.length());
			return null;

		} catch (DateTimeParseException e) {
			log.error("날짜 파싱 실패: {}", dateTimeStr, e);
			throw e;
		}
	}

	/**
	 * URL-encoded 형식의 문자열을 Map으로 파싱
	 *
	 * @param body ResultCode=F100&ResultMsg=정상처리&... 형식의 문자열
	 * @return 파싱된 키-값 맵
	 */
	private Map<String, String> parseUrlEncodedResponse(String body) {
		Map<String, String> params = new HashMap<>();

		if (body == null || body.isEmpty()) {
			return params;
		}

		// & 기준으로 분리
		String[] pairs = body.split("&");

		for (String pair : pairs) {
			// = 기준으로 키와 값 분리 (값에 =이 포함될 수 있으므로 limit=2)
			String[] keyValue = pair.split("=", 2);

			if (keyValue.length == 2) {
				String key = keyValue[0].trim();
				String value = keyValue[1].trim();

				try {
					// URL 디코딩 (필요시)
					value = URLDecoder.decode(value, EUC_KR);
				} catch (Exception e) {
					log.warn("URL 디코딩 실패: key={}, value={}", key, value);
					// 디코딩 실패 시 원본 값 사용
				}

				params.put(key, value);
			} else if (keyValue.length == 1) {
				// 값이 없는 경우 빈 문자열로 처리
				params.put(keyValue[0].trim(), "");
			}
		}

		return params;
	}
}