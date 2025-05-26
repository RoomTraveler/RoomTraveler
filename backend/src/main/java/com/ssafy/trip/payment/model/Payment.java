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
    private Long paymentId;
    private Long reservationId;
    private Long userId;
    private String merchantUid;       // 주문 ID (아임포트용)
    private String paymentMethod;
    private String paymentKey; // imp_uid 등
    private BigDecimal amount;
    private String currency;
    private String status;
    private String cardInfo;
    private String bankInfo;
    private String phoneInfo;
    private String pgProvider;
    private String pgTid;
    private String receiptUrl;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private String failReason;
    private String cancelReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 조인 컬럼
    private String userName;
    private String accommodationTitle;
    private String roomName;
}
