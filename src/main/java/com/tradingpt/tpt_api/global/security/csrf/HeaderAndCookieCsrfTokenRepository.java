package com.tradingpt.tpt_api.global.security.csrf;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Cookie 기반 CSRF 토큰을 쿠키와 응답 헤더 모두에 세팅해주는 저장소.
 */
public class HeaderAndCookieCsrfTokenRepository implements CsrfTokenRepository {

	private static final String DEFAULT_HEADER_NAME = "XSRF-TOKEN";

	private final CookieCsrfTokenRepository delegate;
	private String headerName = DEFAULT_HEADER_NAME;

	public HeaderAndCookieCsrfTokenRepository() {
		this.delegate = CookieCsrfTokenRepository.withHttpOnlyFalse();
		this.delegate.setCookiePath("/");
	}

	@Override
	public CsrfToken generateToken(HttpServletRequest request) {
		return delegate.generateToken(request);
	}

	@Override
	public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
		delegate.saveToken(token, request, response);

		if (token == null) {
			response.setHeader(headerName, "");
			return;
		}

		response.setHeader(headerName, token.getToken());
	}

	@Override
	public CsrfToken loadToken(HttpServletRequest request) {
		return delegate.loadToken(request);
	}

	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public void setCookieDomain(String domainName) {
		delegate.setCookieDomain(domainName);
	}

	public void setCookieHttpOnly(boolean httpOnly) {
		delegate.setCookieHttpOnly(httpOnly);
	}

	public void setCookieName(String cookieName) {
		delegate.setCookieName(cookieName);
	}

	public void setCookiePath(String cookiePath) {
		delegate.setCookiePath(cookiePath);
	}

	public void setCookieMaxAge(int maxAge) {
		delegate.setCookieMaxAge(maxAge);
	}

	public void setCookieCustomizer(java.util.function.Consumer<org.springframework.http.ResponseCookie.ResponseCookieBuilder> customizer) {
		delegate.setCookieCustomizer(customizer);
	}
}
