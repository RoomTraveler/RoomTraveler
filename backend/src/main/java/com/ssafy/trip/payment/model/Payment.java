package com.ssafy.trip.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    private Long paymentId;           // 결제 ID
    private Long reservationId;       // 예약 ID
    private Long userId;              // 사용자 ID
    private String paymentMethod;     // 결제 방법 (CARD, BANK_TRANSFER, PHONE)
    private String paymentKey;        // 결제 키 (imp_uid 등)
    private BigDecimal amount;        // 결제 금액
    private String currency;          // 통화
    private String status;            // 결제 상태 (COMPLETED, CANCELLED 등)
    private String cardInfo;          // 카드 정보(마스킹)
    private String bankInfo;          // 은행 정보(마스킹)
    private String phoneInfo;         // 폰 정보(마스킹)
    private LocalDateTime paidAt;     // 결제 시각
    private LocalDateTime cancelledAt;// 취소 시각
    private String failReason;        // 결제 실패 사유
    private String cancelReason;      // 결제 취소 사유
    private LocalDateTime createdAt;  // 생성일
    private LocalDateTime updatedAt;  // 수정일

    // 조인 컬럼(필요시)
    private String userName;
    private String accommodationTitle;
    private String roomName;
}
