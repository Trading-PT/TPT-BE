package com.tradingpt.tpt_api.auth.service;


import com.tradingpt.tpt_api.auth.exception.code.AuthErrorStatus;
import com.tradingpt.tpt_api.auth.security.CustomUserDetails;
import com.tradingpt.tpt_api.global.exception.AuthException;
import com.tradingpt.tpt_api.user.entity.User;
import com.tradingpt.tpt_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // findByUsername 필요

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(AuthErrorStatus.USER_NOT_FOUND));
        return CustomUserDetails.from(user); // id/username/password/role 포함
    }
}
