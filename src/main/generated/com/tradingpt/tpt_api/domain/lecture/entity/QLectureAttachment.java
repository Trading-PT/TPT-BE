package com.tradingpt.tpt_api.domain.lecture.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLectureAttachment is a Querydsl query type for LectureAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLectureAttachment extends EntityPathBase<LectureAttachment> {

    private static final long serialVersionUID = -1855569185L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLectureAttachment lectureAttachment = new QLectureAttachment("lectureAttachment");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath fileKey = createString("fileKey");

    public final StringPath fileUrl = createString("fileUrl");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLecture lecture;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLectureAttachment(String variable) {
        this(LectureAttachment.class, forVariable(variable), INITS);
    }

    public QLectureAttachment(Path<? extends LectureAttachment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLectureAttachment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLectureAttachment(PathMetadata metadata, PathInits inits) {
        this(LectureAttachment.class, metadata, inits);
    }

    public QLectureAttachment(Class<? extends LectureAttachment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.lecture = inits.isInitialized("lecture") ? new QLecture(forProperty("lecture"), inits.get("lecture")) : null;
    }

}

