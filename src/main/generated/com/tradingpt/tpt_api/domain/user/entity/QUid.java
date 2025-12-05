package com.tradingpt.tpt_api.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUid is a Querydsl query type for Uid
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUid extends EntityPathBase<Uid> {

    private static final long serialVersionUID = -1847649297L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUid uid1 = new QUid("uid1");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final QCustomer customer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath exchangeName = createString("exchangeName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath uid = createString("uid");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QUid(String variable) {
        this(Uid.class, forVariable(variable), INITS);
    }

    public QUid(Path<? extends Uid> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUid(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUid(PathMetadata metadata, PathInits inits) {
        this(Uid.class, metadata, inits);
    }

    public QUid(Class<? extends Uid> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new QCustomer(forProperty("customer"), inits.get("customer")) : null;
    }

}

