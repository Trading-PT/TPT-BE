package com.tradingpt.tpt_api.domain.subscriptionplan.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QSubscriptionPlan is a Querydsl query type for SubscriptionPlan
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSubscriptionPlan extends EntityPathBase<SubscriptionPlan> {

    private static final long serialVersionUID = 91064354L;

    public static final QSubscriptionPlan subscriptionPlan = new QSubscriptionPlan("subscriptionPlan");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DateTimePath<java.time.LocalDateTime> effectiveFrom = createDateTime("effectiveFrom", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> effectiveTo = createDateTime("effectiveTo", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isActive = createBoolean("isActive");

    public final StringPath name = createString("name");

    public final NumberPath<java.math.BigDecimal> price = createNumber("price", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QSubscriptionPlan(String variable) {
        super(SubscriptionPlan.class, forVariable(variable));
    }

    public QSubscriptionPlan(Path<? extends SubscriptionPlan> path) {
        super(path.getType(), path.getMetadata());
    }

    public QSubscriptionPlan(PathMetadata metadata) {
        super(SubscriptionPlan.class, metadata);
    }

}

