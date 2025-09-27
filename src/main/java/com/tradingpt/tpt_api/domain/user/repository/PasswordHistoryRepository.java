package com.tradingpt.tpt_api.domain.user.repository;

import com.tradingpt.tpt_api.domain.user.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long>, PasswordHistoryRepositoryCustom{
}
