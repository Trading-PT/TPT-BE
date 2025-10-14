package com.tradingpt.tpt_api.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.user.entity.Trainer;

public interface TrainerRepository extends JpaRepository<Trainer, Long>, TrainerRepositoryCustom {
}
