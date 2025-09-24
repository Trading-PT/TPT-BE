package com.tradingpt.tpt_api.domain.user.service;

import com.tradingpt.tpt_api.domain.auth.dto.response.FindIdResponse;
import com.tradingpt.tpt_api.domain.auth.dto.response.MeResponse;
import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.exception.AuthException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    public void ensureUnique(String u, String e, String p) { /* 중복 검사 */ }

    @Override
    public FindIdResponse findUserId(String email){
        String userName = userRepository.findUsernameByEmail(email)
                .orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

        return FindIdResponse.builder()
                .userName(userName)
                .build();

    }

    @Override
    public MeResponse getMe(Long userId) {
        Customer c = customerRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.USER_NOT_FOUND));

        boolean isPremium = MembershipLevel.PREMIUM.equals(c.getMembershipLevel())
                && c.getMembershipExpiredAt() != null
                && c.getMembershipExpiredAt().isAfter(LocalDateTime.now());

        String investmentType = (c.getPrimaryInvestmentType() != null)
                ? c.getPrimaryInvestmentType().name()
                : null;

        return MeResponse.builder()
                .name(c.getName())                                   // 부모(User)에서 상속
                .username(c.getUsername())                           // 아이디 = username
                .investmentType(investmentType)                      // 투자유형
                .isCourseCompleted(Boolean.TRUE.equals(c.getIsCourseCompleted())) // 완강 여부
                .isPremium(isPremium)                                // 프리미엄 여부
                .build();
    }


}
