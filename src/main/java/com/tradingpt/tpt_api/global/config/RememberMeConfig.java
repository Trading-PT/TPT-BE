package com.tradingpt.tpt_api.global.config;


import com.tradingpt.tpt_api.domain.auth.service.CustomRememberMeServices;
import com.tradingpt.tpt_api.domain.auth.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class RememberMeConfig {

    private final CustomUserDetailsService userDetailsService;
    private final DataSource dataSource;

    @Value("${security.rememberme.key}")
    private String rememberKey;

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl repo = new JdbcTokenRepositoryImpl();
        repo.setDataSource(dataSource);
//        repo.setCreateTableOnStartup(true); // 최초 1회만 사용
        return repo;
    }

    @Bean
    public RememberMeServices rememberMeServices(PersistentTokenRepository repo) {
        CustomRememberMeServices services =
                new CustomRememberMeServices(rememberKey, userDetailsService, repo);
        services.setTokenValiditySeconds(60 * 60 * 24 * 14); // 14일
        services.setAlwaysRemember(false);                    // JSON/파라미터로 on/off
        services.setCookieName("remember-me");
        // services.setUseSecureCookie(true);                 // 운영 HTTPS에서만
        return services;
    }
}
