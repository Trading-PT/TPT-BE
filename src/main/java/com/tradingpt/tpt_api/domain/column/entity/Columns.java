package com.tradingpt.tpt_api.domain.column.entity;

import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.global.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "columns")
public class Columns extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ColumnCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User user;   //작성자(어드민, 트레이너이므로 슈퍼클래스)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "column_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "subtitle")
    private String subtitle;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "thumbnail_image")
    private String thumbnailImage;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "is_best")
    private Boolean isBest;

    public void update(String title, String subtitle, String content, ColumnCategory category, User newUser) {
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.category = category;
        if (newUser != null) {
            this.user = newUser;
        }
    }

    public void incrementLikeCount(int newCount) {
        this.likeCount = newCount;
    }

    public void markBest() { this.isBest = true; }

    public void unmarkBest() {
        this.isBest = false;
    }
}