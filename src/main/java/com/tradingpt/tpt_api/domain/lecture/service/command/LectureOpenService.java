package com.tradingpt.tpt_api.domain.lecture.service.command;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureProgressRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.subscription.repository.SubscriptionRepository;
import com.tradingpt.tpt_api.domain.user.enums.CourseStatus;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureOpenService {

    private final SubscriptionRepository subscriptionRepository;
    private final LectureRepository lectureRepository;
    private final LectureProgressRepository lectureProgressRepository;

    /**
     * 스케줄러(예: 0시)에 돌면서 ACTIVE 구독자들의 주차를 채워주는 메서드
     */
    @Transactional
    public void openWeeklyForActiveSubscriptions() {
        List<Subscription> actives = subscriptionRepository
                .findAllByStatus(Status.ACTIVE);

        for (Subscription sub : actives) {
            openWeeklyForSubscription(sub);
        }
    }

    private void openWeeklyForSubscription(Subscription sub) {

        Long customerId = sub.getCustomer().getId();

        // 1) 전체 강의 목록
        List<Lecture> ordered = lectureRepository.findAllOrderByChapterAndLectureOrder();
        int totalLectureCount = ordered.size();

        // 2) 지금까지 열린 총 강의 개수
        int openedCountNow = lectureProgressRepository.countByCustomerId(customerId);

        // 3) 이미 모든 강의가 열렸으면 종료 (완강)
        if (openedCountNow >= totalLectureCount) {
            sub.getCustomer().setCourseStatus(CourseStatus.AFTER_COMPLETION);
            return;
        }

        // 4) 이번 구독에서 열려야 할 주차 계산
        LocalDate start = sub.getCurrentPeriodStart();
        long days = ChronoUnit.DAYS.between(start, LocalDate.now());
        int targetInThisPeriod = (int) (days / 7) + 1;

        // 5) 이전 구독 snapshot
        int baseOpenedCount = sub.getBaseOpenedLectureCount();

        // 6) 이번 구독에서 실제로 열린 개수
        int openedInThisPeriod = openedCountNow - baseOpenedCount;

        if (targetInThisPeriod <= openedInThisPeriod) {
            return;
        }

        // 7) 최종 목표: 이전 + 이번 구독 누적
        int totalTargetOpenCount = baseOpenedCount + targetInThisPeriod;
        totalTargetOpenCount = Math.min(totalTargetOpenCount, totalLectureCount);

        // 8) 열어야 하는 강의 오픈
        for (int i = openedCountNow; i < totalTargetOpenCount; i++) {

            Lecture lecture = ordered.get(i);

            boolean exists = lectureProgressRepository
                    .existsByLectureIdAndCustomerId(lecture.getId(), customerId);

            if (exists) continue;

            lectureProgressRepository.save(
                    LectureProgress.builder()
                            .lecture(lecture)
                            .customer(sub.getCustomer())
                            .watchedSeconds(0)
                            .isCompleted(false)
                            .build()
            );
        }
    }
}

