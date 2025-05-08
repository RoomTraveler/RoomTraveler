package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.dao.ReservationDao;
import com.ssafy.trip.accommodation.dao.RoomDao;
import com.ssafy.trip.accommodation.dao.RoomAvailabilityDao;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.RoomAvailability;
import com.ssafy.trip.review.Review;
import com.ssafy.trip.review.ReviewService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * 예약 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationDao reservationDao;
    private final RoomDao roomDao;
    private final RoomAvailabilityDao roomAvailabilityDao;
    private final ReviewService reviewService;

    /**
     * 새 예약을 등록합니다.
     */
    @Override
    @Transactional
    public Long createReservation(Reservation reservation) throws SQLException {
        // 객실 가용성 확인
        if (!isRoomAvailable(reservation.getRoomId(), reservation.getCheckInDate(), reservation.getCheckOutDate(), reservation.getGuestCount())) {
            throw new SQLException("선택한 날짜에 예약 가능한 객실이 없습니다.");
        }

        // 예약 등록
        Long reservationId = reservationDao.insert(reservation);

        // 객실 가용성 업데이트
        Room room = roomDao.getRoomById(reservation.getRoomId());
        LocalDate currentDate = reservation.getCheckInDate();

        while (!currentDate.isAfter(reservation.getCheckOutDate().minusDays(1))) {
            RoomAvailability availability = roomAvailabilityDao.getAvailabilityByRoomIdAndDate(reservation.getRoomId(), currentDate);

            if (availability != null) {
                // 기존 가용성 정보가 있는 경우 업데이트
                int availableCount = availability.getAvailableCount() - reservation.getGuestCount();
                if (availableCount < 0) availableCount = 0;

                roomAvailabilityDao.updateAvailableCount(reservation.getRoomId(), currentDate, availableCount);
            } else {
                // 기존 가용성 정보가 없는 경우 새로 생성
                int availableCount = room.getRoomCount() - reservation.getGuestCount();
                if (availableCount < 0) availableCount = 0;

                RoomAvailability newAvailability = RoomAvailability.builder()
                        .roomId(reservation.getRoomId())
                        .date(currentDate)
                        .availableCount(availableCount)
                        .price(null) // 기본 가격 사용
                        .build();

                roomAvailabilityDao.insert(newAvailability);
            }

            currentDate = currentDate.plusDays(1);
        }

        return reservationId;
    }

    /**
     * 예약 ID로 예약을 조회합니다.
     */
    @Override
    public Reservation getReservationById(Long reservationId) throws SQLException {
        return reservationDao.getReservationById(reservationId);
    }

    /**
     * 사용자 ID로 예약 목록을 조회합니다.
     */
    @Override
    public List<Reservation> getReservationsByUserId(Long userId) throws SQLException {
        return reservationDao.getReservationsByUserId(userId);
    }

    /**
     * 호스트 ID로 예약 목록을 조회합니다.
     */
    @Override
    public List<Reservation> getReservationsByHostId(Long hostId) throws SQLException {
        return reservationDao.getReservationsByHostId(hostId);
    }

    /**
     * 숙소 ID로 예약 목록을 조회합니다.
     */
    @Override
    public List<Reservation> getReservationsByAccommodationId(Long accommodationId) throws SQLException {
        return reservationDao.getReservationsByAccommodationId(accommodationId);
    }

    /**
     * 객실 ID로 예약 목록을 조회합니다.
     */
    @Override
    public List<Reservation> getReservationsByRoomId(Long roomId) throws SQLException {
        return reservationDao.getReservationsByRoomId(roomId);
    }

    /**
     * 예약 상태를 업데이트합니다.
     */
    @Override
    @Transactional
    public int updateReservationStatus(Long reservationId, String status) throws SQLException {
        return reservationDao.updateReservationStatus(reservationId, status);
    }

    /**
     * 결제 상태를 업데이트합니다.
     */
    @Override
    @Transactional
    public int updatePaymentStatus(Long reservationId, String paymentStatus) throws SQLException {
        return reservationDao.updatePaymentStatus(reservationId, paymentStatus);
    }

    /**
     * 예약을 취소합니다.
     */
    @Override
    @Transactional
    public int cancelReservation(Long reservationId) throws SQLException {
        // 예약 정보 조회
        Reservation reservation = reservationDao.getReservationById(reservationId);
        if (reservation == null) {
            throw new SQLException("예약 정보를 찾을 수 없습니다.");
        }

        // 예약 상태가 이미 취소된 경우
        if ("CANCELLED".equals(reservation.getStatus())) {
            return 0;
        }

        // 객실 가용성 복원
        LocalDate currentDate = reservation.getCheckInDate();
        while (!currentDate.isAfter(reservation.getCheckOutDate().minusDays(1))) {
            RoomAvailability availability = roomAvailabilityDao.getAvailabilityByRoomIdAndDate(reservation.getRoomId(), currentDate);

            if (availability != null) {
                // 가용 객실 수 증가
                int availableCount = availability.getAvailableCount() + reservation.getGuestCount();
                roomAvailabilityDao.updateAvailableCount(reservation.getRoomId(), currentDate, availableCount);
            }

            currentDate = currentDate.plusDays(1);
        }

        // 예약 상태 변경
        return reservationDao.cancelReservation(reservationId);
    }

    /**
     * 예약을 삭제합니다.
     */
    @Override
    @Transactional
    public int deleteReservation(Long reservationId) throws SQLException {
        // 예약 정보 조회
        Reservation reservation = reservationDao.getReservationById(reservationId);
        if (reservation == null) {
            throw new SQLException("예약 정보를 찾을 수 없습니다.");
        }

        // 예약 상태가 취소된 경우에만 삭제 가능
        if (!"CANCELLED".equals(reservation.getStatus())) {
            throw new SQLException("취소된 예약만 삭제할 수 있습니다.");
        }

        // 예약 삭제
        return reservationDao.deleteReservation(reservationId);
    }

    /**
     * 필터링된 예약 목록을 조회합니다.
     */
    @Override
    public List<Reservation> getFilteredReservations(Map<String, Object> filters) throws SQLException {
        return reservationDao.getFilteredReservations(filters);
    }

    /**
     * 객실의 가용성을 확인합니다.
     */
    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount) throws SQLException {
        // 객실 정보 조회
        Room room = roomDao.getRoomById(roomId);
        if (room == null) {
            throw new SQLException("객실 정보를 찾을 수 없습니다.");
        }

        // 객실 상태 확인
        if (!"AVAILABLE".equals(room.getStatus())) {
            return false;
        }

        // 날짜별 가용성 확인
        LocalDate currentDate = checkInDate;
        while (!currentDate.isAfter(checkOutDate.minusDays(1))) {
            RoomAvailability availability = roomAvailabilityDao.getAvailabilityByRoomIdAndDate(roomId, currentDate);

            // 가용성 정보가 없는 경우 객실 수로 판단
            int availableCount = (availability != null) ? availability.getAvailableCount() : room.getRoomCount();

            // 예약 가능 여부 확인
            if (availableCount < guestCount) {
                return false;
            }

            currentDate = currentDate.plusDays(1);
        }

        return true;
    }

    /**
     * 날짜 범위에 대한 객실 가용성 정보를 조회합니다.
     */
    @Override
    public List<RoomAvailability> getRoomAvailabilities(Long roomId, LocalDate startDate, LocalDate endDate) throws SQLException {
        return roomAvailabilityDao.getAvailabilitiesByRoomIdAndDateRange(roomId, startDate, endDate);
    }

    /**
     * 객실 가용성 정보를 업데이트합니다.
     */
    @Override
    @Transactional
    public int updateRoomAvailability(Long roomId, LocalDate date, Integer availableCount, Double price) throws SQLException {
        // 가격만 업데이트하는 경우
        if (availableCount == null && price != null) {
            return roomAvailabilityDao.updatePrice(roomId, date, price);
        }

        // 가용 객실 수만 업데이트하는 경우
        if (availableCount != null && price == null) {
            return roomAvailabilityDao.updateAvailableCount(roomId, date, availableCount);
        }

        // 둘 다 업데이트하는 경우
        RoomAvailability availability = roomAvailabilityDao.getAvailabilityByRoomIdAndDate(roomId, date);

        if (availability != null) {
            availability.setAvailableCount(availableCount);
            availability.setPrice(price != null ? new BigDecimal(String.valueOf(price)) : null);
            return roomAvailabilityDao.updateAvailability(availability);
        } else {
            // 새로 생성
            RoomAvailability newAvailability = RoomAvailability.builder()
                    .roomId(roomId)
                    .date(date)
                    .availableCount(availableCount)
                    .price(price != null ? new BigDecimal(String.valueOf(price)) : null)
                    .build();

            roomAvailabilityDao.insert(newAvailability);
            return 1;
        }
    }

    /**
     * 예약에 대한 리뷰를 작성합니다.
     * 
     * 예약 정보를 확인하고 리뷰를 생성합니다.
     * 완료된 예약에 대해서만 리뷰를 작성할 수 있습니다.
     */
    @Override
    @Transactional
    public Long createReview(com.ssafy.trip.review.Review review) throws SQLException {
        // 예약 정보 조회
        Reservation reservation = reservationDao.getReservationById(review.getReservationId());
        if (reservation == null) {
            throw new SQLException("예약 정보를 찾을 수 없습니다.");
        }

        // 예약 상태 확인 (완료된 예약만 리뷰 작성 가능)
        if (!"COMPLETED".equals(reservation.getStatus())) {
            throw new SQLException("완료된 예약에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        // 이미 리뷰가 있는지 확인
        com.ssafy.trip.review.Review existingReview = reviewService.getReviewByReservationId(review.getReservationId());
        if (existingReview != null) {
            throw new SQLException("이미 리뷰가 작성되었습니다.");
        }

        // 리뷰 작성 전 필수 필드 설정
        if (review.getIsVerified() == null) {
            review.setIsVerified(true); // 예약 정보가 확인되었으므로 true
        }
        if (review.getStatus() == null) {
            review.setStatus("ACTIVE");
        }
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(LocalDateTime.now());
        }

        // 리뷰 작성
        return reviewService.createReview(review);
    }

    /**
     * 예약 ID로 리뷰를 조회합니다.
     * 
     * 예약 ID를 기반으로 리뷰 정보를 조회합니다.
     * 해당 예약에 대한 리뷰가 없는 경우 null을 반환합니다.
     */
    @Override
    public Review getReviewByReservationId(Long reservationId) throws SQLException {
        return reviewService.getReviewByReservationId(reservationId);
    }

    /**
     * 숙소 ID로 리뷰 목록을 조회합니다.
     * 
     * 특정 숙소에 작성된 모든 리뷰를 조회합니다.
     * 리뷰는 최신순으로 정렬됩니다.
     */
    @Override
    public List<Review> getReviewsByAccommodationId(Long accommodationId) throws SQLException {
        return reviewService.getReviewsByAccommodationId(accommodationId);
    }

    /**
     * 사용자 ID로 리뷰 목록을 조회합니다.
     * 
     * 특정 사용자가 작성한 모든 리뷰를 조회합니다.
     * 리뷰는 최신순으로 정렬됩니다.
     */
    @Override
    public List<Review> getReviewsByUserId(Long userId) throws SQLException {
        return reviewService.getReviewsByUserId(userId);
    }

    /**
     * 리뷰 ID로 리뷰를 조회합니다.
     * 
     * 리뷰 ID를 기반으로 리뷰 정보를 조회합니다.
     * 해당 ID의 리뷰가 없는 경우 null을 반환합니다.
     */
    @Override
    public Review getReviewById(Long reviewId) throws SQLException {
        return reviewService.getReviewById(reviewId);
    }

    /**
     * 리뷰를 업데이트합니다.
     * 
     * 리뷰 작성자만 리뷰를 수정할 수 있습니다.
     * 리뷰 내용, 평점 등을 업데이트합니다.
     */
    @Override
    @Transactional
    public int updateReview(Review review) throws SQLException {
        // 기존 리뷰 조회
        Review existingReview = reviewService.getReviewById(review.getReviewId());
        if (existingReview == null) {
            throw new SQLException("리뷰 정보를 찾을 수 없습니다.");
        }

        // 리뷰 작성자 확인
        if (!existingReview.getUserId().equals(review.getUserId())) {
            throw new SQLException("리뷰 작성자만 수정할 수 있습니다.");
        }

        // 수정 시간 업데이트
        review.setUpdatedAt(LocalDateTime.now());

        // 기존 값 유지 (수정되지 않은 필드)
        if (review.getTitle() == null) {
            review.setTitle(existingReview.getTitle());
        }
        if (review.getIsVerified() == null) {
            review.setIsVerified(existingReview.getIsVerified());
        }
        if (review.getStatus() == null) {
            review.setStatus(existingReview.getStatus());
        }
        if (review.getCreatedAt() == null) {
            review.setCreatedAt(existingReview.getCreatedAt());
        }

        // 리뷰 업데이트
        boolean updated = reviewService.updateReview(review);
        return updated ? 1 : 0;
    }

    /**
     * 리뷰를 삭제합니다.
     * 
     * 리뷰 ID를 기반으로 리뷰를 삭제합니다.
     * 삭제 성공 시 1을 반환하고, 실패 시 0을 반환합니다.
     */
    @Override
    @Transactional
    public int deleteReview(Long reviewId) throws SQLException {
        boolean deleted = reviewService.deleteReview(reviewId);
        return deleted ? 1 : 0;
    }

    /**
     * 숙소 ID로 평균 평점을 조회합니다.
     * 
     * 특정 숙소에 작성된 모든 리뷰의 평균 평점을 계산합니다.
     * 리뷰가 없는 경우 0.0을 반환합니다.
     */
    @Override
    public double getAverageRatingByAccommodationId(Long accommodationId) throws SQLException {
        Double averageRating = reviewService.getAverageRatingByAccommodationId(accommodationId);
        return averageRating != null ? averageRating : 0.0;
    }

    /**
     * 날짜 범위에 대해 가용한 객실 목록을 조회합니다.
     */
    @Override
    public List<Room> getAvailableRooms(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount) throws SQLException {
        return roomDao.getAvailableRooms(accommodationId, checkInDate, checkOutDate);
    }
}
