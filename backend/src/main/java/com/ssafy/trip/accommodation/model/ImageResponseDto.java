package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageResponseDto {
    private Long imageId;
    private String imageUrl;
    private boolean isMain;

    public static ImageResponseDto fromEntity(Image image) {
        return ImageResponseDto.builder()
                .imageId(image.getImageId())
                .imageUrl(image.getImageUrl())
                .isMain(image.getIsMain() != null ? image.getIsMain() : false)
                .build();
    }

    // 만약 AccommodationImage 엔티티가 있다면, 변환 메서드 추가 가능
    // public static ImageResponseDto fromEntity(AccommodationImage image) {
    //     return ImageResponseDto.builder()
    //             .imageId(image.getId())
    //             .imageUrl(image.getImageUrl())
    //             .isMain(image.isMain())
    //             .build();
    // }
} 