package com.tradingpt.tpt_api.domain.feedbackresponse.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedbackResponseAttachment is a Querydsl query type for FeedbackResponseAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedbackResponseAttachment extends EntityPathBase<FeedbackResponseAttachment> {

    private static final long serialVersionUID = 2145592549L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedbackResponseAttachment feedbackResponseAttachment = new QFeedbackResponseAttachment("feedbackResponseAttachment");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final QFeedbackResponse feedbackResponse;

    public final StringPath fileKey = createString("fileKey");

    public final StringPath fileUrl = createString("fileUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFeedbackResponseAttachment(String variable) {
        this(FeedbackResponseAttachment.class, forVariable(variable), INITS);
    }

    public QFeedbackResponseAttachment(Path<? extends FeedbackResponseAttachment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedbackResponseAttachment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedbackResponseAttachment(PathMetadata metadata, PathInits inits) {
        this(FeedbackResponseAttachment.class, metadata, inits);
    }

    public QFeedbackResponseAttachment(Class<? extends FeedbackResponseAttachment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.feedbackResponse = inits.isInitialized("feedbackResponse") ? new QFeedbackResponse(forProperty("feedbackResponse"), inits.get("feedbackResponse")) : null;
    }

}

