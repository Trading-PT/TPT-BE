package com.tradingpt.tpt_api.domain.monthlytradingsummary.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMonthlyPeriod is a Querydsl query type for MonthlyPeriod
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QMonthlyPeriod extends BeanPath<MonthlyPeriod> {

    private static final long serialVersionUID = 1631956568L;

    public static final QMonthlyPeriod monthlyPeriod = new QMonthlyPeriod("monthlyPeriod");

    public final NumberPath<Integer> month = createNumber("month", Integer.class);

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public QMonthlyPeriod(String variable) {
        super(MonthlyPeriod.class, forVariable(variable));
    }

    public QMonthlyPeriod(Path<? extends MonthlyPeriod> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMonthlyPeriod(PathMetadata metadata) {
        super(MonthlyPeriod.class, metadata);
    }

}

