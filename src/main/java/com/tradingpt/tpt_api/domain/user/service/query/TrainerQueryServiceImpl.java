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
			// evaluator_id 조건 제거: 고객의 최신 평가 데이터를 조회
			// - 고객이 직접 작성한 메모(evaluator_id=null)도 포함
			// - 다른 트레이너가 작성한 평가도 포함
			MonthlyTradingSummary latestMonthly = monthlyTradingSummaryRepository
				.findTopByCustomer_IdOrderByPeriodYearDescPeriodMonthDesc(customer.getId())
				.orElse(null);

			WeeklyTradingSummary latestWeekly = weeklyTradingSummaryRepository
				.findTopByCustomer_IdOrderByPeriodYearDescPeriodMonthDescPeriodWeekDesc(customer.getId())
				.orElse(null);

			return CustomerEvaluationResponseDTO.of(customer, latestMonthly, latestWeekly);
		});
	}
}
