<template>
  <div class="text-left">
    <span v-if="item.hotelGrade" class="block text-xs text-gray-500 mb-0.5">{{ item.hotelGrade }}</span>
    <h3 class="font-semibold text-lg text-gray-800 leading-tight mb-1">{{ item.title }}</h3>
    <div class="flex items-center text-sm text-gray-600 mb-1">
      <i class="bi bi-geo-alt-fill mr-1.5 text-xs"></i>
      <span>{{ item.address }}</span>
    </div>
    <div class="flex items-center text-sm text-gray-600 mb-2.5">
      <i class="bi bi-star-fill text-yellow-400 mr-1 text-sm"></i>
      <span class="font-bold text-gray-800">{{ (item.rating ?? 0).toFixed(1) }}</span>
      <span class="ml-1 text-gray-500">({{ (item.reviewCount ?? 0).toLocaleString() }})</span>
    </div>
    <div v-if="item.couponInfo" class="mb-2">
      <span class="text-xs text-blue-600 bg-blue-100 px-2 py-1 rounded font-medium">{{ item.couponInfo }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { PropType } from 'vue';

// 이 인터페이스는 AccommodationList.vue의 것과 동기화되어야 합니다.
// 간결함을 위해 주요 필드만 포함합니다.
interface AccommodationForCardInfo {
  hotelGrade?: string;
  title: string;
  address: string;
  rating?: number;
  reviewCount?: number;
  couponInfo?: string;
  // 여기에 필요한 다른 필드들을 추가할 수 있습니다.
}

defineProps({
  item: {
    type: Object as PropType<AccommodationForCardInfo>,
    required: true,
  },
});
</script>

<style scoped>
/* 필요한 경우 여기에 커스텀 스타일 추가 */
</style> 