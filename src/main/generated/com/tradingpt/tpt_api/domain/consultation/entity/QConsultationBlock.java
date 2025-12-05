package com.tradingpt.tpt_api.domain.consultation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QConsultationBlock is a Querydsl query type for ConsultationBlock
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QConsultationBlock extends EntityPathBase<ConsultationBlock> {

    private static final long serialVersionUID = 1577900261L;

    public static final QConsultationBlock consultationBlock = new QConsultationBlock("consultationBlock");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final DatePath<java.time.LocalDate> consultationBlockDate = createDate("consultationBlockDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> consultationBlockTime = createTime("consultationBlockTime", java.time.LocalTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QConsultationBlock(String variable) {
        super(ConsultationBlock.class, forVariable(variable));
    }

    public QConsultationBlock(Path<? extends ConsultationBlock> path) {
        super(path.getType(), path.getMetadata());
    }

    public QConsultationBlock(PathMetadata metadata) {
        super(ConsultationBlock.class, metadata);
    }

}

