package com.tradingpt.tpt_api.domain.lecture.service.command;

import com.tradingpt.tpt_api.domain.lecture.entity.Lecture;
import com.tradingpt.tpt_api.domain.lecture.entity.LectureProgress;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureProgressRepository;
import com.tradingpt.tpt_api.domain.lecture.repository.LectureRepository;
import com.tradingpt.tpt_api.domain.subscription.entity.Subscription;
import com.tradingpt.tpt_api.domain.subscription.enums.Status;
import com.tradingpt.tpt_api.domain.subscription.repository.SubscriptionRepository;
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

        // 구독 시작일부터 몇 주 지났는지
        LocalDate start = sub.getCurrentPeriodStart();
        long weeksPassed = ChronoUnit.WEEKS.between(start, LocalDate.now());
        int targetOpenCount = (int) weeksPassed + 1;

        int openedCount = lectureProgressRepository.countByCustomerId(customerId);
        if (targetOpenCount <= openedCount) {
            return;
        }

        List<Lecture> ordered = lectureRepository.findAllOrderByChapterAndLectureOrder();
        targetOpenCount = Math.min(targetOpenCount, ordered.size());

        for (int i = openedCount; i < targetOpenCount; i++) {
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
