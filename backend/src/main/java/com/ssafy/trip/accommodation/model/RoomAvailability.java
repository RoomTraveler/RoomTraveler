package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 객실 가용성 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomAvailability {
    private Long availabilityId;      // 가용성 ID
    private Long roomId;              // 객실 ID
    private LocalDate date;           // 날짜
    private Integer availableCount;   // 가용 객실 수
    private BigDecimal price;         // 특별 가격 (null인 경우 기본 가격 적용)
    private LocalDateTime createdAt;  // 생성 시간
    private LocalDateTime updatedAt;  // 수정 시간
    
    // 추가 필드 - 조인 시 사용
    private String roomName;          // 객실 이름
    private Long accommodationId;     // 숙소 ID
    private String accommodationTitle; // 숙소 이름
    private BigDecimal basePrice;     // 기본 가격
}