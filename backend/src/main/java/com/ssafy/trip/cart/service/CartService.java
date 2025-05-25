package com.ssafy.trip.cart.service;

import com.ssafy.trip.cart.model.Cart;
import com.ssafy.trip.cart.model.CartItem;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * 장바구니 서비스 인터페이스
 */
public interface CartService {
    
    /**
     * 사용자 ID로 장바구니를 조회합니다. (아이템 포함, soft delete 된 아이템은 제외)
     * 장바구니가 존재하지 않으면 null을 반환하거나, 비어있는 Cart 객체를 반환할 수 있습니다.
     * 여기서는 Cart 객체 반환을 가정합니다 (items 리스트는 비어있을 수 있음).
     */
    Cart getCartByUserId(Long userId) throws SQLException;
    
    /**
     * 사용자의 장바구니를 조회하거나, 없으면 새로 생성하여 반환합니다.
     * 내부적으로 getCartByUserId 와 createCart DAO 메서드를 활용합니다.
     */
    Cart getOrCreateCart(Long userId) throws SQLException;
    
    /**
     * 장바구니에 아이템을 추가하거나, 이미 존재하고 soft delete된 경우 재활성화/업데이트합니다.
     * (DAO의 ON DUPLICATE KEY UPDATE 로직 활용)
     * @return 추가되거나 업데이트된 CartItem 정보 (ID 포함)
     */
    CartItem addItemToCart(Long userId, CartItem cartItemData) throws SQLException;
    
    /**
     * 장바구니 아이템의 수량, 날짜 등을 업데이트합니다.
     * soft delete된 아이템은 업데이트하지 않습니다.
     * @param userId 현재 로그인한 사용자 ID (권한 확인 등에 사용)
     * @param cartItemId 업데이트할 장바구니 아이템 ID
     * @param cartItemDetails 업데이트할 내용을 담은 CartItem 객체 (roomId, checkInDate, checkOutDate, guestCount, price 등)
     * @return 업데이트된 CartItem 정보. 업데이트할 아이템이 없거나 권한이 없으면 null 또는 예외 발생.
     */
    CartItem updateCartItem(Long userId, Long cartItemId, CartItem cartItemDetails) throws SQLException;
    
    /**
     * 장바구니에서 특정 아이템을 soft delete 합니다.
     * @param userId 현재 로그인한 사용자 ID (권한 확인 등에 사용)
     * @param cartItemId 삭제할 장바구니 아이템 ID
     * @return 삭제 작업 성공 여부
     */
    boolean removeItemFromCart(Long userId, Long cartItemId) throws SQLException;
    
    /**
     * 특정 사용자의 장바구니에 있는 모든 (soft delete 되지 않은) 아이템을 soft delete 합니다.
     * @param userId 사용자 ID
     * @return 작업 성공 여부 (하나 이상의 아이템이 삭제되었으면 true)
     */
    boolean clearCart(Long userId) throws SQLException;
    
    /**
     * 특정 사용자의 장바구니에 있는 (soft delete 되지 않은) 아이템 목록을 조회합니다.
     */
    List<CartItem> getCartItemsByUserId(Long userId) throws SQLException;
    
    /**
     * 특정 사용자의 장바구니 총액을 계산합니다.
     * (soft delete 되지 않은 아이템 기준)
     */
    BigDecimal calculateTotalPrice(Long userId) throws SQLException;
}