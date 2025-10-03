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
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.EntryPointStatisticsResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyFeedbackSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyFeedbackSummaryResult;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyPerformanceComparison;
import com.tradingpt.tpt_api.domain.monthlytradingsummary.dto.response.MonthlyWeekFeedbackSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import com.tradingpt.tpt_api.domain.user.enums.InvestmentType;

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

	@Override
	public List<CourseStatus> findUniqueCourseStatusByYearMonth(Long customerId, Integer year, Integer month) {
		return queryFactory
			.select(qFeedbackRequest.courseStatus)
			.from(qFeedbackRequest)
			.where(
				qFeedbackRequest.customer.id.eq(customerId)
					.and(qFeedbackRequest.feedbackYear.eq(year))
					.and(qFeedbackRequest.feedbackMonth.eq(month))
			)
			.distinct()
			.fetch();
	}

	@Override
	public MonthlyFeedbackSummaryResponseDTO findMonthlyFeedbackSummaryByCourseStatus(
		Long customerId, Integer year, Integer month, CourseStatus courseStatus) {

		NumberExpression<Integer> unreadCase = new CaseBuilder()
			.when(qFeedbackRequest.status.eq(Status.FN))
			.then(1)
			.otherwise(0);

		NumberExpression<Integer> pendingCase = new CaseBuilder()
			.when(qFeedbackRequest.status.eq(Status.N))
			.then(1)
			.otherwise(0);

		var result = queryFactory
			.select(Projections.constructor(
				MonthlyFeedbackSummaryResult.class,
				qFeedbackRequest.feedbackMonth,
				qFeedbackRequest.count(),
				unreadCase.sum().coalesce(0),
				pendingCase.sum().coalesce(0)
			))
			.from(qFeedbackRequest)
			.where(
				qFeedbackRequest.customer.id.eq(customerId)
					.and(qFeedbackRequest.feedbackYear.eq(year))
					.and(qFeedbackRequest.feedbackMonth.eq(month))
					.and(qFeedbackRequest.courseStatus.eq(courseStatus))
			)
			.fetchOne();

		if (result == null) {
			result = new MonthlyFeedbackSummaryResult(month, 0L, 0, 0);
		}

		return MonthlyFeedbackSummaryResponseDTO.builder()
			.weekFeedbackSummaryResponseDTOS(List.of()) // 임시값
			.winningRate(0.0) // 임시값
			.monthlyAverageRnr(0.0) // 임시값
			.monthlyPnl(java.math.BigDecimal.ZERO) // 임시값
			.build();
	}

	@Override
	public EntryPointStatisticsResponseDTO findEntryPointStatisticsByCourseStatus(
		Long customerId, Integer year, Integer month, CourseStatus courseStatus, InvestmentType investmentType) {

		// 스윙과 데이 트레이딩에만 적용
		if (investmentType == InvestmentType.SWING) {
			var results = queryFactory
				.select(
					qSwingRequestDetail.entryPoint1,
					qSwingRequestDetail.count()
				)
				.from(qSwingRequestDetail)
				.where(
					qSwingRequestDetail.customer.id.eq(customerId)
						.and(qSwingRequestDetail.feedbackYear.eq(year))
						.and(qSwingRequestDetail.feedbackMonth.eq(month))
						.and(qSwingRequestDetail.courseStatus.eq(courseStatus))
				)
				.groupBy(qSwingRequestDetail.entryPoint1)
				.fetch();

			// 통계 계산 및 DTO 구성
			return buildEntryPointStatistics(results);

		} else if (investmentType == InvestmentType.DAY) {
			var results = queryFactory
				.select(
					qDayRequestDetail.entryPoint1,
					qDayRequestDetail.count()
				)
				.from(qDayRequestDetail)
				.where(
					qDayRequestDetail.customer.id.eq(customerId)
						.and(qDayRequestDetail.feedbackYear.eq(year))
						.and(qDayRequestDetail.feedbackMonth.eq(month))
						.and(qDayRequestDetail.courseStatus.eq(courseStatus))
				)
				.groupBy(qDayRequestDetail.entryPoint1)
				.fetch();

			return buildEntryPointStatistics(results);
		}

		// 스캘핑은 진입 타점 통계가 없음
		return EntryPointStatisticsResponseDTO.builder()
			.reverse(EntryPointStatisticsResponseDTO.PositionDetail.builder().count(0).winCount(0).lossCount(0).build())
			.pullBack(
				EntryPointStatisticsResponseDTO.PositionDetail.builder().count(0).winCount(0).lossCount(0).build())
			.breakOut(
				EntryPointStatisticsResponseDTO.PositionDetail.builder().count(0).winCount(0).lossCount(0).build())
			.build();
	}

	@Override
	public MonthlyPerformanceComparison findMonthlyPerformanceComparison(
		Long customerId, Integer year, Integer month, CourseStatus courseStatus) {

		// 현재 달 성과 계산
		MonthlyPerformanceComparison.MonthSnapshot currentSnapshot = calculateMonthSnapshot(customerId, year, month,
			courseStatus);

		// 이전 달 성과 계산
		int prevYear = month == 1 ? year - 1 : year;
		int prevMonth = month == 1 ? 12 : month - 1;
		MonthlyPerformanceComparison.MonthSnapshot prevSnapshot = calculateMonthSnapshot(customerId, prevYear,
			prevMonth, courseStatus);

		return MonthlyPerformanceComparison.builder()
			.currentMonth(currentSnapshot)
			.beforeMonth(prevSnapshot)
			.build();
	}

	public List<MonthlyWeekFeedbackSummaryResponseDTO> findWeeklyStatisticsByCourseStatusTemp(
		Long customerId, Integer year, Integer month, CourseStatus courseStatus, InvestmentType investmentType) {

		// 스캘핑 전용 - 주차별 피드백 요청 수와 미확인 여부 조회
		if (investmentType == InvestmentType.SCALPING) {
			// 임시 구현: 주차별 통계를 수동으로 생성 (실제 구현 시 QueryDSL로 변경 필요)
			return List.of(
				MonthlyWeekFeedbackSummaryResponseDTO.builder()
					.week(1)
					.tradingCount(0)
					.weeklyPnl(java.math.BigDecimal.ZERO)
					.build()
			);
		}

		return List.of();
	}

	private EntryPointStatisticsResponseDTO buildEntryPointStatistics(List<com.querydsl.core.Tuple> results) {
		int reverseCount = 0;
		int pullBackCount = 0;
		int breakOutCount = 0;

		for (com.querydsl.core.Tuple tuple : results) {
			com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint entryPoint =
				tuple.get(0, com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint.class);
			Long count = tuple.get(1, Long.class);

			// EntryPoint enum에 따라 분류 (실제 EntryPoint enum 값을 확인하여 분류해야 함)
			// 임시로 모든 값을 breakOut으로 분류
			breakOutCount += count.intValue();
		}

		return EntryPointStatisticsResponseDTO.builder()
			.reverse(EntryPointStatisticsResponseDTO.PositionDetail.builder()
				.count(reverseCount)
				.winCount(0)
				.lossCount(0)
				.build())
			.pullBack(EntryPointStatisticsResponseDTO.PositionDetail.builder()
				.count(pullBackCount)
				.winCount(0)
				.lossCount(0)
				.build())
			.breakOut(EntryPointStatisticsResponseDTO.PositionDetail.builder()
				.count(breakOutCount)
				.winCount(0)
				.lossCount(0)
				.build())
			.build();
	}

	private MonthlyPerformanceComparison.MonthSnapshot calculateMonthSnapshot(Long customerId, Integer year,
		Integer month, CourseStatus courseStatus) {
		// 실제 성과 계산 로직 구현 필요
		// 이 부분은 비즈니스 로직에 따라 구현해야 합니다.

		// 임시 구현: 0값으로 초기화
		return MonthlyPerformanceComparison.MonthSnapshot.builder()
			.month(month)
			.finalWinRate(java.math.BigDecimal.ZERO)
			.averageRoi(java.math.BigDecimal.ZERO)
			.finalPnL(java.math.BigDecimal.ZERO)
			.build();
	}

}
