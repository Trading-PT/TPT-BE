package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.QDayRequestDetail.*;
import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest.*;
import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.QScalpingRequestDetail.*;
import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.QSwingRequestDetail.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.DayRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.ScalpingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.SwingRequestDetail;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestErrorStatus;
import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackStatusUtil;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.TradingCalculationUtil;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.EntryPointStatistics;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.MonthlyFeedbackProjection;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.MonthlyFeedbackSummary;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.MonthlyPerformanceSnapshot;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.projection.WeeklyRawData;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection.DailyRawData;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection.DirectionStatistics;
import com.tradingpt.tpt_api.domain.weeklytradingsummary.dto.projection.WeeklyPerformanceSnapshot;

import lombok.RequiredArgsConstructor;

/**
 * FeedbackRequest 커스텀 Repository 구현체
 * QueryDSL을 사용한 동적 쿼리 구현
 */
@RequiredArgsConstructor
public class FeedbackRequestRepositoryImpl implements FeedbackRequestRepositoryCustom {

	private final JPAQueryFactory queryFactory;

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
				.selectFrom(dayRequestDetail)
				.where(predicate)
				.orderBy(dayRequestDetail.createdAt.desc())
				.fetch();
			allResults.addAll(dayRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SCALPING) {
			List<ScalpingRequestDetail> scalpingRequests = queryFactory
				.selectFrom(scalpingRequestDetail)
				.where(predicate)
				.orderBy(scalpingRequestDetail.createdAt.desc())
				.fetch();
			allResults.addAll(scalpingRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SWING) {
			List<SwingRequestDetail> swingRequests = queryFactory
				.selectFrom(swingRequestDetail)
				.where(predicate)
				.orderBy(swingRequestDetail.createdAt.desc())
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
	public Slice<FeedbackRequest> findAllFeedbackRequestsSlice(Pageable pageable) {
		List<FeedbackRequest> allResults = new ArrayList<>();

		// 모든 타입의 피드백 조회
		List<DayRequestDetail> dayRequests = queryFactory
			.selectFrom(dayRequestDetail)
			.fetch();
		allResults.addAll(dayRequests);

		List<ScalpingRequestDetail> scalpingRequests = queryFactory
			.selectFrom(scalpingRequestDetail)
			.fetch();
		allResults.addAll(scalpingRequests);

		List<SwingRequestDetail> swingRequests = queryFactory
			.selectFrom(swingRequestDetail)
			.fetch();
		allResults.addAll(swingRequests);

		// 정렬 및 Slice 생성
		return createSlice(allResults, pageable);
	}

	/**
	 * 피드백 목록을 정렬하고 Slice로 변환하는 공통 메서드
	 * 정렬: 베스트 피드백 우선 → 생성일시 내림차순
	 */
	private Slice<FeedbackRequest> createSlice(
		List<FeedbackRequest> allResults,
		Pageable pageable
	) {
		// ✅ 정렬: 베스트 피드백 우선 → 생성일시 내림차순
		allResults.sort((a, b) -> {
			// 1. isBestFeedback 비교 (true가 먼저)
			int bestCompare = Boolean.compare(
				b.getIsBestFeedback() != null && b.getIsBestFeedback(),
				a.getIsBestFeedback() != null && a.getIsBestFeedback()
			);

			if (bestCompare != 0) {
				return bestCompare;
			}

			// 2. createdAt 비교 (최신이 먼저)
			return b.getCreatedAt().compareTo(a.getCreatedAt());
		});

		// ✅ Slice 생성을 위한 페이징 처리
		int start = Math.min((int)pageable.getOffset(), allResults.size());
		int end = Math.min(start + pageable.getPageSize(), allResults.size());

		// 다음 페이지 존재 여부 확인
		boolean hasNext = end < allResults.size();

		List<FeedbackRequest> sliceContent = allResults.subList(start, end);

		return new SliceImpl<>(sliceContent, pageable, hasNext);
	}

	@Override
	public List<FeedbackRequest> findMyFeedbackRequests(
		Long customerId,
		FeedbackType feedbackType,
		Status status
	) {
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.and(feedbackRequest.customer.id.eq(customerId));

		if (status != null) {
			predicate.and(feedbackRequest.status.eq(status));
		}

		List<FeedbackRequest> allResults = new ArrayList<>();

		// FeedbackType에 따라 해당하는 서브타입만 조회
		if (feedbackType == null || feedbackType == FeedbackType.DAY) {
			List<DayRequestDetail> dayRequests = queryFactory
				.selectFrom(dayRequestDetail)
				.where(predicate)
				.orderBy(dayRequestDetail.createdAt.desc())
				.fetch();
			allResults.addAll(dayRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SCALPING) {
			List<ScalpingRequestDetail> scalpingRequests = queryFactory
				.selectFrom(scalpingRequestDetail)
				.where(predicate)
				.orderBy(scalpingRequestDetail.createdAt.desc())
				.fetch();
			allResults.addAll(scalpingRequests);
		}

		if (feedbackType == null || feedbackType == FeedbackType.SWING) {
			List<SwingRequestDetail> swingRequests = queryFactory
				.selectFrom(swingRequestDetail)
				.where(predicate)
				.orderBy(swingRequestDetail.createdAt.desc())
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
			.selectFrom(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId)
					.and(feedbackRequest.feedbackRequestDate.eq(feedbackDate))
			)
			.orderBy(feedbackRequest.createdAt.asc())
			.fetch();
	}

	@Override
	public List<MonthlyFeedbackSummary> findMonthlySummaryByYear(Long customerId, Integer year) {

		NumberExpression<Integer> nCase = new CaseBuilder()
			.when(feedbackRequest.status.eq(Status.N))
			.then(1)
			.otherwise(0);

		NumberExpression<Integer> fnCase = new CaseBuilder()
			.when(feedbackRequest.status.eq(Status.FN))
			.then(1)
			.otherwise(0);

		// 1. Projection으로 조회
		List<MonthlyFeedbackProjection> projections = queryFactory
			.select(Projections.constructor(
				MonthlyFeedbackProjection.class,
				feedbackRequest.feedbackMonth,
				feedbackRequest.count(),
				nCase.sum().coalesce(0),
				fnCase.sum().coalesce(0)
			))
			.from(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackYear.eq(year)
			)
			.groupBy(feedbackRequest.feedbackMonth)
			.orderBy(feedbackRequest.feedbackMonth.asc())
			.fetch();

		// 2. 최종 DTO로 변환
		return projections.stream()
			.map(p -> new MonthlyFeedbackSummary(
				p.getMonth(),
				p.getTotalCount(),
				FeedbackStatusUtil.determineReadStatus(p.getFnCount())
			))
			.toList();
	}

	/**
	 * 기본 조건을 위한 Predicate 빌드 (상태 및 고객 ID만)
	 */
	private BooleanBuilder buildBasePredicate(Status status, Long customerId) {
		BooleanBuilder predicate = new BooleanBuilder();

		// 상태 필터
		if (status != null) {
			predicate.and(feedbackRequest.status.eq(status));
		}

		// 고객 ID 필터 (트레이너만 사용 가능)
		if (customerId != null) {
			predicate.and(feedbackRequest.customer.id.eq(customerId));
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
					.select(dayRequestDetail.count())
					.from(dayRequestDetail)
					.where(
						dayRequestDetail.customer.id.eq(customerId)
							.and(dayRequestDetail.feedbackRequestDate.eq(feedbackDate))
					)
					.fetchOne();
			}
			case SCALPING -> {
				count = queryFactory
					.select(scalpingRequestDetail.count())
					.from(scalpingRequestDetail)
					.where(
						scalpingRequestDetail.customer.id.eq(customerId)
							.and(scalpingRequestDetail.feedbackRequestDate.eq(feedbackDate))
					)
					.fetchOne();
			}
			case SWING -> {
				count = queryFactory
					.select(swingRequestDetail.count())
					.from(swingRequestDetail)
					.where(
						swingRequestDetail.customer.id.eq(customerId)
							.and(swingRequestDetail.feedbackRequestDate.eq(feedbackDate))
					)
					.fetchOne();
			}
			default -> throw new FeedbackRequestException(FeedbackRequestErrorStatus.UNSUPPORTED_REQUEST_FEEDBACK_TYPE);
		}

		return count != null ? count : 0L;
	}

	@Override
	public List<WeeklyRawData> findWeeklyStatistics(
		Long customerId,
		Integer year,
		Integer month,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		switch (investmentType) {
			case DAY -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(dayRequestDetail.customer.id.eq(customerId))
					.and(dayRequestDetail.feedbackYear.eq(year))
					.and(dayRequestDetail.feedbackMonth.eq(month))
					.and(dayRequestDetail.courseStatus.eq(courseStatus));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(dayRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				// ✅ Status 관련 CASE 추가
				NumberExpression<Integer> nCase = new CaseBuilder()
					.when(dayRequestDetail.status.eq(Status.N))
					.then(1)
					.otherwise(0);

				NumberExpression<Integer> fnCase = new CaseBuilder()
					.when(dayRequestDetail.status.eq(Status.FN))
					.then(1)
					.otherwise(0);

				return queryFactory
					.select(Projections.constructor(
						WeeklyRawData.class,
						dayRequestDetail.feedbackWeek,
						dayRequestDetail.count().intValue(),
						dayRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						winCase.sum().coalesce(0),
						dayRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class),
						nCase.sum().coalesce(0),    // ✅ 추가
						fnCase.sum().coalesce(0)    // ✅ 추가
					))
					.from(dayRequestDetail)
					.where(predicate)
					.groupBy(dayRequestDetail.feedbackWeek)
					.orderBy(dayRequestDetail.feedbackWeek.asc())
					.fetch();
			}
			case SWING -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(swingRequestDetail.customer.id.eq(customerId))
					.and(swingRequestDetail.feedbackYear.eq(year))
					.and(swingRequestDetail.feedbackMonth.eq(month))
					.and(swingRequestDetail.courseStatus.eq(courseStatus));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(swingRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				// ✅ Status 관련 CASE 추가
				NumberExpression<Integer> nCase = new CaseBuilder()
					.when(swingRequestDetail.status.eq(Status.N))
					.then(1)
					.otherwise(0);

				NumberExpression<Integer> fnCase = new CaseBuilder()
					.when(swingRequestDetail.status.eq(Status.FN))
					.then(1)
					.otherwise(0);

				return queryFactory
					.select(Projections.constructor(
						WeeklyRawData.class,
						swingRequestDetail.feedbackWeek,
						swingRequestDetail.count().intValue(),
						swingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						winCase.sum().coalesce(0),
						swingRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class),
						nCase.sum().coalesce(0),    // ✅ 추가
						fnCase.sum().coalesce(0)    // ✅ 추가
					))
					.from(swingRequestDetail)
					.where(predicate)
					.groupBy(swingRequestDetail.feedbackWeek)
					.orderBy(swingRequestDetail.feedbackWeek.asc())
					.fetch();
			}
			case SCALPING -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(scalpingRequestDetail.customer.id.eq(customerId))
					.and(scalpingRequestDetail.feedbackYear.eq(year))
					.and(scalpingRequestDetail.feedbackMonth.eq(month))
					.and(scalpingRequestDetail.courseStatus.eq(courseStatus));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(scalpingRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				// ✅ Status 관련 CASE 추가
				NumberExpression<Integer> nCase = new CaseBuilder()
					.when(scalpingRequestDetail.status.eq(Status.N))
					.then(1)
					.otherwise(0);

				NumberExpression<Integer> fnCase = new CaseBuilder()
					.when(scalpingRequestDetail.status.eq(Status.FN))
					.then(1)
					.otherwise(0);

				return queryFactory
					.select(Projections.constructor(
						WeeklyRawData.class,
						scalpingRequestDetail.feedbackWeek,
						scalpingRequestDetail.count().intValue(),
						scalpingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						winCase.sum().coalesce(0),
						scalpingRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class),
						nCase.sum().coalesce(0),    // ✅ 추가
						fnCase.sum().coalesce(0)    // ✅ 추가
					))
					.from(scalpingRequestDetail)
					.where(predicate)
					.groupBy(scalpingRequestDetail.feedbackWeek)
					.orderBy(scalpingRequestDetail.feedbackWeek.asc())
					.fetch();
			}
			default -> throw new FeedbackRequestException(FeedbackRequestErrorStatus.UNSUPPORTED_REQUEST_FEEDBACK_TYPE);
		}
	}

	@Override
	public EntryPointStatistics findEntryPointStatistics(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	) {
		if (investmentType == InvestmentType.DAY) {
			BooleanBuilder basePredicate = new BooleanBuilder()
				.and(dayRequestDetail.customer.id.eq(customerId))
				.and(dayRequestDetail.feedbackYear.eq(year))
				.and(dayRequestDetail.feedbackMonth.eq(month));

			NumberExpression<Integer> winCase = new CaseBuilder()
				.when(dayRequestDetail.pnl.gt(BigDecimal.ZERO))
				.then(1)
				.otherwise(0);

			var reverseStats = queryFactory
				.select(
					dayRequestDetail.count().intValue(),
					winCase.sum().coalesce(0),
					dayRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
					dayRequestDetail.riskTaking.sum().coalesce(0)
				)
				.from(dayRequestDetail)
				.where(basePredicate.and(dayRequestDetail.entryPoint1.eq(EntryPoint.REVERSE)))
				.fetchOne();

			var pullBackStats = queryFactory
				.select(
					dayRequestDetail.count().intValue(),
					winCase.sum().coalesce(0),
					dayRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
					dayRequestDetail.riskTaking.sum().coalesce(0)
				)
				.from(dayRequestDetail)
				.where(basePredicate.and(dayRequestDetail.entryPoint1.eq(EntryPoint.PULL_BACK)))
				.fetchOne();

			var breakOutStats = queryFactory
				.select(
					dayRequestDetail.count().intValue(),
					winCase.sum().coalesce(0),
					dayRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
					dayRequestDetail.riskTaking.sum().coalesce(0)
				)
				.from(dayRequestDetail)
				.where(basePredicate.and(dayRequestDetail.entryPoint1.eq(EntryPoint.BREAK_OUT)))
				.fetchOne();

			return new EntryPointStatistics(
				// REVERSE
				reverseStats != null ? reverseStats.get(0, Integer.class) : 0,
				TradingCalculationUtil.calculateWinRate(  // ✅ 승률 계산
					reverseStats != null ? reverseStats.get(0, Integer.class) : 0,
					reverseStats != null ? reverseStats.get(1, Integer.class) : 0
				),
				TradingCalculationUtil.calculateAverageRnR(  // ✅ Util 사용
					reverseStats != null ? reverseStats.get(2, BigDecimal.class) : BigDecimal.ZERO,
					reverseStats != null ? reverseStats.get(3, BigDecimal.class) : BigDecimal.ZERO
				),
				// PULL_BACK
				pullBackStats != null ? pullBackStats.get(0, Integer.class) : 0,
				TradingCalculationUtil.calculateWinRate(  // ✅ 승률 계산
					pullBackStats != null ? pullBackStats.get(0, Integer.class) : 0,
					pullBackStats != null ? pullBackStats.get(1, Integer.class) : 0
				),
				TradingCalculationUtil.calculateAverageRnR(  // ✅ Util 사용
					reverseStats != null ? reverseStats.get(2, BigDecimal.class) : BigDecimal.ZERO,
					reverseStats != null ? reverseStats.get(3, BigDecimal.class) : BigDecimal.ZERO
				),
				// BREAK_OUT
				breakOutStats != null ? breakOutStats.get(0, Integer.class) : 0,
				TradingCalculationUtil.calculateWinRate(  // ✅ 승률 계산
					breakOutStats != null ? breakOutStats.get(0, Integer.class) : 0,
					breakOutStats != null ? breakOutStats.get(1, Integer.class) : 0
				),
				TradingCalculationUtil.calculateAverageRnR(  // ✅ Util 사용
					reverseStats != null ? reverseStats.get(2, BigDecimal.class) : BigDecimal.ZERO,
					reverseStats != null ? reverseStats.get(3, BigDecimal.class) : BigDecimal.ZERO
				)
			);
		} else { // SWING
			BooleanBuilder basePredicate = new BooleanBuilder()
				.and(swingRequestDetail.customer.id.eq(customerId))
				.and(swingRequestDetail.feedbackYear.eq(year))
				.and(swingRequestDetail.feedbackMonth.eq(month));

			NumberExpression<Integer> winCase = new CaseBuilder()
				.when(swingRequestDetail.pnl.gt(BigDecimal.ZERO))
				.then(1)
				.otherwise(0);

			var reverseStats = queryFactory
				.select(
					swingRequestDetail.count().intValue(),
					winCase.sum().coalesce(0),
					swingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
					swingRequestDetail.riskTaking.sum().coalesce(0)
				)
				.from(swingRequestDetail)
				.where(basePredicate.and(swingRequestDetail.entryPoint1.eq(EntryPoint.REVERSE)))
				.fetchOne();

			var pullBackStats = queryFactory
				.select(
					swingRequestDetail.count().intValue(),
					winCase.sum().coalesce(0),
					swingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
					swingRequestDetail.riskTaking.sum().coalesce(0)
				)
				.from(swingRequestDetail)
				.where(basePredicate.and(swingRequestDetail.entryPoint1.eq(EntryPoint.PULL_BACK)))
				.fetchOne();

			var breakOutStats = queryFactory
				.select(
					swingRequestDetail.count().intValue(),
					winCase.sum().coalesce(0),
					swingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
					swingRequestDetail.riskTaking.sum().coalesce(0)
				)
				.from(swingRequestDetail)
				.where(basePredicate.and(swingRequestDetail.entryPoint1.eq(EntryPoint.BREAK_OUT)))
				.fetchOne();

			return new EntryPointStatistics(
				// REVERSE
				reverseStats != null ? reverseStats.get(0, Integer.class) : 0,
				TradingCalculationUtil.calculateWinRate(  // ✅ 승률 계산
					reverseStats != null ? reverseStats.get(0, Integer.class) : 0,
					reverseStats != null ? reverseStats.get(1, Integer.class) : 0
				),
				TradingCalculationUtil.calculateAverageRnR(  // ✅ Util 사용
					reverseStats != null ? reverseStats.get(2, BigDecimal.class) : BigDecimal.ZERO,
					reverseStats != null ? reverseStats.get(3, BigDecimal.class) : BigDecimal.ZERO
				),
				// PULL_BACK
				pullBackStats != null ? pullBackStats.get(0, Integer.class) : 0,
				TradingCalculationUtil.calculateWinRate(  // ✅ 승률 계산
					pullBackStats != null ? pullBackStats.get(0, Integer.class) : 0,
					pullBackStats != null ? pullBackStats.get(1, Integer.class) : 0
				),
				TradingCalculationUtil.calculateAverageRnR(  // ✅ Util 사용
					reverseStats != null ? reverseStats.get(2, BigDecimal.class) : BigDecimal.ZERO,
					reverseStats != null ? reverseStats.get(3, BigDecimal.class) : BigDecimal.ZERO
				),
				// BREAK_OUT
				breakOutStats != null ? breakOutStats.get(0, Integer.class) : 0,
				TradingCalculationUtil.calculateWinRate(  // ✅ 승률 계산
					breakOutStats != null ? breakOutStats.get(0, Integer.class) : 0,
					breakOutStats != null ? breakOutStats.get(1, Integer.class) : 0
				),
				TradingCalculationUtil.calculateAverageRnR(  // ✅ Util 사용
					reverseStats != null ? reverseStats.get(2, BigDecimal.class) : BigDecimal.ZERO,
					reverseStats != null ? reverseStats.get(3, BigDecimal.class) : BigDecimal.ZERO
				)
			);
		}
	}

	@Override
	public MonthlyPerformanceSnapshot findMonthlyPerformance(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	) {
		switch (investmentType) {
			case DAY -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(dayRequestDetail.customer.id.eq(customerId))
					.and(dayRequestDetail.feedbackYear.eq(year))
					.and(dayRequestDetail.feedbackMonth.eq(month));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(dayRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				var result = queryFactory
					.select(
						dayRequestDetail.count().intValue(),
						winCase.sum().coalesce(0),
						dayRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						dayRequestDetail.riskTaking.sum().coalesce(0)
					)
					.from(dayRequestDetail)
					.where(predicate)
					.fetchOne();

				return buildPerformanceSnapshot(result);
			}
			case SWING -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(swingRequestDetail.customer.id.eq(customerId))
					.and(swingRequestDetail.feedbackYear.eq(year))
					.and(swingRequestDetail.feedbackMonth.eq(month));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(swingRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				var result = queryFactory
					.select(
						swingRequestDetail.count().intValue(),
						winCase.sum().coalesce(0),
						swingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						swingRequestDetail.riskTaking.sum().coalesce(0)
					)
					.from(swingRequestDetail)
					.where(predicate)
					.fetchOne();

				return buildPerformanceSnapshot(result);
			}
			case SCALPING -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(scalpingRequestDetail.customer.id.eq(customerId))
					.and(scalpingRequestDetail.feedbackYear.eq(year))
					.and(scalpingRequestDetail.feedbackMonth.eq(month));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(scalpingRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				var result = queryFactory
					.select(
						scalpingRequestDetail.count().intValue(),
						winCase.sum().coalesce(0),
						scalpingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						scalpingRequestDetail.riskTaking.sum().coalesce(0)
					)
					.from(scalpingRequestDetail)
					.where(predicate)
					.fetchOne();

				return buildPerformanceSnapshot(result);
			}
			default -> throw new FeedbackRequestException(FeedbackRequestErrorStatus.UNSUPPORTED_REQUEST_FEEDBACK_TYPE);
		}
	}

	@Override
	public Optional<FeedbackRequest> findFirstByFeedbackYearAndFeedbackMonth(Long customerId, Integer year,
		Integer month) {
		FeedbackRequest result = queryFactory
			.selectFrom(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId)
					.and(feedbackRequest.feedbackYear.eq(year))
					.and(feedbackRequest.feedbackMonth.eq(month))
			)
			.orderBy(feedbackRequest.feedbackYear.desc(), feedbackRequest.feedbackMonth.desc())
			.fetchFirst();

		return Optional.ofNullable(result);
	}

	@Override
	public List<DailyRawData> findDailyStatistics(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		switch (investmentType) {
			case DAY -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(dayRequestDetail.customer.id.eq(customerId))
					.and(dayRequestDetail.feedbackYear.eq(year))
					.and(dayRequestDetail.feedbackMonth.eq(month))
					.and(dayRequestDetail.feedbackWeek.eq(week))
					.and(dayRequestDetail.courseStatus.eq(courseStatus));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(dayRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				NumberExpression<Integer> nCase = new CaseBuilder()
					.when(dayRequestDetail.status.eq(Status.N))
					.then(1)
					.otherwise(0);

				NumberExpression<Integer> fnCase = new CaseBuilder()
					.when(dayRequestDetail.status.eq(Status.FN))
					.then(1)
					.otherwise(0);

				return queryFactory
					.select(Projections.constructor(
						DailyRawData.class,
						dayRequestDetail.feedbackRequestDate,
						dayRequestDetail.count().intValue(),
						dayRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						winCase.sum().coalesce(0),
						dayRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class),
						nCase.sum().coalesce(0),
						fnCase.sum().coalesce(0)
					))
					.from(dayRequestDetail)
					.where(predicate)
					.groupBy(dayRequestDetail.feedbackRequestDate)
					.orderBy(dayRequestDetail.feedbackRequestDate.asc())
					.fetch();
			}
			case SWING -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(swingRequestDetail.customer.id.eq(customerId))
					.and(swingRequestDetail.feedbackYear.eq(year))
					.and(swingRequestDetail.feedbackMonth.eq(month))
					.and(swingRequestDetail.feedbackWeek.eq(week))
					.and(swingRequestDetail.courseStatus.eq(courseStatus));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(swingRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				NumberExpression<Integer> nCase = new CaseBuilder()
					.when(swingRequestDetail.status.eq(Status.N))
					.then(1)
					.otherwise(0);

				NumberExpression<Integer> fnCase = new CaseBuilder()
					.when(swingRequestDetail.status.eq(Status.FN))
					.then(1)
					.otherwise(0);

				return queryFactory
					.select(Projections.constructor(
						DailyRawData.class,
						swingRequestDetail.feedbackRequestDate,
						swingRequestDetail.count().intValue(),
						swingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						winCase.sum().coalesce(0),
						swingRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class),
						nCase.sum().coalesce(0),
						fnCase.sum().coalesce(0)
					))
					.from(swingRequestDetail)
					.where(predicate)
					.groupBy(swingRequestDetail.feedbackRequestDate)
					.orderBy(swingRequestDetail.feedbackRequestDate.asc())
					.fetch();
			}
			case SCALPING -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(scalpingRequestDetail.customer.id.eq(customerId))
					.and(scalpingRequestDetail.feedbackYear.eq(year))
					.and(scalpingRequestDetail.feedbackMonth.eq(month))
					.and(scalpingRequestDetail.feedbackWeek.eq(week))
					.and(scalpingRequestDetail.courseStatus.eq(courseStatus));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(scalpingRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				NumberExpression<Integer> nCase = new CaseBuilder()
					.when(scalpingRequestDetail.status.eq(Status.N))
					.then(1)
					.otherwise(0);

				NumberExpression<Integer> fnCase = new CaseBuilder()
					.when(scalpingRequestDetail.status.eq(Status.FN))
					.then(1)
					.otherwise(0);

				return queryFactory
					.select(Projections.constructor(
						DailyRawData.class,
						scalpingRequestDetail.feedbackRequestDate,
						scalpingRequestDetail.count().intValue(),
						scalpingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						winCase.sum().coalesce(0),
						scalpingRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class),
						nCase.sum().coalesce(0),
						fnCase.sum().coalesce(0)
					))
					.from(scalpingRequestDetail)
					.where(predicate)
					.groupBy(scalpingRequestDetail.feedbackRequestDate)
					.orderBy(scalpingRequestDetail.feedbackRequestDate.asc())
					.fetch();
			}
			default -> throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.UNSUPPORTED_REQUEST_FEEDBACK_TYPE
			);
		}
	}

	@Override
	public WeeklyPerformanceSnapshot findWeeklyPerformance(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		InvestmentType investmentType
	) {
		switch (investmentType) {
			case DAY -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(dayRequestDetail.customer.id.eq(customerId))
					.and(dayRequestDetail.feedbackYear.eq(year))
					.and(dayRequestDetail.feedbackMonth.eq(month))
					.and(dayRequestDetail.feedbackWeek.eq(week));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(dayRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				var result = queryFactory
					.select(
						dayRequestDetail.count().intValue(),
						winCase.sum().coalesce(0),
						dayRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						dayRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class)
					)
					.from(dayRequestDetail)
					.where(predicate)
					.fetchOne();

				if (result == null) {
					return new WeeklyPerformanceSnapshot(0.0, 0.0, BigDecimal.ZERO);
				}

				Integer totalCount = result.get(0, Integer.class);
				Integer winCount = result.get(1, Integer.class);
				BigDecimal totalPnl = result.get(2, BigDecimal.class);
				BigDecimal totalRiskTaking = result.get(3, BigDecimal.class);

				Double winRate = TradingCalculationUtil.calculateWinRate(totalCount, winCount);
				Double avgRnr = TradingCalculationUtil.calculateAverageRnR(totalPnl, totalRiskTaking);

				return new WeeklyPerformanceSnapshot(winRate, avgRnr, totalPnl);
			}
			case SWING -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(swingRequestDetail.customer.id.eq(customerId))
					.and(swingRequestDetail.feedbackYear.eq(year))
					.and(swingRequestDetail.feedbackMonth.eq(month))
					.and(swingRequestDetail.feedbackWeek.eq(week));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(swingRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				var result = queryFactory
					.select(
						swingRequestDetail.count().intValue(),
						winCase.sum().coalesce(0),
						swingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						swingRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class)
					)
					.from(swingRequestDetail)
					.where(predicate)
					.fetchOne();

				if (result == null) {
					return new WeeklyPerformanceSnapshot(0.0, 0.0, BigDecimal.ZERO);
				}

				Integer totalCount = result.get(0, Integer.class);
				Integer winCount = result.get(1, Integer.class);
				BigDecimal totalPnl = result.get(2, BigDecimal.class);
				BigDecimal totalRiskTaking = result.get(3, BigDecimal.class);

				Double winRate = TradingCalculationUtil.calculateWinRate(totalCount, winCount);
				Double avgRnr = TradingCalculationUtil.calculateAverageRnR(totalPnl, totalRiskTaking);

				return new WeeklyPerformanceSnapshot(winRate, avgRnr, totalPnl);
			}
			case SCALPING -> {
				BooleanBuilder predicate = new BooleanBuilder()
					.and(scalpingRequestDetail.customer.id.eq(customerId))
					.and(scalpingRequestDetail.feedbackYear.eq(year))
					.and(scalpingRequestDetail.feedbackMonth.eq(month))
					.and(scalpingRequestDetail.feedbackWeek.eq(week));

				NumberExpression<Integer> winCase = new CaseBuilder()
					.when(scalpingRequestDetail.pnl.gt(BigDecimal.ZERO))
					.then(1)
					.otherwise(0);

				var result = queryFactory
					.select(
						scalpingRequestDetail.count().intValue(),
						winCase.sum().coalesce(0),
						scalpingRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
						scalpingRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class)
					)
					.from(scalpingRequestDetail)
					.where(predicate)
					.fetchOne();

				if (result == null) {
					return new WeeklyPerformanceSnapshot(0.0, 0.0, BigDecimal.ZERO);
				}

				Integer totalCount = result.get(0, Integer.class);
				Integer winCount = result.get(1, Integer.class);
				BigDecimal totalPnl = result.get(2, BigDecimal.class);
				BigDecimal totalRiskTaking = result.get(3, BigDecimal.class);

				Double winRate = TradingCalculationUtil.calculateWinRate(totalCount, winCount);
				Double avgRnr = TradingCalculationUtil.calculateAverageRnR(totalPnl, totalRiskTaking);

				return new WeeklyPerformanceSnapshot(winRate, avgRnr, totalPnl);
			}
			default -> throw new FeedbackRequestException(
				FeedbackRequestErrorStatus.UNSUPPORTED_REQUEST_FEEDBACK_TYPE);
		}
	}

	@Override
	public DirectionStatistics findDirectionStatistics(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	) {
		// 완강 후 데이 트레이딩만 해당 (directionFrameExists가 null이 아닌 것)
		BooleanBuilder basePredicate = new BooleanBuilder()
			.and(dayRequestDetail.customer.id.eq(customerId))
			.and(dayRequestDetail.feedbackYear.eq(year))
			.and(dayRequestDetail.feedbackMonth.eq(month))
			.and(dayRequestDetail.feedbackWeek.eq(week))
			.and(dayRequestDetail.directionFrameExists.isNotNull());  // ✅ null 제외

		NumberExpression<Integer> winCase = new CaseBuilder()
			.when(dayRequestDetail.pnl.gt(BigDecimal.ZERO))
			.then(1)
			.otherwise(0);

		// 방향성 O 통계
		var directionOStats = queryFactory
			.select(
				dayRequestDetail.count().intValue(),
				winCase.sum().coalesce(0),
				dayRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
				dayRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class)
			)
			.from(dayRequestDetail)
			.where(basePredicate.and(dayRequestDetail.directionFrameExists.eq(true)))
			.fetchOne();

		// 방향성 X 통계
		var directionXStats = queryFactory
			.select(
				dayRequestDetail.count().intValue(),
				winCase.sum().coalesce(0),
				dayRequestDetail.pnl.sum().coalesce(BigDecimal.ZERO),
				dayRequestDetail.riskTaking.sum().coalesce(0).castToNum(BigDecimal.class)
			)
			.from(dayRequestDetail)
			.where(basePredicate.and(dayRequestDetail.directionFrameExists.eq(false)))
			.fetchOne();

		// 방향성 O 계산
		Integer directionOCount = directionOStats != null ? directionOStats.get(0, Integer.class) : 0;
		Integer directionOWinCount = directionOStats != null ? directionOStats.get(1, Integer.class) : 0;
		BigDecimal directionOPnl = directionOStats != null ? directionOStats.get(2, BigDecimal.class) : BigDecimal.ZERO;
		BigDecimal directionORiskTaking =
			directionOStats != null ? directionOStats.get(3, BigDecimal.class) : BigDecimal.ZERO;

		Double directionOWinRate = TradingCalculationUtil.calculateWinRate(directionOCount, directionOWinCount);
		Double directionORnr = TradingCalculationUtil.calculateAverageRnR(directionOPnl, directionORiskTaking);

		// 방향성 X 계산
		Integer directionXCount = directionXStats != null ? directionXStats.get(0, Integer.class) : 0;
		Integer directionXWinCount = directionXStats != null ? directionXStats.get(1, Integer.class) : 0;
		BigDecimal directionXPnl = directionXStats != null ? directionXStats.get(2, BigDecimal.class) : BigDecimal.ZERO;
		BigDecimal directionXRiskTaking =
			directionXStats != null ? directionXStats.get(3, BigDecimal.class) : BigDecimal.ZERO;

		Double directionXWinRate = TradingCalculationUtil.calculateWinRate(directionXCount, directionXWinCount);
		Double directionXRnr = TradingCalculationUtil.calculateAverageRnR(directionXPnl, directionXRiskTaking);

		return new DirectionStatistics(
			directionOCount,
			directionOWinRate,
			directionORnr,
			directionXCount,
			directionXWinRate,
			directionXRnr
		);
	}

	private MonthlyPerformanceSnapshot buildPerformanceSnapshot(com.querydsl.core.Tuple result) {
		if (result == null) {
			return new MonthlyPerformanceSnapshot(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		}

		Integer totalCount = result.get(0, Integer.class);
		Integer winCount = result.get(1, Integer.class);
		BigDecimal totalPnl = result.get(2, BigDecimal.class);
		Integer totalRiskTaking = result.get(3, Integer.class);

		BigDecimal winRate = totalCount > 0
			? BigDecimal.valueOf((double)winCount / totalCount * 100).setScale(2, RoundingMode.HALF_UP)
			: BigDecimal.ZERO;
		BigDecimal avgRnr = totalRiskTaking > 0
			? totalPnl.divide(BigDecimal.valueOf(totalRiskTaking), 2, RoundingMode.HALF_UP)
			: BigDecimal.ZERO;

		return new MonthlyPerformanceSnapshot(winRate, avgRnr, totalPnl);
	}

}
