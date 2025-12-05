package com.tradingpt.tpt_api.domain.user.service.command;

import com.tradingpt.tpt_api.domain.complaint.repository.ComplaintRepository;
import com.tradingpt.tpt_api.domain.consultation.repository.ConsultationBlockRepository;
import com.tradingpt.tpt_api.domain.consultation.repository.ConsultationRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestAttachmentRepository;
import com.tradingpt.tpt_api.domain.feedbackrequest.repository.FeedbackRequestRepository;
import com.tradingpt.tpt_api.domain.feedbackresponse.entity.FeedbackResponse;
import com.tradingpt.tpt_api.domain.investmenttypehistory.repository.InvestmentTypeChangeRequestRepository;
import com.tradingpt.tpt_api.domain.investmenttypehistory.repository.InvestmentTypeHistoryRepository;
import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import com.tradingpt.tpt_api.domain.lecture.enums.ChapterType;
import com.tradingpt.tpt_api.domain.lecture.repository.CustomerAssignmentRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureAttachmentDownloadHistoryRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureProgressRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import com.tradingpt.tpt_api.domain.leveltest.entity.LevelTestResponse;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestAttemptRepository;
import com.tradingpt.tpt_api.domain.leveltest.repository.LeveltestResponseRepository;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.repository.MonthlyTradingSummaryRepository;
import com.tradingpt.tpt_api.domain.payment.repository.PaymentRepository;
import com.tradingpt.tpt_api.domain.paymentmethod.repository.BillingRequestRepository;
import com.tradingpt.tpt_api.domain.paymentmethod.repository.PaymentMethodRepository;
import com.tradingpt.tpt_api.domain.review.repository.ReviewAttachmentRepository;
import com.tradingpt.tpt_api.domain.review.repository.ReviewRepository;
import com.tradingpt.tpt_api.domain.review.repository.ReviewTagMappingRepository;
import com.tradingpt.tpt_api.domain.subscription.repository.SubscriptionRepository;
import com.tradingpt.tpt_api.domain.user.repository.PasswordHistoryRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.repository.WeeklyTradingSummaryRepository;
import java.util.List;
import org.springframework.data.annotation.Persistent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.user.dto.request.GiveUserTokenRequestDTO;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.Uid;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UidRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserCommandServiceImpl implements AdminUserCommandService {

	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;
	private final UidRepository uidRepository;

	private final LectureRepository lectureRepository;
	private final LectureProgressRepository lectureProgressRepository;
	private final LectureAttachmentDownloadHistoryRepository lectureAttachmentDownloadHistoryRepository;

	// === 레벨 테스트 관련 ===
	private final LeveltestAttemptRepository levelTestAttemptRepository;
	private final LeveltestResponseRepository levelTestResponseRepository;

	private final ComplaintRepository complaintRepository;

	// === 피드백(과제) 관련 ===
	private final FeedbackRequestRepository feedbackRequestRepository;
	private final FeedbackRequestAttachmentRepository feedbackRequestAttachmentRepository;

	// === 투자 성향 / 변경 ===
	private final InvestmentTypeHistoryRepository investmentHistoryRepository;
	private final InvestmentTypeChangeRequestRepository investmentTypeChangeRequestRepository;

	// === 구독 / 결제 ===
	private final SubscriptionRepository subscriptionRepository;
	private final PaymentRepository paymentRepository;
	private final PaymentMethodRepository paymentMethodRepository;
	private final BillingRequestRepository billingRequestRepository;

	// === 리뷰 ===
	private final ReviewRepository reviewRepository;
	private final ReviewAttachmentRepository reviewAttachmentRepository;
	private final ReviewTagMappingRepository reviewTagMappingRepository;

	// === 보고서 ===
	private final WeeklyTradingSummaryRepository weeklyTradingSummaryRepository;
	private final MonthlyTradingSummaryRepository monthlyTradingSummaryRepository;

	// === 로그인 / 보안 ===
	private final PasswordHistoryRepository passwordHistoryRepository;

	// === 트레이너 배정 ===
	private final CustomerAssignmentRepository customerAssignmentRepository;
	private final ConsultationRepository consultationRepository;

	@Transactional
	@Override
	public void updateUserStatus(Long userId, UserStatus newStatus) {
		Customer customer = customerRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 승인 또는 거절 상태만 변경 허용
		if (newStatus != UserStatus.UID_APPROVED && newStatus != UserStatus.UID_REJECTED) {
			throw new UserException(UserErrorStatus.INVALID_STATUS_CHANGE);
		}

		customer.setUserStatus(newStatus);

		if (newStatus == UserStatus.UID_APPROVED) {
			createFreeOTLectures(customer);
		}
	}

	@Override
	public void giveUserTokens(Long userId, GiveUserTokenRequestDTO request) {
		Customer customer = customerRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// ✅ 기존 토큰에 누적하여 부여 (교체가 아닌 추가)
		customer.addToken(request.getTokenCount());
		log.info("Tokens granted: userId={}, amount={}, totalTokens={}",
			userId, request.getTokenCount(), customer.getToken());
	}

	@Override
	public void updateUserUid(Long userId, String uidValue) {
		// 1) 고객 존재 확인
		Customer customer = customerRepository.findById(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

		// 2) 해당 고객의 Uid 엔티티 조회 (없으면 예외 또는 생성)
		Uid uid = uidRepository.findByCustomerId(userId)
			.orElseThrow(() -> new UserException(UserErrorStatus.UID_NOT_FOUND));

		// 3) 값 변경
		uid.setUid(uidValue);   // or uid.updateUid(uidValue);

		// JPA 변경감지로 자동 flush
	}

	private void createFreeOTLectures(Customer customer) {
		Long customerId = customer.getId();

		// 1) OT 무료 강의 조회 (ChapterType.FREE + requiredToken = 0)
		List<Lecture> otLectures = lectureRepository
				.findFreeLecturesByChapterType(ChapterType.REGULAR);

		if (otLectures.isEmpty()) {
			return;
		}

		// 2) 각 강의별 LectureProgress가 없으면 생성
		for (Lecture lecture : otLectures) {
			boolean exists = lectureProgressRepository
					.existsByLectureIdAndCustomerId(lecture.getId(), customerId);

			if (!exists) {
				lectureProgressRepository.save(
						LectureProgress.builder()
								.lecture(lecture)
								.customer(customer)
								.watchedSeconds(0)
								.isCompleted(false)
								.build()
				);
			}
		}
	}

	@Transactional
	public void deleteUserHard(Long customerId) {

		// 0) 존재 여부 체크
		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new UserException(UserErrorStatus.USER_NOT_FOUND));

		// 1) 피드백 관련 삭제
		feedbackRequestAttachmentRepository.deleteByCustomerId(customerId);
		feedbackRequestRepository.deleteByCustomerId(customerId);
		investmentHistoryRepository.deleteByCustomerId(customerId);

		// 2) 레벨테스트 관련 삭제
		levelTestResponseRepository.deleteByCustomerId(customerId);
		levelTestAttemptRepository.deleteByCustomerId(customerId);

		// 3) 강의 진행도 삭제
		lectureProgressRepository.deleteByCustomerId(customerId);
		lectureAttachmentDownloadHistoryRepository.deleteByCustomerId(customerId);
		customerAssignmentRepository.deleteByCustomerId(customerId);

		// 4) 민원 삭제
		complaintRepository.deleteByCustomerId(customerId);

		// 5) 투자성향 관련
		investmentHistoryRepository.deleteByCustomerId(customerId);
		investmentTypeChangeRequestRepository.deleteByCustomerId(customerId);

		// 6) 리뷰 관련
		reviewAttachmentRepository.deleteByCustomerId(customerId);
		reviewTagMappingRepository.deleteByCustomerId(customerId);
		reviewRepository.deleteByCustomerId(customerId);

		// 7) 구독/결제
		billingRequestRepository.deleteByCustomerId(customerId);
		paymentMethodRepository.deleteByCustomerId(customerId);
		paymentRepository.deleteByCustomerId(customerId);
		subscriptionRepository.deleteByCustomerId(customerId);

		// 9) UID & 로그인 정보
		passwordHistoryRepository.deleteByCustomerId(customerId);
		uidRepository.deleteByCustomerId(customerId);



		// 11) 마지막으로 고객 삭제
		customerRepository.deleteById(customerId);

		// user 엔티티가 있다면 이것도 삭제
		userRepository.deleteById(customerId);
	}
}
