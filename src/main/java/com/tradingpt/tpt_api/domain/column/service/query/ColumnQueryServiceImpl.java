package com.tradingpt.tpt_api.domain.column.service.query;

import com.tradingpt.tpt_api.domain.column.dto.response.ColumnCategoryResponseDTO;
import com.tradingpt.tpt_api.domain.column.dto.response.ColumnDetailResponseDTO;
import com.tradingpt.tpt_api.domain.column.dto.response.ColumnListResponseDTO;
import com.tradingpt.tpt_api.domain.column.entity.Columns;
import com.tradingpt.tpt_api.domain.column.entity.Comment;
import com.tradingpt.tpt_api.domain.column.exception.ColumnErrorStatus;
import com.tradingpt.tpt_api.domain.column.exception.ColumnException;
import com.tradingpt.tpt_api.domain.column.repository.ColumnCategoryRepository;
import com.tradingpt.tpt_api.domain.column.repository.ColumnCommentRepository;
import com.tradingpt.tpt_api.domain.column.repository.ColumnsRepository;
import com.tradingpt.tpt_api.domain.user.entity.Customer;
import com.tradingpt.tpt_api.domain.user.enums.UserStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserErrorStatus;
import com.tradingpt.tpt_api.domain.user.exception.UserException;
import com.tradingpt.tpt_api.domain.user.repository.CustomerRepository;
import com.tradingpt.tpt_api.global.exception.AuthException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ColumnQueryServiceImpl implements  ColumnQueryService{

    private final ColumnCategoryRepository categoryRepository;
    private final ColumnsRepository columnsRepository;
    private final ColumnCommentRepository commentRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ColumnCategoryResponseDTO> getCategoryList() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(ColumnCategoryResponseDTO::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ColumnListResponseDTO> getColumnList(String categoryName, Pageable pageable) {
        Page<Columns> page;

        if (categoryName == null || categoryName.equalsIgnoreCase("all")) {
            page = columnsRepository.findAll(pageable);
        } else {
            page = columnsRepository.findByCategory_Name(categoryName, pageable);
        }

        var ids = page.stream().map(Columns::getId).toList();
        Map<Long, Long> counts = ids.isEmpty()
                ? Collections.emptyMap()
                : commentRepository.countByColumnIds(ids).stream()
                        .collect(Collectors.toMap(
                                ColumnCommentRepository.ColumnCommentCount::getColumnId,
                                ColumnCommentRepository.ColumnCommentCount::getCnt));

        return page.map(c -> ColumnListResponseDTO.from(c, counts.getOrDefault(c.getId(), 0L)));
    }

    @Override
    @Transactional(readOnly = true)
    public ColumnDetailResponseDTO getColumnDetail(Long userId,Long columnId) {

        Columns c = columnsRepository.findById(columnId)
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.NOT_FOUND));

        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new AuthException(UserErrorStatus.CUSTOMER_NOT_FOUND));

        if ("10억인사이트".equals(c.getCategory().getName())) { //10억 인사이트인 경우 레귤러 이상 회원만 조회 가능
            UserStatus status = customer.getUserStatus();

            if (status == UserStatus.UID_REJECTED || status == UserStatus.UID_REVIEW_PENDING) {
                throw new UserException(UserErrorStatus.ACCESS_DENIED);
            }
        }

        List<Comment> comments = commentRepository.findByColumnIdWithUser(columnId);

        return ColumnDetailResponseDTO.from(c, comments);
    }
}
