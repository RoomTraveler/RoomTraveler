<template>
  <div class="w-full px-4 py-3 flex flex-col justify-between bg-white" style="min-height: 198px">
    <!-- 상단: 숙박 정보 및 상세보기 -->
    <div class="flex justify-between items-start mb-2">
      <div>
        <p class="text-xs font-semibold text-gray-700">숙박</p>
        <p class="text-xxs text-gray-500">
          체크인 {{ roomInfo.checkInTime || "15:00" }} ~ 체크아웃 {{ roomInfo.checkOutTime || "11:00" }}
        </p>
      </div>
      <button @click="viewDetail" class="text-xxs text-gray-400 hover:text-gray-600 flex items-center">
        상세보기 <i class="bi bi-chevron-right text-xxs ml-0.5"></i>
      </button>
    </div>

    <!-- 중단: 남은 객실, 가격, 정책 -->
    <div class="mb-3">
      <div class="flex justify-between items-end mb-0.5">
        <p
          v-if="roomInfo.minAvailableCount !== undefined && roomInfo.minAvailableCount > 0"
          class="text-xs font-semibold text-orange-600"
        >
          남은객실 {{ roomInfo.minAvailableCount }}개
        </p>
        <p v-else-if="roomInfo.minAvailableCount === 0" class="text-xs font-semibold text-red-600">매진</p>
        <div v-else></div>
        <!-- 남은 객실 정보 없을 때 공간 유지용 -->

        <div>
          <span class="text-xl font-bold text-gray-800">{{ formattedPrice }}</span>
          <span v-if="formattedPrice !== '가격문의'" class="text-sm text-gray-800 ml-0.5">원</span>
        </div>
      </div>

      <div class="text-right mb-1">
        <span
          v-if="roomInfo.originalPrice && roomInfo.discountRate && roomInfo.discountRate > 0"
          class="text-xs text-gray-400 line-through mr-1"
        >
          {{ formatPrice(roomInfo.originalPrice) }}
        </span>
        <span v-if="roomInfo.discountRate && roomInfo.discountRate > 0" class="text-sm font-bold text-red-500">
          {{ Math.round(roomInfo.discountRate * 100) }}%
        </span>
      </div>

      <div class="text-right mb-1">
        <span class="text-xxs bg-gray-100 text-gray-600 px-1 py-0.5 rounded-sm font-medium">회원 최대할인가</span>
        <i class="bi bi-info-circle text-gray-400 text-xxs ml-0.5 cursor-pointer" title="가격 관련 상세 정보"></i>
      </div>

      <p class="text-xxs text-gray-500 mt-1 text-right">
        {{ roomInfo.cancellationPolicy || "취소 및 환불 불가" }}
        <i class="bi bi-info-circle text-gray-400 text-xxs ml-0.5 cursor-pointer" title="취소/환불 규정 보기"></i>
      </p>
    </div>

    <!-- 하단: 버튼들 -->
    <div class="flex items-center justify-end gap-x-1.5 mt-auto">
      <button
        @click="addToCart"
        class="bg-white border border-gray-300 text-gray-500 hover:bg-gray-50 rounded-sm flex items-center justify-center p-2"
        aria-label="장바구니 담기"
      >
        <i class="bi bi-cart text-xl"></i>
      </button>
      <button
        @click="bookRoom"
        class="bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-sm text-sm py-2 px-3"
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
  // RoomCard에서 전달되는 모든 Room 속성 + minAvailableCount
  packageName?: string;
  defaultCapacity?: number;
  capacity?: number | string;
  roomFeature?: string;
  bedType?: string;
  price?: number;
  originalPrice?: number;
  discountRate?: number;
  stock?: number; // 이 prop은 minAvailableCount로 대체될 수 있음. stock은 총 객실 수 개념에 가까움
  minAvailableCount?: number; // 백엔드에서 받아오는 남은 객실 수
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
.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.text-xxs {
  /* 10px */
  font-size: 0.625rem;
  line-height: 0.875rem;
}
/* Bootstrap Icons CDN은 이미 index.html 또는 main.ts 등에 추가되어 있다고 가정합니다. */
</style>
