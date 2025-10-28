package com.tradingpt.tpt_api.domain.column.repository;

import com.tradingpt.tpt_api.domain.column.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColumnCommentRepository extends JpaRepository<Comment,Long> {
}
