package com.ssafy.trip.review.dto;

import com.ssafy.trip.review.model.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponseDto {
    private Long reviewId;
    private Long accommodationId;
    private String accommodationTitle; // 숙소명
    private Long userId;
    private String userNickname;      // 작성자 닉네임 (User 테이블에서 가져옴)
    private String userProfileImageUrl; // 작성자 프로필 이미지 URL (User 테이블에서 가져옴)
    private Long reservationId;      // 이 리뷰가 작성된 예약 ID
    private Integer rating;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ImageInfo> images; // 이미지 URL 및 순서 정보 리스트

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ImageInfo {
        private Long reviewImageId;
        private String imageUrl;
        private Integer uploadOrder;
    }

    // Review 엔티티를 ReviewResponseDto로 변환하는 정적 메서드 (필요시 Service에서 사용)
    public static ReviewResponseDto fromEntity(com.ssafy.trip.review.model.Review review, String accommodationTitle, String userNickname, String userProfileImageUrl, List<ImageInfo> imageInfos) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .accommodationId(review.getAccommodationId())
                .accommodationTitle(accommodationTitle) // 서비스 레이어에서 주입
                .userId(review.getUserId())
                .userNickname(userNickname)           // 서비스 레이어에서 주입
                .userProfileImageUrl(userProfileImageUrl) // 서비스 레이어에서 주입
                .reservationId(review.getReservationId())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .images(imageInfos) // 서비스 레이어에서 ReviewImage 리스트를 ImageInfo 리스트로 변환하여 주입
                .build();
    }
} 