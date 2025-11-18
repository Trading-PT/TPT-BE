package com.tradingpt.tpt_api.domain.user.repository;

import com.tradingpt.tpt_api.domain.user.entity.Uid;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UidRepository extends JpaRepository<Uid, Long> {
    Optional<Uid> findByCustomerId(Long customerId);
}
