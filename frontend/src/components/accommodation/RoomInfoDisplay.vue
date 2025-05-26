<template>
  <div class="w-100 px-3 py-3 d-flex flex-column justify-content-between bg-white" style="min-height:198px;">
    <!-- 상단: 숙박 정보 및 상세보기 -->
    <div class="d-flex justify-content-between align-items-start mb-2">
      <div>
        <p class="small fw-semibold text-secondary mb-1">숙박</p>
        <p class="text-muted mb-0" style="font-size:11px;">
          체크인 {{ roomInfo.checkInTime || "15:00" }} ~ 체크아웃 {{ roomInfo.checkOutTime || "11:00" }}
        </p>
      </div>
      <button @click="viewDetail" class="btn btn-link btn-sm p-0 text-secondary" style="font-size:11px;">
        상세보기 <i class="bi bi-chevron-right ms-1"></i>
      </button>
    </div>

    <!-- 중단: 남은 객실, 가격, 정책 -->
    <div class="mb-3">
      <div class="d-flex justify-content-between align-items-end mb-1">
        <p
            v-if="roomInfo.minAvailableCount !== undefined && roomInfo.minAvailableCount > 0"
            class="small fw-semibold text-warning mb-0"
        >
          남은객실 {{ roomInfo.minAvailableCount }}개
        </p>
        <p
            v-else-if="roomInfo.minAvailableCount === 0"
            class="small fw-semibold text-danger mb-0"
        >
          매진
        </p>
        <div v-else style="min-width:60px;"></div>
        <div>
          <span class="fw-bold fs-5 text-dark">{{ formattedPrice }}</span>
          <span v-if="formattedPrice !== '가격문의'" class="ms-1 text-secondary" style="font-size:15px;">원</span>
        </div>
      </div>

      <div class="text-end mb-1">
        <span
            v-if="roomInfo.originalPrice && roomInfo.discountRate && roomInfo.discountRate > 0"
            class="text-decoration-line-through text-muted small me-1"
        >
          {{ formatPrice(roomInfo.originalPrice) }}
        </span>
        <span v-if="roomInfo.discountRate && roomInfo.discountRate > 0" class="fw-bold text-danger small">
          {{ Math.round(roomInfo.discountRate * 100) }}%
        </span>
      </div>

      <div class="text-end mb-1">
        <span class="badge bg-light text-secondary fw-normal" style="font-size:10px;">
          회원 최대할인가
        </span>
        <i class="bi bi-info-circle text-muted ms-1" style="font-size:10px;" title="가격 관련 상세 정보"></i>
      </div>

      <p class="text-end text-muted mt-1 mb-0" style="font-size:10px;">
        {{ roomInfo.cancellationPolicy || "취소 및 환불 불가" }}
        <i class="bi bi-info-circle text-muted ms-1" style="font-size:10px;" title="취소/환불 규정 보기"></i>
      </p>
    </div>

    <!-- 하단: 버튼들 -->
    <div class="d-flex align-items-center justify-content-end gap-2 mt-auto">
      <button
          @click="addToCart"
          class="btn btn-outline-secondary btn-sm d-flex align-items-center justify-content-center"
          aria-label="장바구니 담기"
      >
        <i class="bi bi-cart fs-5"></i>
      </button>
      <button
          @click="bookRoom"
          class="btn btn-primary btn-sm fw-semibold px-3"
      >
        예약하기
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, PropType } from "vue";

interface RoomInfoFromCard {
  roomId: number;
  name?: string;
  packageName?: string;
  defaultCapacity?: number;
  capacity?: number | string;
  roomFeature?: string;
  bedType?: string;
  price?: number;
  originalPrice?: number;
  discountRate?: number;
  stock?: number;
  minAvailableCount?: number;
  cancellationPolicy?: string;
  checkInTime?: string;
  checkOutTime?: string;
  mainImageUrl?: string;
  imageUrls?: string[];
}

const props = defineProps({
  roomInfo: {
    type: Object as PropType<RoomInfoFromCard>,
    required: true,
  },
  isBookable: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(["view-detail", "book-room", "add-to-cart"]);

const formattedPrice = computed(() => {
  if (props.roomInfo.price === undefined || props.roomInfo.price === null || isNaN(props.roomInfo.price)) {
    return "가격문의";
  }
  return new Intl.NumberFormat("ko-KR").format(props.roomInfo.price);
});

const formatPrice = (value: number | undefined) => {
  if (value === undefined || value === null || isNaN(value)) {
    return "";
  }
  return new Intl.NumberFormat("ko-KR").format(value);
};

const viewDetail = () => {
  emit("view-detail", props.roomInfo.roomId);
};

const bookRoom = () => {
  if (!props.isBookable) return;
  emit("book-room", props.roomInfo.roomId);
};

const addToCart = () => {
  if (!props.isBookable) return;
  emit("add-to-cart", props.roomInfo);
  console.log("Add to cart clicked for room:", props.roomInfo.roomId);
};
</script>

<style scoped>
/* Bootstrap에서 지원하지 않는 아주 작은 폰트 크기만 약간 보정 */
.badge, .text-end span, .text-end i {
  vertical-align: middle;
}
</style>
