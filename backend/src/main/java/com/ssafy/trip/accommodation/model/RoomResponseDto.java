package com.ssafy.trip.accommodation.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class RoomResponseDto {
    private Long roomId;
    private Long accommodationId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer capacity;
    private Integer maxCapacity;
    private Integer roomCount;
    private String roomType;
    private List<String> amenities; // List<String>으로 변환된 편의시설
    private String status;
    private List<ImageResponseDto> images; // 대표 이미지 포함, isMain으로 구분

    public static RoomResponseDto fromEntity(Room room, List<Image> roomImages, ObjectMapper objectMapper) {
        List<String> amenitiesList = Collections.emptyList();
        if (room.getAmenities() != null && !room.getAmenities().isEmpty()) {
            try {
                if (room.getAmenities().startsWith("[")) { // JSON 배열 형태
                    amenitiesList = objectMapper.readValue(room.getAmenities(), new TypeReference<List<String>>() {});
                } else { // 콤마 구분 형태
                    amenitiesList = List.of(room.getAmenities().split(","));
                }
            } catch (Exception e) {
                log.error("Error parsing room amenities: {}", room.getAmenities(), e);
            }
        }

        return RoomResponseDto.builder()
                .roomId(room.getRoomId())
                .accommodationId(room.getAccommodationId())
                .name(room.getName())
                .description(room.getDescription())
                .price(room.getPrice())
                .capacity(room.getCapacity())
                .maxCapacity(room.getMaxCapacity())
                .roomCount(room.getRoomCount())
                .roomType(room.getRoomType())
                .amenities(amenitiesList)
                .status(room.getStatus())
                .images(roomImages != null ? roomImages.stream()
                        .map(ImageResponseDto::fromEntity)
                        .collect(Collectors.toList()) : Collections.emptyList())
                .build();
    }
} 