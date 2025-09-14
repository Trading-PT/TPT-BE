package com.tradingpt.tpt_api.domain.monthlytradingsummary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMonthlyTradingSummary is a Querydsl query type for MonthlyTradingSummary
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMonthlyTradingSummary extends EntityPathBase<MonthlyTradingSummary> {

    private static final long serialVersionUID = -786115364L;

    public static final QMonthlyTradingSummary monthlyTradingSummary = new QMonthlyTradingSummary("monthlyTradingSummary");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.monthlytradingsummary.enums.InvestmentType> investmentType = createEnum("investmentType", com.tradingpt.tpt_api.domain.monthlytradingsummary.enums.InvestmentType.class);

    public final NumberPath<Integer> monthlyAvgRatio = createNumber("monthlyAvgRatio", Integer.class);

    public final StringPath monthlyEvaluation = createString("monthlyEvaluation");

    public final NumberPath<java.math.BigDecimal> monthlyFinalPnl = createNumber("monthlyFinalPnl", java.math.BigDecimal.class);

    public final NumberPath<Integer> monthlyWinRate = createNumber("monthlyWinRate", Integer.class);

    public final StringPath nextMonthGoal = createString("nextMonthGoal");

    public final NumberPath<Integer> summary_month = createNumber("summary_month", Integer.class);

    public final NumberPath<Integer> summary_year = createNumber("summary_year", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final ListPath<com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary, com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.QWeeklyTradingSummary> weeklyTradingSummaries = this.<com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary, com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.QWeeklyTradingSummary>createList("weeklyTradingSummaries", com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.WeeklyTradingSummary.class, com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.QWeeklyTradingSummary.class, PathInits.DIRECT2);

    public QMonthlyTradingSummary(String variable) {
        super(MonthlyTradingSummary.class, forVariable(variable));
    }

    public QMonthlyTradingSummary(Path<? extends MonthlyTradingSummary> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMonthlyTradingSummary(PathMetadata metadata) {
        super(MonthlyTradingSummary.class, metadata);
    }

}

