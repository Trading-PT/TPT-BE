package com.tradingpt.tpt_api.domain.lecture.entity;

import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.lecture.enums.LectureExposure;
import com.tradingpt.tpt_api.global.common.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
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

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** S3 영상 접근용 URL */
    @Column(name = "video_url", length = 500)
    private String videoUrl;

    /** S3 영상 삭제용 key */
    @Column(name = "video_key", length = 500)
    private String videoKey;

    @Builder.Default
    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds = 0;  // 강의 총 길이

    /** 챕터 내 정렬(1..N) */
    @Column(name = "lecture_order", nullable = false)
    private Integer lectureOrder;

    /** 강의 노출 정책 **/
    @Enumerated(EnumType.STRING)
    @Column(name = "lecture_exposure", nullable = false)
    private LectureExposure lectureExposure;

    // Lecture.java 안에
    public void update(
            Chapter chapter,
            User trainer,
            String title,
            String content,
            String videoUrl,
            String videoKey,
            Integer durationSeconds,
            Integer lectureOrder,
            LectureExposure exposure
    ) {
        this.chapter = chapter;
        this.trainer = trainer;
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
        this.videoKey = videoKey;
        this.durationSeconds = (durationSeconds != null) ? durationSeconds : 0;
        this.lectureOrder = lectureOrder;
        this.lectureExposure = exposure;
    }
}
