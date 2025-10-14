package com.tradingpt.tpt_api.global.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HTML 콘텐츠 정제 유틸리티
 * - 의존성 없는 순수 유틸 클래스
 * - XSS 공격 방지를 위한 HTML sanitization
 * - 허용된 태그와 속성만 유지
 */
// 유틸리티 클래스이므로 인스턴스화 방지
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HtmlSanitizer {

	/**
	 * HTML 콘텐츠 sanitize를 위한 Safelist
	 * - 기본 텍스트 포맷팅 태그 허용 (p, div, span, strong, em 등)
	 * - 링크 허용 (a 태그, http/https/mailto 프로토콜만)
	 * - 이미지 허용 (img 태그, http/https 프로토콜만)
	 * - 테이블, 리스트 등 구조화된 콘텐츠 허용
	 * - script, iframe, object 등 위험한 태그는 제거
	 */
	private static final Safelist CONTENT_SAFE_LIST = Safelist.relaxed()
		.addAttributes("a", "target", "rel")
		.addProtocols("a", "href", "http", "https", "mailto")
		.addProtocols("img", "src", "http", "https")
		.addTags("figure", "figcaption", "hr");

	/**
	 * HTML 콘텐츠를 sanitize합니다.
	 * - 위험한 태그 및 속성 제거
	 * - XSS 공격 벡터 제거
	 * - 허용된 태그와 속성만 유지
	 *
	 * @param rawHtml 원본 HTML 콘텐츠
	 * @return sanitize된 안전한 HTML 콘텐츠
	 */
	public static String sanitize(String rawHtml) {
		if (!StringUtils.hasText(rawHtml)) {
			log.debug("Empty HTML content provided for sanitization");
			return rawHtml;
		}

		String sanitized = Jsoup.clean(rawHtml, CONTENT_SAFE_LIST);
		log.debug("HTML sanitized: original length={}, sanitized length={}",
			rawHtml.length(), sanitized.length());

		return sanitized;
	}

	/**
	 * HTML 콘텐츠에 data URI 형식의 인라인 이미지가 포함되어 있는지 확인합니다.
	 *
	 * @param html HTML 콘텐츠
	 * @return 인라인 이미지가 있으면 true, 없으면 false
	 */
	public static boolean hasInlineImages(String html) {
		if (!StringUtils.hasText(html)) {
			return false;
		}
		return html.contains("data:image/");
	}

	/**
	 * 커스텀 Safelist를 사용하여 HTML을 sanitize합니다.
	 * 특별한 요구사항이 있을 때 사용
	 *
	 * @param rawHtml 원본 HTML
	 * @param safelist 커스텀 Safelist
	 * @return sanitize된 HTML
	 */
	public static String sanitizeWithCustomSafelist(String rawHtml, Safelist safelist) {
		if (!StringUtils.hasText(rawHtml)) {
			return rawHtml;
		}
		return Jsoup.clean(rawHtml, safelist);
	}

	/**
	 * 기본 Safelist를 반환합니다.
	 * 커스터마이징이 필요한 경우 사용
	 *
	 * @return 기본 Safelist의 복사본
	 */
	public static Safelist getDefaultSafelist() {
		return new Safelist(CONTENT_SAFE_LIST);
	}
}