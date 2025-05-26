package com.ssafy.trip.review.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
public class ReviewCreationRequestDto {

    @NotNull(message = "숙소 ID는 필수입니다.")
    private Long accommodationId;

    @NotNull(message = "예약 ID는 리뷰 작성에 필수입니다.")
    private Long reservationId; // 결제 기반 리뷰 권한용

    @NotNull(message = "평점은 필수입니다.")
    private Integer rating; // 1-5

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    @Size(max = 2000, message = "리뷰 내용은 2000자를 초과할 수 없습니다.")
    private String content;

    // MultipartFile은 controller에서 직접 받아서 service로 넘기는 것을 권장
    // DTO에 직접 포함하기보다, 서비스 메서드 파라미터로 List<MultipartFile> images를 추가
    // private List<MultipartFile> imageFiles; 

    // 필요시 다른 필드 추가 (예: title)
    private String title;
} 