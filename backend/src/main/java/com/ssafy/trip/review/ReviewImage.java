package com.ssafy.trip.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 리뷰 이미지 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImage {
    private Long imageId;           // 이미지 ID (PK)
    private Long reviewId;          // 리뷰 ID (FK)
    private String imageUrl;        // 이미지 URL (S3 경로)
    private String caption;         // 이미지 캡션
    private Boolean isThumbnail;     // 썸네일 여부 (선택적)
    private Integer sortOrder;       // 정렬 순서 (선택적)
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime updatedAt; // 수정 시간 (추가)
} 