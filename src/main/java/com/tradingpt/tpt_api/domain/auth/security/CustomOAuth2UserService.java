package com.tradingpt.tpt_api.domain.auth.security;

import com.tradingpt.tpt_api.domain.auth.dto.response.KakaoResponse;
import com.tradingpt.tpt_api.domain.auth.dto.response.NaverResponse;
import com.tradingpt.tpt_api.domain.auth.dto.response.OAuth2Response;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.Provider;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest req) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(req);
        var attributes = oAuth2User.getAttributes();

        String providerName = req.getClientRegistration().getRegistrationId(); // kakao/naver
        OAuth2Response rsp = switch (providerName) {
            case "kakao" -> new KakaoResponse(attributes);
            case "naver" -> new NaverResponse(attributes);
            default -> throw new OAuth2AuthenticationException("UNSUPPORTED_PROVIDER");
        };

        Provider provider = Provider.valueOf(rsp.getProvider());

        // 1) 이미 연동돼 있으면 그대로 통과
        Optional<User> existing = userRepository.findByProviderAndProviderId(provider, rsp.getProviderId());
        if (existing.isPresent()) {
            User u = existing.get();
            return new CustomOAuth2User(u.getId(), u.getUsername(), u.getRole().name(), attributes);
        }

        // 2) ★ 첫 소셜 로그인일 때만 이메일 검증
        final String email = rsp.getEmail();
        if (email == null || email.isBlank()) {
            // 스코프 동의 안 하거나 제공 안 된 케이스
            throw new OAuth2AuthenticationException("SOCIAL_EMAIL_MISSING");
        }
        if (userRepository.existsByEmail(email)) {
            // "이메일 1개 = 유저 1개" 정책 위반
            throw new OAuth2AuthenticationException("EMAIL_ALREADY_REGISTERED");
        }

        // 3) 통과 시에만 생성 (기존 로직 유지)
        User user = userRepository.save(
                Customer.builder()
                        .username(buildUsername(rsp))
                        .email(email)
                        .name(rsp.getName())
                        .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .provider(provider)
                        .providerId(rsp.getProviderId())
                        .build()
        );

        return new CustomOAuth2User(user.getId(), user.getUsername(), user.getRole().name(), attributes);
    }


    private String buildUsername(OAuth2Response rsp) {
        return rsp.getProvider() + "_" + rsp.getProviderId();
    }
}
