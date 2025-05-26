package com.ssafy.trip.ai;

import lombok.Data;

@Data
public class Place {
    private String name;
    private String address;
    private String contentType;
    private double latitude;
    private double longitude;
}
