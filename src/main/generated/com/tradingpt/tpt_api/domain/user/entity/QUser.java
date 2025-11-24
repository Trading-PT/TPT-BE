package com.tradingpt.tpt_api.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = -1442543604L;

    public static final QUser user = new QUser("user");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath nickname = createString("nickname");

    public final StringPath password = createString("password");

    public final ListPath<PasswordHistory, QPasswordHistory> passwordHistories = this.<PasswordHistory, QPasswordHistory>createList("passwordHistories", PasswordHistory.class, QPasswordHistory.class, PathInits.DIRECT2);

    public final StringPath profileImageKey = createString("profileImageKey");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.Provider> provider = createEnum("provider", com.tradingpt.tpt_api.domain.user.enums.Provider.class);

    public final StringPath providerId = createString("providerId");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath username = createString("username");

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

