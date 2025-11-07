package com.tradingpt.tpt_api.domain.lecture.entity;

import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter @SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
@Table(name = "lecture_progress")
public class LectureProgress extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_progress_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false)
    private Customer customer;

    /** 누적 시청 시간(초) — 서버는 0~durationSeconds로 클램프 */
    @Builder.Default
    @Column(name = "watched_seconds", nullable = false)
    private Integer watchedSeconds = 0;

    /** 마지막 재생 위치(초) — 재접속 시 이어보기 */
    @Builder.Default
    @Column(name = "last_position_seconds", nullable = false)
    private Integer lastPositionSeconds = 0;

    /** 완강 여부 */
    @Builder.Default
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "last_watched_at")
    private LocalDateTime lastWatchedAt;

}
