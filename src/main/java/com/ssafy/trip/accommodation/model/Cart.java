package com.ssafy.trip.accommodation.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.ssafy.trip.accommodation.model.CartItem;

/**
 * 장바구니 정보를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    private Long cartId;              // 장바구니 ID
    private Long userId;              // 사용자 ID
    private LocalDateTime createdAt;  // 생성 시간
    private LocalDateTime updatedAt;  // 수정 시간

    // 추가 필드 - 조인 시 사용
    private String userName;          // 사용자 이름
    private List<CartItem> items;     // 장바구니 아이템 목록

    /**
     * 장바구니의 총 가격을 계산합니다.
     */
    public BigDecimal getTotalPrice() {
        if (items == null || items.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return items.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 장바구니의 총 아이템 수를 반환합니다.
     */
    public int getTotalItems() {
        if (items == null) {
            return 0;
        }
        return items.size();
    }
}
