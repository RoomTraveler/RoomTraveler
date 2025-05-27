package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.dao.AccommodationDao;
import com.ssafy.trip.accommodation.dao.ReservationDao;
import com.ssafy.trip.accommodation.dao.RoomDao;
import com.ssafy.trip.accommodation.dao.RoomAvailabilityDao;
import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.RoomAvailability;
// import com.ssafy.trip.review.model.Review; // Review 모델은 더 이상 직접 사용하지 않음
// import com.ssafy.trip.review.service.ReviewService; // ReviewService 의존성 제거

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
// import java.io.IOException; // MultipartFile 관련 import도 ReviewService와 함께 제거될 수 있음 (현재는 사용되지 않음)
// import org.springframework.web.multipart.MultipartFile; // ReviewService와 함께 제거될 수 있음 (현재는 사용되지 않음)
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.slf4j.LoggerFactory; // 잠시 추가, Slf4j 없을 경우 대비
import lombok.extern.slf4j.Slf4j; // Slf4j 어노테이션 추가
import com.ssafy.trip.exception.InvalidRequestException;
import com.ssafy.trip.exception.ResourceNotFoundException;
import com.ssafy.trip.payment.dto.PaymentPrepareRequestDto; // DTO 임포트
import com.ssafy.trip.payment.dto.ReservationCreationDto; // ReservationCreationDto 임포트 추가
import com.ssafy.trip.cart.service.CartService; // CartService 임포트
import java.util.UUID; // UUID 임포트
import com.ssafy.trip.user.User; // User 클래스 임포트 추가
import com.ssafy.trip.user.UserService; // UserService 임포트 추가

/**
 * 예약 서비스 구현 클래스
 * 예약 생성, 조회, 수정, 취소 및 관련 비즈니스 로직을 처리합니다.
 * 필요한 경우 AccommodationService 등 다른 서비스를 호출합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j // Slf4j 로거 어노테이션 추가
public class ReservationServiceImpl implements ReservationService {

    private final ReservationDao reservationDao;
    private final RoomDao roomDao;
    private final RoomAvailabilityDao roomAvailabilityDao;
    private final AccommodationDao accommodationDao; // 숙소 정보 접근을 위해 추가
    private final CartService cartService; // CartService 주입
    private final UserService userService; // UserService 필드 주입 추가
    // private final ReviewService reviewService; // ReviewService 필드 제거

    /**
     * 예약 폼을 위한 데이터를 조회합니다.
     * 객실 정보, 체크인/아웃 날짜, 투숙객 수, 총 숙박일수, 총 가격 등을 포함합니다.
     * 객실 가용성을 확인하여 예약 불가능할 경우 예외를 발생시킵니다.
     *
     * @param roomId      객실 ID
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param guestCount  투숙객 수
     * @param userId      현재 사용자 ID (로그인 확인용, 현재는 직접 사용되지 않으나 향후 확장 가능성 있음)
     * @return 예약 _Form_ 데이터를 담은 Map
     * @throws SQLException 데이터베이스 오류 또는 객실 예약 불가 시
     */
    @Override
    public Map<String, Object> getReservationFormData(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount, Long userId) throws SQLException {
        Room room = roomDao.getRoomById(roomId);
        if (room == null) {
            throw new SQLException("객실 정보를 찾을 수 없습니다.");
        }

        boolean isAvailable = isRoomAvailable(roomId, checkInDate, checkOutDate, guestCount);
        if (!isAvailable) {
            throw new SQLException("선택한 날짜에 예약 가능한 객실이 없습니다.");
        }

        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nights <= 0) {
            throw new SQLException("체크아웃 날짜는 체크인 날짜 이후여야 합니다.");
        }
        BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(nights));

        Map<String, Object> result = new HashMap<>();
        result.put("room", room);
        result.put("checkInDate", checkInDate);
        result.put("checkOutDate", checkOutDate);
        result.put("guestCount", guestCount);
        result.put("nights", nights);
        result.put("totalPrice", totalPrice);
        return result;
    }

    private String generateUniqueMerchantUid() {
        String merchantUid;
        do {
            merchantUid = UUID.randomUUID().toString();
        } while (reservationDao.existsByMerchantUid(merchantUid));
        return merchantUid;
    }

    
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
    @Override
    @Transactional
    public Long createReservation(Reservation reservation, Long userId) throws SQLException {
        // 객실 가용성 확인
        if (!isRoomAvailable(reservation.getRoomId(), reservation.getCheckInDate(), reservation.getCheckOutDate(), reservation.getGuestCount())) {
            throw new SQLException("선택한 날짜에 예약 가능한 객실이 없습니다.");
        }

        reservation.setUserId(userId);
        reservation.setStatus("PENDING"); // 기본 상태
        reservation.setPaymentStatus("UNPAID"); // 기본 결제 상태
        reservation.setCreatedAt(LocalDateTime.now());

        // 예약 등록
        Long reservationId = reservationDao.insert(reservation);

        // 객실 가용성 업데이트 (체크아웃 날짜는 포함하지 않음)
        updateRoomAvailabilityForBooking(reservation.getRoomId(), reservation.getCheckInDate(), reservation.getCheckOutDate(), reservation.getGuestCount(), true);

        return reservationId;
    }

    /**
     * 장바구니의 항목들로 여러 예약을 한 번에 생성합니다.
     * 각 장바구니 항목에 대해 예약을 생성하고, 생성 후 장바구니를 비웁니다.
     * 현재 cartItems는 Map 형태의 DTO로 전달받는다고 가정합니다.
     * 실제 CartItem 클래스가 있다면 해당 클래스를 사용하는 것이 좋습니다.
     *
     * @param cartItems       장바구니 아이템 목록 (Map: roomId, checkInDate, checkOutDate, guestCount, price)
     * @param userId          예약자 ID
     * @param specialRequests 공통 특별 요청 사항
     * @return 생성된 예약들의 ID 목록
     * @throws SQLException 데이터베이스 오류 또는 장바구니가 비어있거나 객실 예약 불가 시
     */
    @Override
    @Transactional
    public List<Long> createReservationsFromCart(List<Map<String, Object>> cartItems, Long userId, String specialRequests) throws SQLException {
        if (cartItems == null || cartItems.isEmpty()) {
            throw new SQLException("장바구니가 비어있습니다.");
        }

        List<Long> reservationIds = new ArrayList<>();
        for (Map<String, Object> item : cartItems) {
            Reservation reservation = new Reservation();
            reservation.setUserId(userId);
            reservation.setRoomId((Long) item.get("roomId"));
            reservation.setCheckInDate(LocalDate.parse(item.get("checkInDate").toString()));
            reservation.setCheckOutDate(LocalDate.parse(item.get("checkOutDate").toString()));
            reservation.setGuestCount((Integer) item.get("guestCount"));
            reservation.setTotalPrice(new BigDecimal(item.get("price").toString()));
            reservation.setStatus("PENDING");
            reservation.setPaymentStatus("UNPAID");
            reservation.setSpecialRequests(specialRequests);
            // createdAt 등은 createReservation 메서드 내부에서 설정됨

            Long reservationId = createReservation(reservation, userId); // 내부적으로 가용성 체크 및 업데이트 수행
            reservationIds.add(reservationId);
        }
        // 장바구니 비우기 로직은 CartService에서 호출하도록 변경 필요 (현재는 이 서비스에 없음)
        // cartService.clearCart(userId);
        return reservationIds;
    }

    /**
     * 예약 ID로 예약을 조회합니다. 예약자 본인 또는 숙소 호스트/관리자만 조회 가능합니다.
     * 리뷰 정보는 이 메서드에서 직접 반환하지 않습니다.
     * 필요한 경우, 클라이언트가 ApiReviewController를 통해 별도로 조회해야 합니다.
     * (예: GET /api/v1/reviews/reservation/{reservationId})
     *
     * @param reservationId 예약 ID
     * @param userId        현재 사용자 ID (권한 확인용)
     * @param userRole      현재 사용자 역할 (권한 확인용, 예: "USER", "HOST", "ADMIN")
     * @return 예약 정보를 담은 Map
     * @throws SQLException 데이터베이스 오류 또는 조회 권한 없음
     */
    @Override
    public Map<String, Object> getReservationDetail(Long reservationId, Long userId, String userRole) throws SQLException {
        Reservation reservation = reservationDao.getReservationById(reservationId);
        if (reservation == null) {
            throw new SQLException("예약 정보를 찾을 수 없습니다.");
        }

        // 권한 확인
        boolean isOwner = reservation.getUserId().equals(userId);
        boolean isHostOrAdmin = false;
        if ("HOST".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole)) {
            Room room = roomDao.getRoomById(reservation.getRoomId());
            if (room != null) {
                Accommodation accommodation = accommodationDao.getAccommodationById(room.getAccommodationId());
                if (accommodation != null && ("ADMIN".equalsIgnoreCase(userRole) || accommodation.getHostId().equals(userId))) {
                    isHostOrAdmin = true;
                }
            }
        }

        if (!isOwner && !isHostOrAdmin) {
            throw new SQLException("예약 정보를 조회할 권한이 없습니다.");
        }

        // Review review = reviewService.getReviewByReservationId(reservationId); // 리뷰 조회 로직 제거

        Map<String, Object> result = new HashMap<>();
        result.put("reservation", reservation);
        // result.put("review", review); // 리뷰 정보 반환 제거
        return result;
    }

    /**
     * 사용자 ID로 해당 사용자의 모든 예약 목록을 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 예약 목록
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    public List<Reservation> getReservationsByUserId(Long userId) throws SQLException {
        return reservationDao.getReservationsByUserId(userId);
    }

    /**
     * 호스트 ID로 해당 호스트가 관리하는 숙소의 모든 예약 목록을 조회합니다.
     * 관리자(ADMIN)도 조회 가능합니다.
     *
     * @param hostId   호스트 ID (현재 세션의 사용자 ID)
     * @param userRole 현재 사용자 역할 (HOST 또는 ADMIN)
     * @return 예약 목록
     * @throws SQLException 데이터베이스 오류 또는 권한 없음
     */
    @Override
    public List<Reservation> getReservationsByHostId(Long hostId, String userRole) throws SQLException {
        if (!"HOST".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new SQLException("호스트 예약 목록을 조회할 권한이 없습니다.");
        }
        // ADMIN인 경우 모든 호스트의 예약을 볼 수 있게 하려면 hostId 파라미터를 다르게 사용하거나, DAO에서 분기 처리 필요.
        // 현재는 hostId가 관리하는 숙소의 예약만 조회.
        return reservationDao.getReservationsByHostId(hostId);
    }

    @Override
    public List<Reservation> getReservationsByAccommodationId(Long accommodationId) throws SQLException {
        return reservationDao.getReservationsByAccommodationId(accommodationId);
    }

    @Override
    public List<Reservation> getReservationsByRoomId(Long roomId) throws SQLException {
        return reservationDao.getReservationsByRoomId(roomId);
    }

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
    @Override
    @Transactional
    public int updateReservationStatus(Long reservationId, String status, Long updaterId, String userRole) throws SQLException {
        Reservation reservation = reservationDao.getReservationById(reservationId);
        if (reservation == null) {
            throw new SQLException("예약 정보를 찾을 수 없습니다.");
        }

        if (!"HOST".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new SQLException("예약 상태를 업데이트할 권한이 없습니다.");
        }

        if ("HOST".equalsIgnoreCase(userRole)) {
            Room room = roomDao.getRoomById(reservation.getRoomId());
            if (room == null) throw new SQLException("객실 정보를 찾을 수 없습니다.");
            Accommodation accommodation = accommodationDao.getAccommodationById(room.getAccommodationId());
            if (accommodation == null || !accommodation.getHostId().equals(updaterId)) {
                throw new SQLException("해당 숙소의 예약 상태를 업데이트할 권한이 없습니다.");
            }
        }
        // ADMIN은 모든 예약 상태 변경 가능 (추가 검증 불필요)

        reservation.setStatus(status);
        reservation.setUpdatedAt(LocalDateTime.now());
        return reservationDao.updateReservationStatus(reservationId, status);
    }

    /**
     * 결제 상태를 업데이트합니다. 예약자 본인만 가능합니다.
     *
     * @param reservationId 예약 ID
     * @param paymentStatus 새로운 결제 상태
     * @param userId        현재 사용자 ID (예약자 ID)
     * @return 업데이트된 행 수 (성공 시 1)
     * @throws SQLException 데이터베이스 오류 또는 권한 없음
     */
    @Override
    @Transactional
    public int updatePaymentStatus(Long reservationId, String paymentStatus, Long userId) throws SQLException {
        Reservation reservation = reservationDao.getReservationById(reservationId);
        if (reservation == null) {
            throw new SQLException("예약 정보를 찾을 수 없습니다.");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new SQLException("결제 상태를 업데이트할 권한이 없습니다.");
        }
        reservation.setPaymentStatus(paymentStatus);
        reservation.setUpdatedAt(LocalDateTime.now());
        return reservationDao.updatePaymentStatus(reservationId, paymentStatus);
    }

    /**
     * 예약을 취소합니다. 예약자 본인만 가능합니다.
     * 예약 취소 시 객실 가용성을 복원합니다.
     *
     * @param reservationId 예약 ID
     * @param userId        현재 사용자 ID (예약자 ID)
     * @return 업데이트된 행 수 (성공 시 1)
     * @throws SQLException 데이터베이스 오류 또는 권한 없음
     */
    @Override
    @Transactional
    public int cancelReservation(Long reservationId, Long userId) throws SQLException {
        Reservation reservation = reservationDao.getReservationById(reservationId);
        if (reservation == null) {
            throw new SQLException("예약 정보를 찾을 수 없습니다.");
        }
        if (!reservation.getUserId().equals(userId)) {
            throw new SQLException("예약을 취소할 권한이 없습니다.");
        }
        if ("CANCELLED".equals(reservation.getStatus())) {
            return 0; // 이미 취소된 경우
        }

        // 객실 가용성 복원
        updateRoomAvailabilityForBooking(reservation.getRoomId(), reservation.getCheckInDate(), reservation.getCheckOutDate(), reservation.getGuestCount(), false);

        reservation.setStatus("CANCELLED");
        reservation.setUpdatedAt(LocalDateTime.now());
        // 필요시 환불 로직 등 추가
        return reservationDao.cancelReservation(reservationId); // DAO에서는 status를 CANCELLED로, updatedAt을 NOW()로 설정
    }

    /**
     * 예약을 삭제합니다.
     * 현재 구현에서는 'CANCELLED' 상태인 예약만 삭제 가능하다고 가정합니다.
     *
     * @param reservationId 삭제할 예약 ID
     * @return 삭제된 행 수
     * @throws SQLException 데이터베이스 오류 또는 삭제 조건 불충족
     */
    @Override
    @Transactional
    public int deleteReservation(Long reservationId) throws SQLException {
        Reservation reservation = reservationDao.getReservationById(reservationId);
        if (reservation == null) {
            throw new SQLException("예약 정보를 찾을 수 없습니다.");
        }
        // 비즈니스 규칙: 취소된 예약만 삭제 가능하도록 설정 (예시)
        if (!"CANCELLED".equals(reservation.getStatus())) {
            throw new SQLException("취소된 예약만 삭제할 수 있습니다.");
        }
        return reservationDao.deleteReservation(reservationId);
    }

    @Override
    public List<Reservation> getFilteredReservations(Map<String, Object> filters) throws SQLException {
        return reservationDao.getFilteredReservations(filters);
    }

    /**
     * 특정 기간 동안 객실의 가용성을 확인합니다.
     *
     * @param roomId       객실 ID
     * @param checkInDate  체크인 날짜
     * @param checkOutDate 체크아웃 날짜 (예약일 마지막 날이므로 실제 가용성 체크는 checkOutDate - 1일까지)
     * @param guestCount   투숙객 수
     * @return 가용 여부 (true: 예약 가능, false: 예약 불가능)
     * @throws SQLException 데이터베이스 오류 또는 객실 정보 없음
     */
    @Override
    public boolean isRoomAvailable(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount) throws SQLException {
        Room room = roomDao.getRoomById(roomId);
        if (room == null) {
            throw new SQLException("객실 정보를 찾을 수 없습니다. ID: " + roomId);
        }
        // 객실 자체의 상태 (예: 'ACTIVE', 'AVAILABLE' 등) 확인
        // Room 모델 또는 테이블에 status 컬럼이 있고, 그 값이 예약 가능한 상태인지 확인하는 로직 추가 가능
        // 예: if (!"ACTIVE".equals(room.getStatus())) return false;
        
        // 요청된 guestCount가 객실의 수용 가능 인원(capacity)을 초과하는지 확인
        if (guestCount > room.getCapacity()) { 
            // log.warn("Requested guestCount {} exceeds room capacity {} for room ID {}", guestCount, room.getCapacity(), roomId);
            return false; // 수용 인원 초과
        }

        LocalDate currentDate = checkInDate;
        // 체크아웃 날짜는 숙박하지 않으므로, 그 전날까지만 가용성을 확인
        while (!currentDate.isAfter(checkOutDate.minusDays(1))) {
            RoomAvailability availability = roomAvailabilityDao.getAvailabilityByRoomIdAndDate(roomId, currentDate);
            
            int availableCountForDate;
            if (availability != null) {
                availableCountForDate = availability.getAvailableCount();
            } else {
                // room_availability에 해당 날짜의 레코드가 없으면, room 테이블의 room_count를 기본 재고로 사용
                availableCountForDate = (room.getRoomCount() != null && room.getRoomCount() > 0) ? room.getRoomCount() : 0;
            }

            // 실제 필요한 객실 수 (여기서는 1개 객실을 예약한다고 가정)
            int requiredRoomUnits = 1; 
            if (availableCountForDate < requiredRoomUnits) { 
                // log.info("Room ID {} is not available on {}. Available: {}, Required: {}", roomId, currentDate, availableCountForDate, requiredRoomUnits);
                return false; // 특정 날짜에 필요한 만큼 객실이 없는 경우
            }
            currentDate = currentDate.plusDays(1);
        }
        // log.info("Room ID {} is available from {} to {} for {} guests.", roomId, checkInDate, checkOutDate, guestCount);
        return true;
    }

    @Override
    public List<RoomAvailability> getRoomAvailabilities(Long roomId, LocalDate startDate, LocalDate endDate) throws SQLException {
        return roomAvailabilityDao.getAvailabilitiesByRoomIdAndDateRange(roomId, startDate, endDate);
    }

    /**
     * 객실 가용성 정보를 업데이트합니다. (주로 관리자가 수동으로 조정 시 사용)
     * 가격만 업데이트하거나, 가용 객실 수만 업데이트하거나, 둘 다 업데이트하는 경우를 모두 처리합니다.
     *
     * @param roomId         객실 ID
     * @param date           날짜
     * @param availableCount 가용 객실 수 (null인 경우 가격만 업데이트)
     * @param price          특별 가격 (null인 경우 기본 가격 적용 또는 가용 객실 수만 업데이트)
     * @return 업데이트된 행 수
     * @throws SQLException 데이터베이스 오류 발생 시
     */
    @Override
    @Transactional
    public int updateRoomAvailability(Long roomId, LocalDate date, Integer availableCount, Double price) throws SQLException {
        RoomAvailability availability = roomAvailabilityDao.getAvailabilityByRoomIdAndDate(roomId, date);

        if (availability != null) {
            if (availableCount != null) {
                availability.setAvailableCount(availableCount);
            }
            if (price != null) {
                availability.setPrice(BigDecimal.valueOf(price));
            } else {
                // 명시적으로 price가 null로 들어오면, 기존 room의 가격을 사용하거나 null로 설정 (정책에 따라 다름)
                // 여기서는 기존 availability의 가격을 유지하거나, Room의 기본 가격을 조회해서 설정할 수 있음.
                // 만약 availableCount만 업데이트하고 price는 건드리지 않으려면, 이 부분을 조건 처리해야 함.
                // 현재는 price가 null이면 업데이트 하지 않도록 로직 구성 (updateAvailability 메서드가 null을 무시하도록)
                // 하지만, 명시적으로 null로 셋팅하려면 availability.setPrice(null); 필요
            }
            return roomAvailabilityDao.updateAvailability(availability);
        } else {
            // 해당 날짜에 가용성 정보가 없으면 새로 생성
            Room room = roomDao.getRoomById(roomId);
            if (room == null) throw new SQLException("객실 정보를 찾을 수 없습니다.");

            RoomAvailability newAvailability = RoomAvailability.builder()
                    .roomId(roomId)
                    .date(date)
                    .availableCount(availableCount != null ? availableCount : room.getRoomCount()) // null이면 기본 객실 수
                    .price(price != null ? BigDecimal.valueOf(price) : room.getPrice()) // null이면 기본 객실 가격
                    .build();
            roomAvailabilityDao.insert(newAvailability);
            return 1;
        }
    }

    /**
     * 예약 또는 취소 시 객실 가용성 정보를 업데이트하는 내부 헬퍼 메서드.
     *
     * @param roomId       객실 ID
     * @param checkInDate  체크인 날짜
     * @param checkOutDate 체크아웃 날짜 (이 날짜는 가용성 계산에 포함되지 않음)
     * @param guestCount   투숙객 수 (실제로는 객실 1개를 점유하는 것으로 가정)
     * @param decrease     true이면 가용성 감소(예약), false이면 가용성 증가(취소)
     * @throws SQLException 데이터베이스 오류
     */
    private void updateRoomAvailabilityForBooking(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount, boolean decrease) throws SQLException {
        Room room = roomDao.getRoomById(roomId);
        if (room == null) {
            throw new SQLException("객실 정보를 찾을 수 없습니다. ID: " + roomId);
        }
        if (room.getRoomCount() == null || room.getRoomCount() <= 0) {
            // log.warn("Room ID {} has invalid roomCount: {}. Skipping availability update.", roomId, room.getRoomCount());
            // return; // roomCount가 유효하지 않으면 업데이트를 건너뛸 수 있음
            throw new SQLException("객실의 총 수(roomCount) 정보가 유효하지 않습니다. Room ID: " + roomId);
        }

        LocalDate currentDate = checkInDate;
        int actualRoomUnitsChanged = 1; // 한 번의 예약/취소는 객실 1개에 대한 변경으로 가정

        while (!currentDate.isAfter(checkOutDate.minusDays(1))) { // 체크아웃 전날까지만 처리
            RoomAvailability availability = roomAvailabilityDao.getAvailabilityByRoomIdAndDate(roomId, currentDate);
            
            int newAvailableCount;
            if (availability != null) {
                int currentAvailable = availability.getAvailableCount();
                newAvailableCount = decrease ? currentAvailable - actualRoomUnitsChanged : currentAvailable + actualRoomUnitsChanged;
            } else {
                // 해당 날짜에 가용성 정보가 없는 경우, room.getRoomCount()를 기준으로 계산
                int initialTotalCount = room.getRoomCount();
                newAvailableCount = decrease ? initialTotalCount - actualRoomUnitsChanged : initialTotalCount + actualRoomUnitsChanged; 
                // (참고: 취소 시 availability가 없는 경우는 거의 없으나, 로직상 방어 코드로 남겨둠. 예약 시에는 발생 가능)
            }

            // 음수가 되지 않도록, 또한 최대 객실 수를 넘지 않도록 처리
            if (newAvailableCount < 0) {
                // log.warn("Attempted to set available count to negative for room {} on {}. Setting to 0.", roomId, currentDate);
                newAvailableCount = 0;
            }
            if (!decrease && newAvailableCount > room.getRoomCount()) { // 취소 시 최대 객실 수 초과 방지
                // log.warn("Attempted to increase available count beyond total room count for room {} on {}. Setting to total.", roomId, currentDate);
                newAvailableCount = room.getRoomCount();
            }

            // roomAvailabilityDao.updateAvailableCount는 ON DUPLICATE KEY UPDATE를 사용하므로 insert/update를 한번에 처리 가능
            // 단, price 정보가 필요할 수 있음. updateAvailableCount가 price도 함께 처리하는지 확인 필요.
            // 현재 roomAvailability.xml의 updateAvailableCount는 price도 설정함.
            // (SELECT price FROM rooms WHERE room_id = #{param1}) 부분을 사용. 만약 특별 가격을 유지해야 한다면 이 부분 수정 필요.
            // 여기서는 해당 날짜의 가격은 rooms 테이블의 기본 가격을 따른다고 가정.
            roomAvailabilityDao.updateAvailableCount(roomId, currentDate, newAvailableCount);
            
            // log.info("Updated availability for Room ID {}, Date {}: New Available Count = {}", roomId, currentDate, newAvailableCount);
            currentDate = currentDate.plusDays(1);
        }
    }

    @Override
    public List<Room> getAvailableRooms(Long accommodationId, LocalDate checkInDate, LocalDate checkOutDate, int guestCount) throws SQLException {
        // 1. RoomDao를 통해 기본적인 조건(숙소 ID, 날짜 범위 - Dao에서 처리 가능하면 최선)으로 객실 목록 조회
        //    여기서는 RoomDao에 getAvailableRoomsByDateRange와 같은 메서드가 있다고 가정합니다.
        //    또는 accommodationId로 모든 room을 가져온 후 아래에서 필터링합니다.
        List<Room> rooms = roomDao.getRoomsByAccommodationId(accommodationId); // 예시: 숙소의 모든 객실 일단 로드

        // 2. 각 객실에 대해 guestCount 및 isRoomAvailable 조건을 만족하는지 필터링
        return rooms.stream()
                .filter(room -> room.getCapacity() >= guestCount)
                .filter(room -> {
                    try {
                        // isRoomAvailable은 해당 객실이 주어진 기간과 인원수에 대해 예약 가능한지 확인
                        return isRoomAvailable(room.getRoomId(), checkInDate, checkOutDate, 1); // 1개 객실 기준 가용성 체크
                    } catch (SQLException e) {
                        // 예외 발생 시 해당 객실은 사용 불가능한 것으로 간주
                        // 실제 운영환경에서는 로깅 필요
                        System.err.println("Error checking availability for room " + room.getRoomId() + ": " + e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public int calculateMinAvailableCountForRoom(Room room, LocalDate startDate, LocalDate endDate, Integer guests) throws SQLException {
        System.out.println("[ReservationService] calculateMinAvailableCountForRoom called - " +
                "roomId: " + (room != null ? room.getRoomId() : "null") +
                ", startDate: " + startDate +
                ", endDate: " + endDate +
                ", guests: " + guests);
                
        if (room == null || room.getRoomId() == null || room.getRoomCount() == null || room.getRoomCount() <= 0) {
            System.out.println("[ReservationService] Invalid room object provided for calculating min available count: " + room);
            return 0; // 유효하지 않은 room 정보 또는 roomCount가 없으면 0 반환
        }

        // 추가된 로직: 요청 인원(guests)이 객실 수용 인원(capacity)을 초과하는지 확인
        if (guests != null && guests > 0 && room.getCapacity() != null && guests > room.getCapacity()) {
            System.out.println("[ReservationService] Requested guests (" + guests + ") exceed room capacity (" + 
                    room.getCapacity() + ") for room ID " + room.getRoomId());
            return 0; // 요청 인원이 수용 인원을 초과하면 0 반환
        }

        if (startDate == null || endDate == null || startDate.isAfter(endDate) || startDate.isEqual(endDate)) {
            System.out.println("[ReservationService] Invalid date range for room " + room.getRoomId() + 
                    ": startDate=" + startDate + ", endDate=" + endDate);
            // 날짜 범위가 유효하지 않으면, 인원수 조건만 만족했다면 일단 현재 객실의 총 개수를 반환하거나,
            // 혹은 이 경우에도 0을 반환하는 것이 더 안전할 수 있습니다.
            // 여기서는 좀 더 보수적으로 0을 반환하거나, 아니면 최소한 인원 체크는 통과했으므로 room.getRoomCount()를 반환합니다.
            // 이전 로직은 room.getRoomCount()를 반환했으나, guests 체크 후이므로, 여기서 0을 반환하는 것이 더 일관적일 수 있습니다.
            // 하지만, AccommodationServiceImpl에서 이 메서드의 반환값을 "예약 가능한 최소 객실 수"로 사용하므로,
            // 날짜 정보가 없으면 "판단 불가"의 의미로 -1을 반환하는 것도 고려해볼 수 있습니다.
            // 우선은, -1로 설정합니다.
            return -1; 
        }

        int minAvailableAcrossDates = room.getRoomCount(); // 초기 최소값은 해당 객실의 총 개수로 설정
        LocalDate currentDate = startDate;
        System.out.println("[ReservationService] Starting calculation for room " + room.getRoomId() + 
                ", initial minAvailableAcrossDates=" + minAvailableAcrossDates);

        while (!currentDate.isAfter(endDate.minusDays(1))) { // endDate는 포함하지 않음
            // 1. room_availability에서 해당 날짜의 가용 객실 수 조회
            RoomAvailability dailyAvailability = roomAvailabilityDao.getAvailabilityByRoomIdAndDate(room.getRoomId(), currentDate);
            int availableFromAvailabilityTable = (dailyAvailability != null) ? dailyAvailability.getAvailableCount() : room.getRoomCount();
            System.out.println("[ReservationService] Date: " + currentDate + ", availableFromAvailabilityTable=" + availableFromAvailabilityTable);

            // 2. reservations 테이블에서 해당 날짜에 확정된 예약 건수 조회
            // ReservationDao에 해당 날짜에 특정 room_id로 확정된 예약 수를 세는 메소드가 필요.
            // getConfirmedReservationsCountForRoomOnDate(Long roomId, LocalDate date)
            // 여기서는 임시로 reservationDao.getFilteredReservations를 활용하거나, 새 DAO 메소드를 가정합니다.
            // 아래는 개념적인 접근입니다. 실제 DAO 메소드가 필요합니다.
            Map<String, Object> filters = new HashMap<>();
            filters.put("roomId", room.getRoomId());
            filters.put("status", "CONFIRMED");
            filters.put("targetDate", currentDate); // targetDate를 기준으로 check_in_date <= targetDate AND check_out_date > targetDate 인 예약을 카운트
            
            // 아래 Dao 메소드는 새로 만들어야 합니다.
            // int confirmedReservations = reservationDao.countConfirmedReservationsForRoomOnDate(room.getRoomId(), currentDate);
            // 임시 구현: 현재 ReservationDao에는 특정 날짜의 예약 건수를 직접 가져오는 메소드가 없습니다.
            // 여기서는 ReservationServiceImpl의 isRoomAvailable과 유사하게, room_availability의 available_count가
            // 이미 예약을 반영한 수치라고 가정하고 진행합니다. (이전 단계에서 그렇게 수정했으므로)
            // 따라서, availableFromAvailabilityTable이 그날의 실제 남은 방이라고 가정합니다.
            int dailyNetAvailable = availableFromAvailabilityTable;
            System.out.println("[ReservationService] Date: " + currentDate + ", dailyNetAvailable=" + dailyNetAvailable);

            if (dailyNetAvailable < minAvailableAcrossDates) {
                minAvailableAcrossDates = dailyNetAvailable;
                System.out.println("[ReservationService] Updated minAvailableAcrossDates to " + minAvailableAcrossDates);
            }

            if (minAvailableAcrossDates <= 0) {
                System.out.println("[ReservationService] minAvailableAcrossDates <= 0, returning 0");
                return 0; // 중간에 0개 이하로 내려가면 더 계산할 필요 없음
            }
            currentDate = currentDate.plusDays(1);
        }
        System.out.println("[ReservationService] Final minAvailableAcrossDates=" + minAvailableAcrossDates + " for room " + room.getRoomId());
        return minAvailableAcrossDates;
    }

    @Override
    public Page<Reservation> getReservationsByHostIdWithFiltersAndPaging(Long hostId, String status, String checkInDateStr, String guestName, String sortBy, Pageable pageable) throws SQLException {
        // 정렬 처리 (sortBy 파라미터 기반)
        // 예: "createdAtDesc" -> Sort.by(Sort.Direction.DESC, "createdAt")
        //      "checkInDateAsc" -> Sort.by(Sort.Direction.ASC, "checkInDate")
        // Pageable 객체에 이미 sort 정보가 포함되어 올 수 있으므로, 서비스 또는 DAO 레벨에서 sortBy 문자열을 해석하여 Sort 객체를 만들고
        // 이를 PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort) 형태로 조합하여 DAO에 전달할 수 있습니다.
        // 또는 DAO에서 sortBy 문자열을 직접 사용하여 동적 쿼리를 생성할 수도 있습니다.

        // MyBatis Mapper에 전달할 파라미터맵 생성
        Map<String, Object> params = new HashMap<>();
        params.put("hostId", hostId);
        if (status != null && !status.isEmpty()) {
            params.put("status", status);
        }
        if (checkInDateStr != null && !checkInDateStr.isEmpty()) {
            try {
                // 날짜 형식 검증 및 변환 (필요시)
                LocalDate checkInDate = LocalDate.parse(checkInDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                params.put("checkInDate", checkInDate);
            } catch (DateTimeParseException e) {
                // 날짜 형식이 잘못된 경우 로그를 남기거나 예외 처리
                log.warn("Invalid checkInDate format: {}", checkInDateStr);
                // 또는 params.put("checkInDate", null); 또는 예외 발생
            }
        }
        if (guestName != null && !guestName.trim().isEmpty()) {
            params.put("guestName", guestName.trim());
        }
        params.put("sortBy", sortBy); // DAO에서 이 값을 사용하여 ORDER BY 절 구성
        params.put("offset", pageable.getOffset());
        params.put("limit", pageable.getPageSize());

        // DAO 호출
        List<Reservation> reservations = reservationDao.findReservationsByHostWithFiltersAndPaging(params);
        long total = reservationDao.countReservationsByHostWithFilters(params);

        return new PageImpl<>(reservations, pageable, total);
    }

    @Override
    @Transactional
    public Map<String, Object> prepareReservationsForPayment(PaymentPrepareRequestDto prepareRequestDto, Long userId) throws SQLException, InvalidRequestException, ResourceNotFoundException {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("사용자 정보를 찾을 수 없습니다. ID: " + userId);
        }

        List<Reservation> tempReservations = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        String merchantUid = generateUniqueMerchantUid();
        StringBuilder paymentNameBuilder = new StringBuilder();

        for (ReservationCreationDto reservationDto : prepareRequestDto.getReservationsToCreate()) {
            Room room = roomDao.getRoomById(reservationDto.getRoomId());
            if (room == null) {
                throw new ResourceNotFoundException("객실 정보를 찾을 수 없습니다. ID: " + reservationDto.getRoomId());
            }
            Accommodation accommodation = accommodationDao.getAccommodationById(room.getAccommodationId());
            if (accommodation == null) {
                throw new ResourceNotFoundException("숙소 정보를 찾을 수 없습니다. (객실 ID: " + room.getRoomId() + ")");
            }

            // ReservationCreationDto의 날짜 필드는 이미 LocalDate 타입이므로 파싱 불필요
            LocalDate checkInDate = reservationDto.getCheckInDate(); 
            LocalDate checkOutDate = reservationDto.getCheckOutDate();

            if (checkInDate == null || checkOutDate == null) {
                log.error("날짜 정보 누락: 체크인 또는 체크아웃 날짜가 null입니다. DTO: {}", reservationDto);
                throw new InvalidRequestException("체크인 또는 체크아웃 날짜 정보가 누락되었습니다.");
            }
            
            long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            if (nights <= 0) {
                throw new InvalidRequestException("체크아웃 날짜는 체크인 날짜 이후여야 합니다.");
            }
            BigDecimal currentReservationTotalPrice = reservationDto.getTotalPrice();
            totalAmount = totalAmount.add(currentReservationTotalPrice);

            if (paymentNameBuilder.length() > 0) {
                paymentNameBuilder.append(", ");
            }
            paymentNameBuilder.append(room.getName());

            List<LocalDate> dateRange = getDateRange(checkInDate, checkOutDate);
            for (LocalDate date : dateRange) {
                RoomAvailability availabilityToDecrease = new RoomAvailability();
                availabilityToDecrease.setRoomId(reservationDto.getRoomId());
                availabilityToDecrease.setDate(date);
                availabilityToDecrease.setAvailableCount(1);

                int updatedRows = roomAvailabilityDao.decreaseDailyAvailability(availabilityToDecrease);
                if (updatedRows == 0) {
                    RoomAvailability currentDbAvailability = roomAvailabilityDao.getAvailabilityByRoomIdAndDate(reservationDto.getRoomId(), date);
                    Integer currentStock = (currentDbAvailability != null) ? currentDbAvailability.getAvailableCount() : null;
                    log.warn("객실 ID {}의 {} 날짜 재고 확보 실패. DB 업데이트 row: {}. 현재 DB 재고: {}",
                            reservationDto.getRoomId(), date, updatedRows, currentStock);
                    throw new InvalidRequestException(
                        String.format("객실 ID %d의 %s 날짜 재고를 확보할 수 없습니다. 이미 예약되었거나 재고가 부족합니다.",
                                      reservationDto.getRoomId(), date.toString()));
                }
            }

            Reservation tempReservation = new Reservation();
            tempReservation.setUserId(userId);
            tempReservation.setRoomId(reservationDto.getRoomId());
            tempReservation.setAccommodationId(room.getAccommodationId());
            tempReservation.setCheckInDate(checkInDate);
            tempReservation.setCheckOutDate(checkOutDate);
            tempReservation.setGuestCount(reservationDto.getGuestCount());
            tempReservation.setTotalPrice(currentReservationTotalPrice);
            tempReservation.setPaymentStatus("PENDING_PREPARATION");
            tempReservation.setStatus("TEMP_RESERVED");
            tempReservation.setMerchantUid(merchantUid); 
            tempReservation.setSpecialRequests(prepareRequestDto.getSpecialRequests());
            tempReservation.setCreatedAt(LocalDateTime.now());
            tempReservation.setUpdatedAt(LocalDateTime.now());
            
            tempReservations.add(tempReservation);
        }

        String finalPaymentName = paymentNameBuilder.toString();
        if (finalPaymentName.length() > 50) { 
            finalPaymentName = finalPaymentName.substring(0, 47) + "...";
        }
        if (tempReservations.size() > 1) {
            finalPaymentName += " 외 " + (tempReservations.size() -1) + "건";
        }

        Map<String, Object> response = new HashMap<>();
        response.put("merchantUid", merchantUid);
        response.put("amount", totalAmount);
        response.put("paymentName", finalPaymentName);
        response.put("buyerEmail", user.getEmail());
        response.put("buyerName", user.getUsername());
        response.put("buyerTel", user.getPhone());

        log.info("결제 준비 완료: merchantUid={}, amount={}, name='{}', buyer={}, 예약 건수: {}",
             merchantUid, totalAmount, finalPaymentName, user.getEmail(), tempReservations.size());

        return response;
    }

    private List<LocalDate> getDateRange(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateReservationsAfterPayment(String merchantUid, String reservationStatus, String paymentStatus) throws SQLException, ResourceNotFoundException {
        Map<String, Object> params = new HashMap<>();
        params.put("merchantUid", merchantUid);
        params.put("reservationStatus", reservationStatus);
        params.put("paymentStatus", paymentStatus);
        int updatedRows = reservationDao.updateReservationsStatusByMerchantUid(params);
        if (updatedRows == 0) {
            log.warn("merchantUid '{}'에 해당하는 예약들의 상태 변경에 실패했거나, 변경할 예약이 없습니다.", merchantUid);
            throw new ResourceNotFoundException("merchantUid '" + merchantUid + "'에 해당하는 예약들을 찾거나 상태를 변경할 수 없습니다.");
        }
        log.info("merchantUid '{}' 예약 상태 변경 완료: reservationStatus={}, paymentStatus={}", merchantUid, reservationStatus, paymentStatus);
    }

    @Override
    @Transactional
    public void cancelReservationsByMerchantUid(String merchantUid, String reservationStatus, String paymentStatus) throws SQLException {
        List<Reservation> reservationsToCancel = reservationDao.getReservationsByMerchantUid(merchantUid);
        if (reservationsToCancel == null || reservationsToCancel.isEmpty()){
            log.warn("취소할 예약이 없습니다. merchantUid: {}", merchantUid);
            return; 
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("merchantUid", merchantUid);
        params.put("reservationStatus", reservationStatus);
        params.put("paymentStatus", paymentStatus);
        reservationDao.updateReservationsStatusByMerchantUid(params);
        log.info("merchantUid '{}'에 대한 예약 {}건 상태 변경 완료: {}, {}", merchantUid, reservationsToCancel.size(), reservationStatus, paymentStatus);

        for (Reservation reservation : reservationsToCancel) {
            List<LocalDate> dates = getDateRange(reservation.getCheckInDate(), reservation.getCheckOutDate());
            for (LocalDate date : dates) {
                RoomAvailability availabilityUpdate = RoomAvailability.builder()
                                                .roomId(reservation.getRoomId())
                                                .date(date)
                                                .availableCount(1)
                                                .build();
                int updatedRows = roomAvailabilityDao.increaseDailyAvailability(availabilityUpdate);
                if (updatedRows == 0) {
                    log.warn("객실 ID {}의 {} 날짜 재고 복구 중 예상치 못한 결과 발생 (updatedRows=0). DB에 해당 날짜의 재고 레코드가 없거나 동시성 문제일 수 있습니다.", reservation.getRoomId(), date);
                }
            }
        }
        log.info("merchantUid '{}'에 대한 객실 재고 복구 완료.", merchantUid);
    }

    @Override
    @Transactional(readOnly = true)
    public int countPendingReservationsByUserId(Long userId) throws SQLException {
        return reservationDao.countPendingReservationsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation getReservationByIdAndUserId(Long reservationId, Long userId) throws SQLException {
        return reservationDao.getReservationByIdAndUserId(reservationId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> getPendingReservationsByMerchantUid(String merchantUid) throws SQLException {
        return reservationDao.getPendingReservationsByMerchantUid(merchantUid);
    }

    // ... (기타 필요한 메소드들: e.g., 페이징 처리된 예약 목록 조회 등)

    // createReservation, updateReservation, deleteReservation 등 기존 메소드들은
    // 새로운 예약/결제 플로우와 어떻게 통합될지 또는 별도로 유지될지 검토 필요.
    // 예를 들어, createReservation은 이제 prepareReservationsForPayment를 통해 처리될 수 있음.
}
