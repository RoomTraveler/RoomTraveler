package com.ssafy.trip.region.controller;

import com.ssafy.trip.region.model.Sido;
import com.ssafy.trip.region.service.RegionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * 지역 정보 REST API 컨트롤러
 * 
 * 시도 및 구군 정보를 제공하는 REST API 엔드포인트를 정의합니다.
 * 
 * @author AI Assistant
 */
@RestController
@RequestMapping("/api/region")
@RequiredArgsConstructor
public class RegionApiController {

    // SLF4J 로거를 사용하여 디버깅 및 에러 로깅
    private static final Logger logger = LoggerFactory.getLogger(RegionApiController.class);

    // 생성자 주입을 통한 의존성 주입
    private final RegionService regionService;

    /**
     * 시도 목록 조회 엔드포인트
     * 
     * @return List<Sido> 형태의 JSON 배열 또는 에러 정보
     */
    @GetMapping(value = "/sidos", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSidos() {
        try {
            // 데이터베이스에서 시도 목록 조회
            List<Sido> sidos = regionService.getSidos();
            return ResponseEntity.ok(sidos);
        } catch (Exception e) {
            logger.error("시도 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * 구군 목록 조회 엔드포인트
     * 
     * @param sido 선택된 시도 코드
     * @return List<Gugun> 형태의 JSON 배열 또는 에러 정보
     */
    @GetMapping(value = "/guguns", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getGuguns(@RequestParam("sido") String sido) {
        try {
            // 데이터베이스에서 구군 목록 조회
            int sidoCode = Integer.parseInt(sido);
            return ResponseEntity.ok(regionService.getGuguns(sidoCode));
        } catch (NumberFormatException e) {
            logger.error("잘못된 시도 코드 형식: {}", sido, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "잘못된 시도 코드 형식입니다."));
        } catch (Exception e) {
            logger.error("구군 목록 조회 중 오류 발생", e);
            return ResponseEntity.status(500)
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
