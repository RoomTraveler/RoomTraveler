package com.ssafy.trip.accommodation.service;

import com.ssafy.trip.accommodation.dao.CartDao;
import com.ssafy.trip.accommodation.model.Cart;
import com.ssafy.trip.accommodation.model.CartItem;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 * 장바구니 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartDao cartDao;

    /**
     * 사용자의 장바구니를 조회합니다. 장바구니가 없으면 새로 생성합니다.
     */
    @Override
    public Cart getOrCreateCart(Long userId) throws SQLException {
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart == null) {
            Long cartId = cartDao.createCart(userId);
            cart = new Cart();
            cart.setCartId(cartId);
            cart.setUserId(userId);
        } else {
            // 장바구니 아이템 목록 조회
            List<CartItem> items = cartDao.getCartItems(cart.getCartId());
            cart.setItems(items);
        }
        return cart;
    }

    /**
     * 장바구니에 객실을 추가합니다.
     */
    @Override
    public Long addToCart(Long userId, Long roomId, LocalDate checkInDate, LocalDate checkOutDate,
                         Integer guestCount, double price) throws SQLException {
        // 사용자의 장바구니 조회 또는 생성
        Cart cart = getOrCreateCart(userId);
        
        // 장바구니 아이템 생성
        CartItem cartItem = CartItem.builder()
                .cartId(cart.getCartId())
                .roomId(roomId)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .guestCount(guestCount)
                .price(BigDecimal.valueOf(price))
                .build();
        
        // 장바구니에 아이템 추가
        return cartDao.addCartItem(cartItem);
    }

    /**
     * 장바구니 아이템을 삭제합니다.
     */
    @Override
    public boolean removeFromCart(Long cartItemId, Long userId) throws SQLException {
        // 사용자의 장바구니 조회
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart == null) {
            return false;
        }
        
        // 장바구니 아이템 목록 조회
        List<CartItem> items = cartDao.getCartItems(cart.getCartId());
        
        // 삭제하려는 아이템이 사용자의 장바구니에 있는지 확인
        boolean itemExists = items.stream()
                .anyMatch(item -> item.getCartItemId().equals(cartItemId));
        
        if (!itemExists) {
            return false;
        }
        
        // 장바구니 아이템 삭제
        int result = cartDao.removeCartItem(cartItemId);
        return result > 0;
    }

    /**
     * 장바구니를 비웁니다.
     */
    @Override
    public int clearCart(Long userId) throws SQLException {
        // 사용자의 장바구니 조회
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart == null) {
            return 0;
        }
        
        // 장바구니 비우기
        return cartDao.clearCart(cart.getCartId());
    }

    /**
     * 장바구니 아이템을 업데이트합니다.
     */
    @Override
    public boolean updateCartItem(CartItem cartItem, Long userId) throws SQLException {
        // 사용자의 장바구니 조회
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart == null) {
            return false;
        }
        
        // 장바구니 아이템 목록 조회
        List<CartItem> items = cartDao.getCartItems(cart.getCartId());
        
        // 업데이트하려는 아이템이 사용자의 장바구니에 있는지 확인
        boolean itemExists = items.stream()
                .anyMatch(item -> item.getCartItemId().equals(cartItem.getCartItemId()));
        
        if (!itemExists) {
            return false;
        }
        
        // 장바구니 아이템 업데이트
        int result = cartDao.updateCartItem(cartItem);
        return result > 0;
    }

    /**
     * 사용자의 장바구니 아이템 목록을 조회합니다.
     */
    @Override
    public List<CartItem> getCartItems(Long userId) throws SQLException {
        // 사용자의 장바구니 조회
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart == null) {
            return List.of();
        }
        
        // 장바구니 아이템 목록 조회
        return cartDao.getCartItems(cart.getCartId());
    }
}