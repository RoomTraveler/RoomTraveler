package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 숙소 정보를 담는 모델 클래스
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Accommodation {
    
    private Long id;
    private String name;
    private String location;
    private String address;
    private int price;
    private double rating;
    private int reviewCount;
    private String description;
    private String imageUrl;
    private List<String> amenities;
    private String hostId;
    private String hostName;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;
}