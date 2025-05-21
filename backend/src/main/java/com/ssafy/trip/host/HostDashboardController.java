package com.ssafy.trip.host;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.accommodation.service.ReservationService;
import com.ssafy.trip.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/host/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
public class HostDashboardController {
    private final AccommodationService accommodationService;
    private final ReservationService reservationService;
    private final HostService hostService;

    /**
     * 호스트 대시보드 메인 통계 조회
     */
    @GetMapping
    public ResponseEntity<?> getDashboardStats() throws SQLException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        Long hostId = user.getUser().getUserId();
        String role = user.getUser().getRole();
        if (!"HOST".equals(role) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "권한이 없습니다."));
        }
        try {
            Host host = hostService.getHostById(hostId);
            List<Accommodation> accommodations = accommodationService.getAccommodationsByHostId(hostId);
            List<Reservation> reservations = reservationService.getReservationsByHostId(hostId, null);

            // 통계 생성
            DashboardStats stats = new DashboardStats();
            stats.setHost(host);
            stats.setAccommodationCount(accommodations.size());
            stats.setTotalReservations(reservations.size());
            stats.setConfirmedReservations(reservations.stream().filter(r -> "CONFIRMED".equals(r.getStatus())).count());
            stats.setCancelledReservations(reservations.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count());
            stats.setCompletedReservations(reservations.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count());
            stats.setPendingReservations(reservations.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
            double totalRevenue = reservations.stream()
                    .filter(r -> Arrays.asList("CONFIRMED", "COMPLETED").contains(r.getStatus()))
                    .mapToDouble(r -> r.getTotalPrice().doubleValue()).sum();
            stats.setTotalRevenue(totalRevenue);

            // 지난 6개월 월별 통계
            Map<String, Long> monthlyReservations = new LinkedHashMap<>();
            Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
            LocalDate now = LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
            for (int i = 5; i >= 0; i--) {
                LocalDate m = now.minusMonths(i);
                String key = m.format(fmt);
                long cnt = reservations.stream()
                        .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().getYear() == m.getYear()
                                && r.getCreatedAt().getMonthValue() == m.getMonthValue())
                        .count();
                double rev = reservations.stream()
                        .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().getYear() == m.getYear()
                                && r.getCreatedAt().getMonthValue() == m.getMonthValue()
                                && Arrays.asList("CONFIRMED", "COMPLETED").contains(r.getStatus()))
                        .mapToDouble(r -> r.getTotalPrice().doubleValue()).sum();
                monthlyReservations.put(key, cnt);
                monthlyRevenue.put(key, rev);
            }
            stats.setMonthlyReservations(monthlyReservations);
            stats.setMonthlyRevenue(monthlyRevenue);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 특정 숙소 통계 조회
     */
    @GetMapping("/accommodation/{accommodationId}")
    public ResponseEntity<?> getAccommodationStats(@PathVariable Long accommodationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        Long authHostId = user.getUser().getUserId();
        String role = user.getUser().getRole();
        if (!"HOST".equals(role) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "권한이 없습니다."));
        }
        try {
            Accommodation acc = accommodationService.getAccommodationById(accommodationId);
            if (!acc.getHostId().equals(authHostId) && !"ADMIN".equals(role)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "권한이 없습니다."));
            }
            List<Reservation> reservations = reservationService.getReservationsByAccommodationId(accommodationId);
            AccommodationStats stats = new AccommodationStats();
            stats.setAccommodation(acc);
            stats.setTotalReservations(reservations.size());
            stats.setConfirmedReservations(reservations.stream().filter(r -> "CONFIRMED".equals(r.getStatus())).count());
            stats.setCancelledReservations(reservations.stream().filter(r -> "CANCELLED".equals(r.getStatus())).count());
            stats.setCompletedReservations(reservations.stream().filter(r -> "COMPLETED".equals(r.getStatus())).count());
            stats.setPendingReservations(reservations.stream().filter(r -> "PENDING".equals(r.getStatus())).count());
            double totalRevenue = reservations.stream()
                    .filter(r -> Arrays.asList("CONFIRMED", "COMPLETED").contains(r.getStatus()))
                    .mapToDouble(r -> r.getTotalPrice().doubleValue()).sum();
            stats.setTotalRevenue(totalRevenue);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}