package com.tradingpt.tpt_api.domain.user.service.query;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.user.dto.response.FreeCustomerResponseDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.MyCustomerListItemDTO;
import com.tradingpt.tpt_api.domain.user.dto.response.MyCustomerListResponseDTO;
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
	 * 4. Repository에서 조회 후 DTO 변환
	 *
	 * @param pageable 페이징 정보
	 * @return 미구독 고객 Slice
	 */
	@Override
	public Slice<FreeCustomerResponseDTO> getFreeCustomers(Pageable pageable) {
		// 1. Repository에서 미구독 고객 조회
		Slice<Customer> customerSlice = customerRepository.findFreeCustomers(pageable);

		// 2. Entity를 DTO로 변환 (Slice 유지)
		return customerSlice.map(FreeCustomerResponseDTO::from);
	}
}
