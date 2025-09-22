package com.tradingpt.tpt_api.domain.weeklytradingsummary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWeeklyPeriod is a Querydsl query type for WeeklyPeriod
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QWeeklyPeriod extends BeanPath<WeeklyPeriod> {

    private static final long serialVersionUID = -1330305602L;

    public static final QWeeklyPeriod weeklyPeriod = new QWeeklyPeriod("weeklyPeriod");

    public final NumberPath<Integer> month = createNumber("month", Integer.class);

    public final NumberPath<Integer> week = createNumber("week", Integer.class);

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public QWeeklyPeriod(String variable) {
        super(WeeklyPeriod.class, forVariable(variable));
    }

    public QWeeklyPeriod(Path<? extends WeeklyPeriod> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWeeklyPeriod(PathMetadata metadata) {
        super(WeeklyPeriod.class, metadata);
    }

}

