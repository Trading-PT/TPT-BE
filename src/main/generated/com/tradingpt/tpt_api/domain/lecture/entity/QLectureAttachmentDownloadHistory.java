package com.tradingpt.tpt_api.domain.lecture.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLectureAttachmentDownloadHistory is a Querydsl query type for LectureAttachmentDownloadHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLectureAttachmentDownloadHistory extends EntityPathBase<LectureAttachmentDownloadHistory> {

    private static final long serialVersionUID = -495122931L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLectureAttachmentDownloadHistory lectureAttachmentDownloadHistory = new QLectureAttachmentDownloadHistory("lectureAttachmentDownloadHistory");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLectureAttachment lectureAttachment;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLectureAttachmentDownloadHistory(String variable) {
        this(LectureAttachmentDownloadHistory.class, forVariable(variable), INITS);
    }

    public QLectureAttachmentDownloadHistory(Path<? extends LectureAttachmentDownloadHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLectureAttachmentDownloadHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLectureAttachmentDownloadHistory(PathMetadata metadata, PathInits inits) {
        this(LectureAttachmentDownloadHistory.class, metadata, inits);
    }

    public QLectureAttachmentDownloadHistory(Class<? extends LectureAttachmentDownloadHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.lectureAttachment = inits.isInitialized("lectureAttachment") ? new QLectureAttachment(forProperty("lectureAttachment"), inits.get("lectureAttachment")) : null;
    }

}

