package com.ssafy.trip.dto.response;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Image; // Image 엔티티 임포트
import com.fasterxml.jackson.core.type.TypeReference; // JSON 파싱용
import com.fasterxml.jackson.databind.ObjectMapper; // JSON 파싱용
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class AccommodationResponseDto {
    private Long id;
    private String title;
    private String accommodationType;
    private String description;
    private String address;
    private String sidoCode;
    private String sidoName; // 시도 이름 추가
    private String gugunCode;
    private String gugunName; // 구군 이름 추가
    private Double latitude;
    private Double longitude;
    private String checkInTime;  // HH:mm:ss 형식 문자열
    private String checkOutTime; // HH:mm:ss 형식 문자열
    private String phone;
    private String email;
    private String website;
    private List<String> amenities; // List<String>으로 변환된 편의시설
    private List<ImageResponseDto> images; // 대표 이미지 포함, isMain으로 구분
    private String status; // 숙소 상태 (예: PENDING, APPROVED, REJECTED)
    private Long hostId; // 호스트 ID

    public static AccommodationResponseDto fromEntity(Accommodation accommodation, List<Image> accommodationImages, ObjectMapper objectMapper) {
        List<String> amenitiesList = Collections.emptyList();
        if (accommodation.getAmenities() != null && !accommodation.getAmenities().isEmpty()) {
            try {
                // amenities가 JSON 배열 문자열("[\"WIFI\",\"PARKING\"]") 또는 콤마 구분 문자열("WIFI,PARKING")일 수 있음
                if (accommodation.getAmenities().startsWith("[")) { // JSON 배열 형태인 경우
                    amenitiesList = objectMapper.readValue(accommodation.getAmenities(), new TypeReference<List<String>>() {});
                } else { // 콤마 구분 형태인 경우
                    amenitiesList = List.of(accommodation.getAmenities().split(","));
                }
            } catch (Exception e) {
                log.error("Error parsing amenities: {}", accommodation.getAmenities(), e);
                // 기본값으로 빈 리스트 유지
            }
        }

        return AccommodationResponseDto.builder()
                .id(accommodation.getAccommodationId())
                .title(accommodation.getTitle())
                .accommodationType(accommodation.getAccommodationType()) 
                .description(accommodation.getDescription())
                .address(accommodation.getAddress())
                .sidoCode(accommodation.getSidoCode() != null ? String.valueOf(accommodation.getSidoCode()) : null)
                .sidoName(accommodation.getSidoName())   
                .gugunCode(accommodation.getGugunCode() != null ? String.valueOf(accommodation.getGugunCode()) : null) 
                .gugunName(accommodation.getGugunName()) 
                .latitude(accommodation.getLatitude())
                .longitude(accommodation.getLongitude())
                .checkInTime(accommodation.getCheckInTime() != null ? accommodation.getCheckInTime().toString() : null)
                .checkOutTime(accommodation.getCheckOutTime() != null ? accommodation.getCheckOutTime().toString() : null)
                .phone(accommodation.getPhoneNumber() != null ? accommodation.getPhoneNumber() : accommodation.getPhone()) // phoneNumber 우선, 없으면 phone
                .email(accommodation.getEmail())
                .website(accommodation.getWebsite())
                .amenities(amenitiesList) 
                .status(accommodation.getStatus() != null ? accommodation.getStatus().name() : null) 
                .hostId(accommodation.getHostId()) 
                .images(accommodationImages != null ? accommodationImages.stream()
                        .map(ImageResponseDto::fromEntity) // ImageResponseDto에 fromEntity 추가 필요
                        .collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }
} 