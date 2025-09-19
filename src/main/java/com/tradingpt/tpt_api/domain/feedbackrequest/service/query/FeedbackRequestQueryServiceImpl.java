package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DayFeedbackRequestDetailResponseDTO;
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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackRequestQueryServiceImpl implements FeedbackRequestQueryService {

	private final FeedbackRequestRepository feedbackRequestRepository;

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
	public FeedbackRequestDetailResponseDTO getFeedbackRequestById(Long feedbackRequestId, Long currentUserId) {
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 권한 체크: 고객은 자신의 피드백만, 트레이너는 모든 피드백 조회 가능
		if (!hasAccessPermission(feedbackRequestId, currentUserId)) {
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

	@Override
	public boolean hasAccessPermission(Long feedbackRequestId, Long currentUserId) {
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

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
