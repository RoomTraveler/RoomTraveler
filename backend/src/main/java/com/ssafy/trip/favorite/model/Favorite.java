package com.ssafy.trip.favorite.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Favorite {
    private Long favoriteId;
    private Long userId;
    private Long accommodationId;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private String accommodationTitle;
    private String accommodationAddress;
    private String mainImageUrl;
}
