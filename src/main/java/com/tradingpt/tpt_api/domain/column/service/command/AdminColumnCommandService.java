package com.tradingpt.tpt_api.domain.column.service.command;

import com.tradingpt.tpt_api.domain.column.dto.request.ColumnCategoryRequestDTO;
import com.tradingpt.tpt_api.domain.column.dto.request.ColumnCreateRequestDTO;
import com.tradingpt.tpt_api.domain.column.dto.request.ColumnUpdateRequestDTO;

public interface AdminColumnCommandService {
    Long createColumn(Long writerUserId, ColumnCreateRequestDTO request,String role);
    Long updateColumn(Long columnId, Long editorUserId, ColumnUpdateRequestDTO request,String role);
    Long deleteColumn(Long columnId);

    Long createCategory(ColumnCategoryRequestDTO request);
    Long updateCategory(Long categoryId, ColumnCategoryRequestDTO request);
    Long deleteCategory(Long categoryId);
}
