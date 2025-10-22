package com.tradingpt.tpt_api.domain.leveltest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QLeveltestResponse is a Querydsl query type for LeveltestResponse
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLeveltestResponse extends EntityPathBase<LeveltestResponse> {

    private static final long serialVersionUID = -77074883L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QLeveltestResponse leveltestResponse = new QLeveltestResponse("leveltestResponse");

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

    public QLeveltestResponse(String variable) {
        this(LeveltestResponse.class, forVariable(variable), INITS);
    }

    public QLeveltestResponse(Path<? extends LeveltestResponse> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QLeveltestResponse(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QLeveltestResponse(PathMetadata metadata, PathInits inits) {
        this(LeveltestResponse.class, metadata, inits);
    }

    public QLeveltestResponse(Class<? extends LeveltestResponse> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.leveltestAttempt = inits.isInitialized("leveltestAttempt") ? new QLevelTestAttempt(forProperty("leveltestAttempt"), inits.get("leveltestAttempt")) : null;
        this.leveltestQuestion = inits.isInitialized("leveltestQuestion") ? new QLevelTestQuestion(forProperty("leveltestQuestion")) : null;
    }

}

