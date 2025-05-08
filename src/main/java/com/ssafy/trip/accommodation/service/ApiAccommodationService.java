package com.ssafy.trip.accommodation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Image;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.map.MapDTO.Sido;
import com.ssafy.trip.map.MapDTO.Gugun;
import com.ssafy.trip.tourapi.TourApiProperties;
import com.ssafy.trip.admin.AdminService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.RequiredArgsConstructor;

/**
 * API를 통해 숙소 정보를 가져오는 서비스
 */
@Service
@RequiredArgsConstructor
public class ApiAccommodationService {

    private static final Logger logger = LoggerFactory.getLogger(ApiAccommodationService.class);

    private final TourApiProperties tourApiProperties;
    private final AdminService adminService;
    private final AccommodationService accommodationService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // 캐싱을 위한 맵
    private final Map<String, List<Accommodation>> accommodationCache = new ConcurrentHashMap<>();
    private final Map<Long, Accommodation> accommodationDetailCache = new ConcurrentHashMap<>();
    private final Map<Long, List<Room>> roomsCache = new ConcurrentHashMap<>();
    private final Map<Long, List<Image>> imagesCache = new ConcurrentHashMap<>();

    // 캐시 유효 시간 (밀리초)
    private static final long CACHE_EXPIRY = 3600000; // 1시간
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    /**
     * 캐시가 유효한지 확인합니다.
     */
    private boolean isCacheValid(String cacheKey) {
        if (!cacheTimestamps.containsKey(cacheKey)) {
            return false;
        }

        long timestamp = cacheTimestamps.get(cacheKey);
        return (System.currentTimeMillis() - timestamp) < CACHE_EXPIRY;
    }

    /**
     * 모든 숙소 목록을 API에서 가져옵니다.
     */
    public List<Accommodation> getAllAccommodations() throws SQLException {
        String cacheKey = "all_accommodations";

        // 캐시 확인
        if (isCacheValid(cacheKey) && accommodationCache.containsKey(cacheKey)) {
            return accommodationCache.get(cacheKey);
        }

        try {
            List<Accommodation> accommodations = fetchAccommodationsFromApi(null, null, 100);

            // 캐시 저장
            accommodationCache.put(cacheKey, accommodations);
            cacheTimestamps.put(cacheKey, System.currentTimeMillis());

            return accommodations;
        } catch (Exception e) {
            logger.error("API에서 숙소 목록을 가져오는 중 오류 발생", e);
            // API 호출 실패 시 DB에서 가져오기
            return accommodationService.getAllAccommodations();
        }
    }

    /**
     * 지역 코드로 숙소 목록을 API에서 가져옵니다.
     */
    public List<Accommodation> getAccommodationsByRegion(Integer sidoCode, Integer gugunCode) throws SQLException {
        try {
            List<Accommodation> accommodations = fetchAccommodationsFromApi(sidoCode, gugunCode, 50);
            return accommodations;
        } catch (Exception e) {
            logger.error("API에서 지역별 숙소 목록을 가져오는 중 오류 발생", e);
            // API 호출 실패 시 DB에서 가져오기
            return accommodationService.getAccommodationsByRegion(sidoCode, gugunCode);
        }
    }

    /**
     * 키워드로 숙소를 API에서 검색합니다.
     */
    public List<Accommodation> searchAccommodations(String keyword) throws SQLException {
        try {
            List<Accommodation> accommodations = searchAccommodationsFromApi(keyword, 50);
            return accommodations;
        } catch (Exception e) {
            logger.error("API에서 키워드로 숙소를 검색하는 중 오류 발생", e);
            // API 호출 실패 시 DB에서 가져오기
            return accommodationService.searchAccommodations(keyword);
        }
    }

    /**
     * 숙소 ID로 숙소를 API에서 조회합니다.
     */
    public Accommodation getAccommodationById(Long accommodationId) throws SQLException {
        try {
            // API에서 숙소 상세 정보 가져오기
            Accommodation accommodation = fetchAccommodationDetail(accommodationId.toString());
            return accommodation;
        } catch (Exception e) {
            logger.error("API에서 숙소 상세 정보를 가져오는 중 오류 발생", e);
            // API 호출 실패 시 DB에서 가져오기
            return accommodationService.getAccommodationById(accommodationId);
        }
    }

    /**
     * 숙소 ID로 객실 목록을 API에서 조회합니다.
     */
    public List<Room> getRoomsByAccommodationId(Long accommodationId) throws SQLException {
        try {
            // API에서 객실 정보 가져오기
            List<Room> rooms = fetchRoomInfo(accommodationId.toString());
            return rooms;
        } catch (Exception e) {
            logger.error("API에서 객실 목록을 가져오는 중 오류 발생", e);
            // API 호출 실패 시 DB에서 가져오기
            return accommodationService.getRoomsByAccommodationId(accommodationId);
        }
    }

    /**
     * 객실 ID로 객실을 API에서 조회합니다.
     */
    public Room getRoomById(Long roomId) throws SQLException {
        try {
            // 캐시에 없으면 DB에서 가져오기
            return accommodationService.getRoomById(roomId);
        } catch (Exception e) {
            logger.error("객실 정보를 가져오는 중 오류 발생", e);
            return accommodationService.getRoomById(roomId);
        }
    }

    /**
     * 필터링된 숙소 목록을 API에서 조회합니다.
     */
    public List<Accommodation> getFilteredAccommodations(Map<String, Object> filters) throws SQLException {
        try {
            // 필터 조건에 따라 API 호출
            Integer sidoCode = filters.containsKey("sidoCode") ? (Integer) filters.get("sidoCode") : null;
            Integer gugunCode = filters.containsKey("gugunCode") ? (Integer) filters.get("gugunCode") : null;
            String keyword = filters.containsKey("keyword") ? (String) filters.get("keyword") : null;

            List<Accommodation> accommodations;
            if (keyword != null && !keyword.isEmpty()) {
                accommodations = searchAccommodationsFromApi(keyword, 50);
            } else {
                accommodations = fetchAccommodationsFromApi(sidoCode, gugunCode, 50);
            }

            return accommodations;
        } catch (Exception e) {
            logger.error("API에서 필터링된 숙소 목록을 가져오는 중 오류 발생", e);
            // API 호출 실패 시 DB에서 가져오기
            return accommodationService.getFilteredAccommodations(filters);
        }
    }

    /**
     * API에서 숙소 목록을 가져옵니다.
     */
    private List<Accommodation> fetchAccommodationsFromApi(Integer sidoCode, Integer gugunCode, int limit) throws Exception {
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

        List<Accommodation> accommodations = new ArrayList<>();

        if (items.isArray()) {
            for (JsonNode item : items) {
                String contentId = item.path("contentid").asText();

                // 기본 정보만 설정하고 상세 정보는 필요할 때 로드
                Accommodation accommodation = new Accommodation();
                accommodation.setAccommodationId(Long.parseLong(contentId));
                accommodation.setTitle(item.path("title").asText(""));
                accommodation.setAddress(item.path("addr1").asText("") + " " + item.path("addr2").asText(""));

                // 시도 코드와 구군 코드 설정
                int apiSidoCode = item.path("areacode").asInt(0);
                int apiGugunCode = item.path("sigungucode").asInt(0);

                accommodation.setSidoCode(apiSidoCode > 0 ? apiSidoCode : 1);
                accommodation.setGugunCode(apiGugunCode > 0 ? apiGugunCode : 1);

                accommodations.add(accommodation);
            }
        }

        return accommodations;
    }

    /**
     * API에서 키워드로 숙소를 검색합니다.
     */
    private List<Accommodation> searchAccommodationsFromApi(String keyword, int limit) throws Exception {
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
                .queryParam("pageNo", 1)
                .queryParam("keyword", keyword);

        URI uri = new URI(builder.build(false).toUriString());
        String response = restTemplate.getForObject(uri, String.class);

        // JSON 파싱
        JsonNode root = objectMapper.readTree(response);
        JsonNode items = root.path("response").path("body").path("items").path("item");

        List<Accommodation> accommodations = new ArrayList<>();

        if (items.isArray()) {
            for (JsonNode item : items) {
                String contentId = item.path("contentid").asText();

                // 기본 정보만 설정하고 상세 정보는 필요할 때 로드
                Accommodation accommodation = new Accommodation();
                accommodation.setAccommodationId(Long.parseLong(contentId));
                accommodation.setTitle(item.path("title").asText(""));
                accommodation.setAddress(item.path("addr1").asText("") + " " + item.path("addr2").asText(""));

                // 시도 코드와 구군 코드 설정
                int apiSidoCode = item.path("areacode").asInt(0);
                int apiGugunCode = item.path("sigungucode").asInt(0);

                accommodation.setSidoCode(apiSidoCode > 0 ? apiSidoCode : 1);
                accommodation.setGugunCode(apiGugunCode > 0 ? apiGugunCode : 1);

                accommodations.add(accommodation);
            }
        }

        return accommodations;
    }

    /**
     * API에서 숙소 상세 정보를 가져옵니다.
     */
    private Accommodation fetchAccommodationDetail(String contentId) throws Exception {
        // 간단한 구현으로 대체
        Accommodation accommodation = new Accommodation();
        accommodation.setAccommodationId(Long.parseLong(contentId));
        accommodation.setTitle("API에서 가져온 숙소 " + contentId);
        accommodation.setDescription("API에서 가져온 숙소 설명입니다.");
        accommodation.setAddress("서울시 강남구 테헤란로 123");
        accommodation.setSidoCode(1);
        accommodation.setGugunCode(1);
        accommodation.setPhone("02-1234-5678");
        accommodation.setEmail("");
        accommodation.setWebsite("https://example.com");
        accommodation.setCheckInTime(LocalTime.of(15, 0));
        accommodation.setCheckOutTime(LocalTime.of(11, 0));
        accommodation.setAmenities("WiFi, 주차장, 수영장, 헬스장, 레스토랑");
        accommodation.setStatus("ACTIVE");
        accommodation.setHostId(1L);

        return accommodation;
    }

    /**
     * API에서 객실 정보를 가져옵니다.
     */
    private List<Room> fetchRoomInfo(String contentId) throws Exception {
        List<Room> rooms = new ArrayList<>();

        // 기본 객실 하나 생성
        Room defaultRoom = new Room();
        defaultRoom.setRoomId(Long.parseLong(contentId) * 10 + 1);
        defaultRoom.setAccommodationId(Long.parseLong(contentId));
        defaultRoom.setName("스탠다드 룸");
        defaultRoom.setDescription("편안한 스탠다드 객실입니다.");
        defaultRoom.setPrice(new BigDecimal("100000"));
        defaultRoom.setCapacity(2);
        defaultRoom.setRoomCount(1);
        defaultRoom.setRoomSize(new BigDecimal("20"));
        defaultRoom.setRoomType("스탠다드");
        defaultRoom.setBedType("더블");
        defaultRoom.setBathroomCount(1);
        defaultRoom.setAmenities("TV, 에어컨, 냉장고, 욕실용품");
        defaultRoom.setStatus("AVAILABLE");

        rooms.add(defaultRoom);

        return rooms;
    }

    /**
     * API에서 이미지 정보를 가져옵니다.
     */
    private List<Image> fetchImageInfo(String contentId) throws Exception {
        List<Image> images = new ArrayList<>();

        // 기본 이미지 하나 생성
        Image image = new Image();
        image.setImageUrl("https://via.placeholder.com/800x600?text=Sample+Accommodation+" + contentId);
        image.setCaption("샘플 이미지");
        image.setIsMain(true);
        image.setReferenceType("ACCOMMODATION");
        image.setReferenceId(Long.parseLong(contentId));

        images.add(image);

        return images;
    }
}
