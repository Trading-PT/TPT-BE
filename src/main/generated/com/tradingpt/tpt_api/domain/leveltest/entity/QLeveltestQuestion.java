package com.tradingpt.tpt_api.domain.leveltest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLevelTestQuestion is a Querydsl query type for LevelTestQuestion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLevelTestQuestion extends EntityPathBase<LevelTestQuestion> {

    private static final long serialVersionUID = -738301438L;

    public static final QLevelTestQuestion levelTestQuestion = new QLevelTestQuestion("levelTestQuestion");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final StringPath answerText = createString("answerText");

    public final StringPath choice1 = createString("choice1");

    public final StringPath choice2 = createString("choice2");

    public final StringPath choice3 = createString("choice3");

    public final StringPath choice4 = createString("choice4");

    public final StringPath choice5 = createString("choice5");

    public final StringPath content = createString("content");

    public final StringPath correctChoiceNum = createString("correctChoiceNum");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageKey = createString("imageKey");

    public final StringPath imageUrl = createString("imageUrl");

    public final EnumPath<com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType> problemType = createEnum("problemType", com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType.class);

    public final NumberPath<Integer> score = createNumber("score", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLevelTestQuestion(String variable) {
        super(LevelTestQuestion.class, forVariable(variable));
    }

    public QLevelTestQuestion(Path<? extends LevelTestQuestion> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLevelTestQuestion(PathMetadata metadata) {
        super(LevelTestQuestion.class, metadata);
    }

}

