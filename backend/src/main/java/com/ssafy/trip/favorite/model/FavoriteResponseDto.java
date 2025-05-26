package com.ssafy.trip.favorite.model;

import com.ssafy.trip.accommodation.model.Accommodation;
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
    @Schema(description = "찜한 숙소 정보")
    private Accommodation accommodation; // <== 여기만 바꿔주세요

    public FavoriteResponseDto() {}

    public FavoriteResponseDto(Long favoriteId, Long userId, LocalDateTime createdAt, Accommodation accommodation) {
        this.favoriteId = favoriteId;
        this.userId = userId;
        this.createdAt = createdAt;
        this.accommodation = accommodation;
    }
    public Long getFavoriteId() { return favoriteId; }
    public void setFavoriteId(Long favoriteId) { this.favoriteId = favoriteId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Accommodation getAccommodation() { return accommodation; }
    public void setAccommodation(Accommodation accommodation) { this.accommodation = accommodation; }
}
