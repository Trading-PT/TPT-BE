package com.tradingpt.tpt_api.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

public class CustomRememberMeService extends PersistentTokenBasedRememberMeServices {
    public static final String REMEMBER_ME_ATTR = "REMEMBER_ME_JSON";
    public CustomRememberMeService(String key, UserDetailsService uds, PersistentTokenRepository repo) {
        super(key, uds, repo);
    }
    @Override
    protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
        Object attr = request.getAttribute(REMEMBER_ME_ATTR);
        if (attr instanceof Boolean && (Boolean) attr) return true;
        if (attr != null && "true".equalsIgnoreCase(String.valueOf(attr))) return true;
        return super.rememberMeRequested(request, parameter); // 폼 파라미터도 허용
    }
}

