package com.ssafy.trip.review.model;

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
    private Long reviewImageId;  // 리뷰 이미지 ID
    private Long reviewId;       // 리뷰 ID (FK)
    private String imageUrl;     // S3에 업로드된 이미지 URL
    private Integer uploadOrder; // 이미지 표시 순서 (0부터 시작)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 