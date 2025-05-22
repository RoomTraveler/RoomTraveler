package com.ssafy.trip.map;

import lombok.Data;

import java.util.List;

@Data
public class SelectedPlacesMessage {
    private Long squadId;
    private String messageType;
    private Long userId;
    private List<MapDTO.RegionTripRes> content;
}
