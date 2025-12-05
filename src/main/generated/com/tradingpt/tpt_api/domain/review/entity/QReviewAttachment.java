package com.tradingpt.tpt_api.domain.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReviewAttachment is a Querydsl query type for ReviewAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReviewAttachment extends EntityPathBase<ReviewAttachment> {

    private static final long serialVersionUID = -735202391L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReviewAttachment reviewAttachment = new QReviewAttachment("reviewAttachment");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath fileKey = createString("fileKey");

    public final StringPath fileUrl = createString("fileUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QReview review;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReviewAttachment(String variable) {
        this(ReviewAttachment.class, forVariable(variable), INITS);
    }

    public QReviewAttachment(Path<? extends ReviewAttachment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReviewAttachment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReviewAttachment(PathMetadata metadata, PathInits inits) {
        this(ReviewAttachment.class, metadata, inits);
    }

    public QReviewAttachment(Class<? extends ReviewAttachment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.review = inits.isInitialized("review") ? new QReview(forProperty("review"), inits.get("review")) : null;
    }

}

