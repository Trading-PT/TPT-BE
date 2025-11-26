package com.tradingpt.tpt_api.domain.payment.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPayment is a Querydsl query type for Payment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPayment extends EntityPathBase<Payment> {

    private static final long serialVersionUID = -1449558116L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPayment payment = new QPayment("payment");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final NumberPath<java.math.BigDecimal> amount = createNumber("amount", java.math.BigDecimal.class);

    public final DatePath<java.time.LocalDate> billingPeriodEnd = createDate("billingPeriodEnd", java.time.LocalDate.class);

    public final DatePath<java.time.LocalDate> billingPeriodStart = createDate("billingPeriodStart", java.time.LocalDate.class);

    public final DateTimePath<java.time.LocalDateTime> cancelledAt = createDateTime("cancelledAt", java.time.LocalDateTime.class);

    public final StringPath cancelReason = createString("cancelReason");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final NumberPath<java.math.BigDecimal> discountAmount = createNumber("discountAmount", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> failedAt = createDateTime("failedAt", java.time.LocalDateTime.class);

    public final StringPath failureCode = createString("failureCode");

    public final StringPath failureReason = createString("failureReason");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isPromotional = createBoolean("isPromotional");

    public final StringPath orderId = createString("orderId");

    public final StringPath orderName = createString("orderName");

    public final DateTimePath<java.time.LocalDateTime> paidAt = createDateTime("paidAt", java.time.LocalDateTime.class);

    public final StringPath paymentKey = createString("paymentKey");

    public final com.tradingpt.tpt_api.domain.paymentmethod.entity.QPaymentMethod paymentMethod;

    public final StringPath paymentMethodSnapshot = createString("paymentMethodSnapshot");

    public final EnumPath<com.tradingpt.tpt_api.domain.payment.enums.PaymentType> paymentType = createEnum("paymentType", com.tradingpt.tpt_api.domain.payment.enums.PaymentType.class);

    public final StringPath pgAuthCode = createString("pgAuthCode");

    public final StringPath pgGoodsName = createString("pgGoodsName");

    public final StringPath pgMetadata = createString("pgMetadata");

    public final StringPath pgRefundTid = createString("pgRefundTid");

    public final StringPath pgResponseCode = createString("pgResponseCode");

    public final StringPath pgResponseMessage = createString("pgResponseMessage");

    public final StringPath pgTid = createString("pgTid");

    public final StringPath promotionDetail = createString("promotionDetail");

    public final StringPath receiptUrl = createString("receiptUrl");

    public final NumberPath<java.math.BigDecimal> refundAmount = createNumber("refundAmount", java.math.BigDecimal.class);

    public final DateTimePath<java.time.LocalDateTime> refundedAt = createDateTime("refundedAt", java.time.LocalDateTime.class);

    public final StringPath refundReason = createString("refundReason");

    public final DateTimePath<java.time.LocalDateTime> requestedAt = createDateTime("requestedAt", java.time.LocalDateTime.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.payment.enums.PaymentStatus> status = createEnum("status", com.tradingpt.tpt_api.domain.payment.enums.PaymentStatus.class);

    public final com.tradingpt.tpt_api.domain.subscription.entity.QSubscription subscription;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<java.math.BigDecimal> vat = createNumber("vat", java.math.BigDecimal.class);

    public QPayment(String variable) {
        this(Payment.class, forVariable(variable), INITS);
    }

    public QPayment(Path<? extends Payment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPayment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPayment(PathMetadata metadata, PathInits inits) {
        this(Payment.class, metadata, inits);
    }

    public QPayment(Class<? extends Payment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.paymentMethod = inits.isInitialized("paymentMethod") ? new com.tradingpt.tpt_api.domain.paymentmethod.entity.QPaymentMethod(forProperty("paymentMethod"), inits.get("paymentMethod")) : null;
        this.subscription = inits.isInitialized("subscription") ? new com.tradingpt.tpt_api.domain.subscription.entity.QSubscription(forProperty("subscription"), inits.get("subscription")) : null;
    }

}

