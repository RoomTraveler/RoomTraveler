package com.ssafy.trip.accommodation.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class AccommodationRequestDto {

    @NotBlank(message = "숙소명은 필수입니다.")
    private String title;

    @NotBlank(message = "숙소 유형은 필수입니다.")
    private String accommodationType; // 예: MOTEL, HOTEL, PENSION 등 Enum 또는 String 관리

    private String description;

    @NotBlank(message = "주소는 필수입니다.")
    private String address; // 전체 주소 (기본 주소 + 상세 주소 합쳐진 형태)

    @NotBlank(message = "시/도 코드는 필수입니다.")
    private String sidoCode;

    @NotBlank(message = "시/군/구 코드는 필수입니다.")
    private String gugunCode;

    private Double latitude;
    private Double longitude;

    @NotNull(message = "체크인 시간은 필수입니다.")
    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$", message = "체크인 시간 형식이 올바르지 않습니다. (HH:mm:ss)")
    private String checkInTime;

    @NotNull(message = "체크아웃 시간은 필수입니다.")
    @Pattern(regexp = "^([01]\\d|2[0-3]):([0-5]\\d):([0-5]\\d)$", message = "체크아웃 시간 형식이 올바르지 않습니다. (HH:mm:ss)")
    private String checkOutTime;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phone;

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    private String website;

    private String amenities; // JSON 형태의 문자열로 받음 ["WIFI", "PARKING"]

    // 이미지 관련 필드
    private MultipartFile mainImageFile; // 새로 업로드되는 대표 이미지
    private List<MultipartFile> imageFiles; // 새로 업로드되는 추가 이미지들

    // 수정 시 사용될 필드
    private Long mainImageId; // 기존 대표 이미지 ID (새 대표 이미지 파일 없고, 기존 대표 이미지를 계속 사용할 경우)
    private String deletedImageIds; // JSON 배열 형태의 삭제될 기존 이미지 ID 목록 "[1,2,3]"

    private Long hostId; // 호스트 ID 추가
} 