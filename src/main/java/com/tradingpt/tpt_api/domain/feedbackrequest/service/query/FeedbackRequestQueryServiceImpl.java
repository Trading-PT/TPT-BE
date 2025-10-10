package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DayFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackCardDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.ScalpingFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.SwingFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.DayRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.ScalpingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.SwingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.response.FeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.entity.FeedbackResponse;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackRequestQueryServiceImpl implements FeedbackRequestQueryService {

	private final FeedbackRequestRepository feedbackRequestRepository;
	private final UserRepository userRepository;

	@Override
	public Page<FeedbackRequestResponseDTO> getFeedbackRequests(Pageable pageable, FeedbackType feedbackType,
		Status status, Long customerId) {

		// Repository의 QueryDSL 메서드 사용
		Page<FeedbackRequest> feedbackRequestPage = feedbackRequestRepository
			.findFeedbackRequestsWithFilters(pageable, feedbackType, status, customerId);

		// Entity to DTO 변환
		return feedbackRequestPage.map(FeedbackRequestResponseDTO::of);
	}

	@Override
	public Slice<FeedbackCardDTO> getFeedbackListSlice(Pageable pageable) {
		
		Slice<FeedbackRequest> feedbackSlice = feedbackRequestRepository.findAllFeedbackRequestsSlice(pageable);

		return feedbackSlice.map(FeedbackCardDTO::from);
	}

	@Override
	public FeedbackRequestDetailResponseDTO getFeedbackRequestById(Long feedbackRequestId, Long currentUserId) {
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 권한 체크: 고객은 자신의 피드백만, 트레이너는 모든 피드백 조회 가능
		if (!hasAccessPermission(feedbackRequest, currentUserId)) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.ACCESS_DENIED);
		}

		FeedbackRequestDetailResponseDTO.FeedbackRequestDetailResponseDTOBuilder builder =
			FeedbackRequestDetailResponseDTO.builder()
				.id(feedbackRequest.getId())
				.feedbackType(feedbackRequest.getFeedbackType())
				.status(feedbackRequest.getStatus());

		if (feedbackRequest instanceof DayRequestDetail dayRequest) {
			builder.dayDetail(DayFeedbackRequestDetailResponseDTO.of(dayRequest));
		} else if (feedbackRequest instanceof ScalpingRequestDetail scalpingRequest) {
			builder.scalpingDetail(ScalpingFeedbackRequestDetailResponseDTO.of(scalpingRequest));
		} else if (feedbackRequest instanceof SwingRequestDetail swingRequest) {
			builder.swingDetail(SwingFeedbackRequestDetailResponseDTO.of(swingRequest));
		}

		FeedbackResponse feedbackResponse = feedbackRequest.getFeedbackResponse(); // 피드백 응답 조회

		if (feedbackResponse != null) {
			Trainer trainer = feedbackResponse.getTrainer();
			builder.feedbackResponse(FeedbackResponseDTO.of(feedbackResponse, trainer));
		}

		return builder.build();
	}

	@Override
	public List<FeedbackRequestResponseDTO> getMyFeedbackRequests(Long customerId, FeedbackType feedbackType,
		Status status) {

		// Repository의 QueryDSL 메서드 사용
		List<FeedbackRequest> feedbackRequests = feedbackRequestRepository
			.findMyFeedbackRequests(customerId, feedbackType, status);

		// Entity to DTO 변환
		return feedbackRequests.stream()
			.map(FeedbackRequestResponseDTO::of)
			.toList();
	}

	private boolean hasAccessPermission(FeedbackRequest feedbackRequest, Long currentUserId) {
		// 고객은 자신의 피드백만, 트레이너는 모든 피드백 접근 가능
		if (feedbackRequest.getCustomer().getId().equals(currentUserId)) {
			return true;
		}

		// 트레이너인 경우 모든 피드백 접근 가능
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return authentication != null &&
			authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_TRAINER"));
	}
}
