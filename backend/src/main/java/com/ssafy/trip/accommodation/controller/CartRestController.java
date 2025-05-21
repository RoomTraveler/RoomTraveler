package com.ssafy.trip.accommodation.controller;

import com.ssafy.trip.accommodation.model.Cart;
import com.ssafy.trip.accommodation.service.CartService;
import com.ssafy.trip.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Tag(name = "장바구니 API", description = "장바구니 관련 REST API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartRestController {

    private final CartService cartService;
    private static final Logger log = LoggerFactory.getLogger(CartRestController.class);

    /**
     * 세션에서 userId를 추출하는 유틸 메소드.
     */
    private Long getSessionUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    /**
     * 장바구니 조회
     */
    @Operation(
            summary = "장바구니 조회",
            description = "로그인한 사용자의 장바구니를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "장바구니 반환"),
                    @ApiResponse(responseCode = "401", description = "로그인 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping
    public ResponseEntity<?> viewCart() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
                // 인증 정보가 없거나, CustomUserDetails 타입이 아닌 경우 처리
                return unauthorized("인증 정보가 유효하지 않습니다.");
            }
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            
            if (userDetails.getUser() == null) {
                 // userDetails 안에 User 객체가 없는 경우
                return serverError(new RuntimeException("사용자 정보를 가져올 수 없습니다. (User object is null)"));
            }
            
            Long userId = userDetails.getUser().getUserId();
            if (userId == null) {
                return unauthorized("로그인한 사용자 ID를 찾을 수 없습니다.");
            }

            Cart cart = cartService.getOrCreateCart(userId);
            return ResponseEntity.ok(Map.of("cart", cart));
        } catch (SQLException e) {
            // 데이터베이스 관련 예외
            // 실제 운영 환경에서는 e.getMessage() 대신 좀 더 사용자 친화적인 메시지 또는 에러 코드를 반환하는 것이 좋습니다.
            // 또한, 민감한 정보가 로그에 남지 않도록 주의해야 합니다.
            log.error("SQL Exception in viewCart for userId: [확인 필요]", e); // userId 로깅 시 주의
            return serverError(e); 
        } catch (Exception e) {
            // 기타 예외 (NullPointerException, ClassCastException 등)
            log.error("Unexpected exception in viewCart for userId: [확인 필요]", e); // userId 로깅 시 주의
            return serverError(e);
        }
    }

    /**
     * 장바구니에 객실 추가
     */
    @Operation(
            summary = "장바구니에 객실 추가",
            description = "객실 정보를 장바구니에 추가합니다.",
            requestBody = @RequestBody(
                    required = true,
                    description = "추가할 객실 정보 (roomId, checkInDate, checkOutDate, guestCount, price)",
                    content = @Content(
                            schema = @Schema(
                                    example = "{ \"roomId\": 1, \"checkInDate\": \"2025-05-20\", \"checkOutDate\": \"2025-05-22\", \"guestCount\": 2, \"price\": 250000 }"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "장바구니 추가 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody Map<String, Object> payload, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) return unauthorized("로그인 필요");
        try {
            Long roomId = getLong(payload, "roomId");
            LocalDate checkInDate = LocalDate.parse(payload.get("checkInDate").toString());
            LocalDate checkOutDate = LocalDate.parse(payload.get("checkOutDate").toString());
            Integer guestCount = Integer.valueOf(payload.get("guestCount").toString());
            Double price = Double.valueOf(payload.get("price").toString());

            cartService.addToCart(userId, roomId, checkInDate, checkOutDate, guestCount, price);
            return ResponseEntity.ok(Map.of("message", "객실이 장바구니에 추가되었습니다."));
        } catch (SQLException e) {
            return serverError(e);
        } catch (Exception e) {
            return badRequest("파라미터 오류: " + e.getMessage());
        }
    }

    /**
     * 장바구니에서 객실 삭제
     */
    @Operation(
            summary = "장바구니에서 객실 삭제",
            description = "장바구니에서 특정 객실(카트 아이템)을 삭제합니다.",
            parameters = {
                    @Parameter(name = "cartItemId", description = "카트 아이템 ID", required = true, example = "10")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "삭제 실패"),
                    @ApiResponse(responseCode = "401", description = "로그인 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long cartItemId, HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) return unauthorized("로그인 필요");
        try {
            boolean removed = cartService.removeFromCart(cartItemId, userId);
            if (removed) {
                return ResponseEntity.ok(Map.of("message", "객실이 장바구니에서 삭제되었습니다."));
            }
            return badRequest("객실 삭제에 실패했습니다.");
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    /**
     * 장바구니 비우기
     */
    @Operation(
            summary = "장바구니 비우기",
            description = "장바구니에 담긴 모든 객실을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "비우기 성공"),
                    @ApiResponse(responseCode = "401", description = "로그인 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) return unauthorized("로그인 필요");
        try {
            int count = cartService.clearCart(userId);
            return ResponseEntity.ok(Map.of("message", count + "개의 객실이 장바구니에서 삭제되었습니다."));
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    /**
     * 장바구니 -> 예약페이지 데이터 반환
     */
    @Operation(
            summary = "장바구니 -> 예약페이지 데이터 반환",
            description = "장바구니의 객실/금액 등 예약 전 데이터를 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "체크아웃 데이터 반환"),
                    @ApiResponse(responseCode = "400", description = "장바구니 비어 있음"),
                    @ApiResponse(responseCode = "401", description = "로그인 필요"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @GetMapping("/checkout")
    public ResponseEntity<?> checkout(HttpSession session) {
        Long userId = getSessionUserId(session);
        if (userId == null) return unauthorized("로그인 필요");
        try {
            Cart cart = cartService.getOrCreateCart(userId);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                return badRequest("장바구니가 비어있습니다.");
            }
            Map<String, Object> result = new HashMap<>();
            result.put("cartItems", cart.getItems());
            result.put("totalPrice", cart.getTotalPrice());
            return ResponseEntity.ok(result);
        } catch (SQLException e) {
            return serverError(e);
        }
    }

    // --------- private 공통 함수들 (클린코드용) ---------
    private ResponseEntity<Map<String, String>> unauthorized(String msg) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
    }

    private ResponseEntity<Map<String, String>> badRequest(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
    }

    private ResponseEntity<Map<String, String>> serverError(Exception e) {
        // 여기에 SLF4J 등의 로거를 사용하여 예외를 로깅하는 코드를 추가할 수 있습니다.
        // log.error("Server error occurred: ", e); 
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "서버 내부 오류가 발생했습니다: " + e.getMessage()));
    }

    /**
     * Map에서 Long 타입 파라미터 추출 (예외 처리 포함)
     */
    private Long getLong(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) throw new IllegalArgumentException(key + "가 필요합니다.");
        return Long.valueOf(value.toString());
    }
}
