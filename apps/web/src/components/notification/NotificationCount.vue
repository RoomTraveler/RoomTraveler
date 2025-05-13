<template>
  <span v-if="unreadCount > 0" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
    {{ unreadCount > 99 ? '99+' : unreadCount }}
    <span class="visually-hidden">읽지 않은 알림</span>
  </span>
</template>

<script>
/**
 * 알림 카운트 컴포넌트
 * 
 * 이 컴포넌트는 사용자의 읽지 않은 알림 개수를 표시합니다.
 * 주로 헤더의 알림 아이콘 옆에 배지 형태로 표시됩니다.
 */
export default {
  name: 'NotificationCount',
  props: {
    /**
     * 읽지 않은 알림 개수
     */
    unreadCount: {
      type: Number,
      default: 0
    }
  }
};
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