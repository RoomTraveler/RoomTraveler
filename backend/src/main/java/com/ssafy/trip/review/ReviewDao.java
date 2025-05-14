package com.ssafy.trip.review;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 데이터 접근 객체 인터페이스
 */
@Mapper
public interface ReviewDao {
    /**
     * 새 리뷰를 추가합니다.
     */
    int insert(Review review) throws SQLException;

    /**
     * 리뷰 ID로 리뷰를 조회합니다.
     */
    Review selectById(Long reviewId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 목록을 조회합니다.
     */
    List<Review> selectByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 사용자 ID로 리뷰 목록을 조회합니다.
     */
    List<Review> selectByUserId(Long userId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 평균 평점을 조회합니다.
     */
    Double selectAverageRatingByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 개수를 조회합니다.
     */
    Integer selectCountByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 리뷰를 업데이트합니다.
     */
    int update(Review review) throws SQLException;

    /**
     * 리뷰 상태를 업데이트합니다.
     */
    int updateStatus(@Param("reviewId") Long reviewId, @Param("status") String status) throws SQLException;

    /**
     * 리뷰를 삭제합니다.
     */
    int delete(Long reviewId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 요약 정보를 조회합니다.
     */
    Map<String, Object> selectReviewSummaryByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 최근 리뷰 목록을 조회합니다.
     */
    List<Review> selectRecentReviews(int limit) throws SQLException;

    /**
     * 평점별 리뷰 개수를 조회합니다.
     */
    Map<Integer, Integer> selectRatingDistributionByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 예약 ID로 리뷰를 조회합니다.
     */
    Review selectByReservationId(Long reservationId) throws SQLException;

    /**
     * 호스트 ID로 리뷰 목록을 조회합니다.
     */
    List<Review> selectByHostId(Long hostId) throws SQLException;

    /**
     * 필터링된 리뷰 목록을 조회합니다.
     */
    List<Review> selectFilteredReviews(Map<String, Object> filters) throws SQLException;
}
