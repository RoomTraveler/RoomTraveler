package com.ssafy.trip.payment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

/**
 * 결제 컨트롤러
 * 결제 관련 HTTP 요청을 처리합니다.
 */
@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 생성자 주입을 통한 의존성 주입
     * 
     * @param paymentService 결제 서비스
     */
    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 결제 페이지로 이동합니다.
     */
    @GetMapping("/form/{reservationId}")
    public String paymentForm(
            @PathVariable Long reservationId,
            HttpSession session,
            Model model) {
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form?alertMsg=로그인이 필요한 서비스입니다.";
        }
        
        try {
            // 예약 정보 조회 (실제 구현에서는 ReservationService를 통해 조회)
            // Reservation reservation = reservationService.getReservationById(reservationId);
            
            // 예약 정보가 없는 경우 처리
            // if (reservation == null) {
            //     model.addAttribute("error", "예약 정보를 찾을 수 없습니다.");
            //     return "error";
            // }
            
            // 예약 소유자 확인
            // if (!reservation.getUserId().equals(userId)) {
            //     model.addAttribute("error", "해당 예약에 대한 결제 권한이 없습니다.");
            //     return "error";
            // }
            
            // 이미 결제가 완료된 경우 처리
            Payment existingPayment = paymentService.getPaymentByReservationId(reservationId);
            if (existingPayment != null && "COMPLETED".equals(existingPayment.getStatus())) {
                model.addAttribute("error", "이미 결제가 완료된 예약입니다.");
                return "error";
            }
            
            // 모델에 데이터 추가
            model.addAttribute("reservationId", reservationId);
            // model.addAttribute("reservation", reservation);
            // model.addAttribute("totalPrice", reservation.getTotalPrice());
            
            return "payment/form";
        } catch (SQLException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 결제를 처리합니다.
     */
    @PostMapping("/process")
    public String processPayment(
            @RequestParam Long reservationId,
            @RequestParam String paymentMethod,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String cardInfo,
            @RequestParam(required = false) String bankInfo,
            @RequestParam(required = false) String phoneInfo,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form?alertMsg=로그인이 필요한 서비스입니다.";
        }
        
        try {
            // 결제 처리
            Payment payment = null;
            
            switch (paymentMethod) {
                case "CARD":
                    payment = paymentService.processCardPayment(reservationId, userId, amount, cardInfo);
                    break;
                case "BANK_TRANSFER":
                    payment = paymentService.processBankTransferPayment(reservationId, userId, amount, bankInfo);
                    break;
                case "PHONE":
                    payment = paymentService.processPhonePayment(reservationId, userId, amount, phoneInfo);
                    break;
                default:
                    model.addAttribute("error", "지원하지 않는 결제 방법입니다: " + paymentMethod);
                    return "error";
            }
            
            // 결제 성공 시 예약 상태 업데이트 (실제 구현에서는 ReservationService를 통해 업데이트)
            // reservationService.updateReservationStatus(reservationId, "CONFIRMED");
            
            redirectAttributes.addFlashAttribute("message", "결제가 성공적으로 처리되었습니다.");
            return "redirect:/payment/result/" + payment.getPaymentId();
        } catch (SQLException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        } catch (Exception e) {
            model.addAttribute("error", "결제 처리 중 오류가 발생했습니다: " + e.getMessage());
            return "error";
        }
    }

    /**
     * 결제 결과 페이지로 이동합니다.
     */
    @GetMapping("/result/{paymentId}")
    public String paymentResult(
            @PathVariable Long paymentId,
            HttpSession session,
            Model model) {
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form?alertMsg=로그인이 필요한 서비스입니다.";
        }
        
        try {
            // 결제 정보 조회
            Payment payment = paymentService.getPaymentById(paymentId);
            
            // 결제 정보가 없는 경우 처리
            if (payment == null) {
                model.addAttribute("error", "결제 정보를 찾을 수 없습니다.");
                return "error";
            }
            
            // 결제 소유자 확인
            if (!payment.getUserId().equals(userId)) {
                model.addAttribute("error", "해당 결제 정보에 접근할 권한이 없습니다.");
                return "error";
            }
            
            // 모델에 데이터 추가
            model.addAttribute("payment", payment);
            
            return "payment/result";
        } catch (SQLException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 결제 취소 페이지로 이동합니다.
     */
    @GetMapping("/cancel/{paymentId}")
    public String cancelPaymentForm(
            @PathVariable Long paymentId,
            HttpSession session,
            Model model) {
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form?alertMsg=로그인이 필요한 서비스입니다.";
        }
        
        try {
            // 결제 정보 조회
            Payment payment = paymentService.getPaymentById(paymentId);
            
            // 결제 정보가 없는 경우 처리
            if (payment == null) {
                model.addAttribute("error", "결제 정보를 찾을 수 없습니다.");
                return "error";
            }
            
            // 결제 소유자 확인
            if (!payment.getUserId().equals(userId)) {
                model.addAttribute("error", "해당 결제 정보에 접근할 권한이 없습니다.");
                return "error";
            }
            
            // 이미 취소된 결제인 경우 처리
            if ("CANCELLED".equals(payment.getStatus())) {
                model.addAttribute("error", "이미 취소된 결제입니다.");
                return "error";
            }
            
            // 모델에 데이터 추가
            model.addAttribute("payment", payment);
            
            return "payment/cancel-form";
        } catch (SQLException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 결제를 취소합니다.
     */
    @PostMapping("/cancel")
    public String cancelPayment(
            @RequestParam Long paymentId,
            @RequestParam String cancelReason,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model) {
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form?alertMsg=로그인이 필요한 서비스입니다.";
        }
        
        try {
            // 결제 정보 조회
            Payment payment = paymentService.getPaymentById(paymentId);
            
            // 결제 정보가 없는 경우 처리
            if (payment == null) {
                model.addAttribute("error", "결제 정보를 찾을 수 없습니다.");
                return "error";
            }
            
            // 결제 소유자 확인
            if (!payment.getUserId().equals(userId)) {
                model.addAttribute("error", "해당 결제 정보에 접근할 권한이 없습니다.");
                return "error";
            }
            
            // 이미 취소된 결제인 경우 처리
            if ("CANCELLED".equals(payment.getStatus())) {
                model.addAttribute("error", "이미 취소된 결제입니다.");
                return "error";
            }
            
            // 결제 취소 처리
            Payment cancelledPayment = paymentService.cancelPayment(paymentId, cancelReason);
            
            // 결제 취소 실패 시 처리
            if (cancelledPayment == null) {
                model.addAttribute("error", "결제 취소에 실패했습니다.");
                return "error";
            }
            
            // 예약 상태 업데이트 (실제 구현에서는 ReservationService를 통해 업데이트)
            // reservationService.updateReservationStatus(payment.getReservationId(), "CANCELLED");
            
            redirectAttributes.addFlashAttribute("message", "결제가 성공적으로 취소되었습니다.");
            return "redirect:/payment/history";
        } catch (SQLException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 결제 내역 페이지로 이동합니다.
     */
    @GetMapping("/history")
    public String paymentHistory(
            HttpSession session,
            Model model) {
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form?alertMsg=로그인이 필요한 서비스입니다.";
        }
        
        try {
            // 결제 내역 조회
            List<Payment> payments = paymentService.getPaymentsByUserId(userId);
            
            // 모델에 데이터 추가
            model.addAttribute("payments", payments);
            
            return "payment/history";
        } catch (SQLException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 결제 상세 정보 페이지로 이동합니다.
     */
    @GetMapping("/detail/{paymentId}")
    public String paymentDetail(
            @PathVariable Long paymentId,
            HttpSession session,
            Model model) {
        
        // 로그인 확인
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/user/login-form?alertMsg=로그인이 필요한 서비스입니다.";
        }
        
        try {
            // 결제 정보 조회
            Payment payment = paymentService.getPaymentById(paymentId);
            
            // 결제 정보가 없는 경우 처리
            if (payment == null) {
                model.addAttribute("error", "결제 정보를 찾을 수 없습니다.");
                return "error";
            }
            
            // 결제 소유자 확인
            if (!payment.getUserId().equals(userId)) {
                model.addAttribute("error", "해당 결제 정보에 접근할 권한이 없습니다.");
                return "error";
            }
            
            // 모델에 데이터 추가
            model.addAttribute("payment", payment);
            
            return "payment/detail";
        } catch (SQLException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 읽지 않은 알림 개수를 조회하는 API
     */
    @GetMapping("/api/count/unread")
    @ResponseBody
    public ResponseEntity<?> getUnreadPaymentCount(
            @SessionAttribute(name = "userId", required = false) Long userId) {
        
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요한 서비스입니다."));
        }
        
        try {
            // 결제 내역 중 처리 중인 결제 개수 조회
            Map<String, Object> filters = new HashMap<>();
            filters.put("userId", userId);
            filters.put("status", "PENDING");
            
            List<Payment> pendingPayments = paymentService.getFilteredPayments(filters);
            
            return ResponseEntity.ok(Map.of("count", pendingPayments.size()));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}