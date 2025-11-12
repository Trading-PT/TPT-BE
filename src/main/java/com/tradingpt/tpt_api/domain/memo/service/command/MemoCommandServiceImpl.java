package com.tradingpt.tpt_api.domain.memo.service.command;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tradingpt.tpt_api.domain.memo.dto.request.MemoRequestDTO;
import com.tradingpt.tpt_api.domain.memo.dto.response.MemoResponseDTO;
import com.tradingpt.tpt_api.domain.memo.entity.Memo;
import com.tradingpt.tpt_api.domain.memo.exception.MemoErrorStatus;
import com.tradingpt.tpt_api.domain.memo.exception.MemoException;
import com.tradingpt.tpt_api.domain.memo.repository.MemoRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemoCommandServiceImpl implements MemoCommandService {

    private final MemoRepository memoRepository;
    private final CustomerRepository customerRepository;

    /**
     * 메모 생성 또는 수정 (Upsert)
     * - 메모가 없으면 생성
     * - 메모가 있으면 수정
     * @param customerId 고객 ID
     * @param request 메모 요청 DTO
     * @return 메모 응답 DTO
     */
    @Override
    @Transactional
    public MemoResponseDTO createOrUpdateMemo(Long customerId, MemoRequestDTO request) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new UserException(UserErrorStatus.CUSTOMER_NOT_FOUND));

        Memo memo = memoRepository.findByCustomer_Id(customerId)
            .map(existingMemo -> updateMemo(existingMemo, request))
            .orElseGet(() -> createMemo(customer, request));

        return MemoResponseDTO.from(memo);
    }

    /**
     * 메모 삭제
     * @param customerId 고객 ID
     */
    @Override
    @Transactional
    public void deleteMemo(Long customerId) {
        Memo memo = memoRepository.findByCustomer_Id(customerId)
            .orElseThrow(() -> new MemoException(MemoErrorStatus.MEMO_NOT_FOUND));
        memoRepository.delete(memo);
    }

    /**
     * 메모 생성 헬퍼 메서드
     */
    private Memo createMemo(Customer customer, MemoRequestDTO request) {
        Memo memo = Memo.builder()
            .customer(customer)
            .title(request.getTitle())
            .content(request.getContent())
            .build();
        return memoRepository.save(memo);
    }

    /**
     * 메모 수정 헬퍼 메서드
     */
    private Memo updateMemo(Memo memo, MemoRequestDTO request) {
        memo.update(request.getTitle(), request.getContent());
        return memo;
    }
}
