package com.ssafy.trip.payment.controller;

import com.ssafy.trip.payment.dto.PaymentCompleteRequestDto;
import com.ssafy.trip.payment.dto.PaymentPrepareRequestDto;
import com.ssafy.trip.payment.dto.PaymentPrepareResponseDto;
import com.ssafy.trip.payment.model.Payment;
import com.ssafy.trip.payment.service.PaymentService;
import com.ssafy.trip.accommodation.service.ReservationService;
import com.ssafy.trip.exception.ResourceNotFoundException;

import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.ssafy.trip.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "결제/아임포트/내부결제 API")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;
    private final ReservationService reservationService;

    @Operation(summary = "결제 정보 사전 준비", description = "결제할 예약 정보를 PENDING_PAYMENT 상태로 임시 생성하고, 주문ID(merchant_uid)와 결제 금액을 반환합니다.")
    @PostMapping("/prepare")
    public ResponseEntity<?> preparePayment(
            @RequestBody PaymentPrepareRequestDto prepareRequestDto,
            @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        try {
            Map<String, Object> preparationResult = reservationService.prepareReservationsForPayment(prepareRequestDto, user.getUserId());
            
            PaymentPrepareResponseDto responseDto = PaymentPrepareResponseDto.builder()
                .merchantUid((String) preparationResult.get("merchantUid"))
                .amount((BigDecimal) preparationResult.get("amount"))
                .paymentName((String) preparationResult.get("paymentName"))
                .buyerEmail(user.getEmail())
                .buyerName(user.getUsername())
                .buyerTel(user.getPhone())
                .build();

            return ResponseEntity.ok(responseDto);
        } catch (Exception e) {
            log.error("Error during payment preparation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "결제 준비 중 오류 발생: " + e.getMessage()));
        }
    }

    @Operation(summary = "아임포트 결제 검증 및 최종 처리", description = "아임포트 결제 성공 후 imp_uid와 merchant_uid로 결제내역을 검증하고, 예약 및 결제 정보를 최종 저장/업데이트합니다.")
    @PostMapping("/complete")
    public ResponseEntity<?> completePayment(@RequestBody PaymentCompleteRequestDto completeRequestDto, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        try {
            Payment payment = paymentService.validateAndFinalizePayment(completeRequestDto, user.getUserId());
            return ResponseEntity.ok(Map.of(
                "message", "결제가 성공적으로 처리되었습니다.",
                "paymentId", payment.getPaymentId(),
                "relatedReservationInfo", payment.getReservationId()
            ));
        } catch (IamportResponseException | IOException e) {
            log.error("Iamport API error during payment completion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "결제 검증 실패: " + e.getMessage()));
        } catch (IllegalArgumentException e) {
             log.error("Invalid argument during payment completion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "결제 처리 중 유효하지 않은 인자: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during payment completion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "결제 처리 중 예상치 못한 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "아임포트 결제 취소", description = "imp_uid로 결제 취소 후 DB에도 반영")
    @PostMapping("/cancel/imp/{imp_uid}")
    public ResponseEntity<?> cancelIamportPayment(@Parameter(description = "아임포트 imp_uid") @PathVariable String imp_uid, @AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        try {
            // 1. imp_uid로 우리 시스템의 Payment 정보 조회
            Payment paymentToCancel = paymentService.getPaymentByImpUid(imp_uid);

            // 2. 조회된 Payment 정보의 ID와 사용자 ID를 사용하여 아임포트 결제 취소 요청
            IamportResponse<com.siot.IamportRestClient.response.Payment> iamportResponse =
                    paymentService.cancelPayment(paymentToCancel.getPaymentId(), "사용자 요청에 의한 취소", user.getUserId());

            if (iamportResponse != null && iamportResponse.getResponse() != null && "cancelled".equalsIgnoreCase(iamportResponse.getResponse().getStatus())) {
                 // 성공적으로 취소된 경우, 우리 시스템의 최신 Payment 정보를 다시 조회하여 반환 (또는 iamportResponse.getResponse() 기반으로 DTO 만들어 반환)
                Payment finalCancelledPayment = paymentService.getPaymentById(paymentToCancel.getPaymentId());
                return ResponseEntity.ok(finalCancelledPayment);
            } else {
                log.error("아임포트 결제 취소는 되었으나, 최종 확인 과정에서 문제 발생 또는 응답이 예상과 다름. imp_uid: {}", imp_uid);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "결제 취소 후 최종 확인 중 문제가 발생했습니다.", "iamportMessage", iamportResponse != null ? iamportResponse.getMessage() : "No Iamport message"));
            }

        } catch (ResourceNotFoundException e) {
            log.warn("취소할 결제 정보를 찾을 수 없습니다 (imp_uid: {}): {}", imp_uid, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IamportResponseException | IOException e) {
            log.error("Iamport API error during payment cancellation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "결제 취소 실패 (API 오류): " + e.getMessage()));
        } catch (SecurityException e) {
            log.warn("Unauthorized attempt to cancel Iamport payment for imp_uid {}: User {}", imp_uid, user.getUserId());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error during Iamport cancel and update: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "결제 취소 처리 중 예상치 못한 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "내부 결제 취소 (DB 직접 수정)", description = "paymentId와 취소사유로 DB 결제 취소")
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelPayment(
            @Parameter(description = "결제 PK") @RequestParam Long paymentId,
            @Parameter(description = "취소사유") @RequestParam String cancelReason,
            @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "로그인이 필요한 서비스입니다."));
        try {
            Payment cancelled = paymentService.cancelInternalPayment(paymentId, cancelReason, user.getUserId());
            // cancelInternalPayment는 성공 시 Payment 객체를, 실패 시 예외를 발생시키므로 null 체크는 여기서 불필요할 수 있음.
            // 서비스 레이어에서 예외 처리를 통해 명확한 실패를 알리도록 구현되어야 함.
            return ResponseEntity.ok(cancelled);
        } catch (ResourceNotFoundException e) {
            log.warn("내부적으로 취소할 결제 정보를 찾을 수 없습니다 (Payment ID: {}): {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (SecurityException e) {
            log.warn("Unauthorized attempt to cancel payment: User {} tried to cancel payment {}", user.getUserId(), paymentId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) { // 이미 취소된 경우 등
            log.warn("Illegal state during internal payment cancellation (Payment ID: {}): {}", paymentId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        } catch (SQLException e) {
             log.error("SQL error during payment cancellation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "결제 취소 중 DB 오류 발생: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during payment cancellation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "내부 결제 취소 처리 중 예상치 못한 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "결제 내역 조회", description = "내 결제내역 전체 조회")
    @GetMapping("/history")
    public ResponseEntity<?> paymentHistory(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "로그인이 필요한 서비스입니다."));
        try {
            List<Payment> payments = paymentService.getPaymentsByUserId(user.getUserId());
            return ResponseEntity.ok(payments);
        } catch (SQLException e) {
            log.error("SQL error fetching payment history for user {}: {}", user.getUserId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "결제 내역 조회 중 DB 오류 발생: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching payment history for user {}: {}", user.getUserId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "결제 내역 조회 중 예상치 못한 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "결제 상세 정보", description = "내 결제 상세정보")
    @GetMapping("/detail/{paymentId}")
    public ResponseEntity<?> paymentDetail(
            @Parameter(description = "결제 PK") @PathVariable Long paymentId,
            @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "로그인이 필요한 서비스입니다."));
        try {
            Payment payment = paymentService.getPaymentByIdAndUserId(paymentId, user.getUserId());
            if (payment == null) {
                 Payment checkExists = paymentService.getPaymentById(paymentId);
                 if (checkExists == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "결제 정보를 찾을 수 없습니다."));
                 } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "해당 결제 정보에 접근할 권한이 없습니다."));
                 }
            }
            return ResponseEntity.ok(payment);
        } catch (SQLException e) {
            log.error("SQL error fetching payment detail for paymentId {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "결제 상세 정보 조회 중 DB 오류 발생: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error fetching payment detail for paymentId {}: {}", paymentId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "결제 상세 정보 조회 중 예상치 못한 오류가 발생했습니다."));
        }
    }

    @Operation(summary = "처리중 결제(PENDING) 개수", description = "내 결제 중 미완료(PENDING_PAYMENT) 상태의 예약 건수 조회")
    @GetMapping("/count/pending")
    public ResponseEntity<?> getPendingPaymentCount(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "로그인이 필요한 서비스입니다."));
        try {
            int pendingCount = reservationService.countPendingReservationsByUserId(user.getUserId());
            return ResponseEntity.ok(Map.of("count", pendingCount));
        } catch (Exception e) {
            log.error("Error fetching pending payment count for user {}: {}", user.getUserId(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "처리 중인 결제 건수 조회 중 오류 발생: " + e.getMessage()));
        }
    }
}
