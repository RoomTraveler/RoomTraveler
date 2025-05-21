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
          <div class="row g-3">
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
          </div>
        </div>
      </div>

      <!-- 알림 관리 버튼 -->
      <div class="d-flex justify-content-between mb-3">
        <div>
          <button
            class="btn btn-outline-primary me-2"
            @click="handleMarkAllAsRead"
            :disabled="unreadCount === 0 || loading"
          >
            <i class="bi bi-check-all me-1"></i> 모두 읽음으로 표시
          </button>
        </div>
        <div>
          <button
            class="btn btn-outline-danger"
            @click="confirmDeleteAllNotifications"
            :disabled="notifications.length === 0 || loading"
          >
            <i class="bi bi-trash me-1"></i> 모든 알림 삭제
          </button>
        </div>
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
            class="notification-item"
            :class="{ unread: !notification.read }"
            @click="handleMarkAsRead(notification)"
          >
            <div class="notification-icon">
              <i :class="getNotificationIcon(notification.type)"></i>
            </div>
            <div class="notification-content">
              <div class="notification-title">
                {{ notification.title }}
                <span v-if="!notification.read" class="badge bg-danger ms-2">NEW</span>
              </div>
              <div class="notification-message">{{ notification.message }}</div>
              <div class="notification-meta">
                <span class="notification-time">{{ formatDate(notification.createdAt) }}</span>
                <span class="notification-type" :class="getNotificationTypeClass(notification.type)">
                  {{ getNotificationTypeName(notification.type) }}
                </span>
              </div>
            </div>
            <div class="notification-actions">
              <button
                class="btn btn-sm btn-link text-danger"
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
 * 알림 목록 컴포넌트 (Pinia 사용)
 *
 * 이 컴포넌트는 사용자의 알림 목록을 표시합니다.
 * 알림 유형별 필터링, 읽음/읽지 않음 상태 필터링, 정렬 기능을 제공합니다.
 * 알림을 읽음으로 표시하거나 삭제할 수 있습니다.
 */
import { ref, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { storeToRefs } from "pinia";
import { useUserStore } from "@/store/userStore";
import { useNotificationStore } from "@/store/notificationStore";
import Layout from "@/components/layout/Layout.vue";

// 스토어 및 라우터 사용
const userStore = useUserStore();
const notificationStore = useNotificationStore();
const router = useRouter();
const route = useRoute();

// 로그인 상태 (userStore에서 가져옴)
const { isAuthenticated: isLoggedIn } = storeToRefs(userStore);

// 알림 스토어 상태 및 액션
const {
  notifications,
  unreadCount,
  totalItems,
  totalPages,
  currentPage,
  loading,
  error: storeError, // 스토어의 error와 구분하기 위해 이름 변경
  message: storeMessage, // 스토어의 message와 구분하기 위해 이름 변경
} = storeToRefs(notificationStore);

const {
  fetchNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  deleteAllNotifications,
  clearMessage, // 메시지 초기화 액션
  clearError, // 에러 초기화 액션
} = notificationStore;

// 컴포넌트 내부 필터 상태
const filter = ref({
  type: "",
  readStatus: "",
  sortBy: "createdAt",
  page: 1,
  size: 10, // 페이지 당 항목 수
});

// 컴포넌트 마운트 시 실행
onMounted(() => {
  if (!isLoggedIn.value) {
    router.push("/user/login");
    return;
  }

  // URL 쿼리 파라미터에서 필터 조건 가져오기
  const query = route.query;
  if (query.type) filter.value.type = query.type;
  if (query.readStatus) filter.value.readStatus = query.readStatus;
  if (query.sortBy) filter.value.sortBy = query.sortBy;
  if (query.page) filter.value.page = parseInt(query.page);
  if (query.size) filter.value.size = parseInt(query.size);

  loadInitialNotifications();
});

// 필터 변경 시 알림 다시 로드 (페이지는 1로 초기화)
const applyFilterAndLoad = () => {
  filter.value.page = 1; // 필터 변경 시 첫 페이지부터 로드
  loadInitialNotifications();
};

// 알림 목록 로드 함수
const loadInitialNotifications = async () => {
  updateQueryParams(); // URL 쿼리 파라미터 먼저 업데이트
  try {
    await fetchNotifications({ ...filter.value });
  } catch (err) {
    // 에러는 스토어에서 이미 처리 (storeError에 저장됨)
    console.error("알림 목록 로드 실패 (컴포넌트):", err);
  }
};

// 페이지 이동
const goToPage = (pageNumber) => {
  if (pageNumber < 1 || pageNumber > totalPages.value || loading.value) return;
  filter.value.page = pageNumber;
  loadInitialNotifications();
};

// URL 쿼리 파라미터 업데이트
const updateQueryParams = () => {
  const query = {};
  if (filter.value.type) query.type = filter.value.type;
  if (filter.value.readStatus) query.readStatus = filter.value.readStatus;
  if (filter.value.sortBy !== "createdAt") query.sortBy = filter.value.sortBy;
  if (filter.value.page > 1) query.page = filter.value.page;
  if (filter.value.size !== 10) query.size = filter.value.size; // 기본값(10)이 아니면 추가

  // 현재 라우트와 쿼리가 다를 경우에만 push (무한 루프 방지)
  if (JSON.stringify(route.query) !== JSON.stringify(query)) {
    router.push({ query });
  }
};

// 라우트 쿼리 변경 감지 (브라우저 뒤로/앞으로 가기)
watch(
  () => route.query,
  (newQuery) => {
    if (newQuery.page && parseInt(newQuery.page) !== filter.value.page) {
      filter.value.page = parseInt(newQuery.page);
    }
    if (newQuery.type !== undefined && newQuery.type !== filter.value.type) {
      filter.value.type = newQuery.type;
    }
    if (newQuery.readStatus !== undefined && newQuery.readStatus !== filter.value.readStatus) {
      filter.value.readStatus = newQuery.readStatus;
    }
    if (newQuery.sortBy && newQuery.sortBy !== filter.value.sortBy) {
      filter.value.sortBy = newQuery.sortBy;
    }
    // 여기서 fetchNotifications를 직접 호출하면 중복 호출 가능성이 있으므로
    // 필터 값 변경 후 loadInitialNotifications가 호출되도록 로직을 구성하거나,
    // onMounted에서처럼 초기 로드 로직을 더 정교하게 관리합니다.
    // 또는, 필요한 경우에만 API를 호출하도록 조건 추가
    // 이 예제에서는 onMounted 및 필터 변경 시에만 로드합니다.
  },
  { deep: true }
);

// 알림 읽음 처리
const handleMarkAsRead = async (notification) => {
  if (loading.value) return;
  try {
    const success = await markNotificationAsRead(notification.notificationId);
    if (success && notification.link) {
      // 스토어에서 메시지를 설정하므로, 여기서는 추가 메시지 설정 안함
      router.push(notification.link);
    }
  } catch (err) {
    console.error("알림 읽음 처리 실패 (컴포넌트):", err);
  }
};

// 모든 알림 읽음 처리
const handleMarkAllAsRead = async () => {
  if (loading.value || unreadCount.value === 0) return;
  try {
    await markAllNotificationsAsRead();
  } catch (err) {
    console.error("모든 알림 읽음 처리 실패 (컴포넌트):", err);
  }
};

// 알림 삭제 확인
const confirmDeleteNotification = (notificationId) => {
  if (loading.value) return;
  if (confirm("이 알림을 삭제하시겠습니까?")) {
    handleDeleteNotification(notificationId);
  }
};

// 알림 삭제 처리
const handleDeleteNotification = async (notificationId) => {
  if (loading.value) return;
  try {
    await deleteNotification(notificationId);
    // 알림 삭제 후, 현재 페이지의 아이템이 모두 사라졌고, 이전 페이지가 있다면 그 페이지로 이동
    if (notifications.value.length === 0 && currentPage.value > 1) {
      goToPage(currentPage.value - 1);
    } else if (notifications.value.length === 0 && currentPage.value === 1 && totalItems.value > 0) {
      // 현재 페이지 아이템이 다 사라졌지만 전체 아이템은 남아있는 경우 (거의 발생 안함)
      loadInitialNotifications(); // 그냥 현재 필터로 재조회
    } else if (notifications.value.length > 0 && filter.value.page > totalPages.value) {
      // 삭제 후 현재 페이지가 전체 페이지 수보다 커진 경우 (마지막 페이지에서 마지막 아이템 삭제)
      goToPage(totalPages.value || 1);
    }
    // 그 외의 경우는 스토어가 상태를 업데이트하고 UI가 자동으로 반영
  } catch (err) {
    console.error("알림 삭제 실패 (컴포넌트):", err);
  }
};

// 모든 알림 삭제 확인
const confirmDeleteAllNotifications = () => {
  if (loading.value || notifications.value.length === 0) return;
  if (confirm("모든 알림을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
    handleDeleteAllNotifications();
  }
};

// 모든 알림 삭제 처리
const handleDeleteAllNotifications = async () => {
  if (loading.value) return;
  try {
    await deleteAllNotifications();
    // 모든 알림 삭제 후에는 보통 1페이지로 돌아감
    filter.value.page = 1;
    updateQueryParams(); // URL도 초기화
  } catch (err) {
    console.error("모든 알림 삭제 실패 (컴포넌트):", err);
  }
};

// 페이지네이션 아이템 계산
const paginationItems = computed(() => {
  const items = [];
  const maxVisiblePages = 5; // 중앙에 표시될 최대 페이지 수 (양쪽 끝 제외)

  if (totalPages.value <= 1) return []; // 페이지가 하나거나 없으면 페이지네이션 불필요

  if (totalPages.value <= maxVisiblePages + 2) {
    // (1 ... 2 3 4 ... 5) 이런 형태보다 짧으면 전부 표시
    for (let i = 1; i <= totalPages.value; i++) {
      items.push(i);
    }
  } else {
    items.push(1); // 첫 페이지

    let startPage = Math.max(2, currentPage.value - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages.value - 1, currentPage.value + Math.floor(maxVisiblePages / 2));

    // 현재 페이지가 앞쪽에 가까울 때
    if (currentPage.value - 1 < Math.floor(maxVisiblePages / 2) + 1) {
      endPage = Math.min(totalPages.value - 1, maxVisiblePages + 1);
    }
    // 현재 페이지가 뒤쪽에 가까울 때
    if (totalPages.value - currentPage.value < Math.floor(maxVisiblePages / 2) + 1) {
      startPage = Math.max(2, totalPages.value - maxVisiblePages);
    }

    if (startPage > 2) {
      items.push("...");
    }

    for (let i = startPage; i <= endPage; i++) {
      items.push(i);
    }

    if (endPage < totalPages.value - 1) {
      items.push("...");
    }

    items.push(totalPages.value); // 마지막 페이지
  }
  return items;
});

// 유틸리티 함수들 (기존 methods 내용을 setup 스코프로 이동)
const getNotificationIcon = (type) => {
  const icons = {
    RESERVATION: "bi bi-calendar-check",
    REVIEW: "bi bi-star",
    PAYMENT: "bi bi-credit-card",
    SYSTEM: "bi bi-gear",
    MESSAGE: "bi bi-chat-dots", // 예시로 추가
  };
  return icons[type] || "bi bi-bell";
};

const getNotificationTypeName = (type) => {
  const types = {
    RESERVATION: "예약",
    REVIEW: "리뷰",
    PAYMENT: "결제",
    SYSTEM: "시스템",
    MESSAGE: "메시지", // 예시로 추가
  };
  return types[type] || type;
};

const getNotificationTypeClass = (type) => {
  const classes = {
    RESERVATION: "type-reservation",
    REVIEW: "type-review",
    PAYMENT: "type-payment",
    SYSTEM: "type-system",
    MESSAGE: "type-message", // 예시로 추가
  };
  return classes[type] || "";
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
      if (diffMin < 1) {
        return "방금 전";
      }
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
.notification-item {
  display: flex;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 10px;
  background-color: #f8f9fa;
  transition: all 0.3s ease;
  cursor: pointer;
}

.notification-item:hover {
  background-color: #e9ecef;
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.notification-item.unread {
  background-color: #e8f4fd;
  border-left: 4px solid #0d6efd;
}

.notification-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: #e9ecef;
  border-radius: 50%;
  margin-right: 15px;
  font-size: 1.2rem;
}

.notification-item.unread .notification-icon {
  background-color: #cfe2ff;
  color: #0d6efd;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 600;
  margin-bottom: 5px;
}

.notification-message {
  color: #6c757d;
  margin-bottom: 5px;
}

.notification-meta {
  display: flex;
  font-size: 0.8rem;
  color: #6c757d;
}

.notification-time {
  margin-right: 10px;
}

.notification-type {
  padding: 2px 6px;
  border-radius: 10px;
  font-size: 0.7rem;
  font-weight: 600;
}

.type-reservation {
  background-color: #cff4fc;
  color: #055160;
}

.type-review {
  background-color: #fff3cd;
  color: #664d03;
}

.type-payment {
  background-color: #d1e7dd;
  color: #0f5132;
}

.type-system {
  background-color: #f8d7da;
  color: #842029;
}

.type-message {
  background-color: #e2e3e5;
  color: #41464b;
}

.notification-actions {
  display: flex;
  align-items: center;
}

.notification-actions .btn {
  opacity: 0.5;
  transition: opacity 0.3s ease;
}

.notification-item:hover .notification-actions .btn {
  opacity: 1;
}

.pagination {
  margin-top: 20px;
}
</style>
