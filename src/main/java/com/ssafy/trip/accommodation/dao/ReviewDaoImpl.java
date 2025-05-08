package com.ssafy.trip.accommodation.dao;

import com.ssafy.trip.accommodation.model.Review;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReviewDao 구현 클래스
 * 이 클래스는 com.ssafy.trip.accommodation.dao.ReviewDao 인터페이스의 구현체로,
 * 실제로는 아무 동작도 하지 않는 더미 구현입니다.
 * 이는 bean 충돌 문제를 해결하기 위한 임시 조치입니다.
 */
@Component("accommodationReviewDao")
public class ReviewDaoImpl implements ReviewDao {

    @Override
    public Long insert(Review review) throws SQLException {
        // 실제 구현은 없음 - 더미 메서드
        return 0L;
    }

    @Override
    public Review getReviewById(Long reviewId) throws SQLException {
        // 실제 구현은 없음 - 더미 메서드
        return null;
    }

    @Override
    public List<Review> getReviewsByUserId(Long userId) throws SQLException {
        // 실제 구현은 없음 - 더미 메서드
        return new ArrayList<>();
    }

    @Override
    public List<Review> getReviewsByAccommodationId(Long accommodationId) throws SQLException {
        // 실제 구현은 없음 - 더미 메서드
        return new ArrayList<>();
    }

    @Override
    public Review getReviewByReservationId(Long reservationId) throws SQLException {
        // 실제 구현은 없음 - 더미 메서드
        return null;
    }

    @Override
    public List<Review> getReviewsByHostId(Long hostId) throws SQLException {
        // 실제 구현은 없음 - 더미 메서드
        return new ArrayList<>();
    }

    @Override
    public int updateReview(Review review) throws SQLException {
        // 실제 구현은 없음 - 더미 메서드
        return 0;
    }

    @Override
    public int deleteReview(Long reviewId) throws SQLException {
        // 실제 구현은 없음 - 더미 메서드
        return 0;
    }

    @Override
    public double getAverageRatingByAccommodationId(Long accommodationId) throws SQLException {
        // 실제 구현은 없음 - 더미 메서드
        return 0.0;
    }

    @Override
    public List<Review> getFilteredReviews(Map<String, Object> filters) throws SQLException {
        // 실제 구현은 없음 - 더미 메서드
        return new ArrayList<>();
    }
}