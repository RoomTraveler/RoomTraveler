package com.ssafy.trip.cart.controller;

import com.ssafy.trip.cart.model.Cart;
import com.ssafy.trip.cart.model.CartItem;
import com.ssafy.trip.cart.service.CartService;
import com.ssafy.trip.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "장바구니 API", description = "장바구니 관련 REST API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartRestController {

    private final CartService cartService;
    private static final Logger log = LoggerFactory.getLogger(CartRestController.class);

    private Long getCurrentUserId(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUser() == null) {
            return null;
        }
        return userDetails.getUser().getUserId();
    }

    @Operation(summary = "장바구니 조회", description = "로그인한 사용자의 장바구니를 조회합니다.")
    @GetMapping
    public ResponseEntity<?> viewCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        if (userId == null) return unauthorized("로그인이 필요하거나 사용자 정보를 찾을 수 없습니다.");

        try {
            Cart cart = cartService.getOrCreateCart(userId);
            BigDecimal totalPrice = cartService.calculateTotalPrice(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("cartId", cart.getCartId());
            response.put("userId", cart.getUserId());
            response.put("userName", cart.getUserName());
            response.put("items", cart.getItems() == null ? Collections.emptyList() : cart.getItems());
            response.put("totalPrice", totalPrice);
            response.put("createdAt", cart.getCreatedAt());
            response.put("updatedAt", cart.getUpdatedAt());
            return ResponseEntity.ok(response);
        } catch (SQLException e) {
            log.error("SQL Exception in viewCart for userId: {}", userId, e);
            return serverError(e, "장바구니 조회 중 데이터베이스 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("Unexpected exception in viewCart for userId: {}", userId, e);
            return serverError(e, "장바구니 조회 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    @Schema(description = "장바구니 아이템 추가 요청 DTO")
    private static class AddItemRequest {
        @Schema(example = "1") public Long roomId;
        @Schema(example = "2025-06-01") public LocalDate checkInDate;
        @Schema(example = "2025-06-03") public LocalDate checkOutDate;
        @Schema(example = "2") public Integer guestCount;
        @Schema(example = "150000.00") public BigDecimal price;
    }

    @Operation(summary = "장바구니에 객실 추가", description = "객실 정보를 장바구니에 추가합니다.")
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                       @RequestBody AddItemRequest addItemRequest) {
        Long userId = getCurrentUserId(userDetails);
        if (userId == null) return unauthorized("로그인이 필요하거나 사용자 정보를 찾을 수 없습니다.");

        try {
            if (addItemRequest.roomId == null || addItemRequest.checkInDate == null || 
                addItemRequest.checkOutDate == null || addItemRequest.guestCount == null || addItemRequest.price == null) {
                return badRequest("필수 파라미터가 누락되었습니다: roomId, checkInDate, checkOutDate, guestCount, price");
            }
            if (addItemRequest.checkInDate.isAfter(addItemRequest.checkOutDate) || addItemRequest.checkInDate.isEqual(addItemRequest.checkOutDate)){
                return badRequest("체크인 날짜는 체크아웃 날짜보다 이전이어야 합니다.");
            }
            if (addItemRequest.guestCount <= 0) {
                return badRequest("투숙객 수는 1명 이상이어야 합니다.");
            }

            CartItem cartItem = new CartItem();
            cartItem.setRoomId(addItemRequest.roomId);
            cartItem.setCheckInDate(addItemRequest.checkInDate);
            cartItem.setCheckOutDate(addItemRequest.checkOutDate);
            cartItem.setGuestCount(addItemRequest.guestCount);
            cartItem.setPrice(addItemRequest.price);

            CartItem addedOrUpdatedItem = cartService.addItemToCart(userId, cartItem);
            return ResponseEntity.ok(Map.of("message", "객실이 장바구니에 추가/업데이트되었습니다.", "item", addedOrUpdatedItem));
        } catch (SQLException e) {
            log.error("SQL Exception in addToCart for userId: {}", userId, e);
            return serverError(e, "장바구니 추가 중 데이터베이스 오류가 발생했습니다.");
        } catch (IllegalArgumentException e) {
             log.warn("Bad request in addToCart for userId: {}, error: {}", userId, e.getMessage());
            return badRequest("잘못된 요청입니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected exception in addToCart for userId: {}", userId, e);
            return serverError(e, "장바구니 추가 중 예상치 못한 오류가 발생했습니다.");
        }
    }
    
    @Schema(description = "장바구니 아이템 업데이트 요청 DTO")
    private static class UpdateItemRequest {
        @Schema(example = "2025-06-05") public LocalDate checkInDate;
        @Schema(example = "2025-06-07") public LocalDate checkOutDate;
        @Schema(example = "1") public Integer guestCount;
        @Schema(example = "120000.00") public BigDecimal price; 
    }

    @Operation(summary = "장바구니 아이템 업데이트", description = "장바구니의 특정 아이템 정보를 수정합니다.")
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<?> updateCartItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable Long cartItemId,
                                            @RequestBody UpdateItemRequest updateItemRequest) {
        Long userId = getCurrentUserId(userDetails);
        if (userId == null) return unauthorized("로그인이 필요하거나 사용자 정보를 찾을 수 없습니다.");

        try {
            if (updateItemRequest.checkInDate != null && updateItemRequest.checkOutDate != null && 
                (updateItemRequest.checkInDate.isAfter(updateItemRequest.checkOutDate) || updateItemRequest.checkInDate.isEqual(updateItemRequest.checkOutDate))){
                return badRequest("체크인 날짜는 체크아웃 날짜보다 이전이어야 합니다.");
            }
            if (updateItemRequest.guestCount != null && updateItemRequest.guestCount <= 0) {
                return badRequest("투숙객 수는 1명 이상이어야 합니다.");
            }

            CartItem itemDetailsToUpdate = new CartItem();
            if (updateItemRequest.checkInDate != null) itemDetailsToUpdate.setCheckInDate(updateItemRequest.checkInDate);
            if (updateItemRequest.checkOutDate != null) itemDetailsToUpdate.setCheckOutDate(updateItemRequest.checkOutDate);
            if (updateItemRequest.guestCount != null) itemDetailsToUpdate.setGuestCount(updateItemRequest.guestCount);
            if (updateItemRequest.price != null) itemDetailsToUpdate.setPrice(updateItemRequest.price);

            CartItem updatedItem = cartService.updateCartItem(userId, cartItemId, itemDetailsToUpdate);
            if (updatedItem != null) {
                return ResponseEntity.ok(Map.of("message", "장바구니 아이템이 업데이트되었습니다.", "item", updatedItem));
            } else {
                return badRequest("아이템 업데이트에 실패했거나 해당 아이템을 찾을 수 없습니다.");
            }
        } catch (SQLException e) {
            log.error("SQL Exception in updateCartItem for userId: {}, cartItemId: {}", userId, cartItemId, e);
            return serverError(e, "아이템 업데이트 중 데이터베이스 오류가 발생했습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("Bad request in updateCartItem for userId: {}, cartItemId: {}, error: {}", userId, cartItemId, e.getMessage());
            return badRequest("잘못된 요청입니다: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected exception in updateCartItem for userId: {}, cartItemId: {}", userId, cartItemId, e);
            return serverError(e, "아이템 업데이트 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "장바구니에서 객실 삭제", description = "장바구니에서 특정 객실(카트 아이템)을 삭제합니다.")
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<?> removeFromCart(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable Long cartItemId) {
        Long userId = getCurrentUserId(userDetails);
        if (userId == null) return unauthorized("로그인이 필요하거나 사용자 정보를 찾을 수 없습니다.");
        try {
            boolean removed = cartService.removeItemFromCart(userId, cartItemId);
            if (removed) {
                return ResponseEntity.ok(Map.of("message", "객실이 장바구니에서 삭제되었습니다."));
            }
            return badRequest("객실 삭제에 실패했거나 해당 아이템을 찾을 수 없습니다.");
        } catch (SQLException e) {
            log.error("SQL Exception in removeFromCart for userId: {}, cartItemId: {}", userId, cartItemId, e);
            return serverError(e, "객실 삭제 중 데이터베이스 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("Unexpected exception in removeFromCart for userId: {}, cartItemId: {}", userId, cartItemId, e);
            return serverError(e, "객실 삭제 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "장바구니 비우기", description = "장바구니에 담긴 모든 객실을 삭제합니다.")
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        if (userId == null) return unauthorized("로그인이 필요하거나 사용자 정보를 찾을 수 없습니다.");
        try {
            boolean cleared = cartService.clearCart(userId);
            if (cleared) {
                 return ResponseEntity.ok(Map.of("message", "장바구니의 모든 아이템이 삭제되었습니다."));
            } else {
                 return ResponseEntity.ok(Map.of("message", "장바구니가 비어있거나, 삭제할 아이템이 없습니다."));
            }
        } catch (SQLException e) {
            log.error("SQL Exception in clearCart for userId: {}", userId, e);
            return serverError(e, "장바구니 비우기 중 데이터베이스 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("Unexpected exception in clearCart for userId: {}", userId, e);
            return serverError(e, "장바구니 비우기 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    @Operation(summary = "장바구니 -> 예약페이지 데이터 반환", description = "장바구니의 객실/금액 등 예약 전 데이터를 반환합니다.")
    @GetMapping("/checkout-info")
    public ResponseEntity<?> getCheckoutInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = getCurrentUserId(userDetails);
        if (userId == null) return unauthorized("로그인이 필요하거나 사용자 정보를 찾을 수 없습니다.");
        try {
            Cart cart = cartService.getCartByUserId(userId);
            if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
                return badRequest("장바구니가 비어있습니다. 예약할 상품을 먼저 담아주세요.");
            }
            BigDecimal totalPrice = cartService.calculateTotalPrice(userId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("items", cart.getItems());
            result.put("totalPrice", totalPrice);
            return ResponseEntity.ok(result);
        } catch (SQLException e) {
            log.error("SQL Exception in getCheckoutInfo for userId: {}", userId, e);
            return serverError(e, "체크아웃 정보 조회 중 데이터베이스 오류가 발생했습니다.");
        } catch (Exception e) {
            log.error("Unexpected exception in getCheckoutInfo for userId: {}", userId, e);
            return serverError(e, "체크아웃 정보 조회 중 예상치 못한 오류가 발생했습니다.");
        }
    }

    private ResponseEntity<Map<String, String>> unauthorized(String msg) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", msg));
    }

    private ResponseEntity<Map<String, String>> badRequest(String msg) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
    }

    private ResponseEntity<Map<String, String>> serverError(Exception e, String defaultMessage) {
        log.error("Server error: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(Map.of("error", defaultMessage != null ? defaultMessage : "서버 내부 오류가 발생했습니다."));
    }
}
