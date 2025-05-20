package com.ssafy.trip.host;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Data
public class DashboardStats {
    private Host host;
    private int accommodationCount;
    private long totalReservations;
    private long confirmedReservations;
    private long cancelledReservations;
    private long completedReservations;
    private long pendingReservations;
    private double totalRevenue;
    private Map<String, Long> monthlyReservations;
    private Map<String, Double> monthlyRevenue;
}
