package com.tradingpt.tpt_api.domain.review.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.tradingpt.tpt_api.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@DynamicUpdate
@DynamicInsert
@Table(name = "review_tag")
public class ReviewTag extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "review_tag_id")
	private Long id;

	private String name;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String description;
}
