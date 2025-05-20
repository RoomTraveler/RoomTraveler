package com.ssafy.trip.host;

import com.ssafy.trip.security.CustomUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Reservation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.accommodation.service.ReservationService;
import com.ssafy.trip.review.Review;
import com.ssafy.trip.review.ReviewService;

import lombok.RequiredArgsConstructor;

/**
 * 호스트 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hosts")
@CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
public class HostController {
    private final HostService hostService;
    //private final UserService userService;
    private final AccommodationService accommodationService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;

    /**
     * 새 호스트 사용자를 등록합니다.
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerHost(@RequestBody Host host, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
        Long userId = user.getUserId();
        host.setRole("HOST");
        try {
            hostService.registHost(host, userId);
            Cookie cookie = new Cookie("JWT", null);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(0);

            response.addCookie(cookie);
            return ResponseEntity.ok(Map.of("alertMsg", "호스트로 등록되었습니다. 로그인 후 사용해주세요."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("alertMsg", e.getMessage()));
        }
    }

    /**
     * 호스트 상세 페이지로 이동합니다.
     */
    @GetMapping("/{hostId}")
    public ResponseEntity<?> getHostDetail(@PathVariable Long hostId) {
        try {
            Host host = hostService.getHostById(hostId);
            return ResponseEntity.ok(host);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 호스트 목록 조회
     */
    @GetMapping("")
    public ResponseEntity<?> getHostList() {
        try {
            List<Host> hosts = hostService.getHostList();
            return ResponseEntity.ok(hosts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 호스트 정보 업데이트 (본인만)
     */
    @PutMapping("/{hostId}")
    public ResponseEntity<?> updateHost(
            @PathVariable Long hostId,
            @RequestBody Host hostRequest) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
//        if (!user.getUserId().equals(hostId)) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("error", "권한이 없습니다."));
//        }
        try {
            hostRequest.setHostId(hostId);
            hostService.updateHost(hostRequest);
            return ResponseEntity.ok(Map.of("message", "호스트 정보가 업데이트되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 호스트 상태 업데이트 (관리자 전용)
     */
    @PutMapping("/{hostId}/status")
    public ResponseEntity<?> updateHostStatus(
            @PathVariable Long hostId,
            @RequestParam String hostStatus) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
//        if (!user.getRole().equals("ADMIN")) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("error", "관리자만 접근할 수 있습니다."));
//        }
        try {
            hostService.updateHostStatus(hostId, hostStatus);
            return ResponseEntity.ok(Map.of("message", "호스트 상태가 업데이트되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 숙소 등록 권한 확인
     */
    @GetMapping("/{hostId}/can-register-accommodation")
    public ResponseEntity<?> canRegisterAccommodation(@PathVariable Long hostId) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
//        if (!user.getRole().equals("HOST") && !user.getRole().equals("HOST")) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("error", "호스트만 숙소를 등록할 수 있습니다."));
//        }
//        if (user.getRole().equals("HOST")) {
//            return ResponseEntity.ok(Map.of("canRegister", true));
//        }
        try {
            Host host = hostService.getHostById(hostId);
            boolean allowed = host != null && "APPROVED".equals(host.getHostStatus());
            return ResponseEntity.ok(Map.of("canRegister", allowed));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 호스트 예약 관리 조회
     */
    @GetMapping("/{hostId}/reservations")
    public ResponseEntity<?> getHostReservations(
            @PathVariable Long hostId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long accommodationId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        if (!user.getUserId().equals(hostId) && !user.getRole().equals("HOST")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "권한이 없습니다."));
        }
        try {
            List<Reservation> reservations = reservationService.getReservationsByHostId(hostId);
            // 필터링
            if (status != null) {
                reservations.removeIf(r -> !status.equals(r.getStatus()));
            }
            if (accommodationId != null) {
                reservations.removeIf(r -> {
                    try {
                        Room room = accommodationService.getRoomById(r.getRoomId());
                        return !accommodationId.equals(room.getAccommodationId());
                    } catch (Exception ex) {
                        return true;
                    }
                });
            }
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 호스트 리뷰 관리 조회
     */
    @GetMapping("/{hostId}/reviews")
    public ResponseEntity<?> getHostReviews(
            @PathVariable Long hostId,
            @RequestParam(required = false) Long accommodationId,
            @RequestParam(required = false) Integer rating) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
//        if (!user.getUserId().equals(hostId) && !user.getRole().equals("HOST")) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("error", "권한이 없습니다."));
//        }
        try {
            List<Accommodation> accommodations = accommodationService.getAccommodationsByHostId(hostId);
            List<Review> allReviews = new ArrayList<>();
            for (Accommodation acc : accommodations) {
                List<Review> reviews = reviewService.getReviewsByAccommodationId(acc.getAccommodationId());
                if (rating != null) {
                    reviews.removeIf(r -> !rating.equals(r.getRating()));
                }
                allReviews.addAll(reviews);
            }
            if (accommodationId != null) {
                allReviews.removeIf(r -> !accommodationId.equals(r.getAccommodationId()));
            }
            Map<String, Object> result = new HashMap<>();
            result.put("reviews", allReviews);
            double avg = allReviews.stream()
                    .filter(r -> r.getRating() != null)
                    .mapToDouble(Review::getRating)
                    .average().orElse(0);
            result.put("totalReviews", allReviews.size());
            result.put("averageRating", Math.round(avg * 10.0) / 10.0);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 호스트 숙소 관리 조회
     */
    @GetMapping("/{hostId}/accommodations")
    public ResponseEntity<?> getHostAccommodations(@PathVariable Long hostId) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
//        if (!user.getUserId().equals(hostId) && !user.getRole().equals("HOST")) {
//            return ResponseEntity.status(HttpStatus.FORBIDDEN)
//                    .body(Map.of("error", "권한이 없습니다."));
//        }
        try {
            List<Accommodation> accommodations = accommodationService.getAccommodationsByHostId(hostId);
            Map<Long, List<Room>> roomsByAcc = new HashMap<>();
            int totalRooms = 0, activeAcc = 0;
            double totalRating = 0;
            int ratedCount = 0;
            for (Accommodation acc : accommodations) {
                List<Room> rooms = accommodationService.getRoomsByAccommodationId(acc.getAccommodationId());
                roomsByAcc.put(acc.getAccommodationId(), rooms);
                totalRooms += rooms.size();
                if ("ACTIVE".equals(acc.getStatus())) activeAcc++;
                Double avg = reviewService.getAverageRatingByAccommodationId(acc.getAccommodationId());
                if (avg != null && avg > 0) { totalRating += avg; ratedCount++; }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("accommodations", accommodations);
            result.put("roomsByAccommodation", roomsByAcc);
            result.put("totalRooms", totalRooms);
            result.put("activeAccommodations", activeAcc);
            result.put("averageRating", ratedCount > 0 ? Math.round((totalRating / ratedCount) * 10.0) / 10.0 : 0);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
