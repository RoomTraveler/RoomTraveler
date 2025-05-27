package com.ssafy.trip.admin;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Image;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.host.service.HostService;
import com.ssafy.trip.notification.NotificationService;
import com.ssafy.trip.region.model.Sido;
import com.ssafy.trip.region.model.Gugun;
import com.ssafy.trip.user.User;
import com.ssafy.trip.user.UserService;

import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Random;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.security.access.prepost.PreAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import com.ssafy.trip.common.BaseResponse;
import com.ssafy.trip.host.model.Host;
import java.sql.SQLException;
import com.ssafy.trip.accommodation.model.AccommodationResponseDto;
import com.ssafy.trip.accommodation.model.PageDto;

/**
 * 관리자 기능을 위한 API 컨트롤러 (모든 엔드포인트 /api/admin/~~~)
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Value("${tourapi.service-key}")
    private String serviceKey;
    @Value("${tourapi.base-url}")
    private String baseUrl;
    @Value("${tourapi.mobile-os}")
    private String mobileOs;
    @Value("${tourapi.mobile-app}")
    private String mobileApp;

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AccommodationService accommodationService;
    private final UserService userService;
    private final AdminService adminService;
    private final HostService hostService;
    private final NotificationService notificationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AdminController(
            AccommodationService accommodationService,
                          UserService userService,
                          AdminService adminService,
                          HostService hostService,
                          NotificationService notificationService
                          ) {
        this.accommodationService = accommodationService;
        this.userService = userService;
        this.adminService = adminService;
        this.hostService = hostService;
        this.notificationService = notificationService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // [숙소 유형별 개수 조회]
    @GetMapping("/dashboard/type-counts")
    public ResponseEntity<Map<String, Long>> getAccommodationTypeCounts() {
            try {
                Map<String, Long> typeCounts = accommodationService.getAccommodationTypeCounts();
                return ResponseEntity.ok(typeCounts);
            } catch (Exception e) {
                logger.error("숙소 유형별 카운트 조회 오류", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyMap());
            }
        }

    // [관리자 대시보드 전체 데이터]
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getAdminDashboardData() {
        Map<String, Object> responseData = new HashMap<>();
        try {
            Map<String, Long> userStats = adminService.getUserStatistics();
            Map<String, Long> accommodationStats = adminService.getAccommodationStatistics();
            Map<String, Long> reservationStats = adminService.getReservationStatistics();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userStats.getOrDefault("totalUsers", 0L));
            stats.put("userGrowth", 0); // 임시값
            stats.put("totalAccommodations", accommodationStats.getOrDefault("totalAccommodations", 0L));
            stats.put("accommodationGrowth", 0); // 임시값
            stats.put("totalReservations", reservationStats.getOrDefault("totalReservations", 0L));
            stats.put("reservationGrowth", 0); // 임시값
            stats.put("totalRevenue", 0L);
            stats.put("revenueGrowth", 0);

            responseData.put("stats", stats);
            responseData.putIfAbsent("recentAccommodations", new ArrayList<>());
            responseData.putIfAbsent("recentUsers", new ArrayList<>());
            responseData.putIfAbsent("systemStatus", new HashMap<>());
            responseData.putIfAbsent("monthlyData", Map.of("labels", new ArrayList<>(), "reservations", new ArrayList<>(), "revenue", new ArrayList<>()));
            responseData.putIfAbsent("accommodationTypes", Map.of("labels", new ArrayList<>(), "data", new ArrayList<>()));

            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            logger.error("관리자 대시보드 데이터 API 로딩 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "대시보드 데이터 조회 중 오류 발생"));
        }
    }

    // [관리자용 사용자 목록 조회]
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAdminUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            logger.info("GET /api/admin/users 요청 수신 - role: {}, status: {}, keyword: {}, page: {}, size: {}",
                    role, status, keyword, pageable.getPageNumber(), pageable.getPageSize());

            Page<User> userPage = adminService.getAdminUsers(role, status, keyword, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", userPage.getContent());
            response.put("totalPages", userPage.getTotalPages());
            response.put("totalElements", userPage.getTotalElements());
            response.put("number", userPage.getNumber());
            response.put("size", userPage.getSize());
            response.put("first", userPage.isFirst());
            response.put("last", userPage.isLast());
            response.put("numberOfElements", userPage.getNumberOfElements());
            response.put("empty", userPage.isEmpty());

            logger.info("관리자용 사용자 목록 조회 성공. {} 페이지의 {}개 항목 반환. 총 {}개 항목 중 {} 페이지.", 
                userPage.getNumber() +1, userPage.getNumberOfElements(), userPage.getTotalElements(), userPage.getTotalPages());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("관리자용 사용자 목록 조회 API - 잘못된 요청 파라미터: {}", e.getMessage(), e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "잘못된 요청 파라미터입니다: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            logger.error("관리자용 사용자 목록 조회 API 중 서버 오류 발생", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "사용자 목록 조회 중 서버 내부 오류가 발생했습니다.");
            errorResponse.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // [관리자용 사용자 역할 변경]
    @PutMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRoleByAdmin(
            @PathVariable Long userId,
            @RequestBody Map<String, String> payload) { 
        String newRole = payload.get("role");
        if (newRole == null || newRole.trim().isEmpty()) {
            logger.warn("역할 변경 요청 실패: 사용자 ID {}, 새로운 역할 값이 비어있음", userId);
            return ResponseEntity.badRequest().body(Map.of("message", "새로운 역할(role) 값이 필요합니다."));
        }

        // 백엔드 users 테이블의 role 컬럼이 ENUM('USER', 'HOST', 'ADMIN') 이므로 대문자 비교/저장 가정
        String validRole = newRole.trim().toUpperCase();
        List<String> allowedRoles = Arrays.asList("USER", "HOST", "ADMIN");
        if (!allowedRoles.contains(validRole)) {
            logger.warn("역할 변경 요청 실패: 사용자 ID {}, 유효하지 않은 역할 값: {}", userId, newRole);
            return ResponseEntity.badRequest().body(Map.of("message", "유효하지 않은 역할 값입니다: " + newRole + ". 허용되는 값: USER, HOST, ADMIN"));
        }

        try {
            logger.info("관리자 요청 - 사용자 ID {}의 역할을 {}로 변경 시도", userId, validRole);
            boolean success = adminService.updateUserRoleByAdmin(userId, validRole);
            if (success) {
                logger.info("사용자 ID {}의 역할이 {}로 성공적으로 변경되었습니다.", userId, validRole);
                return ResponseEntity.ok(Map.of("message", "사용자 역할이 성공적으로 변경되었습니다.", "userId", userId, "newRole", validRole));
            } else {
                logger.warn("사용자 ID {}의 역할 변경 실패. 사용자를 찾을 수 없거나 변경 사항이 없습니다.", userId);
                // 해당 사용자가 없는 경우 혹은 이미 해당 역할인 경우를 구분해서 메시지를 줄 수 도 있음.
                // User existingUser = userService.getUserById(userId); // 예시
                // if (existingUser == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "해당 ID의 사용자를 찾을 수 없습니다."));
                // if (existingUser.getRole().name().equalsIgnoreCase(validRole)) return ResponseEntity.ok(Map.of("message", "사용자가 이미 해당 역할을 가지고 있습니다."));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "사용자 역할 변경에 실패했습니다. 사용자를 찾을 수 없거나 이미 해당 역할일 수 있습니다."));
            }
        } catch (Exception e) {
            logger.error("사용자 (ID: {}) 역할 변경 중 서버 오류 발생: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "역할 변경 중 서버 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // [사용자 상태 변경]
    @PostMapping("/users/{userId}/status")
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam String status) {
        try {
            int result = userService.updateUserStatus(userId, status);
            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                response.put("success", true);
                response.put("message", "사용자 상태가 성공적으로 변경되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "사용자 상태 변경에 실패했습니다.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("사용자 상태 변경 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "사용자 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // [숙소 상태 변경]
    @PostMapping("/accommodations/{accommodationId}/status")
    public ResponseEntity<Map<String, Object>> updateAccommodationStatus(
            @PathVariable Long accommodationId,
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null || status.trim().isEmpty()) {
            logger.warn("숙소 상태 변경 요청 실패: accommodationId {}, status 값이 비어있음", accommodationId);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "상태(status) 값이 필요합니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        try {
            Accommodation.AccommodationStatus accommodationStatusEnum;
            try {
                accommodationStatusEnum = Accommodation.AccommodationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                logger.error("잘못된 숙소 상태 값입니다: {}", status, e);
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "유효하지 않은 숙소 상태 값입니다: " + status);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            int result = accommodationService.updateAccommodationStatus(accommodationId, accommodationStatusEnum);
            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                response.put("success", true);
                response.put("message", "숙소 상태가 성공적으로 변경되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "숙소 상태 변경에 실패했습니다.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("숙소 상태 변경 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "숙소 상태 변경 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // [숙소 삭제]
    @DeleteMapping("/accommodations/{accommodationId}")
    public ResponseEntity<Map<String, Object>> deleteAccommodation(@PathVariable Long accommodationId) {
        try {
            int result = accommodationService.deleteAccommodation(accommodationId);
            Map<String, Object> response = new HashMap<>();
            if (result > 0) {
                response.put("success", true);
                response.put("message", "숙소가 성공적으로 삭제되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "숙소 삭제에 실패했습니다.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("숙소 삭제 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "숙소 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // [모든 숙소 삭제]
    @DeleteMapping("/accommodations/all")
    public ResponseEntity<Map<String, Object>> deleteAllAccommodations() {
        try {
            logger.info("모든 숙소 삭제 시도");
            int deletedCount = accommodationService.deleteAllAccommodations();
            logger.info("모든 숙소 삭제 완료: {} 개의 숙소가 삭제되었습니다.", deletedCount);
            Map<String, Object> response = new HashMap<>();
            if (deletedCount > 0) {
                response.put("success", true);
                response.put("message", "모든 숙소가 성공적으로 삭제되었습니다. (총 " + deletedCount + "개)");
            } else {
                response.put("success", true);
                response.put("message", "삭제할 숙소가 없습니다.");
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("모든 숙소 삭제 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "모든 숙소 삭제 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // [시도별 구군 목록 조회]
    @GetMapping("/regions/guguns")
    public ResponseEntity<List<Gugun>> getGugunsBySido(@RequestParam int sidoCode) {
        try {
            List<Gugun> guguns = adminService.getGugunsBySido(sidoCode);
            return ResponseEntity.ok(guguns);
        } catch (Exception e) {
            logger.error("구군 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // [TourAPI에서 시도 데이터 가져와 DB에 저장]
    @PostMapping("/regions/import-sidos")
    public ResponseEntity<Map<String, Object>> importSidos() {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/areaCode2")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", mobileOs)
                    .queryParam("MobileApp", mobileApp)
                    .queryParam("_type", "json")
                    .queryParam("numOfRows", 100)
                    .queryParam("pageNo", 1);

            URI uri = new URI(builder.build(false).toUriString());
            String response = restTemplate.getForObject(uri, String.class);
            logger.info("▶▶ TourAPI 시도 조회 URL: {}", uri);

            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            List<Sido> sidos = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    int code = item.path("code").asInt();
                    String name = item.path("name").asText();
                    Sido sido = new Sido(code, name, null);
                    sidos.add(sido);
                }
            }
            int count = adminService.importSidos(sidos);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", count + "개의 시도 데이터가 성공적으로 가져와졌습니다.");
            result.put("count", count);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("시도 데이터 가져오기 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "시도 데이터 가져오기 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // [TourAPI에서 구군 데이터 가져와 DB에 저장]
    @PostMapping("/regions/import-guguns")
    public ResponseEntity<Map<String, Object>> importGuguns() {
        try {
            List<Sido> sidos = adminService.getAllSidos();
            if (sidos.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "시도 데이터가 없습니다. 먼저 시도 데이터를 가져와주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            int totalCount = 0;
            for (Sido sido : sidos) {
                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromHttpUrl(baseUrl + "/areaCode2")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("MobileOS", mobileOs)
                        .queryParam("MobileApp", mobileApp)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", 100)
                        .queryParam("pageNo", 1)
                        .queryParam("areaCode", sido.getCode());

                URI uri = new URI(builder.build(false).toUriString());
                String response = restTemplate.getForObject(uri, String.class);

                JsonNode root = objectMapper.readTree(response);
                JsonNode items = root.path("response").path("body").path("items").path("item");

                List<Gugun> guguns = new ArrayList<>();
                if (items.isArray()) {
                    for (JsonNode item : items) {
                        int code = item.path("code").asInt();
                        String name = item.path("name").asText();
                        Gugun gugun = new Gugun(code, name);
                        guguns.add(gugun);
                    }
                }
                int count = adminService.importGuguns(sido.getCode(), guguns);
                totalCount += count;
            }
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", totalCount + "개의 구군 데이터가 성공적으로 가져와졌습니다.");
            result.put("count", totalCount);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("구군 데이터 가져오기 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "구군 데이터 가져오기 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    /**
     * TourAPI에서 숙소 데이터를 가져와 DB에 저장합니다.
     */
    @PostMapping("/accommodations/import")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importAccommodations(
            @RequestParam(required = false) Integer sidoCode,
            @RequestParam(required = false) Integer gugunCode,
            @RequestParam(required = false) Boolean clearExisting,
            HttpSession session) {
        try {
            // 기존 데이터 삭제 옵션이 활성화된 경우
            if (clearExisting != null && clearExisting) {
                logger.info("기존 숙소 데이터를 삭제합니다.");
                // 모든 숙소 삭제 (이 부분은 실제 구현 필요)
                List<Accommodation> existingAccommodations = accommodationService.getAllAccommodations();
                for (Accommodation acc : existingAccommodations) {
                    accommodationService.deleteAccommodation(acc.getAccommodationId());
                }
                logger.info("기존 숙소 데이터 삭제 완료");
            }

            int importedCount = 0;
            int pageNo = 1;
            int totalPages = 1;
            int numOfRows = 100; // 한 페이지당 가져올 데이터 수

            // 중복 체크를 위한 Set
            java.util.Set<String> processedContentIds = new java.util.HashSet<>();

            // 모든 페이지 처리
            do {
                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromHttpUrl(baseUrl + "/areaBasedList2")              // 올바른 엔드포인트
                        .queryParam("ServiceKey", serviceKey)
                        .queryParam("MobileOS", mobileOs)
                        .queryParam("MobileApp", mobileApp)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", 12)
                        .queryParam("pageNo", pageNo)
                        .queryParam("arrange", "A")
                        .queryParam("contentTypeId", 32)
                        .queryParam("areaCode", sidoCode != null ? sidoCode : "")
                        .queryParam("sigunguCode", gugunCode != null ? gugunCode : "")
                        .queryParam("cat1", "B02")                            // 숙박 대분류
                        .queryParam("cat2", "")
                        .queryParam("cat3", "");

                // 선택적 파라미터 추가
                if (sidoCode != null) {
                    builder.queryParam("areaCode", sidoCode);
                }
                if (gugunCode != null) {
                    builder.queryParam("sigunguCode", gugunCode);
                }

                String tourApiUrl = builder.build(false).toUriString();
                logger.info("▶▶ TourAPI 호출 URL: {}", tourApiUrl);

                String response = restTemplate.getForObject(new URI(tourApiUrl), String.class);

                // **JSON 파싱 전 검사**
                if (response == null || response.trim().startsWith("<")) {
                    logger.error("❌ TourAPI가 JSON이 아닌 응답을 반환했습니다. (HTML 또는 null)\n" +
                                    "   → 응답 일부: {}",
                            response != null
                                    ? response.substring(0, Math.min(response.length(), 200))
                                    : "null");
                    // 적절히 처리: null 리턴, 예외 던지기, 사용자용 메시지 반환 등
                    return null;
                }

                // 정상 JSON 파싱
                JsonNode root = objectMapper.readTree(response);

                JsonNode body = root.path("response").path("body");

                // 총 페이지 수 계산
                int totalCount = body.path("totalCount").asInt(0);
                if (pageNo == 1) { // 첫 페이지에서만 계산
                    totalPages = (int) Math.ceil((double) totalCount / numOfRows);
                    logger.info("총 " + totalCount + "개의 숙소 데이터가 있습니다. 총 " + totalPages + "페이지를 처리합니다.");
                }

                JsonNode items = body.path("items").path("item");

                if (items.isArray()) {
                    for (JsonNode item : items) {
                        String contentId = item.path("contentid").asText();

                        // 중복 체크
                        if (processedContentIds.contains(contentId)) {
                            logger.info("중복된 contentId: " + contentId + ", 건너뜁니다.");
                            continue;
                        }

                        processedContentIds.add(contentId);
                        logger.info("처리 중인 contentId: " + contentId);

                        // 숙소 상세 정보 가져오기
                        // 여기!!!
                        Accommodation accommodation = fetchAccommodationDetail(contentId, session);
                        if (accommodation == null) {
                            logger.info("contentId: " + contentId + " - 숙소 상세 정보를 가져오지 못했습니다. 건너뜁니다.");
                            continue;
                        }

                        // 객실 정보 가져오기
                        List<Room> rooms = fetchRoomInfo(contentId);
                        if (rooms.isEmpty()) {
                            logger.info("contentId: " + contentId + " - 객실 정보를 가져오지 못했습니다. 건너뜁니다.");
                            continue;
                        }

                        // 이미지 정보 가져오기
                        List<Image> images = fetchImageInfo(contentId, rooms);

                        // 이미지가 없으면 건너뛰기
                        if (images.isEmpty()) {
                            logger.info("contentId: " + contentId + " - 이미지가 없습니다. 건너뜁니다.");
                            continue;
                        }

                        // 썸네일 이미지가 있는지 확인
                        boolean hasMainImage = false;
                        for (Image image : images) {
                            if (image.getIsMain() != null && image.getIsMain()) {
                                hasMainImage = true;
                                break;
                            }
                        }

                        // 썸네일 이미지가 없으면 건너뛰기
                        if (!hasMainImage) {
                            logger.info("contentId: " + contentId + " - 썸네일 이미지가 없습니다. 건너뜁니다.");
                            continue;
                        }

                        // DB에 저장
                        Long accommodationId = accommodationService.importFromApi(accommodation, rooms, images);
                        if (accommodationId != null) {
                            importedCount++;
                            logger.info("contentId: " + contentId + " - 숙소 데이터 저장 성공. 숙소 ID: " + accommodationId);
                            logger.info("숙소 데이터 가져오기 진행 중: " + importedCount + "개 완료");
                        } else {
                            logger.warn("contentId: " + contentId + " - 숙소 데이터 저장 실패");
                        }
                    }
                }

                pageNo++;
                logger.info("페이지 " + (pageNo-1) + "/" + totalPages + " 처리 완료");

            } while (pageNo <= totalPages);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", importedCount + "개의 숙소 데이터가 성공적으로 가져와졌습니다.");
            result.put("count", importedCount);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("숙소 데이터 가져오기 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "숙소 데이터 가져오기 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



    // [전체 호스트 목록 조회]
    @GetMapping("/hosts/all")
    public ResponseEntity<?> getAllHosts(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        try {
            logger.info("GET /api/admin/hosts/all - status: {}, keyword: {}, page: {}, size: {}, sortBy: {}, sortDirection: {}",
                    status, keyword, page, size, sortBy, sortDirection);

            Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            // AdminService 또는 HostService에 모든 호스트를 가져오는 메소드 호출
            // 예시: Page<Host> hostPage = hostService.getAllHosts(status, keyword, pageable);
            // 아래는 임시 응답입니다. 실제 서비스 로직으로 교체해야 합니다.
            Page<User> hostPage = adminService.getAllHostsPage(status, keyword, pageable); // 이 메소드가 AdminService에 구현되어 있다고 가정

            Map<String, Object> response = new HashMap<>();
            response.put("content", hostPage.getContent());
            response.put("totalPages", hostPage.getTotalPages());
            response.put("totalElements", hostPage.getTotalElements());
            response.put("number", hostPage.getNumber());
            response.put("size", hostPage.getSize());
            // 필요한 경우 추가적인 페이지 정보 추가

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("잘못된 파라미터입니다: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "잘못된 파라미터입니다: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("전체 호스트 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "전체 호스트 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    // [승인 대기 중인 호스트 신청 목록 조회]
    @GetMapping("/hosts/pending")
    public ResponseEntity<?> getPendingHosts() {
        try {
            List<?> pendingHosts = hostService.getHostsByStatus("WAIT");
            return ResponseEntity.ok(pendingHosts);
        } catch (Exception e) {
            logger.error("승인 대기 중인 호스트 목록 조회 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "승인 대기 호스트 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @Operation(summary = "호스트 신청 승인", description = "관리자가 호스트 신청을 승인합니다.")
    @PutMapping("/approve/{hostId}")
    public ResponseEntity<BaseResponse<Host>> approveHost(@PathVariable Long hostId) throws SQLException {
        hostService.updateHostStatus(hostId, "ACTIVE");
        Host host = hostService.getHostById(hostId);
        if (host != null) {
            userService.updateUserRole(host.getUserId(), "HOST"); // users 테이블의 역할 변경
            return ResponseEntity.ok(BaseResponse.onSuccess(host));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BaseResponse.onFail("호스트 정보를 찾을 수 없습니다."));
            }

    @Operation(summary = "호스트 신청 거절", description = "관리자가 호스트 신청을 거절하고 사유를 입력합니다.")
    @PutMapping("/reject/{hostId}")
    public ResponseEntity<BaseResponse<Void>> rejectHost(@PathVariable Long hostId, @RequestBody String reason) throws SQLException {
        hostService.updateHostStatusAndReason(hostId, "REJECT", reason);
        return ResponseEntity.ok(BaseResponse.onSuccess());
    }

    /**
     * 숙소 관리 페이지를 표시합니다. (HTML 뷰 반환용 - 필요시 유지 또는 삭제)
     * 이 메서드는 API가 아니라 템플릿을 반환하기 위한 것입니다.
     * 프론트엔드가 자체적으로 UI를 구성하므로, 이 메서드는 사용되지 않을 수 있습니다.
     */
    @GetMapping("/accommodations-view")
    public String manageAccommodationsView(Model model) {
        try {
            // 이 부분은 관리자용 숙소 목록 HTML 페이지를 위한 데이터 로딩일 수 있습니다.
            // List<Accommodation> accommodations = accommodationService.getAllAccommodations();
            // model.addAttribute("accommodations", accommodations);

            // 시도 목록 (필터용)
            List<Sido> sidos = adminService.getAllSidos();
            model.addAttribute("sidos", sidos);

            return "admin/accommodations"; // Thymeleaf 또는 JSP 뷰 이름
        } catch (Exception e) {
            logger.error("숙소 관리 (뷰) 페이지 로딩 중 오류 발생", e);
            model.addAttribute("errorMessage", "숙소 정보를 불러오는 중 오류가 발생했습니다.");
            return "error/500";
        }
    }

    /**
     * [수정/신규] 관리자용 숙소 목록 조회 API (JSON, 페이지네이션, 필터링)
     * 프론트엔드 adminStore.js에서 호출하는 API입니다.
     */
    @GetMapping("/accommodations")
    @ResponseBody
    public ResponseEntity<?> getAdminAccommodations(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, name = "type") String accommodationType,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(
                    Sort.Direction.fromString(sortDirection),
                    sortBy == null ? "createdAt" : sortBy
            ));

            // === ★ 핵심: 파라미터 처리 ===
            Map<String, Object> params = new HashMap<>();
            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
                params.put("status", status.trim());
            }
            if (accommodationType != null && !accommodationType.trim().isEmpty() && !"ALL".equalsIgnoreCase(accommodationType.trim())) {
                params.put("accommodationType", accommodationType.trim());
            }
            if (keyword != null && !keyword.trim().isEmpty()) {
                params.put("keyword", keyword.trim());
            }

            // 정렬/페이징용 파라미터는 그대로
            // (Pageable에 이미 정렬/오프셋/리밋 포함이지만, 아래 ServiceImpl에서 넣어줌)
            // params.put("sortBy", ...); etc → ServiceImpl에서 처리

            Page<Accommodation> accommodationPage = adminService.getAdminAccommodations(params, pageable);

            Map<String, Object> result = new HashMap<>();
            result.put("content", accommodationPage.getContent());
            result.put("totalPages", accommodationPage.getTotalPages());
            result.put("number", accommodationPage.getNumber());
            result.put("totalElements", accommodationPage.getTotalElements());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("관리자용 숙소 목록 조회 API 중 오류 발생", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "숙소 목록 조회 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // [숙소 신청 관리 - 승인 대기 목록 조회]
    @GetMapping("/accommodations/pending")
    public String getPendingAccommodations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            PageDto<AccommodationResponseDto> pendingAccommodations = accommodationService.getPendingReviewAccommodations(pageable);
            
            model.addAttribute("page", pendingAccommodations);
            return "admin/approveAccommodation"; 
        } catch (SQLException e) {
            logger.error("승인 대기 중인 숙소 목록 조회 중 SQL 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "승인 대기 숙소 목록을 가져오는 중 오류가 발생했습니다.");
            return "admin/error";
        } catch (Exception e) {
            logger.error("승인 대기 중인 숙소 목록 조회 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "승인 대기 숙소 목록을 처리하는 중 알 수 없는 오류가 발생했습니다.");
            return "admin/error";
        }
    }

    // [숙소 신청 관리 - 승인 처리]
    @PostMapping("/accommodations/{accommodationId}/approve")
    public ResponseEntity<Map<String, Object>> approveAccommodation(@PathVariable Long accommodationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            accommodationService.approveAccommodation(accommodationId);
            response.put("success", true);
            response.put("message", "숙소 등록 신청이 승인되었습니다.");
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            logger.warn("승인 처리 중 숙소를 찾을 수 없음: accommodationId={}", accommodationId, e);
            response.put("success", false);
            response.put("message", "해당 숙소를 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            logger.warn("이미 처리되었거나 승인할 수 없는 상태의 숙소: accommodationId={}", accommodationId, e);
            response.put("success", false);
            response.put("message", e.getMessage()); 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        catch (Exception e) {
            logger.error("숙소 승인 처리 중 오류 발생: accommodationId={}", accommodationId, e);
            response.put("success", false);
            response.put("message", "숙소 승인 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // [숙소 신청 관리 - 거절 처리]
    @PostMapping("/accommodations/{accommodationId}/reject")
    public ResponseEntity<Map<String, Object>> rejectAccommodation(
            @PathVariable Long accommodationId,
            @RequestBody Map<String, String> payload) {
        Map<String, Object> response = new HashMap<>();
        String reason = payload.get("reason");

        if (reason == null || reason.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "거절 사유를 입력해주세요.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            accommodationService.rejectAccommodation(accommodationId, reason);
            response.put("success", true);
            response.put("message", "숙소 등록 신청이 거절되었습니다.");
            return ResponseEntity.ok(response);
        } catch (NoSuchElementException e) {
            logger.warn("거절 처리 중 숙소를 찾을 수 없음: accommodationId={}", accommodationId, e);
            response.put("success", false);
            response.put("message", "해당 숙소를 찾을 수 없습니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException e) {
            logger.warn("이미 처리되었거나 거절할 수 없는 상태의 숙소: accommodationId={}", accommodationId, e);
            response.put("success", false);
            response.put("message", e.getMessage()); 
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("숙소 거절 처리 중 오류 발생: accommodationId={}", accommodationId, e);
            response.put("success", false);
            response.put("message", "숙소 거절 처리 중 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ======================== 내부 헬퍼 메서드 ========================

    // 숙소 상세 정보를 가져옵니다.
    private Accommodation fetchAccommodationDetail(String contentId, HttpSession session) throws Exception {
        String commonUrl = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/detailCommon2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", mobileOs)
                .queryParam("MobileApp", mobileApp)
                .queryParam("_type", "json")
                .queryParam("numOfRows", 1)
                .queryParam("pageNo", 1)
                .queryParam("contentId", contentId)
                .build(false)
                .toUriString();

        logger.info("▶▶ detailCommon1 URL: {}", commonUrl);

        String commonResp = restTemplate.getForObject(URI.create(commonUrl), String.class);
        if (commonResp == null || commonResp.trim().startsWith("<")) {
            logger.error("detailCommon1 응답이 없거나 XML입니다.");
            return null;
        }

        JsonNode item = objectMapper.readTree(commonResp)
                .path("response").path("body").path("items").path("item");
        if (item.isArray()) item = item.get(0);
        if (item.isMissingNode()) return null;

        Accommodation acc = new Accommodation();
        acc.setTitle(item.path("title").asText(""));
        acc.setDescription(item.path("overview").asText(""));
        acc.setAddress(item.path("addr1").asText("") + " " + item.path("addr2").asText(""));
        acc.setPhone(item.path("tel").asText(""));

        // 홈페이지 href만 추출
        String hp = item.path("homepage").asText("");
        acc.setWebsite(hp.replaceAll("(?s).*href=\"([^\"]+)\".*", "$1"));

        acc.setLongitude(item.path("mapx").asDouble(0));
        acc.setLatitude(item.path("mapy").asDouble(0));

        // 시도/구군 검증
        int sc = item.path("areacode").asInt(0);
        if (!adminService.getAllSidos().stream().map(Sido::getCode).toList().contains(sc)) return null;
        acc.setSidoCode(sc);

        int gc = item.path("sigungucode").asInt(0);
        if (!adminService.getGugunsBySido(sc).stream().map(Gugun::getCode).toList().contains(gc)) return null;
        acc.setGugunCode(gc);

        // 소개정보(detailIntro2)
        String introUrl = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/detailIntro2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", mobileOs)
                .queryParam("MobileApp", mobileApp)
                .queryParam("_type", "json")
                .queryParam("numOfRows", 1)
                .queryParam("pageNo", 1)
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", 32)
                .build(false)
                .toUriString();

        logger.info("▶▶ detailIntro1 URL: {}", introUrl);
        String introResp = restTemplate.getForObject(URI.create(introUrl), String.class);
        if (introResp != null && !introResp.trim().startsWith("<")) {
            JsonNode intro = objectMapper.readTree(introResp)
                    .path("response").path("body").path("items").path("item");

            if (intro.isArray()) intro = intro.get(0);
            if (!intro.isMissingNode()) {
                // 체크인/체크아웃
                acc.setCheckInTime(parseTime(intro.path("checkintime").asText(), LocalTime.of(15, 0)));
                acc.setCheckOutTime(parseTime(intro.path("checkouttime").asText(), LocalTime.of(11, 0)));

                // 숙소 타입, 편의시설
                String rt = intro.path("roomtype").asText("");
                if (!rt.isBlank()) acc.setAccommodationType(rt);
                String sf = intro.path("subfacility").asText("");
                String fp = intro.path("foodplace").asText("");
                acc.setAmenities(
                        Stream.of(sf, fp)
                                .filter(s -> !s.isBlank())
                                .collect(Collectors.joining(", "))
                );
            }
        }

        // 세션에서 hostId 가져와 세팅
        Long hostIdToSet = null; // 최종적으로 숙소에 설정될 hostId
        Object sessionUserIdObj = session.getAttribute("userId");

        if (sessionUserIdObj != null) {
            Long parsedSessionUserId = null;
            if (sessionUserIdObj instanceof Long) {
                parsedSessionUserId = (Long) sessionUserIdObj;
            } else if (sessionUserIdObj instanceof Integer) {
                parsedSessionUserId = ((Integer) sessionUserIdObj).longValue();
            } else if (sessionUserIdObj instanceof String) {
                try {
                    parsedSessionUserId = Long.parseLong((String) sessionUserIdObj);
                } catch (NumberFormatException ignored) {
                    logger.warn("세션의 userId '{}'를 Long으로 파싱하는데 실패했습니다.", sessionUserIdObj);
                }
            }

            if (parsedSessionUserId != null) {
                User sessionAdminUser = userService.getUserById(parsedSessionUserId);
                if (sessionAdminUser != null && "ADMIN".equals(sessionAdminUser.getRole())) {
                    hostIdToSet = sessionAdminUser.getUserId();
                    logger.info("세션의 관리자 ID '{}'를 hostId로 우선 설정합니다.", hostIdToSet);
                } else {
                    logger.warn("세션 userId '{}'가 존재하지 않거나 ADMIN 역할이 아닙니다. 시스템의 다른 ADMIN 계정 할당을 시도합니다.", parsedSessionUserId);
                }
            }
        }

        // 세션에서 유효한 ADMIN ID를 찾지 못한 경우, 시스템 내 랜덤 ADMIN ID 할당 시도
        if (hostIdToSet == null) {
            List<User> adminUsers = userService.getAdminUsers(); // UserService에 이 메서드 구현 필요
            if (adminUsers != null && !adminUsers.isEmpty()) {
                Random random = new Random();
                User randomAdmin = adminUsers.get(random.nextInt(adminUsers.size()));
                hostIdToSet = randomAdmin.getUserId();
                logger.info("세션에 유효한 관리자가 없어, 시스템의 랜덤 ADMIN ID '{}' (username: '{}')를 hostId로 설정합니다.", hostIdToSet, randomAdmin.getUsername());
            } else {
                logger.error("시스템에 ADMIN 역할의 사용자가 존재하지 않아 hostId를 설정할 수 없습니다. 이 숙소({})는 등록할 수 없습니다.", contentId);
                return null; // ADMIN 사용자가 없으면 숙소 처리 중단
            }
        }
        if (hostIdToSet == null) { 
             logger.error("알 수 없는 오류로 hostId가 설정되지 않았습니다. contentId: {} 숙소 처리 중단.", contentId);
             return null;
        }
        acc.setHostId(hostIdToSet);
        acc.setStatus(Accommodation.AccommodationStatus.ACTIVE);
        return acc;
    }

    // 객실 정보를 가져옵니다.
    private List<Room> fetchRoomInfo(String contentId) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/detailInfo2")
                .queryParam("ServiceKey", serviceKey)
                .queryParam("_type", "json")
                .queryParam("contentTypeId", "32")
                .queryParam("contentId", contentId)
                .queryParam("MobileOS", mobileOs)
                .queryParam("MobileApp", mobileApp);

        URI uri = new URI(builder.build(false).toUriString());
        String response = restTemplate.getForObject(uri, String.class);

        if (response != null && response.trim().startsWith("<")) {
            logger.warn("객실 정보 API가 JSON이 아닌 응답을 반환했습니다: " + response.substring(0, Math.min(100, response.length())) + "...");
            return new ArrayList<>();
        }

        JsonNode root = objectMapper.readTree(response);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        List<Room> rooms = new ArrayList<>();
        long roomIdBase = Long.parseLong(contentId) * 10000;

        if (items.isArray()) {
            int roomIndex = 0;
            for (JsonNode item : items) {
                Room room = new Room();
                room.setName(item.path("roomtitle").asText("기본 객실"));
                room.setDescription(item.path("roomintro").asText(""));
                String priceStr = item.path("roombasecount").asText("0");
                try {
                    room.setPrice(new java.math.BigDecimal(priceStr.replaceAll("[^0-9]", "")));
                } catch (NumberFormatException e) {
                    room.setPrice(new java.math.BigDecimal("100000"));
                }
                String capacityStr = item.path("roombasecount").asText("2");
                try {
                    room.setCapacity(Integer.parseInt(capacityStr.replaceAll("[^0-9]", "")));
                } catch (NumberFormatException e) {
                    room.setCapacity(2);
                }
                room.setRoomCount(1);
                room.setRoomSize(new java.math.BigDecimal("20"));
                room.setRoomType("스탠다드");
                room.setBedType("더블");
                room.setBathroomCount(1);
                room.setAmenities("TV, 에어컨, 냉장고, 욕실용품");
                room.setStatus("AVAILABLE");
                room.setRoomId(roomIdBase + roomIndex);
                roomIndex++;
                room.setAccommodationId(Long.parseLong(contentId));
                rooms.add(room);
            }
        }

        if (rooms.isEmpty()) {
            Room defaultRoom = new Room();
            defaultRoom.setName("스탠다드 룸");
            defaultRoom.setDescription("편안한 스탠다드 객실입니다.");
            defaultRoom.setPrice(new java.math.BigDecimal("100000"));
            defaultRoom.setCapacity(2);
            defaultRoom.setRoomCount(1);
            defaultRoom.setRoomSize(new java.math.BigDecimal("20"));
            defaultRoom.setRoomType("스탠다드");
            defaultRoom.setBedType("더블");
            defaultRoom.setBathroomCount(1);
            defaultRoom.setAmenities("TV, 에어컨, 냉장고, 욕실용품");
            defaultRoom.setStatus("AVAILABLE");
            defaultRoom.setRoomId(roomIdBase);
            defaultRoom.setAccommodationId(Long.parseLong(contentId));
            rooms.add(defaultRoom);
        }
        return rooms;
    }

    // 이미지 정보를 가져옵니다.
    private List<Image> fetchImageInfo(String contentId, List<Room> rooms) throws Exception {
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/detailImage2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", mobileOs)
                .queryParam("MobileApp", mobileApp)
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("imageYN", "Y")
                .queryParam("numOfRows", 10)
                .queryParam("pageNo", 1)
                .build(false)
                .toUriString();

        logger.info("▶▶ detailImage2 URL: {}", url);
        String response = restTemplate.getForObject(new URI(url), String.class);

        if (response == null || response.trim().startsWith("<")) {
            logger.warn("detailImage2 응답이 없거나 XML/오류입니다: {}", response);
            return List.of();
        }

        JsonNode itemsNode = objectMapper.readTree(response)
                .path("response").path("body").path("items").path("item");
        List<Image> images = new ArrayList<>();

        boolean hasMainImage = false;
        int roomIndex = 0, roomCount = rooms.size();

        if (itemsNode.isArray()) {
            for (JsonNode item : itemsNode) {
                String originUrl = item.path("originimgurl").asText("");
                if (originUrl.isBlank()) {
                    continue;
                }

                Image img = new Image();
                if (!originUrl.startsWith("http")) {
                    originUrl = "http://" + originUrl;
                }
                img.setImageUrl(originUrl);
                img.setCaption(item.path("imgname").asText(""));

                if (!hasMainImage) {
                    img.setIsMain(true);
                    img.setReferenceType("ACCOMMODATION");
                    img.setReferenceId(Long.parseLong(contentId));
                    hasMainImage = true;
                } else {
                    img.setIsMain(false);
                    img.setReferenceType("ROOM");
                    if (roomCount > 0) {
                        Room r = rooms.get(roomIndex % roomCount);
                        img.setReferenceId(r.getRoomId());
                        roomIndex++;
                    } else {
                        img.setReferenceId(Long.parseLong(contentId));
                    }
                }
                images.add(img);
            }
        }

        if (images.isEmpty()) {
            logger.info("contentId={} 에 대한 이미지가 없습니다.", contentId);
        }
        return images;
    }

    // raw 문자열에서 "HH:mm" 패턴을 찾아 파싱합니다.
    private LocalTime parseTime(String raw, LocalTime fallback) {
        if (raw == null) return fallback;
        Matcher m = Pattern.compile("(\\d{1,2}:\\d{2})").matcher(raw);
        if (m.find()) {
            try {
                return LocalTime.parse(m.group(1));
            } catch (DateTimeParseException ignored) {
            }
        }
        return fallback;
    }

}