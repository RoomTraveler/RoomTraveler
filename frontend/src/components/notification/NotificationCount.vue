<template>
  <span
      v-if="unreadCount > 0"
      class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
      role="status"
  >
    {{ badgeText }}
    <span class="visually-hidden">읽지 않은 알림</span>
  </span>
</template>

<script setup>
// props 정의
/**
 * 읽지 않은 알림 개수를 받아와서 배지로 표시합니다.
 * @type {{ unreadCount: number }}
 */
const props = defineProps({
  unreadCount: {
    type: Number,
    default: 0
  }
});

// 표시할 텍스트 계산
const badgeText = computed(() => {
  return props.unreadCount > 99 ? '99+' : String(props.unreadCount);
});
</script>

<style scoped>
.badge {
  font-size: 0.65rem;
  font-weight: 600;
  padding: 0.25em 0.6em;
}

/* 애니메이션 효과 */
@keyframes pulse {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
  }
}

.badge.bg-danger {
  animation: pulse 2s infinite;
}
</style>
