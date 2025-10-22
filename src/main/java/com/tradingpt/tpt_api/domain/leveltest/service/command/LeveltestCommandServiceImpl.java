package com.tradingpt.tpt_api.domain.leveltest.service.command;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.leveltest.dto.request.LeveltestSubmitRequestDTO;
import com.tradingpt.tpt_api.domain.leveltest.dto.response.LeveltestAttemptSubmitResponseDTO;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.entity.LeveltestResponse;
import com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestStaus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestErrorStatus;
import com.tradingpt.tpt_api.domain.leveltest.exception.LeveltestException;
import com.tradingpt.tpt_api.domain.leveltest.repository.LevelTestQuestionRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestAttemptRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestResponseRepository;
import com.tradingpt.tpt_api.domain.leveltest.service.async.GradingAsyncInvoker;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class LeveltestCommandServiceImpl implements LeveltestCommandService {

	private final CustomerRepository customerRepository;
	private final LeveltestAttemptRepository attemptRepository;
	private final LevelTestQuestionRepository questionRepository;
	private final LeveltestResponseRepository responseRepository;
	private final GradingAsyncInvoker gradingAsyncInvoker;

	@Override
	public LeveltestAttemptSubmitResponseDTO submitAttempt(Long customerId, LeveltestSubmitRequestDTO request) {

		Customer customer = customerRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 1) 기존 시도 여부 확인
		boolean hasPreviousAttempt = attemptRepository.existsByCustomer_Id(customerId);

		// 2) 조건 검사
		if (hasPreviousAttempt && customer.getCourseStatus() != CourseStatus.AFTER_COMPLETION) {
			throw new LeveltestException(LeveltestErrorStatus.ATTEMPT_NOT_ALLOWED);
		}

		// 3) 시도 생성 (SUBMITTED)
		LevelTestAttempt attempt = LevelTestAttempt.builder()
			.customer(customer)
			.status(LeveltestStaus.SUBMITTED)
			.totalScore(0)
			.build();
		attemptRepository.save(attempt);

		// 4) 응답 벌크 저장
		List<LeveltestResponse> responses = request.getAnswers().stream()
			.map(a -> LeveltestResponse.builder()
				.leveltestAttempt(attempt)
				.leveltestQuestion(
					questionRepository.findById(a.getQuestionId())
						.orElseThrow(() -> new LeveltestException(LeveltestErrorStatus.QUESTION_NOT_FOUND))
				)
				.choiceNumber(a.getChoiceNumber())
				.answerText(a.getAnswerText())
				.scoredAwarded(null)
				.build())
			.toList();
		responseRepository.saveAll(responses);

		// 5) 비동기 채점
		gradingAsyncInvoker.trigger(attempt.getId());

		// 6) 사용자 상태 갱신
		customer.setUserStatus(UserStatus.PAID_AFTER_TEST_TRAINER_ASSIGNING);

		// 7) 응답 반환
		return LeveltestAttemptSubmitResponseDTO.builder()
			.attemptId(attempt.getId())
			.build();
	}

}
