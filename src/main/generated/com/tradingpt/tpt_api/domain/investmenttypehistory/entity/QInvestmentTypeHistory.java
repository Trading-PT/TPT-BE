package com.tradingpt.tpt_api.domain.investmenttypehistory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInvestmentTypeHistory is a Querydsl query type for InvestmentTypeHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInvestmentTypeHistory extends EntityPathBase<InvestmentTypeHistory> {

    private static final long serialVersionUID = -1936242500L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInvestmentTypeHistory investmentTypeHistory = new QInvestmentTypeHistory("investmentTypeHistory");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.InvestmentType> investmentType = createEnum("investmentType", com.tradingpt.tpt_api.domain.user.enums.InvestmentType.class);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInvestmentTypeHistory(String variable) {
        this(InvestmentTypeHistory.class, forVariable(variable), INITS);
    }

    public QInvestmentTypeHistory(Path<? extends InvestmentTypeHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInvestmentTypeHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInvestmentTypeHistory(PathMetadata metadata, PathInits inits) {
        this(InvestmentTypeHistory.class, metadata, inits);
    }

    public QInvestmentTypeHistory(Class<? extends InvestmentTypeHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
    }

}

