package com.tradingpt.tpt_api.domain.feedbackrequest.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.tradingpt.tpt_api.domain.feedbackrequest.exception.FeedbackRequestException;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil.FeedbackPeriod;
import com.tradingpt.tpt_api.domain.feedbackrequest.util.FeedbackPeriodUtil.WeekDateRange;

class FeedbackPeriodUtilTest {

    @Nested
    @DisplayName("resolveFrom 메서드 테스트")
    class ResolveFromTest {

        @Test
        @DisplayName("2025년 11월 13일은 3주차로 계산되어야 한다")
        void shouldReturn3rdWeekFor2025Nov13() {
            // given
            LocalDate date = LocalDate.of(2025, 11, 13);

            // when
            FeedbackPeriod period = FeedbackPeriodUtil.resolveFrom(date);

            // then
            assertThat(period.year()).isEqualTo(2025);
            assertThat(period.month()).isEqualTo(11);
            assertThat(period.week()).isEqualTo(3);
        }

        @Test
        @DisplayName("월의 첫날(1일)은 항상 1주차로 계산되어야 한다")
        void firstDayOfMonthShouldBeFirstWeek() {
            // given
            LocalDate jan1 = LocalDate.of(2025, 1, 1);  // 수요일
            LocalDate feb1 = LocalDate.of(2025, 2, 1);  // 토요일
            LocalDate mar1 = LocalDate.of(2025, 3, 1);  // 토요일
            LocalDate apr1 = LocalDate.of(2025, 4, 1);  // 화요일
            LocalDate may1 = LocalDate.of(2025, 5, 1);  // 목요일
            LocalDate jun1 = LocalDate.of(2025, 6, 1);  // 일요일

            // when & then
            assertThat(FeedbackPeriodUtil.resolveFrom(jan1).week()).isEqualTo(1);
            assertThat(FeedbackPeriodUtil.resolveFrom(feb1).week()).isEqualTo(1);
            assertThat(FeedbackPeriodUtil.resolveFrom(mar1).week()).isEqualTo(1);
            assertThat(FeedbackPeriodUtil.resolveFrom(apr1).week()).isEqualTo(1);
            assertThat(FeedbackPeriodUtil.resolveFrom(may1).week()).isEqualTo(1);
            assertThat(FeedbackPeriodUtil.resolveFrom(jun1).week()).isEqualTo(1);
        }

        @Test
        @DisplayName("2025년 11월 전체 주차 계산이 올바르게 되어야 한다")
        void november2025WeekCalculation() {
            // 2025년 11월
            // 1일(토), 2일(일) - 1주차
            // 3일(월)~9일(일) - 2주차
            // 10일(월)~16일(일) - 3주차
            // 17일(월)~23일(일) - 4주차
            // 24일(월)~30일(일) - 5주차

            // 1주차
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 1)).week()).isEqualTo(1);
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 2)).week()).isEqualTo(1);

            // 2주차
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 3)).week()).isEqualTo(2);
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 9)).week()).isEqualTo(2);

            // 3주차
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 10)).week()).isEqualTo(3);
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 13)).week()).isEqualTo(3);
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 16)).week()).isEqualTo(3);

            // 4주차
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 17)).week()).isEqualTo(4);
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 23)).week()).isEqualTo(4);

            // 5주차
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 24)).week()).isEqualTo(5);
            assertThat(FeedbackPeriodUtil.resolveFrom(LocalDate.of(2025, 11, 30)).week()).isEqualTo(5);
        }

        @Test
        @DisplayName("null 날짜는 예외를 발생시켜야 한다")
        void nullDateShouldThrowException() {
            // when & then
            assertThatThrownBy(() -> FeedbackPeriodUtil.resolveFrom(null))
                    .isInstanceOf(FeedbackRequestException.class);
        }

        @Test
        @DisplayName("월 경계 테스트 - 월말과 월초")
        void monthBoundaryTest() {
            // 2025년 10월 31일 (금요일) - 10월 5주차
            LocalDate oct31 = LocalDate.of(2025, 10, 31);
            FeedbackPeriod octPeriod = FeedbackPeriodUtil.resolveFrom(oct31);
            assertThat(octPeriod.month()).isEqualTo(10);
            assertThat(octPeriod.week()).isEqualTo(5);

            // 2025년 11월 1일 (토요일) - 11월 1주차
            LocalDate nov1 = LocalDate.of(2025, 11, 1);
            FeedbackPeriod novPeriod = FeedbackPeriodUtil.resolveFrom(nov1);
            assertThat(novPeriod.month()).isEqualTo(11);
            assertThat(novPeriod.week()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("getWeeksInMonth 메서드 테스트")
    class GetWeeksInMonthTest {

        @Test
        @DisplayName("2025년 11월은 5주로 계산되어야 한다")
        void november2025ShouldHave5Weeks() {
            // when
            int weeks = FeedbackPeriodUtil.getWeeksInMonth(2025, 11);

            // then
            assertThat(weeks).isEqualTo(5);
        }

        @Test
        @DisplayName("2025년 2월은 5주로 계산되어야 한다")
        void february2025ShouldHave5Weeks() {
            // 2025년 2월: 1일(토) ~ 28일(금)
            // 1-2일: 1주차, 3-9일: 2주차, 10-16일: 3주차, 17-23일: 4주차, 24-28일: 5주차

            // when
            int weeks = FeedbackPeriodUtil.getWeeksInMonth(2025, 2);

            // then
            assertThat(weeks).isEqualTo(5);
        }

        @Test
        @DisplayName("다양한 월의 주차 수 계산")
        void variousMonthsWeekCount() {
            // 2025년 각 월의 주차 수 확인
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 1)).isEqualTo(5);   // 31일, 수요일 시작
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 2)).isEqualTo(5);   // 28일, 토요일 시작
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 3)).isEqualTo(6);   // 31일, 토요일 시작 (31일 월요일까지 6주차)
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 4)).isEqualTo(5);   // 30일, 화요일 시작
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 5)).isEqualTo(5);   // 31일, 목요일 시작
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 6)).isEqualTo(6);   // 30일, 일요일 시작 (30일 월요일까지 6주차)
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 7)).isEqualTo(5);   // 31일, 화요일 시작
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 8)).isEqualTo(5);   // 31일, 금요일 시작
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 9)).isEqualTo(5);   // 30일, 월요일 시작
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 10)).isEqualTo(5);  // 31일, 수요일 시작
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 11)).isEqualTo(5);  // 30일, 토요일 시작
            assertThat(FeedbackPeriodUtil.getWeeksInMonth(2025, 12)).isEqualTo(5);  // 31일, 월요일 시작
        }
    }

    @Nested
    @DisplayName("getWeekDateRange 메서드 테스트")
    class GetWeekDateRangeTest {

        @Test
        @DisplayName("2025년 11월 3주차 날짜 범위는 10일(월)~16일(일)이어야 한다")
        void november2025Week3Range() {
            // when
            WeekDateRange range = FeedbackPeriodUtil.getWeekDateRange(2025, 11, 3);

            // then
            assertThat(range.startDate()).isEqualTo(LocalDate.of(2025, 11, 10));
            assertThat(range.endDate()).isEqualTo(LocalDate.of(2025, 11, 16));
        }

        @Test
        @DisplayName("2025년 11월 각 주차의 날짜 범위")
        void november2025AllWeekRanges() {
            // 1주차: 1일(토)~2일(일) - 월 초 특별 케이스
            WeekDateRange week1 = FeedbackPeriodUtil.getWeekDateRange(2025, 11, 1);
            assertThat(week1.startDate()).isEqualTo(LocalDate.of(2025, 11, 1));
            assertThat(week1.endDate()).isEqualTo(LocalDate.of(2025, 11, 2));

            // 2주차: 3일(월)~9일(일)
            WeekDateRange week2 = FeedbackPeriodUtil.getWeekDateRange(2025, 11, 2);
            assertThat(week2.startDate()).isEqualTo(LocalDate.of(2025, 11, 3));
            assertThat(week2.endDate()).isEqualTo(LocalDate.of(2025, 11, 9));

            // 3주차: 10일(월)~16일(일)
            WeekDateRange week3 = FeedbackPeriodUtil.getWeekDateRange(2025, 11, 3);
            assertThat(week3.startDate()).isEqualTo(LocalDate.of(2025, 11, 10));
            assertThat(week3.endDate()).isEqualTo(LocalDate.of(2025, 11, 16));

            // 4주차: 17일(월)~23일(일)
            WeekDateRange week4 = FeedbackPeriodUtil.getWeekDateRange(2025, 11, 4);
            assertThat(week4.startDate()).isEqualTo(LocalDate.of(2025, 11, 17));
            assertThat(week4.endDate()).isEqualTo(LocalDate.of(2025, 11, 23));

            // 5주차: 24일(월)~30일(일) - 월말 특별 케이스
            WeekDateRange week5 = FeedbackPeriodUtil.getWeekDateRange(2025, 11, 5);
            assertThat(week5.startDate()).isEqualTo(LocalDate.of(2025, 11, 24));
            assertThat(week5.endDate()).isEqualTo(LocalDate.of(2025, 11, 30));  // 11월은 30일까지
        }

        @Test
        @DisplayName("월요일로 시작하는 월의 1주차")
        void firstWeekStartingMonday() {
            // 2025년 9월 1일은 월요일
            WeekDateRange week1 = FeedbackPeriodUtil.getWeekDateRange(2025, 9, 1);
            assertThat(week1.startDate()).isEqualTo(LocalDate.of(2025, 9, 1));
            assertThat(week1.endDate()).isEqualTo(LocalDate.of(2025, 9, 7));
        }

        @Test
        @DisplayName("월 말이 주 중간에 끝나는 경우")
        void monthEndingMidWeek() {
            // 2025년 11월 30일(일)로 끝남
            WeekDateRange lastWeek = FeedbackPeriodUtil.getWeekDateRange(2025, 11, 5);
            assertThat(lastWeek.endDate()).isEqualTo(LocalDate.of(2025, 11, 30));
            // 12월 1일이 아니라 11월 30일에서 끝나야 함
            assertThat(lastWeek.endDate().getMonthValue()).isEqualTo(11);
        }
    }

    @Nested
    @DisplayName("통합 테스트")
    class IntegrationTest {

        @Test
        @DisplayName("resolveFrom과 getWeekDateRange의 일관성 검증")
        void consistencyBetweenResolveFromAndGetWeekDateRange() {
            // 2025년 11월 13일
            LocalDate testDate = LocalDate.of(2025, 11, 13);

            // resolveFrom으로 주차 계산
            FeedbackPeriod period = FeedbackPeriodUtil.resolveFrom(testDate);
            assertThat(period.week()).isEqualTo(3);

            // 해당 주차의 날짜 범위 확인
            WeekDateRange range = FeedbackPeriodUtil.getWeekDateRange(2025, 11, 3);

            // 13일이 3주차 범위에 포함되는지 확인
            assertThat(testDate).isBetween(range.startDate(), range.endDate());
            assertThat(range.startDate()).isEqualTo(LocalDate.of(2025, 11, 10));
            assertThat(range.endDate()).isEqualTo(LocalDate.of(2025, 11, 16));
        }

        @Test
        @DisplayName("모든 날짜가 정확히 한 주차에만 속하는지 검증")
        void everyDateBelongsToExactlyOneWeek() {
            // 2025년 11월의 모든 날짜 검증
            for (int day = 1; day <= 30; day++) {
                LocalDate date = LocalDate.of(2025, 11, day);
                FeedbackPeriod period = FeedbackPeriodUtil.resolveFrom(date);

                // 주차 번호는 1~5 사이여야 함
                assertThat(period.week()).isBetween(1, 5);

                // 해당 주차의 범위에 포함되어야 함
                WeekDateRange range = FeedbackPeriodUtil.getWeekDateRange(2025, 11, period.week());
                assertThat(date).isBetween(range.startDate(), range.endDate());
            }
        }
    }
}