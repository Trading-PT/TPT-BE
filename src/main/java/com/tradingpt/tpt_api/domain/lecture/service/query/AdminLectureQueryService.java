package com.tradingpt.tpt_api.domain.lecture.service.query;

import com.tradingpt.tpt_api.domain.lecture.dto.LectureListResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminLectureQueryService {
    Page<LectureListResponseDTO> getLectureList(Pageable pageable, String category);
    LectureDetailResponseDTO getLectureDetail(Long lectureId);
}
