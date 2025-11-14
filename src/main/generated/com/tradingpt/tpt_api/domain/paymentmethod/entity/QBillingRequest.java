package com.tradingpt.tpt_api.domain.paymentmethod.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBillingRequest is a Querydsl query type for BillingRequest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBillingRequest extends EntityPathBase<BillingRequest> {

    private static final long serialVersionUID = 1749481887L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBillingRequest billingRequest = new QBillingRequest("billingRequest");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath moid = createString("moid");

    public final StringPath resultCode = createString("resultCode");

    public final StringPath resultMsg = createString("resultMsg");

    public final EnumPath<com.tradingpt.tpt_api.domain.paymentmethod.enums.Status> status = createEnum("status", com.tradingpt.tpt_api.domain.paymentmethod.enums.Status.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QBillingRequest(String variable) {
        this(BillingRequest.class, forVariable(variable), INITS);
    }

    public QBillingRequest(Path<? extends BillingRequest> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBillingRequest(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBillingRequest(PathMetadata metadata, PathInits inits) {
        this(BillingRequest.class, metadata, inits);
    }

    public QBillingRequest(Class<? extends BillingRequest> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
    }

}

