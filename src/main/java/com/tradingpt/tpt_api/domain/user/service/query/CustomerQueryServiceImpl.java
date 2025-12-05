package com.tradingpt.tpt_api.domain.user.service.query;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.consultation.entity.Consultation;
import com.tradingpt.tpt_api.domain.consultation.repository.ConsultationRepository;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestAttemptRepository;
import com.tradingpt.tpt_api.domain.user.dto.response.FreeCustomerResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.FreeCustomerSliceResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.MyCustomerListItemDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.MyCustomerListResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.NewSubscriptionCustomerResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.NewSubscriptionCustomerSliceResponseDTO;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.common.dto.SliceInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerQueryServiceImpl implements CustomerQueryService {

	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;
	private final LeveltestAttemptRepository leveltestAttemptRepository;
	private final ConsultationRepository consultationRepository;

	@Override
	public MyCustomerListResponseDTO getMyCustomers(Long trainerId, Pageable pageable) {
		// 1. 트레이너 존재 여부 확인
		if (!userRepository.existsById(trainerId)) {
			throw new UserException(UserErrorStatus.TRAINER_NOT_FOUND);
		}

		// 2. Slice로 담당 고객 조회
		Slice<Customer> customerSlice = customerRepository
			.findByAssignedTrainerIdOrderByMembershipLevelDescCreatedAtDesc(trainerId, pageable);

		// 3. DTO 변환
		List<MyCustomerListItemDTO> customerDTOs = customerSlice.getContent()
			.stream()
			.map(MyCustomerListItemDTO::from)
			.toList();
		// 4. SliceInfo 생성
		SliceInfo sliceInfo = SliceInfo.of(customerSlice);

		return MyCustomerListResponseDTO.of(customerDTOs, sliceInfo);
	}

	/**
	 * 미구독(무료) 고객 목록 조회
	 *
	 * 비즈니스 로직:
	 * 1. ACTIVE 구독이 없는 BASIC 멤버십 고객 조회
	 * 2. 담당 트레이너가 없는 고객만 조회
	 * 3. Slice 방식 페이징 (무한 스크롤)
	 * 4. 총 인원 수 함께 반환
	 *
	 * @param pageable 페이징 정보
	 * @return 미구독 고객 슬라이스 (총 인원 수 포함)
	 */
	@Override
	public FreeCustomerSliceResponseDTO getFreeCustomers(Pageable pageable) {
		// 1. Repository에서 미구독 고객 조회
		Slice<Customer> customerSlice = customerRepository.findFreeCustomers(pageable);

		// 2. Entity를 DTO로 변환 (Slice 유지)
		Slice<FreeCustomerResponseDTO> dtoSlice = customerSlice.map(FreeCustomerResponseDTO::from);

		// 3. 미구독 고객 총 인원 수 조회
		Long totalCount = customerRepository.countFreeCustomers();

		// 4. 래퍼 DTO 생성 및 반환
		return FreeCustomerSliceResponseDTO.from(dtoSlice, totalCount);
	}

	/**
	 * 신규 구독 고객 목록 조회
	 *
	 * 비즈니스 로직:
	 * 1. Repository에서 신규 구독 고객 조회
	 *    - 24시간 이내 구독 OR 트레이너 미배정 고객
	 * 2. 각 고객별로 레벨테스트 정보 조회
	 * 3. 각 고객별로 상담 여부 확인
	 * 4. DTO로 변환하여 반환
	 *
	 * @param pageable 페이징 정보
	 * @return 신규 구독 고객 슬라이스 (총 인원 수 포함)
	 */
	@Override
	public NewSubscriptionCustomerSliceResponseDTO getNewSubscriptionCustomers(Pageable pageable) {
		// 1. Repository에서 신규 구독 고객 조회
		Slice<Customer> customerSlice = customerRepository.findNewSubscriptionCustomers(pageable);

		// 2. Entity를 DTO로 변환 (Slice 유지)
		Slice<NewSubscriptionCustomerResponseDTO> dtoSlice = customerSlice
			.map(this::toNewSubscriptionCustomerResponseDTO);

		// 3. 신규 구독 고객 총 인원 수 조회
		Long totalCount = customerRepository.countNewSubscriptionCustomers();

		// 4. 래퍼 DTO 생성 및 반환
		return NewSubscriptionCustomerSliceResponseDTO.from(dtoSlice, totalCount);
	}

	/**
	 * Customer 엔티티를 NewSubscriptionCustomerResponseDTO로 변환
	 *
	 * @param customer Customer 엔티티
	 * @return NewSubscriptionCustomerResponseDTO
	 */
	private NewSubscriptionCustomerResponseDTO toNewSubscriptionCustomerResponseDTO(Customer customer) {
		// 1. 레벨테스트 정보 조회 (최신순으로 GRADED > GRADING > SUBMITTED 우선)
		boolean hasAttemptedLevelTest = leveltestAttemptRepository.existsByCustomer_Id(customer.getId());
		NewSubscriptionCustomerResponseDTO.LevelTestInfo levelTestInfo = null;

		if (hasAttemptedLevelTest) {
			// GRADED 상태 조회 시도
			List<LevelTestAttempt> gradedAttempts = leveltestAttemptRepository
				.findByCustomer_IdAndStatus(customer.getId(), LevelTestStaus.GRADED);

			if (!gradedAttempts.isEmpty()) {
				LevelTestAttempt latestGraded = gradedAttempts.get(0);  // 최신 것
				levelTestInfo = NewSubscriptionCustomerResponseDTO.LevelTestInfo.builder()
					.status(latestGraded.getStatus().name())
					.grade(latestGraded.getGrade() != null ? latestGraded.getGrade().name() : null)
					.gradingTrainerName(latestGraded.getTrainer() != null
						? latestGraded.getTrainer().getName()
						: null)  // NPE 방지
					.build();
			} else {
				// GRADING 또는 SUBMITTED 상태 조회
				List<LevelTestAttempt> gradingAttempts = leveltestAttemptRepository
					.findByCustomer_IdAndStatus(customer.getId(), LevelTestStaus.GRADING);

				if (!gradingAttempts.isEmpty()) {
					LevelTestAttempt latestGrading = gradingAttempts.get(0);
					levelTestInfo = NewSubscriptionCustomerResponseDTO.LevelTestInfo.builder()
						.status(latestGrading.getStatus().name())
						.grade(latestGrading.getGrade() != null ? latestGrading.getGrade().name() : null)
						.gradingTrainerName(latestGrading.getTrainer() != null
							? latestGrading.getTrainer().getName()
							: null)  // NPE 방지
						.build();
				} else {
					// SUBMITTED 상태 조회
					List<LevelTestAttempt> submittedAttempts = leveltestAttemptRepository
						.findByCustomer_IdAndStatus(customer.getId(), LevelTestStaus.SUBMITTED);

					if (!submittedAttempts.isEmpty()) {
						LevelTestAttempt latestSubmitted = submittedAttempts.get(0);
						levelTestInfo = NewSubscriptionCustomerResponseDTO.LevelTestInfo.builder()
							.status(latestSubmitted.getStatus().name())
							.grade(null)
							.gradingTrainerName(null)
							.build();
					}
				}
			}
		}

		// 2. 상담 여부 확인
		List<Consultation> consultations =
			consultationRepository.findByCustomerIdOrderByConsultationDateDescConsultationTimeDesc(customer.getId());
		boolean hasConsultation = !consultations.isEmpty();

		// 3. DTO 생성 (static factory method 사용)
		return NewSubscriptionCustomerResponseDTO.from(customer, hasAttemptedLevelTest, levelTestInfo, hasConsultation);
	}
}
