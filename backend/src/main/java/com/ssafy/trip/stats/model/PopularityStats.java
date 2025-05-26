package com.ssafy.trip.stats.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopularityStats {
    private Long id;

    private String statsType; // REGION, ACCOMMODATION, TYPE, TYPE_REGION
    private String regionType; // sido, gugun
    private Integer regionCode;
    private String sidoName;
    private String sidoImgUrl;
    private Long accommodationId;
    private String accommodationTitle;
    private String accommodationMainImageUrl;
    private String accommodationType; // HOTEL, PENSION 등
    private String periodType; // DAILY, WEEKLY, MONTHLY
    private String periodValue; // 집계 기준일자 (시간 제외)

    private Integer reservationCount;
    private Integer viewCount;
    private Integer reviewCount;
    private Double popularityScore;
    private Integer topNRank;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 