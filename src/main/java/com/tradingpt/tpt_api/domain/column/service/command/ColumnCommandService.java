package com.tradingpt.tpt_api.domain.column.service.command;

import com.tradingpt.tpt_api.domain.column.dto.request.CommentRequestDTO;

public interface ColumnCommandService {
    int increaseLikeCount(Long columnId);
    Long createComment(Long columnId, Long userId, CommentRequestDTO request);
    Long updateComment(Long commentId, Long userId, CommentRequestDTO request);
    Long deleteComment(Long commentId, Long userId);
}
