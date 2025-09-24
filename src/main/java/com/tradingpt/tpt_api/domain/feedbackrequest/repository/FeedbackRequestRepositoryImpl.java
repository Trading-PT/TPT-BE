package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.DayRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.QDayRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.QScalpingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.QSwingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.ScalpingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.SwingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;

import lombok.RequiredArgsConstructor;

/**
 * FeedbackRequest 커스텀 Repository 구현체
 * QueryDSL을 사용한 동적 쿼리 구현
 */
@RequiredArgsConstructor
public class FeedbackRequestRepositoryImpl implements FeedbackRequestRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QFeedbackRequest qFeedbackRequest = QFeedbackRequest.feedbackRequest;

	@Override
	public Page<FeedbackRequest> findFeedbackRequestsWithFilters(
		Pageable pageable,
		FeedbackType feedbackType,
		Status status,
		Long customerId
	) {
		BooleanBuilder predicate = buildBasePredicate(status, customerId);

		List<FeedbackRequest> allResults = new ArrayList<>();

		// FeedbackType에 따라 해당하는 서브타입만 조회
		if (feedbackType == null || feedbackType == FeedbackType.DAY) {
			QDayRequestDetail qDay = QDayRequestDetail.dayRequestDetail;
			List<DayRequestDetail> dayRequests = queryFactory
				.selectFrom(qDay)
				.where(predicate)
				.orderBy(qDay.createdAt.desc())
				.fetch();
			allResults.addAll(dayRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SCALPING) {
			QScalpingRequestDetail qScalping = QScalpingRequestDetail.scalpingRequestDetail;
			List<ScalpingRequestDetail> scalpingRequests = queryFactory
				.selectFrom(qScalping)
				.where(predicate)
				.orderBy(qScalping.createdAt.desc())
				.fetch();
			allResults.addAll(scalpingRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SWING) {
			QSwingRequestDetail qSwing = QSwingRequestDetail.swingRequestDetail;
			List<SwingRequestDetail> swingRequests = queryFactory
				.selectFrom(qSwing)
				.where(predicate)
				.orderBy(qSwing.createdAt.desc())
				.fetch();
			allResults.addAll(swingRequests);
		}

		// 메모리에서 정렬 및 페이징
		allResults.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

		int start = Math.min((int)pageable.getOffset(), allResults.size());
		int end = Math.min(start + pageable.getPageSize(), allResults.size());
		List<FeedbackRequest> pageContent = allResults.subList(start, end);

		return new PageImpl<>(pageContent, pageable, allResults.size());
	}

	@Override
	public List<FeedbackRequest> findMyFeedbackRequests(
		Long customerId,
		FeedbackType feedbackType,
		Status status
	) {
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(qFeedbackRequest.customer.id.eq(customerId));

		if (status != null) {
			predicate.and(qFeedbackRequest.status.eq(status));
		}

		List<FeedbackRequest> allResults = new ArrayList<>();

		// FeedbackType에 따라 해당하는 서브타입만 조회
		if (feedbackType == null || feedbackType == FeedbackType.DAY) {
			QDayRequestDetail qDay = QDayRequestDetail.dayRequestDetail;
			List<DayRequestDetail> dayRequests = queryFactory
				.selectFrom(qDay)
				.where(predicate)
				.orderBy(qDay.createdAt.desc())
				.fetch();
			allResults.addAll(dayRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SCALPING) {
			QScalpingRequestDetail qScalping = QScalpingRequestDetail.scalpingRequestDetail;
			List<ScalpingRequestDetail> scalpingRequests = queryFactory
				.selectFrom(qScalping)
				.where(predicate)
				.orderBy(qScalping.createdAt.desc())
				.fetch();
			allResults.addAll(scalpingRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SWING) {
			QSwingRequestDetail qSwing = QSwingRequestDetail.swingRequestDetail;
			List<SwingRequestDetail> swingRequests = queryFactory
				.selectFrom(qSwing)
				.where(predicate)
				.orderBy(qSwing.createdAt.desc())
				.fetch();
			allResults.addAll(swingRequests);
		}

		// 메모리에서 정렬
		allResults.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
		return allResults;
	}

	/**
	 * 기본 조건을 위한 Predicate 빌드 (상태 및 고객 ID만)
	 */
	private BooleanBuilder buildBasePredicate(Status status, Long customerId) {
		BooleanBuilder predicate = new BooleanBuilder();

		// 상태 필터
		if (status != null) {
			predicate.and(qFeedbackRequest.status.eq(status));
		}

		// 고객 ID 필터 (트레이너만 사용 가능)
		if (customerId != null) {
			predicate.and(qFeedbackRequest.customer.id.eq(customerId));
		}

		return predicate;
	}

	@Override
	public long countDayRequestsByCustomerAndDate(Long customerId, LocalDate feedbackDate) {
		QDayRequestDetail qDay = QDayRequestDetail.dayRequestDetail;

		Long count = queryFactory
			.select(qDay.count())
			.from(qDay)
			.where(
				qDay.customer.id.eq(customerId)
					.and(qDay.feedbackRequestedAt.eq(feedbackDate))
			)
			.fetchOne();

		return count != null ? count : 0L;
	}
}
