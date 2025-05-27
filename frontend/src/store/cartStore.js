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

        const formatDateFromArray = (dateArray) => {
          if (!dateArray || !Array.isArray(dateArray) || dateArray.length < 3) return null;
          const date = new Date(dateArray[0], dateArray[1] - 1, dateArray[2]);
          if (isNaN(date.getTime())) return null;
          return date.toISOString().split('T')[0]; // YYYY-MM-DD
        };

        const processedItems = (cartData.items || []).map(item => ({
          ...item,
          checkInDate: formatDateFromArray(item.checkInDate),
          checkOutDate: formatDateFromArray(item.checkOutDate),
          roomName: typeof item.roomName === 'string' ? item.roomName.trim() : item.roomName,
        }));
        
        // cart 객체의 속성을 직접 수정하여 반응성 유지
        this.cart.items = processedItems;
        this.cart.totalPrice = cartData.totalPrice || 0;
        this.cart.totalItems = processedItems.length;
        this.cart.cartId = cartData.cartId;
        this.cart.userId = cartData.userId;
        this.cart.userName = cartData.userName;
        this.cart.createdAt = cartData.createdAt; // 원본 유지 또는 필요시 변환
        this.cart.updatedAt = cartData.updatedAt; // 원본 유지 또는 필요시 변환

      } catch (error) {
        console.error("Error fetching cart:", error);
        this.error = error.response?.data?.error || error.response?.data?.message || "장바구니 정보를 불러올 수 없습니다.";
        // 에러 발생 시 cart 상태 초기화 (기존 로직 유지)
        this.cart.items = [];
        this.cart.totalItems = 0;
        this.cart.totalPrice = 0;
        this.cart.cartId = null;
        this.cart.userId = null;
        this.cart.userName = null;
        this.cart.createdAt = null;
        this.cart.updatedAt = null;
      } finally {
        this.loading = false;
      }
    },

    async addToCart(itemDetails) {
      this.loading = true;
      this.error = null;
      console.log("[CartStore] addToCart called with itemDetails:", JSON.parse(JSON.stringify(itemDetails)));
      try {
        const response = await api.api.post("/api/cart/add", itemDetails);
        console.log("[CartStore] addToCart API response:", response);
        await this.fetchCart(); // 장바구니 상태를 최신으로 동기화
        return response.data; // 성공 메시지 또는 업데이트된 장바구니 정보 반환 가능
      } catch (error) {
        console.error("[CartStore] Error adding item to cart - API Error Object:", error);
        if (error.response) {
          console.error("[CartStore] API Error Response Data:", error.response.data);
          console.error("[CartStore] API Error Response Status:", error.response.status);
          console.error("[CartStore] API Error Response Headers:", error.response.headers);
        }
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

    // 기존 결제 관련 액션들은 paymentStore로 이전되었으므로 여기서는 제거하거나 주석 처리합니다.
    // async preparePaymentForCheckout(prepareRequest) { ... }
    // async completePaymentAfterIamport(completeRequest) { ... }

    clearError() {
      this.error = null;
    }
  },
});
