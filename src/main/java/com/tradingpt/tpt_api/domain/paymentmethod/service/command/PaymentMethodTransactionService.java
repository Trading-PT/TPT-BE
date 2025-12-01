package com.tradingpt.tpt_api.domain.paymentmethod.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.paymentmethod.entity.PaymentMethod;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodErrorStatus;
import com.tradingpt.tpt_api.domain.paymentmethod.exception.PaymentMethodException;
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

	/**
	 * PG사 응답 코드 저장 (별도 트랜잭션)
	 * 메인 트랜잭션이 롤백되어도 PG사 에러 정보가 보존되도록 REQUIRES_NEW 사용
	 * NicePay API 실패 시 디버깅 및 사용자 안내를 위해 에러 정보를 DB에 저장
	 *
	 * @param paymentMethodId 결제수단 ID
	 * @param resultCode      결과 코드 (예: "통신 실패", "3001")
	 * @param resultMessage   결과 메시지 (예: NicePay 에러 메시지)
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updatePgResponseInNewTransaction(Long paymentMethodId, String resultCode, String resultMessage) {
		log.info("PG 응답 저장 (별도 트랜잭션): paymentMethodId={}, resultCode={}", paymentMethodId, resultCode);

		PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
			.orElseThrow(() -> new PaymentMethodException(PaymentMethodErrorStatus.PAYMENT_METHOD_NOT_FOUND));

		// Entity의 비즈니스 메서드를 통한 상태 변경 (JPA dirty checking)
		paymentMethod.setPgResponseCode(resultCode, resultMessage);

		// JPA dirty checking으로 자동 UPDATE - save() 불필요
		log.info("PG 응답 저장 완료: paymentMethodId={}", paymentMethodId);
	}
}
