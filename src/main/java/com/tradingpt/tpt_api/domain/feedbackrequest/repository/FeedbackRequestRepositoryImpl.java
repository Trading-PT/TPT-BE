package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.QDayRequestDetail.*;
import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest.*;
import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.QScalpingRequestDetail.*;
import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.QSwingRequestDetail.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
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
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;

import lombok.RequiredArgsConstructor;

/**
 * FeedbackRequest 커스텀 Repository 구현체
 * QueryDSL을 사용한 동적 쿼리 구현
 */
@RequiredArgsConstructor
public class FeedbackRequestRepositoryImpl implements FeedbackRequestRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QFeedbackRequest qFeedbackRequest = feedbackRequest;
	private final QDayRequestDetail qDayRequestDetail = dayRequestDetail;
	private final QScalpingRequestDetail qScalpingRequestDetail = scalpingRequestDetail;
	private final QSwingRequestDetail qSwingRequestDetail = swingRequestDetail;

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
			List<DayRequestDetail> dayRequests = queryFactory
				.selectFrom(qDayRequestDetail)
				.where(predicate)
				.orderBy(qDayRequestDetail.createdAt.desc())
				.fetch();
			allResults.addAll(dayRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SCALPING) {
			List<ScalpingRequestDetail> scalpingRequests = queryFactory
				.selectFrom(qScalpingRequestDetail)
				.where(predicate)
				.orderBy(qScalpingRequestDetail.createdAt.desc())
				.fetch();
			allResults.addAll(scalpingRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SWING) {
			List<SwingRequestDetail> swingRequests = queryFactory
				.selectFrom(qSwingRequestDetail)
				.where(predicate)
				.orderBy(qSwingRequestDetail.createdAt.desc())
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
			List<DayRequestDetail> dayRequests = queryFactory
				.selectFrom(qDayRequestDetail)
				.where(predicate)
				.orderBy(qDayRequestDetail.createdAt.desc())
				.fetch();
			allResults.addAll(dayRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SCALPING) {
			List<ScalpingRequestDetail> scalpingRequests = queryFactory
				.selectFrom(qScalpingRequestDetail)
				.where(predicate)
				.orderBy(qScalpingRequestDetail.createdAt.desc())
				.fetch();
			allResults.addAll(scalpingRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SWING) {
			List<SwingRequestDetail> swingRequests = queryFactory
				.selectFrom(qSwingRequestDetail)
				.where(predicate)
				.orderBy(qSwingRequestDetail.createdAt.desc())
				.fetch();
			allResults.addAll(swingRequests);
		}

		// 메모리에서 정렬
		allResults.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
		return allResults;
	}

	@Override
	public List<FeedbackRequest> findFeedbackRequestsByCustomerAndDate(Long customerId, LocalDate feedbackDate) {
		return queryFactory
			.selectFrom(qFeedbackRequest)
			.where(
				qFeedbackRequest.customer.id.eq(customerId)
					.and(qFeedbackRequest.feedbackRequestedAt.eq(feedbackDate))
			)
			.orderBy(qFeedbackRequest.createdAt.asc())
			.fetch();
	}

	@Override
	public List<MonthlyFeedbackSummaryResult> findMonthlySummaryByYear(Long customerId, Integer year) {
		NumberExpression<Integer> unreadCase = new CaseBuilder()
			.when(qFeedbackRequest.status.eq(Status.FN))
			.then(1)
			.otherwise(0);

		NumberExpression<Integer> pendingCase = new CaseBuilder()
			.when(qFeedbackRequest.status.eq(Status.N))
			.then(1)
			.otherwise(0);

		return queryFactory
			.select(Projections.constructor(
				MonthlyFeedbackSummaryResult.class,
				qFeedbackRequest.feedbackMonth,
				qFeedbackRequest.count(),
				unreadCase.sum().coalesce(0),
				pendingCase.sum().coalesce(0)
			))
			.from(qFeedbackRequest)
			.where(
				qFeedbackRequest.customer.id.eq(customerId),
				qFeedbackRequest.feedbackYear.eq(year)
			)
			.groupBy(qFeedbackRequest.feedbackMonth)
			.orderBy(qFeedbackRequest.feedbackMonth.asc())
			.fetch();
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
	public long countRequestsByCustomerAndDateAndType(
		Long customerId,
		LocalDate feedbackDate,
		FeedbackType feedbackType
	) {
		Long count;

		switch (feedbackType) {
			case DAY -> {
				count = queryFactory
					.select(qDayRequestDetail.count())
					.from(qDayRequestDetail)
					.where(
						qDayRequestDetail.customer.id.eq(customerId)
							.and(qDayRequestDetail.feedbackRequestedAt.eq(feedbackDate))
					)
					.fetchOne();
			}
			case SCALPING -> {
				count = queryFactory
					.select(qScalpingRequestDetail.count())
					.from(qScalpingRequestDetail)
					.where(
						qScalpingRequestDetail.customer.id.eq(customerId)
							.and(qScalpingRequestDetail.feedbackRequestedAt.eq(feedbackDate))
					)
					.fetchOne();
			}
			case SWING -> {
				count = queryFactory
					.select(qSwingRequestDetail.count())
					.from(qSwingRequestDetail)
					.where(
						qSwingRequestDetail.customer.id.eq(customerId)
							.and(qSwingRequestDetail.feedbackRequestedAt.eq(feedbackDate))
					)
					.fetchOne();
			}
			default -> throw new FeedbackRequestException(FeedbackRequestErrorStatus.UNSUPPORTED_REQUEST_FEEDBACK_TYPE);
		}

		return count != null ? count : 0L;
	}
}
