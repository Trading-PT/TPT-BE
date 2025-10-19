package com.tradingpt.tpt_api.domain.leveltest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLeveltestAttempt is a Querydsl query type for LeveltestAttempt
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLeveltestAttempt extends EntityPathBase<LeveltestAttempt> {

    private static final long serialVersionUID = 164546897L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLeveltestAttempt leveltestAttempt = new QLeveltestAttempt("leveltestAttempt");

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

    public QLeveltestAttempt(String variable) {
        this(LeveltestAttempt.class, forVariable(variable), INITS);
    }

    public QLeveltestAttempt(Path<? extends LeveltestAttempt> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLeveltestAttempt(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLeveltestAttempt(PathMetadata metadata, PathInits inits) {
        this(LeveltestAttempt.class, metadata, inits);
    }

    public QLeveltestAttempt(Class<? extends LeveltestAttempt> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
    }

}

