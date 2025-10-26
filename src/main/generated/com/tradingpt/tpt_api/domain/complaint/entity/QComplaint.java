package com.tradingpt.tpt_api.domain.complaint.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QComplaint is a Querydsl query type for Complaint
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QComplaint extends EntityPathBase<Complaint> {

    private static final long serialVersionUID = 1692167804L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QComplaint complaint = new QComplaint("complaint");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final DateTimePath<java.time.LocalDateTime> answeredAt = createDateTime("answeredAt", java.time.LocalDateTime.class);

    public final StringPath complaintReply = createString("complaintReply");

    public final StringPath content = createString("content");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.tradingpt.tpt_api.domain.complaint.enums.Status> status = createEnum("status", com.tradingpt.tpt_api.domain.complaint.enums.Status.class);

    public final StringPath title = createString("title");

    public final com.tradingpt.tpt_api.domain.user.entity.QTrainer trainer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QComplaint(String variable) {
        this(Complaint.class, forVariable(variable), INITS);
    }

    public QComplaint(Path<? extends Complaint> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QComplaint(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QComplaint(PathMetadata metadata, PathInits inits) {
        this(Complaint.class, metadata, inits);
    }

    public QComplaint(Class<? extends Complaint> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.trainer = inits.isInitialized("trainer") ? new com.tradingpt.tpt_api.domain.user.entity.QTrainer(forProperty("trainer")) : null;
    }

}

