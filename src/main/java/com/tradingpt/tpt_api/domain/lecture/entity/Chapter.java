package com.tradingpt.tpt_api.domain.lecture.entity;

import com.tradingpt.tpt_api.domain.lecture.enums.ChapterType;
import com.tradingpt.tpt_api.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "chapter")
public class Chapter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** 전체 커리큘럼 내 챕터 순서 (1, 2, 3, ...) */
    @Column(name = "chapter_order", nullable = false)
    private Integer chapterOrder;

    /** 유료/무료 여부 (BASIC, PRO) */
    @Enumerated(EnumType.STRING)
    @Column(name = "chapter_type", nullable = false)
    private ChapterType chapterType;

    public void update(String title,
                       String description,
                       Integer chapterOrder,
                       ChapterType chapterType) {

        if (title != null) {
            this.title = title;
        }

        if (description != null) {
            this.description = description;
        }

        if (chapterOrder != null) {
            this.chapterOrder = chapterOrder;
        }

        if (chapterType != null) {
            this.chapterType = chapterType;
        }
    }
}
