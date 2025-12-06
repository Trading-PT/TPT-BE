package com.tradingpt.tpt_api.domain.lecture.scheduler;

import com.tradingpt.tpt_api.domain.lecture.service.command.LectureOpenService;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LectureOpenScheduler {

    private final LectureOpenService lectureOpenService;

    // 매일 00:01에 실행 (정기 결제 스케줄러와 시간 분산)
    @Scheduled(cron = "0 1 0 * * *")
    @SchedulerLock(
            name = "weeklyLectureOpenJob",
            lockAtLeastFor = "PT5S",
            lockAtMostFor = "PT30S"
    )
    public void openWeeklyLectures() {
        lectureOpenService.openWeeklyForActiveSubscriptions();
    }
}
