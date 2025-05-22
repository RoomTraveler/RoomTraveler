<template>
  <header class="yanolja-header sticky-top bg-white shadow-sm">
    <div class="top-header border-bottom">
      <div class="container px-0">
        <div class="d-flex align-items-center justify-content-between position-relative" style="height:48px; min-height:48px;">
          <!-- 왼쪽: 뒤로가기 -->
          <div class="flex-shrink-0 d-flex align-items-center" style="width: 48px;">
            <button @click="goBack" class="btn btn-link p-0 fs-3 text-danger" title="뒤로가기">
              <i class="bi bi-arrow-left-short"></i>
            </button>
          </div>
          <!-- 중앙: 타이틀 -->
          <div class="flex-grow-1 text-center overflow-hidden px-1">
            <span v-if="showStaticTitle" class="fw-bold text-danger header-title d-block text-truncate">
              {{ staticHeaderTitle }}
            </span>
            <span v-else class="fw-bold header-title d-block text-truncate">
              {{ currentAccommodationLabel }}
            </span>
          </div>
          <!-- 오른쪽: 홈, 장바구니 -->
          <div class="flex-shrink-0 d-flex align-items-center justify-content-end gap-1" style="width: 70px;">
            <button @click="goToHome" class="btn btn-link p-0 fs-5 text-secondary" title="홈">
              <i class="bi bi-house"></i>
            </button>
            <button @click="goToCart" class="btn btn-link position-relative p-0 fs-5 text-secondary" title="장바구니">
              <i class="bi bi-cart3"></i>
              <span v-if="cartItemCount > 0" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger" style="font-size:11px;">
                {{ cartItemCount }}
                <span class="visually-hidden">unread messages</span>
              </span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from "vue-router";
import { ref, computed, watch, PropType } from "vue";
import { useCartStore } from "@/store/cartStore";
import { storeToRefs } from "pinia";
import { useUserStore } from "@/store/userStore";

interface AccommodationType {
  label: string;
  value: string | null;
}

// Bootstrap엔 typeahead 등 컴포넌트가 있지만 여기선 단순 label만 출력!
const accommodationTypes: AccommodationType[] = [
  { label: '전체', value: null },
  { label: '모텔', value: 'MOTEL' },
  { label: '호텔/리조트', value: 'HOTEL' },
  { label: '펜션/풀빌라', value: 'PENSION' },
  { label: '프리미엄', value: 'PREMIUM' },
  { label: '글램핑/캠핑', value: 'CAMPING' },
];

const props = defineProps({
  selectedAccommodationType: {
    type: String as PropType<string | null>,
    default: null,
  },
  accommodationTitle: { // 숙소 상세 페이지용 타이틀 prop
    type: String,
    default: '',
  }
});

const router = useRouter();
const route = useRoute();

// Pinia store: cart, user
const cartStore = useCartStore();
const { cart } = storeToRefs(cartStore);
const { fetchCart } = cartStore;
const userStore = useUserStore();
const { isAuthenticated } = storeToRefs(userStore);

// 로그인 시 장바구니 동기화
watch(isAuthenticated, (isUserLoggedIn) => {
  if (isUserLoggedIn) fetchCart();
  // else cartStore.resetCart && cartStore.resetCart();
}, { immediate: true });

const cartItemCount = computed(() => cart.value ? cart.value.totalItems : 0);

const currentAccommodationLabel = computed(() => {
  if (!props.selectedAccommodationType) {
    return accommodationTypes.find(t => t.value === null)?.label || '숙소';
  }
  return accommodationTypes.find(t => t.value === props.selectedAccommodationType)?.label || '숙소';
});

// AccommodationDetail, RoomDetail 라우트에서 고정 타이틀
const showStaticTitle = computed(() =>
    route.name === 'AccommodationDetail' || route.name === 'RoomDetail'
);
const staticHeaderTitle = computed(() => {
  if (route.name === 'AccommodationDetail' && props.accommodationTitle) {
    return props.accommodationTitle;
  }
  if (route.name === 'RoomDetail') {
    return "객실 상세";
  }
  return '';
});

function goBack() {
  if (
      window.history.length > 1 &&
      document.referrer &&
      new URL(document.referrer).hostname === window.location.hostname
  ) {
    router.back();
  } else {
    router.push({ name: "Home" });
  }
}
function goToHome() {
  router.push({ name: "Home" });
}
function goToCart() {
  router.push({ name: "Cart" });
}
</script>

<style scoped>
.header-title {
  font-size: 16px;
  max-width: calc(100vw - 170px);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
@media (max-width: 768px) {
  .header-title {
    font-size: 14px;
    max-width: calc(100vw - 120px);
  }
  .top-header > .container {
    padding-left: 0 !important;
    padding-right: 0 !important;
  }
}
</style>
