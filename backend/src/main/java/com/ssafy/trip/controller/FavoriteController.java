package com.ssafy.trip.controller;

import com.ssafy.trip.dto.FavoriteDto;
import com.ssafy.trip.dto.response.FavoriteResponseDto;
import com.ssafy.trip.exception.AlreadyExistsException; // 사용됨
import com.ssafy.trip.exception.ForbiddenException; // 사용됨
import com.ssafy.trip.exception.ResourceNotFoundException; // 사용됨
import com.ssafy.trip.service.FavoriteService;
import com.ssafy.trip.user.User; 
import com.ssafy.trip.user.UserService; 
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.userdetails.UserDetails; 
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/favorites")
@Tag(name = "찜(즐겨찾기) 컨트롤러", description = "찜 관련 API")
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
    @Operation(summary = "찜 추가", description = "사용자가 특정 숙소를 찜 목록에 추가합니다. 이미 찜한 경우 복구하거나, 이미 활성 상태면 오류를 반환합니다.")
    @ApiResponse(responseCode = "201", description = "찜 추가 또는 복구 성공", content = @Content(schema = @Schema(implementation = FavoriteDto.class)))
    @ApiResponse(responseCode = "400", description = "잘못된 요청 (숙소 ID 누락 등)")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "409", description = "이미 활성화된 찜")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<?> addFavorite(
            @Parameter(description = "숙소 ID", required = true, schema = @Schema(type = "object", example = "{\"accommodationId\": 1}")) @RequestBody Map<String, Long> payload,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication); 
        Long accommodationId = payload.get("accommodationId");

        if (accommodationId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "숙소 ID(accommodationId)는 필수입니다."));
        }
        
        FavoriteDto favoriteDto = favoriteService.addOrRecoverFavorite(userId, accommodationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteDto);
    }

    @DeleteMapping
    @Operation(summary = "찜 삭제 (사용자 ID, 숙소 ID 기준)", description = "사용자가 특정 숙소를 찜 목록에서 삭제합니다 (현재 로그인한 사용자의 userId와 요청된 accommodationId 사용).")
    @ApiResponse(responseCode = "200", description = "찜 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "404", description = "삭제할 찜 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<Map<String, String>> removeFavorite(
            @Parameter(description = "삭제할 숙소 ID", required = true) @RequestParam Long accommodationId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication);
        favoriteService.removeFavorite(userId, accommodationId);
        return ResponseEntity.ok(Map.of("message", "찜이 성공적으로 삭제되었습니다."));
    }
    
    @DeleteMapping("/{favoriteId}")
    @Operation(summary = "찜 삭제 (찜 ID 기준)", description = "찜 ID를 기준으로 찜을 삭제합니다. 본인의 찜만 삭제 가능합니다.")
    @ApiResponse(responseCode = "200", description = "찜 삭제 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (본인 찜 아님)")
    @ApiResponse(responseCode = "404", description = "삭제할 찜 정보 없음")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<Map<String, String>> removeFavoriteById(
            @Parameter(description = "삭제할 찜의 ID", required = true) @PathVariable Long favoriteId,
            Authentication authentication) {
        
        Long currentUserId = getCurrentUserId(authentication);
        favoriteService.removeFavoriteById(favoriteId, currentUserId);
        return ResponseEntity.ok(Map.of("message", "찜(ID: " + favoriteId + ")이 성공적으로 삭제되었습니다."));
    }

    @GetMapping
    @Operation(summary = "내 찜 목록 조회", description = "현재 로그인한 사용자의 모든 찜 목록을 숙소 정보와 함께 조회합니다.")
    @ApiResponse(responseCode = "200", description = "찜 목록 조회 성공", content = @Content(schema = @Schema(implementation = FavoriteResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "인증 실패")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<List<FavoriteResponseDto>> getMyFavorites(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<FavoriteResponseDto> favorites = favoriteService.getFavoritesByUserId(userId);
        return ResponseEntity.ok(favorites);
    }
    
    @GetMapping("/status")
    @Operation(summary = "특정 숙소 찜 상태 확인", description = "현재 사용자가 특정 숙소를 찜했는지 여부를 확인합니다.")
    @ApiResponse(responseCode = "200", description = "찜 상태 확인 성공")
    @ApiResponse(responseCode = "401", description = "인증 실패 (필요시)")
    @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    public ResponseEntity<Map<String, Object>> getFavoriteStatus(
            @Parameter(description = "확인할 숙소 ID", required = true) @RequestParam Long accommodationId,
            Authentication authentication) {
        
        Long userId = getCurrentUserId(authentication); 

        Map<String, Object> response = new HashMap<>();
        boolean isFavorite = favoriteService.isFavorite(userId, accommodationId);
        response.put("isFavorite", isFavorite);
        return ResponseEntity.ok(response);
    }
} 