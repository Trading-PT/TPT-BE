package com.tradingpt.tpt_api.domain.column.service.command;

import com.tradingpt.tpt_api.domain.column.dto.request.CommentRequestDTO;
import com.tradingpt.tpt_api.domain.column.entity.Columns;
import com.tradingpt.tpt_api.domain.column.entity.Comment;
import com.tradingpt.tpt_api.domain.column.exception.ColumnErrorStatus;
import com.tradingpt.tpt_api.domain.column.exception.ColumnException;
import com.tradingpt.tpt_api.domain.column.repository.ColumnCommentRepository;
import com.tradingpt.tpt_api.domain.column.repository.ColumnsRepository;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ColumnCommandServiceImpl implements ColumnCommandService {

    private final ColumnsRepository columnsRepository;
    private final ColumnCommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public int increaseLikeCount(Long columnId) {
        Columns column = columnsRepository.findById(columnId)
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.NOT_FOUND));

        int currentCount = column.getLikeCount();

        // 상한값 제한
        if (currentCount >= 10000) {
            throw new ColumnException(ColumnErrorStatus.LIKE_LIMIT_EXCEEDED);
        }

        int newCount = currentCount + 1;
        column.incrementLikeCount(newCount);

        return newCount;
    }

    @Override
    @Transactional
    public Long createComment(Long columnId, Long userId, CommentRequestDTO request) {
        Columns column = columnsRepository.findById(columnId)
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.WRITER_NOT_FOUND));

        Comment comment = Comment.builder()
                .columns(column)
                .user(user)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);
        return saved.getId();
    }

    @Override
    @Transactional
    public Long updateComment(Long commentId, Long userId, CommentRequestDTO request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId))
            throw new ColumnException(ColumnErrorStatus.UNAUTHORIZED);

        comment.updateContent(request.getContent());
        return comment.getId();
    }

    @Override
    @Transactional
    public Long deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId))
            throw new ColumnException(ColumnErrorStatus.UNAUTHORIZED);

        commentRepository.delete(comment);
        return comment.getId();
    }
}
