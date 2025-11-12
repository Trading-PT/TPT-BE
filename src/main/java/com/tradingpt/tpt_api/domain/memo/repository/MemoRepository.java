package com.tradingpt.tpt_api.domain.memo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tradingpt.tpt_api.domain.memo.entity.Memo;

public interface MemoRepository extends JpaRepository<Memo, Long> {

    /**
     * 고객 ID로 메모 조회
     * @param customerId 고객 ID
     * @return 메모 Optional
     */
    Optional<Memo> findByCustomer_Id(Long customerId);

    /**
     * 고객이 메모를 가지고 있는지 확인
     * @param customerId 고객 ID
     * @return 메모 존재 여부
     */
    boolean existsByCustomer_Id(Long customerId);
}
