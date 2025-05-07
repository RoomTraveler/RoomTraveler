package com.ssafy.trip.accommodation.dao;

import com.ssafy.trip.accommodation.model.Reservation;

import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 예약 데이터 액세스 인터페이스
 */
@Mapper
public interface ReservationDao {

    /**
     * 새 예약을 등록합니다.
     * @param reservation 등록할 예약 정보
     * @return 생성된 예약의 ID
     */
    Long insert(Reservation reservation) throws SQLException;

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
     * 객실 ID로 예약 목록을 조회합니다.
     * @param roomId 객실 ID
     * @return 예약 목록
     */
    List<Reservation> getReservationsByRoomId(Long roomId) throws SQLException;

    /**
     * 숙소 ID로 예약 목록을 조회합니다.
     * @param accommodationId 숙소 ID
     * @return 예약 목록
     */
    List<Reservation> getReservationsByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 호스트 ID로 예약 목록을 조회합니다.
     * @param hostId 호스트 ID
     * @return 예약 목록
     */
    List<Reservation> getReservationsByHostId(Long hostId) throws SQLException;

    /**
     * 날짜 범위로 예약 목록을 조회합니다.
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 예약 목록
     */
    List<Reservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException;

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
}
