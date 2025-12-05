package com.tradingpt.tpt_api.domain.lecture.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLectureProgress is a Querydsl query type for LectureProgress
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLectureProgress extends EntityPathBase<LectureProgress> {

    private static final long serialVersionUID = 1202383593L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLectureProgress lectureProgress = new QLectureProgress("lectureProgress");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final DateTimePath<java.time.LocalDateTime> dueDate = createDateTime("dueDate", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isCompleted = createBoolean("isCompleted");

    public final NumberPath<Integer> lastPositionSeconds = createNumber("lastPositionSeconds", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> lastWatchedAt = createDateTime("lastWatchedAt", java.time.LocalDateTime.class);

    public final QLecture lecture;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Integer> watchedSeconds = createNumber("watchedSeconds", Integer.class);

    public QLectureProgress(String variable) {
        this(LectureProgress.class, forVariable(variable), INITS);
    }

    public QLectureProgress(Path<? extends LectureProgress> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLectureProgress(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLectureProgress(PathMetadata metadata, PathInits inits) {
        this(LectureProgress.class, metadata, inits);
    }

    public QLectureProgress(Class<? extends LectureProgress> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.lecture = inits.isInitialized("lecture") ? new QLecture(forProperty("lecture"), inits.get("lecture")) : null;
    }

}

