package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.model.Review;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.RoomAvailability;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 예약 서비스 인터페이스
 */
public interface ReservationService {

    /**
     * 새 예약을 등록합니다.
     * @param reservation 등록할 예약 정보
     * @return 생성된 예약의 ID
     */
    Long createReservation(Reservation reservation) throws SQLException;

    /**
     * 예약 ID로 예약을 조회합니다.
     * @param reservationId 예약 ID
     * @return 예약 정보
     */
    Reservation getReservationById(Long reservationId) throws SQLException;

    /**
     * 사용자 ID로 예약 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 예약 목록
     */
    List<Reservation> getReservationsByUserId(Long userId) throws SQLException;

    /**
     * 호스트 ID로 예약 목록을 조회합니다.
     * @param hostId 호스트 ID
     * @return 예약 목록
     */
    List<Reservation> getReservationsByHostId(Long hostId) throws SQLException;

    /**
     * 숙소 ID로 예약 목록을 조회합니다.
     * @param accommodationId 숙소 ID
     * @return 예약 목록
     */
    List<Reservation> getReservationsByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 객실 ID로 예약 목록을 조회합니다.
     * @param roomId 객실 ID
     * @return 예약 목록
     */
    List<Reservation> getReservationsByRoomId(Long roomId) throws SQLException;

    /**
     * 예약 상태를 업데이트합니다.
     * @param reservationId 예약 ID
     * @param status 새 상태
     * @return 업데이트된 행 수
     */
    int updateReservationStatus(Long reservationId, String status) throws SQLException;

    /**
     * 결제 상태를 업데이트합니다.
     * @param reservationId 예약 ID
     * @param paymentStatus 새 결제 상태
     * @return 업데이트된 행 수
     */
    int updatePaymentStatus(Long reservationId, String paymentStatus) throws SQLException;

    /**
     * 예약을 취소합니다.
     * @param reservationId 예약 ID
     * @return 업데이트된 행 수
     */
    int cancelReservation(Long reservationId) throws SQLException;

    /**
     * 예약을 삭제합니다.
     * @param reservationId 삭제할 예약 ID
     * @return 삭제된 행 수
     */
    int deleteReservation(Long reservationId) throws SQLException;

    /**
     * 필터링된 예약 목록을 조회합니다.
     * @param filters 필터 조건 (키: 필터 이름, 값: 필터 값)
     * @return 필터링된 예약 목록
     */
    List<Reservation> getFilteredReservations(Map<String, Object> filters) throws SQLException;

    /**
     * 객실의 가용성을 확인합니다.
     * @param roomId 객실 ID
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param guestCount 투숙객 수
     * @return 가용 여부
     */
    boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount) throws SQLException;

    /**
     * 날짜 범위에 대한 객실 가용성 정보를 조회합니다.
     * @param roomId 객실 ID
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 객실 가용성 정보 목록
     */
    List<RoomAvailability> getRoomAvailabilities(Long roomId, LocalDate startDate, LocalDate endDate) throws SQLException;

    /**
     * 객실 가용성 정보를 업데이트합니다.
     * @param roomId 객실 ID
     * @param date 날짜
     * @param availableCount 가용 객실 수
     * @param price 특별 가격 (null인 경우 기본 가격 적용)
     * @return 업데이트된 행 수
     */
    int updateRoomAvailability(Long roomId, LocalDate date, Integer availableCount, Double price) throws SQLException;

    /**
     * 예약에 대한 리뷰를 작성합니다.
     * @param review 작성할 리뷰 정보
     * @return 생성된 리뷰의 ID
     */
    Long createReview(Review review) throws SQLException;

    /**
     * 예약 ID로 리뷰를 조회합니다.
     * @param reservationId 예약 ID
     * @return 리뷰 정보
     */
    Review getReviewByReservationId(Long reservationId) throws SQLException;

    /**
     * 숙소 ID로 리뷰 목록을 조회합니다.
     * @param accommodationId 숙소 ID
     * @return 리뷰 목록
     */
    List<Review> getReviewsByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 사용자 ID로 리뷰 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 리뷰 목록
     */
    List<Review> getReviewsByUserId(Long userId) throws SQLException;

    /**
     * 리뷰 ID로 리뷰를 조회합니다.
     * @param reviewId 리뷰 ID
     * @return 리뷰 정보
     */
    Review getReviewById(Long reviewId) throws SQLException;

    /**
     * 리뷰를 업데이트합니다.
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
     * 날짜 범위에 대해 가용한 객실 목록을 조회합니다.
     * @param accommodationId 숙소 ID
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param guestCount 투숙객 수
     * @return 가용한 객실 목록
     */
    List<Room> getAvailableRooms(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount) throws SQLException;
}
