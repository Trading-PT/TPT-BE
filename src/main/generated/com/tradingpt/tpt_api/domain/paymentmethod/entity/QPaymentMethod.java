package com.tradingpt.tpt_api.domain.paymentmethod.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPaymentMethod is a Querydsl query type for PaymentMethod
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPaymentMethod extends EntityPathBase<PaymentMethod> {

    private static final long serialVersionUID = -179592420L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentMethod paymentMethod = new QPaymentMethod("paymentMethod");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final StringPath billingKey = createString("billingKey");

    public final DateTimePath<java.time.LocalDateTime> billingKeyIssuedAt = createDateTime("billingKeyIssuedAt", java.time.LocalDateTime.class);

    public final QBillingRequest billingRequest;

    public final StringPath cardCompanyCode = createString("cardCompanyCode");

    public final StringPath cardCompanyName = createString("cardCompanyName");

    public final EnumPath<com.tradingpt.tpt_api.domain.paymentmethod.enums.CardType> cardType = createEnum("cardType", com.tradingpt.tpt_api.domain.paymentmethod.enums.CardType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final DateTimePath<java.time.LocalDateTime> deletedAt = createDateTime("deletedAt", java.time.LocalDateTime.class);

    public final StringPath displayName = createString("displayName");

    public final DatePath<java.time.LocalDate> expiresAt = createDate("expiresAt", java.time.LocalDate.class);

    public final NumberPath<Integer> failureCount = createNumber("failureCount", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final BooleanPath isDeleted = createBoolean("isDeleted");

    public final BooleanPath isPrimary = createBoolean("isPrimary");

    public final DateTimePath<java.time.LocalDateTime> lastFailedAt = createDateTime("lastFailedAt", java.time.LocalDateTime.class);

    public final StringPath maskedIdentifier = createString("maskedIdentifier");

    public final StringPath orderId = createString("orderId");

    public final EnumPath<com.tradingpt.tpt_api.domain.paymentmethod.enums.PaymentMethodType> paymentMethodType = createEnum("paymentMethodType", com.tradingpt.tpt_api.domain.paymentmethod.enums.PaymentMethodType.class);

    public final StringPath pgCustomerKey = createString("pgCustomerKey");

    public final StringPath pgMetadata = createString("pgMetadata");

    public final StringPath pgResponseCode = createString("pgResponseCode");

    public final StringPath pgResponseMessage = createString("pgResponseMessage");

    public final StringPath simplePayMetadata = createString("simplePayMetadata");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QPaymentMethod(String variable) {
        this(PaymentMethod.class, forVariable(variable), INITS);
    }

    public QPaymentMethod(Path<? extends PaymentMethod> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPaymentMethod(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPaymentMethod(PathMetadata metadata, PathInits inits) {
        this(PaymentMethod.class, metadata, inits);
    }

    public QPaymentMethod(Class<? extends PaymentMethod> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.billingRequest = inits.isInitialized("billingRequest") ? new QBillingRequest(forProperty("billingRequest"), inits.get("billingRequest")) : null;
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
    }

}

