<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">알림 목록</h2>

      <!-- 알림 메시지 -->
      <div v-if="storeMessage" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ storeMessage }}
        <button type="button" class="btn-close" @click="clearMessage" aria-label="Close"></button>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="storeError" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ storeError }}
        <button type="button" class="btn-close" @click="clearError" aria-label="Close"></button>
      </div>

      <!-- 필터 및 정렬 -->
      <div class="card mb-4">
        <div class="card-body">
          <form class="row g-3" @submit.prevent>
            <div class="col-md-4">
              <label for="filterType" class="form-label">알림 유형</label>
              <select class="form-select" id="filterType" v-model="filter.type" @change="applyFilterAndLoad">
                <option value="">전체</option>
                <option value="RESERVATION">예약</option>
                <option value="REVIEW">리뷰</option>
                <option value="PAYMENT">결제</option>
                <option value="SYSTEM">시스템</option>
              </select>
            </div>
            <div class="col-md-4">
              <label for="filterStatus" class="form-label">읽음 상태</label>
              <select class="form-select" id="filterStatus" v-model="filter.readStatus" @change="applyFilterAndLoad">
                <option value="">전체</option>
                <option value="READ">읽음</option>
                <option value="UNREAD">읽지 않음</option>
              </select>
            </div>
            <div class="col-md-4">
              <label for="sortBy" class="form-label">정렬</label>
              <select class="form-select" id="sortBy" v-model="filter.sortBy" @change="applyFilterAndLoad">
                <option value="createdAt">최신순</option>
                <option value="type">유형별</option>
              </select>
            </div>
          </form>
        </div>
      </div>

      <!-- 알림 관리 버튼 -->
      <div class="d-flex justify-content-between align-items-center mb-3">
        <button
            class="btn btn-outline-primary me-2"
            @click="handleMarkAllAsRead"
            :disabled="unreadCount === 0 || loading"
        >
          <i class="bi bi-check-all me-1"></i> 모두 읽음으로 표시
        </button>
        <button
            class="btn btn-outline-danger"
            @click="confirmDeleteAllNotifications"
            :disabled="notifications.length === 0 || loading"
        >
          <i class="bi bi-trash me-1"></i> 모든 알림 삭제
        </button>
      </div>

      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">알림을 불러오는 중입니다...</p>
      </div>

      <!-- 알림 목록 -->
      <div v-else>
        <div v-if="notifications.length === 0" class="alert alert-info">알림이 없습니다.</div>

        <div v-else>
          <div
              v-for="notification in notifications"
              :key="notification.notificationId"
              :class="['card mb-3 shadow-sm cursor-pointer', notification.read ? '' : 'border-primary border-2']"
              @click="handleMarkAsRead(notification)"
              style="transition: box-shadow 0.3s;"
          >
            <div class="card-body d-flex align-items-center p-3">
              <div class="me-3 d-flex align-items-center justify-content-center" style="width:44px; height:44px;">
                <i :class="[getNotificationIcon(notification.type), notification.read ? 'text-secondary' : 'text-primary']" style="font-size:1.5rem;"></i>
              </div>
              <div class="flex-grow-1">
                <div class="fw-bold">
                  {{ notification.title }}
                  <span v-if="!notification.read" class="badge bg-danger ms-2 align-middle">NEW</span>
                </div>
                <div class="small text-muted mb-1">{{ notification.message }}</div>
                <div class="d-flex gap-2 align-items-center small">
                  <span class="text-secondary">{{ formatDate(notification.createdAt) }}</span>
                  <span :class="['badge', getNotificationTypeClass(notification.type)]">{{ getNotificationTypeName(notification.type) }}</span>
                </div>
              </div>
              <button
                  class="btn btn-sm btn-link text-danger ms-2"
                  @click.stop="confirmDeleteNotification(notification.notificationId)"
                  title="알림 삭제"
                  :disabled="loading"
              >
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>

          <!-- 페이지네이션 -->
          <nav v-if="totalPages > 1" aria-label="Page navigation" class="mt-4">
            <ul class="pagination justify-content-center">
              <li class="page-item" :class="{ disabled: currentPage === 1 || loading }">
                <a class="page-link" href="#" @click.prevent="goToPage(currentPage - 1)">이전</a>
              </li>
              <li
                  v-for="page in paginationItems"
                  :key="page"
                  class="page-item"
                  :class="{ active: page === currentPage, disabled: page === '...' || loading }"
              >
                <a class="page-link" href="#" @click.prevent="page !== '...' && goToPage(page)">{{ page }}</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages || loading }">
                <a class="page-link" href="#" @click.prevent="goToPage(currentPage + 1)">다음</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
/**
 * 알림 목록 컴포넌트 (Bootstrap 기반, Pinia 사용)
 */
import { ref, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { storeToRefs } from "pinia";
import { useUserStore } from "@/store/userStore";
import { useNotificationStore } from "@/store/notificationStore";
import Layout from "@/components/layout/Layout.vue";

// 스토어 및 라우터
const userStore = useUserStore();
const notificationStore = useNotificationStore();
const router = useRouter();
const route = useRoute();

// 스토어에서 필요한 상태 가져오기
const {
  notifications,
  unreadCount,
  totalItems,
  totalPages,
  currentPage,
  loading,
  error: storeError,
  message: storeMessage,
} = storeToRefs(notificationStore);

const {
  fetchNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  deleteAllNotifications,
  clearMessage,
  clearError,
} = notificationStore;

const { isAuthenticated: isLoggedIn } = storeToRefs(userStore);

// 필터
const filter = ref({
  type: "",
  readStatus: "",
  sortBy: "createdAt",
  page: 1,
  size: 10,
});

// 마운트 시 알림 로드
onMounted(() => {
  if (!isLoggedIn.value) {
    router.push("/user/login");
    return;
  }

  // URL 쿼리 파라미터 반영
  const query = route.query;
  if (query.type) filter.value.type = query.type;
  if (query.readStatus) filter.value.readStatus = query.readStatus;
  if (query.sortBy) filter.value.sortBy = query.sortBy;
  if (query.page) filter.value.page = parseInt(query.page);
  if (query.size) filter.value.size = parseInt(query.size);

  loadInitialNotifications();
});

// 필터 변경시
const applyFilterAndLoad = () => {
  filter.value.page = 1;
  loadInitialNotifications();
};

const loadInitialNotifications = async () => {
  updateQueryParams();
  try {
    await fetchNotifications({ ...filter.value });
  } catch (err) {
    // 이미 스토어에서 처리
  }
};

// 페이지 이동
const goToPage = (pageNumber) => {
  if (pageNumber < 1 || pageNumber > totalPages.value || loading.value) return;
  filter.value.page = pageNumber;
  loadInitialNotifications();
};

// URL 쿼리 동기화
const updateQueryParams = () => {
  const query = {};
  if (filter.value.type) query.type = filter.value.type;
  if (filter.value.readStatus) query.readStatus = filter.value.readStatus;
  if (filter.value.sortBy !== "createdAt") query.sortBy = filter.value.sortBy;
  if (filter.value.page > 1) query.page = filter.value.page;
  if (filter.value.size !== 10) query.size = filter.value.size;
  if (JSON.stringify(route.query) !== JSON.stringify(query)) {
    router.push({ query });
  }
};

// 라우트 쿼리 감시
watch(
    () => route.query,
    (newQuery) => {
      if (newQuery.page && parseInt(newQuery.page) !== filter.value.page) filter.value.page = parseInt(newQuery.page);
      if (newQuery.type !== undefined && newQuery.type !== filter.value.type) filter.value.type = newQuery.type;
      if (newQuery.readStatus !== undefined && newQuery.readStatus !== filter.value.readStatus) filter.value.readStatus = newQuery.readStatus;
      if (newQuery.sortBy && newQuery.sortBy !== filter.value.sortBy) filter.value.sortBy = newQuery.sortBy;
    },
    { deep: true }
);

// 알림 읽음 처리
const handleMarkAsRead = async (notification) => {
  if (loading.value) return;
  try {
    const success = await markNotificationAsRead(notification.notificationId);
    if (success && notification.link) {
      router.push(notification.link);
    }
  } catch (err) {}
};

// 모두 읽음 처리
const handleMarkAllAsRead = async () => {
  if (loading.value || unreadCount.value === 0) return;
  try {
    await markAllNotificationsAsRead();
  } catch (err) {}
};

// 개별 알림 삭제
const confirmDeleteNotification = (notificationId) => {
  if (loading.value) return;
  if (confirm("이 알림을 삭제하시겠습니까?")) {
    handleDeleteNotification(notificationId);
  }
};
const handleDeleteNotification = async (notificationId) => {
  if (loading.value) return;
  try {
    await deleteNotification(notificationId);
    if (notifications.value.length === 0 && currentPage.value > 1) {
      goToPage(currentPage.value - 1);
    } else if (notifications.value.length > 0 && filter.value.page > totalPages.value) {
      goToPage(totalPages.value || 1);
    }
  } catch (err) {}
};

// 전체 알림 삭제
const confirmDeleteAllNotifications = () => {
  if (loading.value || notifications.value.length === 0) return;
  if (confirm("모든 알림을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
    handleDeleteAllNotifications();
  }
};
const handleDeleteAllNotifications = async () => {
  if (loading.value) return;
  try {
    await deleteAllNotifications();
    filter.value.page = 1;
    updateQueryParams();
  } catch (err) {}
};

// 페이지네이션 표시 계산
const paginationItems = computed(() => {
  const items = [];
  const maxVisiblePages = 5;
  if (totalPages.value <= 1) return [];
  if (totalPages.value <= maxVisiblePages + 2) {
    for (let i = 1; i <= totalPages.value; i++) items.push(i);
  } else {
    items.push(1);
    let startPage = Math.max(2, currentPage.value - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages.value - 1, currentPage.value + Math.floor(maxVisiblePages / 2));
    if (currentPage.value - 1 < Math.floor(maxVisiblePages / 2) + 1) endPage = Math.min(totalPages.value - 1, maxVisiblePages + 1);
    if (totalPages.value - currentPage.value < Math.floor(maxVisiblePages / 2) + 1) startPage = Math.max(2, totalPages.value - maxVisiblePages);
    if (startPage > 2) items.push("...");
    for (let i = startPage; i <= endPage; i++) items.push(i);
    if (endPage < totalPages.value - 1) items.push("...");
    items.push(totalPages.value);
  }
  return items;
});

// 유틸리티 함수들
const getNotificationIcon = (type) => {
  const icons = {
    RESERVATION: "bi bi-calendar-check",
    REVIEW: "bi bi-star",
    PAYMENT: "bi bi-credit-card",
    SYSTEM: "bi bi-gear",
    MESSAGE: "bi bi-chat-dots",
  };
  return icons[type] || "bi bi-bell";
};
const getNotificationTypeName = (type) => {
  const types = {
    RESERVATION: "예약",
    REVIEW: "리뷰",
    PAYMENT: "결제",
    SYSTEM: "시스템",
    MESSAGE: "메시지",
  };
  return types[type] || type;
};
const getNotificationTypeClass = (type) => {
  const classes = {
    RESERVATION: "bg-info text-dark",
    REVIEW: "bg-warning text-dark",
    PAYMENT: "bg-success text-white",
    SYSTEM: "bg-danger text-white",
    MESSAGE: "bg-secondary text-white",
  };
  return classes[type] || "bg-light text-dark";
};
const formatDate = (dateString) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = now - date;
  const diffSec = Math.floor(diffMs / 1000);
  const diffMin = Math.floor(diffSec / 60);
  const diffHour = Math.floor(diffMin / 60);
  const diffDay = Math.floor(diffHour / 24);

  if (diffDay < 1) {
    if (diffHour < 1) {
      if (diffMin < 1) return "방금 전";
      return `${diffMin}분 전`;
    }
    return `${diffHour}시간 전`;
  }
  if (diffDay < 7) {
    return `${diffDay}일 전`;
  }
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};
</script>

<style scoped>
.cursor-pointer {
  cursor: pointer;
}
</style>
