package com.tradingpt.tpt_api.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * CSRF 토큰을 강제로 초기화해 CookieCsrfTokenRepository가 XSRF-TOKEN 쿠키를 발급하도록 만드는 필터.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									 HttpServletResponse response,
									 FilterChain filterChain) throws ServletException, IOException {

		CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
		if (csrfToken == null) {
			csrfToken = (CsrfToken) request.getAttribute("_csrf");
		}

		if (csrfToken != null) {
			csrfToken.getToken(); // DeferredCsrfToken 초기화를 트리거
		}

		filterChain.doFilter(request, response);
	}
}
