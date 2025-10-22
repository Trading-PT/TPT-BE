package com.tradingpt.tpt_api.domain.leveltest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLevelTestResponse is a Querydsl query type for LevelTestResponse
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLevelTestResponse extends EntityPathBase<LevelTestResponse> {

    private static final long serialVersionUID = 87245405L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLevelTestResponse levelTestResponse = new QLevelTestResponse("levelTestResponse");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final StringPath answerText = createString("answerText");

    public final StringPath choiceNumber = createString("choiceNumber");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QLevelTestAttempt leveltestAttempt;

    public final QLevelTestQuestion leveltestQuestion;

    public final NumberPath<Integer> scoredAwarded = createNumber("scoredAwarded", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLevelTestResponse(String variable) {
        this(LevelTestResponse.class, forVariable(variable), INITS);
    }

    public QLevelTestResponse(Path<? extends LevelTestResponse> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLevelTestResponse(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLevelTestResponse(PathMetadata metadata, PathInits inits) {
        this(LevelTestResponse.class, metadata, inits);
    }

    public QLevelTestResponse(Class<? extends LevelTestResponse> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.leveltestAttempt = inits.isInitialized("leveltestAttempt") ? new QLevelTestAttempt(forProperty("leveltestAttempt"), inits.get("leveltestAttempt")) : null;
        this.leveltestQuestion = inits.isInitialized("leveltestQuestion") ? new QLevelTestQuestion(forProperty("leveltestQuestion")) : null;
    }

}

