<template>
  <div class="text-start">
    <span v-if="item.hotelGrade" class="d-block small text-secondary mb-1">{{ item.hotelGrade }}</span>
    <h3 class="fw-semibold fs-5 text-dark lh-sm mb-2">{{ item.title }}</h3>
    <div class="d-flex align-items-center small text-secondary mb-2">
      <i class="bi bi-geo-alt-fill me-2"></i>
      <span>{{ item.address }}</span>
    </div>
    <div class="d-flex align-items-center small text-secondary mb-3">
      <i class="bi bi-star-fill text-warning me-1"></i>
      <span class="fw-bold text-dark">{{ (item.rating ?? 0).toFixed(1) }}</span>
      <span class="ms-2 text-muted">({{ (item.reviewCount ?? 0).toLocaleString() }})</span>
    </div>
    <div v-if="item.couponInfo" class="mb-2">
      <span class="badge bg-primary bg-opacity-10 text-primary px-2 py-1">{{ item.couponInfo }}</span>
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
/* 필요하면 추가 커스텀 스타일 작성 */
</style>
