package com.tradingpt.tpt_api.domain.column.controller;

import com.tradingpt.tpt_api.domain.auth.security.AuthSessionUser;
import com.tradingpt.tpt_api.domain.column.dto.request.ColumnCategoryRequestDTO;
import com.tradingpt.tpt_api.domain.column.dto.request.ColumnCreateRequestDTO;
import com.tradingpt.tpt_api.domain.column.dto.request.ColumnUpdateRequestDTO;
import com.tradingpt.tpt_api.domain.column.service.command.AdminColumnCommandService;
import com.tradingpt.tpt_api.domain.column.service.query.AdminColumnQueryService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/columns")
@RequiredArgsConstructor
@Tag(name = "칼럼(어드민)", description = "칼럼 관리 API (작성/수정은 관리자·트레이너, 삭제는 관리자 전용)")
public class AdminV1ColumnController {

    private final AdminColumnCommandService commandService;
//    private final AdminColumnQueryService queryService;

    @Operation(
            summary = "칼럼 작성",
            description = "관리자 또는 트레이너가 칼럼을 작성합니다. 요청 본문에 제목/내용/카테고리를 포함합니다."
    )
    @PostMapping
    public ResponseEntity<BaseResponse<Long>> createColumn(
            @AuthenticationPrincipal(expression = "id") Long writerUserId,
            @AuthenticationPrincipal AuthSessionUser user,
            @RequestBody @Valid ColumnCreateRequestDTO request
    ) {
        Long id = commandService.createColumn(writerUserId, request, user.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccess(id));
    }

    @Operation(
            summary = "칼럼 수정",
            description = "관리자 또는 작성자가 칼럼을 수정합니다. 제목/내용/카테고리를 갱신합니다."
    )
    @PutMapping("/{columnId}")
    public ResponseEntity<BaseResponse<Long>> updateColumn(
            @PathVariable Long columnId,
            @AuthenticationPrincipal(expression = "id") Long editorUserId,
            @AuthenticationPrincipal AuthSessionUser user,
            @RequestBody @Valid ColumnUpdateRequestDTO request
    ) {
        Long id = commandService.updateColumn(columnId, editorUserId, request,user.role());
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }

    @Operation(
            summary = "칼럼 삭제(관리자 전용)",
            description = "해당 칼럼을 삭제합니다. 관리자 권한이 필요합니다."
    )
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{columnId}")
    public ResponseEntity<BaseResponse<Long>> deleteColumn(
            @PathVariable Long columnId
    ) {
        Long id = commandService.deleteColumn(columnId);
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }

    @Operation(summary = "칼럼 카테고리 생성", description = "새로운 칼럼 카테고리를 생성합니다.")
    @PostMapping("/categories")
    public ResponseEntity<BaseResponse<Long>> createCategory(
            @RequestBody @Valid ColumnCategoryRequestDTO request
    ) {
        Long id = commandService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccess(id));
    }

    @Operation(summary = "칼럼 카테고리 수정", description = "기존 칼럼 카테고리의 이름이나 색상을 수정합니다.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<BaseResponse<Long>> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody @Valid ColumnCategoryRequestDTO request
    ) {
        Long id = commandService.updateCategory(categoryId, request);
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }

    @Operation(summary = "칼럼 카테고리 삭제", description = "해당 카테고리를 삭제합니다. 포함된 칼럼도 함께 삭제됩니다.")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<BaseResponse<Long>> deleteCategory(@PathVariable Long categoryId) {
        Long id = commandService.deleteCategory(categoryId);
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }
}
