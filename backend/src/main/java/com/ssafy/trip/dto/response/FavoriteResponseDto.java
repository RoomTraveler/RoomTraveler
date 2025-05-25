package com.ssafy.trip.dto.response;

// import com.ssafy.trip.dto.AccommodationDto; // 숙소 정보 DTO (기존에 있다고 가정)
import com.ssafy.trip.dto.FavoriteDto; // 기본 찜 정보 DTO
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "찜 목록 응답 DTO (숙소 정보 포함)")
public class FavoriteResponseDto {

    @Schema(description = "찜 ID")
    private Long favoriteId;

    @Schema(description = "사용자 ID")
    private Long userId;

    @Schema(description = "찜한 날짜")
    private LocalDateTime createdAt;
    
    // 포함될 숙소 정보
    @Schema(description = "찜한 숙소 정보")
    private SimpleAccommodationDto accommodation; // 숙소 전체 정보 대신 간략한 정보만 포함하는 DTO

    // 기본 생성자
    public FavoriteResponseDto() {}

    // 생성자 등
    public FavoriteResponseDto(FavoriteDto favorite, SimpleAccommodationDto accommodation) {
        if (favorite != null) {
            this.favoriteId = favorite.getFavoriteId();
            this.userId = favorite.getUserId();
            this.createdAt = favorite.getCreatedAt();
        }
        this.accommodation = accommodation;
    }
    
    // 내부 클래스로 SimpleAccommodationDto 정의 (또는 별도 파일로 생성)
    // 이 DTO는 숙소 목록에서 각 항목을 간략히 보여줄 때 필요한 정보만 담습니다.
    @Schema(description = "간략한 숙소 정보 DTO")
    public static class SimpleAccommodationDto {
        @Schema(description = "숙소 ID")
        private Long accommodationId;
        @Schema(description = "숙소명")
        private String title;
        @Schema(description = "숙소 주소")
        private String address;
        @Schema(description = "숙소 대표 이미지 URL")
        private String mainImageUrl;
        @Schema(description = "숙소 유형")
        private String accommodationType; // 예: HOTEL, PENSION 등
        // 필요하다면 평점, 가격 등 추가
        
        // MyBatis resultMap에서 사용하기 위한 기본 생성자
        public SimpleAccommodationDto() {}

        public SimpleAccommodationDto(Long accommodationId, String title, String address, String mainImageUrl, String accommodationType) {
            this.accommodationId = accommodationId;
            this.title = title;
            this.address = address;
            this.mainImageUrl = mainImageUrl;
            this.accommodationType = accommodationType;
        }

        // Getters
        public Long getAccommodationId() { return accommodationId; }
        public String getTitle() { return title; }
        public String getAddress() { return address; }
        public String getMainImageUrl() { return mainImageUrl; }
        public String getAccommodationType() { return accommodationType; }

        // Setters (MyBatis가 값을 채울 수 있도록 필요)
        public void setAccommodationId(Long accommodationId) { this.accommodationId = accommodationId; }
        public void setTitle(String title) { this.title = title; }
        public void setAddress(String address) { this.address = address; }
        public void setMainImageUrl(String mainImageUrl) { this.mainImageUrl = mainImageUrl; }
        public void setAccommodationType(String accommodationType) { this.accommodationType = accommodationType; }
    }


    // Getters
    public Long getFavoriteId() { return favoriteId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public SimpleAccommodationDto getAccommodation() { return accommodation; }

    // Setters (MyBatis가 값을 채울 수 있도록 필요)
    public void setFavoriteId(Long favoriteId) { this.favoriteId = favoriteId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setAccommodation(SimpleAccommodationDto accommodation) { this.accommodation = accommodation; }
} 