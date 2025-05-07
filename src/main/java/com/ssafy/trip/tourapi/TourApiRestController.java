package com.ssafy.trip.tourapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 숙박 구역 코드(시도/구군)를 가져오기 위한 REST 컨트롤러
 * 투어 API에서. JSON 및 XML 오류 응답을 균일하게 처리합니다.
 */
@RestController
@RequestMapping("/accommodation/api")
public class TourApiRestController {
    // SLF4J 로거를 사용하여 디버깅 및 에러 로깅
    private static final Logger logger = LoggerFactory.getLogger(TourApiRestController.class);

    private final TourApiProperties props;   // API 설정(키, URL, 파라미터 등)
    private final RestTemplate rest;         // 외부 API 호출 도구
    private final ObjectMapper jsonMapper;   // JSON 파싱 도구
    private final XmlMapper xmlMapper;       // XML 파싱 도구

    /**
     * 생성자: 필요한 의존성을 주입하고 JSON 매퍼를 구성합니다.
     */
    public TourApiRestController(TourApiProperties props) {
        this.props = props;
        this.rest = new RestTemplate();
        this.jsonMapper = new ObjectMapper()
                // API 응답에 추가 필드가 있더라도 무시하도록 설정
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.xmlMapper = new XmlMapper();
    }

    /**
     * 시도 목록 조회 엔드포인트
     * @return List<AreaDto> 형태의 JSON 배열 또는 에러 정보
     */
    @GetMapping(value = "/sidos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSidos() {
        // fetchAreas 메서드에 sido 파라미터 없이 호출
        return fetchAreas(null);
    }

    /**
     * 구군 목록 조회 엔드포인트
     * @param sido 선택된 시도 코드
     * @return List<AreaDto> 형태의 JSON 배열 또는 에러 정보
     */
    @GetMapping(value = "/guguns", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getGuguns(@RequestParam("sido") String sido) {
        return fetchAreas(sido);
    }

    /**
     * 관광지 정보 조회 엔드포인트
     * @param areaCode 지역 코드
     * @param sigunguCode 시군구 코드
     * @param contentTypeId 관광타입 ID (12:관광지, 14:문화시설, 15:축제공연행사, 25:여행코스, 28:레포츠, 32:숙박, 38:쇼핑, 39:음식점)
     * @param pageNo 페이지 번호
     * @param numOfRows 한 페이지 결과 수
     * @return 관광지 정보 목록 또는 에러 정보
     */
    @GetMapping(value = "/attractions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAttractions(
            @RequestParam(value = "areaCode", required = false) String areaCode,
            @RequestParam(value = "sigunguCode", required = false) String sigunguCode,
            @RequestParam(value = "contentTypeId", required = false) String contentTypeId,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "numOfRows", required = false) Integer numOfRows) {

        try {
            // 서비스 키 유효성 검증
            String serviceKey = props.getServiceKey();
            if (serviceKey == null || serviceKey.isBlank()) {
                logger.error("TourAPI serviceKey is not configured");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Service key is not configured"));
            }

            // 요청 URL 빌더 초기화
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(props.getBaseUrl() + "/areaBasedList1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", props.getMobileOs())
                    .queryParam("MobileApp", props.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("pageNo", pageNo != null ? pageNo : props.getDefaultPageNo())
                    .queryParam("numOfRows", numOfRows != null ? numOfRows : props.getDefaultNumOfRows());

            // 선택적 파라미터 추가
            if (areaCode != null && !areaCode.isBlank()) {
                builder.queryParam("areaCode", areaCode);
            }
            if (sigunguCode != null && !sigunguCode.isBlank()) {
                builder.queryParam("sigunguCode", sigunguCode);
            }
            if (contentTypeId != null && !contentTypeId.isBlank()) {
                builder.queryParam("contentTypeId", contentTypeId);
            }

            // URI 클래스로 인코딩된 URL 생성
            String rawUrl = builder.build(false).toUriString();
            URI uri = new URI(rawUrl);
            logger.debug("Fetching TourAPI attractions URI: {}", uri);

            // 외부 API 호출
            String raw = rest.getForObject(uri, String.class);
            if (raw == null) {
                logger.error("No response from TourAPI");
                return ResponseEntity.status(502)
                        .body(Map.of("error", "No response from API"));
            }
            raw = raw.trim();

            // XML 에러 응답 감지: 문자열이 '<'로 시작하면 XML 파싱
            if (raw.startsWith("<")) {
                ErrorResponse err = xmlMapper.readValue(raw, ErrorResponse.class);
                logger.warn("TourAPI XML Error: {} [{}]",
                        err.getCmmMsgHeader().getErrMsg(),
                        err.getCmmMsgHeader().getReturnAuthMsg());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", err.getCmmMsgHeader().getErrMsg(),
                        "authMsg", err.getCmmMsgHeader().getReturnAuthMsg(),
                        "code", err.getCmmMsgHeader().getReturnReasonCode()
                ));
            }

            // JSON 파싱: response 또는 한글 키 '응답'
            JsonNode root = jsonMapper.readTree(raw);
            JsonNode respNode = root.path("response");
            if (respNode.isMissingNode()) respNode = root.path("응답");
            if (respNode.isMissingNode()) {
                logger.error("Invalid JSON response; missing 'response' node: {}", raw);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid API response"));
            }

            // 결과 코드 검증
            JsonNode header = respNode.path("header");
            String resultCode = header.path("resultCode").asText(
                    header.path("결과 코드").asText());
            if (!"0000".equals(resultCode)) {
                String msg = header.path("resultMsg").asText(
                        header.path("결과 메시지").asText("API error"));
                logger.warn("TourAPI returned error code {}: {}", resultCode, msg);
                return ResponseEntity.badRequest().body(Map.of(
                        "error", msg,
                        "code", resultCode
                ));
            }

            // 데이터 추출: JSON 경로 'body.items.item' 또는 한글 경로
            JsonNode items = respNode.path("body").path("items").path("item");
            if (items.isMissingNode()) items = respNode.path("몸").path("항목").path("항목");

            // 페이징 정보 추출
            JsonNode body = respNode.path("body");
            int totalCount = body.path("totalCount").asInt(0);
            int pageNoResult = body.path("pageNo").asInt(1);
            int numOfRowsResult = body.path("numOfRows").asInt(10);

            // 결과가 없는 경우 빈 배열 반환
            if (items.isMissingNode() || items.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "items", new ArrayList<>(),
                    "totalCount", totalCount,
                    "pageNo", pageNoResult,
                    "numOfRows", numOfRowsResult
                ));
            }

            // 단일 항목인 경우 배열로 변환
            if (!items.isArray()) {
                JsonNode singleItem = items;
                items = jsonMapper.createArrayNode().add(singleItem);
            }

            // 결과 반환
            List<Object> attractions = jsonMapper.convertValue(
                    items,
                    jsonMapper.getTypeFactory().constructCollectionType(List.class, Object.class)
            );

            return ResponseEntity.ok(Map.of(
                "items", attractions,
                "totalCount", totalCount,
                "pageNo", pageNoResult,
                "numOfRows", numOfRowsResult
            ));

        } catch (Exception e) {
            // 예외 발생 시 로그 출력 및 500 반환
            logger.error("Exception while fetching attractions", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 관광지 상세 정보 조회 엔드포인트
     * @param contentId 콘텐츠 ID
     * @param contentTypeId 관광타입 ID
     * @return 관광지 상세 정보 또는 에러 정보
     */
    @GetMapping(value = "/attraction/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAttractionDetail(
            @RequestParam("contentId") String contentId,
            @RequestParam(value = "contentTypeId", required = false) String contentTypeId) {

        try {
            // 서비스 키 유효성 검증
            String serviceKey = props.getServiceKey();
            if (serviceKey == null || serviceKey.isBlank()) {
                logger.error("TourAPI serviceKey is not configured");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Service key is not configured"));
            }

            // 기본 정보 요청 URL 빌더 초기화
            UriComponentsBuilder commonBuilder = UriComponentsBuilder
                    .fromHttpUrl(props.getBaseUrl() + "/detailCommon1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", props.getMobileOs())
                    .queryParam("MobileApp", props.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("contentId", contentId)
                    .queryParam("defaultYN", "Y")
                    .queryParam("firstImageYN", "Y")
                    .queryParam("areacodeYN", "Y")
                    .queryParam("catcodeYN", "Y")
                    .queryParam("addrinfoYN", "Y")
                    .queryParam("mapinfoYN", "Y")
                    .queryParam("overviewYN", "Y");

            // 기본 정보 요청
            String rawUrl = commonBuilder.build(false).toUriString();
            URI uri = new URI(rawUrl);
            logger.debug("Fetching TourAPI attraction detail URI: {}", uri);

            // 외부 API 호출
            String raw = rest.getForObject(uri, String.class);
            if (raw == null) {
                logger.error("No response from TourAPI");
                return ResponseEntity.status(502)
                        .body(Map.of("error", "No response from API"));
            }
            raw = raw.trim();

            // XML 에러 응답 감지: 문자열이 '<'로 시작하면 XML 파싱
            if (raw.startsWith("<")) {
                ErrorResponse err = xmlMapper.readValue(raw, ErrorResponse.class);
                logger.warn("TourAPI XML Error: {} [{}]",
                        err.getCmmMsgHeader().getErrMsg(),
                        err.getCmmMsgHeader().getReturnAuthMsg());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", err.getCmmMsgHeader().getErrMsg(),
                        "authMsg", err.getCmmMsgHeader().getReturnAuthMsg(),
                        "code", err.getCmmMsgHeader().getReturnReasonCode()
                ));
            }

            // JSON 파싱: response 또는 한글 키 '응답'
            JsonNode root = jsonMapper.readTree(raw);
            JsonNode respNode = root.path("response");
            if (respNode.isMissingNode()) respNode = root.path("응답");
            if (respNode.isMissingNode()) {
                logger.error("Invalid JSON response; missing 'response' node: {}", raw);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid API response"));
            }

            // 결과 코드 검증
            JsonNode header = respNode.path("header");
            String resultCode = header.path("resultCode").asText(
                    header.path("결과 코드").asText());
            if (!"0000".equals(resultCode)) {
                String msg = header.path("resultMsg").asText(
                        header.path("결과 메시지").asText("API error"));
                logger.warn("TourAPI returned error code {}: {}", resultCode, msg);
                return ResponseEntity.badRequest().body(Map.of(
                        "error", msg,
                        "code", resultCode
                ));
            }

            // 데이터 추출: JSON 경로 'body.items.item' 또는 한글 경로
            JsonNode items = respNode.path("body").path("items").path("item");
            if (items.isMissingNode()) items = respNode.path("몸").path("항목").path("항목");

            // 결과가 없는 경우
            if (items.isMissingNode() || items.isEmpty()) {
                return ResponseEntity.ok(Map.of("item", Map.of()));
            }

            // 단일 항목 또는 첫 번째 항목 추출
            JsonNode item = items.isArray() ? items.get(0) : items;

            // 기본 정보를 Map으로 변환
            Map<String, Object> detailInfo = jsonMapper.convertValue(
                    item,
                    jsonMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
            );

            // contentTypeId가 제공된 경우 추가 정보 요청
            if (contentTypeId != null && !contentTypeId.isBlank()) {
                try {
                    // 추가 정보 요청 URL 빌더 초기화
                    UriComponentsBuilder introBuilder = UriComponentsBuilder
                            .fromHttpUrl(props.getBaseUrl() + "/detailIntro1")
                            .queryParam("serviceKey", serviceKey)
                            .queryParam("MobileOS", props.getMobileOs())
                            .queryParam("MobileApp", props.getMobileApp())
                            .queryParam("_type", "json")
                            .queryParam("contentId", contentId)
                            .queryParam("contentTypeId", contentTypeId);

                    // 추가 정보 요청
                    URI introUri = new URI(introBuilder.build(false).toUriString());
                    logger.debug("Fetching TourAPI attraction intro URI: {}", introUri);

                    String introRaw = rest.getForObject(introUri, String.class);
                    if (introRaw != null && !introRaw.trim().startsWith("<")) {
                        JsonNode introRoot = jsonMapper.readTree(introRaw.trim());
                        JsonNode introRespNode = introRoot.path("response");
                        if (!introRespNode.isMissingNode()) {
                            JsonNode introItems = introRespNode.path("body").path("items").path("item");
                            if (!introItems.isMissingNode() && !introItems.isEmpty()) {
                                JsonNode introItem = introItems.isArray() ? introItems.get(0) : introItems;

                                // 추가 정보를 Map으로 변환하여 기본 정보에 추가
                                Map<String, Object> introInfo = jsonMapper.convertValue(
                                        introItem,
                                        jsonMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                                );

                                // 추가 정보를 별도 키로 저장
                                detailInfo.put("introInfo", introInfo);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Failed to fetch intro information: {}", e.getMessage());
                    // 추가 정보 요청 실패 시 기본 정보만 반환
                }
            }

            return ResponseEntity.ok(Map.of("item", detailInfo));

        } catch (Exception e) {
            // 예외 발생 시 로그 출력 및 500 반환
            logger.error("Exception while fetching attraction detail", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 키워드로 관광지 검색 엔드포인트
     * @param keyword 검색 키워드
     * @param areaCode 지역 코드 (선택적)
     * @param sigunguCode 시군구 코드 (선택적)
     * @param contentTypeId 관광타입 ID (선택적)
     * @param pageNo 페이지 번호 (선택적)
     * @param numOfRows 한 페이지 결과 수 (선택적)
     * @return 검색 결과 목록 또는 에러 정보
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> searchByKeyword(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "areaCode", required = false) String areaCode,
            @RequestParam(value = "sigunguCode", required = false) String sigunguCode,
            @RequestParam(value = "contentTypeId", required = false) String contentTypeId,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "numOfRows", required = false) Integer numOfRows) {

        try {
            // 서비스 키 유효성 검증
            String serviceKey = props.getServiceKey();
            if (serviceKey == null || serviceKey.isBlank()) {
                logger.error("TourAPI serviceKey is not configured");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Service key is not configured"));
            }

            // 키워드 유효성 검증
            if (keyword == null || keyword.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Keyword is required"));
            }

            // 요청 URL 빌더 초기화
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(props.getBaseUrl() + "/searchKeyword1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", props.getMobileOs())
                    .queryParam("MobileApp", props.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("keyword", keyword)
                    .queryParam("pageNo", pageNo != null ? pageNo : props.getDefaultPageNo())
                    .queryParam("numOfRows", numOfRows != null ? numOfRows : props.getDefaultNumOfRows());

            // 선택적 파라미터 추가
            if (areaCode != null && !areaCode.isBlank()) {
                builder.queryParam("areaCode", areaCode);
            }
            if (sigunguCode != null && !sigunguCode.isBlank()) {
                builder.queryParam("sigunguCode", sigunguCode);
            }
            if (contentTypeId != null && !contentTypeId.isBlank()) {
                builder.queryParam("contentTypeId", contentTypeId);
            }

            // URI 클래스로 인코딩된 URL 생성
            String rawUrl = builder.build(false).toUriString();
            URI uri = new URI(rawUrl);
            logger.debug("Fetching TourAPI search URI: {}", uri);

            // 외부 API 호출
            String raw = rest.getForObject(uri, String.class);
            if (raw == null) {
                logger.error("No response from TourAPI");
                return ResponseEntity.status(502)
                        .body(Map.of("error", "No response from API"));
            }
            raw = raw.trim();

            // XML 에러 응답 감지: 문자열이 '<'로 시작하면 XML 파싱
            if (raw.startsWith("<")) {
                ErrorResponse err = xmlMapper.readValue(raw, ErrorResponse.class);
                logger.warn("TourAPI XML Error: {} [{}]",
                        err.getCmmMsgHeader().getErrMsg(),
                        err.getCmmMsgHeader().getReturnAuthMsg());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", err.getCmmMsgHeader().getErrMsg(),
                        "authMsg", err.getCmmMsgHeader().getReturnAuthMsg(),
                        "code", err.getCmmMsgHeader().getReturnReasonCode()
                ));
            }

            // JSON 파싱: response 또는 한글 키 '응답'
            JsonNode root = jsonMapper.readTree(raw);
            JsonNode respNode = root.path("response");
            if (respNode.isMissingNode()) respNode = root.path("응답");
            if (respNode.isMissingNode()) {
                logger.error("Invalid JSON response; missing 'response' node: {}", raw);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid API response"));
            }

            // 결과 코드 검증
            JsonNode header = respNode.path("header");
            String resultCode = header.path("resultCode").asText(
                    header.path("결과 코드").asText());
            if (!"0000".equals(resultCode)) {
                String msg = header.path("resultMsg").asText(
                        header.path("결과 메시지").asText("API error"));
                logger.warn("TourAPI returned error code {}: {}", resultCode, msg);
                return ResponseEntity.badRequest().body(Map.of(
                        "error", msg,
                        "code", resultCode
                ));
            }

            // 데이터 추출: JSON 경로 'body.items.item' 또는 한글 경로
            JsonNode items = respNode.path("body").path("items").path("item");
            if (items.isMissingNode()) items = respNode.path("몸").path("항목").path("항목");

            // 페이징 정보 추출
            JsonNode body = respNode.path("body");
            int totalCount = body.path("totalCount").asInt(0);
            int pageNoResult = body.path("pageNo").asInt(1);
            int numOfRowsResult = body.path("numOfRows").asInt(10);

            // 결과가 없는 경우 빈 배열 반환
            if (items.isMissingNode() || items.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "items", new ArrayList<>(),
                    "totalCount", totalCount,
                    "pageNo", pageNoResult,
                    "numOfRows", numOfRowsResult,
                    "keyword", keyword
                ));
            }

            // 단일 항목인 경우 배열로 변환
            if (!items.isArray()) {
                JsonNode singleItem = items;
                items = jsonMapper.createArrayNode().add(singleItem);
            }

            // 결과 반환
            List<Object> searchResults = jsonMapper.convertValue(
                    items,
                    jsonMapper.getTypeFactory().constructCollectionType(List.class, Object.class)
            );

            return ResponseEntity.ok(Map.of(
                "items", searchResults,
                "totalCount", totalCount,
                "pageNo", pageNoResult,
                "numOfRows", numOfRowsResult,
                "keyword", keyword
            ));

        } catch (Exception e) {
            // 예외 발생 시 로그 출력 및 500 반환
            logger.error("Exception while searching attractions", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 공통 로직: Tour API 호출, 응답 파싱 및 에러 처리
     * @param sido 시도 코드(null일 경우 시도 목록 조회)
     * @return 지역 코드 리스트 또는 에러 응답
     */
    private ResponseEntity<?> fetchAreas(String sido) {
        try {
            // 서비스 키 유효성 검증
            String serviceKey = props.getServiceKey();
            if (serviceKey == null || serviceKey.isBlank()) {
                logger.error("TourAPI serviceKey is not configured");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Service key is not configured"));
            }

            // 요청 URL 빌더 초기화
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(props.getBaseUrl() + "/areaCode1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", props.getMobileOs())
                    .queryParam("MobileApp", props.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("numOfRows", props.getDefaultNumOfRows())
                    .queryParam("pageNo", props.getDefaultPageNo());

            // sido 파라미터가 있으면 areaCode 추가
            if (sido != null) {
                builder.queryParam("areaCode", sido);
            }

            // URI 클래스로 인코딩된 URL 생성
            String rawUrl = builder.build(false).toUriString();
            URI uri = new URI(rawUrl);
            logger.debug("Fetching TourAPI URI via java.net.URI: {}", uri);

            // 외부 API 호출
            String raw = rest.getForObject(uri, String.class);
            if (raw == null) {
                logger.error("No response from TourAPI");
                return ResponseEntity.status(502)
                        .body(Map.of("error", "No response from API"));
            }
            raw = raw.trim();

            // XML 에러 응답 감지: 문자열이 '<'로 시작하면 XML 파싱
            if (raw.startsWith("<")) {
                ErrorResponse err = xmlMapper.readValue(raw, ErrorResponse.class);
                logger.warn("TourAPI XML Error: {} [{}]",
                        err.getCmmMsgHeader().getErrMsg(),
                        err.getCmmMsgHeader().getReturnAuthMsg());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", err.getCmmMsgHeader().getErrMsg(),
                        "authMsg", err.getCmmMsgHeader().getReturnAuthMsg(),
                        "code", err.getCmmMsgHeader().getReturnReasonCode()
                ));
            }

            // JSON 파싱: response 또는 한글 키 '응답'
            JsonNode root = jsonMapper.readTree(raw);
            JsonNode respNode = root.path("response");
            if (respNode.isMissingNode()) respNode = root.path("응답");
            if (respNode.isMissingNode()) {
                logger.error("Invalid JSON response; missing 'response' node: {}", raw);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid API response"));
            }

            // 결과 코드 검증
            JsonNode header = respNode.path("header");
            String resultCode = header.path("resultCode").asText(
                    header.path("결과 코드").asText());
            if (!"0000".equals(resultCode)) {
                String msg = header.path("resultMsg").asText(
                        header.path("결과 메시지").asText("API error"));
                logger.warn("TourAPI returned error code {}: {}", resultCode, msg);
                return ResponseEntity.badRequest().body(Map.of(
                        "error", msg,
                        "code", resultCode
                ));
            }

            // 데이터 추출: JSON 경로 'body.items.item' 또는 한글 경로
            JsonNode items = respNode.path("body").path("items").path("item");
            if (items.isMissingNode()) items = respNode.path("몸").path("항목").path("항목");

            // AreaDto 리스트로 변환
            List<AreaDto> list = jsonMapper.convertValue(
                    items,
                    jsonMapper.getTypeFactory().constructCollectionType(List.class, AreaDto.class)
            );
            return ResponseEntity.ok(list);

        } catch (Exception e) {
            // 예외 발생 시 로그 출력 및 500 반환
            logger.error("Exception while fetching areas", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 숙박 정보 조회 엔드포인트
     * @param areaCode 지역 코드
     * @param sigunguCode 시군구 코드
     * @param pageNo 페이지 번호
     * @param numOfRows 한 페이지 결과 수
     * @return 숙박 정보 목록 또는 에러 정보
     */
    @GetMapping(value = "/accommodations", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAccommodations(
            @RequestParam(value = "areaCode", required = false) String areaCode,
            @RequestParam(value = "sigunguCode", required = false) String sigunguCode,
            @RequestParam(value = "pageNo", required = false) Integer pageNo,
            @RequestParam(value = "numOfRows", required = false) Integer numOfRows) {

        try {
            // 서비스 키 유효성 검증
            String serviceKey = props.getServiceKey();
            if (serviceKey == null || serviceKey.isBlank()) {
                logger.error("TourAPI serviceKey is not configured");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Service key is not configured"));
            }

            // 요청 URL 빌더 초기화
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(props.getBaseUrl() + "/searchStay1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", props.getMobileOs())
                    .queryParam("MobileApp", props.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("listYN", "Y")
                    .queryParam("arrange", "A")
                    .queryParam("pageNo", pageNo != null ? pageNo : props.getDefaultPageNo())
                    .queryParam("numOfRows", numOfRows != null ? numOfRows : props.getDefaultNumOfRows());

            // 선택적 파라미터 추가
            if (areaCode != null && !areaCode.isBlank()) {
                builder.queryParam("areaCode", areaCode);
            }
            if (sigunguCode != null && !sigunguCode.isBlank()) {
                builder.queryParam("sigunguCode", sigunguCode);
            }

            // URI 클래스로 인코딩된 URL 생성
            String rawUrl = builder.build(false).toUriString();
            URI uri = new URI(rawUrl);
            logger.debug("Fetching TourAPI accommodations URI: {}", uri);

            // 외부 API 호출
            String raw = rest.getForObject(uri, String.class);
            if (raw == null) {
                logger.error("No response from TourAPI");
                return ResponseEntity.status(502)
                        .body(Map.of("error", "No response from API"));
            }
            raw = raw.trim();

            // XML 에러 응답 감지: 문자열이 '<'로 시작하면 XML 파싱
            if (raw.startsWith("<")) {
                ErrorResponse err = xmlMapper.readValue(raw, ErrorResponse.class);
                logger.warn("TourAPI XML Error: {} [{}]",
                        err.getCmmMsgHeader().getErrMsg(),
                        err.getCmmMsgHeader().getReturnAuthMsg());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", err.getCmmMsgHeader().getErrMsg(),
                        "authMsg", err.getCmmMsgHeader().getReturnAuthMsg(),
                        "code", err.getCmmMsgHeader().getReturnReasonCode()
                ));
            }

            // JSON 파싱: response 또는 한글 키 '응답'
            JsonNode root = jsonMapper.readTree(raw);
            JsonNode respNode = root.path("response");
            if (respNode.isMissingNode()) respNode = root.path("응답");
            if (respNode.isMissingNode()) {
                logger.error("Invalid JSON response; missing 'response' node: {}", raw);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid API response"));
            }

            // 결과 코드 검증
            JsonNode header = respNode.path("header");
            String resultCode = header.path("resultCode").asText(
                    header.path("결과 코드").asText());
            if (!"0000".equals(resultCode)) {
                String msg = header.path("resultMsg").asText(
                        header.path("결과 메시지").asText("API error"));
                logger.warn("TourAPI returned error code {}: {}", resultCode, msg);
                return ResponseEntity.badRequest().body(Map.of(
                        "error", msg,
                        "code", resultCode
                ));
            }

            // 데이터 추출: JSON 경로 'body.items.item' 또는 한글 경로
            JsonNode items = respNode.path("body").path("items").path("item");
            if (items.isMissingNode()) items = respNode.path("몸").path("항목").path("항목");

            // 페이징 정보 추출
            JsonNode body = respNode.path("body");
            int totalCount = body.path("totalCount").asInt(0);
            int pageNoResult = body.path("pageNo").asInt(1);
            int numOfRowsResult = body.path("numOfRows").asInt(10);

            // 결과가 없는 경우 빈 배열 반환
            if (items.isMissingNode() || items.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "items", new ArrayList<>(),
                    "totalCount", 0,
                    "pageNo", pageNoResult,
                    "numOfRows", numOfRowsResult
                ));
            }

            // 단일 항목인 경우 배열로 변환
            if (!items.isArray()) {
                JsonNode singleItem = items;
                items = jsonMapper.createArrayNode().add(singleItem);
            }

            // 결과를 List로 변환
            List<Map<String, Object>> accommodations = jsonMapper.convertValue(
                    items,
                    jsonMapper.getTypeFactory().constructCollectionType(List.class, 
                        jsonMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class))
            );

            // 객실이 있고 썸네일 이미지가 있는 숙소만 필터링
            List<Map<String, Object>> filteredAccommodations = new ArrayList<>();

            for (Map<String, Object> accommodation : accommodations) {
                String contentId = accommodation.get("contentid").toString();
                Object firstImage = accommodation.get("firstimage");
                // 객실이 있고 썸네일 이미지가 있는 경우만 추가
                if (hasRooms(contentId) && firstImage != null && !firstImage.toString().isEmpty()) {
                    filteredAccommodations.add(accommodation);
                }
            }

            // 필터링된 결과의 실제 개수를 totalCount로 사용
            int filteredCount = filteredAccommodations.size();

            // 시군구 코드가 비어있는 경우("전체" 선택) 원래 totalCount 값을 유지
            // 이렇게 하면 "전체" 선택 시 모든 결과가 표시됨
            if (sigunguCode == null || sigunguCode.isBlank()) {
                // totalCount는 그대로 유지 (API에서 반환한 전체 개수)
                logger.debug("Using original totalCount {} for 'All' sigungu selection", totalCount);
            } else {
                // 특정 시군구가 선택된 경우 필터링된 개수를 사용
                totalCount = filteredCount;
                logger.debug("Using filtered totalCount {} for specific sigungu {}", filteredCount, sigunguCode);
            }

            return ResponseEntity.ok(Map.of(
                "items", filteredAccommodations,
                "totalCount", totalCount,
                "pageNo", pageNoResult,
                "numOfRows", numOfRowsResult
            ));

        } catch (Exception e) {
            // 예외 발생 시 로그 출력 및 500 반환
            logger.error("Exception while fetching accommodations", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 숙박 시설에 객실이 있는지 확인하는 메소드
     * @param contentId 콘텐츠 ID
     * @return 객실이 있으면 true, 없으면 false
     */
    private boolean hasRooms(String contentId) {
        try {
            // 서비스 키 유효성 검증
            String serviceKey = props.getServiceKey();
            if (serviceKey == null || serviceKey.isBlank()) {
                logger.error("TourAPI serviceKey is not configured");
                return false;
            }

            // 객실 정보 요청 URL 빌더 초기화
            UriComponentsBuilder roomBuilder = UriComponentsBuilder
                    .fromHttpUrl(props.getBaseUrl() + "/detailInfo1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", props.getMobileOs())
                    .queryParam("MobileApp", props.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("contentId", contentId)
                    .queryParam("contentTypeId", "32"); // 숙박 타입 ID

            // 객실 정보 요청
            URI roomUri = new URI(roomBuilder.build(false).toUriString());
            logger.debug("Checking rooms for accommodation {}: {}", contentId, roomUri);

            String roomRaw = rest.getForObject(roomUri, String.class);
            if (roomRaw == null || roomRaw.trim().startsWith("<")) {
                return false;
            }

            JsonNode roomRoot = jsonMapper.readTree(roomRaw.trim());
            JsonNode roomRespNode = roomRoot.path("response");
            if (roomRespNode.isMissingNode()) {
                return false;
            }

            JsonNode roomItems = roomRespNode.path("body").path("items").path("item");
            return !roomItems.isMissingNode() && !roomItems.isEmpty();

        } catch (Exception e) {
            logger.error("Exception while checking rooms for accommodation {}", contentId, e);
            return false;
        }
    }

    /**
     * 객실 정보 조회 엔드포인트
     * @param contentId 콘텐츠 ID
     * @return 객실 정보 목록 또는 에러 정보
     */
    @GetMapping(value = "/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getRoomInfo(
            @RequestParam("contentId") String contentId) {

        try {
            // 서비스 키 유효성 검증
            String serviceKey = props.getServiceKey();
            if (serviceKey == null || serviceKey.isBlank()) {
                logger.error("TourAPI serviceKey is not configured");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Service key is not configured"));
            }

            // 기본 정보 요청 URL 빌더 초기화
            UriComponentsBuilder commonBuilder = UriComponentsBuilder
                    .fromHttpUrl(props.getBaseUrl() + "/detailCommon1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", props.getMobileOs())
                    .queryParam("MobileApp", props.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("contentId", contentId)
                    .queryParam("defaultYN", "Y")
                    .queryParam("firstImageYN", "Y")
                    .queryParam("areacodeYN", "Y")
                    .queryParam("catcodeYN", "Y")
                    .queryParam("addrinfoYN", "Y")
                    .queryParam("mapinfoYN", "Y")
                    .queryParam("overviewYN", "Y");

            // 기본 정보 요청
            String rawUrl = commonBuilder.build(false).toUriString();
            URI uri = new URI(rawUrl);
            logger.debug("Fetching TourAPI accommodation detail URI: {}", uri);

            // 외부 API 호출
            String raw = rest.getForObject(uri, String.class);
            if (raw == null) {
                logger.error("No response from TourAPI");
                return ResponseEntity.status(502)
                        .body(Map.of("error", "No response from API"));
            }
            raw = raw.trim();

            // XML 에러 응답 감지: 문자열이 '<'로 시작하면 XML 파싱
            if (raw.startsWith("<")) {
                ErrorResponse err = xmlMapper.readValue(raw, ErrorResponse.class);
                logger.warn("TourAPI XML Error: {} [{}]",
                        err.getCmmMsgHeader().getErrMsg(),
                        err.getCmmMsgHeader().getReturnAuthMsg());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", err.getCmmMsgHeader().getErrMsg(),
                        "authMsg", err.getCmmMsgHeader().getReturnAuthMsg(),
                        "code", err.getCmmMsgHeader().getReturnReasonCode()
                ));
            }

            // JSON 파싱: response 또는 한글 키 '응답'
            JsonNode root = jsonMapper.readTree(raw);
            JsonNode respNode = root.path("response");
            if (respNode.isMissingNode()) respNode = root.path("응답");
            if (respNode.isMissingNode()) {
                logger.error("Invalid JSON response; missing 'response' node: {}", raw);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid API response"));
            }

            // 결과 코드 검증
            JsonNode header = respNode.path("header");
            String resultCode = header.path("resultCode").asText(
                    header.path("결과 코드").asText());
            if (!"0000".equals(resultCode)) {
                String msg = header.path("resultMsg").asText(
                        header.path("결과 메시지").asText("API error"));
                logger.warn("TourAPI returned error code {}: {}", resultCode, msg);
                return ResponseEntity.badRequest().body(Map.of(
                        "error", msg,
                        "code", resultCode
                ));
            }

            // 데이터 추출: JSON 경로 'body.items.item' 또는 한글 경로
            JsonNode items = respNode.path("body").path("items").path("item");
            if (items.isMissingNode()) items = respNode.path("몸").path("항목").path("항목");

            // 결과가 없는 경우
            if (items.isMissingNode() || items.isEmpty()) {
                return ResponseEntity.ok(Map.of("item", Map.of()));
            }

            // 단일 항목 또는 첫 번째 항목 추출
            JsonNode item = items.isArray() ? items.get(0) : items;

            // 기본 정보를 Map으로 변환
            Map<String, Object> detailInfo = jsonMapper.convertValue(
                    item,
                    jsonMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
            );

            // 소개 정보 요청 URL 빌더 초기화
            UriComponentsBuilder introBuilder = UriComponentsBuilder
                    .fromHttpUrl(props.getBaseUrl() + "/detailIntro1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", props.getMobileOs())
                    .queryParam("MobileApp", props.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("contentId", contentId)
                    .queryParam("contentTypeId", "32"); // 숙박 타입 ID

            // 소개 정보 요청
            URI introUri = new URI(introBuilder.build(false).toUriString());
            logger.debug("Fetching TourAPI accommodation intro URI: {}", introUri);

            String introRaw = rest.getForObject(introUri, String.class);
            Map<String, Object> introInfo = new HashMap<>();
            if (introRaw != null && !introRaw.trim().startsWith("<")) {
                JsonNode introRoot = jsonMapper.readTree(introRaw.trim());
                JsonNode introRespNode = introRoot.path("response");
                if (!introRespNode.isMissingNode()) {
                    JsonNode introItems = introRespNode.path("body").path("items").path("item");
                    if (!introItems.isMissingNode() && !introItems.isEmpty()) {
                        JsonNode introItem = introItems.isArray() ? introItems.get(0) : introItems;

                        // 소개 정보를 Map으로 변환
                        introInfo = jsonMapper.convertValue(
                                introItem,
                                jsonMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                        );
                    }
                }
            }

            // 객실 정보 요청 URL 빌더 초기화
            UriComponentsBuilder roomBuilder = UriComponentsBuilder
                    .fromHttpUrl(props.getBaseUrl() + "/detailInfo1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", props.getMobileOs())
                    .queryParam("MobileApp", props.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("contentId", contentId)
                    .queryParam("contentTypeId", "32"); // 숙박 타입 ID

            // 객실 정보 요청
            URI roomUri = new URI(roomBuilder.build(false).toUriString());
            logger.debug("Fetching TourAPI accommodation room info URI: {}", roomUri);

            String roomRaw = rest.getForObject(roomUri, String.class);
            List<Object> roomInfo = new ArrayList<>();
            if (roomRaw != null && !roomRaw.trim().startsWith("<")) {
                JsonNode roomRoot = jsonMapper.readTree(roomRaw.trim());
                JsonNode roomRespNode = roomRoot.path("response");
                if (!roomRespNode.isMissingNode()) {
                    JsonNode roomItems = roomRespNode.path("body").path("items").path("item");
                    if (!roomItems.isMissingNode() && !roomItems.isEmpty()) {
                        // 단일 항목인 경우 배열로 변환
                        if (!roomItems.isArray()) {
                            JsonNode singleItem = roomItems;
                            roomItems = jsonMapper.createArrayNode().add(singleItem);
                        }

                        // 객실 정보를 List<Map>으로 변환
                        roomInfo = jsonMapper.convertValue(
                                roomItems,
                                jsonMapper.getTypeFactory().constructCollectionType(List.class, Object.class)
                        );
                    }
                }
            }

            // 이미지 정보 요청 URL 빌더 초기화
            UriComponentsBuilder imageBuilder = UriComponentsBuilder
                    .fromHttpUrl(props.getBaseUrl() + "/detailImage1")
                    .queryParam("serviceKey", serviceKey)
                    .queryParam("MobileOS", props.getMobileOs())
                    .queryParam("MobileApp", props.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("contentId", contentId)
                    .queryParam("imageYN", "Y")
                    .queryParam("subImageYN", "Y");

            // 이미지 정보 요청
            URI imageUri = new URI(imageBuilder.build(false).toUriString());
            logger.debug("Fetching TourAPI accommodation image info URI: {}", imageUri);

            String imageRaw = rest.getForObject(imageUri, String.class);
            List<Object> imageInfo = new ArrayList<>();
            if (imageRaw != null && !imageRaw.trim().startsWith("<")) {
                JsonNode imageRoot = jsonMapper.readTree(imageRaw.trim());
                JsonNode imageRespNode = imageRoot.path("response");
                if (!imageRespNode.isMissingNode()) {
                    JsonNode imageItems = imageRespNode.path("body").path("items").path("item");
                    if (!imageItems.isMissingNode() && !imageItems.isEmpty()) {
                        // 단일 항목인 경우 배열로 변환
                        if (!imageItems.isArray()) {
                            JsonNode singleItem = imageItems;
                            imageItems = jsonMapper.createArrayNode().add(singleItem);
                        }

                        // 이미지 정보를 List<Map>으로 변환
                        imageInfo = jsonMapper.convertValue(
                                imageItems,
                                jsonMapper.getTypeFactory().constructCollectionType(List.class, Object.class)
                        );
                    }
                }
            }

            // 결과 반환
            Map<String, Object> result = new HashMap<>();
            result.put("basicInfo", detailInfo);
            result.put("introInfo", introInfo);
            result.put("roomInfo", roomInfo);
            result.put("imageInfo", imageInfo);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            // 예외 발생 시 로그 출력 및 500 반환
            logger.error("Exception while fetching room info", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 공공데이터 API에서 반환하는 XML 에러 구조 매핑 클래스
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorResponse {
        private CmmMsgHeader cmmMsgHeader;
        public CmmMsgHeader getCmmMsgHeader() { return cmmMsgHeader; }
        public void setCmmMsgHeader(CmmMsgHeader header) { this.cmmMsgHeader = header; }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class CmmMsgHeader {
            private String errMsg;
            private String returnAuthMsg;
            private int returnReasonCode;

            public String getErrMsg() { return errMsg; }
            public void setErrMsg(String errMsg) { this.errMsg = errMsg; }
            public String getReturnAuthMsg() { return returnAuthMsg; }
            public void setReturnAuthMsg(String returnAuthMsg) { this.returnAuthMsg = returnAuthMsg; }
            public int getReturnReasonCode() { return returnReasonCode; }
            public void setReturnReasonCode(int returnReasonCode) { this.returnReasonCode = returnReasonCode; }
        }
    }
}
