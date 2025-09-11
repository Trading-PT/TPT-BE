package com.tradingpt.tpt_api.domain.user.repository;

import com.tradingpt.tpt_api.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String userName);
}
