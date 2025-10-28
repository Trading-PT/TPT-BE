package com.tradingpt.tpt_api.domain.column.service.command;

import com.tradingpt.tpt_api.domain.column.dto.request.ColumnCategoryRequestDTO;
import com.tradingpt.tpt_api.domain.column.dto.request.ColumnCreateRequestDTO;
import com.tradingpt.tpt_api.domain.column.dto.request.ColumnUpdateRequestDTO;
import com.tradingpt.tpt_api.domain.column.entity.ColumnCategory;
import com.tradingpt.tpt_api.domain.column.entity.Columns;
import com.tradingpt.tpt_api.domain.column.exception.ColumnErrorStatus;
import com.tradingpt.tpt_api.domain.column.exception.ColumnException;
import com.tradingpt.tpt_api.domain.column.repository.ColumnCategoryRepository;
import com.tradingpt.tpt_api.domain.column.repository.ColumnsRepository;
import com.tradingpt.tpt_api.domain.user.entity.User;
import com.tradingpt.tpt_api.domain.user.repository.AdminRepository;
import com.tradingpt.tpt_api.domain.user.repository.TrainerRepository;
import com.tradingpt.tpt_api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminColumnCommandServiceImpl implements AdminColumnCommandService {

    private final ColumnsRepository columnsRepository;
    private final ColumnCategoryRepository columnCategoryRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final TrainerRepository trainerRepository;

    @Override
    @Transactional
    public Long createColumn(Long writerUserId, ColumnCreateRequestDTO request, String role) {
        User writer;

        if ("ROLE_ADMIN".equals(role)) {
            writer = adminRepository.findById(writerUserId)
                    .orElseThrow(() -> new ColumnException(ColumnErrorStatus.WRITER_NOT_FOUND));
        } else if ("ROLE_TRAINER".equals(role)) {
            writer = trainerRepository.findById(writerUserId)
                    .orElseThrow(() -> new ColumnException(ColumnErrorStatus.WRITER_NOT_FOUND));
        } else {
            throw new ColumnException(ColumnErrorStatus.INVALID_ROLE);
        }

        ColumnCategory category = columnCategoryRepository.findByName(request.getCategory())
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.CATEGORY_NOT_FOUND));

        Columns column = Columns.builder()
                .user(writer)
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .content(request.getContent())
                .category(category)
                .likeCount(0)
                .build();

        Columns saved = columnsRepository.save(column);
        return saved.getId();
    }

    @Override
    @Transactional
    public Long updateColumn(Long columnId, Long editorUserId, ColumnUpdateRequestDTO request, String role) {
        Columns column = columnsRepository.findById(columnId)
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.NOT_FOUND));

        // 작성자 본인만 수정 가능
        if (!column.getUser().getId().equals(editorUserId)) {
            throw new ColumnException(ColumnErrorStatus.UNAUTHORIZED);
        }

        User newWriter = null;
        if (request.getWriterName() != null && !request.getWriterName().equals(column.getUser().getName())) {
            newWriter = userRepository.findByName(request.getWriterName())
                    .orElseThrow(() -> new ColumnException(ColumnErrorStatus.WRITER_NOT_FOUND));
        }

        ColumnCategory newCategory = column.getCategory(); // 기본적으로 기존 유지
        if (request.getCategory() != null
                && !request.getCategory().equals(column.getCategory().getName())) {
            newCategory = columnCategoryRepository.findByName(request.getCategory())
                    .orElseThrow(() -> new ColumnException(ColumnErrorStatus.CATEGORY_NOT_FOUND));
        }

        column.update(request.getTitle(),request.getSubtitle(), request.getContent(), newCategory, newWriter);
        return column.getId();
    }

    @Override
    @Transactional
    public Long deleteColumn(Long columnId) {
        Columns column = columnsRepository.findById(columnId)
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.NOT_FOUND));

        columnsRepository.delete(column);
        return column.getId();
    }

    @Override
    @Transactional
    public Long createCategory(ColumnCategoryRequestDTO request) {
        ColumnCategory category = ColumnCategory.builder()
                .name(request.getName())
                .color(request.getColor())
                .build();

        ColumnCategory saved = columnCategoryRepository.save(category);
        return saved.getId();
    }

    @Override
    @Transactional
    public Long updateCategory(Long categoryId, ColumnCategoryRequestDTO request) {
        ColumnCategory category = columnCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.CATEGORY_NOT_FOUND));

        // 일괄 변경
        category.update(request.getName(), request.getColor());
        return category.getId();
    }

    @Override
    public Long deleteCategory(Long categoryId) {
        ColumnCategory category = columnCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new ColumnException(ColumnErrorStatus.CATEGORY_NOT_FOUND));

        // cascade = ALL, orphanRemoval = true 덕분에 칼럼도 함께 삭제됨
        columnCategoryRepository.delete(category);
        return categoryId;
    }
}
