package com.ssafy.trip.host.controller;

import com.ssafy.trip.host.service.HostService;
import com.ssafy.trip.host.model.Host;
import com.ssafy.trip.host.model.HostRegistrationRequestDto;
import com.ssafy.trip.host.model.HostUpdateRequestDto;
import com.ssafy.trip.security.CustomUserDetails;
import com.ssafy.trip.user.UserService;

import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.accommodation.service.ReservationService;
import com.ssafy.trip.review.service.ReviewService;
import com.ssafy.trip.accommodation.model.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import java.sql.SQLException;

/**
 * 호스트 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/host")
@Slf4j
// @CrossOrigin(origins = "http://localhost:3000", methods = {RequestMethod.OPTIONS, RequestMethod.DELETE, RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT}, allowCredentials = "true") 
public class HostController {
    private final HostService hostService;
    private final UserService userService;

    private final AccommodationService accommodationService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;

    /**
     * 새 호스트 사용자를 등록합니다. (사업자 등록증 파일 포함)
     */
    @PostMapping(value = "/register", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> registerHost(
            @RequestPart("dto") HostRegistrationRequestDto registrationDto,
            @RequestPart(value = "businessLicenseFile", required = true) MultipartFile businessLicenseFile,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        Long userId = userDetails.getUser().getUserId();

        try {
            Host registeredHost = hostService.registerHost(userId, registrationDto, businessLicenseFile);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredHost);
        } catch (Exception e) {
            log.error("Error during host registration for userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "호스트 등록 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 호스트 ID로 상세 정보를 조회합니다.
     */
    @GetMapping("/{hostId}")
    public ResponseEntity<?> getHostDetail(@PathVariable Long hostId) {
        try {
            Host host = hostService.getHostById(hostId);
            if (host == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "호스트 정보를 찾을 수 없습니다."));
            }
            return ResponseEntity.ok(host);
        } catch (Exception e) {
            log.error("Error fetching host detail for hostId {}: {}", hostId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "호스트 정보 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 사용자 ID로 호스트 정보를 조회합니다.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getHostByUserId(@PathVariable Long userId) {
        try {
            Host host = hostService.getHostByUserId(userId);
            if (host == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "사용자 ID에 해당하는 호스트 정보를 찾을 수 없습니다."));
            }
            return ResponseEntity.ok(host);
        } catch (Exception e) {
            log.error("Error fetching host by userId {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "호스트 정보 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 모든 호스트 목록을 조회합니다. (주로 관리자용)
     */
    @GetMapping("")
    public ResponseEntity<?> getAllHosts(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || !"ADMIN".equals(userDetails.getUser().getRole())) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "관리자만 접근 가능합니다."));
        }
        try {
            List<Host> hosts = hostService.getAllHosts();
            return ResponseEntity.ok(hosts);
        } catch (Exception e) {
            log.error("Error fetching all hosts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "호스트 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 호스트 정보를 업데이트합니다. (사업자 등록증 파일은 재제출 API 사용, 본인 또는 관리자)
     */
    @PutMapping(value = "/{hostId}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> updateHostInfo(
            @PathVariable Long hostId,
            @RequestPart("dto") HostUpdateRequestDto updateDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            Host existingHost = hostService.getHostById(hostId);
            if (existingHost == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "수정할 호스트 정보를 찾을 수 없습니다."));
            }

            boolean isAdmin = "ADMIN".equals(userDetails.getUser().getRole());
            boolean isOwner = userDetails.getUser().getUserId() != null && userDetails.getUser().getUserId().equals(existingHost.getUserId());

            if (!isAdmin && !isOwner) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "호스트 정보를 수정할 권한이 없습니다."));
            }

            Host updatedHost = hostService.updateHostInfo(hostId, updateDto);
            return ResponseEntity.ok(updatedHost);
        } catch (Exception e) {
            log.error("Error updating host info for hostId {}: {}", hostId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "호스트 정보 업데이트 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 호스트 상태를 업데이트합니다. (관리자 전용)
     */
    @PutMapping("/{hostId}/status")
    public ResponseEntity<?> updateHostStatus(
            @PathVariable Long hostId,
            @RequestBody Map<String, String> payload,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null || !"ADMIN".equals(userDetails.getUser().getRole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "관리자만 호스트 상태를 변경할 수 있습니다."));
        }
        
        String status = payload.get("status");
        String adminComment = payload.get("adminComment");

        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "상태(status) 값은 필수입니다."));
        }

        try {
            hostService.updateHostStatus(hostId, status, adminComment);
            Host updatedHost = hostService.getHostById(hostId);
            return ResponseEntity.ok(updatedHost);
        } catch (Exception e) {
            log.error("Error updating host status for hostId {}: {}", hostId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "호스트 상태 업데이트 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 사업자 등록증을 재제출합니다. (본인만)
     */
    @PostMapping(value = "/{hostId}/license/resubmit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> resubmitLicense(
            @PathVariable Long hostId,
            @RequestPart(value = "licenseResubmitFile", required = true) MultipartFile licenseResubmitFile,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }

        try {
            Host host = hostService.getHostById(hostId);
            if (host == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "호스트 정보를 찾을 수 없습니다."));
            }
            if (!host.getUserId().equals(userDetails.getUser().getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "자신의 사업자 등록증만 재제출할 수 있습니다."));
            }

            Host updatedHost = hostService.resubmitLicense(hostId, licenseResubmitFile);
            return ResponseEntity.ok(updatedHost);
        } catch (Exception e) {
            log.error("Error resubmitting license for hostId {}: {}", hostId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "사업자 등록증 재제출 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 호스트를 삭제합니다. (관리자 또는 본인 - 정책에 따라 결정)
     * 여기서는 관리자만 가능하도록 예시를 들되, 실제 정책에 따라 본인 삭제 허용 여부 결정 필요
     */
    @DeleteMapping("/{hostId}")
    public ResponseEntity<?> deleteHost(@PathVariable Long hostId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        if (!"ADMIN".equals(userDetails.getUser().getRole())) {
             return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "호스트 삭제 권한이 없습니다. (관리자 문의)"));
        }

        try {
            boolean deleted = hostService.deleteHost(hostId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "호스트 정보가 성공적으로 삭제되었습니다."));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "삭제할 호스트 정보를 찾을 수 없거나 삭제에 실패했습니다."));
        } catch (Exception e) {
            log.error("Error deleting host for hostId {}: {}", hostId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "호스트 삭제 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 호스트 숙소 관리 조회
     */
    @GetMapping("/{hostId}/accommodations")
    public ResponseEntity<?> getHostAccommodations(@PathVariable Long hostId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }
        if (!userDetails.getUser().getUserId().equals(hostId) && !userDetails.getUser().getRole().equals("HOST")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "권한이 없습니다."));
        }
        try {
            List<Accommodation> accommodations = accommodationService.getAccommodationsByHostId(hostId);
            Map<Long, List<Room>> roomsByAcc = new HashMap<>();
            int totalRooms = 0, activeAcc = 0;
            double totalRating = 0;
            int ratedCount = 0;
            for (Accommodation acc : accommodations) {
                List<Room> rooms = accommodationService.getRoomsByAccommodationId(acc.getAccommodationId(), null, null, null);
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

    /**
     * 특정 호스트의 예약 목록을 조회합니다. (페이징 및 필터링 포함)
     * 프론트엔드 요청 경로: GET /api/host/{hostId}/reservations
     * 예시 파라미터: ?status=PENDING&checkInDate=2024-01-01&guestName=김&sortBy=createdAtDesc&page=0&size=10
     */
    @GetMapping("/{hostId}/reservations")
    public ResponseEntity<?> getHostReservations(
            @PathVariable Long hostId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String checkInDate, // 날짜 형식 고려 필요 (String or LocalDate)
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false, defaultValue = "createdAtDesc") String sortBy,
            @PageableDefault(size = 10) Pageable pageable, // 기본 정렬은 서비스에서 처리하거나, 프론트와 맞춰야 함
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
        }

        Host currentHost;
        try {
            currentHost = hostService.getHostById(hostId);
        } catch (SQLException e) {
            log.error("Error fetching host by ID {} for reservations: {}", hostId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "호스트 정보 조회 중 DB 오류 발생: " + e.getMessage()));
        }

        if (currentHost == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "호스트 정보를 찾을 수 없습니다."));
        }
        boolean isAdmin = "ADMIN".equals(userDetails.getUser().getRole());
        boolean isOwner = currentHost.getUserId().equals(userDetails.getUser().getUserId());

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "예약 정보를 조회할 권한이 없습니다."));
        }

        try {
            // ReservationService에 필터링 및 페이징 로직이 포함된 메소드 호출
            // 예: getReservationsByHostIdWithFiltersAndPaging(Long hostId, String status, String checkInDate, String guestName, String sortBy, Pageable pageable)
            // 해당 메소드가 아직 없다면 ReservationService 및 DAO/Mapper에 구현이 필요합니다.
            // 아래는 해당 메소드가 있다고 가정하고 호출하는 부분입니다.
            Page<Reservation> reservationsPage = reservationService.getReservationsByHostIdWithFiltersAndPaging(hostId, status, checkInDate, guestName, sortBy, pageable);
            return ResponseEntity.ok(reservationsPage);

        } catch (Exception e) {
            log.error("Error fetching reservations for hostId {}: {}, params: status={}, checkInDate={}, guestName={}, sortBy={}, pageable={}",
                hostId, e.getMessage(), status, checkInDate, guestName, sortBy, pageable, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "예약 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }
}
