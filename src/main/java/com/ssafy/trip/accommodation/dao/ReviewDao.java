package com.ssafy.trip.accommodation.dao;

import com.ssafy.trip.accommodation.model.Review;

import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 데이터 액세스 인터페이스
 */
@Mapper
public interface ReviewDao {

    /**
     * 새 리뷰를 등록합니다.
     * @param review 등록할 리뷰 정보
     * @return 생성된 리뷰의 ID
     */
    Long insert(Review review) throws SQLException;

    /**
     * 리뷰 ID로 리뷰를 조회합니다.
     * @param reviewId 리뷰 ID
     * @return 리뷰 정보
     */
    Review getReviewById(Long reviewId) throws SQLException;

    /**
     * 사용자 ID로 리뷰 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 리뷰 목록
     */
    List<Review> getReviewsByUserId(Long userId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 목록을 조회합니다.
     * @param accommodationId 숙소 ID
     * @return 리뷰 목록
     */
    List<Review> getReviewsByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 예약 ID로 리뷰를 조회합니다.
     * @param reservationId 예약 ID
     * @return 리뷰 정보
     */
    Review getReviewByReservationId(Long reservationId) throws SQLException;

    /**
     * 호스트 ID로 리뷰 목록을 조회합니다.
     * @param hostId 호스트 ID
     * @return 리뷰 목록
     */
    List<Review> getReviewsByHostId(Long hostId) throws SQLException;

    /**
     * 리뷰 정보를 업데이트합니다.
     * @param review 업데이트할 리뷰 정보
     * @return 업데이트된 행 수
     */
    int updateReview(Review review) throws SQLException;

    /**
     * 리뷰를 삭제합니다.
     * @param reviewId 삭제할 리뷰 ID
     * @return 삭제된 행 수
     */
    int deleteReview(Long reviewId) throws SQLException;

    /**
     * 숙소 ID로 평균 평점을 조회합니다.
     * @param accommodationId 숙소 ID
     * @return 평균 평점
     */
    double getAverageRatingByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 필터링된 리뷰 목록을 조회합니다.
     * @param filters 필터 조건 (키: 필터 이름, 값: 필터 값)
     * @return 필터링된 리뷰 목록
     */
    List<Review> getFilteredReviews(Map<String, Object> filters) throws SQLException;
}
