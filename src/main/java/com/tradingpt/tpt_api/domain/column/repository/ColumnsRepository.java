package com.tradingpt.tpt_api.domain.column.repository;

import com.tradingpt.tpt_api.domain.column.entity.Columns;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColumnsRepository extends JpaRepository<Columns, Long> {
}
