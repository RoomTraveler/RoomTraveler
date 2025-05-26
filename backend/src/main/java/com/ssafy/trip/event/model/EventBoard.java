package com.ssafy.trip.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventBoard {
    private Long eventId;
    private String title;
    private String content;         // <img src="..."> 태그 포함
    private String writer;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 이미지 리스트 (연관)
    private List<EventBoardImg> images;

    // 대표 이미지 URL을 반환하는 메소드
    // 1. isThumbnail=true인 이미지를 찾아서 반환
    // 2. 썸네일이 없으면 리스트의 첫 번째 이미지 URL 반환
    // 3. 이미지 자체가 없으면 null 반환
    public String getMainImageUrl() {
        if (this.images != null && !this.images.isEmpty()) {
            // 1. 썸네일 이미지 찾기
            for (EventBoardImg img : this.images) {
                if (img != null && Boolean.TRUE.equals(img.getIsThumbnail()) && img.getImgUrl() != null) {
                    return img.getImgUrl();
                }
            }
            // 2. 썸네일이 없으면 첫 번째 이미지 반환 (null이 아니고 URL이 있는 경우)
            EventBoardImg firstImage = this.images.get(0);
            if (firstImage != null && firstImage.getImgUrl() != null) {
                return firstImage.getImgUrl();
            }
        }
        return null; // 적절한 이미지가 없는 경우
    }
}
