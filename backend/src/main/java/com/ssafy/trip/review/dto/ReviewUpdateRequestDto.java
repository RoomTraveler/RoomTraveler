package com.ssafy.trip.review.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
public class ReviewUpdateRequestDto {

    @NotNull(message = "평점은 필수입니다.")
    private Integer rating; // 1-5

    @Size(max = 2000, message = "리뷰 내용은 2000자를 초과할 수 없습니다.")
    private String content;

    private String title;

    // 수정 시 기존 이미지 중 삭제할 이미지 ID 목록
    private List<Long> deletedImageIds;

    // 새로 추가할 이미지 파일 목록은 ReviewCreationRequestDto와 마찬가지로 controller에서 별도 파라미터로 처리
} 