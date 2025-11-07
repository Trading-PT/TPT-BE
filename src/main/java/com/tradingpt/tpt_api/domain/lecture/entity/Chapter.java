package com.tradingpt.tpt_api.domain.lecture.entity;

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

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="chapter_id")
    private Long id;

    @Column(name="title", nullable=false)
    private String title;

    @Column(name="description", columnDefinition="TEXT")
    private String description;
}
