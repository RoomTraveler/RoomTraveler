package com.ssafy.trip.stats.controller;

import com.ssafy.trip.common.BaseException;
import com.ssafy.trip.stats.model.PopularityStats;
import com.ssafy.trip.stats.service.PopularityStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ssafy.trip.stats.model.ManualAggregationRequest;
import com.ssafy.trip.stats.model.ManualDailyRankingRequest;
import org.springframework.validation.annotation.Validated;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats/popularity")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Popularity Stats", description = "인기 통계 및 랭킹 관련 API")
public class ApiPopularityStatsController {

    private final PopularityStatsService popularityStatsService;

    @Operation(summary = "인기 통계/랭킹 조회", description = "다양한 조건에 따라 통계 및 랭킹 데이터를 조회합니다.\n" +
            "periodValue 형식: DAILY -> YYYY-MM-DD, WEEKLY -> YYYY-Www (예: 2023-W52), MONTHLY -> YYYY-MM (예: 2023-12)")
    @GetMapping
    public ResponseEntity<?> getPopularityStats(
            @Parameter(description = "통계 유형 (REGION, ACCOMMODATION, TYPE, TYPE_REGION)") @RequestParam(required = false) String statsType,
            @Parameter(description = "기간 유형 (DAILY, WEEKLY, MONTHLY)") @RequestParam(required = true) String periodType,
            @Parameter(description = "기간 값 (형식은 periodType에 따름)") @RequestParam(required = true) String periodValue,
            @Parameter(description = "지역 유형 (sido, gugun) - statsType=REGION일 때 사용") @RequestParam(required = false) String regionType,
            @Parameter(description = "지역 코드 - statsType=REGION일 때 사용") @RequestParam(required = false) Integer regionCode,
            @Parameter(description = "숙소 유형 (HOTEL 등) - statsType=TYPE일 때 사용") @RequestParam(required = false) String accommodationType,
            @Parameter(description = "숙소 ID - statsType=ACCOMMODATION일 때 사용") @RequestParam(required = false) Long accommodationId,
            @Parameter(description = "정렬 기준 (예: popularity_score DESC)") @RequestParam(required = false) String orderBy,
            @Parameter(description = "가져올 개수") @RequestParam(required = false, defaultValue = "10") int limit,
            @Parameter(description = "건너뛸 개수 (페이징)") @RequestParam(required = false, defaultValue = "0") int offset
    ) {
        Map<String, Object> criteria = new HashMap<>();
        if (statsType != null && !statsType.isBlank()) criteria.put("statsType", statsType);
        if (periodType != null && !periodType.isBlank()) criteria.put("periodType", periodType.toUpperCase());
        if (periodValue != null && !periodValue.isBlank()) criteria.put("periodValue", periodValue);
        if (regionType != null && !regionType.isBlank()) criteria.put("regionType", regionType);
        if (regionCode != null) criteria.put("regionCode", regionCode);
        if (accommodationType != null && !accommodationType.isBlank()) criteria.put("accommodationType", accommodationType);
        if (accommodationId != null) criteria.put("accommodationId", accommodationId);
        if (orderBy != null && !orderBy.isBlank()) criteria.put("orderBy", orderBy);
        
        criteria.put("limit", limit > 0 ? limit : 10);
        criteria.put("offset", offset >= 0 ? offset : 0);

        // periodValue 유효성 검사 (간단한 예시)
        if ("DAILY".equalsIgnoreCase(periodType)) {
            try {
                LocalDate.parse(periodValue, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                return ResponseEntity.badRequest().body("DAILY periodValue는 YYYY-MM-DD 형식이어야 합니다.");
            }
        } else if ("WEEKLY".equalsIgnoreCase(periodType)) {
            // YYYY-Www 형식 검사 (정규식 또는 더 엄격한 파싱 필요)
            if (!periodValue.matches("\\d{4}-W\\d{2}")) {
                 return ResponseEntity.badRequest().body("WEEKLY periodValue는 YYYY-Www 형식이어야 합니다.");
            }
        } else if ("MONTHLY".equalsIgnoreCase(periodType)) {
             if (!periodValue.matches("\\d{4}-\\d{2}")) {
                 return ResponseEntity.badRequest().body("MONTHLY periodValue는 YYYY-MM 형식이어야 합니다.");
            }
        }


        try {
            List<PopularityStats> stats = popularityStatsService.getPopularityStats(criteria);
            if (stats == null || stats.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(stats);
        } catch (SQLException e) {
            log.error("인기 통계 조회 중 DB 오류 발생: {}", criteria, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("통계 조회 중 오류가 발생했습니다.");
        } catch (BaseException e) {
            log.warn("인기 통계 조회 중 비즈니스 로직 오류: {}, 코드: {}", e.getMessage(), e.getErrorCode().getCode());
            return ResponseEntity.status(e.getErrorCode().getStatus()).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("인기 통계 조회 중 잘못된 파라미터: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("인기 통계 조회 중 알 수 없는 오류 발생: {}", criteria, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "특정 숙소의 기간별 통계 조회", description = "숙소 ID와 기간 타입, 기간 값을 기준으로 특정 숙소의 통계 정보를 조회합니다.")
    @GetMapping("/accommodation/{accommodationId}")
    public ResponseEntity<?> getAccommodationStats(
            @Parameter(description = "숙소 ID", required = true) @PathVariable Long accommodationId,
            @Parameter(description = "기간 유형 (DAILY, WEEKLY, MONTHLY)", required = true) @RequestParam String periodType,
            @Parameter(description = "기간 값 (형식은 periodType에 따름)", required = true) @RequestParam String periodValue
    ) {
        if (accommodationId == null) {
            return ResponseEntity.badRequest().body("숙소 ID는 필수입니다.");
        }
        if (periodType == null || periodType.isBlank()) {
            return ResponseEntity.badRequest().body("기간 유형은 필수입니다.");
        }
        if (periodValue == null || periodValue.isBlank()) {
            return ResponseEntity.badRequest().body("기간 값은 필수입니다.");
        }
        
        // periodValue 유효성 검사 (위와 유사하게 추가 가능)

        try {
            PopularityStats stats = popularityStatsService.getAccommodationStats(accommodationId, periodType.toUpperCase(), periodValue);
            if (stats == null) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "해당 조건의 숙소 통계 데이터가 없습니다.");
                response.put("accommodationId", String.valueOf(accommodationId));
                response.put("periodType", periodType);
                response.put("periodValue", periodValue);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            return ResponseEntity.ok(stats);
        } catch (SQLException e) {
            log.error("숙소 통계 조회 중 DB 오류 발생: accommodationId={}, periodType={}, periodValue={}", accommodationId, periodType, periodValue, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("숙소 통계 조회 중 오류가 발생했습니다.");
        } catch (BaseException e) {
            log.warn("숙소 통계 조회 중 비즈니스 로직 오류: {}, 코드: {}", e.getMessage(), e.getErrorCode().getCode());
            return ResponseEntity.status(e.getErrorCode().getStatus()).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("숙소 통계 조회 중 잘못된 파라미터: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("숙소 통계 조회 중 알 수 없는 오류 발생: accommodationId={}, periodType={}, periodValue={}", accommodationId, periodType, periodValue, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("알 수 없는 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "[관리자] 수동 통계 집계 실행", description = "지정된 기간과 유형(주간/월간)에 대해 통계 집계 및 랭킹 업데이트를 수동으로 실행합니다.")
    @PostMapping("/admin/trigger-aggregation")
    // @PreAuthorize("hasRole('ADMIN')") // TODO: 관리자 권한 필요 시 추가
    public ResponseEntity<?> triggerManualAggregation(@Validated @RequestBody ManualAggregationRequest request) {
        try {
            log.info("수동 통계 집계 요청: {}", request);
            String periodValue = popularityStatsService.triggerManualAggregation(
                    request.getStartDate(), request.getEndDate(), request.getPeriodType(), request.getTopN()
            );
            Map<String, String> response = new HashMap<>();
            response.put("message", "수동 통계 집계 및 랭킹 업데이트가 성공적으로 실행되었습니다.");
            response.put("periodType", request.getPeriodType().toUpperCase());
            response.put("generatedPeriodValue", periodValue);
            response.put("processedDateRange", request.getStartDate() + " ~ " + request.getEndDate());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("수동 통계 집계 요청 오류 (잘못된 파라미터): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SQLException e) {
            log.error("수동 통계 집계 중 DB 오류 발생: {}", request, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수동 통계 집계 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("수동 통계 집계 중 알 수 없는 오류 발생: {}", request, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수동 통계 집계 중 알 수 없는 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "[관리자] 수동 일일 랭킹 업데이트 실행", description = "지정된 날짜를 기준으로 일일 통계의 랭킹을 수동으로 업데이트합니다.")
    @PostMapping("/admin/trigger-daily-ranking")
    // @PreAuthorize("hasRole('ADMIN')") // TODO: 관리자 권한 필요 시 추가
    public ResponseEntity<?> triggerManualDailyRanking(@Validated @RequestBody ManualDailyRankingRequest request) {
        try {
            log.info("수동 일일 랭킹 업데이트 요청: {}", request);
            popularityStatsService.triggerManualDailyRanking(request.getDate(), request.getTopN());
            Map<String, String> response = new HashMap<>();
            response.put("message", "수동 일일 랭킹 업데이트가 성공적으로 실행되었습니다.");
            response.put("rankedDate", request.getDate().toString());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("수동 일일 랭킹 업데이트 요청 오류 (잘못된 파라미터): {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SQLException e) {
            log.error("수동 일일 랭킹 업데이트 중 DB 오류 발생: {}", request, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수동 일일 랭킹 업데이트 중 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("수동 일일 랭킹 업데이트 중 알 수 없는 오류 발생: {}", request, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("수동 일일 랭킹 업데이트 중 알 수 없는 오류가 발생했습니다.");
        }
    }
} 