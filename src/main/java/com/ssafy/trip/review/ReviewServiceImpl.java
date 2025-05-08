package com.ssafy.trip.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 리뷰 서비스 구현 클래스
 * 리뷰 관련 비즈니스 로직을 처리합니다.
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewDao reviewDao;

    /**
     * 생성자 주입을 통한 의존성 주입
     */
    @Autowired
    public ReviewServiceImpl(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    /**
     * 새 리뷰를 생성합니다.
     */
    @Override
    @Transactional
    public Long createReview(Review review) throws SQLException {
        // 생성 시간이 설정되지 않은 경우 현재 시간으로 설정
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }
        
        // 상태가 설정되지 않은 경우 기본값으로 ACTIVE 설정
        if (review.getStatus() == null) {
            review.setStatus("ACTIVE");
        }
        
        reviewDao.insert(review);
        return review.getReviewId();
    }

    /**
     * 리뷰 ID로 리뷰를 조회합니다.
     */
    @Override
    public Review getReviewById(Long reviewId) throws SQLException {
        return reviewDao.selectById(reviewId);
    }

    /**
     * 숙소 ID로 리뷰 목록을 조회합니다.
     */
    @Override
    public List<Review> getReviewsByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectByAccommodationId(accommodationId);
    }

    /**
     * 사용자 ID로 리뷰 목록을 조회합니다.
     */
    @Override
    public List<Review> getReviewsByUserId(Long userId) throws SQLException {
        return reviewDao.selectByUserId(userId);
    }

    /**
     * 숙소 ID로 리뷰 평균 평점을 조회합니다.
     */
    @Override
    public Double getAverageRatingByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectAverageRatingByAccommodationId(accommodationId);
    }

    /**
     * 숙소 ID로 리뷰 개수를 조회합니다.
     */
    @Override
    public Integer getReviewCountByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectCountByAccommodationId(accommodationId);
    }

    /**
     * 리뷰를 업데이트합니다.
     */
    @Override
    @Transactional
    public boolean updateReview(Review review) throws SQLException {
        // 수정 시간 업데이트
        review.setUpdatedAt(LocalDateTime.now());
        return reviewDao.update(review) > 0;
    }

    /**
     * 리뷰 상태를 업데이트합니다.
     */
    @Override
    @Transactional
    public boolean updateReviewStatus(Long reviewId, String status) throws SQLException {
        return reviewDao.updateStatus(reviewId, status) > 0;
    }

    /**
     * 리뷰를 삭제합니다.
     */
    @Override
    @Transactional
    public boolean deleteReview(Long reviewId) throws SQLException {
        return reviewDao.delete(reviewId) > 0;
    }

    /**
     * 숙소 ID로 리뷰 요약 정보를 조회합니다.
     */
    @Override
    public Map<String, Object> getReviewSummaryByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectReviewSummaryByAccommodationId(accommodationId);
    }

    /**
     * 최근 리뷰 목록을 조회합니다.
     */
    @Override
    public List<Review> getRecentReviews(int limit) throws SQLException {
        return reviewDao.selectRecentReviews(limit);
    }

    /**
     * 평점별 리뷰 개수를 조회합니다.
     */
    @Override
    public Map<Integer, Integer> getRatingDistributionByAccommodationId(Long accommodationId) throws SQLException {
        return reviewDao.selectRatingDistributionByAccommodationId(accommodationId);
    }

    /**
     * 리뷰 작성 자격을 확인합니다.
     * 사용자가 해당 숙소에 실제로 숙박했는지 확인합니다.
     * 
     * 참고: 이 메서드는 예약 시스템과 연동하여 구현해야 합니다.
     * 현재는 간단한 구현으로 모든 사용자가 리뷰를 작성할 수 있도록 합니다.
     */
    @Override
    public boolean checkReviewEligibility(Long userId, Long accommodationId) throws SQLException {
        // TODO: 예약 시스템과 연동하여 사용자가 해당 숙소에 실제로 숙박했는지 확인하는 로직 구현
        // 현재는 간단한 구현으로 모든 사용자가 리뷰를 작성할 수 있도록 함
        return true;
    }
}