package com.tradingpt.tpt_api.domain.subscription.repository;

import static com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest.*;
import static com.tradingpt.tpt_api.domain.subscription.entity.QSubscription.*;
import static com.tradingpt.tpt_api.domain.user.entity.QCustomer.*;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.subscription.dto.response.SubscriptionCustomerResponseDTO;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.user.entity.QUser;

import lombok.RequiredArgsConstructor;

/**
 * Subscription 커스텀 리포지토리 구현체
 * QueryDSL을 사용한 구독 고객 조회 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	/**
	 * 활성 구독 고객 목록 조회 (슬라이스 방식)
	 *
	 * 조회 항목:
	 * - 고객 ID, 이름, 전화번호
	 * - 배정된 트레이너 이름
	 *
	 * 정렬: 구독 생성일 최신순
	 * 필터: status = ACTIVE, trainerId 옵션
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

		// 기본 구독 정보 조회 (limit + 1 방식으로 hasNext 판단)
		List<SubscriptionCustomerResponseDTO> content = queryFactory
			.select(Projections.constructor(
				SubscriptionCustomerResponseDTO.class,
				customer.id,
				customer.username,
				customer.phoneNumber,
				trainer.name,
				JPAExpressions
					.select(feedbackRequest.count())
					.from(feedbackRequest)
					.where(feedbackRequest.customer.id.eq(customer.id))
			))
			.from(subscription)
			.innerJoin(subscription.customer, customer)
			.leftJoin(customer.assignedTrainer, trainer)
			.where(
				subscription.status.eq(Status.ACTIVE),
				trainerIdFilter(trainerId)
			)
			.orderBy(getOrderSpecifiers())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize() + 1)  // +1로 hasNext 판단
			.fetch();

		// 빈 결과 체크
		if (content.isEmpty()) {
			return new SliceImpl<>(content, pageable, false);
		}

		// hasNext 판단 및 마지막 요소 제거
		boolean hasNext = false;
		if (content.size() > pageable.getPageSize()) {
			hasNext = true;
			content.remove(pageable.getPageSize());
		}

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
		// Trainer alias 생성 (trainerIdFilter에서 사용)
		QUser trainer = new QUser("trainer");

		Long count = queryFactory
			.select(subscription.count())
			.from(subscription)
			.innerJoin(subscription.customer, customer)
			.leftJoin(customer.assignedTrainer, trainer)
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
	 * 구독 생성일 내림차순 (최신 순)
	 */
	private OrderSpecifier<?>[] getOrderSpecifiers() {
		return new OrderSpecifier<?>[] {
			subscription.createdAt.desc()
		};
	}
}
