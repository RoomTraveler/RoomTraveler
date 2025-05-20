package com.ssafy.trip.host;

import com.ssafy.trip.accommodation.model.Accommodation;
import lombok.Data;

@Data
public class AccommodationStats {
    private Accommodation accommodation;
    private long totalReservations;
    private long confirmedReservations;
    private long cancelledReservations;
    private long completedReservations;
    private long pendingReservations;
    private double totalRevenue;
}
