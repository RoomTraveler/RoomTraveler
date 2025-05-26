package com.ssafy.trip.review.dao;

import com.ssafy.trip.review.model.Review;
import com.ssafy.trip.review.model.ReviewImage;
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
    int insertReview(Review review) throws SQLException;

    /**
     * 리뷰 ID로 리뷰를 조회합니다.
     */
    Review selectReviewById(Long reviewId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 목록을 조회합니다.
     */
    List<Review> selectReviewsByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 사용자 ID로 리뷰 목록을 조회합니다.
     */
    List<Review> selectReviewsByUserId(Long userId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 평균 평점을 조회합니다.
     */
    Double selectAverageRatingByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 개수를 조회합니다.
     */
    Integer selectCountByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 통계(평균 별점, 리뷰 수)를 조회합니다.
     * @param accommodationId 숙소 ID
     * @return Map (keys: "avg_rating", "review_count")
     */
    Map<String, Object> selectReviewStatsByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 리뷰를 업데이트합니다.
     */
    int updateReview(Review review) throws SQLException;

    /**
     * 리뷰 상태를 업데이트합니다.
     */
    int updateStatus(@Param("reviewId") Long reviewId, @Param("status") String status) throws SQLException;

    /**
     * 리뷰를 삭제합니다.
     */
    int deleteReviewById(@Param("reviewId") Long reviewId, @Param("userId") Long userId) throws SQLException;

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
    Review selectReviewByReservationId(Long reservationId) throws SQLException;

    /**
     * 호스트 ID로 리뷰 목록을 조회합니다.
     */
    List<Review> selectReviewsByHostId(Long hostId) throws SQLException;

    /**
     * 필터링된 리뷰 목록을 조회합니다.
     */
    List<Review> selectFilteredReviews(Map<String, Object> filters) throws SQLException;

    /**
     * 단일 리뷰 이미지를 추가합니다.
     */
    int insertReviewImage(ReviewImage reviewImage) throws SQLException;

    /**
     * 리뷰 ID로 해당 리뷰의 모든 이미지를 삭제합니다.
     */
    int deleteReviewImagesByReviewId(Long reviewId) throws SQLException;

    /**
     * 이미지 ID로 단일 리뷰 이미지를 조회합니다.
     */
    ReviewImage selectReviewImageById(Long imageId) throws SQLException;

    /**
     * 이미지 ID로 특정 이미지를 삭제합니다.
     */
    int deleteReviewImageById(Long imageId) throws SQLException;

    /**
     * 특정 이미지의 썸네일 상태를 업데이트합니다.
     * @param imageId 이미지 ID
     * @param isThumbnail 썸네일 여부
     */
    int updateReviewImageThumbnailStatus(@Param("imageId") Long imageId, @Param("isThumbnail") boolean isThumbnail) throws SQLException;

    /**
     * 특정 리뷰에 속한 모든 이미지들의 썸네일 상태를 false로 초기화합니다.
     * @param reviewId 리뷰 ID
     */
    int resetThumbnailStatusByReviewId(Long reviewId) throws SQLException;

    /**
     * 특정 이미지의 정렬 순서를 업데이트합니다.
     * @param imageId 이미지 ID
     * @param sortOrder 새로운 정렬 순서
     */
    int updateReviewImageSortOrder(@Param("imageId") Long imageId, @Param("sortOrder") int sortOrder) throws SQLException;

    /**
     * (선택적) 이미지의 모든 정보를 업데이트합니다.
     * ReviewImage 객체 전체를 받아 업데이트합니다.
     * @param reviewImage 업데이트할 이미지 정보 객체
     */
    int updateReviewImage(ReviewImage reviewImage) throws SQLException;

    /**
     * 특정 리뷰 이미지의 캡션을 업데이트합니다.
     * @param imageId 캡션을 수정할 이미지 ID
     * @param caption 새로운 캡션 내용
     */
    int updateReviewImageCaption(@Param("imageId") Long imageId, @Param("caption") String caption) throws SQLException;

    /**
     * 사용자가 특정 숙소에 대해 결제 완료한 예약이 있는지 확인 (리뷰 작성 권한 검사용)
     */
    boolean checkUserReservationForAccommodation(@Param("userId") Long userId, @Param("accommodationId") Long accommodationId) throws SQLException;
}
