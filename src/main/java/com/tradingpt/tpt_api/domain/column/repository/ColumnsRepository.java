package com.tradingpt.tpt_api.domain.column.repository;

import com.tradingpt.tpt_api.domain.column.entity.Columns;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ColumnsRepository extends JpaRepository<Columns, Long> {

    @EntityGraph(attributePaths = {"category", "user"})
    Page<Columns> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "user"})
    Optional<Columns> findById(Long id);

    @EntityGraph(attributePaths = {"category", "user"})
    Page<Columns> findByCategory_Name(String categoryName, Pageable pageable);

    long countByCategory_IdAndIsBestTrue(Long categoryId);
}
