package com.tradingpt.tpt_api.domain.investmenthistory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInvestmentHistory is a Querydsl query type for InvestmentHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInvestmentHistory extends EntityPathBase<InvestmentHistory> {

    private static final long serialVersionUID = -137267940L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInvestmentHistory investmentHistory = new QInvestmentHistory("investmentHistory");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final DatePath<java.time.LocalDate> endedAt = createDate("endedAt", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.InvestmentType> investmentType = createEnum("investmentType", com.tradingpt.tpt_api.domain.user.enums.InvestmentType.class);

    public final DatePath<java.time.LocalDate> startedAt = createDate("startedAt", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInvestmentHistory(String variable) {
        this(InvestmentHistory.class, forVariable(variable), INITS);
    }

    public QInvestmentHistory(Path<? extends InvestmentHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInvestmentHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInvestmentHistory(PathMetadata metadata, PathInits inits) {
        this(InvestmentHistory.class, metadata, inits);
    }

    public QInvestmentHistory(Class<? extends InvestmentHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
    }

}

