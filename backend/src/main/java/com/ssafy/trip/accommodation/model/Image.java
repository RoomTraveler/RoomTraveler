package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이미지 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {
    private Long imageId;             // 이미지 ID
    private Long referenceId;         // 참조 ID (숙소 ID 또는 객실 ID)
    private String referenceType;     // 참조 타입 (ACCOMMODATION, ROOM)
    private Long accommodationId;     // 숙소 ID (외래 키)
    private Long roomId;              // 객실 ID (외래 키)
    private String imageUrl;          // 이미지 URL
    private String caption;           // 이미지 설명
    private Boolean isMain;           // 대표 이미지 여부
    private LocalDateTime createdAt;  // 생성 시간

    // 추가 필드 - 조인 시 사용
    private String accommodationTitle; // 숙소 이름 (referenceType이 ACCOMMODATION인 경우)
    private String roomName;          // 객실 이름 (referenceType이 ROOM인 경우)
}
