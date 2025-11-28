package com.tradingpt.tpt_api.domain.lecture.entity;

import com.tradingpt.tpt_api.domain.lecture.enums.LectureExposure;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.global.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "lecture")
public class Lecture extends BaseEntity {

    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LectureAttachment> attachments = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "thumbnail_key")
    private String thumbnailKey;

    /** S3 영상 접근용 URL */
    @Column(name = "video_url")
    private String videoUrl;

    /** S3 영상 삭제용 key */
    @Column(name = "video_key")
    private String videoKey;

    @Builder.Default
    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds = 0;

    /** 챕터 내 정렬(1..N) */
    @Column(name = "lecture_order", nullable = false)
    private Integer lectureOrder;

    /** 강의 노출 정책 **/
    @Enumerated(EnumType.STRING)
    @Column(name = "lecture_exposure", nullable = false)
    private LectureExposure lectureExposure;

    /**  수강에 필요한 토큰 수 (무료 = 0) */
    @Builder.Default
    @Column(name = "required_tokens", nullable = false)
    private Integer requiredTokens = 0;


    public void update(
            Chapter chapter,
            User trainer,
            String title,
            String content,
            String videoKey,
            Integer durationSeconds,
            Integer lectureOrder,
            LectureExposure exposure,
            Integer requiredTokens,
            String thumbnailUrl
    ) {
        this.chapter = chapter;
        this.trainer = trainer;
        this.title = title;
        this.content = content;
        this.videoKey = videoKey;
        this.durationSeconds = (durationSeconds != null) ? durationSeconds : 0;
        this.lectureOrder = lectureOrder;
        this.lectureExposure = exposure;
        this.requiredTokens = (requiredTokens != null) ? requiredTokens : 0;
        this.thumbnailUrl = thumbnailUrl;
    }
}
