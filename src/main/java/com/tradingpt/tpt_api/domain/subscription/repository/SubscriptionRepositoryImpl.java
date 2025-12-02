package com.tradingpt.tpt_api.domain.subscription.repository;

import static com.tradingpt.tpt_api.domain.subscription.entity.QSubscription.*;
import static com.tradingpt.tpt_api.domain.subscriptionplan.entity.QSubscriptionPlan.*;
import static com.tradingpt.tpt_api.domain.user.entity.QCustomer.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.lecture.entity.QCustomerAssignment;
import com.tradingpt.tpt_api.domain.lecture.entity.QLectureProgress;
import com.tradingpt.tpt_api.domain.leveltest.entity.QLevelTestAttempt;
import com.tradingpt.tpt_api.domain.leveltest.enums.LevelTestStaus;
import com.tradingpt.tpt_api.domain.subscription.dto.response.CustomerAggregationDTO;
import com.tradingpt.tpt_api.domain.subscription.dto.response.SubscriptionCustomerResponseDTO;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;

/**
 * Subscription 커스텀 리포지토리 구현체
 * QueryDSL을 사용한 복잡한 조회 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	/**
	 * 활성 구독 고객 목록 조회 (슬라이스 방식 + Batch Fetching)
	 *
	 * 주요 특징:
	 * - N+1 문제 방지: Batch fetching (1+1 query pattern)
	 * - 1차 쿼리: 기본 구독 정보 조회
	 * - 2차 쿼리: 고객별 집계 데이터 batch 조회
	 * - 정렬: membershipLevel DESC, createdAt DESC
	 * - 필터: status = ACTIVE, trainerId 옵션
	 *
	 * @param trainerId 트레이너 ID (null이면 전체 조회)
	 * @param pageable 페이징 정보
	 * @return 구독 고객 슬라이스
	 */
	@Override
	public Slice<SubscriptionCustomerResponseDTO> findActiveSubscriptionCustomers(
		Long trainerId,
		Pageable pageable
	) {
		// Trainer alias 생성 (고객에게 배정된 트레이너 조회용)
		QUser trainer = new QUser("trainer");

		// 1. 기본 구독 정보 조회 (limit + 1 방식으로 hasNext 판단)
		List<SubscriptionCustomerResponseDTO> content = queryFactory
			.select(Projections.constructor(
				SubscriptionCustomerResponseDTO.class,
				customer.id,
				customer.username,
				customer.phoneNumber,
				customer.primaryInvestmentType,
				customer.membershipLevel,
				subscription.status,
				subscriptionPlan.name,
				subscription.nextBillingDate,
				subscription.currentPeriodEnd,
				trainer.name
			))
			.from(subscription)
			.innerJoin(subscription.customer, customer)
			.innerJoin(subscription.subscriptionPlan, subscriptionPlan)
			.leftJoin(customer.assignedTrainer, trainer)
			.where(
				subscription.status.eq(Status.ACTIVE),
				trainerIdFilter(trainerId)
			)
			.orderBy(
				getOrderSpecifiers()
			)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)  // +1로 hasNext 판단
			.fetch();

		// 2. 빈 결과 체크
		if (content.isEmpty()) {
			return new SliceImpl<>(content, pageable, false);
		}

		// 3. hasNext 판단 및 마지막 요소 제거
		boolean hasNext = false;
		if (content.size() > pageable.getPageSize()) {
			hasNext = true;
			content.remove(pageable.getPageSize());
		}

		// 4. 고객 ID 추출
		List<Long> customerIds = content.stream()
			.map(SubscriptionCustomerResponseDTO::getCustomerId)
			.distinct()
			.collect(Collectors.toList());

		// 5. 집계 데이터 batch fetching
		Map<Long, CustomerAggregationDTO> aggregations = fetchCustomerAggregations(customerIds);

		// 6. 집계 데이터 enrichment
		content.forEach(dto -> dto.enrichWithAggregations(aggregations.get(dto.getCustomerId())));

		return new SliceImpl<>(content, pageable, hasNext);
	}

	/**
	 * 활성 구독 고객 총 인원 수 조회
	 *
	 * @param trainerId 트레이너 ID (null이면 전체 조회)
	 * @return 활성 구독 고객 총 인원 수
	 */
	@Override
	public Long countActiveSubscriptionCustomers(Long trainerId) {
		Long count = queryFactory
			.select(subscription.count())
			.from(subscription)
			.innerJoin(subscription.customer, customer)
			.where(
				subscription.status.eq(Status.ACTIVE),
				trainerIdFilter(trainerId)
			)
			.fetchOne();

		return count != null ? count : 0L;
	}

	/**
	 * 트레이너 필터 조건
	 * trainerId가 null이면 전체 조회
	 */
	private BooleanExpression trainerIdFilter(Long trainerId) {
		return trainerId != null ? customer.assignedTrainer.id.eq(trainerId) : null;
	}

	/**
	 * 정렬 조건
	 * 1. membershipLevel DESC (PREMIUM이 먼저)
	 * 2. createdAt DESC (최신 순)
	 */
	private OrderSpecifier<?>[] getOrderSpecifiers() {
		return new OrderSpecifier<?>[] {
			customer.membershipLevel.desc(),
			subscription.createdAt.desc()
		};
	}

	/**
	 * 고객별 집계 데이터 배치 조회 (N+1 문제 방지)
	 *
	 * 성능 최적화 전략:
	 * - 단일 쿼리로 여러 고객의 집계 데이터 동시 조회
	 * - 6개의 서브쿼리를 사용하여 복잡한 집계 처리
	 * - IN 절로 여러 고객을 한 번에 조회
	 *
	 * 집계 항목:
	 * 1. 최근 레벨테스트 등급 (GRADED 상태, 최신순)
	 * 2. 최근 레벨테스트 점수
	 * 3. 전체 강의 진행 수
	 * 4. 완료한 강의 수 (isCompleted = true)
	 * 5. 전체 할당된 과제 수
	 * 6. 미제출 과제 수 (submitted = false)
	 *
	 * @param customerIds 조회할 고객 ID 목록
	 * @return 고객 ID를 키로 하는 집계 데이터 맵
	 */
	private Map<Long, CustomerAggregationDTO> fetchCustomerAggregations(List<Long> customerIds) {
		if (customerIds == null || customerIds.isEmpty()) {
			return Map.of();
		}

		// 서브쿼리용 alias Q-class 생성
		QLevelTestAttempt subLevelTest = new QLevelTestAttempt("subLevelTest");
		QLectureProgress subProgress = new QLectureProgress("subProgress");
		QCustomerAssignment subAssignment = new QCustomerAssignment("subAssignment");

		List<CustomerAggregationDTO> aggregations = queryFactory
			.select(Projections.constructor(
				CustomerAggregationDTO.class,
				customer.id,

				// 1. 최근 레벨테스트 등급 (가장 최근 GRADED 상태)
				JPAExpressions
					.select(subLevelTest.grade)
					.from(subLevelTest)
					.where(
						subLevelTest.customer.id.eq(customer.id),
						subLevelTest.status.eq(LevelTestStaus.GRADED)
					)
					.orderBy(subLevelTest.createdAt.desc())
					.limit(1),

				// 2. 최근 레벨테스트 점수
				JPAExpressions
					.select(subLevelTest.totalScore)
					.from(subLevelTest)
					.where(
						subLevelTest.customer.id.eq(customer.id),
						subLevelTest.status.eq(LevelTestStaus.GRADED)
					)
					.orderBy(subLevelTest.createdAt.desc())
					.limit(1),

				// 3. 전체 강의 진행 수
				JPAExpressions
					.select(subProgress.count())
					.from(subProgress)
					.where(subProgress.customer.id.eq(customer.id)),

				// 4. 완료한 강의 수 (isCompleted = true)
				JPAExpressions
					.select(subProgress.count())
					.from(subProgress)
					.where(
						subProgress.customer.id.eq(customer.id),
						subProgress.isCompleted.eq(true)
					),

				// 5. 전체 할당된 과제 수
				JPAExpressions
					.select(subAssignment.count())
					.from(subAssignment)
					.where(subAssignment.customer.id.eq(customer.id)),

				// 6. 미제출 과제 수 (submitted = false)
				JPAExpressions
					.select(subAssignment.count())
					.from(subAssignment)
					.where(
						subAssignment.customer.id.eq(customer.id),
						subAssignment.submitted.eq(false)
					)
			))
			.from(customer)
			.where(customer.id.in(customerIds))
			.fetch();

		// List를 Map으로 변환 (customerId를 키로)
		return aggregations.stream()
			.collect(Collectors.toMap(
				CustomerAggregationDTO::getCustomerId,
				Function.identity()
			));
	}
}
