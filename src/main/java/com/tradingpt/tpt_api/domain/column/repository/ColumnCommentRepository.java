package com.tradingpt.tpt_api.domain.column.repository;

import com.tradingpt.tpt_api.domain.column.entity.Comment;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ColumnCommentRepository extends JpaRepository<Comment, Long> {

    @Query("""
        select cm
        from Comment cm
        join fetch cm.user
        where cm.columns.id = :columnId
        order by cm.createdAt asc
    """)
    List<Comment> findByColumnIdWithUser(@Param("columnId") Long columnId);

    @Query("""
           select c.columns.id as columnId, count(c.id) as cnt
           from Comment c
           where c.columns.id in :columnIds
           group by c.columns.id
           """)
    List<ColumnCommentCount> countByColumnIds(@Param("columnIds") List<Long> columnIds);

    public interface ColumnCommentCount {
        Long getColumnId();
        long getCnt();
    }
}
