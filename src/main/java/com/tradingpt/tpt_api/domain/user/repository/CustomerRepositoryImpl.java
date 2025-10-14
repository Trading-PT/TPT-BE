package com.tradingpt.tpt_api.domain.user.repository;

import static com.tradingpt.tpt_api.domain.payment.entity.QPaymentMethod.*;
import static com.tradingpt.tpt_api.domain.user.entity.QCustomer.*;
import static com.tradingpt.tpt_api.domain.user.entity.QUid.*;

import com.querydsl.core.Tuple;
import com.tradingpt.tpt_api.domain.user.dto.response.TrainerListResponseDTO.AssignedCustomerDTO;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.Set;
import org.springframework.stereotype.Repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
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
			.leftJoin(customer.paymentMethods, paymentMethod1).fetchJoin()
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
}
