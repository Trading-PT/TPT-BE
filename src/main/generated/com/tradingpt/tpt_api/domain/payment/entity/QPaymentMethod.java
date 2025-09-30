package com.tradingpt.tpt_api.domain.payment.entity;

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

    private static final long serialVersionUID = 1337268125L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPaymentMethod paymentMethod1 = new QPaymentMethod("paymentMethod1");

    public final StringPath billingKey = createString("billingKey");

    public final EnumPath<com.tradingpt.tpt_api.domain.payment.enums.CardType> cardType = createEnum("cardType", com.tradingpt.tpt_api.domain.payment.enums.CardType.class);

    public final StringPath displayName = createString("displayName");

    public final DateTimePath<java.time.LocalDateTime> expiresAt = createDateTime("expiresAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath issuerName = createString("issuerName");

    public final StringPath maskedIdentifier = createString("maskedIdentifier");

    public final EnumPath<com.tradingpt.tpt_api.domain.payment.enums.PaymentMethodType> paymentMethod = createEnum("paymentMethod", com.tradingpt.tpt_api.domain.payment.enums.PaymentMethodType.class);

    public final StringPath pgCustomerKey = createString("pgCustomerKey");

    public final StringPath pgMetadata = createString("pgMetadata");

    public final EnumPath<com.tradingpt.tpt_api.domain.payment.enums.PgProvider> pgProvider = createEnum("pgProvider", com.tradingpt.tpt_api.domain.payment.enums.PgProvider.class);

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer user;

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
        this.user = inits.isInitialized("user") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("user"), inits.get("user")) : null;
    }

}

