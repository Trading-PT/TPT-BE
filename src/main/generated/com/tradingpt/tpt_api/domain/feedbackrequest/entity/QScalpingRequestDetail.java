package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QScalpingRequestDetail is a Querydsl query type for ScalpingRequestDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QScalpingRequestDetail extends EntityPathBase<ScalpingRequestDetail> {

    private static final long serialVersionUID = -1620422267L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QScalpingRequestDetail scalpingRequestDetail = new QScalpingRequestDetail("scalpingRequestDetail");

    public final QFeedbackRequest _super;

    //inherited
    public final StringPath category;

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.CourseStatus> courseStatus;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt;

    // inherited
    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final NumberPath<java.math.BigDecimal> entryPrice = createNumber("entryPrice", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> exitPrice = createNumber("exitPrice", java.math.BigDecimal.class);

    //inherited
    public final NumberPath<Integer> feedbackMonth;

    //inherited
    public final ListPath<FeedbackRequestAttachment, QFeedbackRequestAttachment> feedbackRequestAttachments;

    //inherited
    public final DatePath<java.time.LocalDate> feedbackRequestedAt;

    // inherited
    public final com.tradingpt.tpt_api.domain.feedbackresponse.entity.QFeedbackResponse feedbackResponse;

    //inherited
    public final NumberPath<Integer> feedbackWeek;

    //inherited
    public final NumberPath<Integer> feedbackYear;

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final BooleanPath isBestFeedback;

    //inherited
    public final BooleanPath isRead;

    //inherited
    public final BooleanPath isResponded;

    //inherited
    public final NumberPath<Integer> leverage;

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.MembershipLevel> membershipLevel;

    public final NumberPath<Integer> operatingFundsRatio = createNumber("operatingFundsRatio", Integer.class);

    //inherited
    public final NumberPath<java.math.BigDecimal> pnl;

    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position> position = createEnum("position", com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position.class);

    public final StringPath positionEndReason = createString("positionEndReason");

    //inherited
    public final StringPath positionHoldingTime;

    public final StringPath positionStartReason = createString("positionStartReason");

    // inherited
    public final QPreCourseFeedbackDetail preCourseFeedbackDetail;

    //inherited
    public final NumberPath<Integer> riskTaking;

    //inherited
    public final NumberPath<Double> rnr;

    public final NumberPath<java.math.BigDecimal> settingStopLoss = createNumber("settingStopLoss", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> settingTakeProfit = createNumber("settingTakeProfit", java.math.BigDecimal.class);

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status> status;

    //inherited
    public final StringPath title;

    //inherited
    public final StringPath tradingReview;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt;

    public QScalpingRequestDetail(String variable) {
        this(ScalpingRequestDetail.class, forVariable(variable), INITS);
    }

    public QScalpingRequestDetail(Path<? extends ScalpingRequestDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QScalpingRequestDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QScalpingRequestDetail(PathMetadata metadata, PathInits inits) {
        this(ScalpingRequestDetail.class, metadata, inits);
    }

    public QScalpingRequestDetail(Class<? extends ScalpingRequestDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QFeedbackRequest(type, metadata, inits);
        this.category = _super.category;
        this.courseStatus = _super.courseStatus;
        this.createdAt = _super.createdAt;
        this.customer = _super.customer;
        this.feedbackMonth = _super.feedbackMonth;
        this.feedbackRequestAttachments = _super.feedbackRequestAttachments;
        this.feedbackRequestedAt = _super.feedbackRequestedAt;
        this.feedbackResponse = _super.feedbackResponse;
        this.feedbackWeek = _super.feedbackWeek;
        this.feedbackYear = _super.feedbackYear;
        this.id = _super.id;
        this.isBestFeedback = _super.isBestFeedback;
        this.isRead = _super.isRead;
        this.isResponded = _super.isResponded;
        this.leverage = _super.leverage;
        this.membershipLevel = _super.membershipLevel;
        this.pnl = _super.pnl;
        this.positionHoldingTime = _super.positionHoldingTime;
        this.preCourseFeedbackDetail = _super.preCourseFeedbackDetail;
        this.riskTaking = _super.riskTaking;
        this.rnr = _super.rnr;
        this.status = _super.status;
        this.title = _super.title;
        this.tradingReview = _super.tradingReview;
        this.updatedAt = _super.updatedAt;
    }

}

