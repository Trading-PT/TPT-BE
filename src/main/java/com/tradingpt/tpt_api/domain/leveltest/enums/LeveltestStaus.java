package com.tradingpt.tpt_api.domain.leveltest.enums;

public enum LeveltestStaus {
    SUBMITTED,GRADING,GRADED    //제출은 됬지만 채점 전, (다른 서버 인스턴스가 중복 채점 방지를 위한) 채점 진행중,  채점 후
}
