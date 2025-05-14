package com.ssafy.trip.accommodation.dao;

import com.ssafy.trip.accommodation.model.Cart;
import com.ssafy.trip.accommodation.model.CartItem;

import org.apache.ibatis.annotations.Mapper;

import java.sql.SQLException;
import java.util.List;

/**
 * 장바구니 데이터 액세스 인터페이스
 */
@Mapper
public interface CartDao {

    /**
     * 사용자의 장바구니를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 장바구니 정보
     * @throws SQLException SQL 예외
     */
    Cart getCartByUserId(Long userId) throws SQLException;

    /**
     * 장바구니를 생성합니다.
     * 
     * @param userId 사용자 ID
     * @return 생성된 장바구니 ID
     * @throws SQLException SQL 예외
     */
    Long createCart(Long userId) throws SQLException;

    /**
     * 장바구니에 아이템을 추가합니다.
     * 
     * @param cartItem 장바구니 아이템
     * @return 생성된 장바구니 아이템 ID
     * @throws SQLException SQL 예외
     */
    Long addCartItem(CartItem cartItem) throws SQLException;

    /**
     * 장바구니 아이템을 조회합니다.
     * 
     * @param cartId 장바구니 ID
     * @return 장바구니 아이템 목록
     * @throws SQLException SQL 예외
     */
    List<CartItem> getCartItems(Long cartId) throws SQLException;

    /**
     * 장바구니 아이템을 삭제합니다.
     * 
     * @param cartItemId 장바구니 아이템 ID
     * @return 삭제된 행 수
     * @throws SQLException SQL 예외
     */
    int removeCartItem(Long cartItemId) throws SQLException;

    /**
     * 장바구니를 비웁니다.
     * 
     * @param cartId 장바구니 ID
     * @return 삭제된 행 수
     * @throws SQLException SQL 예외
     */
    int clearCart(Long cartId) throws SQLException;

    /**
     * 장바구니 아이템을 업데이트합니다.
     * 
     * @param cartItem 장바구니 아이템
     * @return 업데이트된 행 수
     * @throws SQLException SQL 예외
     */
    int updateCartItem(CartItem cartItem) throws SQLException;
}
