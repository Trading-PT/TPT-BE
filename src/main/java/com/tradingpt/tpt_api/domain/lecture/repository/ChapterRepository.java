package com.tradingpt.tpt_api.domain.lecture.repository;

import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterListResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.Chapter;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    @Query("SELECT new com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterListResponseDTO(" +
            "c.id, c.title, c.chapterType) " +  // ← 타입 추가
            "FROM Chapter c " +
            "ORDER BY c.chapterOrder ASC")
    List<ChapterListResponseDTO> findAllSimple();
}
