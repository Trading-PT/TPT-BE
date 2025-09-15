package com.tradingpt.tpt_api.domain.monthlytrainerevaluation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMonthlyTrainerEvaluation is a Querydsl query type for MonthlyTrainerEvaluation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMonthlyTrainerEvaluation extends EntityPathBase<MonthlyTrainerEvaluation> {

    private static final long serialVersionUID = 1945819646L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMonthlyTrainerEvaluation monthlyTrainerEvaluation = new QMonthlyTrainerEvaluation("monthlyTrainerEvaluation");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath monthlyFinalEvaluation = createString("monthlyFinalEvaluation");

    public final com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.QMonthlyTradingSummary monthlyTradingSummary;

    public final StringPath tradingGoal = createString("tradingGoal");

    public final com.tradingpt.tpt_api.domain.user.entity.QTrainer trainer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMonthlyTrainerEvaluation(String variable) {
        this(MonthlyTrainerEvaluation.class, forVariable(variable), INITS);
    }

    public QMonthlyTrainerEvaluation(Path<? extends MonthlyTrainerEvaluation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMonthlyTrainerEvaluation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMonthlyTrainerEvaluation(PathMetadata metadata, PathInits inits) {
        this(MonthlyTrainerEvaluation.class, metadata, inits);
    }

    public QMonthlyTrainerEvaluation(Class<? extends MonthlyTrainerEvaluation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.monthlyTradingSummary = inits.isInitialized("monthlyTradingSummary") ? new com.tradingpt.tpt_api.domain.monthlytradingsummary.entity.QMonthlyTradingSummary(forProperty("monthlyTradingSummary")) : null;
        this.trainer = inits.isInitialized("trainer") ? new com.tradingpt.tpt_api.domain.user.entity.QTrainer(forProperty("trainer")) : null;
    }

}

