package com.tradingpt.tpt_api.domain.feedbackrequest.service.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tradingpt.tpt_api.domain.feedbackrequest.dto.request.CreateFeedbackRequestDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.response.FeedbackRequestDetailResponseDTO;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequestAttachment;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.global.common.RewardConstants;
import com.tradingpt.tpt_api.global.infrastructure.s3.response.S3UploadResult;
import com.tradingpt.tpt_api.global.infrastructure.s3.service.S3FileService;

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
	public FeedbackRequestDetailResponseDTO createFeedbackRequest(CreateFeedbackRequestDTO request, Long customerId) {
		Customer customer = getCustomerById(customerId);

		// 사용자의 트레이딩 타입 체크 (throw exception)
		customer.checkTradingType(request.getInvestmentType());

		// ✅ 토큰 검증 및 차감 (선택적)
		validateAndConsumeTokenIfNeeded(customer, request.getUseToken(), request.getTokenAmount());

		// 피드백 기간 정보 계산
		FeedbackPeriodUtil.FeedbackPeriod period = FeedbackPeriodUtil.resolveFrom(request.getFeedbackRequestDate());

		// 거래 날짜를 기반으로 제목을 자동 생성함.
		String title = buildFeedbackTitle(request.getFeedbackRequestDate(),
			request.getCategory(), request.getTotalAssetPnl());

		// FeedbackRequest 생성 (Single Table 전략 - investmentType으로 구분)
		FeedbackRequest feedbackRequest = FeedbackRequest.builder()
			// 기본 정보
			.investmentType(request.getInvestmentType())
			.customer(customer)
			.title(title)
			.courseStatus(request.getCourseStatus())
			.membershipLevel(request.getMembershipLevel())
			// 날짜 정보
			.feedbackYear(period.year())
			.feedbackMonth(period.month())
			.feedbackWeek(period.week())
			.feedbackRequestDate(request.getFeedbackRequestDate())
			// 매매 기본 정보
			.category(request.getCategory())
			.positionHoldingTime(request.getPositionHoldingTime())
			.riskTaking(request.getRiskTaking())
			.leverage(request.getLeverage())
			.position(request.getPosition())
			.pnl(request.getPnl())
			.totalAssetPnl(request.getTotalAssetPnl())
			.rnr(request.getRnr())
			// 매매 상세 정보
			.operatingFundsRatio(request.getOperatingFundsRatio())
			.entryPrice(request.getEntryPrice())
			.exitPrice(request.getExitPrice())
			.settingStopLoss(request.getSettingStopLoss())
			.settingTakeProfit(request.getSettingTakeProfit())
			.positionStartReason(request.getPositionStartReason())
			.positionEndReason(request.getPositionEndReason())
			.tradingReview(request.getTradingReview())
			// 완강 후 필드 (DAY/SWING 공통)
			.directionFrameExists(request.getDirectionFrameExists())
			.directionFrame(request.getDirectionFrame())
			.mainFrame(request.getMainFrame())
			.subFrame(request.getSubFrame())
			.trendAnalysis(request.getTrendAnalysis())
			.trainerFeedbackRequestContent(request.getTrainerFeedbackRequestContent())
			.entryPoint(request.getEntryPoint())
			.grade(request.getGrade())
			.additionalBuyCount(request.getAdditionalBuyCount())
			.splitSellCount(request.getSplitSellCount())
			// SWING 전용 필드 (DAY에서는 null)
			.positionStartDate(request.getPositionStartDate())
			.positionEndDate(request.getPositionEndDate())
			.build();

		// ⭐ 스크린샷 업로드 (공통 메서드 사용)
		uploadScreenshots(request.getScreenshotFiles(), feedbackRequest);

		// ✅ 토큰 사용 여부 설정
		if (Boolean.TRUE.equals(request.getUseToken())) {
			Integer tokenAmount = request.getTokenAmount() != null ? request.getTokenAmount() : RewardConstants.DEFAULT_TOKEN_CONSUMPTION;
			feedbackRequest.useToken(tokenAmount);
			log.info("Feedback request created with token: customerId={}, tokenAmount={}",
				customerId, tokenAmount);
		} else {
			log.info("Feedback request created as record-only (no token): customerId={}", customerId);
		}

		// CASCADE 설정으로 FeedbackRequest 저장 시 attachment도 자동 저장됨
		FeedbackRequest saved = feedbackRequestRepository.save(feedbackRequest);

		// ⭐ 피드백 카운트 증가 및 토큰 보상 (DDD 패턴)
		customer.incrementFeedbackCount();
		boolean rewarded = customer.rewardTokensIfEligible(
			RewardConstants.FEEDBACK_THRESHOLD,
			RewardConstants.TOKEN_REWARD_AMOUNT
		);

		if (rewarded) {
			log.info("🎉 Token reward milestone reached! customerId={}, feedbackCount={}, tokensEarned={}, totalTokens={}",
				customerId,
				customer.getFeedbackRequestCount(),
				RewardConstants.TOKEN_REWARD_AMOUNT,
				customer.getToken());
		}

		// JPA Dirty Checking이 자동으로 Customer UPDATE (save() 불필요)

		return FeedbackRequestDetailResponseDTO.from(saved);
	}

	@Override
	public Void deleteFeedbackRequest(Long feedbackRequestId, Long customerId) {
		FeedbackRequest feedbackRequest = feedbackRequestRepository.findById(feedbackRequestId)
			.orElseThrow(() -> new FeedbackRequestException(FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND));

		// 권한 확인: 자신의 피드백 요청만 삭제 가능
		if (!feedbackRequest.getCustomer().getId().equals(customerId)) {
			throw new FeedbackRequestException(FeedbackRequestErrorStatus.DELETE_PERMISSION_DENIED);
		}

		// ✅ 누적 작성 횟수는 삭제 시에도 감소하지 않음 (총 몇 개를 작성했는지만 카운트)
		// 피드백 카운트는 단조증가하므로 decrementFeedbackCount() 호출 제거

		feedbackRequestRepository.delete(feedbackRequest);

		log.info("Feedback deleted: feedbackRequestId={}, customerId={}",
			feedbackRequestId, customerId);

		return null;
	}

	@Override
	public Void updateBestFeedbacks(List<Long> feedbackIds) {
		// 1. 개수 검증 (최대 개수는 FeedbackRequest.MAX_BEST_FEEDBACK_COUNT)
		if (feedbackIds.size() > FeedbackRequest.MAX_BEST_FEEDBACK_COUNT) {
			throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.BEST_FEEDBACK_LIMIT_EXCEEDED
			);
		}

		// 2. 기존 베스트 피드백 모두 해제
		List<FeedbackRequest> currentBestFeedbacks = feedbackRequestRepository
			.findByIsBestFeedbackTrue();

		currentBestFeedbacks.forEach(feedback ->
			feedback.updateIsBestFeedback(false)
		);

		// 3. 빈 배열이면 여기서 종료 (모든 베스트 해제만)
		if (feedbackIds.isEmpty()) {
			log.info("All best feedbacks have been cleared");
			return null;
		}

		// 4. 새로운 베스트 피드백 지정
		List<FeedbackRequest> newBestFeedbacks = feedbackRequestRepository
			.findAllById(feedbackIds);

		// 5. 요청된 ID가 모두 존재하는지 확인
		if (newBestFeedbacks.size() != feedbackIds.size()) {
			throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.FEEDBACK_REQUEST_NOT_FOUND
			);
		}

		// 6. 베스트로 지정
		newBestFeedbacks.forEach(feedback ->
			feedback.updateIsBestFeedback(true)
		);

		log.info("Best feedbacks updated: {} feedbacks selected", newBestFeedbacks.size());

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
				FeedbackRequestAttachment.createFrom(feedbackRequest, uploadResult.url(), uploadResult.key());
			}
		}
	}

	/**
	 * ✅ 토큰 검증 및 차감 (선택적)
	 *
	 * 변경된 로직:
	 * - BASIC 멤버십: 토큰 사용 선택 가능
	 *   - useToken=true → 토큰 차감 후 트레이너가 볼 수 있음
	 *   - useToken=false → 기록용으로만 생성 (트레이너가 볼 수 없음)
	 * - PREMIUM 멤버십: 토큰 사용 불가 (기존 유지)
	 */
	private void validateAndConsumeTokenIfNeeded(Customer customer, Boolean useToken, Integer tokenAmount) {
		MembershipLevel membershipLevel = customer.getMembershipLevel();

		// PREMIUM 멤버십인 경우
		if (membershipLevel == MembershipLevel.PREMIUM) {
			// 토큰 사용 불가
			if (Boolean.TRUE.equals(useToken)) {
				throw new FeedbackRequestException(
					FeedbackRequestErrorStatus.TOKEN_NOT_ALLOWED_FOR_PREMIUM_MEMBERSHIP);
			}
			// PREMIUM은 토큰 없이 자유롭게 생성 가능
			return;
		}

		// BASIC 멤버십인 경우
		if (membershipLevel == MembershipLevel.BASIC) {
			// 토큰 사용을 선택한 경우
			if (Boolean.TRUE.equals(useToken)) {
				// 토큰 개수 기본값 설정 (기본 3개 소모)
				int requiredTokens = tokenAmount != null ? tokenAmount : RewardConstants.DEFAULT_TOKEN_CONSUMPTION;

				// 토큰 부족 체크
				if (customer.getToken() < requiredTokens) {
					log.warn("Insufficient tokens: customerId={}, required={}, available={}",
						customer.getId(), requiredTokens, customer.getToken());
					throw new FeedbackRequestException(
						FeedbackRequestErrorStatus.INSUFFICIENT_TOKEN);
				}

				// 토큰 차감
				customer.updateToken(customer.getToken() - requiredTokens);
				log.info("Token consumed: customerId={}, amount={}, remaining={}",
					customer.getId(), requiredTokens, customer.getToken());
			} else {
				// 토큰 사용 안 함 → 기록용으로만 생성
				log.info("BASIC member creating record-only feedback: customerId={}",
					customer.getId());
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
	 * 형식: "월/일 종목 ±전체자산대비PnL%"
	 * 예시: "11/17 주식 +5%", "11/18 코인 -3.5%"
	 *
	 * @param requestDate 요청 날짜
	 * @param category 종목
	 * @param totalAssetPnl 전체 자산 대비 P&L
	 * @return 생성된 제목
	 */
	private String buildFeedbackTitle(LocalDate requestDate, String category, BigDecimal totalAssetPnl) {
		int month = requestDate.getMonthValue();
		int day = requestDate.getDayOfMonth();

		// totalAssetPnl을 퍼센트 문자열로 변환
		String pnlString;
		if (totalAssetPnl == null) {
			pnlString = "0%";
		} else {
			// 불필요한 trailing zeros 제거
			BigDecimal strippedPnl = totalAssetPnl.stripTrailingZeros();

			// 양수면 + 기호 추가, 음수는 자동으로 - 기호 포함
			if (totalAssetPnl.compareTo(BigDecimal.ZERO) > 0) {
				pnlString = "+" + strippedPnl.toPlainString() + "%";
			} else if (totalAssetPnl.compareTo(BigDecimal.ZERO) < 0) {
				pnlString = strippedPnl.toPlainString() + "%";
			} else {
				pnlString = "0%";
			}
		}

		return String.format("%d/%d %s %s", month, day, category, pnlString);
	}

}
