package com.tradingpt.tpt_api.domain.leveltest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLevelTestAttempt is a Querydsl query type for LevelTestAttempt
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLevelTestAttempt extends EntityPathBase<LevelTestAttempt> {

    private static final long serialVersionUID = 1832415537L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLevelTestAttempt levelTestAttempt = new QLevelTestAttempt("levelTestAttempt");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final EnumPath<com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestGrade> grade = createEnum("grade", com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestGrade.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestStaus> status = createEnum("status", com.tradingpt.tpt_api.domain.leveltest.enums.LeveltestStaus.class);

    public final NumberPath<Integer> totalScore = createNumber("totalScore", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLevelTestAttempt(String variable) {
        this(LevelTestAttempt.class, forVariable(variable), INITS);
    }

    public QLevelTestAttempt(Path<? extends LevelTestAttempt> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLevelTestAttempt(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLevelTestAttempt(PathMetadata metadata, PathInits inits) {
        this(LevelTestAttempt.class, metadata, inits);
    }

    public QLevelTestAttempt(Class<? extends LevelTestAttempt> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
    }

}

