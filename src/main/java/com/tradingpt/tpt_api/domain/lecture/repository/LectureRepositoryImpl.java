package com.tradingpt.tpt_api.domain.lecture.repository;

import static com.tradingpt.tpt_api.domain.lecture.entity.QChapter.chapter;
import static com.tradingpt.tpt_api.domain.lecture.entity.QLecture.lecture;
import static com.tradingpt.tpt_api.domain.lecture.entity.QLectureProgress.lectureProgress;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tradingpt.tpt_api.domain.lecture.dto.response.ChapterBlockDTO;
import com.tradingpt.tpt_api.domain.lecture.dto.response.LectureResponseDTO;
import com.tradingpt.tpt_api.domain.lecture.entity.QLectureProgress;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class LectureRepositoryImpl implements LectureRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChapterBlockDTO> findCurriculum(Long userId, int page, int size) {

        long offset = (long) page * size;

        // 1) 먼저 페이징 기준이 될 챕터 id만 뽑기
        List<Long> chapterIds = queryFactory
                .select(chapter.id)
                .from(chapter)
                .orderBy(chapter.chapterOrder.asc())
                .offset(offset)
                .limit(size)
                .fetch();

        if (chapterIds.isEmpty()) {
            return List.of();
        }

        // 2) 최신 LectureProgress만 가져오도록 서브쿼리 생성
        QLectureProgress lpSub = new QLectureProgress("lpSub");

        // 3) 챕터 + 강의 + 최신 Progress 함께 조회
        List<Tuple> rows = queryFactory
                .select(
                        chapter.id,
                        chapter.title,
                        chapter.description,
                        chapter.chapterType,

                        lecture.id,
                        lecture.title,
                        lecture.content,
                        lecture.thumbnailUrl,
                        lecture.durationSeconds,
                        lecture.requiredTokens,

                        lectureProgress.watchedSeconds,
                        lectureProgress.isCompleted,
                        lectureProgress.lastWatchedAt,
                        lectureProgress.dueDate
                )
                .from(chapter)
                .join(lecture).on(lecture.chapter.eq(chapter))
                .leftJoin(lectureProgress)
                .on(
                        lectureProgress.lecture.eq(lecture)
                                .and(lectureProgress.customer.id.eq(userId))
                                .and(
                                        lectureProgress.dueDate.eq(
                                                JPAExpressions
                                                        .select(lpSub.dueDate.max())
                                                        .from(lpSub)
                                                        .where(
                                                                lpSub.lecture.eq(lecture)
                                                                        .and(lpSub.customer.id.eq(userId))
                                                        )
                                        )
                                )
                )
                .where(chapter.id.in(chapterIds))
                .orderBy(
                        chapter.chapterOrder.asc(),
                        lecture.lectureOrder.asc()
                )
                .fetch();

        // 4) ChapterId 기준으로 묶어서 ChapterBlockDTO 구성
        Map<Long, ChapterBlockDTO> chapterMap = new LinkedHashMap<>();

        int totalProLectures = 0;   // PRO 전체 강의 수
        int completedProCount = 0;  // 완강한 PRO 강의 수

        for (Tuple t : rows) {
            Long chapterId = t.get(chapter.id);

            // ChapterBlockDTO 생성 (없을 때만)
            chapterMap.putIfAbsent(
                    chapterId,
                    ChapterBlockDTO.builder()
                            .chapterId(chapterId)
                            .chapterTitle(t.get(chapter.title))
                            .description(t.get(chapter.description))
                            .chapterType(
                                    t.get(chapter.chapterType) != null
                                            ? t.get(chapter.chapterType).name()
                                            : null
                            )
                            .lectures(new ArrayList<>())
                            .build()
            );

            ChapterBlockDTO chapterDTO = chapterMap.get(chapterId);

            // 무료/유료 판별
            Integer requiredTokens = t.get(lecture.requiredTokens);
            boolean isPaid = (requiredTokens != null && requiredTokens == 0);

            // Progress 정보
            Integer watchedSeconds = t.get(lectureProgress.watchedSeconds);
            Boolean completed = t.get(lectureProgress.isCompleted);
            LocalDateTime lastWatchedAt = t.get(lectureProgress.lastWatchedAt);
            LocalDateTime dueDate = t.get(lectureProgress.dueDate);

            // PRO 강의 여부
            boolean isProLecture = (requiredTokens != null && requiredTokens == 0);
            if (isProLecture) {
                totalProLectures += 1;
                if (Boolean.TRUE.equals(completed)) {
                    completedProCount += 1;
                }
            }

            // LectureResponseDTO 구성
            LectureResponseDTO lectureDTO = LectureResponseDTO.builder()
                    .lectureId(t.get(lecture.id))
                    .chapterId(chapterId)
                    .title(t.get(lecture.title))
                    .content(t.get(lecture.content))
                    .thumbnailUrl(t.get(lecture.thumbnailUrl))
                    .durationSeconds(t.get(lecture.durationSeconds))
                    .requiredTokens(requiredTokens != null ? requiredTokens : 0)
                    .paid(isPaid)
                    .watchedSeconds(watchedSeconds)
                    .completed(completed)
                    .lastWatchedAt(lastWatchedAt)
                    .dueDate(dueDate)              // ⭐ 최신 LectureProgress의 dueDate
                    .build();

            chapterDTO.getLectures().add(lectureDTO);
        }

        // 5) 전체 PRO 기준 진도율 계산
        int finalProgressPercent =
                totalProLectures == 0 ? 0 :
                        (int) ((completedProCount * 100.0) / totalProLectures);

        // 모든 챕터에 동일하게 적용
        for (ChapterBlockDTO dto : chapterMap.values()) {
            dto.setProgressPercent(finalProgressPercent);
        }

        return new ArrayList<>(chapterMap.values());
    }
}
