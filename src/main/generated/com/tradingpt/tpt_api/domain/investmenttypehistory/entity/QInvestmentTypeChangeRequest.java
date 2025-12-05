package com.tradingpt.tpt_api.domain.investmenttypehistory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QInvestmentTypeChangeRequest is a Querydsl query type for InvestmentTypeChangeRequest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInvestmentTypeChangeRequest extends EntityPathBase<InvestmentTypeChangeRequest> {

    private static final long serialVersionUID = -232155417L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QInvestmentTypeChangeRequest investmentTypeChangeRequest = new QInvestmentTypeChangeRequest("investmentTypeChangeRequest");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.InvestmentType> currentType = createEnum("currentType", com.tradingpt.tpt_api.domain.user.enums.InvestmentType.class);

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> processedAt = createDateTime("processedAt", java.time.LocalDateTime.class);

    public final StringPath reason = createString("reason");

    public final StringPath rejectionReason = createString("rejectionReason");

    public final DatePath<java.time.LocalDate> requestedDate = createDate("requestedDate", java.time.LocalDate.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.InvestmentType> requestedType = createEnum("requestedType", com.tradingpt.tpt_api.domain.user.enums.InvestmentType.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.investmenttypehistory.enums.ChangeRequestStatus> status = createEnum("status", com.tradingpt.tpt_api.domain.investmenttypehistory.enums.ChangeRequestStatus.class);

    public final DatePath<java.time.LocalDate> targetChangeDate = createDate("targetChangeDate", java.time.LocalDate.class);

    public final com.tradingpt.tpt_api.domain.user.entity.QTrainer trainer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInvestmentTypeChangeRequest(String variable) {
        this(InvestmentTypeChangeRequest.class, forVariable(variable), INITS);
    }

    public QInvestmentTypeChangeRequest(Path<? extends InvestmentTypeChangeRequest> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QInvestmentTypeChangeRequest(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QInvestmentTypeChangeRequest(PathMetadata metadata, PathInits inits) {
        this(InvestmentTypeChangeRequest.class, metadata, inits);
    }

    public QInvestmentTypeChangeRequest(Class<? extends InvestmentTypeChangeRequest> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.trainer = inits.isInitialized("trainer") ? new com.tradingpt.tpt_api.domain.user.entity.QTrainer(forProperty("trainer")) : null;
    }

}

