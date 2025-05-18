package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.Accommodation;
import com.ssafy.trip.accommodation.model.Room;
import com.ssafy.trip.accommodation.service.AccommodationService;
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

    private final AccommodationService apiAccommodationService;

    /**
     * 전체 숙소 목록 조회 - 페이징 추가 고려 (필요시)
     * 현재는 페이징 없이 전체 목록 반환
     */
    @GetMapping
    public ResponseEntity<?> listAccommodations() {
        try {
            List<Accommodation> accommodations = apiAccommodationService.getAllAccommodations();
            return ResponseEntity.ok(accommodations);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "숙소 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 지역별 숙소 목록 조회 - 페이징 추가 고려 (필요시)
     * 현재는 페이징 없이 전체 목록 반환
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
                    .body(Map.of("error", "지역별 숙소 목록 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 키워드로 숙소 검색 - 페이징 추가 고려 (필요시)
     * 현재는 페이징 없이 전체 목록 반환
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchAccommodations(@RequestParam String keyword) {
        try {
            List<Accommodation> accommodations = apiAccommodationService.searchAccommodations(keyword);
            return ResponseEntity.ok(accommodations);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "숙소 검색 중 오류 발생: " + e.getMessage()));
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
                        .body(Map.of("error", "숙소를 찾을 수 없습니다. ID: " + accommodationId));
            }
            List<Room> rooms = apiAccommodationService.getRoomsByAccommodationId(accommodationId);
            Map<String, Object> response = new HashMap<>();
            response.put("accommodation", accommodation);
            response.put("rooms", rooms);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "숙소 상세 정보 조회 중 오류 발생: " + e.getMessage()));
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
                        .body(Map.of("error", "객실을 찾을 수 없습니다. ID: " + roomId));
            }
            Accommodation accommodation = apiAccommodationService.getAccommodationById(room.getAccommodationId());
            Map<String, Object> response = new HashMap<>();
            response.put("room", room);
            response.put("accommodation", accommodation);
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "객실 상세 정보 조회 중 오류 발생: " + e.getMessage()));
        }
    }

    /**
     * 조건별 숙소 필터링 (페이징 적용)
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredAccommodations(
            @RequestParam(required = false) Integer sidoCode,
            @RequestParam(required = false) Integer gugunCode,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String accommodationType,
            @RequestParam(required = false) Integer guestCount,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Map<String, Object> filters = new HashMap<>();
            if (sidoCode != null) filters.put("sidoCode", sidoCode);
            if (gugunCode != null) filters.put("gugunCode", gugunCode);
            if (keyword != null && !keyword.trim().isEmpty()) filters.put("keyword", keyword.trim());
            if (accommodationType != null && !accommodationType.trim().isEmpty() && !accommodationType.equalsIgnoreCase("ALL")) {
                filters.put("accommodationType", accommodationType.trim());
            }
            if (guestCount != null && guestCount > 0) filters.put("guestCount", guestCount);
            if (sortBy != null && !sortBy.trim().isEmpty()) filters.put("sortBy", sortBy.trim());
            
            // 페이징 파라미터 추가
            filters.put("offset", (page - 1) * size);
            filters.put("limit", size);
            filters.put("page", page);
            filters.put("size", size);

            Map<String, Object> pagedResult = apiAccommodationService.getFilteredAccommodations(filters);

            return ResponseEntity.ok(pagedResult);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "필터링된 숙소 목록 조회 중 오류 발생: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "예상치 못한 오류 발생: " + e.getMessage()));
        }
    }
}
