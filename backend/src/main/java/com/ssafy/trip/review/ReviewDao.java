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

    /**
     * 단일 리뷰 이미지를 추가합니다.
     */
    int insertReviewImage(ReviewImage reviewImage) throws SQLException;

    /**
     * 여러 리뷰 이미지를 추가합니다. (배치 처리용)
     */
    int insertReviewImages(@Param("list") List<ReviewImage> reviewImages) throws SQLException;

    /**
     * 리뷰 ID로 해당 리뷰의 모든 이미지 목록을 조회합니다.
     */
    List<ReviewImage> selectReviewImagesByReviewId(Long reviewId) throws SQLException;

    /**
     * 이미지 ID로 단일 리뷰 이미지를 조회합니다.
     */
    ReviewImage selectReviewImageById(Long imageId) throws SQLException;

    /**
     * 리뷰 ID로 해당 리뷰의 모든 이미지를 삭제합니다.
     */
    int deleteReviewImagesByReviewId(Long reviewId) throws SQLException;

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
     * 특정 이미지의 캡션을 업데이트합니다.
     * @param imageId 이미지 ID
     * @param caption 새로운 캡션
     */
    int updateReviewImageCaption(@Param("imageId") Long imageId, @Param("caption") String caption) throws SQLException;

    /**
     * (선택적) 이미지의 모든 정보를 업데이트합니다.
     * ReviewImage 객체 전체를 받아 업데이트합니다.
     * @param reviewImage 업데이트할 이미지 정보 객체
     */
    int updateReviewImage(ReviewImage reviewImage) throws SQLException;
}
