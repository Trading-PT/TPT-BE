package com.tradingpt.tpt_api.domain.auth.security;

import com.tradingpt.tpt_api.domain.auth.dto.response.KakaoResponse;
import com.tradingpt.tpt_api.domain.auth.dto.response.NaverResponse;
import com.tradingpt.tpt_api.domain.auth.dto.response.OAuth2Response;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.Provider;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
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
            default -> throw new OAuth2AuthenticationException("지원하지 않는 로그인 방식입니다.");
        };

        Provider provider = Provider.valueOf(rsp.getProvider());

        // 찾고, 없으면 디폴트로 Customer 생성 (필요시 Trainer로 분기)
        User user = userRepository.findByProviderAndProviderId(provider, rsp.getProviderId())
                .orElseGet(() -> userRepository.save(
                        Customer.builder() // 또는 상황에 따라 Trainer.builder()
                                .username(buildUsername(rsp))
                                .email(rsp.getEmail())
                                .name(rsp.getName())
                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                .provider(provider)
                                .providerId(rsp.getProviderId())
                                .build()
                ));

        return new CustomOAuth2User(user.getId(), user.getUsername(), user.getRole().name(), attributes);
    }

    private String buildUsername(OAuth2Response rsp) {
        return rsp.getProvider() + "_" + rsp.getProviderId();
    }
}
