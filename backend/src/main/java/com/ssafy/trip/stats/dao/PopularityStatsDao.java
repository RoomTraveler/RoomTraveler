package com.ssafy.trip.stats.dao;

import com.ssafy.trip.stats.model.PopularityStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface PopularityStatsDao {

    /**
     * 단일 통계 데이터를 조회하거나 생성/업데이트 (INSERT ... ON DUPLICATE KEY UPDATE)
     * @param stats PopularityStats 객체
     * @return 영향 받은 행 수
     */
    int upsertStats(PopularityStats stats) throws SQLException;

    /**
     * 특정 조건으로 통계 데이터 목록 조회 (랭킹용)
     * @param params 조회 조건 (statsType, regionType, regionCode, accommodationType, periodType, periodValue 등)
     * @return PopularityStats 목록
     */
    List<PopularityStats> findStatsByCriteria(Map<String, Object> params) throws SQLException;

    /**
     * 특정 숙소의 특정 기간 통계 조회
     * @param accommodationId 숙소 ID
     * @param periodType DAILY, WEEKLY, MONTHLY
     * @param periodValue 기준일
     * @return PopularityStats 객체 또는 null
     */
    PopularityStats findAccommodationStats(@Param("accommodationId") Long accommodationId,
                                           @Param("periodType") String periodType,
                                           @Param("periodValue") String periodValue) throws SQLException;
    
    /**
     * 특정 숙소의 조회수를 증가시킵니다.
     * @param accommodationId 숙소 ID
     * @param periodValue 기준일 (보통 LocalDate.now())
     * @param increment 증가량 (보통 1)
     * @return 영향 받은 행 수
     */
    int incrementAccommodationViewCount(@Param("accommodationId") Long accommodationId, 
                                        @Param("periodValue") LocalDate periodValue, 
                                        @Param("increment") int increment) throws SQLException;
    
    /**
     * 특정 숙소의 예약수를 증가시킵니다.
     * @param accommodationId 숙소 ID
     * @param periodValue 기준일
     * @param increment 증가량
     * @return 영향 받은 행 수
     */
    int incrementAccommodationReservationCount(@Param("accommodationId") Long accommodationId, 
                                             @Param("periodValue") LocalDate periodValue, 
                                             @Param("increment") int increment) throws SQLException;

    /**
     * 특정 숙소의 리뷰수를 증가시킵니다.
     * @param accommodationId 숙소 ID
     * @param periodValue 기준일
     * @param increment 증가량
     * @return 영향 받은 행 수
     */
    int incrementAccommodationReviewCount(@Param("accommodationId") Long accommodationId, 
                                          @Param("periodValue") LocalDate periodValue, 
                                          @Param("increment") int increment) throws SQLException;
    
    /**
     * 특정 기간 동안의 일일 통계를 그룹별로 합산합니다.
     * stats_type에 따라 필요한 groupByFields를 동적으로 구성하여 전달해야 합니다.
     * 예: ACCOMMODATION -> group by accommodation_id
     *     REGION -> group by region_type, region_code
     *     TYPE -> group by accommodation_type
     *     TYPE_REGION -> group by accommodation_type, region_type, region_code
     * @param startDate 시작일
     * @param endDate 종료일
     * @param statsType 집계할 stats_type
     * @return 합산된 PopularityStats 목록 (id, periodType, periodValue, topNRank, createdAt, updatedAt 등은 설정되지 않음)
     */
    List<PopularityStats> sumDailyStatsForPeriod(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("statsType") String statsType);

    /**
     * 여러 통계 데이터를 일괄적으로 upsert 합니다.
     * @param statsList PopularityStats 목록
     * @return 영향 받은 총 행 수 (DB 드라이버에 따라 다를 수 있음)
     */
    int upsertBulkStats(@Param("statsList") List<PopularityStats> statsList) throws SQLException;

    /**
     * 특정 조건의 통계 데이터에 랭킹을 업데이트합니다.
     * @param statsList 랭킹이 매겨진 PopularityStats 목록 (id와 topNRank 필드가 채워져 있어야 함)
     * @return 업데이트된 총 행 수
     */
    int updateBatchRank(@Param("statsList") List<PopularityStats> statsList) throws SQLException;

    /**
     * 특정 조건에 맞는 통계 데이터를 인기도 점수 기준으로 상위 N개 조회합니다.
     * @param statsType 통계 유형
     * @param periodType 기간 유형
     * @param periodValue 기간 값
     * @param limit N 값
     * @return PopularityStats 목록
     */
    List<PopularityStats> findTopNByScore(@Param("statsType") String statsType,
                                           @Param("periodType") String periodType,
                                           @Param("periodValue") String periodValue,
                                           @Param("limit") int limit);

    // TODO: 배치 집계를 위한 쿼리 추가 (예: 어제자 예약/리뷰 데이터 기반으로 일일 통계 생성)
    // TODO: 랭킹 업데이트를 위한 쿼리 추가
} 