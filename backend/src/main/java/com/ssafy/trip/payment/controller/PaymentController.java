package com.ssafy.trip.payment.controller;

import com.ssafy.trip.payment.model.Payment;
import com.ssafy.trip.payment.dto.OrderDto;
import com.ssafy.trip.payment.service.PaymentService;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.exception.IamportResponseException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "결제/아임포트/내부결제 API")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "아임포트 결제 검증+저장", description = "imp_uid로 결제내역 검증 후 DB에 저장. 성공 시 Payment 반환.")
    @PostMapping("/validation/{imp_uid}")
    public ResponseEntity<?> validateIamportAndSave(@Parameter(description = "아임포트 imp_uid") @PathVariable String imp_uid) {
        try {
            Payment payment = paymentService.validateAndSaveIamport(imp_uid);
            if (payment == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "결제 검증 또는 저장에 실패했습니다."));
            }
            return ResponseEntity.ok(payment);
        } catch (IamportResponseException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "폼/수동 주문 결제 정보 저장", description = "프론트에서 직접 결제정보를 넘기면 DB에 저장")
    @PostMapping("/order")
    public ResponseEntity<String> processOrder(@RequestBody OrderDto orderDto) {
        return ResponseEntity.ok(paymentService.saveOrder(orderDto));
    }

    @Operation(summary = "아임포트 결제 취소", description = "imp_uid로 결제 취소 후 DB에도 반영")
    @PostMapping("/cancel/imp/{imp_uid}")
    public ResponseEntity<?> cancelIamportPayment(@Parameter(description = "아임포트 imp_uid") @PathVariable String imp_uid) {
        try {
            Payment cancelled = paymentService.cancelIamportAndUpdate(imp_uid);
            if (cancelled == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "결제 취소에 실패했습니다."));
            }
            return ResponseEntity.ok(cancelled);
        } catch (IamportResponseException | IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "내부 결제 취소", description = "paymentId와 취소사유로 DB 결제 취소")
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(
            @Parameter(description = "결제 PK") @RequestParam Long paymentId,
            @Parameter(description = "취소사유") @RequestParam String cancelReason,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "로그인이 필요한 서비스입니다."));
        try {
            Payment cancelled = paymentService.cancelPayment(paymentId, cancelReason);
            if (cancelled == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "결제 취소에 실패했습니다."));
            return ResponseEntity.ok(cancelled);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "결제 내역 조회", description = "내 결제내역 전체 조회")
    @GetMapping("/history")
    public ResponseEntity<?> paymentHistory(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "로그인이 필요한 서비스입니다."));
        try {
            List<Payment> payments = paymentService.getPaymentsByUserId(userId);
            return ResponseEntity.ok(payments);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "결제 상세 정보", description = "내 결제 상세정보")
    @GetMapping("/detail/{paymentId}")
    public ResponseEntity<?> paymentDetail(
            @Parameter(description = "결제 PK") @PathVariable Long paymentId,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "로그인이 필요한 서비스입니다."));
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            if (payment == null)
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "결제 정보를 찾을 수 없습니다."));
            if (!payment.getUserId().equals(userId))
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "해당 결제 정보에 접근할 권한이 없습니다."));
            return ResponseEntity.ok(payment);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "처리중 결제(PENDING) 개수", description = "내 결제 중 미완료(PENDING) 개수 조회")
    @GetMapping("/count/unread")
    public ResponseEntity<?> getUnreadPaymentCount(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "로그인이 필요한 서비스입니다."));
        try {
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
