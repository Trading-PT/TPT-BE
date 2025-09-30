package com.tradingpt.tpt_api.domain.feedbackrequest.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QPreCourseFeedbackDetail is a Querydsl query type for PreCourseFeedbackDetail
 */
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QPreCourseFeedbackDetail extends BeanPath<PreCourseFeedbackDetail> {

    private static final long serialVersionUID = 584292134L;

    public static final QPreCourseFeedbackDetail preCourseFeedbackDetail = new QPreCourseFeedbackDetail("preCourseFeedbackDetail");

    public final NumberPath<java.math.BigDecimal> entryPrice = createNumber("entryPrice", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> exitPrice = createNumber("exitPrice", java.math.BigDecimal.class);

    public final NumberPath<Integer> operatingFundsRatio = createNumber("operatingFundsRatio", Integer.class);

    public final StringPath positionEndReason = createString("positionEndReason");

    public final StringPath positionStartReason = createString("positionStartReason");

    public final NumberPath<Double> rnr = createNumber("rnr", Double.class);

    public final NumberPath<java.math.BigDecimal> settingStopLoss = createNumber("settingStopLoss", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> settingTakeProfit = createNumber("settingTakeProfit", java.math.BigDecimal.class);

    public QPreCourseFeedbackDetail(String variable) {
        super(PreCourseFeedbackDetail.class, forVariable(variable));
    }

    public QPreCourseFeedbackDetail(Path<? extends PreCourseFeedbackDetail> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPreCourseFeedbackDetail(PathMetadata metadata) {
        super(PreCourseFeedbackDetail.class, metadata);
    }

}

