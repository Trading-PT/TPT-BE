package com.tradingpt.tpt_api.global.infrastructure.nicepay.exception;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * NicePay API 에러 응답을 처리하는 Feign ErrorDecoder
 */
@Slf4j
public class NicePayErrorDecoder implements ErrorDecoder {

	private static final Charset EUC_KR = Charset.forName("EUC-KR");
	private final ErrorDecoder defaultErrorDecoder = new Default();

	@Override
	public Exception decode(String methodKey, Response response) {
		log.error("NicePay API error: method={}, status={}", methodKey, response.status());

		// HTTP 상태 코드에 따른 처리
		if (response.status() >= 500) {
			log.error("NicePay server error: {}", response.status());
			return new NicePayException(NicePayErrorStatus.API_CONNECTION_FAILED);
		}

		if (response.status() == 408) {
			log.error("NicePay API timeout");
			return new NicePayException(NicePayErrorStatus.API_TIMEOUT);
		}

		// 응답 본문 읽기 시도
		try {
			if (response.body() != null) {
				String body = new String(
					response.body().asInputStream().readAllBytes(),
					EUC_KR
				);
				log.error("NicePay API error body: {}", body);

				// URL-encoded 형식 파싱
				Map<String, String> params = parseUrlEncodedResponse(body);

				String resultCode = params.get("ResultCode");
				String resultMsg = params.get("ResultMsg");

				if (resultCode != null) {
					log.error("NicePay error: ResultCode={}, ResultMsg={}", resultCode, resultMsg);
					NicePayErrorStatus errorStatus = NicePayErrorStatus.fromResultCode(resultCode);
					return new NicePayException(errorStatus);
				}
			}
		} catch (IOException e) {
			log.error("Failed to read NicePay error response", e);
		}

		// 기본 에러 디코더로 위임
		return defaultErrorDecoder.decode(methodKey, response);
	}

	/**
	 * URL-encoded 형식의 문자열을 Map으로 파싱
	 * NicePayFormDecoder와 동일한 로직
	 *
	 * @param body ResultCode=F100&ResultMsg=정상처리&... 형식의 문자열
	 * @return 파싱된 키-값 맵
	 */
	private Map<String, String> parseUrlEncodedResponse(String body) {
		Map<String, String> params = new HashMap<>();

		if (body == null || body.isEmpty()) {
			return params;
		}

		String[] pairs = body.split("&");

		for (String pair : pairs) {
			String[] keyValue = pair.split("=", 2);

			if (keyValue.length == 2) {
				String key = keyValue[0].trim();
				String value = keyValue[1].trim();

				try {
					value = URLDecoder.decode(value, EUC_KR);
				} catch (Exception e) {
					log.warn("URL 디코딩 실패: key={}, value={}", key, value);
				}

				params.put(key, value);
			} else if (keyValue.length == 1) {
				params.put(keyValue[0].trim(), "");
			}
		}

		return params;
	}
}