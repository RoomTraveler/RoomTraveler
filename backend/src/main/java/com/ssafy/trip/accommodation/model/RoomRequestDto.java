package com.ssafy.trip.accommodation.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RoomRequestDto {
    private String name;
    private String description;
    private BigDecimal price;
    private Integer capacity;
    private Integer maxCapacity;
    private Integer roomCount; // 해당 타입의 객실 수 (예: 디럭스룸 5개)
    private String roomType; // 예: 더블룸, 트윈룸, 스위트룸 등 숙소 내부에서의 객실 분류
    private String amenities; // JSON string for amenities e.g., "["WIFI","TV"]" or comma-separated "WIFI,TV"
    private String status; // 예: AVAILABLE, UNDER_MAINTENANCE

    // 이미지 관련
    private MultipartFile mainImageFile; // 대표 이미지 파일
    private List<MultipartFile> imageFiles; // 추가 이미지 파일 목록
    private String deletedImageIds; // 삭제할 기존 이미지 ID 목록 (JSON 배열 문자열 형태, 예: "[1,2,3]")
    private Long mainImageId; // 수정 시, 새 파일 없이 기존 대표 이미지를 유지할 경우 그 ID
} 