package com.tradingpt.tpt_api.domain.column.controller;

import com.tradingpt.tpt_api.domain.column.dto.request.CommentRequestDTO;
import com.tradingpt.tpt_api.domain.column.service.command.ColumnCommandService;
import com.tradingpt.tpt_api.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/columns")
@RequiredArgsConstructor
@Tag(name = "칼럼(사용자)", description = "칼럼 조회 및 좋아요,댓글 API")
public class ColumnV1Controller {

    private final ColumnCommandService commandService; // 좋아요 증가 로직 재사용

    @Operation(
            summary = "칼럼 좋아요 수 증가",
            description = "특정 칼럼의 좋아요 수를 1 증가시킵니다. 10,000을 초과하면 예외가 발생합니다."
    )
    @PostMapping("/{columnId}/likes")
    public ResponseEntity<BaseResponse<Integer>> increaseLikeCount(
            @PathVariable Long columnId
    ) {
        int updatedCount = commandService.increaseLikeCount(columnId);
        return ResponseEntity.ok(BaseResponse.onSuccess(updatedCount));
    }

    @Operation(summary = "댓글 작성", description = "특정 칼럼에 댓글을 작성합니다.")
    @PostMapping("/{columnId}/comments")
    public ResponseEntity<BaseResponse<Long>> createComment(
            @PathVariable Long columnId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestBody @Valid CommentRequestDTO request
    ) {
        Long id = commandService.createComment(columnId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.onSuccess(id));
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글만 수정할 수 있습니다.")
    @PutMapping("/{columnId}/comments/{commentId}")
    public ResponseEntity<BaseResponse<Long>> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestBody @Valid CommentRequestDTO request
    ) {
        Long id = commandService.updateComment(commentId, userId, request);
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }

    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글만 삭제할 수 있습니다.")
    @DeleteMapping("/{columnId}/comments/{commentId}")
    public ResponseEntity<BaseResponse<Long>> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal(expression = "id") Long userId
    ) {
        Long id = commandService.deleteComment(commentId, userId);
        return ResponseEntity.ok(BaseResponse.onSuccess(id));
    }
}
