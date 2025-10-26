package com.tradingpt.tpt_api.domain.auth.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.web.csrf.CsrfToken;

import com.tradingpt.tpt_api.global.security.csrf.HeaderAndCookieCsrfTokenRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 로그인 성공 응답 등 CSRF 면제 URL에서 헤더에 토큰을 내려주기 위한 필터.
 */
@RequiredArgsConstructor
public class CsrfTokenResponseHeaderBindingFilter extends OncePerRequestFilter {

	private final HeaderAndCookieCsrfTokenRepository csrfTokenRepository;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {


		CsrfToken token = csrfTokenRepository.loadToken(request);
		if (token == null) {
			token = csrfTokenRepository.generateToken(request);
		}
		csrfTokenRepository.saveToken(token, request, response);

		filterChain.doFilter(request, response);
	}
}
