package com.tradingpt.tpt_api.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.enums.Provider;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByUsername(String username);

	Optional<User> findByUsername(String userName);

	Optional<User> findByName(String name);

	List<User> findAllByEmail(String email);

	Optional<User> findByProviderAndProviderId(Provider provider, String providerId);

	Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
