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

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import lombok.extern.slf4j.Slf4j;

/**
 * NicePay URL-encoded 응답을 DTO로 변환하는 커스텀 Decoder
 *
 * NicePay 구버전 API는 다음과 같은 형식으로 응답합니다:
 * ResultCode=F100&ResultMsg=정상처리&BID=BK_xxx&TID=xxx...
 *
 * 이 Decoder는 해당 형식을 파싱하여 DTO 객체에 자동 매핑합니다.
 * 특정 날짜 필드는 자동으로 LocalDateTime으로 변환됩니다.
 */
@Slf4j
public class NicePayFormDecoder implements Decoder {

	private static final Charset EUC_KR = Charset.forName("EUC-KR");

	/**
	 * NicePay 날짜 형식: YYMMDDHHMISS (12자리)
	 * 예: "250114123045" → 2025-01-14T12:30:45
	 */
	private static final DateTimeFormatter NICEPAY_DATETIME_FORMATTER =
		DateTimeFormatter.ofPattern("yyMMddHHmmss");

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
		if (response.body() == null) {
			throw new IOException("Response body is null");
		}

		// 응답 본문을 EUC-KR로 읽기
		String body = new String(
			response.body().asInputStream().readAllBytes(),
			EUC_KR
		);

		log.debug("NicePay 응답 원문: {}", body);

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
	 * NicePay 날짜 형식(YYMMDDHHMISS)을 LocalDateTime으로 변환
	 *
	 * @param dateTimeStr YYMMDDHHMISS 형식의 문자열 (예: "250114123045")
	 * @return 변환된 LocalDateTime (예: 2025-01-14T12:30:45)
	 * @throws DateTimeParseException 변환 실패 시
	 */
	private LocalDateTime parseNicePayDateTime(String dateTimeStr) {
		if (dateTimeStr == null || dateTimeStr.isEmpty()) {
			return null;
		}

		try {
			// YYMMDDHHMISS (12자리) 형식 파싱
			if (dateTimeStr.length() == 12) {
				return LocalDateTime.parse(dateTimeStr, NICEPAY_DATETIME_FORMATTER);
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