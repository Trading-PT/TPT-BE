package com.tradingpt.tpt_api.domain.user.repository;

import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer,Long> {
}
