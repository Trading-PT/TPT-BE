package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFeedbackRequestAttachment is a Querydsl query type for FeedbackRequestAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFeedbackRequestAttachment extends EntityPathBase<FeedbackRequestAttachment> {

    private static final long serialVersionUID = -1530867137L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFeedbackRequestAttachment feedbackRequestAttachment = new QFeedbackRequestAttachment("feedbackRequestAttachment");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final QFeedbackRequest feedbackRequest;

    public final StringPath fileKey = createString("fileKey");

    public final StringPath fileUrl = createString("fileUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QFeedbackRequestAttachment(String variable) {
        this(FeedbackRequestAttachment.class, forVariable(variable), INITS);
    }

    public QFeedbackRequestAttachment(Path<? extends FeedbackRequestAttachment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFeedbackRequestAttachment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFeedbackRequestAttachment(PathMetadata metadata, PathInits inits) {
        this(FeedbackRequestAttachment.class, metadata, inits);
    }

    public QFeedbackRequestAttachment(Class<? extends FeedbackRequestAttachment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.feedbackRequest = inits.isInitialized("feedbackRequest") ? new QFeedbackRequest(forProperty("feedbackRequest"), inits.get("feedbackRequest")) : null;
    }

}

