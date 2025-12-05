package com.tradingpt.tpt_api.domain.auth.security;


import com.tradingpt.tpt_api.domain.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.Role;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.global.exception.AuthException;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository; // 필요 없으면 지워도 됨

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1) User 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(UserErrorStatus.USER_NOT_FOUND));

        // 2) Soft Delete된 유저면 로그인 막기
        if (user.isDeleted()) {

            throw new AuthException(UserErrorStatus.DELETED_USER); // 이런 코드가 있다면
        }

        // 3) 정상 유저면 UserDetails 리턴
        return CustomUserDetails.from(user);
    }
}

