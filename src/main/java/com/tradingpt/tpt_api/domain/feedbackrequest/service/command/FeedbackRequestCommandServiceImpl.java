package com.tradingpt.tpt_api.domain.feedbackrequest.service.command;

import java.time.LocalDate;
import java.util.List;

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
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
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

		// 사용자의 트레이딩 타입 체크 ( throw exception )
		customer.checkTradingType(InvestmentType.DAY);

		// Day는 몇 주차 피드백인지 서버에서 자동으로 알아내야한다.
		FeedbackPeriodUtil.FeedbackPeriod period = FeedbackPeriodUtil.resolveFrom(request.getFeedbackRequestDate());

		// 거래 날짜를 기반으로 제목을 자동 생성함.
		String title = buildFeedbackTitle(request.getFeedbackRequestDate(),
			feedbackRequestRepository.countRequestsByCustomerAndDateAndType(
				customerId, request.getFeedbackRequestDate(), InvestmentType.DAY) + 1);

		// DayRequestDetail 생성
		DayRequestDetail dayRequest = DayRequestDetail.createFrom(request, customer, period, title);

		// ⭐ 스크린샷 업로드 (공통 메서드 사용)
		uploadScreenshots(request.getScreenshotFiles(), dayRequest);

		// CASCADE 설정으로 FeedbackRequest 저장 시 attachment도 자동 저장됨
		DayRequestDetail saved = (DayRequestDetail)feedbackRequestRepository.save(dayRequest);
		return DayFeedbackRequestDetailResponseDTO.of(saved);
	}

	@Override
	public ScalpingFeedbackRequestDetailResponseDTO createScalpingRequest(CreateScalpingRequestDetailRequestDTO request,
		Long customerId) {
		Customer customer = getCustomerById(customerId);

		// 사용자의 트레이딩 타입 체크 ( throw exception )
		customer.checkTradingType(InvestmentType.SCALPING);

		FeedbackPeriodUtil.FeedbackPeriod period = FeedbackPeriodUtil.resolveFrom(request.getFeedbackRequestDate());

		String title = buildFeedbackTitle(request.getFeedbackRequestDate(),
			feedbackRequestRepository.countRequestsByCustomerAndDateAndType(
				customerId, request.getFeedbackRequestDate(), InvestmentType.SCALPING) + 1);

		// ScalpingRequestDetail 생성
		ScalpingRequestDetail scalpingRequest = ScalpingRequestDetail.createFrom(request, customer, period, title);

		// ⭐ 스크린샷 업로드 (공통 메서드 사용)
		uploadScreenshots(request.getScreenshotFiles(), scalpingRequest);

		// CASCADE 설정으로 FeedbackRequest 저장 시 attachment도 자동 저장됨
		ScalpingRequestDetail saved = (ScalpingRequestDetail)feedbackRequestRepository.save(scalpingRequest);
		return ScalpingFeedbackRequestDetailResponseDTO.of(saved);
	}

	@Override
	public SwingFeedbackRequestDetailResponseDTO createSwingRequest(CreateSwingRequestDetailRequestDTO request,
		Long customerId) {
		Customer customer = getCustomerById(customerId);

		// 사용자의 트레이딩 타입 체크 ( throw exception )
		customer.checkTradingType(InvestmentType.SWING);

		String title = buildFeedbackTitle(request.getFeedbackRequestDate(),
			feedbackRequestRepository.countRequestsByCustomerAndDateAndType(
				customerId, request.getFeedbackRequestDate(), InvestmentType.SWING) + 1);

		// SwingRequestDetail 생성
		SwingRequestDetail swingRequest = SwingRequestDetail.createFrom(request, customer, title);

		// ⭐ 스크린샷 업로드 (공통 메서드 사용)
		uploadScreenshots(request.getScreenshotFiles(), swingRequest);

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

	// ========================================
	// Private Helper Methods
	// ========================================

	/**
	 * 스크린샷 파일들을 S3에 업로드하고 FeedbackRequestAttachment를 생성한다.
	 *
	 * @param screenshotFiles 업로드할 스크린샷 파일 리스트
	 * @param feedbackRequest 첨부될 피드백 요청 엔티티
	 */
	private void uploadScreenshots(List<MultipartFile> screenshotFiles, FeedbackRequest feedbackRequest) {
		if (screenshotFiles == null || screenshotFiles.isEmpty()) {
			return;
		}

		for (MultipartFile screenshotFile : screenshotFiles) {
			if (screenshotFile != null && !screenshotFile.isEmpty()) {
				S3UploadResult uploadResult = s3FileService.upload(screenshotFile, "feedback-requests/screenshots");
				FeedbackRequestAttachment.createFrom(feedbackRequest, uploadResult.url());
			}
		}
	}

	/**
	 * Customer ID로 Customer 엔티티를 조회한다.
	 *
	 * @param customerId 조회할 고객 ID
	 * @return 조회된 Customer 엔티티
	 * @throws UserException 고객을 찾을 수 없는 경우
	 */
	private Customer getCustomerById(Long customerId) {
		return (Customer)userRepository.findById(customerId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));
	}

	/**
	 * 피드백 요청의 제목을 생성한다.
	 * 형식: "월/일 (순서) 작성완료"
	 *
	 * @param requestDate 요청 날짜
	 * @param order 같은 날짜의 몇 번째 요청인지
	 * @return 생성된 제목
	 */
	private String buildFeedbackTitle(LocalDate requestDate, long order) {
		int month = requestDate.getMonthValue();
		int day = requestDate.getDayOfMonth();
		return String.format("%d/%d (%d) 작성완료", month, day, order);
	}

}
