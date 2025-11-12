package com.tradingpt.tpt_api.domain.memo.service.query;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.memo.dto.response.MemoResponseDTO;
import com.tradingpt.tpt_api.domain.memo.entity.Memo;
import com.tradingpt.tpt_api.domain.memo.exception.MemoErrorStatus;
import com.tradingpt.tpt_api.domain.memo.exception.MemoException;
import com.tradingpt.tpt_api.domain.memo.repository.MemoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemoQueryServiceImpl implements MemoQueryService {

    private final MemoRepository memoRepository;

    /**
     * 내 메모 조회
     * @param customerId 고객 ID
     * @return 메모 응답 DTO
     */
    @Override
    public MemoResponseDTO getMemo(Long customerId) {
        Memo memo = memoRepository.findByCustomer_Id(customerId)
            .orElseThrow(() -> new MemoException(MemoErrorStatus.MEMO_NOT_FOUND));
        return MemoResponseDTO.from(memo);
    }
}
