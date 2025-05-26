package com.ssafy.trip.ai;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TravelRoute {
    private List<Place> selectedPlaces;

    public TravelRoute() {
        selectedPlaces = new ArrayList<>();
    }
}
