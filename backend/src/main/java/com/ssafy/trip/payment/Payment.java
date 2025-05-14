package com.ssafy.trip.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    private Long paymentId;           // 결제 ID
    private Long reservationId;       // 예약 ID
    private Long userId;              // 사용자 ID
    private String paymentMethod;     // 결제 방법 (CARD, BANK_TRANSFER, PHONE)
    private String paymentKey;        // 결제 키 (외부 결제 시스템에서 제공하는 고유 키)
    private BigDecimal amount;        // 결제 금액
    private String currency;          // 통화 (KRW, USD 등)
    private String status;            // 결제 상태 (PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED)
    private String cardInfo;          // 카드 정보 (마스킹 처리된 정보)
    private String bankInfo;          // 은행 정보 (마스킹 처리된 정보)
    private String phoneInfo;         // 휴대폰 정보 (마스킹 처리된 정보)
    private LocalDateTime paidAt;     // 결제 시간
    private LocalDateTime cancelledAt; // 취소 시간
    private String failReason;        // 실패 사유
    private String cancelReason;      // 취소 사유
    private LocalDateTime createdAt;  // 생성 시간
    private LocalDateTime updatedAt;  // 수정 시간
    
    // 추가 필드 - 조인 시 사용
    private String userName;          // 사용자 이름
    private String accommodationTitle; // 숙소 이름
    private String roomName;          // 객실 이름
}