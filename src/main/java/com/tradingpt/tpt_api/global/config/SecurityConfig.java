package com.tradingpt.tpt_api.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradingpt.tpt_api.auth.filter.JsonUsernamePasswordAuthFilter;
import com.tradingpt.tpt_api.auth.handler.CustomFailureHandler;
import com.tradingpt.tpt_api.auth.handler.CustomSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ObjectMapper objectMapper;
    private final CustomSuccessHandler customSuccessHandler;
    private final CustomFailureHandler customFailureHandler;
    private final RememberMeServices rememberMeServices;

    //세션 동시세션 관련 Bean
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public static HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    //JSON 로그인 필터
    @Bean
    public JsonUsernamePasswordAuthFilter jsonUsernamePasswordAuthFilter(AuthenticationManager authManager) {
        JsonUsernamePasswordAuthFilter filter = new JsonUsernamePasswordAuthFilter(objectMapper);
        filter.setFilterProcessesUrl("/api/auth/login");
        filter.setAuthenticationManager(authManager);
        filter.setAuthenticationSuccessHandler(customSuccessHandler);
        filter.setAuthenticationFailureHandler(customFailureHandler);
        filter.setRememberMeServices(rememberMeServices); // remember-me 연동
        return filter;
    }

    //
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider provider,
            AuthenticationManager authManager,
            SessionRegistry sessionRegistry,
            JsonUsernamePasswordAuthFilter jsonLoginFilter) throws Exception {

        http
                .authenticationProvider(provider)
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/**", "/oauth2/**"))
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(sf -> sf.migrateSession())
                        .sessionConcurrency(sc -> sc
                                .maximumSessions(3)               // 동일 계정 동시 세션 3개
                                .maxSessionsPreventsLogin(false)   // 4번째 로그인 시 가장 오래된 세션 만료
                                .sessionRegistry(sessionRegistry)
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**", "/oauth2/**",
                                "/swagger-ui.html", "/swagger-ui/**",
                                "/swagger-resources/**", "/webjars/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .rememberMe(rm -> rm
                        .rememberMeServices(rememberMeServices) // 체인 등록(쿠키/검증)
                )
//                .logout(lo -> lo
//                        .logoutUrl("/api/auth/logout")
//                        .addLogoutHandler((req, res, auth) -> rememberMeServices.logout(req, res, auth))
//                        .deleteCookies("JSESSIONID", "remember-me")
//                        .logoutSuccessHandler((req, res, auth) -> res.setStatus(200))
//                )
                .addFilterAt(jsonLoginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
