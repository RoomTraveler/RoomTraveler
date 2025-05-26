package com.ssafy.trip.payment.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationCreationDto {
    private Long roomId;
    private Long accommodationId; // 객실의 숙소 ID
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private BigDecimal totalPrice; // 이미 계산된 각 예약 건의 가격
    private String roomName; // 주문명 생성 등에 사용 (선택적)
} 