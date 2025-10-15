package com.tradingpt.tpt_api.domain.user.repository;

import static com.tradingpt.tpt_api.domain.user.entity.QTrainer.trainer;
import static com.tradingpt.tpt_api.domain.user.entity.QUser.user;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TrainerRepositoryImpl implements TrainerRepositoryCustom {

    private final JPAQueryFactory query;

    @Override
    public List<TrainerRow> findTrainerListRows() {
        return query
                .select(Projections.constructor(
                        TrainerRow.class,
                        trainer.id,
                        user.name,
                        user.username,
                        trainer.phoneNumber,
                        trainer.onelineIntroduction,
                        user.profileImageUrl
                ))
                .from(trainer)
                .join(user).on(user.id.eq(trainer.id))
                .orderBy(user.name.asc())
                .fetch();
    }
}
