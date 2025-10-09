package com.tradingpt.tpt_api.domain.consultation.service.command;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AdminConsultationCommandService {

    Long accept(Long consultationId);
    Long updateMemo(Long consultationId, String memo);

    /** 해당 날짜/시간을 차단하고 blockId 반환(이미 차단돼 있으면 기존 id 반환: 멱등) */
    Long createBlock(LocalDate date, LocalTime time);
    /** 해당 날짜/시간 차단 해제(없어도 에러 없이 0 처리 가능) */
    Long deleteBlock(LocalDate date, LocalTime time);
}
