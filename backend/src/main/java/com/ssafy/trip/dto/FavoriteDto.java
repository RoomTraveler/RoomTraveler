package com.ssafy.trip.dto;

import java.time.LocalDateTime;

public class FavoriteDto {
    private Long favoriteId;
    private Long userId;
    private Long accommodationId;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    // 기본 생성자
    public FavoriteDto() {}

    // 모든 필드를 포함하는 생성자 (필요에 따라 추가)
    public FavoriteDto(Long favoriteId, Long userId, Long accommodationId, LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.favoriteId = favoriteId;
        this.userId = userId;
        this.accommodationId = accommodationId;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    // Getters and Setters
    public Long getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(Long favoriteId) {
        this.favoriteId = favoriteId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAccommodationId() {
        return accommodationId;
    }

    public void setAccommodationId(Long accommodationId) {
        this.accommodationId = accommodationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        return "FavoriteDto{" +
                "favoriteId=" + favoriteId +
                ", userId=" + userId +
                ", accommodationId=" + accommodationId +
                ", createdAt=" + createdAt +
                ", deletedAt=" + deletedAt +
                '}';
    }
} 