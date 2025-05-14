package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 예약 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation {
    private Long reservationId;       // 예약 ID
    private Long userId;              // 사용자 ID
    private Long roomId;              // 객실 ID
    private LocalDate checkInDate;    // 체크인 날짜
    private LocalDate checkOutDate;   // 체크아웃 날짜
    private Integer guestCount;       // 투숙객 수
    private BigDecimal totalPrice;    // 총 가격
    private String status;            // 상태 (PENDING, CONFIRMED, CANCELLED, COMPLETED)
    private String paymentStatus;     // 결제 상태 (UNPAID, PAID, REFUNDED)
    private String specialRequests;   // 특별 요청 사항
    private LocalDateTime createdAt;  // 생성 시간
    private LocalDateTime updatedAt;  // 수정 시간
    
    // 추가 필드 - 조인 시 사용
    private String userName;          // 사용자 이름
    private String userEmail;         // 사용자 이메일
    private String userPhone;         // 사용자 전화번호
    private String roomName;          // 객실 이름
    private String accommodationTitle; // 숙소 이름
    private Long accommodationId;     // 숙소 ID
    private String hostName;          // 호스트 이름
    private Integer nights;           // 숙박 일수
}