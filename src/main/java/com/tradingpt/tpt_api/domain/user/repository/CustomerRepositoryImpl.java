package com.tradingpt.tpt_api.domain.user.repository;

import static com.tradingpt.tpt_api.domain.payment.entity.QPaymentMethod.paymentMethod1;
import static com.tradingpt.tpt_api.domain.user.entity.QCustomer.customer;
import static com.tradingpt.tpt_api.domain.user.entity.QUid.uid1;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
                .leftJoin(customer.trainer).fetchJoin()
                .leftJoin(customer.uid, uid1).fetchJoin()
                .leftJoin(customer.paymentMethods, paymentMethod1).fetchJoin()
                .where(customer.id.eq(id))
                .fetchOne();
        return Optional.ofNullable(result);
    }
}
