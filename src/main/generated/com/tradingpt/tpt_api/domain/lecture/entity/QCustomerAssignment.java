package com.tradingpt.tpt_api.domain.lecture.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCustomerAssignment is a Querydsl query type for CustomerAssignment
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomerAssignment extends EntityPathBase<CustomerAssignment> {

    private static final long serialVersionUID = -1397582995L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCustomerAssignment customerAssignment = new QCustomerAssignment("customerAssignment");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLecture lecture;

    public final BooleanPath submitted = createBoolean("submitted");

    public final DateTimePath<java.time.LocalDateTime> submittedAt = createDateTime("submittedAt", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCustomerAssignment(String variable) {
        this(CustomerAssignment.class, forVariable(variable), INITS);
    }

    public QCustomerAssignment(Path<? extends CustomerAssignment> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCustomerAssignment(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCustomerAssignment(PathMetadata metadata, PathInits inits) {
        this(CustomerAssignment.class, metadata, inits);
    }

    public QCustomerAssignment(Class<? extends CustomerAssignment> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
        this.lecture = inits.isInitialized("lecture") ? new QLecture(forProperty("lecture"), inits.get("lecture")) : null;
    }

}

