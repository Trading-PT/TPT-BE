package com.tradingpt.tpt_api.domain.paymentmethod.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.paymentmethod.repository.PaymentMethodRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제수단 트랜잭션 분리 서비스
 * REQUIRES_NEW가 필요한 메서드를 별도 클래스로 분리하여
 * Spring AOP 프록시가 정상 동작하도록 함
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMethodTransactionService {

	private final PaymentMethodRepository paymentMethodRepository;

	/**
	 * 결제수단 저장 (별도 트랜잭션)
	 * 첫 결제 실패 시에도 결제수단이 롤백되지 않도록 REQUIRES_NEW 사용
	 *
	 * @param paymentMethod 저장할 결제수단
	 * @return 저장된 결제수단
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public PaymentMethod savePaymentMethod(PaymentMethod paymentMethod) {
		log.info("결제수단 저장 (별도 트랜잭션): customerId={}", paymentMethod.getCustomer().getId());
		PaymentMethod saved = paymentMethodRepository.save(paymentMethod);
		log.info("결제수단 저장 완료: paymentMethodId={}", saved.getId());
		return saved;
	}
}
