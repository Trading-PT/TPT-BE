package com.tradingpt.tpt_api.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAdmin is a Querydsl query type for Admin
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAdmin extends EntityPathBase<Admin> {

    private static final long serialVersionUID = -1788088530L;

    public static final QAdmin admin = new QAdmin("admin");

    public final QUser _super = new QUser(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final StringPath email = _super.email;

    //inherited
    public final NumberPath<Long> id = _super.id;

    //inherited
    public final StringPath name = _super.name;

    //inherited
    public final StringPath nickname = _super.nickname;

    public final StringPath onelineIntroduction = createString("onelineIntroduction");

    //inherited
    public final StringPath password = _super.password;

    //inherited
    public final ListPath<PasswordHistory, QPasswordHistory> passwordHistories = _super.passwordHistories;

    public final StringPath phoneNumber = createString("phoneNumber");

    //inherited
    public final StringPath profileImageKey = _super.profileImageKey;

    //inherited
    public final StringPath profileImageUrl = _super.profileImageUrl;

    //inherited
    public final EnumPath<com.tradingpt.tpt_api.domain.user.enums.Provider> provider = _super.provider;

    //inherited
    public final StringPath providerId = _super.providerId;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final StringPath username = _super.username;

    //inherited
    public final NumberPath<Long> version = _super.version;

    public QAdmin(String variable) {
        super(Admin.class, forVariable(variable));
    }

    public QAdmin(Path<? extends Admin> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAdmin(PathMetadata metadata) {
        super(Admin.class, metadata);
    }

}

