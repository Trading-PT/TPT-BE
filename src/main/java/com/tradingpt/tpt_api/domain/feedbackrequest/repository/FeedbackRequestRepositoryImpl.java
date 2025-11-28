package com.tradingpt.tpt_api.domain.feedbackrequest.repository;

import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest.*;
import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest.*;
import static com.tradingpt.tpt_api.domain.user.entity.QCustomer.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection.DailyPnlProjection;
import com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection.TradeRnRData;
import com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint;
import com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status;
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
 * Single Table 전략으로 통합된 QueryDSL 구현
 */
@RequiredArgsConstructor
public class FeedbackRequestRepositoryImpl implements FeedbackRequestRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Slice<FeedbackRequest> findAllFeedbackRequestsSlice(Pageable pageable) {
		List<FeedbackRequest> allResults = queryFactory
			.selectFrom(feedbackRequest)
			.fetch();

		return createSlice(allResults, pageable);
	}

	/**
	 * 피드백 목록을 정렬하고 Slice로 변환하는 공통 메서드
	 * 정렬: 베스트 피드백 우선 (최대 4개) → 생성일시 내림차순
	 */
	private Slice<FeedbackRequest> createSlice(
		List<FeedbackRequest> allResults,
		Pageable pageable
	) {
		// 1단계: 베스트 피드백과 일반 피드백 분리
		List<FeedbackRequest> bestFeedbacks = allResults.stream()
			.filter(fr -> fr.getIsBestFeedback() != null && fr.getIsBestFeedback())
			.sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
			.limit(MAX_BEST_FEEDBACK_COUNT)
			.toList();

		List<FeedbackRequest> regularFeedbacks = allResults.stream()
			.filter(fr -> fr.getIsBestFeedback() == null || !fr.getIsBestFeedback())
			.sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
			.toList();

		// 2단계: 베스트 피드백 + 일반 피드백 순서로 결합
		List<FeedbackRequest> sortedResults = new ArrayList<>();
		sortedResults.addAll(bestFeedbacks);
		sortedResults.addAll(regularFeedbacks);

		// 3단계: Slice 생성을 위한 페이징 처리
		int start = Math.min((int)pageable.getOffset(), sortedResults.size());
		int end = Math.min(start + pageable.getPageSize(), sortedResults.size());

		boolean hasNext = end < sortedResults.size();

		List<FeedbackRequest> sliceContent = sortedResults.subList(start, end);

		return new SliceImpl<>(sliceContent, pageable, hasNext);
	}

	@Override
	public Slice<FeedbackRequest> findAllFeedbacksByCreatedAtDesc(Pageable pageable) {
		List<FeedbackRequest> allResults = queryFactory
			.selectFrom(feedbackRequest)
			.orderBy(feedbackRequest.createdAt.desc())
			.fetch();

		int start = Math.min((int)pageable.getOffset(), allResults.size());
		int end = Math.min(start + pageable.getPageSize(), allResults.size());
		boolean hasNext = end < allResults.size();
		List<FeedbackRequest> sliceContent = allResults.subList(start, end);

		return new SliceImpl<>(sliceContent, pageable, hasNext);
	}

	@Override
	public List<FeedbackRequest> findFeedbackRequestsByCustomerAndDate(Long customerId, LocalDate feedbackDate) {
		return queryFactory
			.selectFrom(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackRequestDate.eq(feedbackDate)
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

		return projections.stream()
			.map(p -> new MonthlyFeedbackSummary(
				p.getMonth(),
				p.getTotalCount(),
				FeedbackStatusUtil.determineReadStatus(p.getFnCount())
			))
			.toList();
	}

	@Override
	public long countRequestsByCustomerAndDateAndType(
		Long customerId,
		LocalDate feedbackDate,
		InvestmentType investmentType
	) {
		Long count = queryFactory
			.select(feedbackRequest.count())
			.from(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackRequestDate.eq(feedbackDate),
				feedbackRequest.investmentType.eq(investmentType)
			)
			.fetchOne();

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
		BooleanBuilder predicate = new BooleanBuilder()
			.and(feedbackRequest.customer.id.eq(customerId))
			.and(feedbackRequest.feedbackYear.eq(year))
			.and(feedbackRequest.feedbackMonth.eq(month))
			.and(feedbackRequest.courseStatus.eq(courseStatus))
			.and(feedbackRequest.investmentType.eq(investmentType));

		NumberExpression<Integer> winCase = new CaseBuilder()
			.when(feedbackRequest.pnl.gt(BigDecimal.ZERO))
			.then(1)
			.otherwise(0);

		NumberExpression<Integer> nCase = new CaseBuilder()
			.when(feedbackRequest.status.eq(Status.N))
			.then(1)
			.otherwise(0);

		NumberExpression<Integer> fnCase = new CaseBuilder()
			.when(feedbackRequest.status.eq(Status.FN))
			.then(1)
			.otherwise(0);

		return queryFactory
			.select(Projections.constructor(
				WeeklyRawData.class,
				feedbackRequest.feedbackWeek,
				feedbackRequest.count().intValue(),
				feedbackRequest.totalAssetPnl.sum().coalesce(BigDecimal.ZERO),
				winCase.sum().coalesce(0),
				feedbackRequest.riskTaking.sum().coalesce(BigDecimal.ZERO).castToNum(BigDecimal.class),
				nCase.sum().coalesce(0),
				fnCase.sum().coalesce(0)
			))
			.from(feedbackRequest)
			.where(predicate)
			.groupBy(feedbackRequest.feedbackWeek)
			.orderBy(feedbackRequest.feedbackWeek.asc())
			.fetch();
	}

	@Override
	public EntryPointStatistics findEntryPointStatistics(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	) {
		// courseStatus 필터 제거: 해당 월의 모든 피드백을 대상으로 통계
		// (완강 전/후는 Service에서 분기 처리되므로 여기서는 필터링 불필요)
		BooleanBuilder basePredicate = new BooleanBuilder()
			.and(feedbackRequest.customer.id.eq(customerId))
			.and(feedbackRequest.feedbackYear.eq(year))
			.and(feedbackRequest.feedbackMonth.eq(month))
			.and(feedbackRequest.investmentType.eq(investmentType));

		// 승률 계산: totalAssetPnl > 0 기준 (전체 자산 기준 P&L)
		NumberExpression<Integer> winCase = new CaseBuilder()
			.when(feedbackRequest.totalAssetPnl.gt(BigDecimal.ZERO))
			.then(1)
			.otherwise(0);

		// 수익 매매의 rnr 합계 (totalAssetPnl > 0인 경우만 rnr 합산)
		NumberExpression<Double> winningRnrCase = new CaseBuilder()
			.when(feedbackRequest.totalAssetPnl.gt(BigDecimal.ZERO))
			.then(feedbackRequest.rnr)
			.otherwise((Double) null);

		var reverseStats = queryFactory
			.select(
				feedbackRequest.count().intValue(),
				winCase.sum().coalesce(0),
				winningRnrCase.sum().coalesce(0.0)
			)
			.from(feedbackRequest)
			.where(basePredicate.and(feedbackRequest.entryPoint.eq(EntryPoint.REVERSE)))
			.fetchOne();

		var pullBackStats = queryFactory
			.select(
				feedbackRequest.count().intValue(),
				winCase.sum().coalesce(0),
				winningRnrCase.sum().coalesce(0.0)
			)
			.from(feedbackRequest)
			.where(basePredicate.and(feedbackRequest.entryPoint.eq(EntryPoint.PULL_BACK)))
			.fetchOne();

		var breakOutStats = queryFactory
			.select(
				feedbackRequest.count().intValue(),
				winCase.sum().coalesce(0),
				winningRnrCase.sum().coalesce(0.0)
			)
			.from(feedbackRequest)
			.where(basePredicate.and(feedbackRequest.entryPoint.eq(EntryPoint.BREAK_OUT)))
			.fetchOne();

		return new EntryPointStatistics(
			// REVERSE
			reverseStats != null ? reverseStats.get(0, Integer.class) : 0,
			TradingCalculationUtil.calculateWinRate(
				reverseStats != null ? reverseStats.get(0, Integer.class) : 0,
				reverseStats != null ? reverseStats.get(1, Integer.class) : 0
			),
			calculateAverageWinningRnr(
				reverseStats != null ? reverseStats.get(2, Double.class) : 0.0,
				reverseStats != null ? reverseStats.get(1, Integer.class) : 0
			),
			// PULL_BACK
			pullBackStats != null ? pullBackStats.get(0, Integer.class) : 0,
			TradingCalculationUtil.calculateWinRate(
				pullBackStats != null ? pullBackStats.get(0, Integer.class) : 0,
				pullBackStats != null ? pullBackStats.get(1, Integer.class) : 0
			),
			calculateAverageWinningRnr(
				pullBackStats != null ? pullBackStats.get(2, Double.class) : 0.0,
				pullBackStats != null ? pullBackStats.get(1, Integer.class) : 0
			),
			// BREAK_OUT
			breakOutStats != null ? breakOutStats.get(0, Integer.class) : 0,
			TradingCalculationUtil.calculateWinRate(
				breakOutStats != null ? breakOutStats.get(0, Integer.class) : 0,
				breakOutStats != null ? breakOutStats.get(1, Integer.class) : 0
			),
			calculateAverageWinningRnr(
				breakOutStats != null ? breakOutStats.get(2, Double.class) : 0.0,
				breakOutStats != null ? breakOutStats.get(1, Integer.class) : 0
			)
		);
	}

	/**
	 * 수익 매매의 평균 RnR 계산
	 * @param winningRnrSum 수익 매매의 rnr 합계
	 * @param winCount 수익 매매 개수
	 * @return 평균 RnR (수익 매매가 없으면 0.0)
	 */
	private Double calculateAverageWinningRnr(Double winningRnrSum, Integer winCount) {
		if (winCount == null || winCount == 0 || winningRnrSum == null) {
			return 0.0;
		}
		return winningRnrSum / winCount;
	}

	@Override
	public MonthlyPerformanceSnapshot findMonthlyPerformance(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	) {
		BooleanBuilder predicate = new BooleanBuilder()
			.and(feedbackRequest.customer.id.eq(customerId))
			.and(feedbackRequest.feedbackYear.eq(year))
			.and(feedbackRequest.feedbackMonth.eq(month))
			.and(feedbackRequest.investmentType.eq(investmentType));

		NumberExpression<Integer> winCase = new CaseBuilder()
			.when(feedbackRequest.pnl.gt(BigDecimal.ZERO))
			.then(1)
			.otherwise(0);

		var result = queryFactory
			.select(
				feedbackRequest.count().intValue(),
				winCase.sum().coalesce(0),
				feedbackRequest.totalAssetPnl.sum().coalesce(BigDecimal.ZERO),
				feedbackRequest.riskTaking.sum().coalesce(BigDecimal.ZERO).castToNum(BigDecimal.class)
			)
			.from(feedbackRequest)
			.where(predicate)
			.fetchOne();

		return buildPerformanceSnapshot(result);
	}

	@Override
	public Optional<FeedbackRequest> findFirstByFeedbackYearAndFeedbackMonth(Long customerId, Integer year,
		Integer month) {
		FeedbackRequest result = queryFactory
			.selectFrom(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackYear.eq(year),
				feedbackRequest.feedbackMonth.eq(month)
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
		BooleanBuilder predicate = new BooleanBuilder()
			.and(feedbackRequest.customer.id.eq(customerId))
			.and(feedbackRequest.feedbackYear.eq(year))
			.and(feedbackRequest.feedbackMonth.eq(month))
			.and(feedbackRequest.feedbackWeek.eq(week))
			.and(feedbackRequest.courseStatus.eq(courseStatus))
			.and(feedbackRequest.investmentType.eq(investmentType));

		NumberExpression<Integer> winCase = new CaseBuilder()
			.when(feedbackRequest.pnl.gt(BigDecimal.ZERO))
			.then(1)
			.otherwise(0);

		NumberExpression<Integer> nCase = new CaseBuilder()
			.when(feedbackRequest.status.eq(Status.N))
			.then(1)
			.otherwise(0);

		NumberExpression<Integer> fnCase = new CaseBuilder()
			.when(feedbackRequest.status.eq(Status.FN))
			.then(1)
			.otherwise(0);

		return queryFactory
			.select(Projections.constructor(
				DailyRawData.class,
				feedbackRequest.feedbackRequestDate,
				feedbackRequest.count().intValue(),
				feedbackRequest.totalAssetPnl.sum().coalesce(BigDecimal.ZERO),
				winCase.sum().coalesce(0),
				feedbackRequest.riskTaking.sum().coalesce(BigDecimal.ZERO).castToNum(BigDecimal.class),
				nCase.sum().coalesce(0),
				fnCase.sum().coalesce(0)
			))
			.from(feedbackRequest)
			.where(predicate)
			.groupBy(feedbackRequest.feedbackRequestDate)
			.orderBy(feedbackRequest.feedbackRequestDate.asc())
			.fetch();
	}

	@Override
	public WeeklyPerformanceSnapshot findWeeklyPerformance(
		Long customerId,
		Integer year,
		Integer month,
		Integer week,
		InvestmentType investmentType
	) {
		BooleanBuilder predicate = new BooleanBuilder()
			.and(feedbackRequest.customer.id.eq(customerId))
			.and(feedbackRequest.feedbackYear.eq(year))
			.and(feedbackRequest.feedbackMonth.eq(month))
			.and(feedbackRequest.feedbackWeek.eq(week))
			.and(feedbackRequest.investmentType.eq(investmentType));

		NumberExpression<Integer> winCase = new CaseBuilder()
			.when(feedbackRequest.pnl.gt(BigDecimal.ZERO))
			.then(1)
			.otherwise(0);

		var result = queryFactory
			.select(
				feedbackRequest.count().intValue(),
				winCase.sum().coalesce(0),
				feedbackRequest.totalAssetPnl.sum().coalesce(BigDecimal.ZERO),
				feedbackRequest.riskTaking.sum().coalesce(BigDecimal.ZERO).castToNum(BigDecimal.class)
			)
			.from(feedbackRequest)
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

	@Override
	public DirectionStatistics findDirectionStatistics(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	) {
		// DAY 타입의 완강 후 피드백만 해당 (directionFrameExists가 null이 아닌 것)
		BooleanBuilder basePredicate = new BooleanBuilder()
			.and(feedbackRequest.customer.id.eq(customerId))
			.and(feedbackRequest.feedbackYear.eq(year))
			.and(feedbackRequest.feedbackMonth.eq(month))
			.and(feedbackRequest.feedbackWeek.eq(week))
			.and(feedbackRequest.investmentType.eq(InvestmentType.DAY))
			.and(feedbackRequest.directionFrameExists.isNotNull());

		NumberExpression<Integer> winCase = new CaseBuilder()
			.when(feedbackRequest.pnl.gt(BigDecimal.ZERO))
			.then(1)
			.otherwise(0);

		// 방향성 O 통계
		var directionOStats = queryFactory
			.select(
				feedbackRequest.count().intValue(),
				winCase.sum().coalesce(0),
				feedbackRequest.totalAssetPnl.sum().coalesce(BigDecimal.ZERO),
				feedbackRequest.riskTaking.sum().coalesce(BigDecimal.ZERO).castToNum(BigDecimal.class)
			)
			.from(feedbackRequest)
			.where(basePredicate.and(feedbackRequest.directionFrameExists.eq(true)))
			.fetchOne();

		// 방향성 X 통계
		var directionXStats = queryFactory
			.select(
				feedbackRequest.count().intValue(),
				winCase.sum().coalesce(0),
				feedbackRequest.totalAssetPnl.sum().coalesce(BigDecimal.ZERO),
				feedbackRequest.riskTaking.sum().coalesce(BigDecimal.ZERO).castToNum(BigDecimal.class)
			)
			.from(feedbackRequest)
			.where(basePredicate.and(feedbackRequest.directionFrameExists.eq(false)))
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

	@Override
	public boolean existsByCustomerIdAndYearAndMonthAndCourseStatus(
		Long customerId,
		Integer year,
		Integer month,
		CourseStatus courseStatus
	) {
		Integer exists = queryFactory
			.selectOne()
			.from(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackYear.eq(year),
				feedbackRequest.feedbackMonth.eq(month),
				feedbackRequest.courseStatus.eq(courseStatus)
			)
			.fetchFirst();

		return exists != null;
	}

	@Override
	public List<FeedbackRequest> findByCustomerIdAndDate(
		Long customerId,
		LocalDate targetDate
	) {
		return queryFactory
			.selectFrom(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackRequestDate.eq(targetDate)
			)
			.orderBy(feedbackRequest.createdAt.asc())
			.fetch();
	}

	@Override
	public List<DailyPnlProjection> findDailyPnlByCustomerIdAndYearAndMonth(
		Long customerId,
		Integer year,
		Integer month
	) {
		LocalDate startDate = LocalDate.of(year, month, 1);
		LocalDate endDate = startDate.plusMonths(1).minusDays(1);

		return queryFactory
			.select(Projections.constructor(
				DailyPnlProjection.class,
				feedbackRequest.feedbackRequestDate,
				feedbackRequest.totalAssetPnl.sum(),
				new CaseBuilder()
					.when(feedbackRequest.totalAssetPnl.gt(BigDecimal.ZERO))
					.then(1L)
					.otherwise(0L)
					.sum(),
				feedbackRequest.count()
			))
			.from(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackRequestDate.between(startDate, endDate)
			)
			.groupBy(feedbackRequest.feedbackRequestDate)
			.orderBy(feedbackRequest.feedbackRequestDate.asc())
			.fetch();
	}

	@Override
	public Slice<FeedbackRequest> findTokenUsedFeedbackRequests(Pageable pageable) {
		List<FeedbackRequest> content = queryFactory
			.selectFrom(feedbackRequest)
			.where(feedbackRequest.isTokenUsed.isTrue())
			.orderBy(feedbackRequest.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = content.size() > pageable.getPageSize();

		if (hasNext) {
			content = content.subList(0, pageable.getPageSize());
		}

		return new SliceImpl<>(content, pageable, hasNext);
	}

	@Override
	public Slice<FeedbackRequest> findNewFeedbackRequestsByTrainer(
		Long trainerId,
		Pageable pageable
	) {
		List<FeedbackRequest> content = queryFactory
			.selectFrom(feedbackRequest)
			.join(feedbackRequest.customer, customer).fetchJoin()
			.where(
				customer.assignedTrainer.id.eq(trainerId),
				feedbackRequest.status.eq(Status.N)
			)
			.orderBy(feedbackRequest.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)
			.fetch();

		boolean hasNext = content.size() > pageable.getPageSize();

		if (hasNext) {
			content = content.subList(0, pageable.getPageSize());
		}

		return new SliceImpl<>(content, pageable, hasNext);
	}

	@Override
	public List<Integer> findWeeksByCustomerIdAndYearAndMonth(
		Long customerId,
		Integer year,
		Integer month
	) {
		return queryFactory
			.select(feedbackRequest.feedbackWeek)
			.from(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackYear.eq(year),
				feedbackRequest.feedbackMonth.eq(month)
			)
			.distinct()
			.orderBy(feedbackRequest.feedbackWeek.asc())
			.fetch();
	}

	@Override
	public List<Integer> findDaysByCustomerIdAndYearAndMonthAndWeek(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	) {
		return queryFactory
			.select(feedbackRequest.feedbackRequestDate.dayOfMonth())
			.from(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackYear.eq(year),
				feedbackRequest.feedbackMonth.eq(month),
				feedbackRequest.feedbackWeek.eq(week)
			)
			.distinct()
			.orderBy(feedbackRequest.feedbackRequestDate.dayOfMonth().asc())
			.fetch();
	}

	@Override
	public List<FeedbackRequest> findProfitFeedbacksByCustomerAndYearAndMonthAndWeek(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	) {
		return queryFactory
			.selectFrom(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackYear.eq(year),
				feedbackRequest.feedbackMonth.eq(month),
				feedbackRequest.feedbackWeek.eq(week),
				feedbackRequest.pnl.gt(BigDecimal.ZERO)
			)
			.orderBy(feedbackRequest.feedbackRequestDate.desc())
			.fetch();
	}

	@Override
	public List<FeedbackRequest> findLossFeedbacksByCustomerAndYearAndMonthAndWeek(
		Long customerId,
		Integer year,
		Integer month,
		Integer week
	) {
		return queryFactory
			.selectFrom(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackYear.eq(year),
				feedbackRequest.feedbackMonth.eq(month),
				feedbackRequest.feedbackWeek.eq(week),
				feedbackRequest.totalAssetPnl.loe(BigDecimal.ZERO)
			)
			.orderBy(feedbackRequest.feedbackRequestDate.desc())
			.fetch();
	}

	@Override
	public Slice<FeedbackRequest> findTrainerWrittenFeedbacks(Pageable pageable) {
		List<FeedbackRequest> content = queryFactory
			.selectFrom(feedbackRequest)
			.leftJoin(feedbackRequest.feedbackRequestAttachments).fetchJoin()
			.where(feedbackRequest.isTrainerWritten.isTrue())
			.orderBy(feedbackRequest.createdAt.desc())
			.distinct()
			.fetch();

		// 메모리 페이징 (fetchJoin으로 인해)
		int start = Math.min((int)pageable.getOffset(), content.size());
		int end = Math.min(start + pageable.getPageSize() + 1, content.size());

		List<FeedbackRequest> pagedContent = start >= content.size()
			? new ArrayList<>()
			: content.subList(start, end);

		boolean hasNext = pagedContent.size() > pageable.getPageSize();

		if (hasNext) {
			pagedContent = pagedContent.subList(0, pageable.getPageSize());
		}

		return new SliceImpl<>(pagedContent, pageable, hasNext);
	}

	@Override
	public List<TradeRnRData> findWinningTradesForWeeklySummary(
		Long customerId,
		Integer year,
		Integer month,
		CourseStatus courseStatus,
		InvestmentType investmentType
	) {
		return queryFactory
			.select(Projections.constructor(
				TradeRnRData.class,
				feedbackRequest.pnl,
				feedbackRequest.riskTaking
			))
			.from(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackYear.eq(year),
				feedbackRequest.feedbackMonth.eq(month),
				feedbackRequest.courseStatus.eq(courseStatus),
				feedbackRequest.investmentType.eq(investmentType),
				feedbackRequest.pnl.gt(BigDecimal.ZERO)
			)
			.fetch();
	}

	@Override
	public List<TradeRnRData> findWinningTradesForMonthlySummary(
		Long customerId,
		Integer year,
		Integer month,
		InvestmentType investmentType
	) {
		return queryFactory
			.select(Projections.constructor(
				TradeRnRData.class,
				feedbackRequest.pnl,
				feedbackRequest.riskTaking
			))
			.from(feedbackRequest)
			.where(
				feedbackRequest.customer.id.eq(customerId),
				feedbackRequest.feedbackYear.eq(year),
				feedbackRequest.feedbackMonth.eq(month),
				feedbackRequest.investmentType.eq(investmentType),
				feedbackRequest.pnl.gt(BigDecimal.ZERO)
			)
			.fetch();
	}

	private MonthlyPerformanceSnapshot buildPerformanceSnapshot(com.querydsl.core.Tuple result) {
		if (result == null) {
			return new MonthlyPerformanceSnapshot(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
		}

		Integer totalCount = result.get(0, Integer.class);
		Integer winCount = result.get(1, Integer.class);
		BigDecimal totalPnl = result.get(2, BigDecimal.class);
		BigDecimal totalRiskTaking = result.get(3, BigDecimal.class);

		BigDecimal winRate = totalCount > 0
			? BigDecimal.valueOf((double)winCount / totalCount * 100).setScale(2, RoundingMode.HALF_UP)
			: BigDecimal.ZERO;
		BigDecimal avgRnr = totalRiskTaking.compareTo(BigDecimal.ZERO) > 0
			? totalPnl.divide(totalRiskTaking, 2, RoundingMode.HALF_UP)
			: BigDecimal.ZERO;

		return new MonthlyPerformanceSnapshot(winRate, avgRnr, totalPnl);
	}

}
