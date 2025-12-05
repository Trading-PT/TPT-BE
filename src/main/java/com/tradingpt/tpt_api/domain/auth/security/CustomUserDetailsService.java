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

    private final UserRepository userRepository; // findByUsername 필요
    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1) User 먼저 찾고
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException(UserErrorStatus.USER_NOT_FOUND));

        Customer customer = customerRepository.findById(user.getId())
                .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

        if (customer.getDeletedAt() != null) {
            throw new AuthException(UserErrorStatus.DELETED_USER);
        }
        return CustomUserDetails.from(user); // id/username/password/role 포함
    }
}
