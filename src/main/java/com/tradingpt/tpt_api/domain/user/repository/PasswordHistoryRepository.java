package com.tradingpt.tpt_api.domain.user.repository;

import com.tradingpt.tpt_api.domain.user.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long>, PasswordHistoryRepositoryCustom{

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM PasswordHistory ph
        WHERE ph.user.id = :userId
        """)
    void deleteByCustomerId(@Param("userId") Long userId);
}
