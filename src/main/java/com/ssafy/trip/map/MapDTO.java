// src/main/java/com/ssafy/trip/model/dto/MapDTO.java
package com.ssafy.trip.map;

import lombok.*;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public class MapDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanStoreDTO {
        private String id;
        private List<Integer> attractionIds;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegionTripResDto {
        private int no;
        private String title;
        private String image;
        private String addr1;
        private String addr2;
        private String tel;
        private double latitude;
        private double longitude;
        private String homepage;
        private String overview;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShortestPlanRequest {
        private RegionTripResDto startLocation;
        private List<RegionTripResDto> locations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Plan {
        private int planId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sido {
    	private int code;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Gugun {
        private int code;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentType {
        private int code;
        private String name;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatLng {
        private double latitude;
        private double longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MapBound {
        private LatLng southWest;
        private LatLng northEast;
    }
}
