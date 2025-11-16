package com.tradingpt.tpt_api.domain.lecture.service.query;

import com.tradingpt.tpt_api.domain.lecture.dto.LectureListResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterListResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureDetailResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.enums.LectureExposure;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureErrorStatus;
import com.tradingpt.tpt_api.domain.lecture.exception.LectureException;
import com.tradingpt.tpt_api.domain.lecture.repository.ChapterRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminLectureQueryServiceImpl implements AdminLectureQueryService {

    private final LectureRepository lectureRepository;
    private final ChapterRepository chapterRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<LectureListResponseDTO> getLectureList(Pageable pageable, String category) {
        LectureExposure exposure = parseCategory(category);

        Page<Lecture> page;
        if (exposure == null) {
            page = lectureRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            page = lectureRepository.findByLectureExposureOrderByCreatedAtDesc(exposure, pageable);
        }

        return page.map(LectureListResponseDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterListResponseDTO> getAllChapters() {
        return chapterRepository.findAllSimple();
    }

    @Override
    @Transactional(readOnly = true)
    public LectureDetailResponseDTO getLectureDetail(Long lectureId) {
        Lecture lecture = lectureRepository.findById(lectureId)
                .orElseThrow(() -> new LectureException(LectureErrorStatus.NOT_FOUND));
        return LectureDetailResponseDTO.from(lecture);
    }

    /**
     * "All" 이면 null 리턴해서 필터 안 걸고,
     * 그 외인데 enum에 없으면 예외 던진다.
     */
    private LectureExposure parseCategory(String category) {
        if (category == null || category.equalsIgnoreCase("All")) {
            return null;
        }
        try {
            return LectureExposure.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new LectureException(LectureErrorStatus.INVALID_CATEGORY);
        }
    }
}
