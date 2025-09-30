package com.tradingpt.tpt_api.domain.weeklytradingsummary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QWeeklyTradingSummary is a Querydsl query type for WeeklyTradingSummary
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWeeklyTradingSummary extends EntityPathBase<WeeklyTradingSummary> {

    private static final long serialVersionUID = 611419714L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QWeeklyTradingSummary weeklyTradingSummary = new QWeeklyTradingSummary("weeklyTradingSummary");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final DateTimePath<java.time.LocalDateTime> evaluatedAt = createDateTime("evaluatedAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.InvestmentType> investmentType = createEnum("investmentType", com.tradingpt.tpt_api.domain.user.enums.InvestmentType.class);

    public final QWeeklyPeriod period;

    public final com.tradingpt.tpt_api.domain.user.entity.QTrainer trainer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath weeklyEvaluation = createString("weeklyEvaluation");

    public final StringPath weeklyLossTradingAnalysis = createString("weeklyLossTradingAnalysis");

    public final StringPath weeklyProfitableTradingAnalysis = createString("weeklyProfitableTradingAnalysis");

    public QWeeklyTradingSummary(String variable) {
        this(WeeklyTradingSummary.class, forVariable(variable), INITS);
    }

    public QWeeklyTradingSummary(Path<? extends WeeklyTradingSummary> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QWeeklyTradingSummary(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QWeeklyTradingSummary(PathMetadata metadata, PathInits inits) {
        this(WeeklyTradingSummary.class, metadata, inits);
    }

    public QWeeklyTradingSummary(Class<? extends WeeklyTradingSummary> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.period = inits.isInitialized("period") ? new QWeeklyPeriod(forProperty("period")) : null;
        this.trainer = inits.isInitialized("trainer") ? new com.tradingpt.tpt_api.domain.user.entity.QTrainer(forProperty("trainer")) : null;
    }

}

