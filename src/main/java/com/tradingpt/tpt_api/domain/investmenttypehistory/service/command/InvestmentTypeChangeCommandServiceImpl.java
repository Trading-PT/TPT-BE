package com.tradingpt.tpt_api.domain.investmenttypehistory.service.command;

import java.time.LocalDate;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.request.ApproveChangeRequestDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.request.CreateChangeRequestDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.dto.response.ChangeRequestResponseDTO;
import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeChangeRequest;
import com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeHistory;
import com.tradingpt.tpt_api.domain.investmenttypehistory.exception.InvestmentHistoryErrorStatus;
import com.tradingpt.tpt_api.domain.investmenttypehistory.exception.InvestmentHistoryException;
import com.tradingpt.tpt_api.domain.investmenttypehistory.repository.InvestmentTypeChangeRequestRepository;
import com.tradingpt.tpt_api.domain.investmenttypehistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class InvestmentTypeChangeCommandServiceImpl implements InvestmentTypeChangeCommandService {

	private final UserRepository userRepository;
	private final InvestmentTypeHistoryRepository investmentTypeHistoryRepository;
	private final InvestmentTypeChangeRequestRepository investmentTypeChangeRequestRepository;

	@Override
	public ChangeRequestResponseDTO createChangeRequest(Long customerId, CreateChangeRequestDTO request) {
		log.info("Creating investment type change request for customerId={}", customerId);

		// 1. 고객 조회
		Customer customer = (Customer)userRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 2. 현재 투자 유형 조회
		InvestmentType currentType = customer.getPrimaryInvestmentType();

		// 3. 동일한 타입으로 변경 시도 체크
		if (currentType == request.getRequestedType()) {
			throw new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.SAME_INVESTMENT_TYPE);
		}

		// 4. 이미 대기 중인 신청이 있는지 확인
		if (investmentTypeChangeRequestRepository.existsPendingRequestByCustomerId(customerId)) {
			throw new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.PENDING_REQUEST_EXISTS);
		}

		// 5. 변경 예정일 계산 (다음 달 1일)
		LocalDate targetChangeDate = calculateNextMonthFirstDay();

		// 6. 신청 생성
		InvestmentTypeChangeRequest changeRequest = InvestmentTypeChangeRequest.createRequest(
			customer,
			currentType,
			request.getRequestedType(),
			request.getReason(),
			targetChangeDate
		);

		InvestmentTypeChangeRequest saved = investmentTypeChangeRequestRepository.save(changeRequest);

		log.info("Change request created: id={}, customerId={}, {} -> {}",
			saved.getId(), customerId, currentType, request.getRequestedType());

		return ChangeRequestResponseDTO.from(saved);
	}

	@Override
	public ChangeRequestResponseDTO processChangeRequest(
		Long requestId,
		Long trainerId,
		ApproveChangeRequestDTO request
	) {
		log.info("Processing change request: requestId={}, trainerId={}, approved={}",
			requestId, trainerId, request.getApproved());

		if (LocalDate.now().getDayOfMonth() != 1) {
			throw new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.INVESTMENT_HISTORY_TYPE_CHANGE_CAN_BE_PROCEEDED_AT_FIRST_DATE);
		}

		// 1. 신청 조회
		InvestmentTypeChangeRequest changeRequest = investmentTypeChangeRequestRepository.findById(requestId)
			.orElseThrow(() -> new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.CHANGE_REQUEST_NOT_FOUND));

		// 2. 처리 가능 여부 확인
		if (!changeRequest.canBeProcessed()) {
			throw new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.REQUEST_ALREADY_PROCESSED);
		}

		// 3. 트레이너 조회
		Trainer trainer = (Trainer)userRepository.findById(trainerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.TRAINER_NOT_FOUND));

		// 4. 승인/거부 처리
		if (Boolean.TRUE.equals(request.getApproved())) {
			// 승인
			changeRequest.approve(trainer);

			// 기존 진행 중인 InvestmentTypeHistory 종료
			closeCurrentInvestmentHistory(changeRequest.getCustomer(), changeRequest.getTargetChangeDate());

			// 새로운 InvestmentTypeHistory 생성
			createNewInvestmentTypeHistory(changeRequest);

			// Customer의 primaryInvestmentType 업데이트
			updateCustomerInvestmentType(changeRequest);

			log.info("Change request approved: requestId={}", requestId);
		} else {
			// 거부
			changeRequest.reject(trainer, request.getRejectionReason());
			log.info("Change request rejected: requestId={}", requestId);
		}

		return ChangeRequestResponseDTO.from(changeRequest);
	}

	@Override
	public Void cancelChangeRequest(Long customerId, Long requestId) {
		log.info("Cancelling change request: requestId={}, customerId={}", requestId, customerId);

		InvestmentTypeChangeRequest request = investmentTypeChangeRequestRepository.findById(requestId)
			.orElseThrow(() -> new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.CHANGE_REQUEST_NOT_FOUND));

		// 본인 확인
		if (!request.getCustomer().getId().equals(customerId)) {
			throw new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.UNAUTHORIZED_ACCESS);
		}

		// 취소 가능 여부 확인
		if (!request.canBeProcessed()) {
			throw new InvestmentHistoryException(
				InvestmentHistoryErrorStatus.REQUEST_ALREADY_PROCESSED);
		}

		request.cancel();
		log.info("Change request cancelled: requestId={}, customerId={}", requestId, customerId);

		return null;
	}

	/**
	 * =============================
	 * PRIVATE HELPER METHODS
	 * =============================
	 */

	/**
	 * 기존 진행 중인 InvestmentTypeHistory 종료
	 */
	private void closeCurrentInvestmentHistory(Customer customer, LocalDate endDate) {
		// 변경 예정일 전날로 종료
		LocalDate closeDate = endDate.minusDays(1);

		investmentTypeHistoryRepository.findActiveHistoryForMonth(
			customer.getId(),
			closeDate.getYear(),
			closeDate.getMonthValue()
		).ifPresent(history -> {
			history.closeAt(closeDate);
			log.info("Closed existing InvestmentTypeHistory: id={}, customerId={}, endDate={}",
				history.getId(), customer.getId(), closeDate);
		});
	}

	/**
	 * 새로운 InvestmentTypeHistory 생성
	 */
	private void createNewInvestmentTypeHistory(InvestmentTypeChangeRequest changeRequest) {
		InvestmentTypeHistory newHistory = InvestmentTypeHistory.createFrom(
			changeRequest.getCustomer(),
			changeRequest.getRequestedType(),
			changeRequest.getTargetChangeDate()
		);

		investmentTypeHistoryRepository.save(newHistory);

		log.info("New InvestmentTypeHistory created: customerId={}, type={}, startDate={}",
			changeRequest.getCustomer().getId(),
			changeRequest.getRequestedType(),
			changeRequest.getTargetChangeDate());
	}

	/**
	 * Customer의 primaryInvestmentType 업데이트
	 */
	private void updateCustomerInvestmentType(InvestmentTypeChangeRequest changeRequest) {
		Customer customer = changeRequest.getCustomer();
		customer.updatePrimaryInvestmentType(changeRequest.getRequestedType());

		log.info("Customer primaryInvestmentType updated: customerId={}, newType={}",
			customer.getId(), changeRequest.getRequestedType());
	}

	/**
	 * 다음 달 1일 계산
	 */
	private LocalDate calculateNextMonthFirstDay() {
		YearMonth nextMonth = YearMonth.now().plusMonths(1);
		return nextMonth.atDay(1);
	}
}