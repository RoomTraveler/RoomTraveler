package com.ssafy.trip.event.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventBoardImg {
    private Long imgId;
    private Long eventId;
    private String imgUrl;
    private LocalDateTime createdAt;

}
