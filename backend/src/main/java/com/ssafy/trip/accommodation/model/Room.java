package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 객실 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {
    private Long roomId;              // 객실 ID
    private Long accommodationId;     // 숙소 ID
    private String name;              // 객실 이름
    private String description;       // 객실 설명
    private BigDecimal price;         // 최종 판매가
    private Integer capacity;         // 수용 인원
    private Integer roomCount;        // 객실 수
    private BigDecimal roomSize;      // 객실 크기
    private String roomType;          // 객실 유형
    private String bedType;           // 침대 유형
    private Integer bathroomCount;    // 욕실 수
    private String amenities;         // 편의시설
    private String status;            // 상태 (AVAILABLE, UNAVAILABLE)
    private LocalDateTime createdAt;  // 생성 시간
    private LocalDateTime updatedAt;  // 수정 시간

    // 추가 필드 - 조인 시 사용
    private String accommodationTitle; // 숙소 이름
    private String mainImageUrl;       // 대표 이미지 URL
    private List<String> imageUrls;    // 이미지 URL 목록
    private Boolean isAvailable;       // 예약 가능 여부
    private BigDecimal currentPrice;   // 현재 가격 (특별 가격이 있는 경우)
    private Integer minAvailableCount; // 특정 기간 동안의 최소 가용 객실 수

    // 사용자 요청에 따른 새 필드 (RoomCard.vue 및 RoomInfoDisplay.vue에서 사용)
    private BigDecimal originalPrice;   // 원래 가격 (할인 전)
    private Double discountRate;        // 할인율 (예: 0.11은 11%)
    private String cancellationPolicy;  // 취소 및 환불 정책

    // 매퍼와 일치시키기 위한 pricePerNight 게터
    public BigDecimal getPricePerNight() {
        return price;
    }

    // 매퍼와 일치시키기 위한 pricePerNight 세터
    public void setPricePerNight(BigDecimal pricePerNight) {
        this.price = pricePerNight;
    }
}
