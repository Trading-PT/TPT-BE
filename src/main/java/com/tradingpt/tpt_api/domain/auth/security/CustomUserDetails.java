package com.tradingpt.tpt_api.domain.auth.security;

import com.tradingpt.tpt_api.domain.user.user.entity.User;
import com.tradingpt.tpt_api.domain.user.user.enums.Role;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String username;
    private final String password;              // DaoAuthenticationProvider가 비교할 해시
    private final Role role;
    private final String name;
    private final String email;

    private final List<GrantedAuthority> authorities;

    private CustomUserDetails(Long id,
                              String username,
                              String password,
                              Role role,
                              String name,
                              String email) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.email = email;
        this.authorities = List.of(new SimpleGrantedAuthority(role.name())); // e.g. ROLE_CUSTOMER
    }

    public static CustomUserDetails from(User u) {
        return new CustomUserDetails(
                u.getId(),
                u.getUsername(),
                u.getPassword(),   // 반드시 인코딩된 값이어야 함
                u.getRole(),
                u.getName(),
                u.getEmail()
        );
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }  //추후 구현
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}