package com.tradingpt.tpt_api.domain.memo.dto.response;

import java.time.LocalDateTime;

import com.tradingpt.tpt_api.domain.memo.entity.Memo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "메모 응답 DTO")
public class MemoResponseDTO {

    @Schema(description = "메모 ID", example = "1")
    private Long id;

    @Schema(description = "메모 제목", example = "오늘의 트레이딩 포인트")
    private String title;

    @Schema(description = "메모 내용", example = "- 손절가 설정 잊지 말기\n- 감정적 트레이딩 주의")
    private String content;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    /**
     * Memo 엔티티를 MemoResponseDTO로 변환
     * @param memo 메모 엔티티
     * @return MemoResponseDTO
     */
    public static MemoResponseDTO from(Memo memo) {
        return MemoResponseDTO.builder()
            .id(memo.getId())
            .title(memo.getTitle())
            .content(memo.getContent())
            .createdAt(memo.getCreatedAt())
            .updatedAt(memo.getUpdatedAt())
            .build();
    }
}
