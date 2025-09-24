package com.tradingpt.tpt_api.domain.feedbackrequest.service.command;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateDayRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateScalpingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateSwingRequestDetailRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DayFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.ScalpingFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.SwingFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.DayRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequestAttachment;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.ScalpingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.SwingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.infrastructure.s3.S3FileService;
import com.tradingpt.tpt_api.global.infrastructure.s3.S3UploadResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackRequestCommandServiceImpl implements FeedbackRequestCommandService {

	private final FeedbackRequestRepository feedbackRequestRepository;
	private final UserRepository userRepository;
	private final S3FileService s3FileService;

	@Override
	public DayFeedbackRequestDetailResponseDTO createDayRequest(CreateDayRequestDetailRequestDTO request,
		Long customerId) {
		Customer customer = getCustomerById(customerId);

		// Day는 몇 주차 피드백인지 서버에서 자동으로 알아내야한다.
		FeedbackPeriodUtil.FeedbackPeriod period = FeedbackPeriodUtil.resolveFrom(request.getRequestDate());

		String title = buildDayRequestTitle(request.getRequestDate(),
			feedbackRequestRepository.countDayRequestsByCustomerAndDate(customerId, request.getRequestDate()) + 1);

		// DayRequestDetail 생성
		DayRequestDetail dayRequest = DayRequestDetail.createFrom(request, customer, period, title);

		// 스크린샷 파일들이 있으면 S3에 업로드하고 attachment 생성
		if (request.getScreenshotFiles() != null && !request.getScreenshotFiles().isEmpty()) {
			for (MultipartFile screenshotFile : request.getScreenshotFiles()) {
				if (screenshotFile != null && !screenshotFile.isEmpty()) {
					S3UploadResult uploadResult = s3FileService.upload(screenshotFile, "feedback-requests/screenshots");
					FeedbackRequestAttachment.createFrom(dayRequest, uploadResult.url()); // 양방향 연관 관계 매핑
				}
			}
		}

		// CASCADE 설정으로 FeedbackRequest 저장 시 attachment도 자동 저장됨
		DayRequestDetail saved = (DayRequestDetail)feedbackRequestRepository.save(dayRequest);
		return DayFeedbackRequestDetailResponseDTO.of(saved);
	}

	@Override
	public ScalpingFeedbackRequestDetailResponseDTO createScalpingRequest(CreateScalpingRequestDetailRequestDTO request,
		Long customerId) {
		Customer customer = getCustomerById(customerId);

		FeedbackPeriodUtil.FeedbackPeriod period = FeedbackPeriodUtil.resolveFrom(request.getRequestDate());

		// ScalpingRequestDetail 생성
		ScalpingRequestDetail scalpingRequest = ScalpingRequestDetail.createFrom(request, customer, period);

		// 스크린샷 파일들이 있으면 S3에 업로드하고 attachment 생성
		if (request.getScreenshotFiles() != null && !request.getScreenshotFiles().isEmpty()) {
			for (MultipartFile screenshotFile : request.getScreenshotFiles()) {
				if (screenshotFile != null && !screenshotFile.isEmpty()) {
					S3UploadResult uploadResult = s3FileService.upload(screenshotFile, "feedback-requests/screenshots");
					FeedbackRequestAttachment.createFrom(scalpingRequest, uploadResult.url());
				}
			}
		}

		// CASCADE 설정으로 FeedbackRequest 저장 시 attachment도 자동 저장됨
		ScalpingRequestDetail saved = (ScalpingRequestDetail)feedbackRequestRepository.save(scalpingRequest);
		return ScalpingFeedbackRequestDetailResponseDTO.of(saved);
	}

	@Override
	public SwingFeedbackRequestDetailResponseDTO createSwingRequest(CreateSwingRequestDetailRequestDTO request,
		Long customerId) {
		Customer customer = getCustomerById(customerId);

		// SwingRequestDetail 생성
		SwingRequestDetail swingRequest = SwingRequestDetail.createFrom(request, customer);

		// 스크린샷 파일들이 있으면 S3에 업로드하고 attachment 생성
		if (request.getScreenshotFiles() != null && !request.getScreenshotFiles().isEmpty()) {
			for (MultipartFile screenshotFile : request.getScreenshotFiles()) {
				if (screenshotFile != null && !screenshotFile.isEmpty()) {
					S3UploadResult uploadResult = s3FileService.upload(screenshotFile, "feedback-requests/screenshots");
					FeedbackRequestAttachment.createFrom(swingRequest, uploadResult.url());
				}
			}
		}

		// CASCADE 설정으로 FeedbackRequest 저장 시 attachment도 자동 저장됨
		SwingRequestDetail saved = (SwingRequestDetail)feedbackRequestRepository.save(swingRequest);
		return SwingFeedbackRequestDetailResponseDTO.of(saved);
	}

	@Override
	public Void deleteFeedbackRequest(Long feedbackRequestId, Long customerId) {
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 권한 확인: 자신의 피드백 요청만 삭제 가능
		if (!feedbackRequest.getCustomer().getId().equals(customerId)) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.DELETE_PERMISSION_DENIED);
		}

		feedbackRequestRepository.delete(feedbackRequest);

		return null;
	}

	private Customer getCustomerById(Long customerId) {
		return (Customer)userRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));
	}

	private String buildDayRequestTitle(LocalDate requestDate, long order) {
		int month = requestDate.getMonthValue();
		int day = requestDate.getDayOfMonth();
		return String.format("%d/%d (%d) 작성완료", month, day, order);
	}

}
