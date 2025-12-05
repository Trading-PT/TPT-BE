package com.tradingpt.tpt_api.domain.paymentmethod.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.user.entity.Customer;

/**
 * PaymentMethod 리포지토리
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

	/**
	 * 고객의 활성 결제수단 목록 조회 (삭제되지 않은 것만)
	 *
	 * @param customer 고객
	 * @return 활성 결제수단 목록
	 */
	List<PaymentMethod> findByCustomerAndIsDeletedFalseOrderByIsPrimaryDescCreatedAtDesc(Customer customer);

	/**
	 * 고객의 주 결제수단 조회
	 *
	 * @param customer 고객
	 * @return 주 결제수단 (없으면 empty)
	 */
	Optional<PaymentMethod> findByCustomerAndIsPrimaryTrueAndIsDeletedFalse(Customer customer);

	/**
	 * 빌링키로 결제수단 조회 (삭제되지 않은 것만)
	 *
	 * @param billingKey 빌링키
	 * @return 결제수단 (없으면 empty)
	 */
	Optional<PaymentMethod> findByBillingKeyAndIsDeletedFalse(String billingKey);

	/**
	 * 고객의 특정 결제수단 조회 (삭제되지 않은 것만)
	 *
	 * @param id 결제수단 ID
	 * @param customer 고객
	 * @return 결제수단 (없으면 empty)
	 */
	Optional<PaymentMethod> findByIdAndCustomerAndIsDeletedFalse(Long id, Customer customer);

	/**
	 * 고객의 활성 결제수단 개수 조회
	 *
	 * @param customerId 고객
	 * @return 결제수단 개수
	 */
	long countByCustomer_IdAndIsDeletedFalse(Long customerId);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
        DELETE FROM PaymentMethod pm
        WHERE pm.customer.id = :customerId
        """)
	void deleteByCustomerId(@Param("customerId") Long customerId);

}
