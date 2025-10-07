package com.tradingpt.tpt_api.domain.monthlytradingsummary.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.request.CreateMonthlyTradingSummaryRequestDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.repository.MonthlyTradingSummaryRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MonthlyTradingSummaryCommandServiceImpl implements MonthlyTradingSummaryCommandService {

	private final CustomerRepository customerRepository;
	private final MonthlyTradingSummaryRepository monthlyTradingSummaryRepository;

	@Override
	public Void createMonthlySummary(Integer year, Integer month, Long customerId,
		CreateMonthlyTradingSummaryRequestDTO request) {

		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		Trainer trainer = customer.getTrainer();

		// TODO
		//  - 고객의 해당 달의 CourseStatus가 AFTER_COMPLETION 일 경우에만 월별 매매 일지에 최종 평가를 달 수 있도록
		//  - 검증 로직을 추가해야한다.

		MonthlyTradingSummary newMonthlyTradingSummary = MonthlyTradingSummary.createFrom(request, customer, trainer,
			year, month);

		monthlyTradingSummaryRepository.save(newMonthlyTradingSummary);

		return null;
	}
}
