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
