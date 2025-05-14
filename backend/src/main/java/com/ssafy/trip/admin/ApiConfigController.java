package com.ssafy.trip.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.trip.tourapi.TourApiProperties;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * API 설정 관리 컨트롤러
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/api-config")
public class ApiConfigController {

    private final TourApiProperties tourApiProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * API 설정 페이지를 표시합니다.
     */
    @GetMapping("")
    public String viewApiConfig(HttpSession session, Model model) {
        // 관리자 권한 확인
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            model.addAttribute("error", "관리자 권한이 필요합니다.");
            return "error";
        }

        model.addAttribute("apiConfig", tourApiProperties);
        return "admin/api-config";
    }

    /**
     * API 설정을 업데이트합니다.
     */
    @PostMapping("/update")
    public String updateApiConfig(
            @RequestParam String baseUrl,
            @RequestParam String serviceKey,
            @RequestParam String mobileOs,
            @RequestParam String mobileApp,
            @RequestParam int defaultPageNo,
            @RequestParam int defaultNumOfRows,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        // 관리자 권한 확인
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "관리자 권한이 필요합니다.");
            return "redirect:/admin";
        }

        // API 설정 업데이트
        tourApiProperties.setBaseUrl(baseUrl);
        tourApiProperties.setServiceKey(serviceKey);
        tourApiProperties.setMobileOs(mobileOs);
        tourApiProperties.setMobileApp(mobileApp);
        tourApiProperties.setDefaultPageNo(defaultPageNo);
        tourApiProperties.setDefaultNumOfRows(defaultNumOfRows);

        redirectAttributes.addFlashAttribute("message", "API 설정이 업데이트되었습니다.");
        return "redirect:/admin/api-config";
    }

    /**
     * API 테스트를 수행합니다.
     */
    @PostMapping("/test")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> testApi(
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {

        // 관리자 권한 확인
        String role = (String) session.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "관리자 권한이 필요합니다.");
            return ResponseEntity.badRequest().body(response);
        }

        try {
            String endpoint = (String) requestBody.get("endpoint");
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) requestBody.get("params");

            // API 요청 URL 구성
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromHttpUrl(tourApiProperties.getBaseUrl() + "/" + endpoint)
                    .queryParam("serviceKey", tourApiProperties.getServiceKey())
                    .queryParam("MobileOS", tourApiProperties.getMobileOs())
                    .queryParam("MobileApp", tourApiProperties.getMobileApp())
                    .queryParam("_type", "json")
                    .queryParam("numOfRows", tourApiProperties.getDefaultNumOfRows())
                    .queryParam("pageNo", tourApiProperties.getDefaultPageNo());

            // 추가 파라미터 설정
            if (params != null) {
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    builder.queryParam(entry.getKey(), entry.getValue());
                }
            }

            URI uri = new URI(builder.build(false).toUriString());
            String apiResponse = restTemplate.getForObject(uri, String.class);

            // JSON 파싱
            JsonNode root = objectMapper.readTree(apiResponse);

            // 응답 구성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", root);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
