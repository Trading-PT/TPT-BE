package com.tradingpt.tpt_api.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.domain.auth.dto.response.AdminLoginResponse;
import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;
import com.tradingpt.tpt_api.domain.auth.security.CustomUserDetails;
import com.tradingpt.tpt_api.global.security.csrf.HeaderAndCookieCsrfTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.security.web.csrf.CsrfToken;

@Component("adminSuccessHandler")
@RequiredArgsConstructor
public class AdminSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req,
                                        HttpServletResponse res,
                                        Authentication auth) throws IOException {

        req.getSession(true);

        // (1) 세션 저장
        var cud = (CustomUserDetails) auth.getPrincipal();
        var sessionUser = new AuthSessionUser(cud.getId(), cud.getUsername(), cud.getRole().name());
        var auth2 = new UsernamePasswordAuthenticationToken(sessionUser, null, auth.getAuthorities());
        auth2.setDetails(auth.getDetails());

        var ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth2);
        new HttpSessionSecurityContextRepository().saveContext(ctx, req, res);



        // (3) 응답 바디 작성
        var body = new AdminLoginResponse(
                cud.getId(),
                cud.getUsername(),
                cud.getRole().name(),
                cud.getName(),
                cud.getEmail()
        );

        res.setStatus(HttpServletResponse.SC_OK);
        res.setContentType("application/json;charset=UTF-8");
        res.setHeader("Cache-Control", "no-store");
        objectMapper.writeValue(res.getOutputStream(), body);
    }
}
