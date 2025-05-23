package com.ssafy.trip.stats.service;

import com.ssafy.trip.stats.model.PopularityStats;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PopularityStatsService {

    /**
     * 특정 숙소의 일일 조회수를 증가시킵니다.
     * @param accommodationId 숙소 ID
     */
    void incrementDailyViewCount(Long accommodationId) throws SQLException;

    /**
     * 특정 숙소의 일일 예약수를 증가시킵니다.
     * @param accommodationId 숙소 ID
     */
    void incrementDailyReservationCount(Long accommodationId) throws SQLException;

    /**
     * 특정 숙소의 일일 리뷰수를 증가시킵니다.
     * @param accommodationId 숙소 ID
     */
    void incrementDailyReviewCount(Long accommodationId) throws SQLException;

    /**
     * 주어진 조건에 맞는 통계/랭킹 데이터를 조회합니다.
     * @param criteria 조회 조건 (예: Map.of("statsType", "REGION", "periodType", "WEEKLY"))
     * @return PopularityStats 목록
     */
    List<PopularityStats> getPopularityStats(Map<String, Object> criteria) throws SQLException, Exception;

    /**
     * 특정 숙소의 특정 기간 통계를 조회합니다.
     * @param accommodationId 숙소 ID
     * @param periodType DAILY, WEEKLY, MONTHLY
     * @param periodValue 기준일
     * @return PopularityStats 객체 또는 null
     */
    PopularityStats getAccommodationStats(Long accommodationId, String periodType, String periodValue) throws SQLException;
    
    /**
     * 특정 기간(주간/월간)의 통계를 집계하고 랭킹을 매깁니다.
     * @param periodType 집계할 기간 유형 (WEEKLY, MONTHLY)
     * @param periodValue 기간 값 (예: 2023-W52, 2023-12)
     * @param startDate 해당 기간의 시작일
     * @param endDate 해당 기간의 종료일
     * @param topN 랭킹 상위 N개
     */
    void aggregateAndRankPeriodicStats(String periodType, String periodValue, LocalDate startDate, LocalDate endDate, int topN) throws SQLException;

    /**
     * 특정 날짜의 일일 통계에 대해 랭킹을 매깁니다.
     * @param date 랭킹을 매길 기준 날짜 (보통 어제 날짜)
     * @param topN 랭킹 상위 N개
     */
    void rankDailyStats(LocalDate date, int topN) throws SQLException;

    /**
     * 특정 기간의 주간 또는 월간 통계를 수동으로 집계하고 랭킹을 매깁니다.
     * @param startDate 집계 시작일
     * @param endDate 집계 종료일
     * @param periodType 집계할 기간 유형 (WEEKLY, MONTHLY)
     * @param topN 랭킹 상위 N개
     * @return 생성된 periodValue (예: "2023-W52", "2023-12")
     */
    String triggerManualAggregation(LocalDate startDate, LocalDate endDate, String periodType, int topN) throws SQLException;

    /**
     * 특정 날짜의 일일 통계에 대해 랭킹을 수동으로 매깁니다.
     * @param date 랭킹을 매길 기준 날짜
     * @param topN 랭킹 상위 N개
     */
    void triggerManualDailyRanking(LocalDate date, int topN) throws SQLException;

    // TODO: 스케줄러에서 사용할 주간/월간 집계 메소드
    // TODO: 랭킹 업데이트 로직 (필요시)
} 