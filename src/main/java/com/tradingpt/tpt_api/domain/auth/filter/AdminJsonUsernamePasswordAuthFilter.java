package com.tradingpt.tpt_api.domain.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * /admin/api/login 전용 JSON 로그인 필터
 * - 인증 성공/실패 후 로직은 Success/FailureHandler가 담당
 */
@RequiredArgsConstructor
public class AdminJsonUsernamePasswordAuthFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> creds = objectMapper.readValue(request.getInputStream(), Map.class);
            String username = String.valueOf(creds.getOrDefault("id", ""));
            String password = String.valueOf(creds.getOrDefault("pw", ""));

            var authRequest = new UsernamePasswordAuthenticationToken(username, password);
            setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            throw new AuthenticationServiceException("Invalid JSON request", e);
        }
    }
}
