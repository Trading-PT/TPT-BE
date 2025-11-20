package com.tradingpt.tpt_api.domain.user.repository;

import static com.tradingpt.tpt_api.domain.paymentmethod.entity.QPaymentMethod.*;
import static com.tradingpt.tpt_api.domain.subscription.entity.QSubscription.*;
import static com.tradingpt.tpt_api.domain.user.entity.QCustomer.*;
import static com.tradingpt.tpt_api.domain.user.entity.QUid.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.MembershipLevel;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<Customer> findCustomersWithUidByStatus(UserStatus status) {
		return queryFactory
			.selectDistinct(customer)
			.from(customer)
			.leftJoin(customer.uid, uid1).fetchJoin()
			.where(customer.userStatus.eq(status))
			.orderBy(customer.createdAt.desc())
			.fetch();
	}

	@Override
	public Optional<Customer> findWithBasicsAndPaymentMethodsById(Long id) {
		Customer result = queryFactory
			.selectDistinct(customer)
			.from(customer)
			.leftJoin(customer.assignedTrainer).fetchJoin()
			.leftJoin(customer.uid, uid1).fetchJoin()
			.leftJoin(customer.paymentMethods, paymentMethod).fetchJoin()
			.where(customer.id.eq(id))
			.fetchOne();
		return Optional.ofNullable(result);
	}

	@Override
	public Map<Long, List<AssignedCustomerInfo>> findAssignedMapByTrainerIds(Set<Long> trainerIds) {
		if (trainerIds == null || trainerIds.isEmpty())
			return Map.of();

		// QCustomer.customer.assignedTrainer 로 바뀐 점 주의!
		List<Tuple> rows = queryFactory
			.select(
				customer.assignedTrainer.id,  // key
				customer.id,
				customer.name
			)
			.from(customer)
			.where(customer.assignedTrainer.id.in(trainerIds))
			.orderBy(customer.assignedTrainer.id.asc(), customer.id.asc())
			.fetch();

		Map<Long, List<AssignedCustomerInfo>> map = new LinkedHashMap<>();
		for (Tuple t : rows) {
			Long tid = t.get(customer.assignedTrainer.id);
			AssignedCustomerInfo info = new AssignedCustomerInfo(
				t.get(customer.id),
				t.get(customer.name)
			);
			map.computeIfAbsent(tid, k -> new ArrayList<>()).add(info);
		}
		return map;
	}

	@Override
	public Map<Long, List<AssignedCustomerInfo>> findAssignedMapByAdminIds(Set<Long> userIds) {
		if (userIds == null || userIds.isEmpty())
			return Map.of();

		List<Tuple> rows = queryFactory
			.select(
				customer.assignedTrainer.id, // FK → User(Trainer/Admin 공통)
				customer.id,
				customer.name
			)
			.from(customer)
			.where(customer.assignedTrainer.id.in(userIds))
			.orderBy(customer.assignedTrainer.id.asc(), customer.id.asc())
			.fetch();

		Map<Long, List<AssignedCustomerInfo>> map = new LinkedHashMap<>();
		for (Tuple t : rows) {
			Long uid = t.get(customer.assignedTrainer.id);
			AssignedCustomerInfo info = new AssignedCustomerInfo(
				t.get(customer.id),
				t.get(customer.name)
			);
			map.computeIfAbsent(uid, k -> new ArrayList<>()).add(info);
		}
		return map;
	}

	/**
	 * 미구독(무료) 고객 목록 조회
	 *
	 * 미구독 고객 정의:
	 * 1. Subscription이 존재하지 않음 (LEFT JOIN으로 NULL 확인)
	 * 2. 또는 Subscription이 있지만 status가 ACTIVE가 아님
	 * 3. membershipLevel이 BASIC인 고객
	 * 4. 담당 트레이너가 없음 (assignedTrainer IS NULL)
	 *
	 * 성능 최적화 전략:
	 * - LEFT JOIN으로 Subscription 조회 (없을 수 있으므로)
	 * - Slice 방식: limit + 1로 hasNext 판단
	 *
	 * @param pageable 페이징 정보 (정렬 포함)
	 * @return 미구독 고객 Slice
	 */
	@Override
	public Slice<Customer> findFreeCustomers(Pageable pageable) {
		// 1. 미구독 고객 조회 (limit + 1 방식)
		List<Customer> content = queryFactory
			.selectDistinct(customer)
			.from(customer)
			.leftJoin(subscription).on(subscription.customer.eq(customer))
			.where(
				// 미구독 고객 조건
				isFreeCustomer(),
				// 담당 트레이너 없음
				customer.assignedTrainer.isNull()
			)
			.orderBy(getOrderSpecifiers(pageable.getSort()))
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

		return new SliceImpl<>(content, pageable, hasNext);
	}

	/**
	 * 미구독 고객 조건
	 * 1. Subscription이 없거나 (LEFT JOIN 결과 NULL)
	 * 2. Subscription이 있지만 status가 ACTIVE가 아님
	 * 3. membershipLevel이 BASIC
	 */
	private BooleanExpression isFreeCustomer() {
		return (subscription.isNull().or(subscription.status.ne(Status.ACTIVE)))
			.and(customer.membershipLevel.eq(MembershipLevel.BASIC));
	}

	/**
	 * 동적 정렬 조건 생성
	 * 지원 필드: createdAt, name, token (tokenCount)
	 * 기본값: createdAt DESC
	 */
	private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
		List<OrderSpecifier<?>> orders = new ArrayList<>();

		if (sort.isEmpty()) {
			// 기본 정렬: 최근 가입순
			orders.add(new OrderSpecifier<>(Order.DESC, customer.createdAt));
		} else {
			for (Sort.Order order : sort) {
				Order direction = order.isAscending() ? Order.ASC : Order.DESC;

				switch (order.getProperty()) {
					case "createdAt":
						orders.add(new OrderSpecifier<>(direction, customer.createdAt));
						break;
					case "name":
						orders.add(new OrderSpecifier<>(direction, customer.name));
						break;
					case "tokenCount":
					case "token":
						orders.add(new OrderSpecifier<>(direction, customer.token));
						break;
					default:
						// 지원하지 않는 정렬 필드는 무시하고 기본 정렬 적용
						orders.add(new OrderSpecifier<>(Order.DESC, customer.createdAt));
						break;
				}
			}
		}

		return orders.toArray(new OrderSpecifier[0]);
	}

	/**
	 * 신규 구독 고객 목록 조회
	 *
	 * 신규 구독 고객 정의:
	 * 1. ACTIVE 상태의 Subscription 보유
	 * 2. 다음 중 하나에 해당:
	 *    - Subscription.createdAt이 24시간 이내
	 *    - 트레이너가 아직 배정되지 않음 (assignedTrainer IS NULL)
	 *
	 * 성능 최적화 전략:
	 * - INNER JOIN으로 Subscription 조회 (필수 조건)
	 * - assignedTrainer fetch join (N+1 방지)
	 * - Slice 방식: limit + 1로 hasNext 판단
	 *
	 * @param pageable 페이징 정보 (정렬 포함)
	 * @return 신규 구독 고객 Slice
	 */
	@Override
	public Slice<Customer> findNewSubscriptionCustomers(Pageable pageable) {
		// 24시간 전 시점 계산
		LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

		// 1. 신규 구독 고객 조회 (limit + 1 방식)
		List<Customer> content = queryFactory
			.selectDistinct(customer)
			.from(customer)
			.innerJoin(subscription).on(subscription.customer.eq(customer))
			.leftJoin(customer.assignedTrainer).fetchJoin()
			.where(
				// 신규 구독 조건
				subscription.status.eq(Status.ACTIVE),
				// 24시간 이내 신규 구독 OR 트레이너 미배정
				subscription.createdAt.after(twentyFourHoursAgo)
					.or(customer.assignedTrainer.isNull())
			)
			.orderBy(subscription.createdAt.desc())  // 최신 구독순
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

		return new SliceImpl<>(content, pageable, hasNext);
	}
}
