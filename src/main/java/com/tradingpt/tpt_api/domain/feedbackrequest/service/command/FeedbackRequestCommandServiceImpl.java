package com.tradingpt.tpt_api.domain.feedbackrequest.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateDayRequestDetailRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateScalpingRequestDetailRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateSwingRequestDetailRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestResponse;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.DayRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.ScalpingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.SwingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackRequestCommandServiceImpl implements FeedbackRequestCommandService {

	private final FeedbackRequestRepository feedbackRequestRepository;
	private final UserRepository userRepository;

	@Override
	public FeedbackRequestResponse createDayRequest(CreateDayRequestDetailRequest request, Long customerId) {
		Customer customer = getCustomerById(customerId);
		DayRequestDetail dayRequest = DayRequestDetail.createFrom(request, customer);
		DayRequestDetail saved = (DayRequestDetail)feedbackRequestRepository.save(dayRequest);
		return FeedbackRequestResponse.of(saved);
	}

	@Override
	public FeedbackRequestResponse createScalpingRequest(CreateScalpingRequestDetailRequest request, Long customerId) {
		Customer customer = getCustomerById(customerId);
		ScalpingRequestDetail scalpingRequest = ScalpingRequestDetail.createFrom(request, customer);
		ScalpingRequestDetail saved = (ScalpingRequestDetail)feedbackRequestRepository.save(scalpingRequest);
		return FeedbackRequestResponse.of(saved);
	}

	@Override
	public FeedbackRequestResponse createSwingRequest(CreateSwingRequestDetailRequest request, Long customerId) {
		Customer customer = getCustomerById(customerId);
		SwingRequestDetail swingRequest = SwingRequestDetail.createFrom(request, customer);
		SwingRequestDetail saved = (SwingRequestDetail)feedbackRequestRepository.save(swingRequest);
		return FeedbackRequestResponse.of(saved);
	}

	@Override
	public void deleteFeedbackRequest(Long feedbackRequestId, Long customerId) {
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 권한 확인: 자신의 피드백 요청만 삭제 가능
		if (!feedbackRequest.getCustomer().getId().equals(customerId)) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.DELETE_PERMISSION_DENIED);
		}

		// 완료된 피드백은 삭제 불가
		if (feedbackRequest.getStatus() == Status.DONE) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.COMPLETED_FEEDBACK_DELETE_NOT_ALLOWED);
		}

		feedbackRequestRepository.delete(feedbackRequest);
	}


	private Customer getCustomerById(Long customerId) {
		return (Customer)userRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));
	}

}