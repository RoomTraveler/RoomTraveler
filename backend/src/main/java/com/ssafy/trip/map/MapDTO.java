// src/main/java/com/ssafy/trip/model/dto/MapDTO.java
package com.ssafy.trip.map;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public class MapDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(title = "PlanStoreDTO(여행 계획 저장)", description = "유저의 여행 계획 정보")
    public static class PlanStoreDTO {
        @Schema(description = "유저 아이디", requiredMode = RequiredMode.REQUIRED)
        private Long userId;
        @Schema(description = "계획 아이디", requiredMode = RequiredMode.NOT_REQUIRED)
        private Long planId;
        @Schema(description = "계획 관광지", requiredMode = RequiredMode.REQUIRED)
        private List<Long> attractionIds;
        @Schema(description = "계획 공유 여부", requiredMode = RequiredMode.NOT_REQUIRED)
        private Long isShared;
        @Schema(description = "여행 일자", requiredMode = RequiredMode.NOT_REQUIRED)
        private LocalDate travelDate;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegionTripResDto {
        private int no;
        private String title;
        private String image;
        private String image2;
        private String addr1;
        private String addr2;
        private String tel;
        private double latitude;
        private double longitude;
        private int contentTypeId;
        private String homepage;
        private String overview;
        private int totalCount;
        private long likes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanAttractionDTO {
        private Long attractionId;
        private int order;
        private String title;
        private String imageUrl;
        private String contentType;
        private Double latitude;
        private Double longitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanDTO {
        private Long planId;
        private List<PlanAttractionDTO> planAttractions;
        private long likes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmailPlanDTO {
        private Long planId;
        private String email;
    }

//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    @Builder
//    public static class ShortestPlanRequest {
//        private RegionTripResDto startLocation;
//        private List<RegionTripResDto> locations;
//    }

//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class Sido {
//    	private int code;
//        private String name;
//    }
//
//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class Gugun {
//        private int code;
//        private String name;
//    }

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

//    @Data
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class TotalPage {
//        private int totalPages;
//    }
}
