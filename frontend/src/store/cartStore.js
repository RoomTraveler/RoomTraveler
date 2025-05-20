import { defineStore } from "pinia";
import axios from "axios"; // API 호출을 위해 axios 사용 (가정)

export const useCartStore = defineStore("cart", {
  state: () => ({
    cart: {
      items: [],
      totalItems: 0,
      totalPrice: 0,
    },
    loading: false,
    error: null,
  }),
  getters: {
    // 예시: 카트가 비어있는지 확인하는 getter
    isEmpty: (state) => state.cart.items.length === 0,
    // Cart.vue에서 사용하던 cart.totalItems, cart.totalPrice는 state에서 직접 접근 가능
  },
  actions: {
    // 장바구니 정보 가져오기
    async fetchCart() {
      this.loading = true;
      this.error = null;
      try {
        const response = await axios.get("/api/cart"); // 장바구니 조회 API
        this.cart = response.data.cart;
      } catch (error) {
        console.error("Error fetching cart:", error);
        this.error = "장바구니 정보를 불러올 수 없습니다.";
      } finally {
        this.loading = false;
      }
    },

    // 장바구니에 아이템 추가
    async addToCart(itemDetails) {
      this.loading = true;
      this.error = null;
      try {
        // itemDetails: { roomId, checkInDate, checkOutDate, guestCount, price }
        // 날짜 형식은 백엔드 API에 맞게 YYYY-MM-DD로 전달해야 합니다.
        const response = await axios.post("/api/cart/add", itemDetails);
        await this.fetchCart(); // 장바구니 정보 다시 로드하여 상태 업데이트
        return response.data; // 성공 메시지 또는 데이터 반환
      } catch (error) {
        console.error("Error adding item to cart:", error);
        this.error = error.response?.data?.error || error.response?.data?.message || "장바구니에 상품을 추가할 수 없습니다.";
        throw this.error; // 컴포넌트에서 추가적인 에러 처리를 할 수 있도록 throw
      } finally {
        this.loading = false;
      }
    },

    // 장바구니 항목 삭제
    async removeCartItem(cartItemId) {
      this.loading = true;
      this.error = null;
      try {
        // 실제 API 엔드포인트로 변경해야 합니다.
        await axios.delete(`/api/cart/items/${cartItemId}`);
        // 성공 시 장바구니를 다시 불러오거나, 로컬에서 해당 아이템을 제거합니다.
        // 여기서는 간단하게 fetchCart를 다시 호출합니다.
        await this.fetchCart();
      } catch (error) {
        console.error("Error removing cart item:", error);
        this.error = "장바구니 항목을 삭제할 수 없습니다.";
      } finally {
        this.loading = false;
      }
    },

    // 장바구니 비우기
    async clearCartItems() {
      this.loading = true;
      this.error = null;
      try {
        // 실제 API 엔드포인트로 변경해야 합니다.
        await axios.post("/api/cart/clear");
        this.cart = { items: [], totalItems: 0, totalPrice: 0 }; // 로컬 상태 즉시 업데이트
      } catch (error) {
        console.error("Error clearing cart:", error);
        this.error = "장바구니를 비울 수 없습니다.";
      } finally {
        this.loading = false;
      }
    },

    // 장바구니의 모든 항목에 대해 한 번에 예약을 생성합니다.
    async checkoutCart(payload = {}) { // payload로 specialRequests 등을 받을 수 있도록 수정
      this.loading = true;
      this.error = null;
      try {
        // 백엔드의 /api/reservation/create-from-cart API 호출
        const response = await axios.post("/api/reservation/create-from-cart", payload);
        
        // 성공 시 장바구니를 비우고, 상태를 업데이트합니다.
        // 백엔드 컨트롤러에서 cartService.clearCart(userId)를 호출하므로,
        // 최신 장바구니 상태를 가져오기 위해 fetchCart를 호출합니다.
        await this.fetchCart(); 
        
        return response.data; // API 응답 반환 (예: 생성된 예약 ID 목록 등)
      } catch (error) {
        console.error("Error during cart checkout:", error);
        this.error = error.response?.data?.error || "장바구니 예약 처리에 실패했습니다.";
        throw error; // 컴포넌트에서 에러를 처리할 수 있도록 다시 throw
      } finally {
        this.loading = false;
      }
    },
  },
});
