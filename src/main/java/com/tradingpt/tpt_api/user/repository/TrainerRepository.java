package com.tradingpt.tpt_api.user.repository;

import com.tradingpt.tpt_api.user.entity.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer,Long> {
}
