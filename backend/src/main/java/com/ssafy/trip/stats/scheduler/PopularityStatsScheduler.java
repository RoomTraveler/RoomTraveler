package com.ssafy.trip.stats.scheduler;

import com.ssafy.trip.stats.service.PopularityStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopularityStatsScheduler {

    private final PopularityStatsService popularityStatsService;
    private static final int TOP_N_RANKING = 10; // 상위 랭킹 N개
    private static final DateTimeFormatter WEEKLY_FORMATTER = DateTimeFormatter.ofPattern("YYYY-'W'ww");
    private static final DateTimeFormatter MONTHLY_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM");

    @Scheduled(cron = "${stats.schedule.weekly:0 0 1 ? * MON}") // 매주 월요일 새벽 1시에 실행 (지난주 데이터 집계)
    public void aggregateWeeklyStats() {
        LocalDate today = LocalDate.now();
        LocalDate lastWeekStartDate = today.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate lastWeekEndDate = lastWeekStartDate.plusDays(6);
        String weeklyPeriodValue = lastWeekStartDate.format(WEEKLY_FORMATTER);

        log.info("주간 통계 집계 시작: {} ({} ~ {})", weeklyPeriodValue, lastWeekStartDate, lastWeekEndDate);
        try {
            popularityStatsService.aggregateAndRankPeriodicStats("WEEKLY", weeklyPeriodValue, lastWeekStartDate, lastWeekEndDate, TOP_N_RANKING);
            log.info("주간 통계 집계 및 랭킹 업데이트 완료: {}", weeklyPeriodValue);
        } catch (Exception e) {
            log.error("주간 통계 집계 중 오류 발생: {}", weeklyPeriodValue, e);
        }
    }

    @Scheduled(cron = "${stats.schedule.monthly:0 0 2 1 * ?}") // 매월 1일 새벽 2시에 실행 (지난달 데이터 집계)
    public void aggregateMonthlyStats() {
        LocalDate today = LocalDate.now();
        LocalDate lastMonthStartDate = today.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEndDate = lastMonthStartDate.withDayOfMonth(lastMonthStartDate.lengthOfMonth());
        String monthlyPeriodValue = lastMonthStartDate.format(MONTHLY_FORMATTER);

        log.info("월간 통계 집계 시작: {} ({} ~ {})", monthlyPeriodValue, lastMonthStartDate, lastMonthEndDate);
        try {
            popularityStatsService.aggregateAndRankPeriodicStats("MONTHLY", monthlyPeriodValue, lastMonthStartDate, lastMonthEndDate, TOP_N_RANKING);
            log.info("월간 통계 집계 및 랭킹 업데이트 완료: {}", monthlyPeriodValue);
        } catch (Exception e) {
            log.error("월간 통계 집계 중 오류 발생: {}", monthlyPeriodValue, e);
        }
    }

    @Scheduled(cron = "${stats.schedule.daily-ranking:0 30 0 * * ?}") // 매일 0시 30분에 실행 (어제자 데이터 기준 랭킹)
    public void updateDailyStatsRanking() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("일일 통계 랭킹 업데이트 시작 (기준일: {})", yesterday);
        try {
            popularityStatsService.rankDailyStats(yesterday, TOP_N_RANKING);
            log.info("일일 통계 랭킹 업데이트 완료 (기준일: {})", yesterday);
        } catch (Exception e) {
            log.error("일일 통계 랭킹 업데이트 중 오류 발생 (기준일: {}): {}", yesterday, e.getMessage(), e);
        }
    }

    // TODO: (옵션) 애플리케이션 시작 시 과거 데이터 집계 로직 (필요한 경우)
    /*
    @PostConstruct
    public void initPastStats() {
        // 특정 기간 동안의 과거 데이터를 집계하고 싶을 때 사용
        // 예: LocalDate startDate = LocalDate.of(2023, 1, 1);
        //      LocalDate endDate = LocalDate.now().minusDays(1);
        //      ... aggregateAndRankPeriodicStats 호출 로직 ...
    }
    */
} 