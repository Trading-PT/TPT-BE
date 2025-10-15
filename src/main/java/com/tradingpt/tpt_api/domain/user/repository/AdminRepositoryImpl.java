package com.tradingpt.tpt_api.domain.user.repository;

import static com.tradingpt.tpt_api.domain.user.entity.QAdmin.admin;
import static com.tradingpt.tpt_api.domain.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<AdminRow> findAdminListRows() {
        return query
                .select(Projections.constructor(
                        AdminRow.class,
                        admin.id,
                        user.name,
                        user.username,
                        admin.phoneNumber,
                        admin.onelineIntroduction,
                        user.profileImageUrl
                ))
                .from(admin)
                .join(user).on(user.id.eq(admin.id))
                .orderBy(user.name.asc())
                .fetch();
    }
}

