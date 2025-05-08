package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.CartItem;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.model.RoomAvailability;
import com.ssafy.trip.review.Review;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.accommodation.service.CartService;
import com.ssafy.trip.accommodation.service.ReservationService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 예약 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;
    private final AccommodationService accommodationService;
    private final CartService cartService;

    /**
     * 예약 폼 페이지로 이동합니다.
     */
    @GetMapping("/form/{roomId}")
    public String reservationForm(
            @PathVariable Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(defaultValue = "1") int guestCount,
            HttpSession session,
            Model model) {

        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            // 객실 정보 조회
            Room room = accommodationService.getRoomById(roomId);

            // 객실 가용성 확인
            boolean isAvailable = reservationService.isRoomAvailable(roomId, checkInDate, checkOutDate, guestCount);
            if (!isAvailable) {
                model.addAttribute("error", "선택한 날짜에 예약 가능한 객실이 없습니다.");
                return "error";
            }

            // 숙박 일수 계산
            long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);

            // 총 가격 계산
            BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(nights));

            model.addAttribute("room", room);
            model.addAttribute("checkInDate", checkInDate);
            model.addAttribute("checkOutDate", checkOutDate);
            model.addAttribute("guestCount", guestCount);
            model.addAttribute("nights", nights);
            model.addAttribute("totalPrice", totalPrice);

            return "reservation/form";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 새 예약을 등록합니다.
     */
    @PostMapping("/create")
    public String createReservation(
            @ModelAttribute Reservation reservation,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            // 사용자 ID 설정
            reservation.setUserId(userId);

            // 상태 설정 (기본값: PENDING)
            reservation.setStatus("PENDING");

            // 결제 상태 설정 (기본값: UNPAID)
            reservation.setPaymentStatus("UNPAID");

            // 예약 등록
            Long reservationId = reservationService.createReservation(reservation);

            redirectAttributes.addFlashAttribute("message", "예약이 완료되었습니다. 결제를 진행해주세요.");
            return "redirect:/reservation/detail/" + reservationId;
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "reservation/form";
        }
    }

    /**
     * 예약 상세 페이지로 이동합니다.
     */
    @GetMapping("/detail/{reservationId}")
    public String reservationDetail(@PathVariable Long reservationId, HttpSession session, Model model) {
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);

            // 예약 소유자 또는 호스트만 조회 가능
            if (!reservation.getUserId().equals(userId)) {
                // 호스트 확인 (객실의 숙소의 호스트인지 확인)
                Room room = accommodationService.getRoomById(reservation.getRoomId());
                if (room != null) {
                    Long hostId = accommodationService.getAccommodationById(room.getAccommodationId()).getHostId();
                    if (!hostId.equals(userId)) {
                        model.addAttribute("error", "해당 예약의 정보를 조회할 권한이 없습니다.");
                        return "error";
                    }
                } else {
                    model.addAttribute("error", "해당 예약의 정보를 조회할 권한이 없습니다.");
                    return "error";
                }
            }

            // 리뷰 정보 조회
            Review review = reservationService.getReviewByReservationId(reservationId);

            model.addAttribute("reservation", reservation);
            model.addAttribute("review", review);
            return "reservation/detail";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 사용자의 예약 목록 페이지로 이동합니다.
     */
    @GetMapping("/my-reservations")
    public String myReservations(HttpSession session, Model model) {
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            List<Reservation> reservations = reservationService.getReservationsByUserId(userId);
            model.addAttribute("reservations", reservations);
            return "reservation/my-reservations";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 호스트의 예약 목록 페이지로 이동합니다.
     */
    @GetMapping("/host-reservations")
    public String hostReservations(HttpSession session, Model model) {
        // 로그인 및 호스트 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || (!"HOST".equals(role) && !"ADMIN".equals(role))) {
            return "redirect:/user/login-form";
        }

        try {
            List<Reservation> reservations = reservationService.getReservationsByHostId(hostId);
            model.addAttribute("reservations", reservations);
            return "reservation/host-reservations";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 예약을 취소합니다.
     */
    @GetMapping("/cancel/{reservationId}")
    public String cancelReservation(
            @PathVariable Long reservationId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);

            // 예약 소유자만 취소 가능
            if (!reservation.getUserId().equals(userId)) {
                model.addAttribute("error", "해당 예약을 취소할 권한이 없습니다.");
                return "error";
            }

            // 예약 취소
            reservationService.cancelReservation(reservationId);

            redirectAttributes.addFlashAttribute("message", "예약이 취소되었습니다.");
            return "redirect:/reservation/my-reservations";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 예약 상태를 업데이트합니다. (호스트 전용)
     */
    @PostMapping("/update-status/{reservationId}")
    public String updateReservationStatus(
            @PathVariable Long reservationId,
            @RequestParam String status,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 및 호스트/관리자 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || (!"HOST".equals(role) && !"ADMIN".equals(role))) {
            return "redirect:/user/login-form";
        }

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);

            // 해당 예약의 호스트인지 확인
            Room room = accommodationService.getRoomById(reservation.getRoomId());
            if (room != null) {
                Long accommodationHostId = accommodationService.getAccommodationById(room.getAccommodationId()).getHostId();
                if (!accommodationHostId.equals(hostId)) {
                    model.addAttribute("error", "해당 예약의 상태를 변경할 권한이 없습니다.");
                    return "error";
                }
            } else {
                model.addAttribute("error", "해당 예약의 상태를 변경할 권한이 없습니다.");
                return "error";
            }

            // 예약 상태 업데이트
            reservationService.updateReservationStatus(reservationId, status);

            redirectAttributes.addFlashAttribute("message", "예약 상태가 업데이트되었습니다.");
            return "redirect:/reservation/host-reservations";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 결제 상태를 업데이트합니다.
     */
    @PostMapping("/update-payment/{reservationId}")
    public String updatePaymentStatus(
            @PathVariable Long reservationId,
            @RequestParam String paymentStatus,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);

            // 예약 소유자만 결제 상태 변경 가능
            if (!reservation.getUserId().equals(userId)) {
                model.addAttribute("error", "해당 예약의 결제 상태를 변경할 권한이 없습니다.");
                return "error";
            }

            // 결제 상태 업데이트
            reservationService.updatePaymentStatus(reservationId, paymentStatus);

            redirectAttributes.addFlashAttribute("message", "결제 상태가 업데이트되었습니다.");
            return "redirect:/reservation/detail/" + reservationId;
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 리뷰 작성 폼 페이지로 이동합니다.
     */
    @GetMapping("/review-form/{reservationId}")
    public String reviewForm(@PathVariable Long reservationId, HttpSession session, Model model) {
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);

            // 예약 소유자만 리뷰 작성 가능
            if (!reservation.getUserId().equals(userId)) {
                model.addAttribute("error", "해당 예약에 대한 리뷰를 작성할 권한이 없습니다.");
                return "error";
            }

            // 예약 상태 확인 (완료된 예약만 리뷰 작성 가능)
            if (!"COMPLETED".equals(reservation.getStatus())) {
                model.addAttribute("error", "완료된 예약에 대해서만 리뷰를 작성할 수 있습니다.");
                return "error";
            }

            // 이미 리뷰가 있는지 확인
            Review existingReview = reservationService.getReviewByReservationId(reservationId);
            if (existingReview != null) {
                model.addAttribute("error", "이미 리뷰가 작성되었습니다.");
                return "error";
            }

            model.addAttribute("reservation", reservation);
            return "reservation/review-form";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 리뷰를 작성합니다.
     */
    @PostMapping("/create-review")
    public String createReview(
            @ModelAttribute Review review,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            // 사용자 ID 설정
            review.setUserId(userId);

            // 리뷰 작성
            reservationService.createReview(review);

            redirectAttributes.addFlashAttribute("message", "리뷰가 작성되었습니다.");
            return "redirect:/reservation/detail/" + review.getReservationId();
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "reservation/review-form";
        }
    }

    /**
     * 리뷰 수정 폼 페이지로 이동합니다.
     */
    @GetMapping("/update-review-form/{reviewId}")
    public String updateReviewForm(@PathVariable Long reviewId, HttpSession session, Model model) {
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            Review review = reservationService.getReviewById(reviewId);

            // 리뷰 작성자만 수정 가능
            if (!review.getUserId().equals(userId)) {
                model.addAttribute("error", "해당 리뷰를 수정할 권한이 없습니다.");
                return "error";
            }

            model.addAttribute("review", review);
            return "reservation/update-review-form";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 리뷰를 수정합니다.
     */
    @PostMapping("/update-review")
    public String updateReview(
            @ModelAttribute Review review,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            // 사용자 ID 설정
            review.setUserId(userId);

            // 리뷰 수정
            reservationService.updateReview(review);

            redirectAttributes.addFlashAttribute("message", "리뷰가 수정되었습니다.");
            return "redirect:/reservation/detail/" + review.getReservationId();
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "reservation/update-review-form";
        }
    }

    /**
     * 리뷰를 삭제합니다.
     */
    @GetMapping("/delete-review/{reviewId}")
    public String deleteReview(
            @PathVariable Long reviewId,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            Review review = reservationService.getReviewById(reviewId);

            // 리뷰 작성자만 삭제 가능
            if (!review.getUserId().equals(userId)) {
                model.addAttribute("error", "해당 리뷰를 삭제할 권한이 없습니다.");
                return "error";
            }

            // 리뷰 삭제
            reservationService.deleteReview(reviewId);

            redirectAttributes.addFlashAttribute("message", "리뷰가 삭제되었습니다.");
            return "redirect:/reservation/detail/" + review.getReservationId();
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 장바구니에서 예약 페이지로 이동합니다.
     */
    @GetMapping("/cart-checkout")
    public String cartCheckout(HttpSession session, Model model) {
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        // 장바구니 아이템 확인
        @SuppressWarnings("unchecked")
        List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
        if (cartItems == null || cartItems.isEmpty()) {
            model.addAttribute("error", "장바구니가 비어있습니다.");
            return "redirect:/cart";
        }

        // 총 가격 확인
        BigDecimal totalPrice = (BigDecimal) session.getAttribute("totalPrice");
        if (totalPrice == null) {
            totalPrice = cartItems.stream()
                    .map(CartItem::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);

        return "reservation/cart-checkout";
    }

    /**
     * 장바구니에서 예약을 생성합니다.
     */
    @PostMapping("/create-from-cart")
    public String createReservationsFromCart(
            @RequestParam(required = false) String specialRequests,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {

        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form";
        }

        try {
            // 장바구니 아이템 확인
            @SuppressWarnings("unchecked")
            List<CartItem> cartItems = (List<CartItem>) session.getAttribute("cartItems");
            if (cartItems == null || cartItems.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "장바구니가 비어있습니다.");
                return "redirect:/cart";
            }

            // 각 장바구니 아이템에 대해 예약 생성
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

                // 예약 등록
                reservationService.createReservation(reservation);
            }

            // 장바구니 비우기
            cartService.clearCart(userId);

            // 세션에서 장바구니 아이템 제거
            session.removeAttribute("cartItems");
            session.removeAttribute("totalPrice");

            redirectAttributes.addFlashAttribute("message", "예약이 완료되었습니다. 결제를 진행해주세요.");
            return "redirect:/reservation/my-reservations";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "reservation/cart-checkout";
        }
    }
}
