package com.tradingpt.tpt_api.domain.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;
import com.tradingpt.tpt_api.domain.auth.security.CustomOAuth2User;
import com.tradingpt.tpt_api.domain.auth.security.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

public class JsonUsernamePasswordAuthFilter extends UsernamePasswordAuthenticationFilter {

    private static final Logger log = LoggerFactory.getLogger(JsonUsernamePasswordAuthFilter.class);
    private static final String REMEMBER_ME_ATTR = "REMEMBER_ME_JSON";
    private final ObjectMapper objectMapper;

    public JsonUsernamePasswordAuthFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        setFilterProcessesUrl("/api/v1/auth/login");
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

                request.setAttribute(REMEMBER_ME_ATTR, rememberMe);

                UsernamePasswordAuthenticationToken authRequest =
                        new UsernamePasswordAuthenticationToken(username, password);
                setDetails(request, authRequest);

                return this.getAuthenticationManager().authenticate(authRequest);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse login JSON body", e);
            }
        }

        return super.attemptAuthentication(request, response);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException, ServletException {

        boolean rememberMe = Boolean.TRUE.equals(request.getAttribute(REMEMBER_ME_ATTR));
        RememberMeServices remember = getRememberMeServices();

        // 1) UserDetails → AuthSessionUser 변환
        Object principal = authResult.getPrincipal();
        AuthSessionUser sessionUser;
        if (principal instanceof CustomUserDetails cud) {
            sessionUser = new AuthSessionUser(cud.getId(), cud.getUsername(), cud.getRole().name());
        } else {
            sessionUser = new AuthSessionUser(null, authResult.getName(), "ROLE_CUSTOMER");
        }

        // 2) safe Authentication (세션에 최소 정보만 넣음)
        Authentication safeAuth = new UsernamePasswordAuthenticationToken(
                sessionUser,
                null,
                authResult.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(safeAuth);
        SecurityContextHolder.setContext(context);

        // 3) 세션 저장
        new HttpSessionSecurityContextRepository().saveContext(context, request, response);

        // 4) remember-me 처리
        if (remember != null && rememberMe) {
            remember.loginSuccess(request, response, authResult);
        }

        // 5) SuccessHandler 호출
        getSuccessHandler().onAuthenticationSuccess(request, response, safeAuth);
    }
}
