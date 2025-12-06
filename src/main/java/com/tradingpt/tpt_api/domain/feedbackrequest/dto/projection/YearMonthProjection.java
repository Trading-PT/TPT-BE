package com.tradingpt.tpt_api.domain.feedbackrequest.dto.projection;

import java.time.YearMonth;

/**
 * FeedbackRequest의 연/월 정보를 담는 Projection DTO
 * QueryDSL Projections.constructor로 생성됨
 */
public record YearMonthProjection(Integer year, Integer month) {

    public YearMonth toYearMonth() {
        return YearMonth.of(year, month);
    }
}
