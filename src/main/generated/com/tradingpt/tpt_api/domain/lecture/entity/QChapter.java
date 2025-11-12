package com.tradingpt.tpt_api.domain.lecture.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QChapter is a Querydsl query type for Chapter
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QChapter extends EntityPathBase<Chapter> {

    private static final long serialVersionUID = 684756843L;

    public static final QChapter chapter = new QChapter("chapter");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final NumberPath<Integer> chapterOrder = createNumber("chapterOrder", Integer.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.lecture.enums.ChapterType> chapterType = createEnum("chapterType", com.tradingpt.tpt_api.domain.lecture.enums.ChapterType.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QChapter(String variable) {
        super(Chapter.class, forVariable(variable));
    }

    public QChapter(Path<? extends Chapter> path) {
        super(path.getType(), path.getMetadata());
    }

    public QChapter(PathMetadata metadata) {
        super(Chapter.class, metadata);
    }

}

