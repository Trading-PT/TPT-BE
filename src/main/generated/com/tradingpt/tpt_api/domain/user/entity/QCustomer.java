package com.tradingpt.tpt_api.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCustomer is a Querydsl query type for Customer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomer extends EntityPathBase<Customer> {

    private static final long serialVersionUID = -1185606753L;

    public static final QCustomer customer = new QCustomer("customer");

    public final QUser _super = new QUser(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath email = _super.email;

    public final ListPath<com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest, com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest> feedbackRequests = this.<com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest, com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest>createList("feedbackRequests", com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest.class, com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isCourseCompleted = createBoolean("isCourseCompleted");

    public final DateTimePath<java.time.LocalDateTime> membershipExpiredAt = createDateTime("membershipExpiredAt", java.time.LocalDateTime.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.MembershipLevel> membershipLevel = createEnum("membershipLevel", com.tradingpt.tpt_api.domain.user.enums.MembershipLevel.class);

    //inherited
    public final StringPath name = _super.name;

    //inherited
    public final StringPath password = _super.password;

    public final StringPath phoneNumber = createString("phoneNumber");

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.InvestmentType> primaryInvestmentType = createEnum("primaryInvestmentType", com.tradingpt.tpt_api.domain.user.enums.InvestmentType.class);

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.Provider> provider = _super.provider;

    //inherited
    public final StringPath providerId = _super.providerId;

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.AccountStatus> status = createEnum("status", com.tradingpt.tpt_api.domain.user.enums.AccountStatus.class);

    public final ListPath<Uid, QUid> uids = this.<Uid, QUid>createList("uids", Uid.class, QUid.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath username = _super.username;

    public QCustomer(String variable) {
        super(Customer.class, forVariable(variable));
    }

    public QCustomer(Path<? extends Customer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCustomer(PathMetadata metadata) {
        super(Customer.class, metadata);
    }

}

