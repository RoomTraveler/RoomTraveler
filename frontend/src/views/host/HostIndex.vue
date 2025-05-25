<template>
  <div class="host-layout container-fluid mt-3">
    <el-tabs v-model="activeTab" type="border-card" @tab-click="handleTabClick">
      <el-tab-pane label="대시보드" name="HostDashboard">
        <template #label>
          <span><i class="bi bi-speedometer2"></i> 대시보드</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="숙소 관리" name="HostAccommodations">
        <template #label>
          <span><i class="bi bi-house-gear-fill"></i> 숙소 관리</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="예약 관리" name="HostReservations">
        <template #label>
          <span><i class="bi bi-calendar-check"></i> 예약 관리</span>
        </template>
      </el-tab-pane>
      <el-tab-pane label="리뷰 관리" name="HostReviews">
        <template #label>
          <span><i class="bi bi-chat-left-text"></i> 리뷰 관리</span>
        </template>
      </el-tab-pane>
      <!-- 필요시 추가 탭 (예: 정산, 프로필 수정 등) -->
    </el-tabs>

    <div class="host-content mt-4 p-3 bg-light border rounded">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElTabs, ElTabPane } from 'element-plus';

const router = useRouter();
const route = useRoute();
const activeTab = ref('HostDashboard'); // 기본 활성 탭

// 현재 라우트에 따라 activeTab 설정
watch(
  () => route.name,
  (routeName) => {
    if (routeName && typeof routeName === 'string' && 
        (routeName.startsWith('HostDashboard') || 
         routeName.startsWith('HostAccommodations') || 
         routeName.startsWith('HostAccommodation') || // New, Edit, Detail 포함
         routeName.startsWith('HostReservations') ||
         routeName.startsWith('HostReviews'))) {
      
      if (routeName.includes('Accommodation')) {
        activeTab.value = 'HostAccommodations';
      } else if (routeName.includes('Dashboard')) {
        activeTab.value = 'HostDashboard';
      } else if (routeName.includes('Reservations')) {
        activeTab.value = 'HostReservations';
      } else if (routeName.includes('Reviews')) {
        activeTab.value = 'HostReviews';
      } else {
         activeTab.value = 'HostDashboard';
      }
    }
  },
  { immediate: true }
);

const handleTabClick = (tab) => {
  if (tab.props.name) {
    router.push({ name: tab.props.name });
  }
};
</script>

<style scoped>
.host-layout {
  max-width: 1400px; /* 전체 레이아웃 너비 제한 */
  margin-left: auto;
  margin-right: auto;
}

.host-content {
  min-height: 500px; /* 컨텐츠 영역 최소 높이 */
}

/* 탭 아이콘과 텍스트 정렬 */
.el-tabs__item .bi {
  margin-right: 6px;
  vertical-align: middle;
}
.el-tabs__item span {
  vertical-align: middle;
}

/* 트랜지션 효과 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 