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
        // 실제 API 엔드포인트로 변경해야 합니다.
        const response = await axios.get("/api/cart");
        this.cart = response.data;
      } catch (error) {
        console.error("Error fetching cart:", error);
        this.error = "장바구니 정보를 불러올 수 없습니다.";
        // API 호출 실패 시, cart 상태를 초기화하거나 이전 상태를 유지할 수 있습니다.
        // 여기서는 초기화하지 않고 에러 메시지만 설정합니다.
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

    // 예약하기 (결제)
    async checkoutCart() {
      this.loading = true;
      this.error = null;
      try {
        // 실제 API 엔드포인트로 변경해야 합니다.
        const response = await axios.post("/api/checkout/cart");
        // 성공 시 로직 (예: 주문 완료 페이지로 이동, 장바구니 비우기 등)
        // 여기서는 일단 성공했다고 가정하고, 에러를 발생시키지 않습니다.
        // this.cart = { items: [], totalItems: 0, totalPrice: 0 }; // 결제 성공 시 장바구니 비우기
        return response.data; // API 응답 반환 (예: 주문 ID 등)
      } catch (error) {
        console.error("Error during checkout:", error);
        this.error = "예약 처리에 실패했습니다.";
        throw error; // 컴포넌트에서 에러를 처리할 수 있도록 다시 throw
      } finally {
        this.loading = false;
      }
    },
  },
});
