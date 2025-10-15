package com.tradingpt.tpt_api.domain.user.service.query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.MonthlyTradingSummary;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.repository.MonthlyTradingSummaryRepository;
import com.tradingpt.tpt_api.domain.user.dto.response.CustomerEvaluationResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.repository.WeeklyTradingSummaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerQueryServiceImpl implements TrainerQueryService {

	private final CustomerRepository customerRepository;
	private final MonthlyTradingSummaryRepository monthlyTradingSummaryRepository;
	private final WeeklyTradingSummaryRepository weeklyTradingSummaryRepository;

	@Override
	public Page<CustomerEvaluationResponseDTO> getManagedCustomersEvaluations(Pageable pageable, Long trainerId) {
		Page<Customer> customerPage = customerRepository.findByAssignedTrainer_Id(trainerId, pageable);

		return customerPage.map(customer -> {
			MonthlyTradingSummary latestMonthly = monthlyTradingSummaryRepository
				.findTopByTrainer_IdAndCustomer_IdOrderByPeriodYearDescPeriodMonthDesc(trainerId, customer.getId())
				.orElse(null);

			WeeklyTradingSummary latestWeekly = weeklyTradingSummaryRepository
				.findTopByTrainer_IdAndCustomer_IdOrderByPeriodYearDescPeriodMonthDescPeriodWeekDesc(trainerId,
					customer.getId())
				.orElse(null);

			return CustomerEvaluationResponseDTO.of(customer, latestMonthly, latestWeekly);
		});
	}
}
