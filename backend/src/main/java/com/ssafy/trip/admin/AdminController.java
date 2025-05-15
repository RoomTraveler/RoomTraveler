package com.ssafy.trip.admin;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Image;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.host.HostService;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 관리자 기능을 위한 컨트롤러
 */
@Controller
@RequestMapping("/admin")
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
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 생성자: 필요한 의존성을 주입받습니다.
     */
    public AdminController(AccommodationService accommodationService, 
                          UserService userService,
                          AdminService adminService,
                          HostService hostService
                          ) {
        this.accommodationService = accommodationService;
        this.userService = userService;
        this.adminService = adminService;
        this.hostService = hostService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 관리자 대시보드 페이지를 표시합니다.
     */
    @GetMapping("")
    public String adminDashboard(Model model) {
        try {
            // 사용자 통계 정보
            Map<String, Long> userStats = adminService.getUserStatistics();
            model.addAttribute("userStats", userStats);

            // 숙소 통계 정보
            Map<String, Long> accommodationStats = adminService.getAccommodationStatistics();
            model.addAttribute("accommodationStats", accommodationStats);

            // 예약 통계 정보
            Map<String, Long> reservationStats = adminService.getReservationStatistics();
            model.addAttribute("reservationStats", reservationStats);

            return "admin/dashboard";
        } catch (Exception e) {
            logger.error("관리자 대시보드 로딩 중 오류 발생", e);
            model.addAttribute("errorMessage", "대시보드 정보를 불러오는 중 오류가 발생했습니다.");
            return "error/500";
        }
    }

    /**
     * 사용자 관리 페이지를 표시합니다.
     */
    @GetMapping("/users")
    public String manageUsers(Model model) {
        try {
            List<User> users = userService.getAllUsers();
            model.addAttribute("users", users);
            return "admin/users";
        } catch (Exception e) {
            logger.error("사용자 관리 페이지 로딩 중 오류 발생", e);
            model.addAttribute("errorMessage", "사용자 정보를 불러오는 중 오류가 발생했습니다.");
            return "error/500";
        }
    }

    /**
     * 사용자 상태를 변경합니다.
     */
    @PostMapping("/users/{userId}/status")
    @ResponseBody
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

    /**
     * 숙소 관리 페이지를 표시합니다.
     */
    @GetMapping("/accommodations")
    public String manageAccommodations(Model model) {
        try {
            List<Accommodation> accommodations = accommodationService.getAllAccommodations();

            // 숙소 ID가 null인 숙소 개수 확인
            int nullIdCount = 0;
            for (Accommodation acc : accommodations) {
                if (acc.getAccommodationId() == null) {
                    nullIdCount++;
                    logger.warn("숙소 ID가 null입니다. 제목: {}", acc.getTitle());
                }
            }

            logger.info("총 숙소 수: {}, null ID 숙소 수: {}", accommodations.size(), nullIdCount);

            model.addAttribute("accommodations", accommodations);

            // 시도 목록을 가져와서 모델에 추가
            try {
                List<Sido> sidos = adminService.getAllSidos();
                model.addAttribute("sidos", sidos);
            } catch (Exception e) {
                logger.warn("시도 목록을 가져오는 중 오류 발생", e);
                // 오류가 발생해도 페이지는 계속 로드
            }

            return "admin/accommodations";
        } catch (Exception e) {
            logger.error("숙소 관리 페이지 로딩 중 오류 발생", e);
            model.addAttribute("errorMessage", "숙소 정보를 불러오는 중 오류가 발생했습니다.");
            return "error/500";
        }
    }

    /**
     * 숙소 상태를 변경합니다.
     */
    @PostMapping("/accommodations/{accommodationId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateAccommodationStatus(
            @PathVariable Long accommodationId,
            @RequestParam String status) {
        try {
            int result = accommodationService.updateAccommodationStatus(accommodationId, status);
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

    /**
     * 숙소를 삭제합니다.
     */
    @DeleteMapping("/accommodations/{accommodationId}")
    @ResponseBody
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

    /**
     * 모든 숙소를 삭제합니다.
     */
    @DeleteMapping("/accommodations/all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAllAccommodations() {
        try {
            logger.info("모든 숙소 삭제 시도");

            // 모든 숙소 삭제 실행
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

    /**
     * 지역 데이터 관리 페이지를 표시합니다.
     */
    @GetMapping("/regions")
    public String manageRegions(Model model) {
        try {
            List<Sido> sidos = adminService.getAllSidos();
            model.addAttribute("sidos", sidos);
            return "admin/regions";
        } catch (Exception e) {
            logger.error("지역 데이터 관리 페이지 로딩 중 오류 발생", e);
            model.addAttribute("errorMessage", "지역 정보를 불러오는 중 오류가 발생했습니다.");
            return "error/500";
        }
    }

    /**
     * 시도 코드에 해당하는 구군 목록을 조회합니다.
     */
    @GetMapping("/regions/guguns")
    @ResponseBody
    public ResponseEntity<List<Gugun>> getGugunsBySido(@RequestParam int sidoCode) {
        try {
            List<Gugun> guguns = adminService.getGugunsBySido(sidoCode);
            return ResponseEntity.ok(guguns);
        } catch (Exception e) {
            logger.error("구군 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * TourAPI에서 시도 데이터를 가져와 DB에 저장합니다.
     */
    @PostMapping("/regions/import-sidos")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importSidos() {
        try {
            // TourAPI에서 시도 데이터 가져오기

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/areaCode1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", mobileOs)
                    .queryParam("MobileApp", mobileApp)
                    .queryParam("_type", "json")
                    .queryParam("numOfRows", 100)
                    .queryParam("pageNo", 1);

            URI uri = new URI(builder.build(false).toUriString());
            String response = restTemplate.getForObject(uri, String.class);
            // 3) 로그에 찍기
            logger.info("▶▶ TourAPI 시도 조회 URL: {}", uri );

            // JSON 파싱
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            List<Sido> sidos = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    int code = item.path("code").asInt();
                    String name = item.path("name").asText();

                    Sido sido = new Sido(code, name);
                    sidos.add(sido);
                }
            }

            // DB에 저장
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

    /**
     * TourAPI에서 구군 데이터를 가져와 DB에 저장합니다.
     */
    @PostMapping("/regions/import-guguns")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importGuguns() {
        try {
            // 모든 시도 코드 가져오기
            List<Sido> sidos = adminService.getAllSidos();
            if (sidos.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "시도 데이터가 없습니다. 먼저 시도 데이터를 가져와주세요.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }


            int totalCount = 0;

            // 각 시도별로 구군 데이터 가져오기
            for (Sido sido : sidos) {
                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromHttpUrl(baseUrl + "/areaCode1")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("MobileOS", mobileOs)
                        .queryParam("MobileApp", mobileApp)
                        .queryParam("_type", "json")
                        .queryParam("numOfRows", 100)
                        .queryParam("pageNo", 1)
                        .queryParam("areaCode", sido.getCode());

                URI uri = new URI(builder.build(false).toUriString());
                String response = restTemplate.getForObject(uri, String.class);


                // JSON 파싱
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

                // DB에 저장
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

    /**
     * 숙소 상세 정보를 가져옵니다.
     * 공통정보임 ㅇㅇ
     */
    private Accommodation fetchAccommodationDetail(String contentId, HttpSession session) throws Exception {
        // 1) 공통정보(detailCommon1)에 numOfRows/pageNo 추가
        String commonUrl = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/detailCommon2")
                .queryParam("serviceKey",   serviceKey)
                .queryParam("MobileOS",     mobileOs)
                .queryParam("MobileApp",    mobileApp)
                .queryParam("_type",        "json")
                .queryParam("numOfRows",    1)
                .queryParam("pageNo",       1)
                .queryParam("contentId",    contentId)
//                .queryParam("contentTypeId",32)
//                .queryParam("defaultYN",    "Y")
//                .queryParam("firstImageYN", "Y")
//                .queryParam("areacodeYN",   "Y")
//                .queryParam("catcodeYN",    "Y")
//                .queryParam("addrinfoYN",   "Y")
//                .queryParam("mapinfoYN",    "Y")
//                .queryParam("overviewYN",   "Y")
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


        // 2) 소개정보(detailIntro1)에 numOfRows/pageNo 추가
        String introUrl = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/detailIntro2")
                .queryParam("serviceKey",   serviceKey)
                .queryParam("MobileOS",     mobileOs)
                .queryParam("MobileApp",    mobileApp)
                .queryParam("_type",        "json")
                .queryParam("numOfRows",    1)
                .queryParam("pageNo",       1)
                .queryParam("contentId",    contentId)
                .queryParam("contentTypeId",32)
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
                acc.setCheckInTime(parseTime(intro.path("checkintime").asText(), LocalTime.of(15,0)));
                acc.setCheckOutTime(parseTime(intro.path("checkouttime").asText(), LocalTime.of(11,0)));

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

        // 3) 세션에서 hostId 가져와 세팅
        Long userId = null;
        Object userIdObj = session.getAttribute("userId");
        if (userIdObj instanceof Long) {
            userId = (Long) userIdObj;
        } else if (userIdObj instanceof Integer) {
            userId = ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof String) {
            try { userId = Long.parseLong((String)userIdObj); }
            catch (NumberFormatException ignored) {}
        }
        if (userId != null) {
            acc.setHostId(userId);
        } else {
            String email = (String)session.getAttribute("email");
            // 기본 관리자 ID = 6
            acc.setHostId(6L);
            if ("admin.lee@example.com".equals(email)) {
                acc.setHostId(6L);
            }
        }

        acc.setStatus("ACTIVE");
        return acc;
    }
    /**
     * 객실 정보를 가져옵니다.
     * @param contentId 숙소 컨텐츠 ID (TourAPI)
     * @return 객실 목록
     */
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

        // 응답이 JSON이 아닌 경우 처리 (HTML 등)
        if (response != null && response.trim().startsWith("<")) {
            logger.warn("객실 정보 API가 JSON이 아닌 응답을 반환했습니다: " + response.substring(0, Math.min(100, response.length())) + "...");
            return new ArrayList<>(); // 빈 객실 목록 반환
        }

        // JSON 파싱
        JsonNode root = objectMapper.readTree(response);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        List<Room> rooms = new ArrayList<>();
        long roomIdBase = Long.parseLong(contentId) * 10000; // 고유한 room_id를 생성하기 위한 베이스 값

        if (items.isArray()) {
            int roomIndex = 0;
            for (JsonNode item : items) {
                Room room = new Room();

                // 객실 정보 매핑
                room.setName(item.path("roomtitle").asText("기본 객실"));
                room.setDescription(item.path("roomintro").asText(""));

                // 가격 정보가 있는 경우에만 설정
                String priceStr = item.path("roombasecount").asText("0");
                try {
                    room.setPrice(new java.math.BigDecimal(priceStr.replaceAll("[^0-9]", "")));
                } catch (NumberFormatException e) {
                    room.setPrice(new java.math.BigDecimal("100000"));  // 기본 가격
                }

                // 수용 인원 정보가 있는 경우에만 설정
                String capacityStr = item.path("roombasecount").asText("2");
                try {
                    room.setCapacity(Integer.parseInt(capacityStr.replaceAll("[^0-9]", "")));
                } catch (NumberFormatException e) {
                    room.setCapacity(2);  // 기본 수용 인원
                }

                room.setRoomCount(1);  // 기본값
                room.setRoomSize(new java.math.BigDecimal("20"));  // 기본값
                room.setRoomType("스탠다드");  // 기본값
                room.setBedType("더블");  // 기본값
                room.setBathroomCount(1);  // 기본값
                room.setAmenities("TV, 에어컨, 냉장고, 욕실용품");  // 기본값
                room.setStatus("AVAILABLE");

                // 고유한 임시 ID 설정 (실제 저장 시 자동 생성됨)
                room.setRoomId(roomIdBase + roomIndex);
                roomIndex++;

                // 숙소 ID 설정 (contentId를 숙소 ID로 사용)
                room.setAccommodationId(Long.parseLong(contentId));

                rooms.add(room);
            }
        }

        // 객실 정보가 없는 경우 기본 객실 하나 생성
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
            defaultRoom.setRoomId(roomIdBase); // 고유한 임시 ID 설정

            // 숙소 ID 설정 (contentId를 숙소 ID로 사용)
            defaultRoom.setAccommodationId(Long.parseLong(contentId));

            rooms.add(defaultRoom);
        }

        return rooms;
    }

    /**
     * 이미지 정보를 가져옵니다.
     * @param contentId 숙소 컨텐츠 ID (TourAPI)
     * @param rooms 객실 목록 (이미지를 객실에 분배하기 위해 필요)
     * @return 이미지 목록
     */
    private List<Image> fetchImageInfo(String contentId, List<Room> rooms) throws Exception {
        // 1) detailImage2 호출 URL 구성
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/detailImage2")
                .queryParam("serviceKey",   serviceKey)
                .queryParam("MobileOS",     mobileOs)
                .queryParam("MobileApp",    mobileApp)
                .queryParam("_type",        "json")
                .queryParam("contentId",    contentId)
                .queryParam("imageYN",      "Y")
                .queryParam("numOfRows",    10)
                .queryParam("pageNo",       1)
                .build(false)
                .toUriString();

        logger.info("▶▶ detailImage2 URL: {}", url);
        String response = restTemplate.getForObject(new URI(url), String.class);

        // JSON 아닌 응답이면 빈 리스트
        if (response == null || response.trim().startsWith("<")) {
            logger.warn("detailImage2 응답이 없거나 XML/오류입니다: {}", response);
            return List.of();
        }

        // 2) JSON 파싱
        JsonNode itemsNode = objectMapper.readTree(response)
                .path("response").path("body").path("items").path("item");
        List<Image> images = new ArrayList<>();

        boolean hasMainImage = false;
        int roomIndex = 0, roomCount = rooms.size();

        if (itemsNode.isArray()) {
            for (JsonNode item : itemsNode) {
                String originUrl = item.path("originimgurl").asText("");
                if (originUrl.isBlank()) {
                    // 빈 URL이면 건너뛰기
                    continue;
                }

                Image img = new Image();
                // URL 보정
                if (!originUrl.startsWith("http")) {
                    originUrl = "http://" + originUrl;
                }
                img.setImageUrl(originUrl);
                img.setCaption(item.path("imgname").asText(""));

                if (!hasMainImage) {
                    // 첫 번째 이미지를 대표 이미지로
                    img.setIsMain(true);
                    img.setReferenceType("ACCOMMODATION");
                    img.setReferenceId(Long.parseLong(contentId));
                    hasMainImage = true;
                } else {
                    // 나머지는 객실 이미지
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


    /**
     * raw 문자열에서 "HH:mm" 패턴을 찾아 파싱합니다.
     * 못 찾거나 예외 발생 시 fallback 리턴.
     */
    private LocalTime parseTime(String raw, LocalTime fallback) {
        if (raw == null) return fallback;
        // "익일11:00", "11:00", "15:30" 등에서 HH:mm 부분만 캡처
        Matcher m = Pattern.compile("(\\d{1,2}:\\d{2})").matcher(raw);
        if (m.find()) {
            try {
                return LocalTime.parse(m.group(1));
            } catch (DateTimeParseException ignored) { }
        }
        return fallback;
    }

}
