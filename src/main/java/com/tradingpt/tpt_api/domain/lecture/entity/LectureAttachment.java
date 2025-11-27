package com.tradingpt.tpt_api.domain.lecture.entity;

import com.tradingpt.tpt_api.domain.lecture.enums.LectureAttachmentType;
import com.tradingpt.tpt_api.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "lecture_attachment")
public class LectureAttachment extends BaseEntity {

    /** 어떤 강의에 속한 첨부파일인지 (N:1 관계) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_attachment_id")
    private Long id;

    /** 첨부파일 URL (S3 접근용) */
    @Column(name = "file_url", nullable = true)
    private String fileUrl;

    /** S3 객체 삭제용 key */
    @Column(name = "file_key", nullable = false)
    private String fileKey;

    /** 첨부파일 타입 (과제/일반 구분) */
    @Enumerated(EnumType.STRING)
    @Column(name = "attachment_type", nullable = false)
    private LectureAttachmentType attachmentType;
}

