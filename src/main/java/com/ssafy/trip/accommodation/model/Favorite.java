package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사용자의 즐겨찾기(찜) 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {
    private Long favoriteId;           // 즐겨찾기 ID
    private Long userId;               // 사용자 ID
    private Long accommodationId;      // 숙소 ID
    private LocalDateTime createdAt;   // 생성 시간
    
    // 추가 필드 - 조인 시 사용
    private String accommodationTitle; // 숙소 이름
    private String accommodationAddress; // 숙소 주소
    private String mainImageUrl;       // 숙소 대표 이미지 URL
}