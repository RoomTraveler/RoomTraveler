package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.Favorite;
import com.ssafy.trip.accommodation.service.FavoriteService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/favorites")
public class FavoriteRestController {

    private final FavoriteService favoriteService;

    /**
     * [GET] 내 즐겨찾기 목록 조회
     */
    @GetMapping
    public ResponseEntity<?> getFavorites(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));

        try {
            List<Favorite> favorites = favoriteService.getFavoritesByUserId(userId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "favorites", favorites
            ));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * [POST] 즐겨찾기에 추가
     * body: { "accommodationId": 123 }
     */
    @PostMapping
    public ResponseEntity<?> addFavorite(
            @RequestBody Map<String, Long> payload,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));

        Long accommodationId = payload.get("accommodationId");
        try {
            Long favoriteId = favoriteService.addFavorite(userId, accommodationId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "favoriteId", favoriteId,
                    "message", "즐겨찾기에 추가되었습니다."
            ));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "즐겨찾기 추가 중 오류: " + e.getMessage()));
        }
    }

    /**
     * [DELETE] 즐겨찾기에서 삭제 (찜 해제)
     * DELETE /api/favorites/{favoriteId}
     */
    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<?> removeFavorite(
            @PathVariable Long favoriteId,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "로그인이 필요합니다."));

        try {
            boolean removed = favoriteService.removeFavorite(favoriteId, userId);
            if (removed) {
                return ResponseEntity.ok(Map.of("success", true, "message", "즐겨찾기에서 삭제되었습니다."));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "즐겨찾기 삭제에 실패했습니다."));
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "즐겨찾기 삭제 중 오류: " + e.getMessage()));
        }
    }

    /**
     * [GET] 특정 숙소 즐겨찾기 여부 확인 (내가 해당 숙소를 찜했는지)
     * /api/favorites/check?accommodationId=xxx
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkFavorite(
            @RequestParam Long accommodationId,
            HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");
        Map<String, Object> response = new HashMap<>();
        if (userId == null) {
            response.put("isFavorite", false);
            response.put("success", false);
            return ResponseEntity.ok(response);
        }

        try {
            boolean isFavorite = favoriteService.isFavorite(userId, accommodationId);
            response.put("isFavorite", isFavorite);
            response.put("success", true);

            if (isFavorite) {
                Favorite favorite = favoriteService.getFavoritesByUserId(userId).stream()
                        .filter(f -> f.getAccommodationId().equals(accommodationId))
                        .findFirst()
                        .orElse(null);
                if (favorite != null)
                    response.put("favoriteId", favorite.getFavoriteId());
            }
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            response.put("isFavorite", false);
            response.put("success", false);
            response.put("message", "확인 중 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
