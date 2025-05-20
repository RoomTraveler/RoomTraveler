<template>
  <Layout>
    <div class="container my-5">
      <h2 class="mb-4">장바구니</h2>

      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>

      <div v-if="componentError" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ componentError }}
        <button type="button" class="btn-close" @click="componentError = ''" aria-label="Close"></button>
      </div>

      <!-- 장바구니가 비어있을 때 -->
      <div v-if="!cart.items || cart.items.length === 0" class="empty-cart">
        <i class="bi bi-cart"></i>
        <h3>장바구니가 비어있습니다</h3>
        <p>객실을 장바구니에 추가해보세요!</p>
        <router-link to="/accommodation/list" class="btn btn-primary">객실 둘러보기</router-link>
      </div>

      <!-- 장바구니에 항목이 있을 때 -->
      <div v-else class="row">
        <div class="col-md-8">
          <div v-for="item in cart.items" :key="item.cartItemId" class="cart-item">
            <div class="row">
              <div class="col-md-4">
                <img
                  :src="item.imageUrl || require('@/assets/no-image.jpg')"
                  :alt="item.roomName"
                  class="cart-item-image"
                />
              </div>
              <div class="col-md-8">
                <h4>{{ item.accommodationTitle }}</h4>
                <h5>{{ item.roomName }}</h5>
                <p>
                  <strong>체크인:</strong> {{ item.checkInDate }}<br />
                  <strong>체크아웃:</strong> {{ item.checkOutDate }}<br />
                  <strong>숙박일수:</strong> {{ item.nights }}박<br />
                  <strong>인원:</strong> {{ item.guestCount }}명<br />
                  <strong>가격:</strong> {{ formatPrice(item.price) }}
                </p>
                <div class="d-flex justify-content-end">
                  <button @click="removeItem(item.cartItemId)" class="btn btn-outline-danger">삭제</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="cart-summary">
            <h4 class="mb-3">주문 요약</h4>
            <p><strong>총 객실 수:</strong> {{ cart.totalItems }}개</p>
            <p><strong>총 가격:</strong> {{ formatPrice(cart.totalPrice) }}</p>

            <button @click="checkout" class="btn btn-primary w-100 mb-2">예약하기</button>
            <button @click="clearCart" class="btn btn-outline-secondary w-100">장바구니 비우기</button>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 장바구니 컴포넌트
 *
 * 이 컴포넌트는 사용자의 장바구니를 표시하고 관리합니다.
 * 장바구니 항목 조회, 삭제, 장바구니 비우기, 예약하기 기능을 제공합니다.
 */
import Layout from "@/components/layout/Layout.vue";
import { useUserStore } from "@/store/userStore";
import { useCartStore } from "@/store/cartStore"; // Pinia cart 스토어 가져오기
import { computed, ref, onMounted } from "vue"; // ref, onMounted 추가
import { useRouter, useRoute } from "vue-router"; // useRouter, useRoute 추가
import { storeToRefs } from "pinia"; // storeToRefs 추가

export default {
  name: "Cart",
  components: {
    Layout,
  },
  setup() {
    const userStore = useUserStore();
    const cartStore = useCartStore();
    const router = useRouter();
    const route = useRoute();

    const isLoggedIn = computed(() => userStore.isAuthenticated);
    const { cart, loading, error: cartError } = storeToRefs(cartStore);
    const { fetchCart, removeCartItem, clearCartItems, checkoutCart } = cartStore;
    const message = ref("");
    const componentError = ref("");

    onMounted(async () => {
      // 로깅 추가
      console.log("[Cart.vue] onMounted - START");
      console.log("[Cart.vue] onMounted - initial userStore.user:", JSON.parse(JSON.stringify(userStore.user))); // 스토어 상태 직접 확인 (반응형 객체 복사)
      console.log("[Cart.vue] onMounted - initial isLoggedIn.value:", isLoggedIn.value);

      if (!isLoggedIn.value) {
        // 이 시점에서 isLoggedIn.value가 false로 평가되는지 확인
        console.warn("[Cart.vue] onMounted - User not logged in according to isLoggedIn.value. Redirecting to /login.");
        router.push("/login");
        return;
      }

      console.log("[Cart.vue] onMounted - User is logged in. Proceeding to load cart data.");
      await loadCartData();

      if (route.query.message) {
        message.value = route.query.message;
      }
      if (route.query.error) {
        componentError.value = route.query.error;
      }
      console.log("[Cart.vue] onMounted - END");
    });

    async function loadCartData() {
      console.log("[Cart.vue] loadCartData - START");
      try {
        await fetchCart();
        if (cartStore.error) {
          componentError.value = cartStore.error;
          console.warn("[Cart.vue] loadCartData - Error from cartStore after fetchCart:", cartStore.error);
        }
      } catch (err) {
        console.error("[Cart.vue] loadCartData - CATCH error:", err);
        componentError.value = "장바구니 정보를 불러올 수 없습니다. 다시 시도해주세요.";
      }
      console.log("[Cart.vue] loadCartData - END");
    }

    async function removeItem(cartItemId) {
      console.log("[Cart.vue] removeItem - START", { cartItemId });
      try {
        await removeCartItem(cartItemId);
        if (cartStore.error) {
          componentError.value = cartStore.error;
          console.warn("[Cart.vue] removeItem - Error from cartStore after removeCartItem:", cartStore.error);
        } else {
          message.value = "장바구니에서 항목이 삭제되었습니다.";
          console.log("[Cart.vue] removeItem - Success");
        }
      } catch (err) {
        console.error("[Cart.vue] removeItem - CATCH error:", err);
        componentError.value = "장바구니 항목을 삭제할 수 없습니다. 다시 시도해주세요.";
      }
      console.log("[Cart.vue] removeItem - END");
    }

    async function clearCartHandler() {
      console.log("[Cart.vue] clearCartHandler - START");
      if (confirm("장바구니를 비우시겠습니까?")) {
        try {
          await clearCartItems();
          if (cartStore.error) {
            componentError.value = cartStore.error;
            console.warn("[Cart.vue] clearCartHandler - Error from cartStore after clearCartItems:", cartStore.error);
          } else {
            message.value = "장바구니가 비워졌습니다.";
            console.log("[Cart.vue] clearCartHandler - Success");
          }
        } catch (err) {
          console.error("[Cart.vue] clearCartHandler - CATCH error:", err);
          componentError.value = "장바구니를 비울 수 없습니다. 다시 시도해주세요.";
        }
      }
      console.log("[Cart.vue] clearCartHandler - END");
    }

    async function checkoutHandler() {
      console.log("[Cart.vue] checkoutHandler - START");
      try {
        // payload 객체 생성 (예: 특별 요청 사항)
        // const payload = { specialRequests: "창가 자리로 부탁드립니다." }; 
        // 현재는 특별한 payload가 없으므로 빈 객체 또는 필요한 데이터를 담아 전달합니다.
        const response = await checkoutCart({ specialRequests: '' }); // 빈 payload 또는 실제 데이터 전달
        if (cartStore.error) {
          componentError.value = cartStore.error;
          console.warn("[Cart.vue] checkoutHandler - Error from cartStore after checkoutCart:", cartStore.error);
        } else {
          // 예약 성공 후 처리. 예를 들어 예약 완료 페이지로 이동하거나 메시지 표시.
          // 백엔드 응답(response)에 따라 다른 처리가 가능합니다.
          // 예시: router.push({ name: 'ReservationComplete', params: { reservationIds: response.reservationIds } });
          message.value = response.message || "장바구니의 항목들이 예약되었습니다.";
          console.log("[Cart.vue] checkoutHandler - Success, reservation data:", response);
          // router.push("/reservation/cart-checkout"); // 이 경로는 예약 정보 확인 페이지일 수 있으므로, 실제 경로로 수정 필요
          // 예약 ID 목록을 다음 페이지로 전달하거나, 사용자 예약 목록 페이지로 이동할 수 있습니다.
          router.push({ name: 'MyReservations' }); // 예시: 내 예약 목록 페이지로 이동
        }
      } catch (err) {
        console.error("[Cart.vue] checkoutHandler - CATCH error:", err);
        // componentError.value는 cartStore.error에 의해 이미 설정되었을 수 있습니다.
        if (!componentError.value) {
            componentError.value = "예약 처리에 실패했습니다. 다시 시도해주세요.";
        }
      }
      console.log("[Cart.vue] checkoutHandler - END");
    }

    function formatPrice(price) {
      return new Intl.NumberFormat("ko-KR", {
        style: "currency",
        currency: "KRW",
        maximumFractionDigits: 0,
      }).format(price);
    }

    return {
      isLoggedIn,
      cart,
      loading,
      cartError,
      message,
      componentError,
      loadCartData,
      removeItem,
      clearCartHandler,
      checkoutHandler,
      formatPrice,
    };
  },
};
</script>

<style scoped>
/* 장바구니 스타일 */
.cart-item {
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
  padding: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.cart-item-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.cart-summary {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  position: sticky;
  top: 20px;
}

.empty-cart {
  text-align: center;
  padding: 50px 0;
}

.empty-cart i {
  font-size: 48px;
  color: #ccc;
  margin-bottom: 20px;
}
</style>
