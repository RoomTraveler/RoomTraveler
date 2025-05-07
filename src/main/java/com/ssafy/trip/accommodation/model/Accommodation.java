package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 숙소 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accommodation {
    private Long accommodationId;     // 숙소 ID
    private Long hostId;              // 호스트 ID
    private String title;             // 숙소 이름
    private String description;       // 숙소 설명
    private String address;           // 주소
    private Integer sidoCode;         // 시도 코드
    private Integer gugunCode;        // 구군 코드
    private Double latitude;          // 위도
    private Double longitude;         // 경도
    private String accommodationType; // 숙소 유형
    private String phone;             // 전화번호
    private String email;             // 이메일
    private String website;           // 웹사이트
    private LocalTime checkInTime;    // 체크인 시간
    private LocalTime checkOutTime;   // 체크아웃 시간
    private String amenities;         // 편의시설
    private String status;            // 상태 (ACTIVE, INACTIVE, PENDING_REVIEW)
    private LocalDateTime createdAt;  // 생성 시간
    private LocalDateTime updatedAt;  // 수정 시간

    // 추가 필드 - 조인 시 사용
    private String sidoName;          // 시도 이름
    private String gugunName;         // 구군 이름
    private String hostName;          // 호스트 이름
    private String mainImageUrl;      // 대표 이미지 URL
}
