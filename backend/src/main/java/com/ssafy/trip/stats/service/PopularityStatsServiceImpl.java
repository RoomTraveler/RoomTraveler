package com.ssafy.trip.stats.service;

import com.ssafy.trip.region.model.Sido;
import com.ssafy.trip.region.service.RegionService;
import com.ssafy.trip.stats.dao.PopularityStatsDao;
import com.ssafy.trip.stats.model.PopularityStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularityStatsServiceImpl implements PopularityStatsService {

    private final PopularityStatsDao popularityStatsDao;
    private final RegionService regionService;

    // 인기 점수 가중치 (설정 파일 또는 상수로 관리 가능)
    private static final int VIEW_WEIGHT = 1;
    private static final int REVIEW_WEIGHT = 3;
    private static final int RESERVATION_WEIGHT = 5;

    // 날짜 포매터 추가
    private static final DateTimeFormatter WEEKLY_FORMATTER = DateTimeFormatter.ofPattern("YYYY-'W'ww");
    private static final DateTimeFormatter MONTHLY_FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM");

    @Override
    @Transactional
    public void incrementDailyViewCount(Long accommodationId) throws SQLException {
        if (accommodationId == null) return;
        log.debug("Incrementing daily view count for accommodationId: {}", accommodationId);
        popularityStatsDao.incrementAccommodationViewCount(accommodationId, LocalDate.now(), 1);
    }

    @Override
    @Transactional
    public void incrementDailyReservationCount(Long accommodationId) throws SQLException {
        if (accommodationId == null) return;
        log.debug("Incrementing daily reservation count for accommodationId: {}", accommodationId);
        popularityStatsDao.incrementAccommodationReservationCount(accommodationId, LocalDate.now(), 1);
    }

    @Override
    @Transactional
    public void incrementDailyReviewCount(Long accommodationId) throws SQLException {
        if (accommodationId == null) return;
        log.debug("Incrementing daily review count for accommodationId: {}", accommodationId);
        popularityStatsDao.incrementAccommodationReviewCount(accommodationId, LocalDate.now(), 1);
    }

    @Override
    public List<PopularityStats> getPopularityStats(Map<String, Object> criteria) throws SQLException, Exception {
        log.debug("Fetching popularity stats with criteria: {}", criteria);
        List<PopularityStats> stats = popularityStatsDao.findStatsByCriteria(criteria);

        if ("REGION".equals(criteria.get("statsType")) && "SIDO".equals(criteria.get("regionType")) && (stats == null || stats.isEmpty())) {
            log.info("No popular region stats found, returning random sidos.");
            List<Sido> allSidos = regionService.getAllSidos();
            if (allSidos != null && !allSidos.isEmpty()) {
                List<PopularityStats> randomSidoStats = new ArrayList<>();
                Random random = new Random();
                List<Sido> shuffledSidos = new ArrayList<>(allSidos);
                Collections.shuffle(shuffledSidos, random);

                int count = 0;
                for (Sido sido : shuffledSidos) {
                    if (count >= 5) break;
                    PopularityStats pseudoStat = PopularityStats.builder()
                            .statsType("REGION")
                            .regionType("SIDO")
                            .regionCode(sido.getCode())
                            .sidoName(sido.getName())
                            .sidoImgUrl(sido.getSidoImgUrl())
                            .popularityScore(0.0)
                            .build();
                    randomSidoStats.add(pseudoStat);
                    count++;
                }
                return randomSidoStats;
            }
        }
        return stats;
    }

    @Override
    public PopularityStats getAccommodationStats(Long accommodationId, String periodType, String periodValue) throws SQLException {
        log.debug("Fetching accommodation stats for id: {}, period: {}, date: {}", accommodationId, periodType, periodValue);
        return popularityStatsDao.findAccommodationStats(accommodationId, periodType, periodValue);
    }

    @Override
    @Transactional
    public void aggregateAndRankPeriodicStats(String periodType, String periodValue, LocalDate startDate, LocalDate endDate, int topN) throws SQLException {
        if (!("WEEKLY".equals(periodType) || "MONTHLY".equals(periodType))) {
            throw new IllegalArgumentException("periodType은 WEEKLY 또는 MONTHLY만 가능합니다.");
        }

        String[] statsTypes = {"ACCOMMODATION", "REGION", "TYPE", "TYPE_REGION"};
        List<PopularityStats> allAggregatedStats = new ArrayList<>();

        for (String statsType : statsTypes) {
            List<PopularityStats> dailySum = popularityStatsDao.sumDailyStatsForPeriod(startDate, endDate, statsType);

            List<PopularityStats> aggregatedStatsForType = new ArrayList<>();
            for (PopularityStats sum : dailySum) {
                PopularityStats aggregatedStat = new PopularityStats();
                aggregatedStat.setStatsType(statsType);
                aggregatedStat.setPeriodType(periodType);
                aggregatedStat.setPeriodValue(periodValue);

                if (sum.getAccommodationId() != null) aggregatedStat.setAccommodationId(sum.getAccommodationId());
                if (sum.getRegionType() != null) aggregatedStat.setRegionType(sum.getRegionType());
                if (sum.getRegionCode() != null) aggregatedStat.setRegionCode(sum.getRegionCode());
                if (sum.getAccommodationType() != null) aggregatedStat.setAccommodationType(sum.getAccommodationType());

                aggregatedStat.setViewCount(sum.getViewCount());
                aggregatedStat.setReservationCount(sum.getReservationCount());
                aggregatedStat.setReviewCount(sum.getReviewCount());
                aggregatedStat.setPopularityScore(calculatePopularityScore(sum.getViewCount(), sum.getReservationCount(), sum.getReviewCount()));
                aggregatedStatsForType.add(aggregatedStat);
            }
            allAggregatedStats.addAll(aggregatedStatsForType);
        }

        if (!allAggregatedStats.isEmpty()) {
            popularityStatsDao.upsertBulkStats(allAggregatedStats); 
        }

        for (String statsType : statsTypes) {
            List<PopularityStats> rankedStats = popularityStatsDao.findTopNByScore(statsType, periodType, periodValue, topN);
            if (rankedStats != null && !rankedStats.isEmpty()) {
                for (int i = 0; i < rankedStats.size(); i++) {
                    PopularityStats stat = rankedStats.get(i);
                    stat.setTopNRank(i + 1);
                }
                popularityStatsDao.updateBatchRank(rankedStats);
            }
        }
    }

    @Override
    @Transactional
    public void rankDailyStats(LocalDate date, int topN) throws SQLException {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null일 수 없습니다.");
        }
        String periodValue = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        String[] statsTypes = {"ACCOMMODATION", "REGION", "TYPE", "TYPE_REGION"};

        log.info("일일 랭킹 업데이트 시작: 날짜 - {}, TOP_N - {}", periodValue, topN);

        for (String statsType : statsTypes) {
            List<PopularityStats> rankedStats = popularityStatsDao.findTopNByScore(statsType, "DAILY", periodValue, topN);
            if (rankedStats != null && !rankedStats.isEmpty()) {
                for (int i = 0; i < rankedStats.size(); i++) {
                    PopularityStats stat = rankedStats.get(i);
                    stat.setTopNRank(i + 1); 
                }
                popularityStatsDao.updateBatchRank(rankedStats);
                log.debug("일일 랭킹 업데이트 완료: 날짜 - {}, StatsType - {}, 업데이트된 항목 수 - {}", periodValue, statsType, rankedStats.size());
            } else {
                log.debug("일일 랭킹 업데이트 대상 없음: 날짜 - {}, StatsType - {}", periodValue, statsType);
            }
        }
        log.info("일일 랭킹 업데이트 완료: 날짜 - {}", periodValue);
    }
    
    private double calculatePopularityScore(int viewCount, int reservationCount, int reviewCount) {
        return (viewCount * 0.2) + (reservationCount * 0.5) + (reviewCount * 0.3);
    }

    // 수동 집계 메소드들 추가
    @Override
    @Transactional
    public String triggerManualAggregation(LocalDate startDate, LocalDate endDate, String periodType, int topN) throws SQLException {
        if (startDate == null || endDate == null || periodType == null) {
            throw new IllegalArgumentException("시작일, 종료일, 기간 유형은 null일 수 없습니다.");
        }
        if (!("WEEKLY".equalsIgnoreCase(periodType) || "MONTHLY".equalsIgnoreCase(periodType))) {
            throw new IllegalArgumentException("periodType은 WEEKLY 또는 MONTHLY만 가능합니다.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료일은 시작일보다 이전일 수 없습니다.");
        }

        String periodValue;
        if ("WEEKLY".equalsIgnoreCase(periodType)) {
            // 주간 집계 시 periodValue는 해당 주의 시작일(월요일) 기준으로 YYYY-Www 형식
            periodValue = startDate.with(java.time.DayOfWeek.MONDAY).format(WEEKLY_FORMATTER);
        } else { // MONTHLY
            // 월간 집계 시 periodValue는 해당 월의 1일 기준으로 YYYY-MM 형식
            periodValue = startDate.withDayOfMonth(1).format(MONTHLY_FORMATTER);
        }

        log.info("수동 통계 집계 시작: 기간 [{} ~ {}], 유형 [{}], PeriodValue [{}], TopN [{}]", startDate, endDate, periodType.toUpperCase(), periodValue, topN);
        aggregateAndRankPeriodicStats(periodType.toUpperCase(), periodValue, startDate, endDate, topN);
        log.info("수동 통계 집계 완료: PeriodValue [{}]", periodValue);
        return periodValue;
    }

    @Override
    @Transactional
    public void triggerManualDailyRanking(LocalDate date, int topN) throws SQLException {
        if (date == null) {
            throw new IllegalArgumentException("날짜는 null일 수 없습니다.");
        }
        log.info("수동 일일 랭킹 업데이트 시작: 날짜 [{}], TopN [{}]", date, topN);
        rankDailyStats(date, topN); // 기존 rankDailyStats 메소드 호출
        log.info("수동 일일 랭킹 업데이트 완료: 날짜 [{}]", date);
    }
} 