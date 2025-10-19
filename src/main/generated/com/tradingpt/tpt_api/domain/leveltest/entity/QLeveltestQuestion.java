package com.tradingpt.tpt_api.domain.leveltest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QLeveltestQuestion is a Querydsl query type for LeveltestQuestion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QLeveltestQuestion extends EntityPathBase<LeveltestQuestion> {

    private static final long serialVersionUID = -902621726L;

    public static final QLeveltestQuestion leveltestQuestion = new QLeveltestQuestion("leveltestQuestion");

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

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageKey = createString("imageKey");

    public final StringPath imageUrl = createString("imageUrl");

    public final EnumPath<com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType> problemType = createEnum("problemType", com.tradingpt.tpt_api.domain.leveltest.enums.ProblemType.class);

    public final NumberPath<Integer> score = createNumber("score", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QLeveltestQuestion(String variable) {
        super(LeveltestQuestion.class, forVariable(variable));
    }

    public QLeveltestQuestion(Path<? extends LeveltestQuestion> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLeveltestQuestion(PathMetadata metadata) {
        super(LeveltestQuestion.class, metadata);
    }

}

