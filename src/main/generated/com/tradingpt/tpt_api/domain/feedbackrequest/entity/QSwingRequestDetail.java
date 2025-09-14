package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSwingRequestDetail is a Querydsl query type for SwingRequestDetail
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSwingRequestDetail extends EntityPathBase<SwingRequestDetail> {

    private static final long serialVersionUID = 658923376L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSwingRequestDetail swingRequestDetail = new QSwingRequestDetail("swingRequestDetail");

    public final QFeedbackRequest _super;

    public final StringPath category = createString("category");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt;

    // inherited
    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final StringPath directionFrame = createString("directionFrame");

    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint> entryPoint1 = createEnum("entryPoint1", com.tradingpt.tpt_api.domain.feedbackrequest.enums.EntryPoint.class);

    public final DateTimePath<java.time.LocalDateTime> entryPoint2 = createDateTime("entryPoint2", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> entryPoint3 = createDateTime("entryPoint3", java.time.LocalDateTime.class);

    public final NumberPath<Integer> feedbackMonth = createNumber("feedbackMonth", Integer.class);

    //inherited
    public final ListPath<FeedbackRequestAttachment, QFeedbackRequestAttachment> feedbackRequestAttachments;

    //inherited
    public final DatePath<java.time.LocalDate> feedbackRequestedAt;

    // inherited
    public final com.tradingpt.tpt_api.domain.feedbackresponse.entity.QFeedbackResponse feedbackResponse;

    public final NumberPath<Integer> feedbackWeek = createNumber("feedbackWeek", Integer.class);

    //inherited
    public final NumberPath<Integer> feedbackYear;

    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade> grade = createEnum("grade", com.tradingpt.tpt_api.domain.feedbackrequest.enums.Grade.class);

    //inherited
    public final NumberPath<Long> id;

    //inherited
    public final BooleanPath isBestFeedback;

    //inherited
    public final BooleanPath isCourseCompleted;

    public final NumberPath<Integer> leverage = createNumber("leverage", Integer.class);

    public final StringPath mainFrame = createString("mainFrame");

    public final NumberPath<java.math.BigDecimal> pnl = createNumber("pnl", java.math.BigDecimal.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position> position = createEnum("position", com.tradingpt.tpt_api.domain.feedbackrequest.enums.Position.class);

    public final DatePath<java.time.LocalDate> positionEndDate = createDate("positionEndDate", java.time.LocalDate.class);

    public final StringPath positionEndReason = createString("positionEndReason");

    public final DatePath<java.time.LocalDate> positionStartDate = createDate("positionStartDate", java.time.LocalDate.class);

    public final StringPath positionStartReason = createString("positionStartReason");

    public final DatePath<java.time.LocalDate> requestDate = createDate("requestDate", java.time.LocalDate.class);

    public final NumberPath<Integer> riskTaking = createNumber("riskTaking", Integer.class);

    public final StringPath screenshotImageUrl = createString("screenshotImageUrl");

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status> status;

    public final StringPath subFrame = createString("subFrame");

    public final StringPath tradingReview = createString("tradingReview");

    public final StringPath trainerFeedbackRequestContent = createString("trainerFeedbackRequestContent");

    public final StringPath trendAnalysis = createString("trendAnalysis");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt;

    // inherited
    public final com.tradingpt.tpt_api.domain.weeklytradingsummary.entity.QWeeklyTradingSummary weeklyTradingSummary;

    public final StringPath winLossRatio = createString("winLossRatio");

    public QSwingRequestDetail(String variable) {
        this(SwingRequestDetail.class, forVariable(variable), INITS);
    }

    public QSwingRequestDetail(Path<? extends SwingRequestDetail> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSwingRequestDetail(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSwingRequestDetail(PathMetadata metadata, PathInits inits) {
        this(SwingRequestDetail.class, metadata, inits);
    }

    public QSwingRequestDetail(Class<? extends SwingRequestDetail> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this._super = new QFeedbackRequest(type, metadata, inits);
        this.createdAt = _super.createdAt;
        this.customer = _super.customer;
        this.feedbackRequestAttachments = _super.feedbackRequestAttachments;
        this.feedbackRequestedAt = _super.feedbackRequestedAt;
        this.feedbackResponse = _super.feedbackResponse;
        this.feedbackYear = _super.feedbackYear;
        this.id = _super.id;
        this.isBestFeedback = _super.isBestFeedback;
        this.isCourseCompleted = _super.isCourseCompleted;
        this.status = _super.status;
        this.updatedAt = _super.updatedAt;
        this.weeklyTradingSummary = _super.weeklyTradingSummary;
    }

}

