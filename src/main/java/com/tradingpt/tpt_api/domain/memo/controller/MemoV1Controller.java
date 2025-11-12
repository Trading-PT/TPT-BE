package com.tradingpt.tpt_api.domain.memo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tradingpt.tpt_api.domain.memo.dto.request.MemoRequestDTO;
import com.tradingpt.tpt_api.domain.memo.dto.response.MemoResponseDTO;
import com.tradingpt.tpt_api.domain.memo.service.command.MemoCommandService;
import com.tradingpt.tpt_api.domain.memo.service.query.MemoQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/memos")
@RequiredArgsConstructor
@Tag(name = "메모 (Memo)", description = "마이페이지 메모 관리 API")
public class MemoV1Controller {

    private final MemoQueryService memoQueryService;
    private final MemoCommandService memoCommandService;

    @Operation(
        summary = "내 메모 조회",
        description = """
            로그인한 사용자의 메모를 조회합니다.

            메모가 없으면 404 에러를 반환합니다.
            """
    )
    @GetMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public BaseResponse<MemoResponseDTO> getMemo(
        @AuthenticationPrincipal(expression = "id") Long customerId
    ) {
        return BaseResponse.onSuccess(memoQueryService.getMemo(customerId));
    }

    @Operation(
        summary = "메모 생성/수정 (Upsert)",
        description = """
            메모가 없으면 새로 생성하고, 이미 있으면 수정합니다.

            특징:
            - 사용자는 하나의 메모만 가질 수 있습니다
            - 메모 ID 없이 요청합니다
            - 기존 메모가 있으면 자동으로 수정됩니다
            """
    )
    @PutMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public BaseResponse<MemoResponseDTO> createOrUpdateMemo(
        @AuthenticationPrincipal(expression = "id") Long customerId,
        @Valid @RequestBody MemoRequestDTO request
    ) {
        return BaseResponse.onSuccess(
            memoCommandService.createOrUpdateMemo(customerId, request)
        );
    }

    @Operation(
        summary = "메모 삭제",
        description = """
            로그인한 사용자의 메모를 삭제합니다.

            메모가 없으면 404 에러를 반환합니다.
            """
    )
    @DeleteMapping
    @PreAuthorize("hasRole('ROLE_CUSTOMER')")
    public BaseResponse<Void> deleteMemo(
        @AuthenticationPrincipal(expression = "id") Long customerId
    ) {
        memoCommandService.deleteMemo(customerId);
        return BaseResponse.onSuccessDelete(null);
    }
}
