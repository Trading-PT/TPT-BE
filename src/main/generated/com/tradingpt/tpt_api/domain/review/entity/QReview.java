package com.tradingpt.tpt_api.domain.review.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReview is a Querydsl query type for Review
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReview extends EntityPathBase<Review> {

    private static final long serialVersionUID = -1030347290L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReview review = new QReview("review");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final ListPath<ReviewAttachment, QReviewAttachment> attachments = this.<ReviewAttachment, QReviewAttachment>createList("attachments", ReviewAttachment.class, QReviewAttachment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Integer> rating = createNumber("rating", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> repliedAt = createDateTime("repliedAt", java.time.LocalDateTime.class);

    public final StringPath replyContent = createString("replyContent");

    public final ListPath<ReviewTagMapping, QReviewTagMapping> reviewTagMappings = this.<ReviewTagMapping, QReviewTagMapping>createList("reviewTagMappings", ReviewTagMapping.class, QReviewTagMapping.class, PathInits.DIRECT2);

    public final EnumPath<com.tradingpt.tpt_api.domain.review.enums.Status> status = createEnum("status", com.tradingpt.tpt_api.domain.review.enums.Status.class);

    public final DateTimePath<java.time.LocalDateTime> submittedAt = createDateTime("submittedAt", java.time.LocalDateTime.class);

    public final com.tradingpt.tpt_api.domain.user.entity.QTrainer trainer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QReview(String variable) {
        this(Review.class, forVariable(variable), INITS);
    }

    public QReview(Path<? extends Review> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReview(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReview(PathMetadata metadata, PathInits inits) {
        this(Review.class, metadata, inits);
    }

    public QReview(Class<? extends Review> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.trainer = inits.isInitialized("trainer") ? new com.tradingpt.tpt_api.domain.user.entity.QTrainer(forProperty("trainer")) : null;
    }

}

