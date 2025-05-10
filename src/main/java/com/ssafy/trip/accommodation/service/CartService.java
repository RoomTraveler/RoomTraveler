package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.model.Cart;
import com.ssafy.trip.accommodation.model.CartItem;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * 장바구니 서비스 인터페이스
 */
public interface CartService {
    
    /**
     * 사용자의 장바구니를 조회합니다. 장바구니가 없으면 새로 생성합니다.
     * 
     * @param userId 사용자 ID
     * @return 장바구니 정보
     * @throws SQLException SQL 예외
     */
    Cart getOrCreateCart(Long userId) throws SQLException;
    
    /**
     * 장바구니에 객실을 추가합니다.
     * 
     * @param userId 사용자 ID
     * @param roomId 객실 ID
     * @param checkInDate 체크인 날짜
     * @param checkOutDate 체크아웃 날짜
     * @param guestCount 투숙객 수
     * @param price 가격
     * @return 생성된 장바구니 아이템 ID
     * @throws SQLException SQL 예외
     */
    Long addToCart(Long userId, Long roomId, LocalDate checkInDate, LocalDate checkOutDate, 
                  Integer guestCount, double price) throws SQLException;
    
    /**
     * 장바구니 아이템을 삭제합니다.
     * 
     * @param cartItemId 장바구니 아이템 ID
     * @param userId 사용자 ID (권한 확인용)
     * @return 삭제 성공 여부
     * @throws SQLException SQL 예외
     */
    boolean removeFromCart(Long cartItemId, Long userId) throws SQLException;
    
    /**
     * 장바구니를 비웁니다.
     * 
     * @param userId 사용자 ID
     * @return 삭제된 아이템 수
     * @throws SQLException SQL 예외
     */
    int clearCart(Long userId) throws SQLException;
    
    /**
     * 장바구니 아이템을 업데이트합니다.
     * 
     * @param cartItem 장바구니 아이템
     * @param userId 사용자 ID (권한 확인용)
     * @return 업데이트 성공 여부
     * @throws SQLException SQL 예외
     */
    boolean updateCartItem(CartItem cartItem, Long userId) throws SQLException;
    
    /**
     * 사용자의 장바구니 아이템 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 장바구니 아이템 목록
     * @throws SQLException SQL 예외
     */
    List<CartItem> getCartItems(Long userId) throws SQLException;
}