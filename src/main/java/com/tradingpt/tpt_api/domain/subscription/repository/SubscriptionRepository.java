package com.tradingpt.tpt_api.domain.subscription.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long>, SubscriptionRepositoryCustom {

    /**
     * 상태별 구독 조회
     */
    List<Subscription> findAllByStatus(Status status);

    /**
     * 고객 ID로 활성 구독 조회
     */
    Optional<Subscription> findByCustomer_IdAndStatus(Long customerId, Status status);

    /**
     * 고객 ID로 모든 구독 조회 (최신순)
     */
    @Query("SELECT s FROM Subscription s WHERE s.customer.id = :customerId ORDER BY s.createdAt DESC")
    List<Subscription> findAllByCustomerIdOrderByCreatedAtDesc(@Param("customerId") Long customerId);

    /**
     * 다음 결제일이 오늘 이전인 활성 구독 조회 (정기 결제 대상)
     * N+1 쿼리 방지를 위해 customer, paymentMethod를 fetch join
     */
    @Query("SELECT s FROM Subscription s " +
           "JOIN FETCH s.customer " +
           "JOIN FETCH s.paymentMethod " +
           "WHERE s.status = 'ACTIVE' " +
           "AND s.nextBillingDate <= :targetDate " +
           "AND s.paymentMethod IS NOT NULL")
    List<Subscription> findSubscriptionsDueForPayment(@Param("targetDate") LocalDate targetDate);

    /**
     * 결제 실패 횟수가 특정 값 이상인 활성 구독 조회
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' " +
           "AND s.paymentFailedCount >= :threshold")
    List<Subscription> findActiveSubscriptionsWithFailureThreshold(@Param("threshold") int threshold);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        DELETE FROM Subscription s
        WHERE s.customer.id = :customerId
        """)
    void deleteByCustomerId(@Param("customerId") Long customerId);
}

