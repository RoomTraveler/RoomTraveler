package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 리뷰 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    private Long reviewId;            // 리뷰 ID
    private Long reservationId;       // 예약 ID
    private Long userId;              // 사용자 ID
    private Long accommodationId;     // 숙소 ID
    private BigDecimal rating;        // 평점
    private String comment;           // 리뷰 내용
    private LocalDateTime createdAt;  // 생성 시간
    private LocalDateTime updatedAt;  // 수정 시간
    
    // 추가 필드 - 조인 시 사용
    private String userName;          // 사용자 이름
    private String accommodationTitle; // 숙소 이름
    private String hostName;          // 호스트 이름
    private LocalDateTime stayDate;   // 숙박 날짜
}