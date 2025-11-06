package com.tradingpt.tpt_api.domain.column.dto.response;

import com.tradingpt.tpt_api.domain.column.entity.Columns;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "칼럼 목록 응답 DTO")
public class ColumnListResponseDTO {

    @Schema(description = "칼럼 ID", example = "15")
    private Long columnId;

    @Schema(description = "카테고리 이름", example = "ETF")
    private String categoryName;

    @Schema(description = "칼럼 제목", example = "첫문장 10글자로 시작하는 칼럼 제목")
    private String title;

    @Schema(description = "부제목", example = "투자 습관 만들기")
    private String subtitle;

    @Schema(description = "썸네일 이미지", example = "썸네일 이미지")
    private String thumbnailImage;

    @Schema(description = "좋아요 수", example = "120")
    private int likeCount;

    @Schema(description = "댓글 수", example = "15")
    private long commentCount;

    @Schema(description = "작성자 이름", example = "이직원")
    private String writerName;

    @Schema(description = "베스트인지 여부", example = "true")
    private Boolean isBest;

    @Schema(description = "작성일시", example = "2025-06-14T13:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2025-06-14T14:15:00")
    private LocalDateTime updatedAt;

    public static ColumnListResponseDTO from(Columns c, long commentCount) {
        return ColumnListResponseDTO.builder()
                .columnId(c.getId())
                .categoryName(c.getCategory() != null ? c.getCategory().getName() : null)
                .title(c.getTitle())
                .subtitle(c.getSubtitle())
                .thumbnailImage(c.getThumbnailImage())
                .likeCount(c.getLikeCount())
                .commentCount(commentCount)
                .writerName(c.getUser() != null ? c.getUser().getName() : null)
                .isBest(c.getIsBest())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
