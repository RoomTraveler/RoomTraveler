import { defineStore } from "pinia";
import api from "@/api/index";

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
    isEmpty: (state) => state.cart.items.length === 0,
  },
  actions: {
    async fetchCart() {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.api.get("/api/cart");
        // 응답 구조가 { cart: { ... } }이면
        this.cart = response.data.cart;
      } catch (error) {
        console.error("Error fetching cart:", error);
        this.error = "장바구니 정보를 불러올 수 없습니다.";
      } finally {
        this.loading = false;
      }
    },

    async addToCart(itemDetails) {
      this.loading = true;
      this.error = null;
      try {
        const response = await api.api.post("/api/cart/add", itemDetails);
        await this.fetchCart();
        return response.data;
      } catch (error) {
        console.error("Error adding item to cart:", error);
        this.error =
            error.response?.data?.error ||
            error.response?.data?.message ||
            "장바구니에 상품을 추가할 수 없습니다.";
        throw this.error;
      } finally {
        this.loading = false;
      }
    },

    async removeCartItem(cartItemId) {
      this.loading = true;
      this.error = null;
      try {
        await api.api.delete(`/api/cart/items/${cartItemId}`);
        await this.fetchCart();
      } catch (error) {
        console.error("Error removing cart item:", error);
        this.error = "장바구니 항목을 삭제할 수 없습니다.";
      } finally {
        this.loading = false;
      }
    },

    async clearCartItems() {
      this.loading = true;
      this.error = null;
      try {
        await api.api.post("/api/cart/clear");
        this.cart = { items: [], totalItems: 0, totalPrice: 0 };
      } catch (error) {
        console.error("Error clearing cart:", error);
        this.error = "장바구니를 비울 수 없습니다.";
      } finally {
        this.loading = false;
      }
    },

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
  },
});
