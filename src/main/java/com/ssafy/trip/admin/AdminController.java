package com.ssafy.trip.admin;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Image;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.service.AccommodationService;
import com.ssafy.trip.host.Host;
import com.ssafy.trip.host.HostService;
import com.ssafy.trip.map.MapDTO.Sido;
import com.ssafy.trip.map.MapDTO.Gugun;
import com.ssafy.trip.tourapi.TourApiProperties;
import com.ssafy.trip.user.User;
import com.ssafy.trip.user.UserService;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 관리자 기능을 위한 컨트롤러
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final AccommodationService accommodationService;
    private final UserService userService;
    private final AdminService adminService;
    private final HostService hostService;
    private final TourApiProperties tourApiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 생성자: 필요한 의존성을 주입받습니다.
     */
    public AdminController(AccommodationService accommodationService, 
                          UserService userService,
                          AdminService adminService,
                          HostService hostService,
                          TourApiProperties tourApiProperties) {
        this.accommodationService = accommodationService;
        this.userService = userService;
        this.adminService = adminService;
        this.hostService = hostService;
        this.tourApiProperties = tourApiProperties;
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
            model.addAttribute("accommodations", accommodations);
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
            String serviceKey = tourApiProperties.getServiceKey();
            String baseUrl = tourApiProperties.getBaseUrl();

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/areaCode1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", tourApiProperties.getMobileOs())
                    .queryParam("MobileApp", tourApiProperties.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("numOfRows", 100)
                    .queryParam("pageNo", 1);

            URI uri = new URI(builder.build(false).toUriString());
            String response = restTemplate.getForObject(uri, String.class);

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

            String serviceKey = tourApiProperties.getServiceKey();
            String baseUrl = tourApiProperties.getBaseUrl();
            int totalCount = 0;

            // 각 시도별로 구군 데이터 가져오기
            for (Sido sido : sidos) {
                UriComponentsBuilder builder = UriComponentsBuilder
                        .fromHttpUrl(baseUrl + "/areaCode1")
                        .queryParam("serviceKey", serviceKey)
                        .queryParam("MobileOS", tourApiProperties.getMobileOs())
                        .queryParam("MobileApp", tourApiProperties.getMobileApp())
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
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            String serviceKey = tourApiProperties.getServiceKey();
            String baseUrl = tourApiProperties.getBaseUrl();

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/searchStay1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", tourApiProperties.getMobileOs())
                    .queryParam("MobileApp", tourApiProperties.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("listYN", "Y")
                    .queryParam("arrange", "A")
                    .queryParam("numOfRows", limit)
                    .queryParam("pageNo", 1);

            // 선택적 파라미터 추가
            if (sidoCode != null) {
                builder.queryParam("areaCode", sidoCode);
            }
            if (gugunCode != null) {
                builder.queryParam("sigunguCode", gugunCode);
            }

            URI uri = new URI(builder.build(false).toUriString());
            String response = restTemplate.getForObject(uri, String.class);

            // JSON 파싱
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("response").path("body").path("items").path("item");

            int importedCount = 0;

            if (items.isArray()) {
                for (JsonNode item : items) {
                    String contentId = item.path("contentid").asText();

                    // 숙소 상세 정보 가져오기
                    Accommodation accommodation = fetchAccommodationDetail(contentId);
                    if (accommodation == null) continue;

                    // 객실 정보 가져오기
                    List<Room> rooms = fetchRoomInfo(contentId);
                    if (rooms.isEmpty()) continue;

                    // 이미지 정보 가져오기
                    List<Image> images = fetchImageInfo(contentId);

                    // DB에 저장
                    Long accommodationId = accommodationService.importFromApi(accommodation, rooms, images);
                    if (accommodationId != null) {
                        importedCount++;
                    }

                    // 요청한 개수만큼 가져왔으면 중단
                    if (importedCount >= limit) {
                        break;
                    }
                }
            }

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
     */
    private Accommodation fetchAccommodationDetail(String contentId) throws Exception {
        String serviceKey = tourApiProperties.getServiceKey();
        String baseUrl = tourApiProperties.getBaseUrl();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/detailCommon1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", tourApiProperties.getMobileOs())
                .queryParam("MobileApp", tourApiProperties.getMobileApp())
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("defaultYN", "Y")
                .queryParam("firstImageYN", "Y")
                .queryParam("areacodeYN", "Y")
                .queryParam("catcodeYN", "Y")
                .queryParam("addrinfoYN", "Y")
                .queryParam("mapinfoYN", "Y")
                .queryParam("overviewYN", "Y");

        URI uri = new URI(builder.build(false).toUriString());
        String response = restTemplate.getForObject(uri, String.class);

        // JSON 파싱
        JsonNode root = objectMapper.readTree(response);
        JsonNode item = root.path("response").path("body").path("items").path("item");

        if (item.isArray() && item.size() > 0) {
            item = item.get(0);
        }

        if (item.isMissingNode()) {
            return null;
        }

        // 숙소 정보 매핑
        Accommodation accommodation = new Accommodation();
        accommodation.setTitle(item.path("title").asText(""));
        accommodation.setDescription(item.path("overview").asText(""));
        accommodation.setAddress(item.path("addr1").asText("") + " " + item.path("addr2").asText(""));
        accommodation.setSidoCode(item.path("areacode").asInt(0));
        accommodation.setGugunCode(item.path("sigungucode").asInt(0));

        // 위도, 경도가 있는 경우에만 설정
        if (!item.path("mapx").isMissingNode() && !item.path("mapy").isMissingNode()) {
            accommodation.setLongitude(item.path("mapx").asDouble(0));
            accommodation.setLatitude(item.path("mapy").asDouble(0));
        }

        accommodation.setPhone(item.path("tel").asText(""));
        accommodation.setEmail("");  // API에서 제공하지 않음
        accommodation.setWebsite(item.path("homepage").asText(""));

        // 기본 체크인/아웃 시간 설정
        accommodation.setCheckInTime(java.time.LocalTime.of(15, 0));
        accommodation.setCheckOutTime(java.time.LocalTime.of(11, 0));

        accommodation.setAmenities("");  // API에서 제공하지 않음
        accommodation.setStatus("ACTIVE");

        // 호스트 ID는 임시로 1로 설정 (실제 구현에서는 적절한 호스트 ID 할당 필요)
        accommodation.setHostId(1L);

        return accommodation;
    }

    /**
     * 객실 정보를 가져옵니다.
     */
    private List<Room> fetchRoomInfo(String contentId) throws Exception {
        String serviceKey = tourApiProperties.getServiceKey();
        String baseUrl = tourApiProperties.getBaseUrl();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/detailInfo1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", tourApiProperties.getMobileOs())
                .queryParam("MobileApp", tourApiProperties.getMobileApp())
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("contentTypeId", "32");  // 숙박 타입 ID

        URI uri = new URI(builder.build(false).toUriString());
        String response = restTemplate.getForObject(uri, String.class);

        // JSON 파싱
        JsonNode root = objectMapper.readTree(response);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        List<Room> rooms = new ArrayList<>();

        if (items.isArray()) {
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
                room.setBedType("더블");  // 기본값
                room.setAmenities("TV, 에어컨, 냉장고, 욕실용품");  // 기본값
                room.setStatus("AVAILABLE");

                // 임시 ID 설정 (실제 저장 시 자동 생성됨)
                room.setRoomId(Long.parseLong(contentId));

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
            defaultRoom.setBedType("더블");
            defaultRoom.setAmenities("TV, 에어컨, 냉장고, 욕실용품");
            defaultRoom.setStatus("AVAILABLE");
            defaultRoom.setRoomId(Long.parseLong(contentId));

            rooms.add(defaultRoom);
        }

        return rooms;
    }

    /**
     * 이미지 정보를 가져옵니다.
     */
    private List<Image> fetchImageInfo(String contentId) throws Exception {
        String serviceKey = tourApiProperties.getServiceKey();
        String baseUrl = tourApiProperties.getBaseUrl();

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/detailImage1")
                .queryParam("serviceKey", serviceKey)
                .queryParam("MobileOS", tourApiProperties.getMobileOs())
                .queryParam("MobileApp", tourApiProperties.getMobileApp())
                .queryParam("_type", "json")
                .queryParam("contentId", contentId)
                .queryParam("imageYN", "Y")
                .queryParam("subImageYN", "Y");

        URI uri = new URI(builder.build(false).toUriString());
        String response = restTemplate.getForObject(uri, String.class);

        // JSON 파싱
        JsonNode root = objectMapper.readTree(response);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        List<Image> images = new ArrayList<>();

        if (items.isArray()) {
            boolean hasMainImage = false;

            for (JsonNode item : items) {
                Image image = new Image();

                // 이미지 정보 매핑
                image.setImageUrl(item.path("originimgurl").asText(""));
                image.setCaption(item.path("imgname").asText(""));

                // 첫 번째 이미지를 대표 이미지로 설정
                if (!hasMainImage) {
                    image.setIsMain(true);
                    image.setReferenceType("ACCOMMODATION");
                    hasMainImage = true;
                } else {
                    image.setIsMain(false);
                    image.setReferenceType("ROOM");
                }

                // 임시 ID 설정 (실제 저장 시 자동 생성됨)
                image.setReferenceId(Long.parseLong(contentId));

                images.add(image);
            }
        }

        return images;
    }

    /**
     * 샘플 객실 및 지역 코드 데이터를 생성합니다.
     */
    @PostMapping("/create-sample-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createSampleData() {
        try {
            // 샘플 시도 데이터 생성
            List<Sido> sampleSidos = createSampleSidos();
            int sidoCount = adminService.importSidos(sampleSidos);

            // 샘플 구군 데이터 생성
            int gugunCount = 0;
            for (Sido sido : sampleSidos) {
                List<Gugun> sampleGuguns = createSampleGuguns(sido.getCode());
                gugunCount += adminService.importGuguns(sido.getCode(), sampleGuguns);
            }

            // 샘플 호스트 생성
            Long hostId = createSampleHost();
            if (hostId == null) {
                throw new RuntimeException("샘플 호스트 생성에 실패했습니다.");
            }

            // 샘플 숙소 및 객실 데이터 생성
            int accommodationCount = 0;
            for (int i = 0; i < 5; i++) {
                Accommodation accommodation = createSampleAccommodation(i + 1, hostId);
                List<Room> rooms = createSampleRooms(i + 1);
                List<Image> images = createSampleImages(i + 1);

                Long accommodationId = accommodationService.importFromApi(accommodation, rooms, images);
                if (accommodationId != null) {
                    accommodationCount++;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "샘플 데이터가 성공적으로 생성되었습니다. " + 
                    sidoCount + "개의 시도, " + 
                    gugunCount + "개의 구군, " + 
                    accommodationCount + "개의 숙소 데이터가 생성되었습니다.");

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("샘플 데이터 생성 중 오류 발생", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "샘플 데이터 생성 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 샘플 시도 데이터를 생성합니다.
     */
    private List<Sido> createSampleSidos() {
        List<Sido> sidos = new ArrayList<>();
        sidos.add(new Sido(1, "서울"));
        sidos.add(new Sido(2, "인천"));
        sidos.add(new Sido(3, "대전"));
        sidos.add(new Sido(4, "대구"));
        sidos.add(new Sido(5, "광주"));
        sidos.add(new Sido(6, "부산"));
        sidos.add(new Sido(7, "울산"));
        sidos.add(new Sido(8, "세종"));
        sidos.add(new Sido(31, "경기도"));
        sidos.add(new Sido(32, "강원도"));
        return sidos;
    }

    /**
     * 샘플 구군 데이터를 생성합니다.
     */
    private List<Gugun> createSampleGuguns(int sidoCode) {
        List<Gugun> guguns = new ArrayList<>();

        switch (sidoCode) {
            case 1: // 서울
                guguns.add(new Gugun(1, "강남구"));
                guguns.add(new Gugun(2, "강동구"));
                guguns.add(new Gugun(3, "강서구"));
                guguns.add(new Gugun(4, "관악구"));
                guguns.add(new Gugun(5, "마포구"));
                break;
            case 2: // 인천
                guguns.add(new Gugun(1, "중구"));
                guguns.add(new Gugun(2, "동구"));
                guguns.add(new Gugun(3, "미추홀구"));
                guguns.add(new Gugun(4, "연수구"));
                guguns.add(new Gugun(5, "남동구"));
                break;
            case 31: // 경기도
                guguns.add(new Gugun(1, "수원시"));
                guguns.add(new Gugun(2, "성남시"));
                guguns.add(new Gugun(3, "용인시"));
                guguns.add(new Gugun(4, "부천시"));
                guguns.add(new Gugun(5, "안산시"));
                break;
            default:
                // 기본 구군 데이터
                for (int i = 1; i <= 5; i++) {
                    guguns.add(new Gugun(i, "샘플구" + i));
                }
        }

        return guguns;
    }

    /**
     * 샘플 호스트 데이터를 생성합니다.
     */
    private Long createSampleHost() throws SQLException {
        // 이미 존재하는 호스트 ID 4 (hostkim) 또는 5 (hostlee)를 사용하거나
        // 호스트가 없는 경우 새로 생성

        // 먼저 ID 4로 시도
        Host existingHost = hostService.getHostById(4L);
        if (existingHost != null) {
            return existingHost.getHostId();
        }

        // ID 4가 없으면 ID 5로 시도
        existingHost = hostService.getHostById(5L);
        if (existingHost != null) {
            return existingHost.getHostId();
        }

        // 호스트 생성 - 먼저 ID 4로 시도
        Host host = new Host();
        host.setHostId(4L); // users.sql에 있는 hostkim의 ID
        host.setBusinessName("샘플 호스트 비즈니스");
        host.setBusinessRegNo("123-45-67890");
        host.setBankAccount("국민은행 123-456-789012");
        host.setProfileText("이것은 테스트를 위한 샘플 호스트입니다.");
        host.setHostStatus("APPROVED");

        try {
            int result = hostService.registHost(host);
            if (result > 0) {
                return host.getHostId();
            }
        } catch (Exception e) {
            logger.warn("ID 4로 호스트 생성 실패: " + e.getMessage());
        }

        // ID 4로 실패하면 ID 5로 시도
        host.setHostId(5L); // users.sql에 있는 hostlee의 ID
        try {
            int result = hostService.registHost(host);
            if (result > 0) {
                return host.getHostId();
            }
        } catch (Exception e) {
            logger.warn("ID 5로 호스트 생성 실패: " + e.getMessage());
        }

        // 모든 시도가 실패하면 새 호스트 ID 생성
        // 실제 운영 환경에서는 이 부분을 적절히 수정해야 함
        host.setHostId(1L); // 기본 관리자 계정 사용
        try {
            int result = hostService.registHost(host);
            if (result > 0) {
                return host.getHostId();
            }
        } catch (Exception e) {
            logger.warn("ID 1로 호스트 생성 실패: " + e.getMessage());
        }

        // 그래도 실패하면 임의의 ID 사용
        return 1L; // 기본값으로 1 반환
    }

    /**
     * 샘플 숙소 데이터를 생성합니다.
     */
    private Accommodation createSampleAccommodation(int index, Long hostId) {
        Accommodation accommodation = new Accommodation();
        accommodation.setTitle("샘플 숙소 " + index);
        accommodation.setDescription("이것은 테스트를 위한 샘플 숙소입니다. 실제 숙소가 아닙니다.");
        accommodation.setAddress("서울시 강남구 테헤란로 " + (100 + index * 10) + "번길 " + index);
        accommodation.setSidoCode(1);  // 서울
        accommodation.setGugunCode(1);  // 강남구
        accommodation.setLongitude(126.9 + (index * 0.01));
        accommodation.setLatitude(37.5 + (index * 0.01));
        accommodation.setPhone("02-1234-" + (5678 + index));
        accommodation.setEmail("sample" + index + "@example.com");
        accommodation.setWebsite("https://example.com/hotel" + index);
        accommodation.setCheckInTime(java.time.LocalTime.of(15, 0));
        accommodation.setCheckOutTime(java.time.LocalTime.of(11, 0));
        accommodation.setAmenities("WiFi, 주차장, 수영장, 헬스장, 레스토랑");
        accommodation.setStatus("ACTIVE");
        accommodation.setHostId(hostId);

        return accommodation;
    }

    /**
     * 샘플 객실 데이터를 생성합니다.
     */
    private List<Room> createSampleRooms(int accommodationIndex) {
        List<Room> rooms = new ArrayList<>();

        // 각 숙소마다 3개의 객실 생성
        String[] roomTypes = {"스탠다드", "디럭스", "스위트"};
        String[] bedTypes = {"싱글", "더블", "트윈"};

        for (int i = 0; i < 3; i++) {
            Room room = new Room();
            room.setName(roomTypes[i] + " 룸");
            room.setDescription(roomTypes[i] + " 타입의 객실입니다. 편안한 휴식을 제공합니다.");
            room.setPrice(new BigDecimal(100000 + (i * 50000)));
            room.setCapacity(i + 2);
            room.setRoomCount(5);
            room.setRoomSize(new BigDecimal(20 + (i * 10)));
            room.setBedType(bedTypes[i]);
            room.setAmenities("TV, 에어컨, 냉장고, 욕실용품, WiFi");
            room.setStatus("AVAILABLE");
            room.setRoomId((long)(accommodationIndex * 10 + i));

            rooms.add(room);
        }

        return rooms;
    }

    /**
     * 샘플 이미지 데이터를 생성합니다.
     */
    private List<Image> createSampleImages(int accommodationIndex) {
        List<Image> images = new ArrayList<>();

        // 숙소 대표 이미지
        Image mainImage = new Image();
        mainImage.setImageUrl("https://via.placeholder.com/800x600?text=Sample+Accommodation+" + accommodationIndex);
        mainImage.setCaption("샘플 숙소 " + accommodationIndex + " 대표 이미지");
        mainImage.setIsMain(true);
        mainImage.setReferenceType("ACCOMMODATION");
        mainImage.setReferenceId((long)accommodationIndex);
        images.add(mainImage);

        // 객실 이미지
        for (int i = 0; i < 3; i++) {
            Image roomImage = new Image();
            roomImage.setImageUrl("https://via.placeholder.com/600x400?text=Sample+Room+" + (accommodationIndex * 10 + i));
            roomImage.setCaption("샘플 객실 " + (i + 1) + " 이미지");
            roomImage.setIsMain(false);
            roomImage.setReferenceType("ROOM");
            roomImage.setReferenceId((long)(accommodationIndex * 10 + i));
            images.add(roomImage);
        }

        return images;
    }
}
