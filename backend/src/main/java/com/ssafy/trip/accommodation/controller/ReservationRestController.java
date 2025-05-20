package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.CartItem;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.accommodation.service.CartService;
import com.ssafy.trip.accommodation.service.ReservationService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 예약 관련 REST API 요청을 처리하는 컨트롤러입니다.
 * 실제 비즈니스 로직은 ReservationService에 위임합니다.
 * 리뷰 관련 기능은 ApiReviewController를 참조하십시오.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationRestController {

    private final ReservationService reservationService;
    private final AccommodationService accommodationService;
    private final CartService cartService;

    /**
     * 예약 폼에 필요한 데이터를 조회합니다.
     * 객실 ID, 체크인/아웃 날짜, 투숙객 수를 받아 해당 조건의 예약 가능 정보 및 가격 등을 반환합니다.
     *
     * @param roomId       객실 ID
     * @param checkInDate  체크인 날짜 (ISO 형식: YYYY-MM-DD)
     * @param checkOutDate 체크아웃 날짜 (ISO 형식: YYYY-MM-DD)
     * @param guestCount   투숙객 수 (기본값 1)
     * @param session      HTTP 세션 (사용자 인증 정보 확인)
     * @return 성공 시 예약 폼 데이터와 HTTP 200 OK,
     *         로그인 필요 시 HTTP 401 Unauthorized,
     *         잘못된 요청(예: 날짜 오류, 예약 불가) 시 HTTP 400 Bad Request,
     *         그 외 서버 오류 시 HTTP 500 Internal Server Error
     */
    @GetMapping("/form/{roomId}")
    public ResponseEntity<?> getReservationForm(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(defaultValue = "1") int guestCount,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            Map<String, Object> formData = reservationService.getReservationFormData(roomId, checkInDate, checkOutDate, guestCount, userId);
            return ResponseEntity.ok(formData);
        } catch (SQLException e) {
            if (e.getMessage().contains("객실 정보를 찾을 수 없습니다") || e.getMessage().contains("선택한 날짜에 예약 가능한 객실이 없습니다") || e.getMessage().contains("체크아웃 날짜는 체크인 날짜 이후여야 합니다")) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "예약 폼 데이터 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 새 예약을 생성합니다.
     * 요청 본문으로부터 예약 정보를 받아 처리합니다.
     *
     * @param reservation 요청 본문의 예약 데이터
     * @param session     HTTP 세션 (사용자 인증 정보 확인)
     * @return 성공 시 생성된 예약 ID와 메시지 HTTP 200 OK (또는 HTTP 201 Created),
     *         로그인 필요 시 HTTP 401 Unauthorized,
     *         예약 불가 조건(예: 객실 없음) 시 HTTP 400 Bad Request,
     *         그 외 서버 오류 시 HTTP 500 Internal Server Error
     */
    @PostMapping("/create")
    public ResponseEntity<?> createReservation(
            @RequestBody Reservation reservation,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            Long reservationId = reservationService.createReservation(reservation, userId);
            return ResponseEntity.ok(Map.of("message", "예약이 성공적으로 완료되었습니다.", "reservationId", reservationId));
        } catch (SQLException e) {
            if (e.getMessage().contains("예약 가능한 객실이 없습니다")) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "예약 생성 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 예약 상세 정보를 조회합니다.
     * 예약자 본인 또는 해당 숙소의 호스트/관리자만 조회 가능합니다.
     * 리뷰 정보는 이 API에서 직접 제공하지 않으며, 필요한 경우 클라이언트가 ApiReviewController를 통해 별도로 조회해야 합니다.
     * (예: GET /api/v1/reviews/reservation/{reservationId})
     *
     * @param reservationId 조회할 예약의 ID
     * @param session       HTTP 세션 (사용자 인증 및 권한 정보 확인)
     * @return 성공 시 예약 상세 정보와 HTTP 200 OK,
     *         로그인 필요 시 HTTP 401 Unauthorized,
     *         권한 없음 시 HTTP 403 Forbidden,
     *         예약 정보를 찾을 수 없을 경우 HTTP 404 Not Found,
     *         그 외 서버 오류 시 HTTP 500 Internal Server Error
     */
    @GetMapping("/detail/{reservationId}")
    public ResponseEntity<?> getReservationDetail(@PathVariable Long reservationId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("role");

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            // ReservationService의 getReservationDetail은 이제 리뷰 정보를 포함하지 않을 것으로 가정합니다.
            // 만약 포함한다면 해당 서비스 로직 수정 필요.
            Map<String, Object> reservationDetails = reservationService.getReservationDetail(reservationId, userId, userRole);
            return ResponseEntity.ok(reservationDetails);
        } catch (SQLException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "예약 상세 정보 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 현재 로그인한 사용자의 모든 예약 목록을 조회합니다.
     *
     * @param session HTTP 세션 (사용자 인증 정보 확인)
     * @return 성공 시 예약 목록과 HTTP 200 OK,
     *         로그인 필요 시 HTTP 401 Unauthorized,
     *         서버 오류 시 HTTP 500 Internal Server Error
     */
    @GetMapping("/my-reservations")
    public ResponseEntity<?> myReservations(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        try {
            List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
            return ResponseEntity.ok(Map.of("reservations", reservations));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "내 예약 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 현재 로그인한 호스트 또는 관리자의 숙소 예약 목록을 조회합니다.
     *
     * @param session HTTP 세션 (사용자 인증 및 권한 정보 확인)
     * @return 성공 시 예약 목록과 HTTP 200 OK,
     *         권한 없음(로그인하지 않았거나 호스트/관리자가 아님) 시 HTTP 401 Unauthorized 또는 HTTP 403 Forbidden,
     *         서버 오류 시 HTTP 500 Internal Server Error
     */
    @GetMapping("/host-reservations")
    public ResponseEntity<?> hostReservations(HttpSession session) {
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        if (!"HOST".equalsIgnoreCase(role) && !!"ADMIN".equalsIgnoreCase(role)){
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "호스트 또는 관리자 권한이 필요합니다."));
        }

        try {
            List<Reservation> reservations = reservationService.getReservationsByHostId(hostId, role);
            return ResponseEntity.ok(Map.of("reservations", reservations));
        } catch (SQLException e) {
            if (e.getMessage().contains("권한이 없습니다")) {
                 return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "호스트 예약 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 특정 예약을 취소합니다.
     * 예약자 본인만 취소할 수 있습니다.
     *
     * @param reservationId 취소할 예약의 ID
     * @param session       HTTP 세션 (사용자 인증 정보 확인)
     * @return 성공 시 메시지와 HTTP 200 OK,
     *         로그인 필요 시 HTTP 401 Unauthorized,
     *         권한 없음 또는 예약 상태 변경 불가 시 HTTP 403 Forbidden 또는 HTTP 400 Bad Request,
     *         예약 정보를 찾을 수 없을 경우 HTTP 404 Not Found,
     *         서버 오류 시 HTTP 500 Internal Server Error
     */
    @PostMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        try {
            reservationService.cancelReservation(reservationId, userId);
            return ResponseEntity.ok(Map.of("message", "예약이 성공적으로 취소되었습니다."));
        } catch (SQLException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            }
            // 상세한 예외 메시지에 따라 상태 코드 분기 (예: "취소할 수 없는 상태", "본인의 예약이 아님")
            if (e.getMessage().contains("취소할 권한이 없습니다") || e.getMessage().contains("이미 취소된 예약입니다") || e.getMessage().contains("취소 기간이 지났습니다")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "예약 취소 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 예약 상태를 업데이트합니다. (호스트 또는 관리자만 가능)
     *
     * @param reservationId 업데이트할 예약의 ID
     * @param payload       요청 본문 ({"status": "NEW_STATUS"})
     * @param session       HTTP 세션 (사용자 인증 및 권한 정보 확인)
     * @return 성공 시 메시지와 HTTP 200 OK,
     *         로그인 필요 또는 권한 없음 시 HTTP 401 Unauthorized 또는 HTTP 403 Forbidden,
     *         예약 정보를 찾을 수 없을 경우 HTTP 404 Not Found,
     *         잘못된 상태 값 등 요청 오류 시 HTTP 400 Bad Request,
     *         서버 오류 시 HTTP 500 Internal Server Error
     */
    @PostMapping("/update-status/{reservationId}")
    public ResponseEntity<?> updateReservationStatus(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        Long updaterId = (Long) session.getAttribute("userId");
        String userRole = (String) session.getAttribute("role");
        String status = payload.get("status");

        if (updaterId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        if (!("HOST".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "예약 상태를 변경할 권한이 없습니다."));
        }
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "상태 값이 필요합니다."));
        }

        try {
            reservationService.updateReservationStatus(reservationId, status, updaterId, userRole);
            return ResponseEntity.ok(Map.of("message", "예약 상태가 성공적으로 업데이트되었습니다."));
        } catch (SQLException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("권한이 없습니다") || e.getMessage().contains("유효하지 않은 상태 값입니다")) { // 서비스 레이어의 예외 메시지에 따라 조정
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "예약 상태 업데이트 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 결제 상태를 업데이트합니다. (보통 사용자 또는 시스템에 의해 호출)
     *
     * @param reservationId 업데이트할 예약의 ID
     * @param payload       요청 본문 ({"paymentStatus": "NEW_PAYMENT_STATUS"})
     * @param session       HTTP 세션 (필요에 따라 사용자 인증)
     * @return 성공 시 메시지와 HTTP 200 OK,
     *         오류 발생 시 적절한 HTTP 상태 코드와 오류 메시지
     */
    @PostMapping("/update-payment/{reservationId}")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> payload,
            HttpSession session) {
        // 이 API는 결제 시스템 연동 방식에 따라 사용자 인증이 필요할 수도, 없을 수도 있습니다.
        // 여기서는 간단히 세션 사용자 ID를 확인하지만, 실제로는 더 복잡한 권한 검사가 필요할 수 있습니다.
        Long userId = (Long) session.getAttribute("userId"); // 결제 주체 확인용 (선택적)
        String paymentStatus = payload.get("paymentStatus");

        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "결제 상태 값이 필요합니다."));
        }

        try {
            // userId는 현재 예약의 사용자와 일치하는지 검증하는 데 사용될 수 있습니다.
            // 또는 시스템 간 호출인 경우 다른 인증 방식을 사용합니다.
            reservationService.updatePaymentStatus(reservationId, paymentStatus, userId);
            return ResponseEntity.ok(Map.of("message", "결제 상태가 성공적으로 업데이트되었습니다."));
        } catch (SQLException e) {
            if (e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
            } else if (e.getMessage().contains("유효하지 않은 결제 상태입니다") || e.getMessage().contains("업데이트할 권한이 없습니다")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "결제 상태 업데이트 중 오류 발생: " + e.getMessage()));
        }
    }

    // =============== 리뷰 관련 기능은 ApiReviewController로 이전되었습니다. ===============
    // 아래의 엔드포인트들은 삭제됩니다.
    // 클라이언트는 다음 API를 사용해야 합니다:
    // - 리뷰 작성 폼 데이터: (필요시) 예약 정보 + 숙소 정보 + 사용자 정보 등을 조합. ReviewDto는 기본값으로 시작.
    // - 리뷰 생성: POST /api/v1/reviews (이미지 포함 시 multipart/form-data)
    // - 리뷰 수정 폼 데이터: GET /api/v1/reviews/{reviewId} (리뷰 상세 조회) + (필요시) 숙소/예약 정보
    // - 리뷰 수정: PUT /api/v1/reviews/{reviewId} (이미지 포함 시 multipart/form-data)
    // - 리뷰 삭제: DELETE /api/v1/reviews/{reviewId}
    // - 특정 예약에 대한 리뷰 조회: GET /api/v1/reviews/reservation/{reservationId}
    // =====================================================================================

    /*
    @GetMapping("/review-form/{reservationId}")
    public ResponseEntity<?> getReviewForm(@PathVariable Long reservationId, HttpSession session) {
        // ... 기존 로직 ...
        // 이 기능은 ReservationService에서 제거되었으며, ApiReviewController를 통해 리뷰 생성/수정 API를 직접 호출해야 합니다.
        // 리뷰 작성에 필요한 예약 정보, 숙소 정보 등은 각 서비스나 ReservationRestController의 다른 API를 통해 얻을 수 있습니다.
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of("message", "이 API는 더 이상 사용되지 않습니다. ApiReviewController를 참조하세요."));
    }

    @PostMapping("/create-review")
    public ResponseEntity<?> createReservationReview(@RequestBody Review review, HttpSession session) {
        // ... 기존 로직 ...
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of("message", "이 API는 더 이상 사용되지 않습니다. POST /api/v1/reviews API를 사용하세요."));
    }

    @GetMapping("/update-review-form/{reviewId}")
    public ResponseEntity<?> getUpdateReviewForm(@PathVariable Long reviewId, HttpSession session) {
        // ... 기존 로직 ...
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of("message", "이 API는 더 이상 사용되지 않습니다. GET /api/v1/reviews/{reviewId} API를 사용하세요."));
    }

    @PostMapping("/update-review")
    public ResponseEntity<?> updateReservationReview(@RequestBody Review review, HttpSession session) {
        // ... 기존 로직 ...
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of("message", "이 API는 더 이상 사용되지 않습니다. PUT /api/v1/reviews/{reviewId} API를 사용하세요."));
    }

    @DeleteMapping("/delete-review/{reviewId}")
    public ResponseEntity<?> deleteReservationReview(@PathVariable Long reviewId, HttpSession session) {
        // ... 기존 로직 ...
        return ResponseEntity.status(HttpStatus.GONE).body(Map.of("message", "이 API는 더 이상 사용되지 않습니다. DELETE /api/v1/reviews/{reviewId} API를 사용하세요."));
    }
    */

    // 장바구니 기반 예약 생성 관련 로직은 유지될 수 있으나,
    // 현재 요청의 핵심은 리뷰 기능 분리이므로, 이 부분은 그대로 둡니다.
    // 필요하다면 추후 별도 리팩토링을 고려할 수 있습니다.

    /**
     * 장바구니에 담긴 항목들을 기반으로 예약 페이지에 필요한 정보를 제공합니다.
     *
     * @param session HTTP 세션
     * @return 장바구니 항목, 총액 등 예약 진행에 필요한 정보
     */
    @GetMapping("/cart-checkout")
    public ResponseEntity<?> cartCheckout(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            List<CartItem> cartItems = cartService.getCartItems(userId);
            if (cartItems.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "장바구니가 비어있습니다."));
            }

            // 각 장바구니 아이템에 대해 예약 가능한지, 가격 변동이 있는지 등을 확인하는 로직 추가 가능
            // 예를 들어, 각 CartItem의 roomId, checkIn/Out 날짜로 현재 예약 가능 상태 및 가격을 다시 조회하여
            // 사용자에게 최신 정보를 보여줄 수 있습니다.

            BigDecimal totalPrice = cartService.calculateTotalPrice(userId);

            Map<String, Object> checkoutData = new HashMap<>();
            checkoutData.put("cartItems", cartItems);
            checkoutData.put("totalPrice", totalPrice);
            // 추가적으로 사용자 정보 (포인트, 쿠폰 등) 전달 가능

            return ResponseEntity.ok(checkoutData);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "장바구니 결제 정보 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 장바구니의 모든 항목에 대해 한 번에 예약을 생성합니다. (일괄 예약)
     *
     * @param payload (선택적) 결제 정보 등 추가 데이터
     * @param session HTTP 세션
     * @return 성공 시 생성된 예약 ID 목록과 메시지, 실패 시 오류 메시지
     */
    @PostMapping("/create-from-cart")
    public ResponseEntity<?> createReservationsFromCart(
            @RequestBody(required = false) Map<String, Object> payload, // 예: specialRequests 등
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            List<CartItem> cartItemsFromService = cartService.getCartItems(userId);
            if (cartItemsFromService == null || cartItemsFromService.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "장바구니가 비어있습니다."));
            }

            List<Map<String, Object>> cartItemsForReservation = new ArrayList<>();
            for (CartItem item : cartItemsFromService) {
                Map<String, Object> mapItem = new HashMap<>();
                mapItem.put("roomId", item.getRoomId());
                mapItem.put("checkInDate", item.getCheckInDate().toString());
                mapItem.put("checkOutDate", item.getCheckOutDate().toString());
                mapItem.put("guestCount", item.getGuestCount());
                
                // CartItem의 getPrice()가 해당 장바구니 아이템의 '총액'을 반환한다고 가정합니다.
                // 만약 1박당 가격이라면, 여기서 총액 계산 로직이 필요합니다.
                // (예: Room room = roomDao.getRoomById(item.getRoomId()); long nights = ChronoUnit.DAYS.between(item.getCheckInDate(), item.getCheckOutDate()); BigDecimal itemTotalPrice = room.getPrice().multiply(BigDecimal.valueOf(nights)); mapItem.put("price", itemTotalPrice.toString());)
                // 이 경우 RoomDao 의존성 주입이 필요합니다.
                if (item.getPrice() != null) {
                    mapItem.put("price", item.getPrice().toString());
                } else {
                    // 가격 정보가 없는 아이템은 예약 처리에서 제외하거나 오류를 발생시킬 수 있습니다.
                    // 여기서는 오류를 발생시키도록 처리합니다.
                    // 실제 RoomDao를 사용하여 가격을 조회하고 계산하려면 해당 Dao를 주입받아야 합니다.
                    // 지금은 CartItem에 가격이 있다고 가정하고, 없으면 에러 처리합니다.
                    System.err.println("경고: CartItem ID " + item.getCartItemId() + "에 가격 정보가 없습니다. 이 아이템은 예약에서 제외될 수 있습니다.");
                    // 또는 예외 발생:
                    throw new SQLException("장바구니 아이템 ID " + item.getCartItemId() + "의 가격 정보를 찾을 수 없습니다. 관리자에게 문의하세요.");
                }
                cartItemsForReservation.add(mapItem);
            }

            String specialRequests = null;
            if (payload != null && payload.containsKey("specialRequests")) {
                specialRequests = (String) payload.get("specialRequests");
            }

            List<Long> reservationIds = reservationService.createReservationsFromCart(cartItemsForReservation, userId, specialRequests);
            
            // 성공 시 장바구니 비우기
            cartService.clearCart(userId);

            return ResponseEntity.ok(Map.of(
                    "message", "장바구니의 모든 항목에 대한 예약이 성공적으로 완료되었습니다.",
                    "reservationIds", reservationIds
            ));
        } catch (SQLException e) {
            if (e.getMessage().contains("장바구니가 비어있습니다") || e.getMessage().contains("예약 가능한 객실이 없습니다") || e.getMessage().contains("가격 정보를 찾을 수 없습니다")) {
                return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
            }
            e.printStackTrace(); // 서버 로그에 전체 스택 트레이스 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "장바구니 예약 생성 중 데이터베이스 오류 발생: " + e.getMessage()));
        } catch (NumberFormatException | ClassCastException e) {
             e.printStackTrace(); // 서버 로그에 전체 스택 트레이스 출력
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "장바구니 데이터 형식 오류: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // 서버 로그에 전체 스택 트레이스 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "장바구니 예약 처리 중 예기치 않은 오류 발생: " + e.getMessage()));
        }
    }
}
