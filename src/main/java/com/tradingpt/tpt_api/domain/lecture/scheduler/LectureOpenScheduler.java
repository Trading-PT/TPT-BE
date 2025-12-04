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

    // 매 시 정각(서버 시간 기준)마다 실행
    @Scheduled(cron = "0 0 * * * *")
    @SchedulerLock(
            name = "weeklyLectureOpenJob",
            lockAtLeastFor = "PT5S",
            lockAtMostFor = "PT30S"
    )
    public void openWeeklyLectures() {
        lectureOpenService.openWeeklyForActiveSubscriptions();
    }
}
