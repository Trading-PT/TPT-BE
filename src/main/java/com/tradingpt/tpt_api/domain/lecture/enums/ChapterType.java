package com.tradingpt.tpt_api.domain.lecture.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ChapterType {
    REGULAR(false),  // 무료
    PRO(true);     // 유료

    private final boolean isPaid;
}
