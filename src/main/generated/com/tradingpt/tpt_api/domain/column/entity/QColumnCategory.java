package com.tradingpt.tpt_api.domain.column.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QColumnCategory is a Querydsl query type for ColumnCategory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QColumnCategory extends EntityPathBase<ColumnCategory> {

    private static final long serialVersionUID = -1597724544L;

    public static final QColumnCategory columnCategory = new QColumnCategory("columnCategory");

    public final StringPath color = createString("color");

    public final ListPath<Columns, QColumns> columns = this.<Columns, QColumns>createList("columns", Columns.class, QColumns.class, PathInits.DIRECT2);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public QColumnCategory(String variable) {
        super(ColumnCategory.class, forVariable(variable));
    }

    public QColumnCategory(Path<? extends ColumnCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QColumnCategory(PathMetadata metadata) {
        super(ColumnCategory.class, metadata);
    }

}

