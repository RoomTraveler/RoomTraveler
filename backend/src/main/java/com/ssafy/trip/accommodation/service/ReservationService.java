package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.RoomAvailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 예약 서비스 인터페이스
 * 예약 생성, 조회, 수정, 취소 및 관련 비즈니스 로직을 처리합니다.
 */
public interface ReservationService {

    /**
     * 예약 폼을 위한 데이터를 조회합니다.
     * 객실 정보, 체크인/아웃 날짜, 투숙객 수, 총 숙박일수, 총 가격 등을 포함합니다.
     * 객실 가용성을 확인하여 예약 불가능할 경우 예외를 발생시킵니다.
     *
     * @param roomId      객실 ID
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param guestCount  투숙객 수
     * @param userId      현재 사용자 ID (로그인 확인용)
     * @return 예약 _Form_ 데이터를 담은 Map
     * @throws SQLException 데이터베이스 오류 또는 객실 예약 불가 시
     */
    Map<String, Object> getReservationFormData(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount, Long userId) throws SQLException;

    /**
     * 새 예약을 등록합니다.
     * 사용자 ID, 예약 상태(PENDING), 결제 상태(UNPAID)를 설정하여 예약을 생성합니다.
     * 객실 가용성을 확인하고, 예약 생성 후 객실 가용성 정보를 업데이트합니다.
     *
     * @param reservation 등록할 예약 정보 (userId, status, paymentStatus는 서비스에서 설정)
     * @param userId      예약자 ID
     * @return 생성된 예약의 ID
     * @throws SQLException 데이터베이스 오류 또는 객실 예약 불가 시
     */
    Long createReservation(Reservation reservation, Long userId) throws SQLException;

    /**
     * 장바구니의 항목들로 여러 예약을 한 번에 생성합니다.
     * 각 장바구니 항목에 대해 예약을 생성하고, 생성 후 장바구니를 비웁니다.
     *
     * @param cartItems       장바구니 아이템 목록
     * @param userId          예약자 ID
     * @param specialRequests 공통 특별 요청 사항
     * @return 생성된 예약들의 ID 목록 (또는 성공 메시지, 현재는 void로 처리 가능성 있음 - 컨트롤러 반환값에 맞춰 조정)
     * @throws SQLException 데이터베이스 오류 또는 장바구니가 비어있거나 객실 예약 불가 시
     */
    List<Long> createReservationsFromCart(List<Map<String, Object>> cartItems, Long userId, String specialRequests) throws SQLException;

    /**
     * 예약 ID로 예약을 조회합니다. 예약자 본인 또는 숙소 호스트만 조회 가능합니다.
     * 리뷰 정보는 이 메서드에서 직접 반환하지 않습니다.
     * 필요한 경우, 클라이언트가 ApiReviewController를 통해 별도로 조회해야 합니다.
     * (예: GET /api/v1/reviews/reservation/{reservationId})
     *
     * @param reservationId 예약 ID
     * @param userId        현재 사용자 ID (권한 확인용)
     * @param userRole      현재 사용자 역할 (권한 확인용)
     * @return 예약 정보를 담은 Map
     * @throws SQLException 데이터베이스 오류 또는 조회 권한 없음
     */
    Map<String, Object> getReservationDetail(Long reservationId, Long userId, String userRole) throws SQLException;

    /**
     * 사용자 ID로 해당 사용자의 모든 예약 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 예약 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    List<Reservation> getReservationsByUserId(Long userId) throws SQLException;

    /**
     * 호스트 ID로 해당 호스트가 관리하는 숙소의 모든 예약 목록을 조회합니다.
     * 관리자(ADMIN)도 조회 가능합니다.
     *
     * @param hostId   호스트 ID (현재 세션의 사용자 ID)
     * @param userRole 현재 사용자 역할 (HOST 또는 ADMIN)
     * @return 예약 목록
     * @throws SQLException 데이터베이스 오류 또는 권한 없음
     */
    List<Reservation> getReservationsByHostId(Long hostId, String userRole) throws SQLException;

    /**
     * 숙소 ID로 예약 목록을 조회합니다.
     * (내부 관리용 또는 특정 숙소의 모든 예약을 보고자 할 때 사용)
     *
     * @param accommodationId 숙소 ID
     * @return 예약 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    List<Reservation> getReservationsByAccommodationId(Long accommodationId) throws SQLException;

    /**
     * 객실 ID로 예약 목록을 조회합니다.
     * (내부 관리용 또는 특정 객실의 모든 예약을 보고자 할 때 사용)
     *
     * @param roomId 객실 ID
     * @return 예약 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    List<Reservation> getReservationsByRoomId(Long roomId) throws SQLException;

    /**
     * 예약 상태를 업데이트합니다. 호스트 또는 관리자만 가능합니다.
     *
     * @param reservationId 예약 ID
     * @param status        새로운 예약 상태
     * @param updaterId     현재 사용자 ID (호스트 ID 또는 관리자 ID)
     * @param userRole      현재 사용자 역할 (HOST 또는 ADMIN)
     * @return 업데이트된 행 수 (성공 시 1)
     * @throws SQLException 데이터베이스 오류 또는 권한 없음
     */
    int updateReservationStatus(Long reservationId, String status, Long updaterId, String userRole) throws SQLException;

    /**
     * 결제 상태를 업데이트합니다. 예약자 본인만 가능합니다.
     *
     * @param reservationId 예약 ID
     * @param paymentStatus 새로운 결제 상태
     * @param userId        현재 사용자 ID (예약자 ID)
     * @return 업데이트된 행 수 (성공 시 1)
     * @throws SQLException 데이터베이스 오류 또는 권한 없음
     */
    int updatePaymentStatus(Long reservationId, String paymentStatus, Long userId) throws SQLException;

    /**
     * 예약을 취소합니다. 예약자 본인만 가능합니다.
     * 예약 취소 시 객실 가용성을 복원합니다.
     *
     * @param reservationId 예약 ID
     * @param userId        현재 사용자 ID (예약자 ID)
     * @return 업데이트된 행 수 (성공 시 1)
     * @throws SQLException 데이터베이스 오류 또는 권한 없음
     */
    int cancelReservation(Long reservationId, Long userId) throws SQLException;

    /**
     * 예약을 삭제합니다. (소프트 삭제 또는 특정 조건 만족 시 물리 삭제)
     * 현재는 CANCELLED 상태의 예약만 삭제 가능하도록 가정합니다.
     *
     * @param reservationId 삭제할 예약 ID
     * @return 삭제된 행 수
     * @throws SQLException 데이터베이스 오류 또는 삭제 조건 불충족
     */
    int deleteReservation(Long reservationId) throws SQLException;

    /**
     * 필터링된 예약 목록을 조회합니다.
     * (관리자 페이지 등에서 다양한 조건으로 검색 시 사용)
     *
     * @param filters 필터 조건 (키: 필터 이름, 값: 필터 값)
     * @return 필터링된 예약 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    List<Reservation> getFilteredReservations(Map<String, Object> filters) throws SQLException;

    /**
     * 특정 기간 동안 객실의 가용성을 확인합니다.
     *
     * @param roomId       객실 ID
     * @param checkInDate  체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param guestCount   투숙객 수
     * @return 가용 여부 (true: 예약 가능, false: 예약 불가능)
     * @throws SQLException 데이터베이스 오류 또는 객실 정보 없음
     */
    boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount) throws SQLException;

    /**
     * 날짜 범위에 대한 객실 가용성 정보를 조회합니다.
     * (달력 UI 등에서 특정 객실의 날짜별 가용 현황 표시 시 사용)
     *
     * @param roomId    객실 ID
     * @param startDate 시작 날짜
     * @param endDate   종료 날짜
     * @return 객실 가용성 정보 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    List<RoomAvailability> getRoomAvailabilities(Long roomId, LocalDate startDate, LocalDate endDate) throws SQLException;

    /**
     * 객실 가용성 정보를 업데이트합니다. (주로 관리자가 수동으로 조정 시 사용)
     *
     * @param roomId         객실 ID
     * @param date           날짜
     * @param availableCount 가용 객실 수 (null인 경우 가격만 업데이트)
     * @param price          특별 가격 (null인 경우 기본 가격 적용 또는 가용 객실 수만 업데이트)
     * @return 업데이트된 행 수
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    int updateRoomAvailability(Long roomId, LocalDate date, Integer availableCount, Double price) throws SQLException;

    /**
     * 날짜 범위와 투숙객 수에 대해 예약 가능한 객실 목록을 조회합니다.
     * (숙소 상세 페이지 등에서 사용)
     *
     * @param accommodationId 숙소 ID
     * @param checkInDate     체크인 날짜
     * @param checkOutDate    체크아웃 날짜
     * @param guestCount      투숙객 수
     * @return 가용한 객실 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    List<Room> getAvailableRooms(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount) throws SQLException;

    /**
     * 특정 객실에 대해 주어진 기간 동안의 최소 가용 객실 수를 계산합니다.
     * 이 계산은 room_availability 테이블과 reservations 테이블을 모두 고려하며, 객실의 수용 인원도 고려합니다.
     *
     * @param room        대상 객실 (room_count, capacity 정보 포함)
     * @param startDate   조회 시작 날짜
     * @param endDate     조회 종료 날짜 (이 날짜 미포함)
     * @param guests      요청된 총 투숙객 수
     * @return 해당 기간 동안 매일 예약 가능한 객실 수 중 최소값 (수용 인원 초과 시 0 반환)
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    int calculateMinAvailableCountForRoom(Room room, LocalDate startDate, LocalDate endDate, Integer guests) throws SQLException;

    /**
     * 특정 호스트의 예약 목록을 필터링 및 페이징하여 조회합니다.
     *
     * @param hostId 호스트 ID
     * @param status 예약 상태
     * @param checkInDate 체크인 날짜 (YYYY-MM-DD 형식의 문자열)
     * @param guestName 게스트 이름
     * @param sortBy 정렬 기준
     * @param pageable 페이징 정보
     * @return 페이징된 예약 목록
     */
    Page<Reservation> getReservationsByHostIdWithFiltersAndPaging(Long hostId, String status, String checkInDate, String guestName, String sortBy, Pageable pageable);
}
