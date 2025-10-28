package com.tradingpt.tpt_api.domain.column.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "column_category")
public class ColumnCategory {

    // 카테고리 삭제 시, 해당 카테고리에 포함된 칼럼도 함께 삭제
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Columns> columns = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "column_category_id")
    private Long id;

    @Column(name = "category_name")
    private String name;

    @Column(name = "color")
    private String color;

    public void update(String name, String color) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (color != null && !color.isBlank()) {
            this.color = color;
        }
    }
}
