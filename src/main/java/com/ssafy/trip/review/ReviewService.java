package com.ssafy.trip.review;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 서비스 인터페이스
 * 리뷰 관련 비즈니스 로직을 처리합니다.
 */
public interface ReviewService {
    /**
     * 새 리뷰를 생성합니다.
     */
    Long createReview(Review review) throws SQLException;
    
    /**
     * 리뷰 ID로 리뷰를 조회합니다.
     */
    Review getReviewById(Long reviewId) throws SQLException;
    
    /**
     * 숙소 ID로 리뷰 목록을 조회합니다.
     */
    List<Review> getReviewsByAccommodationId(Long accommodationId) throws SQLException;
    
    /**
     * 사용자 ID로 리뷰 목록을 조회합니다.
     */
    List<Review> getReviewsByUserId(Long userId) throws SQLException;
    
    /**
     * 숙소 ID로 리뷰 평균 평점을 조회합니다.
     */
    Double getAverageRatingByAccommodationId(Long accommodationId) throws SQLException;
    
    /**
     * 숙소 ID로 리뷰 개수를 조회합니다.
     */
    Integer getReviewCountByAccommodationId(Long accommodationId) throws SQLException;
    
    /**
     * 리뷰를 업데이트합니다.
     */
    boolean updateReview(Review review) throws SQLException;
    
    /**
     * 리뷰 상태를 업데이트합니다.
     */
    boolean updateReviewStatus(Long reviewId, String status) throws SQLException;
    
    /**
     * 리뷰를 삭제합니다.
     */
    boolean deleteReview(Long reviewId) throws SQLException;
    
    /**
     * 숙소 ID로 리뷰 요약 정보를 조회합니다.
     */
    Map<String, Object> getReviewSummaryByAccommodationId(Long accommodationId) throws SQLException;
    
    /**
     * 최근 리뷰 목록을 조회합니다.
     */
    List<Review> getRecentReviews(int limit) throws SQLException;
    
    /**
     * 평점별 리뷰 개수를 조회합니다.
     */
    Map<Integer, Integer> getRatingDistributionByAccommodationId(Long accommodationId) throws SQLException;
    
    /**
     * 리뷰 작성 자격을 확인합니다.
     * 사용자가 해당 숙소에 실제로 숙박했는지 확인합니다.
     */
    boolean checkReviewEligibility(Long userId, Long accommodationId) throws SQLException;
}