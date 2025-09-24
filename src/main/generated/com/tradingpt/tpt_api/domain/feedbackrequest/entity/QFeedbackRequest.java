package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedbackRequest is a Querydsl query type for FeedbackRequest
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedbackRequest extends EntityPathBase<FeedbackRequest> {

    private static final long serialVersionUID = 1156343804L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedbackRequest feedbackRequest = new QFeedbackRequest("feedbackRequest");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final NumberPath<Integer> feedbackMonth = createNumber("feedbackMonth", Integer.class);

    public final ListPath<FeedbackRequestAttachment, QFeedbackRequestAttachment> feedbackRequestAttachments = this.<FeedbackRequestAttachment, QFeedbackRequestAttachment>createList("feedbackRequestAttachments", FeedbackRequestAttachment.class, QFeedbackRequestAttachment.class, PathInits.DIRECT2);

    public final DatePath<java.time.LocalDate> feedbackRequestedAt = createDate("feedbackRequestedAt", java.time.LocalDate.class);

    public final com.tradingpt.tpt_api.domain.feedbackresponse.entity.QFeedbackResponse feedbackResponse;

    public final NumberPath<Integer> feedbackWeek = createNumber("feedbackWeek", Integer.class);

    public final NumberPath<Integer> feedbackYear = createNumber("feedbackYear", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isBestFeedback = createBoolean("isBestFeedback");

    public final BooleanPath isCourseCompleted = createBoolean("isCourseCompleted");

    public final BooleanPath isRead = createBoolean("isRead");

    public final BooleanPath isResponded = createBoolean("isResponded");

    public final EnumPath<com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status> status = createEnum("status", com.tradingpt.tpt_api.domain.feedbackrequest.enums.Status.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFeedbackRequest(String variable) {
        this(FeedbackRequest.class, forVariable(variable), INITS);
    }

    public QFeedbackRequest(Path<? extends FeedbackRequest> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedbackRequest(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedbackRequest(PathMetadata metadata, PathInits inits) {
        this(FeedbackRequest.class, metadata, inits);
    }

    public QFeedbackRequest(Class<? extends FeedbackRequest> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.feedbackResponse = inits.isInitialized("feedbackResponse") ? new com.tradingpt.tpt_api.domain.feedbackresponse.entity.QFeedbackResponse(forProperty("feedbackResponse"), inits.get("feedbackResponse")) : null;
    }

}

