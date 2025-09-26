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

    public final StringPath category = createString("category");

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.CourseStatus> courseStatus;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt;

    // inherited
    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final NumberPath<Integer> dailyTradingCount = createNumber("dailyTradingCount", Integer.class);

    //inherited
    public final NumberPath<Integer> feedbackMonth;

    //inherited
    public final ListPath<FeedbackRequestAttachment, QFeedbackRequestAttachment> feedbackRequestAttachments;

    //inherited
    public final DatePath<java.time.LocalDate> feedbackRequestedAt;

    // inherited
    public final com.tradingpt.tpt_api.domain.feedbackresponse.entity.QFeedbackResponse feedbackResponse;

    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType> feedbackType = createEnum("feedbackType", com.tradingpt.tpt_api.domain.feedbackrequest.enums.FeedbackType.class);

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

    public final NumberPath<Integer> leverage = createNumber("leverage", Integer.class);

    // inherited
    public final QPreCourseFeedbackDetail preCourseFeedbackDetail;

    public final NumberPath<Integer> riskTaking = createNumber("riskTaking", Integer.class);

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status> status;

    //inherited
    public final StringPath title;

    public final NumberPath<Integer> totalPositionTakingCount = createNumber("totalPositionTakingCount", Integer.class);

    public final NumberPath<Integer> totalProfitMarginPerTrades = createNumber("totalProfitMarginPerTrades", Integer.class);

    public final StringPath trainerFeedbackRequestContent = createString("trainerFeedbackRequestContent");

    public final StringPath trendAnalysis = createString("trendAnalysis");

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
        this.preCourseFeedbackDetail = _super.preCourseFeedbackDetail;
        this.status = _super.status;
        this.title = _super.title;
        this.updatedAt = _super.updatedAt;
    }

}

