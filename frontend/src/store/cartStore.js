import { defineStore } from "pinia";
import api from "@/api/index";

export const useCartStore = defineStore("cart", {
  state: () => ({
    cart: {
      items: [],
      totalItems: 0,
      totalPrice: 0,
      cartId: null,
      userId: null,
      userName: null,
      createdAt: null,
      updatedAt: null,
    },
    loading: false,
    error: null,
    // 결제 프로세스 중 필요한 임시 데이터 저장용 (선택 사항)
    // currentPaymentInfo: null, // { merchantUid, amount, paymentName }
  }),
  getters: {
    isEmpty: (state) => state.cart.items.length === 0,
    // 선택된 아이템들의 ID를 반환하는 getter (PaymentPrepareRequestDto 생성 시 사용)
    selectedCartItemIds: (state) => {
      // 만약 장바구니 아이템 중 일부만 선택하여 결제하는 기능이 있다면,
      // items 배열에 selected 같은 플래그가 있어야 하고, 그것을 기준으로 필터링해야 함.
      // 현재는 장바구니 전체를 결제한다고 가정하고 모든 item의 cartItemId를 반환.
      return state.cart.items.map(item => item.cartItemId).filter(id => id != null);
    }
  },
  actions: {
    async fetchCart() {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.api.get("/api/cart");
        const cartData = response.data;
        this.cart = {
          items: cartData.items || [],
          totalPrice: cartData.totalPrice || 0,
          totalItems: (cartData.items || []).length, // 백엔드가 totalItems를 직접 주면 그것을 사용
          cartId: cartData.cartId,
          userId: cartData.userId,
          userName: cartData.userName,
          createdAt: cartData.createdAt,
          updatedAt: cartData.updatedAt,
        };
      } catch (error) {
        console.error("Error fetching cart:", error);
        this.error = error.response?.data?.error || error.response?.data?.message || "장바구니 정보를 불러올 수 없습니다.";
        // 에러 발생 시에도 cart 구조는 유지하되, 비워줌
        this.cart = { items: [], totalItems: 0, totalPrice: 0, cartId: null, userId: null, userName: null, createdAt: null, updatedAt: null };
      } finally {
        this.loading = false;
      }
    },

    async addToCart(itemDetails) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.api.post("/api/cart/add", itemDetails);
        await this.fetchCart(); // 장바구니 상태를 최신으로 동기화
        return response.data; // 성공 메시지 또는 업데이트된 장바구니 정보 반환 가능
      } catch (error) {
        console.error("Error adding item to cart:", error);
        this.error =
            error.response?.data?.error ||
            error.response?.data?.message ||
            "장바구니에 상품을 추가할 수 없습니다.";
        throw new Error(this.error); // 에러를 throw하여 호출부에서 catch 가능하도록
      } finally {
        this.loading = false;
      }
    },

    async removeCartItem(cartItemId) {
      this.loading = true;
      this.error = null;
      try {
        await api.api.delete(`/api/cart/remove/${cartItemId}`);
        await this.fetchCart(); // 장바구니 상태를 최신으로 동기화
      } catch (error) {
        console.error("Error removing cart item:", error);
        this.error = error.response?.data?.error || error.response?.data?.message || "장바구니 항목을 삭제할 수 없습니다.";
        throw new Error(this.error);
      } finally {
        this.loading = false;
      }
    },

    async clearCartItems() {
      this.loading = true;
      this.error = null;
      try {
        await api.api.delete("/api/cart/clear");
        // 성공 시 장바구니 상태를 초기화
        this.cart = { items: [], totalItems: 0, totalPrice: 0, cartId: null, userId: null, userName: null, createdAt: null, updatedAt: null };
      } catch (error) {
        console.error("Error clearing cart:", error);
        this.error = error.response?.data?.error || error.response?.data?.message || "장바구니를 비울 수 없습니다.";
        throw new Error(this.error);
      } finally {
        this.loading = false;
      }
    },

    // 기존 checkoutCart 액션은 새로운 결제 플로우로 대체되므로 주석 처리 또는 삭제
    /*
    async checkoutCart(payload = {}) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.api.post("/api/reservation/create-from-cart", payload);
        await this.fetchCart();
        return response.data;
      } catch (error) {
        console.error("Error during cart checkout:", error);
        this.error =
            error.response?.data?.error || "장바구니 예약 처리에 실패했습니다.";
        throw error;
      } finally {
        this.loading = false;
      }
    },
    */

    // 1. 결제 준비 액션
    async preparePaymentForCheckout(prepareRequest) {
      // prepareRequest는 { reservationsToCreate: [ReservationCreationDto], specialRequests: "..." } 형태여야 함
      this.loading = true;
      this.error = null;
      try {
        const response = await api.api.post("/api/v1/payments/prepare", prepareRequest);
        // this.currentPaymentInfo = response.data; // 필요시 state에 임시 저장
        return response.data; // { merchantUid, amount, paymentName }
      } catch (error) {
        console.error("Error preparing payment:", error);
        this.error = error.response?.data?.error || error.response?.data?.message || "결제 준비 중 오류가 발생했습니다.";
        throw new Error(this.error);
      } finally {
        this.loading = false;
      }
    },

    // 2. 결제 완료 및 검증 액션
    async completePaymentAfterIamport(completeRequest) {
      // completeRequest는 { impUid: "...", merchantUid: "..." } 형태여야 함
      this.loading = true;
      this.error = null;
      try {
        const response = await api.api.post("/api/v1/payments/complete", completeRequest);
        await this.fetchCart(); // 결제 완료 후 장바구니 비우기 (백엔드에서 처리 안 할 경우)
        return response.data; // { message, paymentId, reservationIds }
      } catch (error) {
        console.error("Error completing payment:", error);
        this.error = error.response?.data?.error || error.response?.data?.message || "결제 처리 중 오류가 발생했습니다.";
        // this.currentPaymentInfo = null; // 임시 정보 정리
        throw new Error(this.error);
      } finally {
        this.loading = false;
      }
    }
  },
});
