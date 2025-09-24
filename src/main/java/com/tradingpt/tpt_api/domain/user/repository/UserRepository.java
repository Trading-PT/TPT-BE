package com.tradingpt.tpt_api.domain.user.repository;

import com.fasterxml.jackson.datatype.jsr310.ser.YearSerializer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    Optional<User> findByUsername(String userName);

    @Query("select u.username from User u where u.email = :email")
    Optional<String> findUsernameByEmail(@Param("email") String email);

    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

    Boolean existsByEmail(String email);
}
