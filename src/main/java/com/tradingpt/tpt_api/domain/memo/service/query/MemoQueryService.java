package com.tradingpt.tpt_api.domain.memo.service.query;

import com.tradingpt.tpt_api.domain.memo.dto.response.MemoResponseDTO;

public interface MemoQueryService {

    /**
     * 내 메모 조회
     * @param customerId 고객 ID
     * @return 메모 응답 DTO
     */
    MemoResponseDTO getMemo(Long customerId);
}
