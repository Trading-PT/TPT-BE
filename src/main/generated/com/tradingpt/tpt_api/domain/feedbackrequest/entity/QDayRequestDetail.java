package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QDayRequestDetail is a Querydsl query type for DayRequestDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QDayRequestDetail extends EntityPathBase<DayRequestDetail> {

    private static final long serialVersionUID = 1459304210L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QDayRequestDetail dayRequestDetail = new QDayRequestDetail("dayRequestDetail");

    public final QFeedbackRequest _super;

    //inherited
    public final StringPath category;

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.CourseStatus> courseStatus;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt;

    // inherited
    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final StringPath directionFrame = createString("directionFrame");

    public final BooleanPath directionFrameExists = createBoolean("directionFrameExists");

    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint> entryPoint1 = createEnum("entryPoint1", com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint.class);

    public final StringPath entryPoint2 = createString("entryPoint2");

    //inherited
    public final NumberPath<java.math.BigDecimal> entryPrice;

    //inherited
    public final NumberPath<java.math.BigDecimal> exitPrice;

    //inherited
    public final NumberPath<Integer> feedbackMonth;

    //inherited
    public final ListPath<FeedbackRequestAttachment, QFeedbackRequestAttachment> feedbackRequestAttachments;

    //inherited
    public final DatePath<java.time.LocalDate> feedbackRequestDate;

    // inherited
    public final com.tradingpt.tpt_api.domain.feedbackresponse.entity.QFeedbackResponse feedbackResponse;

    //inherited
    public final NumberPath<Integer> feedbackWeek;

    //inherited
    public final NumberPath<Integer> feedbackYear;

    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade> grade = createEnum("grade", com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade.class);

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final BooleanPath isBestFeedback;

    //inherited
    public final BooleanPath isRead;

    //inherited
    public final BooleanPath isResponded;

    //inherited
    public final BooleanPath isTokenUsed;

    //inherited
    public final BooleanPath isTrainerWritten;

    //inherited
    public final NumberPath<Integer> leverage;

    public final StringPath mainFrame = createString("mainFrame");

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.MembershipLevel> membershipLevel;

    //inherited
    public final NumberPath<Integer> operatingFundsRatio;

    //inherited
    public final NumberPath<java.math.BigDecimal> pnl;

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position> position;

    //inherited
    public final StringPath positionEndReason;

    //inherited
    public final StringPath positionHoldingTime;

    //inherited
    public final StringPath positionStartReason;

    //inherited
    public final NumberPath<Integer> riskTaking;

    //inherited
    public final NumberPath<Double> rnr;

    //inherited
    public final NumberPath<java.math.BigDecimal> settingStopLoss;

    //inherited
    public final NumberPath<java.math.BigDecimal> settingTakeProfit;

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status> status;

    public final StringPath subFrame = createString("subFrame");

    //inherited
    public final StringPath title;

    //inherited
    public final NumberPath<Integer> tokenAmount;

    //inherited
    public final StringPath tradingReview;

    public final StringPath trainerFeedbackRequestContent = createString("trainerFeedbackRequestContent");

    public final StringPath trendAnalysis = createString("trendAnalysis");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt;

    public QDayRequestDetail(String variable) {
        this(DayRequestDetail.class, forVariable(variable), INITS);
    }

    public QDayRequestDetail(Path<? extends DayRequestDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QDayRequestDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QDayRequestDetail(PathMetadata metadata, PathInits inits) {
        this(DayRequestDetail.class, metadata, inits);
    }

    public QDayRequestDetail(Class<? extends DayRequestDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QFeedbackRequest(type, metadata, inits);
        this.category = _super.category;
        this.courseStatus = _super.courseStatus;
        this.createdAt = _super.createdAt;
        this.customer = _super.customer;
        this.entryPrice = _super.entryPrice;
        this.exitPrice = _super.exitPrice;
        this.feedbackMonth = _super.feedbackMonth;
        this.feedbackRequestAttachments = _super.feedbackRequestAttachments;
        this.feedbackRequestDate = _super.feedbackRequestDate;
        this.feedbackResponse = _super.feedbackResponse;
        this.feedbackWeek = _super.feedbackWeek;
        this.feedbackYear = _super.feedbackYear;
        this.id = _super.id;
        this.isBestFeedback = _super.isBestFeedback;
        this.isRead = _super.isRead;
        this.isResponded = _super.isResponded;
        this.isTokenUsed = _super.isTokenUsed;
        this.isTrainerWritten = _super.isTrainerWritten;
        this.leverage = _super.leverage;
        this.membershipLevel = _super.membershipLevel;
        this.operatingFundsRatio = _super.operatingFundsRatio;
        this.pnl = _super.pnl;
        this.position = _super.position;
        this.positionEndReason = _super.positionEndReason;
        this.positionHoldingTime = _super.positionHoldingTime;
        this.positionStartReason = _super.positionStartReason;
        this.riskTaking = _super.riskTaking;
        this.rnr = _super.rnr;
        this.settingStopLoss = _super.settingStopLoss;
        this.settingTakeProfit = _super.settingTakeProfit;
        this.status = _super.status;
        this.title = _super.title;
        this.tokenAmount = _super.tokenAmount;
        this.tradingReview = _super.tradingReview;
        this.updatedAt = _super.updatedAt;
    }

}

