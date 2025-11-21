package com.tradingpt.tpt_api.domain.feedbackrequest.service.query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection.DailyPnlProjection;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.AdminFeedbackCardResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.AdminFeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DailyPnlDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.DayFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackCardResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestListItemResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MonthlyPnlCalendarResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MyCustomerNewFeedbackListItemDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.MyCustomerNewFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.ScalpingFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.SelectedBestFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.SwingFeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.TokenUsedFeedbackListItemDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.TokenUsedFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.TotalFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.TrainerWrittenFeedbackItemDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.TrainerWrittenFeedbackListResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.DayRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.ScalpingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.SwingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.DateValidationUtil;
import com.tradingpt.tpt_api.domain.feedbackresponse.dto.response.FeedbackResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackresponse.entity.FeedbackResponse;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Trainer;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
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
public class FeedbackRequestQueryServiceImpl implements FeedbackRequestQueryService {

	private final FeedbackRequestRepository feedbackRequestRepository;
	private final UserRepository userRepository;
	private final CustomerRepository customerRepository;

	@Override
	public FeedbackListResponseDTO getFeedbackListSlice(Pageable pageable) {
		Slice<FeedbackRequest> feedbackSlice = feedbackRequestRepository
			.findAllFeedbackRequestsSlice(pageable);

		Slice<FeedbackCardResponseDTO> cardSlice = feedbackSlice
			.map(FeedbackCardResponseDTO::from);

		return FeedbackListResponseDTO.of(
			cardSlice.getContent(),
			SliceInfo.of(cardSlice)
		);
	}

	@Override
	public AdminFeedbackResponseDTO getAdminFeedbackListSlice(Pageable pageable) {
		// 1. 베스트 피드백 3개 조회 및 변환
		List<FeedbackRequest> bestFeedbacks = feedbackRequestRepository
			.findTop3ByIsBestFeedbackTrueOrderByCreatedAtDesc();

		List<AdminFeedbackCardResponseDTO> bestFeedbackCards = bestFeedbacks.stream()
			.map(this::toAdminFeedbackCardDTO)
			.toList();

		SelectedBestFeedbackListResponseDTO selectedBestFeedbacks =
			SelectedBestFeedbackListResponseDTO.from(bestFeedbackCards);

		// 2. 전체 피드백 조회 및 변환
		Slice<FeedbackRequest> allFeedbackSlice = feedbackRequestRepository
			.findAllFeedbacksByCreatedAtDesc(pageable);

		Slice<AdminFeedbackCardResponseDTO> adminFeedbackCardSlice = allFeedbackSlice
			.map(this::toAdminFeedbackCardDTO);

		TotalFeedbackListResponseDTO totalFeedbacks = TotalFeedbackListResponseDTO.builder()
			.adminFeedbackCardResponseDTOS(adminFeedbackCardSlice.getContent())
			.sliceInfo(SliceInfo.of(adminFeedbackCardSlice))
			.build();

		// 3. 최종 응답 반환
		return AdminFeedbackResponseDTO.of(selectedBestFeedbacks, totalFeedbacks);
	}

	@Override
	public List<FeedbackRequestListItemResponseDTO> getDailyFeedbackRequests(
		Long customerId,
		Integer year,
		Integer month,
		Integer day
	) {
		log.info("Fetching daily feedback requests for customerId={}, date={}-{}-{}",
			customerId, year, month, day);

		// 1. 날짜 유효성 검증
		DateValidationUtil.validateDate(year, month, day);

		// 2. 고객 존재 여부 확인
		if (!customerRepository.existsById(customerId)) {
			throw new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND);
		}

		// 3. LocalDate 생성
		LocalDate targetDate = LocalDate.of(year, month, day);

		// 4. 해당 날짜의 피드백 요청 조회
		List<FeedbackRequest> requests = feedbackRequestRepository
			.findByCustomerIdAndDate(customerId, targetDate);

		log.info("Found {} feedback requests for date {}", requests.size(), targetDate);

		// 5. DTO 변환
		return requests.stream()
			.map(FeedbackRequestListItemResponseDTO::from)
			.toList();
	}

	@Override
	public MonthlyPnlCalendarResponseDTO getMonthlyPnlCalendar(
		Long customerId,
		Integer year,
		Integer month
	) {
		log.info("Fetching monthly PnL calendar for customerId={}, year={}, month={}",
			customerId, year, month);

		// 1. 날짜 검증
		DateValidationUtil.validatePastOrPresentYearMonth(year, month);

		// 2. 고객 존재 여부 확인
		if (!customerRepository.existsById(customerId)) {
			throw new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND);
		}

		// 3. 일별 PnL 데이터 조회
		List<DailyPnlProjection> projections = feedbackRequestRepository
			.findDailyPnlByCustomerIdAndYearAndMonth(customerId, year, month);

		// 4. DTO 변환
		List<DailyPnlDTO> dailyPnls = projections.stream()
			.map(projection -> DailyPnlDTO.builder()
				.day(projection.getFeedbackRequestDate().getDayOfMonth())
				.pnl(projection.getTotalPnl())
				.pnlPercentage(projection.getAveragePnlPercentage())
				.feedbackCount(projection.getFeedbackCount().intValue())
				.build())
			.collect(Collectors.toList());

		// 5. 월 전체 통계 계산
		BigDecimal totalPnl = projections.stream()
			.map(DailyPnlProjection::getTotalPnl)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		Double averagePnlPercentage = projections.isEmpty() ? 0.0 :
			projections.stream()
				.mapToDouble(p -> p.getAveragePnlPercentage() != null ? p.getAveragePnlPercentage() : 0.0)
				.average()
				.orElse(0.0);

		// 소수점 2자리까지 반올림
		averagePnlPercentage = Math.round(averagePnlPercentage * 100.0) / 100.0;

		log.info("Found {} days with PnL data for {}-{}", dailyPnls.size(), year, month);

		return MonthlyPnlCalendarResponseDTO.builder()
			.year(year)
			.month(month)
			.dailyPnls(dailyPnls)
			.totalPnl(totalPnl)
			.averagePnlPercentage(averagePnlPercentage)
			.build();
	}

	@Override
	public TokenUsedFeedbackListResponseDTO getTokenUsedFeedbackRequests(Pageable pageable) {
		log.info("Fetching token-used feedback requests with page={}, size={}",
			pageable.getPageNumber(), pageable.getPageSize());

		// 1. Slice로 토큰 사용 피드백 조회
		Slice<FeedbackRequest> feedbackSlice = feedbackRequestRepository
			.findTokenUsedFeedbackRequests(pageable);

		// 2. DTO 변환
		List<TokenUsedFeedbackListItemDTO> feedbackDTOs = feedbackSlice.getContent()
			.stream()
			.map(TokenUsedFeedbackListItemDTO::from)
			.collect(Collectors.toList());

		// 3. SliceInfo 생성
		SliceInfo sliceInfo = SliceInfo.of(feedbackSlice);

		log.info("Found {} token-used feedback requests, hasNext={}",
			feedbackDTOs.size(), sliceInfo.getHasNext());

		return TokenUsedFeedbackListResponseDTO.of(feedbackDTOs, sliceInfo);
	}

	@Override
	public MyCustomerNewFeedbackListResponseDTO getMyCustomerNewFeedbackRequests(Long trainerId, Pageable pageable) {
		// 1. 트레이너 존재 여부 확인
		if (!userRepository.existsById(trainerId)) {
			throw new UserException(UserErrorStatus.TRAINER_NOT_FOUND);
		}

		// 2. Slice로 새로운 피드백 요청 조회
		Slice<FeedbackRequest> feedbackSlice = feedbackRequestRepository
			.findNewFeedbackRequestsByTrainer(trainerId, pageable);

		// 3. DTO 변환
		List<MyCustomerNewFeedbackListItemDTO> feedbackDTOs = feedbackSlice.getContent()
			.stream()
			.map(MyCustomerNewFeedbackListItemDTO::from)
			.toList();

		// 4. SliceInfo 생성
		SliceInfo sliceInfo = SliceInfo.of(feedbackSlice);

		return MyCustomerNewFeedbackListResponseDTO.of(feedbackDTOs, sliceInfo);
	}

	@Override
	public FeedbackRequestDetailResponseDTO getAdminFeedbackDetail(Long feedbackRequestId) {
		// 1. 피드백 조회
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(
				FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 2. 피드백 답변 조회
		FeedbackResponse feedbackResponse = feedbackRequest.getFeedbackResponse();

		// 3. 응답 DTO 생성
		FeedbackRequestDetailResponseDTO.FeedbackRequestDetailResponseDTOBuilder builder =
			FeedbackRequestDetailResponseDTO.builder()
				.id(feedbackRequest.getId())
				.investmentType(feedbackRequest.getInvestmentType())
				.membershipLevel(feedbackRequest.getMembershipLevel())
				.status(feedbackRequest.getStatus());

		// 4. 타입별 상세 정보 추가
		if (feedbackRequest instanceof DayRequestDetail dayRequest) {
			builder.dayDetail(DayFeedbackRequestDetailResponseDTO.of(dayRequest));
		} else if (feedbackRequest instanceof ScalpingRequestDetail scalpingRequest) {
			builder.scalpingDetail(ScalpingFeedbackRequestDetailResponseDTO.of(scalpingRequest));
		} else if (feedbackRequest instanceof SwingRequestDetail swingRequest) {
			builder.swingDetail(SwingFeedbackRequestDetailResponseDTO.of(swingRequest));
		}

		// 5. 피드백 응답 정보 추가
		if (feedbackResponse != null) {
			Trainer trainer = feedbackResponse.getTrainer();
			builder.feedbackResponse(FeedbackResponseDTO.of(feedbackResponse, trainer));
		}

		return builder.build();

	}

	/**
	 * FeedbackRequest를 AdminFeedbackCardDTO로 변환하는 헬퍼 메서드
	 */
	private AdminFeedbackCardResponseDTO toAdminFeedbackCardDTO(FeedbackRequest feedback) {
		return AdminFeedbackCardResponseDTO.of(
			feedback.getId(),
			feedback.getIsBestFeedback(),
			feedback.getCustomer().getUsername(),
			feedback.getCustomer().getAssignedTrainer() != null ?
				feedback.getCustomer().getAssignedTrainer().getUsername() : null,
			feedback.getInvestmentType(),
			feedback.getCourseStatus(),
			feedback.getCreatedAt(),
			feedback.getFeedbackResponse() != null ?
				feedback.getFeedbackResponse().getSubmittedAt() : null
		);
	}

	/**
	 * 피드백 요청 상세 조회
	 *
	 * ⚠️ 조회 시 자동으로 읽음 처리:
	 * - FeedbackResponse가 존재하고 아직 읽지 않은 경우 (Status.FN)
	 * - 자동으로 Status.FR로 변경 (읽음 처리)
	 */
	@Override
	@Transactional  // ✅ 메서드 레벨에서 readOnly=false로 오버라이드
	public FeedbackRequestDetailResponseDTO getFeedbackRequestById(
		Long feedbackRequestId,
		Long currentUserId
	) {
		log.info("Fetching feedback request detail: id={}, userId={}",
			feedbackRequestId, currentUserId);

		// 1. 피드백 조회
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(
				FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 2. 접근 권한 검증
		validateFeedbackAccess(feedbackRequest, currentUserId);

		// 3. ✅ 읽음 처리 (FeedbackResponse가 있고 아직 읽지 않은 경우)
		FeedbackResponse feedbackResponse = feedbackRequest.getFeedbackResponse();
		if (feedbackResponse != null && feedbackRequest.getStatus() == Status.FN) {
			feedbackRequest.setStatus(Status.FR);
			log.info("Feedback request marked as read: id={}", feedbackRequestId);
		}

		// 4. 응답 DTO 생성
		FeedbackRequestDetailResponseDTO.FeedbackRequestDetailResponseDTOBuilder builder =
			FeedbackRequestDetailResponseDTO.builder()
				.id(feedbackRequest.getId())
				.investmentType(feedbackRequest.getInvestmentType())
				.membershipLevel(feedbackRequest.getMembershipLevel())
				.status(feedbackRequest.getStatus());

		// 5. 타입별 상세 정보 추가
		if (feedbackRequest instanceof DayRequestDetail dayRequest) {
			builder.dayDetail(DayFeedbackRequestDetailResponseDTO.of(dayRequest));
		} else if (feedbackRequest instanceof ScalpingRequestDetail scalpingRequest) {
			builder.scalpingDetail(ScalpingFeedbackRequestDetailResponseDTO.of(scalpingRequest));
		} else if (feedbackRequest instanceof SwingRequestDetail swingRequest) {
			builder.swingDetail(SwingFeedbackRequestDetailResponseDTO.of(swingRequest));
		}

		// 6. 피드백 응답 정보 추가
		if (feedbackResponse != null) {
			Trainer trainer = feedbackResponse.getTrainer();
			builder.feedbackResponse(FeedbackResponseDTO.of(feedbackResponse, trainer));
		}

		return builder.build();
	}

	/**
	 * 피드백 접근 권한 검증
	 *
	 * 접근 규칙:
	 * 1. 베스트 피드백: 누구나 조회 가능 (로그인/비로그인 무관)
	 * 2. 트레이너/어드민: 모든 피드백 조회 가능
	 * 3. 일반 피드백 + 구독자(PREMIUM): 모든 피드백 조회 가능
	 * 4. 일반 피드백 + 비구독자: 자신의 피드백만 조회 가능
	 * 5. 비로그인 + 일반 피드백: 접근 불가
	 *
	 * @param feedbackRequest 조회할 피드백
	 * @param currentUserId 현재 사용자 ID (null 가능)
	 * @throws FeedbackRequestException 접근 권한이 없을 경우
	 */
	private void validateFeedbackAccess(FeedbackRequest feedbackRequest, Long currentUserId) {
		// ✅ 1. 베스트 피드백은 누구나 접근 가능
		if (Boolean.TRUE.equals(feedbackRequest.getIsBestFeedback())) {
			log.debug("Best feedback access allowed for feedbackId: {}", feedbackRequest.getId());
			return;
		}

		// 일반 피드백일 경우
		// ✅ 2. 비로그인 사용자는 일반 피드백 접근 불가
		if (currentUserId == null) {
			log.warn("Unauthorized access attempt to feedback: {}", feedbackRequest.getId());
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.ACCESS_DENIED);
		}

		// ✅ 3. 트레이너/어드민 권한 확인
		if (isTrainerOrAdmin()) {
			log.debug("Trainer/Admin access allowed for feedbackId: {}", feedbackRequest.getId());
			return;
		}

		// ✅ 4. 자신의 피드백인 경우 접근 허용
		if (feedbackRequest.getCustomer().getId().equals(currentUserId)) {
			log.debug("Owner access allowed for feedbackId: {}", feedbackRequest.getId());
			return;
		}

		// ✅ 5. 구독자(PREMIUM)인 경우 모든 피드백 접근 가능
		Customer customer = (Customer)userRepository.findById(currentUserId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		if (customer.getMembershipLevel() == MembershipLevel.PREMIUM) {
			log.debug("Premium member access allowed for feedbackId: {}", feedbackRequest.getId());
			return;
		}

		// ✅ 6. 그 외의 경우 접근 거부 (비구독자가 다른 사람의 피드백 조회 시도)
		log.warn("Access denied for user {} to feedback {}", currentUserId, feedbackRequest.getId());
		throw new FeedbackRequestException(FeedbackRequestErrorStatus.ACCESS_DENIED);
	}

	/**
	 * 현재 사용자가 트레이너 또는 어드민 권한을 가지고 있는지 확인
	 *
	 * @return 트레이너/어드민이면 true, 아니면 false
	 */
	private boolean isTrainerOrAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		return authentication.getAuthorities().stream()
			.anyMatch(authority ->
				authority.getAuthority().equals("ROLE_TRAINER") ||
					authority.getAuthority().equals("ROLE_ADMIN")
			);
	}

	@Override
	public TrainerWrittenFeedbackListResponseDTO getTrainerWrittenFeedbacks(Pageable pageable) {
		log.info("Fetching trainer-written feedback requests with page={}, size={}",
			pageable.getPageNumber(), pageable.getPageSize());

		// 1. Slice로 트레이너 작성 매매일지 조회
		Slice<FeedbackRequest> feedbackSlice = feedbackRequestRepository
			.findTrainerWrittenFeedbacks(pageable);

		// 2. DTO 변환
		List<TrainerWrittenFeedbackItemDTO> feedbackDTOs = feedbackSlice.getContent()
			.stream()
			.map(TrainerWrittenFeedbackItemDTO::from)
			.collect(Collectors.toList());

		// 3. SliceInfo 생성
		SliceInfo sliceInfo = SliceInfo.of(feedbackSlice);

		log.info("Found {} trainer-written feedback requests, hasNext={}",
			feedbackDTOs.size(), sliceInfo.getHasNext());

		return TrainerWrittenFeedbackListResponseDTO.of(feedbackDTOs, sliceInfo);
	}
}