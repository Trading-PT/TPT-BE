package com.tradingpt.tpt_api.domain.user.service;

import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void ensureUnique(String u, String e, String p) { /* 중복 검사 */ }

}
