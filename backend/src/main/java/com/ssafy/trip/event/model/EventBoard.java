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
}
