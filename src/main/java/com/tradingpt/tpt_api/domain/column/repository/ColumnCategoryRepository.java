package com.tradingpt.tpt_api.domain.column.repository;

import com.tradingpt.tpt_api.domain.column.entity.ColumnCategory;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColumnCategoryRepository extends JpaRepository<ColumnCategory, Long> {

    Optional<ColumnCategory> findByName(String name);
}

