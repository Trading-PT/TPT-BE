package com.tradingpt.tpt_api.domain.feedbackresponse.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedbackResponse is a Querydsl query type for FeedbackResponse
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedbackResponse extends EntityPathBase<FeedbackResponse> {

    private static final long serialVersionUID = 1064350242L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedbackResponse feedbackResponse = new QFeedbackResponse("feedbackResponse");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest feedbackRequest;

    public final ListPath<FeedbackResponseAttachment, QFeedbackResponseAttachment> feedbackResponseAttachments = this.<FeedbackResponseAttachment, QFeedbackResponseAttachment>createList("feedbackResponseAttachments", FeedbackResponseAttachment.class, QFeedbackResponseAttachment.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath responseContent = createString("responseContent");

    public final DateTimePath<java.time.LocalDateTime> submittedAt = createDateTime("submittedAt", java.time.LocalDateTime.class);

    public final com.tradingpt.tpt_api.domain.user.entity.QTrainer trainer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFeedbackResponse(String variable) {
        this(FeedbackResponse.class, forVariable(variable), INITS);
    }

    public QFeedbackResponse(Path<? extends FeedbackResponse> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedbackResponse(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedbackResponse(PathMetadata metadata, PathInits inits) {
        this(FeedbackResponse.class, metadata, inits);
    }

    public QFeedbackResponse(Class<? extends FeedbackResponse> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.feedbackRequest = inits.isInitialized("feedbackRequest") ? new com.tradingpt.tpt_api.domain.feedbackrequest.entity.QFeedbackRequest(forProperty("feedbackRequest"), inits.get("feedbackRequest")) : null;
        this.trainer = inits.isInitialized("trainer") ? new com.tradingpt.tpt_api.domain.user.entity.QTrainer(forProperty("trainer")) : null;
    }

}

