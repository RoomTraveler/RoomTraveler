package com.ssafy.trip.host;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.accommodation.service.ReservationService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 호스트 대시보드 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/host/dashboard")
public class HostDashboardController {
    private final AccommodationService accommodationService;
    private final ReservationService reservationService;
    private final HostService hostService;

    /**
     * 호스트 대시보드 메인 페이지로 이동합니다.
     */
    @GetMapping({"", "/"})
    public String dashboard(HttpSession session, Model model) {
        // 로그인 및 호스트/관리자 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || (!"HOST".equals(role) && !"ADMIN".equals(role))) {
            return "redirect:/user/login-form";
        }

        try {
            // 호스트 정보 조회
            Host host = hostService.getHostById(hostId);
            model.addAttribute("host", host);

            // 숙소 목록 조회
            List<Accommodation> accommodations = accommodationService.getAccommodationsByHostId(hostId);
            model.addAttribute("accommodations", accommodations);
            model.addAttribute("accommodationCount", accommodations.size());

            // 예약 통계 조회
            List<Reservation> reservations = reservationService.getReservationsByHostId(hostId);
            model.addAttribute("reservations", reservations);

            // 총 예약 수
            model.addAttribute("totalReservations", reservations.size());

            // 확정된 예약 수
            long confirmedReservations = reservations.stream()
                    .filter(r -> "CONFIRMED".equals(r.getStatus()))
                    .count();
            model.addAttribute("confirmedReservations", confirmedReservations);

            // 취소된 예약 수
            long cancelledReservations = reservations.stream()
                    .filter(r -> "CANCELLED".equals(r.getStatus()))
                    .count();
            model.addAttribute("cancelledReservations", cancelledReservations);

            // 완료된 예약 수
            long completedReservations = reservations.stream()
                    .filter(r -> "COMPLETED".equals(r.getStatus()))
                    .count();
            model.addAttribute("completedReservations", completedReservations);

            // 대기 중인 예약 수
            long pendingReservations = reservations.stream()
                    .filter(r -> "PENDING".equals(r.getStatus()))
                    .count();
            model.addAttribute("pendingReservations", pendingReservations);

            // 총 수익
            double totalRevenue = reservations.stream()
                    .filter(r -> "CONFIRMED".equals(r.getStatus()) || "COMPLETED".equals(r.getStatus()))
                    .mapToDouble(r -> r.getTotalPrice().doubleValue())
                    .sum();
            model.addAttribute("totalRevenue", totalRevenue);

            // 월별 예약 통계 (최근 6개월)
            Map<String, Long> monthlyReservations = new HashMap<>();
            Map<String, Double> monthlyRevenue = new HashMap<>();

            LocalDate now = LocalDate.now();
            DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");

            for (int i = 0; i < 6; i++) {
                LocalDate monthDate = now.minusMonths(i);
                String monthKey = monthDate.format(monthFormatter);

                final String currentMonth = monthKey;
                long reservationsInMonth = reservations.stream()
                        .filter(r -> {
                            if (r.getCreatedAt() == null) return false;
                            return r.getCreatedAt().getYear() == monthDate.getYear() && 
                                   r.getCreatedAt().getMonthValue() == monthDate.getMonthValue();
                        })
                        .count();

                double revenueInMonth = reservations.stream()
                        .filter(r -> {
                            if (r.getCreatedAt() == null) return false;
                            return r.getCreatedAt().getYear() == monthDate.getYear() && 
                                   r.getCreatedAt().getMonthValue() == monthDate.getMonthValue();
                        })
                        .filter(r -> "CONFIRMED".equals(r.getStatus()) || "COMPLETED".equals(r.getStatus()))
                        .mapToDouble(r -> r.getTotalPrice().doubleValue())
                        .sum();

                monthlyReservations.put(monthKey, reservationsInMonth);
                monthlyRevenue.put(monthKey, revenueInMonth);
            }

            model.addAttribute("monthlyReservations", monthlyReservations);
            model.addAttribute("monthlyRevenue", monthlyRevenue);

            return "host/dashboard";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    /**
     * 숙소별 통계 페이지로 이동합니다.
     */
    @GetMapping("/accommodation")
    public String accommodationStats(
            @RequestParam Long accommodationId,
            HttpSession session, 
            Model model) {

        // 로그인 및 호스트/관리자 권한 확인
        Long hostId = (Long) session.getAttribute("userId");
        String role = (String) session.getAttribute("role");

        if (hostId == null || (!"HOST".equals(role) && !"ADMIN".equals(role))) {
            return "redirect:/user/login-form";
        }

        try {
            // 숙소 정보 조회
            Accommodation accommodation = accommodationService.getAccommodationById(accommodationId);

            // 해당 숙소의 호스트인지 확인
            if (!accommodation.getHostId().equals(hostId) && !"ADMIN".equals(role)) {
                model.addAttribute("error", "해당 숙소의 통계를 볼 권한이 없습니다.");
                return "error";
            }

            model.addAttribute("accommodation", accommodation);

            // 해당 숙소의 예약 목록 조회
            List<Reservation> reservations = reservationService.getReservationsByAccommodationId(accommodationId);
            model.addAttribute("reservations", reservations);

            // 총 예약 수
            model.addAttribute("totalReservations", reservations.size());

            // 확정된 예약 수
            long confirmedReservations = reservations.stream()
                    .filter(r -> "CONFIRMED".equals(r.getStatus()))
                    .count();
            model.addAttribute("confirmedReservations", confirmedReservations);

            // 취소된 예약 수
            long cancelledReservations = reservations.stream()
                    .filter(r -> "CANCELLED".equals(r.getStatus()))
                    .count();
            model.addAttribute("cancelledReservations", cancelledReservations);

            // 완료된 예약 수
            long completedReservations = reservations.stream()
                    .filter(r -> "COMPLETED".equals(r.getStatus()))
                    .count();
            model.addAttribute("completedReservations", completedReservations);

            // 대기 중인 예약 수
            long pendingReservations = reservations.stream()
                    .filter(r -> "PENDING".equals(r.getStatus()))
                    .count();
            model.addAttribute("pendingReservations", pendingReservations);

            // 총 수익
            double totalRevenue = reservations.stream()
                    .filter(r -> "CONFIRMED".equals(r.getStatus()) || "COMPLETED".equals(r.getStatus()))
                    .mapToDouble(r -> r.getTotalPrice().doubleValue())
                    .sum();
            model.addAttribute("totalRevenue", totalRevenue);

            return "host/accommodation-stats";
        } catch (SQLException e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
