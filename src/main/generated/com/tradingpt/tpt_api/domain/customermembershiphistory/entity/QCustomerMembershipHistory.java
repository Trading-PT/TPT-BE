package com.tradingpt.tpt_api.domain.customermembershiphistory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCustomerMembershipHistory is a Querydsl query type for CustomerMembershipHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomerMembershipHistory extends EntityPathBase<CustomerMembershipHistory> {

    private static final long serialVersionUID = 1182313692L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCustomerMembershipHistory customerMembershipHistory = new QCustomerMembershipHistory("customerMembershipHistory");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.MembershipLevel> membershipLevel = createEnum("membershipLevel", com.tradingpt.tpt_api.domain.user.enums.MembershipLevel.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.customermembershiphistory.enums.MembershipStatus> membershipStatus = createEnum("membershipStatus", com.tradingpt.tpt_api.domain.customermembershiphistory.enums.MembershipStatus.class);

    public final StringPath reasonCode = createString("reasonCode");

    public final StringPath reasonDetail = createString("reasonDetail");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final DateTimePath<java.time.LocalDateTime> validFrom = createDateTime("validFrom", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> validTo = createDateTime("validTo", java.time.LocalDateTime.class);

    public QCustomerMembershipHistory(String variable) {
        this(CustomerMembershipHistory.class, forVariable(variable), INITS);
    }

    public QCustomerMembershipHistory(Path<? extends CustomerMembershipHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCustomerMembershipHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCustomerMembershipHistory(PathMetadata metadata, PathInits inits) {
        this(CustomerMembershipHistory.class, metadata, inits);
    }

    public QCustomerMembershipHistory(Class<? extends CustomerMembershipHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
    }

}

