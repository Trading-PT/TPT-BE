package com.tradingpt.tpt_api.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public class JsonUsernamePasswordAuthFilter extends UsernamePasswordAuthenticationFilter {

    private static final String REMEMBER_ME_ATTR = "REMEMBER_ME_JSON";
    private final ObjectMapper objectMapper;

    public JsonUsernamePasswordAuthFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // 이 URL로 들어온 JSON 로그인 요청을 필터가 처리
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().contains("application/json")) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = objectMapper.readValue(request.getInputStream(), Map.class);

                String username = String.valueOf(body.getOrDefault("username", ""));
                String password = String.valueOf(body.getOrDefault("password", ""));
                boolean rememberMe = Boolean.TRUE.equals(body.get("rememberMe"));

                // 성공 시점에 사용할 플래그를 요청 속성에 저장
                request.setAttribute(REMEMBER_ME_ATTR, rememberMe);

                UsernamePasswordAuthenticationToken authRequest =
                        new UsernamePasswordAuthenticationToken(username, password);
                setDetails(request, authRequest);

                return this.getAuthenticationManager().authenticate(authRequest);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse login JSON body", e);
            }
        }

        // JSON이 아니면 부모 동작(폼 파라미터) 사용
        return super.attemptAuthentication(request, response);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {

        // 1) 세션 생성 후 ID 교체 (세션 고정 방지)
        request.getSession(true);
        request.changeSessionId();

        // 2) SecurityContext 생성 및 세션에 저장
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);

        // 3) remember-me 처리
        boolean rememberMe = Boolean.TRUE.equals(request.getAttribute("REMEMBER_ME_JSON"));
        RememberMeServices remember = getRememberMeServices();
        if (remember != null && rememberMe) {
            remember.loginSuccess(request, response, authResult);
        }

        // 4) 성공 핸들러 호출
        getSuccessHandler().onAuthenticationSuccess(request, response, authResult);
    }
}