package com.tradingpt.tpt_api.domain.subscription.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSubscription is a Querydsl query type for Subscription
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscription extends EntityPathBase<Subscription> {

    private static final long serialVersionUID = -1791292496L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSubscription subscription = new QSubscription("subscription");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final NumberPath<Integer> baseOpenedLectureCount = createNumber("baseOpenedLectureCount", Integer.class);

    public final StringPath cancellationReason = createString("cancellationReason");

    public final DateTimePath<java.time.LocalDateTime> cancelledAt = createDateTime("cancelledAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DatePath<java.time.LocalDate> currentPeriodEnd = createDate("currentPeriodEnd", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> currentPeriodStart = createDate("currentPeriodStart", java.time.LocalDate.class);

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DatePath<java.time.LocalDate> lastBillingDate = createDate("lastBillingDate", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> lastPaymentFailedAt = createDateTime("lastPaymentFailedAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> nextBillingDate = createDate("nextBillingDate", java.time.LocalDate.class);

    public final NumberPath<Integer> paymentFailedCount = createNumber("paymentFailedCount", Integer.class);

    public final com.tradingpt.tpt_api.domain.paymentmethod.entity.QPaymentMethod paymentMethod;

    public final StringPath promotionNote = createString("promotionNote");

    public final EnumPath<com.tradingpt.tpt_api.domain.subscription.enums.Status> status = createEnum("status", com.tradingpt.tpt_api.domain.subscription.enums.Status.class);

    public final NumberPath<java.math.BigDecimal> subscribedPrice = createNumber("subscribedPrice", java.math.BigDecimal.class);

    public final com.tradingpt.tpt_api.domain.subscriptionplan.entity.QSubscriptionPlan subscriptionPlan;

    public final EnumPath<com.tradingpt.tpt_api.domain.subscription.enums.SubscriptionType> subscriptionType = createEnum("subscriptionType", com.tradingpt.tpt_api.domain.subscription.enums.SubscriptionType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSubscription(String variable) {
        this(Subscription.class, forVariable(variable), INITS);
    }

    public QSubscription(Path<? extends Subscription> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSubscription(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSubscription(PathMetadata metadata, PathInits inits) {
        this(Subscription.class, metadata, inits);
    }

    public QSubscription(Class<? extends Subscription> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.paymentMethod = inits.isInitialized("paymentMethod") ? new com.tradingpt.tpt_api.domain.paymentmethod.entity.QPaymentMethod(forProperty("paymentMethod"), inits.get("paymentMethod")) : null;
        this.subscriptionPlan = inits.isInitialized("subscriptionPlan") ? new com.tradingpt.tpt_api.domain.subscriptionplan.entity.QSubscriptionPlan(forProperty("subscriptionPlan")) : null;
    }

}

