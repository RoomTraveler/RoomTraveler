package com.ssafy.trip.host.controller;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.accommodation.service.ReservationService;
import com.ssafy.trip.host.service.HostService;
import com.ssafy.trip.host.model.AccommodationStats;
import com.ssafy.trip.host.model.DashboardStats;
import com.ssafy.trip.host.model.Host;
import com.ssafy.trip.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/host/dashboard")
@RequiredArgsConstructor
// @CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT}, allowCredentials = "true") // 전역 CORS 설정 사용을 위해 주석 처리 또는 삭제
public class HostDashboardController {
    private final AccommodationService accommodationService;
    private final ReservationService reservationService;
    private final HostService hostService;

    /**
     * 호스트 대시보드 메인 통계 조회
     */
    @GetMapping
    public ResponseEntity<?> getDashboardStats(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        Long hostUserId = userDetails.getUser().getUserId();
        String userRole = userDetails.getUser().getRole();

        if (!"HOST".equals(userRole) && !"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "대시보드 접근 권한이 없습니다."));
        }

        try {
            Host host = hostService.getHostByUserId(hostUserId);
            if (host == null && "HOST".equals(userRole)) {
                log.warn("Host role user (userId: {}) has no host entry.", hostUserId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "호스트 정보를 찾을 수 없습니다. 계정 상태를 확인하거나 관리자에게 문의하세요."));
            }

            Long actualHostIdToQuery = (host != null) ? host.getHostId() : null;
            if (actualHostIdToQuery == null && "HOST".equals(userRole)){
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "호스트 정보를 로드할 수 없습니다."));
            }

            if (!"HOST".equals(userRole)) {
                 log.info("Admin user accessing dashboard, hostId for stats will be based on ADMIN's own potential host entry if any.");
            }

            List<Accommodation> accommodations = (actualHostIdToQuery != null) ? accommodationService.getAccommodationsByHostId(actualHostIdToQuery) : Collections.emptyList();
            List<Reservation> reservations = (actualHostIdToQuery != null) ? reservationService.getReservationsByHostId(actualHostIdToQuery, userRole) : Collections.emptyList();

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
                    .mapToDouble(r -> r.getTotalPrice() != null ? r.getTotalPrice().doubleValue() : 0.0).sum();
            stats.setTotalRevenue(totalRevenue);

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
                        .mapToDouble(r -> r.getTotalPrice() != null ? r.getTotalPrice().doubleValue() : 0.0).sum();
                monthlyReservations.put(key, cnt);
                monthlyRevenue.put(key, rev);
            }
            stats.setMonthlyReservations(monthlyReservations);
            stats.setMonthlyRevenue(monthlyRevenue);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching dashboard stats for user {}: {}", hostUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "대시보드 통계 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 특정 숙소 통계 조회
     */
    @GetMapping("/accommodation/{accommodationId}")
    public ResponseEntity<?> getAccommodationStats(@PathVariable Long accommodationId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        Long authUserHostId = userDetails.getUser().getUserId();
        String userRole = userDetails.getUser().getRole();

        if (!"HOST".equals(userRole) && !"ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "접근 권한이 없습니다."));
        }

        try {
            Accommodation acc = accommodationService.getAccommodationById(accommodationId);
            if (acc == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "숙소 정보를 찾을 수 없습니다."));
            }

            Host accommodationHostOwnerInfo = hostService.getHostById(acc.getHostId());
            if (accommodationHostOwnerInfo == null) {
                 return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "숙소의 호스트 정보를 찾는 중 오류 발생"));
            }

            boolean isOwner = accommodationHostOwnerInfo.getUserId().equals(authUserHostId);

            if (!isOwner && !"ADMIN".equals(userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "해당 숙소 통계에 대한 접근 권한이 없습니다."));
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
                    .mapToDouble(r -> r.getTotalPrice() != null ? r.getTotalPrice().doubleValue() : 0.0).sum();
            stats.setTotalRevenue(totalRevenue);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error fetching accommodation stats for accommodationId {}: {}", accommodationId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "숙소 통계 조회 중 오류 발생: " + e.getMessage()));
        }
    }
}