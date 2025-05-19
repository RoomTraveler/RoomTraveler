package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.CartItem;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.review.Review;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservation")
public class ReservationRestController {

    private final ReservationService reservationService;
    private final AccommodationService accommodationService;
    private final CartService cartService;

    // 1. 예약 폼 데이터 (객실/날짜/가격) 반환
    @GetMapping("/form/{roomId}")
    public ResponseEntity<?> getReservationForm(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(defaultValue = "1") int guestCount,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));

        try {
            Room room = accommodationService.getRoomById(roomId);
            boolean isAvailable = reservationService.isRoomAvailable(roomId, checkInDate, checkOutDate, guestCount);
            if (!isAvailable)
                return ResponseEntity.badRequest().body(Map.of("error", "선택한 날짜에 예약 가능한 객실이 없습니다."));

            long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(nights));

            Map<String, Object> result = new HashMap<>();
            result.put("room", room);
            result.put("checkInDate", checkInDate);
            result.put("checkOutDate", checkOutDate);
            result.put("guestCount", guestCount);
            result.put("nights", nights);
            result.put("totalPrice", totalPrice);
            return ResponseEntity.ok(result);

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 2. 예약 생성
    @PostMapping("/create")
    public ResponseEntity<?> createReservation(
            @RequestBody Reservation reservation,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));

        try {
            reservation.setUserId(userId);
            reservation.setStatus("PENDING");
            reservation.setPaymentStatus("UNPAID");

            Long reservationId = reservationService.createReservation(reservation);
            return ResponseEntity.ok(Map.of("message", "예약이 완료되었습니다.", "reservationId", reservationId));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 3. 예약 상세
    @GetMapping("/detail/{reservationId}")
    public ResponseEntity<?> getReservationDetail(@PathVariable Long reservationId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            // 예약 소유자 or 호스트만
            if (!reservation.getUserId().equals(userId)) {
                Room room = accommodationService.getRoomById(reservation.getRoomId());
                if (room == null ||
                        !accommodationService.getAccommodationById(room.getAccommodationId()).getHostId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한 없음"));
                }
            }
            Review review = reservationService.getReviewByReservationId(reservationId);
            return ResponseEntity.ok(Map.of("reservation", reservation, "review", review));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 4. 내 예약 목록
    @GetMapping("/my-reservations")
    public ResponseEntity<?> myReservations(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        try {
            List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
            return ResponseEntity.ok(Map.of("reservations", reservations));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 5. 호스트 예약 목록
    @GetMapping("/host-reservations")
    public ResponseEntity<?> hostReservations(HttpSession session) {
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || (!"HOST".equals(role) && !"ADMIN".equals(role)))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "권한 없음"));
        try {
            List<Reservation> reservations = reservationService.getReservationsByHostId(hostId);
            return ResponseEntity.ok(Map.of("reservations", reservations));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 6. 예약 취소
    @PostMapping("/cancel/{reservationId}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long reservationId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (!reservation.getUserId().equals(userId))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한 없음"));

            reservationService.cancelReservation(reservationId);
            return ResponseEntity.ok(Map.of("message", "예약이 취소되었습니다."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 7. 예약 상태 업데이트 (호스트/관리자)
    @PostMapping("/update-status/{reservationId}")
    public ResponseEntity<?> updateReservationStatus(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");
        String status = payload.get("status");

        if (hostId == null || (!"HOST".equals(role) && !"ADMIN".equals(role)))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "권한 없음"));

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            Room room = accommodationService.getRoomById(reservation.getRoomId());
            if (room == null || !accommodationService.getAccommodationById(room.getAccommodationId()).getHostId().equals(hostId))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한 없음"));

            reservationService.updateReservationStatus(reservationId, status);
            return ResponseEntity.ok(Map.of("message", "예약 상태가 업데이트되었습니다."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 8. 결제 상태 업데이트 (예약 소유자)
    @PostMapping("/update-payment/{reservationId}")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long reservationId,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        String paymentStatus = payload.get("paymentStatus");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (!reservation.getUserId().equals(userId))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한 없음"));

            reservationService.updatePaymentStatus(reservationId, paymentStatus);
            return ResponseEntity.ok(Map.of("message", "결제 상태가 업데이트되었습니다."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 9. 리뷰 작성 폼 데이터 (예약 검증)
    @GetMapping("/review-form/{reservationId}")
    public ResponseEntity<?> reviewForm(@PathVariable Long reservationId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            if (!reservation.getUserId().equals(userId))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한 없음"));

            if (!"COMPLETED".equals(reservation.getStatus()))
                return ResponseEntity.badRequest().body(Map.of("error", "완료된 예약만 리뷰 작성 가능"));

            Review existingReview = reservationService.getReviewByReservationId(reservationId);
            if (existingReview != null)
                return ResponseEntity.badRequest().body(Map.of("error", "이미 리뷰가 작성되었습니다."));

            return ResponseEntity.ok(Map.of("reservation", reservation));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 10. 리뷰 작성
    @PostMapping("/create-review")
    public ResponseEntity<?> createReview(@RequestBody Review review, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        try {
            review.setUserId(userId);
            reservationService.createReview(review);
            return ResponseEntity.ok(Map.of("message", "리뷰가 작성되었습니다."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 11. 리뷰 수정 폼 데이터
    @GetMapping("/update-review-form/{reviewId}")
    public ResponseEntity<?> updateReviewForm(@PathVariable Long reviewId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        try {
            Review review = reservationService.getReviewById(reviewId);
            if (!review.getUserId().equals(userId))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한 없음"));
            return ResponseEntity.ok(Map.of("review", review));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 12. 리뷰 수정
    @PostMapping("/update-review")
    public ResponseEntity<?> updateReview(@RequestBody Review review, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        try {
            review.setUserId(userId);
            reservationService.updateReview(review);
            return ResponseEntity.ok(Map.of("message", "리뷰가 수정되었습니다."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 13. 리뷰 삭제
    @DeleteMapping("/delete-review/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long reviewId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        try {
            Review review = reservationService.getReviewById(reviewId);
            if (!review.getUserId().equals(userId))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한 없음"));
            reservationService.deleteReview(reviewId);
            return ResponseEntity.ok(Map.of("message", "리뷰가 삭제되었습니다."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    // 14. 장바구니 예약 폼 데이터
    @GetMapping("/cart-checkout")
    public ResponseEntity<?> cartCheckout(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));

        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        if (cartItems == null || cartItems.isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "장바구니가 비어있습니다."));

        BigDecimal totalPrice = (BigDecimal) session.getAttribute("totalPrice");
        if (totalPrice == null)
            totalPrice = cartItems.stream().map(CartItem::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        return ResponseEntity.ok(Map.of("cartItems", cartItems, "totalPrice", totalPrice));
    }

    // 15. 장바구니에서 예약 생성
    @PostMapping("/create-from-cart")
    public ResponseEntity<?> createReservationsFromCart(
            @RequestBody Map<String, Object> payload,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        String specialRequests = (String) payload.get("specialRequests");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인 필요"));
        try {
            @SuppressWarnings("unchecked")
            List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
            if (cartItems == null || cartItems.isEmpty())
                return ResponseEntity.badRequest().body(Map.of("error", "장바구니가 비어있습니다."));

            for (CartItem item : cartItems) {
                Reservation reservation = new Reservation();
                reservation.setUserId(userId);
                reservation.setRoomId(item.getRoomId());
                reservation.setCheckInDate(item.getCheckInDate());
                reservation.setCheckOutDate(item.getCheckOutDate());
                reservation.setGuestCount(item.getGuestCount());
                reservation.setTotalPrice(item.getPrice());
                reservation.setStatus("PENDING");
                reservation.setPaymentStatus("UNPAID");
                reservation.setSpecialRequests(specialRequests);
                reservationService.createReservation(reservation);
            }
            cartService.clearCart(userId);
            session.removeAttribute("cartItems");
            session.removeAttribute("totalPrice");
            return ResponseEntity.ok(Map.of("message", "예약이 완료되었습니다."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}
