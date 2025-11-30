package com.tradingpt.tpt_api.domain.lecture.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAssignmentAttachment is a Querydsl query type for AssignmentAttachment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAssignmentAttachment extends EntityPathBase<AssignmentAttachment> {

    private static final long serialVersionUID = -979841678L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAssignmentAttachment assignmentAttachment = new QAssignmentAttachment("assignmentAttachment");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final NumberPath<Integer> attemptNo = createNumber("attemptNo", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QCustomerAssignment customerAssignment;

    public final StringPath fileKey = createString("fileKey");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QAssignmentAttachment(String variable) {
        this(AssignmentAttachment.class, forVariable(variable), INITS);
    }

    public QAssignmentAttachment(Path<? extends AssignmentAttachment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAssignmentAttachment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAssignmentAttachment(PathMetadata metadata, PathInits inits) {
        this(AssignmentAttachment.class, metadata, inits);
    }

    public QAssignmentAttachment(Class<? extends AssignmentAttachment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customerAssignment = inits.isInitialized("customerAssignment") ? new QCustomerAssignment(forProperty("customerAssignment"), inits.get("customerAssignment")) : null;
    }

}

