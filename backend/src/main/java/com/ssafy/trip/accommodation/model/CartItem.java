package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 장바구니 아이템 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
    private Long cartItemId;         // 장바구니 아이템 ID
    private Long cartId;             // 장바구니 ID
    private Long roomId;             // 객실 ID
    private LocalDate checkInDate;   // 체크인 날짜
    private LocalDate checkOutDate;  // 체크아웃 날짜
    private Integer guestCount;      // 투숙객 수
    private BigDecimal price;        // 가격
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime updatedAt; // 수정 시간
    
    // 추가 필드 - 조인 시 사용
    private String roomName;          // 객실 이름
    private String accommodationTitle; // 숙소 이름
    private Long accommodationId;     // 숙소 ID
    private String imageUrl;          // 이미지 URL
    
    /**
     * 숙박 일수를 계산합니다.
     */
    public int getNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }
        return (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }
    
    /**
     * 1박당 가격을 계산합니다.
     */
    public BigDecimal getPricePerNight() {
        int nights = getNights();
        if (nights <= 0 || price == null) {
            return BigDecimal.ZERO;
        }
        return price.divide(BigDecimal.valueOf(nights), 2, BigDecimal.ROUND_HALF_UP);
    }
}