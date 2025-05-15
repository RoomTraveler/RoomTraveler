package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.service.AccommodationServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API를 통해 숙소 정보를 제공하는 REST 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accommodations") // 리소스 명 복수형
public class ApiAccommodationController {

    private final AccommodationServiceImpl apiAccommodationService;

    /**
     * 전체 숙소 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> listAccommodations() {
        try {
            List<Accommodation> accommodations = apiAccommodationService.getAllAccommodations();
            return ResponseEntity.ok(accommodations);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 지역별 숙소 목록 조회
     */
    @GetMapping("/region")
    public ResponseEntity<?> getAccommodationsByRegion(
            @RequestParam(required = false) Integer sidoCode,
            @RequestParam(required = false) Integer gugunCode) {
        try {
            List<Accommodation> accommodations = apiAccommodationService.getAccommodationsByRegion(sidoCode, gugunCode);
            return ResponseEntity.ok(accommodations);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 키워드로 숙소 검색
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAccommodations(@RequestParam String keyword) {
        try {
            List<Accommodation> accommodations = apiAccommodationService.searchAccommodations(keyword);
            return ResponseEntity.ok(accommodations);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 숙소 상세 정보 조회
     */
    @GetMapping("/{accommodationId}")
    public ResponseEntity<?> getAccommodationDetail(@PathVariable Long accommodationId) {
        try {
            Accommodation accommodation = apiAccommodationService.getAccommodationById(accommodationId);
            if (accommodation == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "숙소를 찾을 수 없습니다."));
            }
            List<Room> rooms = apiAccommodationService.getRoomsByAccommodationId(accommodationId);
            Map<String, Object> response = new HashMap<>();
            response.put("accommodation", accommodation);
            response.put("rooms", rooms);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 객실 상세 정보 조회
     */
    @GetMapping("/room/{roomId}")
    public ResponseEntity<?> getRoomDetail(@PathVariable Long roomId) {
        try {
            Room room = apiAccommodationService.getRoomById(roomId);
            if (room == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "객실을 찾을 수 없습니다."));
            }
            Accommodation accommodation = apiAccommodationService.getAccommodationById(room.getAccommodationId());
            Map<String, Object> response = new HashMap<>();
            response.put("room", room);
            response.put("accommodation", accommodation);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 조건별 숙소 필터링
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredAccommodations(
            @RequestParam(required = false) Integer sidoCode,
            @RequestParam(required = false) Integer gugunCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String accommodationType,
            @RequestParam(required = false) Integer minPrice,
            @RequestParam(required = false) Integer maxPrice,
            @RequestParam(required = false) String sortBy
    ) {
        try {
            Map<String, Object> filters = new HashMap<>();
            if (sidoCode != null) filters.put("sidoCode", sidoCode);
            if (gugunCode != null) filters.put("gugunCode", gugunCode);
            if (keyword != null && !keyword.isEmpty()) filters.put("keyword", keyword);
            if (accommodationType != null && !accommodationType.isEmpty()) filters.put("accommodationType", accommodationType);
            if (minPrice != null) filters.put("minPrice", minPrice);
            if (maxPrice != null) filters.put("maxPrice", maxPrice);
            if (sortBy != null && !sortBy.isEmpty()) filters.put("sortBy", sortBy);

            List<Accommodation> accommodations = apiAccommodationService.getFilteredAccommodations(filters);
            return ResponseEntity.ok(accommodations);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
