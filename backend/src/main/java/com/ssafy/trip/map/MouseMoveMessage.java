package com.ssafy.trip.map;

import lombok.Data;

@Data
public class MouseMoveMessage {
    private String squadId;
    private String messageType;
    private String userId;
    private MapDTO.LatLng content;
    private int position;
}
