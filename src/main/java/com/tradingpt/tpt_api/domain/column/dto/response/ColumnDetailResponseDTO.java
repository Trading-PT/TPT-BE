package com.tradingpt.tpt_api.domain.column.dto.response;

import com.tradingpt.tpt_api.domain.column.entity.Columns;
import com.tradingpt.tpt_api.domain.column.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "칼럼 상세 조회 응답")
public class ColumnDetailResponseDTO {

    @Schema(description = "칼럼 ID", example = "10")
    private Long columnId;

    @Schema(description = "카테고리명", example = "ETF")
    private String categoryName;

    @Schema(description = "제목", example = "어떻게 하면 바짝 벌고 퇴사할 것인가")
    private String title;

    @Schema(description = "부제목", example = "공부와 준비")
    private String subtitle;

    @Schema(description = "작성자 이름", example = "이조교")
    private String writerName;

    @Schema(description = "작성자 이미지 url", example = "https://dfalkdfhkfjd.com")
    private String profileImageUrl;

    @Schema(description = "베스트인지 여부", example = "true")
    private Boolean isBest;

    @Schema(description = "작성시각", example = "2025-06-14T13:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정시각", example = "2025-06-14T13:10:00")
    private LocalDateTime updatedAt;

    @Schema(description = "좋아요 수", example = "99")
    private Integer likeCount;

    @Schema(description = "댓글 수", example = "13")
    private int commentCount;

    @Schema(description = "본문")
    private String content;

    @Schema(description = "댓글 목록(작성시각 오름차순)")
    private List<CommentItem> comments;

    // 정적 팩토리 메서드
    public static ColumnDetailResponseDTO from(Columns column, List<Comment> comments) {
        return ColumnDetailResponseDTO.builder()
                .columnId(column.getId())
                .categoryName(column.getCategory().getName())
                .title(column.getTitle())
                .subtitle(column.getSubtitle())
                .writerName(column.getUser().getName())
                .profileImageUrl(column.getUser().getProfileImageUrl())
                .createdAt(column.getCreatedAt())
                .updatedAt(column.getUpdatedAt())
                .likeCount(column.getLikeCount())
                .commentCount(comments.size())
                .content(column.getContent())
                .isBest(column.getIsBest())
                .comments(
                        comments.stream()
                                .map(CommentItem::from)
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Getter
    @Builder
    @Schema(description = "댓글 DTO")
    public static class CommentItem {
        private Long commentId;
        private String writerName;
        private String content;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        // Comment 엔티티 → DTO 변환
        public static CommentItem from(Comment comment) {
            return CommentItem.builder()
                    .commentId(comment.getId())
                    .writerName(comment.getWriterName())
                    .content(comment.getContent())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
        }
    }
}
