package com.ssafy.trip.map;

import lombok.Data;

@Data
public class JoinMessage {
    private Long squadId;
    private String messageType;
    private Long userId;
    private String content;
}
