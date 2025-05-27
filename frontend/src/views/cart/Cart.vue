<template>
  <Layout>
    <div class="container my-5">
      <h2 class="mb-4">장바구니</h2>

      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="componentError" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ componentError }}
        <button type="button" class="btn-close" @click="componentError = ''" aria-label="Close"></button>
      </div>

      <!-- 장바구니가 비었을 때 -->
      <div v-if="!cart.items || cart.items.length === 0" class="text-center py-5">
        <i class="bi bi-cart display-1 text-secondary mb-3"></i>
        <h3 class="mb-3">장바구니가 비어있습니다</h3>
        <p>객실을 장바구니에 추가해보세요!</p>
        <router-link to="/accommodation/list" class="btn btn-primary mt-2">객실 둘러보기</router-link>
      </div>

      <!-- 장바구니에 항목이 있을 때 -->
      <div v-else class="row g-4">
        <div class="col-lg-8">
          <div v-for="item in cart.items" :key="item.cartItemId" class="card mb-4 shadow-sm">
            <div class="row g-0">
              <div class="col-md-4">
                <img
                  :src="item.imageUrl || noImagePlaceholder"
                  :alt="item.roomName"
                  class="img-fluid rounded-start w-100 h-100 object-fit-cover"
                  style="max-height: 200px"
                />
              </div>
              <div class="col-md-8">
                <div class="card-body d-flex flex-column h-100">
                  <h4 class="card-title mb-2">{{ item.accommodationTitle }}</h4>
                  <h5 class="card-subtitle mb-2 text-muted">{{ item.roomName }}</h5>
                  <ul class="list-unstyled mb-3 small">
                    <li><strong>체크인:</strong> {{ item.checkInDate }}</li>
                    <li><strong>체크아웃:</strong> {{ item.checkOutDate }}</li>
                    <li><strong>숙박일수:</strong> {{ item.nights }}박</li>
                    <li><strong>인원:</strong> {{ item.guestCount }}명</li>
                    <li><strong>가격:</strong> {{ formatPrice(item.price) }}</li>
                  </ul>
                  <div class="mt-auto text-end">
                    <button @click="removeItemHandler(item.cartItemId)" class="btn btn-outline-danger btn-sm">
                      삭제
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="col-lg-4">
          <div class="card p-4 sticky-top" style="top: 80px">
            <h4 class="card-title mb-3">주문 요약</h4>
            <div class="mb-2">
              <span><strong>총 객실 수:</strong> {{ cart.totalItems }}개</span>
            </div>
            <div class="mb-3">
              <span><strong>총 가격:</strong> {{ formatPrice(cart.totalPrice) }}</span>
            </div>
            <button @click="checkoutHandler" class="btn btn-primary w-100 mb-2" :disabled="cart.items.length === 0">
              예약하기
            </button>
            <button
              @click="clearCartHandler"
              class="btn btn-outline-secondary w-100"
              :disabled="cart.items.length === 0"
            >
              장바구니 비우기
            </button>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 장바구니 컴포넌트 (Vue3, Bootstrap)
 * - 장바구니 목록/삭제/비우기/예약 기능 제공
 */
import Layout from "@/components/layout/Layout.vue";
import { useUserStore } from "@/store/userStore";
import { useCartStore } from "@/store/cartStore";
import { computed, ref, onMounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import { storeToRefs } from "pinia";
import noImagePlaceholder from "@/assets/no-image.jpg";

export default {
  name: "Cart",
  components: { Layout },
  setup() {
    const userStore = useUserStore();
    const cartStore = useCartStore();
    const router = useRouter();
    const route = useRoute();

    const isLoggedIn = computed(() => userStore.isAuthenticated);
    const { cart } = storeToRefs(cartStore);
    const message = ref("");
    const componentError = ref("");

    // 장바구니 데이터 로드
    async function loadCartData() {
      try {
        await cartStore.fetchCart();
        if (cartStore.error) {
          componentError.value = cartStore.error;
        }
      } catch (err) {
        componentError.value = "장바구니 정보를 불러올 수 없습니다. 다시 시도해주세요.";
      }
    }

    // 장바구니 항목 삭제
    async function removeItemHandler(cartItemId) {
      try {
        await cartStore.removeCartItem(cartItemId);
        if (cartStore.error) {
          componentError.value = cartStore.error;
        } else {
          message.value = "장바구니에서 항목이 삭제되었습니다.";
        }
      } catch (err) {
        componentError.value = "장바구니 항목을 삭제할 수 없습니다. 다시 시도해주세요.";
      }
    }

    // 장바구니 전체 비우기
    async function clearCartHandler() {
      if (confirm("장바구니를 비우시겠습니까?")) {
        try {
          await cartStore.clearCartItems();
          if (cartStore.error) {
            componentError.value = cartStore.error;
          } else {
            message.value = "장바구니가 비워졌습니다.";
          }
        } catch (err) {
          componentError.value = "장바구니를 비울 수 없습니다. 다시 시도해주세요.";
        }
      }
    }

    // 수정된 예약하기 핸들러: CartCheckout 페이지로 이동
    function checkoutHandler() {
      if (cart.value && cart.value.items && cart.value.items.length > 0) {
        router.push({ name: "AccommodationCartCheckout" }); // CartCheckout 라우트 이름 수정
      } else {
        componentError.value = "장바구니에 예약할 상품이 없습니다.";
      }
    }

    // 가격 포맷
    function formatPrice(price) {
      return new Intl.NumberFormat("ko-KR", {
        style: "currency",
        currency: "KRW",
        maximumFractionDigits: 0,
      }).format(price);
    }

    onMounted(async () => {
      if (!isLoggedIn.value) {
        router.push("/login");
        return;
      }
      await loadCartData();
      // 쿼리 파라미터 메시지/에러 처리
      if (route.query.message) message.value = route.query.message;
      if (route.query.error) componentError.value = route.query.error;
    });

    return {
      cart,
      message,
      componentError,
      removeItemHandler,
      clearCartHandler,
      checkoutHandler,
      formatPrice,
      noImagePlaceholder,
    };
  },
};
</script>

<style scoped>
/* Bootstrap에서 이미 대부분 처리됨. 추가 디자인만 약간 보강 */

.card {
  border-radius: 0.75rem;
}

.card-title {
  font-weight: 600;
}

.card-subtitle {
  font-size: 1rem;
}

.object-fit-cover {
  object-fit: cover;
}

.sticky-top {
  z-index: 2;
}

ul {
  margin-bottom: 0.5rem;
}
</style>
