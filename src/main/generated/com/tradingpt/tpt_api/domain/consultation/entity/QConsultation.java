package com.tradingpt.tpt_api.domain.consultation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QConsultation is a Querydsl query type for Consultation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QConsultation extends EntityPathBase<Consultation> {

    private static final long serialVersionUID = 646254728L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QConsultation consultation = new QConsultation("consultation");

    public final com.tradingpt.tpt_api.global.common.QBaseEntity _super = new com.tradingpt.tpt_api.global.common.QBaseEntity(this);

    public final DatePath<java.time.LocalDate> consultationDate = createDate("consultationDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> consultationTime = createTime("consultationTime", java.time.LocalTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final com.tradingpt.tpt_api.domain.user.entity.QCustomer customer;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isProcessed = createBoolean("isProcessed");

    public final StringPath memo = createString("memo");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QConsultation(String variable) {
        this(Consultation.class, forVariable(variable), INITS);
    }

    public QConsultation(Path<? extends Consultation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QConsultation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QConsultation(PathMetadata metadata, PathInits inits) {
        this(Consultation.class, metadata, inits);
    }

    public QConsultation(Class<? extends Consultation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.tradingpt.tpt_api.domain.user.entity.QCustomer(forProperty("customer"), inits.get("customer")) : null;
    }

}

