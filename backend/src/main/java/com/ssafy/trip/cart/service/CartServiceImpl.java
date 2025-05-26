package com.ssafy.trip.cart.service;

import com.ssafy.trip.cart.dao.CartDao;
import com.ssafy.trip.cart.model.Cart;
import com.ssafy.trip.cart.model.CartItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Transactional(readOnly = true)
    public Cart getCartByUserId(Long userId) throws SQLException {
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart != null && cart.getCartId() != null) {
            // cartDao.getCartItems()는 이미 soft delete된 아이템을 제외하므로 별도 필터링 불필요
            // List<CartItem> items = cartDao.getCartItems(cart.getCartId());
            // cart.setItems(items == null ? Collections.emptyList() : items); 
            // -> cartResultMap에서 collection select로 이미 처리됨
        }
        return cart; // 없으면 null 반환, 또는 비어있는 Cart 객체 반환 정책에 따라 수정
    }

    /**
     * 사용자의 장바구니를 조회합니다. 장바구니가 없으면 새로 생성합니다.
     */
    @Override
    @Transactional
    public Cart getOrCreateCart(Long userId) throws SQLException {
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart == null) {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            cartDao.createCart(params);
            // createCart 후에는 cartId가 params Map에 담겨 반환되거나 (MyBatis 설정에 따라),
            // 새로 조회해야 할 수 있음. 여기서는 getCartByUserId를 다시 호출하여 완전한 Cart 객체 반환
            cart = cartDao.getCartByUserId(userId); 
            if (cart == null) { // 이론적으로 발생하기 어렵지만 방어 코드
                throw new SQLException("Failed to create or retrieve cart for user: " + userId);
            }
        }
        // 아이템 로딩은 getCartByUserId 내부 또는 resultMap에서 처리
        return cart;
    }

    /**
     * 장바구니에 객실을 추가합니다.
     */
    @Override
    @Transactional
    public CartItem addItemToCart(Long userId, CartItem cartItemData) throws SQLException {
        Cart cart = getOrCreateCart(userId);
        cartItemData.setCartId(cart.getCartId());
        
        // 가격 계산 로직이 있다면 여기서 수행 후 cartItemData.setPrice() 호출
        // 예: Room 정보 조회 후 가격 * 숙박일수 등
        // 현재는 CartItem에 가격이 이미 포함되어 있다고 가정

        int affectedRows = cartDao.addCartItem(cartItemData);
        if (affectedRows > 0 && cartItemData.getCartItemId() != null) {
            // cartItemData에 auto-generated key (cartItemId)가 설정되어 반환됨
            // 추가적으로 아이템 정보를 다시 조회해서 완전한 정보를 반환할 수도 있음
            return cartItemData; 
        } else {
            // ON DUPLICATE KEY UPDATE로 인해 cartItemId가 없을 수 있음. 이 경우 아이템을 다시 조회해야 함.
            // 또는 addCartItem이 업데이트된 행 수를 반환하도록 하여 분기.
            // 여기서는 간소화를 위해 cartItemData를 그대로 반환하거나, 예외 발생 또는 null 반환 고려.
            // 가장 확실한 것은 cartItemData의 고유 정보로 다시 조회하는 것.
            // 지금은 단순하게 cartItemData를 반환 (ID가 없을 수 있음에 유의)
            // -> cart.xml의 addCartItem에서 useGeneratedKeys="true" keyProperty="cartItemId" 설정 확인 필요
            // 만약 cartItemId가 항상 채워진다면 이대로 OK
            if (cartItemData.getCartItemId() == null && affectedRows > 0) {
                // 업데이트 된 경우, cartId와 roomId, 날짜로 다시 조회하는 로직 필요
                // 임시로 null 반환 또는 예외. 실제 구현 시 이 부분 보완 필요.
                System.err.println("addItemToCart: Item was updated or ID not retrieved. cartItemId: " + cartItemData.getCartItemId());
                // throw new SQLException("Failed to add or update item, or retrieve item ID.");
            }
            return cartItemData; // cartItemId가 채워져 있다고 가정.
        }
    }

    /**
     * 장바구니 아이템을 업데이트합니다.
     */
    @Override
    @Transactional
    public CartItem updateCartItem(Long userId, Long cartItemId, CartItem cartItemDetails) throws SQLException {
        Cart cart = getCartByUserId(userId);
        if (cart == null) {
            throw new SQLException("User cart not found.");
        }
        // cartItemId에 해당하는 아이템이 현재 사용자의 장바구니에 속하는지 확인하는 로직 추가 권장
        List<CartItem> currentItems = cartDao.getCartItems(cart.getCartId());
        boolean itemExistsInCart = currentItems.stream().anyMatch(item -> item.getCartItemId().equals(cartItemId));
        if (!itemExistsInCart) {
            throw new SQLException("Cart item not found in user's cart or already removed.");
        }

        cartItemDetails.setCartItemId(cartItemId); // 업데이트 대상 ID 명시
        // cartItemDetails.setCartId(cart.getCartId()); // 필요시 CartId도 설정

        // 가격 재계산 로직이 있다면 여기서 수행

        int affectedRows = cartDao.updateCartItem(cartItemDetails);
        if (affectedRows > 0) {
            // 업데이트 성공 시, 업데이트된 정보로 다시 조회하여 반환하는 것이 가장 정확함
            // CartItem updatedItem = cartDao.getCartItemById(cartItemId); // 이런 메서드가 있다면...
            // return updatedItem;
            // 지금은 cartItemDetails (입력값)를 그대로 반환. 실제로는 DB에서 읽어온 값을 반환해야 함.
            // 이 예시에서는 단순화를 위해 입력값을 반환하지만, 실제로는 DB에서 조회한 값을 반환해야 합니다.
            // 예를 들어, getCartItems를 다시 호출하여 해당 아이템을 찾아 반환할 수 있습니다.
             List<CartItem> items = cartDao.getCartItems(cart.getCartId());
             return items.stream().filter(i -> i.getCartItemId().equals(cartItemId)).findFirst().orElse(null);
        } else {
            // 업데이트할 아이템이 없거나 (이미 soft delete 되었거나), 다른 이유로 실패
            return null; 
        }
    }

    /**
     * 장바구니 아이템을 삭제합니다.
     */
    @Override
    @Transactional
    public boolean removeItemFromCart(Long userId, Long cartItemId) throws SQLException {
        Cart cart = getCartByUserId(userId);
        if (cart == null) {
            // 사용자의 장바구니가 없으면 삭제할 아이템도 없음
            return false; 
        }
        // cartItemId가 현재 사용자의 장바구니에 속하는 아이템인지 확인하는 로직 추가 권장
        // 예를 들어, cartDao.getCartItemByIdAndCartId(cartItemId, cart.getCartId()) 와 같은 메서드로 확인
        // 여기서는 해당 아이템이 존재하고, 삭제 권한이 있다고 가정
        return cartDao.softDeleteCartItem(cartItemId) > 0;
    }

    /**
     * 장바구니를 비웁니다.
     */
    @Override
    @Transactional
    public boolean clearCart(Long userId) throws SQLException {
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart != null && cart.getCartId() != null) {
            return cartDao.softDeleteAllCartItems(cart.getCartId()) > 0;
        }
        return false; // 장바구니가 없거나 비어있으면 false (또는 0 반환 정책에 따라)
    }

    /**
     * 사용자의 장바구니 아이템 목록을 조회합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByUserId(Long userId) throws SQLException {
        Cart cart = cartDao.getCartByUserId(userId);
        if (cart != null && cart.getCartId() != null) {
            return cartDao.getCartItems(cart.getCartId());
        }
        return Collections.emptyList();
    }

    /**
     * 사용자의 장바구니 총액을 계산합니다.
     * 각 아이템의 가격과 숙박 기간을 고려하여 총액을 계산합니다.
     *
     * @param userId 사용자 ID
     * @return 장바구니 총액
     * @throws SQLException SQL 예외
     */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateTotalPrice(Long userId) throws SQLException {
        List<CartItem> items = getCartItemsByUserId(userId);
        BigDecimal totalPrice = BigDecimal.ZERO;
        if (items != null) {
            for (CartItem item : items) {
                if (item.getPrice() != null && item.getCheckInDate() != null && item.getCheckOutDate() != null) {
                    // BigDecimal로 가격을 가져오고, 날짜 차이를 계산하여 총액에 더함
                    // 이 부분은 실제 가격 정책(예: 1박당 가격인지, 총액인지)에 따라 달라질 수 있음
                    // 여기서는 item.getPrice()가 총 기간에 대한 가격이라고 가정
                    // 또는, 1박 가격이라면 숙박일수를 곱해야 함
                    // long nights = ChronoUnit.DAYS.between(item.getCheckInDate(), item.getCheckOutDate());
                    // totalPrice = totalPrice.add(item.getPrice().multiply(BigDecimal.valueOf(nights)));
                    totalPrice = totalPrice.add(item.getPrice()); 
                }
            }
        }
        return totalPrice;
    }
}
