package com.ssafy.trip.favorite.controller;

import com.ssafy.trip.favorite.model.Favorite;
import com.ssafy.trip.favorite.model.FavoriteResponseDto;
import com.ssafy.trip.favorite.service.FavoriteService;
import com.ssafy.trip.user.User;
import com.ssafy.trip.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "찜(즐겨찾기) 컨트롤러", description = "찜 관련 API (엔티티 기반 버전)")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);
    private final FavoriteService favoriteService;
    private final UserService userService;

    public FavoriteController(FavoriteService favoriteService, UserService userService) {
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new BadCredentialsException("인증되지 않은 사용자입니다. 로그인이 필요합니다.");
        }
        Object principal = authentication.getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            logger.warn("알 수 없는 Principal 타입입니다: {}", principal.getClass().getName());
            throw new BadCredentialsException("인증 정보 추출에 실패했습니다. (알 수 없는 Principal 타입)");
        }
        if (email == null) {
            throw new BadCredentialsException("인증된 사용자 이메일을 찾을 수 없습니다.");
        }
        Optional<User> userOptional = userService.getUserByEmail(email);
        if (userOptional.isEmpty()) {
            logger.warn("DB에서 이메일로 사용자를 찾을 수 없습니다: {}", email);
            throw new BadCredentialsException("인증된 사용자 정보를 DB에서 찾을 수 없습니다.");
        }
        User user = userOptional.get();
        if (user.getUserId() == null) {
            logger.error("조회된 User 객체에 userId가 없습니다. Email: {}", email);
            throw new BadCredentialsException("사용자 ID 정보를 가져올 수 없습니다.");
        }
        return user.getUserId();
    }

    @PostMapping
    @Operation(summary = "찜 추가", description = "사용자가 특정 숙소를 찜 목록에 추가합니다. 이미 찜한 경우 복구하거나, 이미 활성 상태면 기존 ID 반환.")
    public ResponseEntity<Map<String, Object>> addFavorite(
            @RequestBody Map<String, Long> payload,
            Authentication authentication) throws SQLException {
        Long userId = getCurrentUserId(authentication);
        Long accommodationId = payload.get("accommodationId");
        if (accommodationId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "숙소 ID(accommodationId)는 필수입니다."));
        }
        Long favoriteId = favoriteService.addOrRecoverFavorite(userId, accommodationId);
        Map<String, Object> result = new HashMap<>();
        result.put("favoriteId", favoriteId);
        result.put("message", "찜이 추가되거나 복구되었습니다.");
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @DeleteMapping("/{favoriteId}")
    @Operation(summary = "찜 삭제", description = "찜 ID와 본인 확인을 통해 찜을 삭제합니다.")
    public ResponseEntity<Map<String, Object>> removeFavorite(
            @PathVariable Long favoriteId,
            Authentication authentication) throws SQLException {
        Long userId = getCurrentUserId(authentication);
        boolean success = favoriteService.removeFavorite(favoriteId, userId);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "찜이 삭제되었습니다.", "favoriteId", favoriteId));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "찜이 존재하지 않거나 삭제 권한이 없습니다."));
        }
    }

    @GetMapping
    @Operation(summary = "내 찜 목록 조회", description = "현재 로그인한 사용자의 모든 찜 목록을 조회합니다.")
    public ResponseEntity<List<FavoriteResponseDto>> getMyFavorites(Authentication authentication) throws SQLException {
        Long userId = getCurrentUserId(authentication);
        List<FavoriteResponseDto> favorites = favoriteService.getFavoritesByUserId(userId);
        return ResponseEntity.ok(favorites);
    }

    @GetMapping("/status")
    @Operation(summary = "특정 숙소 찜 상태 확인", description = "현재 사용자가 특정 숙소를 찜했는지 여부를 확인합니다.")
    public ResponseEntity<Map<String, Object>> getFavoriteStatus(
            @RequestParam Long accommodationId,
            Authentication authentication) throws SQLException {
        Long userId = getCurrentUserId(authentication);
        boolean isFavorite = favoriteService.isFavorite(userId, accommodationId);
        return ResponseEntity.ok(Map.of("isFavorite", isFavorite));
    }

    @GetMapping("/{favoriteId}")
    @Operation(summary = "찜 단건 조회", description = "찜 ID로 해당 찜 정보를 조회합니다.")
    public ResponseEntity<?> getFavoriteById(
            @PathVariable Long favoriteId,
            Authentication authentication) throws SQLException {
        getCurrentUserId(authentication);
        Favorite favorite = favoriteService.getFavoriteById(favoriteId);
        if (favorite == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "찜을 찾을 수 없습니다."));
        }
        return ResponseEntity.ok(favorite);
    }

    @DeleteMapping
    @Operation(summary = "찜 삭제 (사용자 ID, 숙소 ID 기준)", description = "사용자가 특정 숙소를 찜 목록에서 삭제합니다.")
    public ResponseEntity<Map<String, String>> removeFavoriteByAccommodationId(
            @Parameter(description = "삭제할 숙소 ID", required = true) @RequestParam Long accommodationId,
            Authentication authentication) throws SQLException {
        Long userId = getCurrentUserId(authentication);
        boolean removed = favoriteService.removeFavoriteByUserIdAndAccommodationId(userId, accommodationId);
        if (removed) {
            return ResponseEntity.ok(Map.of("message", "찜이 성공적으로 삭제되었습니다."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "삭제할 찜 정보가 없거나 이미 삭제되었습니다."));
        }
    }
}
