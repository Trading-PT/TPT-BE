package com.tradingpt.tpt_api.domain.memo.service.command;

import com.tradingpt.tpt_api.domain.memo.dto.request.MemoRequestDTO;
import com.tradingpt.tpt_api.domain.memo.dto.response.MemoResponseDTO;

public interface MemoCommandService {

    /**
     * 메모 생성 또는 수정 (Upsert)
     * @param customerId 고객 ID
     * @param request 메모 요청 DTO
     * @return 메모 응답 DTO
     */
    MemoResponseDTO createOrUpdateMemo(Long customerId, MemoRequestDTO request);

    /**
     * 메모 삭제
     * @param customerId 고객 ID
     */
    void deleteMemo(Long customerId);
}
