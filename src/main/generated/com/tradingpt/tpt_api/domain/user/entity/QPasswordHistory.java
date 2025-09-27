package com.tradingpt.tpt_api.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPasswordHistory is a Querydsl query type for PasswordHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPasswordHistory extends EntityPathBase<PasswordHistory> {

    private static final long serialVersionUID = 783519384L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPasswordHistory passwordHistory = new QPasswordHistory("passwordHistory");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath passwordHash = createString("passwordHash");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final QUser user;

    public QPasswordHistory(String variable) {
        this(PasswordHistory.class, forVariable(variable), INITS);
    }

    public QPasswordHistory(Path<? extends PasswordHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPasswordHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPasswordHistory(PathMetadata metadata, PathInits inits) {
        this(PasswordHistory.class, metadata, inits);
    }

    public QPasswordHistory(Class<? extends PasswordHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

