package com.tradingpt.tpt_api.domain.lecture.service.query;

import com.tradingpt.tpt_api.domain.lecture.dto.LectureListResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterListResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.CustomerHomeworkSummaryResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminLectureQueryService {
    Page<LectureListResponseDTO> getLectureList(Pageable pageable, String category);
    LectureDetailResponseDTO getLectureDetail(Long lectureId);
    List<ChapterListResponseDTO> getAllChapters();
    CustomerHomeworkSummaryResponseDTO getCustomerHomeworkSummary(Long customerId);
}
