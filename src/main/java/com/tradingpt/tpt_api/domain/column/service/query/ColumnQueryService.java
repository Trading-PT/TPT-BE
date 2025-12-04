package com.tradingpt.tpt_api.domain.column.service.query;

import com.tradingpt.tpt_api.domain.column.dto.response.ColumnCategoryResponseDTO;
import com.tradingpt.tpt_api.domain.column.dto.response.ColumnDetailResponseDTO;
import com.tradingpt.tpt_api.domain.column.dto.response.ColumnListResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ColumnQueryService {

    List<ColumnCategoryResponseDTO> getCategoryList();

    Page<ColumnListResponseDTO> getColumnList(String category,  Pageable pageable);

    ColumnDetailResponseDTO getColumnDetail(Long userId,Long columnId);
}
