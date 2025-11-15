package com.tradingpt.tpt_api.domain.payment.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tradingpt.tpt_api.domain.payment.entity.Payment;
import com.tradingpt.tpt_api.domain.payment.enums.PaymentStatus;
import com.tradingpt.tpt_api.domain.payment.enums.PaymentType;

/**
 * 결제 Repository
 * 결제 정보 데이터베이스 접근 인터페이스
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 주문번호로 결제 조회
     */
    Optional<Payment> findByOrderId(String orderId);

    /**
     * PG 거래 고유번호로 결제 조회
     */
    Optional<Payment> findByPgTid(String pgTid);

    /**
     * 구독 ID로 결제 내역 조회 (최신순)
     */
    @Query("SELECT p FROM Payment p WHERE p.subscription.id = :subscriptionId ORDER BY p.requestedAt DESC")
    List<Payment> findAllBySubscriptionIdOrderByRequestedAtDesc(@Param("subscriptionId") Long subscriptionId);

    /**
     * 구독 ID와 결제 상태로 결제 조회
     */
    List<Payment> findBySubscription_IdAndStatus(Long subscriptionId, PaymentStatus status);

    /**
     * 고객 ID로 결제 내역 조회 (최신순)
     */
    @Query("SELECT p FROM Payment p WHERE p.customer.id = :customerId ORDER BY p.requestedAt DESC")
    List<Payment> findAllByCustomerIdOrderByRequestedAtDesc(@Param("customerId") Long customerId);

    /**
     * 특정 기간 동안의 정기 결제 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.paymentType = :paymentType " +
           "AND p.billingPeriodStart >= :startDate AND p.billingPeriodEnd <= :endDate")
    List<Payment> findRecurringPaymentsByPeriod(
        @Param("paymentType") PaymentType paymentType,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * 구독별 마지막 성공 결제 조회
     */
    @Query("SELECT p FROM Payment p WHERE p.subscription.id = :subscriptionId " +
           "AND p.status = 'SUCCESS' ORDER BY p.paidAt DESC LIMIT 1")
    Optional<Payment> findLastSuccessPaymentBySubscriptionId(@Param("subscriptionId") Long subscriptionId);
}
