package com.ssafy.trip.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 숙소 리뷰 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    private Long reviewId;           // 리뷰 ID
    private Long accommodationId;    // 숙소 ID
    private Long userId;             // 사용자 ID
    private Long reservationId;      // 예약 ID
    private Integer rating;          // 평점 (1-5)
    private String title;            // 리뷰 제목
    private String content;          // 리뷰 내용
    private LocalDateTime stayDate;  // 숙박 날짜
    private Boolean isVerified;      // 실제 숙박 여부 확인
    private String status;           // 상태 (ACTIVE, REPORTED, REMOVED)
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime updatedAt; // 수정 시간

    // 추가 필드 - 조인 시 사용
    private String username;         // 사용자 이름
    private String accommodationTitle; // 숙소 이름
    private String userProfileImage; // 사용자 프로필 이미지
}
