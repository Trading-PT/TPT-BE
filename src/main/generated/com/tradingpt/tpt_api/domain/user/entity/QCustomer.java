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

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCustomer customer = new QCustomer("customer");

    public final QUser _super = new QUser(this);

    public final QTrainer assignedTrainer;

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.CourseStatus> courseStatus = createEnum("courseStatus", com.tradingpt.tpt_api.domain.user.enums.CourseStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final ListPath<com.tradingpt.tpt_api.domain.customermembershiphistory.entity.CustomerMembershipHistory, com.tradingpt.tpt_api.domain.customermembershiphistory.entity.QCustomerMembershipHistory> customerMembershipHistories = this.<com.tradingpt.tpt_api.domain.customermembershiphistory.entity.CustomerMembershipHistory, com.tradingpt.tpt_api.domain.customermembershiphistory.entity.QCustomerMembershipHistory>createList("customerMembershipHistories", com.tradingpt.tpt_api.domain.customermembershiphistory.entity.CustomerMembershipHistory.class, com.tradingpt.tpt_api.domain.customermembershiphistory.entity.QCustomerMembershipHistory.class, PathInits.DIRECT2);

    //inherited
    public final StringPath email = _super.email;

    public final ListPath<com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest, com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest> feedbackRequests = this.<com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest, com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest>createList("feedbackRequests", com.tradingpt.tpt_api.domain.feedbackrequest.entity.FeedbackRequest.class, com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest.class, PathInits.DIRECT2);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final ListPath<com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeHistory, com.tradingpt.tpt_api.domain.investmenttypehistory.entity.QInvestmentTypeHistory> investmentHistories = this.<com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeHistory, com.tradingpt.tpt_api.domain.investmenttypehistory.entity.QInvestmentTypeHistory>createList("investmentHistories", com.tradingpt.tpt_api.domain.investmenttypehistory.entity.InvestmentTypeHistory.class, com.tradingpt.tpt_api.domain.investmenttypehistory.entity.QInvestmentTypeHistory.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> membershipExpiredAt = createDateTime("membershipExpiredAt", java.time.LocalDateTime.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.MembershipLevel> membershipLevel = createEnum("membershipLevel", com.tradingpt.tpt_api.domain.user.enums.MembershipLevel.class);

    //inherited
    public final StringPath name = _super.name;

    public final NumberPath<Integer> openChapterNumber = createNumber("openChapterNumber", Integer.class);

    //inherited
    public final StringPath password = _super.password;

    //inherited
    public final ListPath<PasswordHistory, QPasswordHistory> passwordHistories = _super.passwordHistories;

    public final ListPath<com.tradingpt.tpt_api.domain.payment.entity.PaymentMethod, com.tradingpt.tpt_api.domain.payment.entity.QPaymentMethod> paymentMethods = this.<com.tradingpt.tpt_api.domain.payment.entity.PaymentMethod, com.tradingpt.tpt_api.domain.payment.entity.QPaymentMethod>createList("paymentMethods", com.tradingpt.tpt_api.domain.payment.entity.PaymentMethod.class, com.tradingpt.tpt_api.domain.payment.entity.QPaymentMethod.class, PathInits.DIRECT2);

    public final StringPath phoneNumber = createString("phoneNumber");

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.InvestmentType> primaryInvestmentType = createEnum("primaryInvestmentType", com.tradingpt.tpt_api.domain.user.enums.InvestmentType.class);

    public final StringPath profileImageKey = createString("profileImageKey");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.Provider> provider = _super.provider;

    //inherited
    public final StringPath providerId = _super.providerId;

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.AccountStatus> status = createEnum("status", com.tradingpt.tpt_api.domain.user.enums.AccountStatus.class);

    public final NumberPath<Integer> token = createNumber("token", Integer.class);

    public final QUid uid;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath username = _super.username;

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.UserStatus> userStatus = createEnum("userStatus", com.tradingpt.tpt_api.domain.user.enums.UserStatus.class);

    public QCustomer(String variable) {
        this(Customer.class, forVariable(variable), INITS);
    }

    public QCustomer(Path<? extends Customer> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCustomer(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCustomer(PathMetadata metadata, PathInits inits) {
        this(Customer.class, metadata, inits);
    }

    public QCustomer(Class<? extends Customer> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.assignedTrainer = inits.isInitialized("assignedTrainer") ? new QTrainer(forProperty("assignedTrainer")) : null;
        this.uid = inits.isInitialized("uid") ? new QUid(forProperty("uid"), inits.get("uid")) : null;
    }

}

