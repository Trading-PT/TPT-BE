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

		filterChain.doFilter(request, response);

		CsrfToken csrfToken = (CsrfToken)request.getAttribute(CsrfToken.class.getName());
		if (csrfToken == null) {
			csrfToken = (CsrfToken)request.getAttribute("_csrf");
		}

		String tokenValue = csrfToken != null ? csrfToken.getToken() : null;
		if (tokenValue == null) {
			for (String setCookie : response.getHeaders("Set-Cookie")) {
				int index = setCookie.indexOf("XSRF-TOKEN=");
				if (index < 0) {
					continue;
				}

				String valuePortion = setCookie.substring(index + "XSRF-TOKEN=".length());
				int semicolon = valuePortion.indexOf(';');
				if (semicolon >= 0) {
					valuePortion = valuePortion.substring(0, semicolon);
				}

				tokenValue = java.net.URLDecoder.decode(valuePortion, java.nio.charset.StandardCharsets.UTF_8);
				break;
			}
		}

		if (tokenValue != null) {
			response.setHeader("XSRF-TOKEN", tokenValue);
		}
	}
}
