<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">알림 목록</h2>

      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>

      <!-- 필터 및 정렬 -->
      <div class="card mb-4">
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-4">
              <label for="filterType" class="form-label">알림 유형</label>
              <select class="form-select" id="filterType" v-model="filter.type" @change="loadNotifications">
                <option value="">전체</option>
                <option value="RESERVATION">예약</option>
                <option value="REVIEW">리뷰</option>
                <option value="PAYMENT">결제</option>
                <option value="SYSTEM">시스템</option>
              </select>
            </div>
            <div class="col-md-4">
              <label for="filterStatus" class="form-label">읽음 상태</label>
              <select class="form-select" id="filterStatus" v-model="filter.readStatus" @change="loadNotifications">
                <option value="">전체</option>
                <option value="READ">읽음</option>
                <option value="UNREAD">읽지 않음</option>
              </select>
            </div>
            <div class="col-md-4">
              <label for="sortBy" class="form-label">정렬</label>
              <select class="form-select" id="sortBy" v-model="filter.sortBy" @change="loadNotifications">
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
            @click="markAllAsRead"
            :disabled="unreadCount === 0"
          >
            <i class="bi bi-check-all me-1"></i> 모두 읽음으로 표시
          </button>
        </div>
        <div>
          <button 
            class="btn btn-outline-danger" 
            @click="confirmDeleteAll"
            :disabled="notifications.length === 0"
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
        <div v-if="notifications.length === 0" class="alert alert-info">
          알림이 없습니다.
        </div>

        <div v-else>
          <div v-for="notification in notifications" :key="notification.notificationId" 
               class="notification-item" 
               :class="{ 'unread': !notification.read }"
               @click="markAsRead(notification.notificationId)">
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
                @click.stop="confirmDelete(notification.notificationId)"
                title="알림 삭제"
              >
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </div>

          <!-- 페이지네이션 -->
          <nav v-if="totalPages > 1" aria-label="Page navigation" class="mt-4">
            <ul class="pagination justify-content-center">
              <li class="page-item" :class="{ disabled: currentPage === 1 }">
                <a class="page-link" href="#" @click.prevent="goToPage(currentPage - 1)">이전</a>
              </li>
              <li 
                v-for="page in paginationItems" 
                :key="page" 
                class="page-item" 
                :class="{ active: page === currentPage, disabled: page === '...' }"
              >
                <a class="page-link" href="#" @click.prevent="page !== '...' && goToPage(page)">{{ page }}</a>
              </li>
              <li class="page-item" :class="{ disabled: currentPage === totalPages }">
                <a class="page-link" href="#" @click.prevent="goToPage(currentPage + 1)">다음</a>
              </li>
            </ul>
          </nav>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 알림 목록 컴포넌트
 * 
 * 이 컴포넌트는 사용자의 알림 목록을 표시합니다.
 * 알림 유형별 필터링, 읽음/읽지 않음 상태 필터링, 정렬 기능을 제공합니다.
 * 알림을 읽음으로 표시하거나 삭제할 수 있습니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'NotificationList',
  components: {
    Layout
  },
  data() {
    return {
      loading: true,
      message: '',
      error: '',
      notifications: [],
      unreadCount: 0,
      totalItems: 0,
      totalPages: 0,
      currentPage: 1,
      filter: {
        type: '',
        readStatus: '',
        sortBy: 'createdAt',
        page: 1,
        size: 10
      }
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.user.isLoggedIn
    }),

    /**
     * 페이지네이션 아이템 계산
     * @returns {Array} 페이지네이션 아이템 배열
     */
    paginationItems() {
      const items = [];
      const maxVisiblePages = 5;

      if (this.totalPages <= maxVisiblePages) {
        // 전체 페이지가 최대 표시 페이지 수보다 작거나 같으면 모든 페이지 표시
        for (let i = 1; i <= this.totalPages; i++) {
          items.push(i);
        }
      } else {
        // 현재 페이지 주변의 페이지만 표시
        items.push(1); // 첫 페이지는 항상 표시

        if (this.currentPage > 3) {
          items.push('...'); // 현재 페이지가 3보다 크면 '...' 표시
        }

        // 현재 페이지 주변 페이지 표시
        const start = Math.max(2, this.currentPage - 1);
        const end = Math.min(this.totalPages - 1, this.currentPage + 1);

        for (let i = start; i <= end; i++) {
          items.push(i);
        }

        if (this.currentPage < this.totalPages - 2) {
          items.push('...'); // 현재 페이지가 마지막에서 3번째 이전이면 '...' 표시
        }

        items.push(this.totalPages); // 마지막 페이지는 항상 표시
      }

      return items;
    }
  },
  created() {
    // 로그인 상태 확인
    if (!this.isLoggedIn) {
      this.$router.push('/user/login');
      return;
    }

    // URL 쿼리 파라미터에서 필터 조건 가져오기
    const query = this.$route.query;
    if (query.type) this.filter.type = query.type;
    if (query.readStatus) this.filter.readStatus = query.readStatus;
    if (query.sortBy) this.filter.sortBy = query.sortBy;
    if (query.page) this.filter.page = parseInt(query.page);

    // 알림 목록 로드
    this.loadNotifications();
  },
  methods: {
    ...mapActions('notification', [
      'fetchNotifications', 
      'markNotificationAsRead', 
      'markAllNotificationsAsRead', 
      'deleteNotification',
      'deleteAllNotifications'
    ]),

    /**
     * 알림 목록 로드
     */
    async loadNotifications() {
      this.loading = true;

      try {
        // URL 쿼리 파라미터 업데이트
        this.updateQueryParams();

        // 알림 목록 가져오기
        const result = await this.fetchNotifications(this.filter);

        this.notifications = result.content;
        this.totalItems = result.totalElements;
        this.totalPages = result.totalPages;
        this.currentPage = result.number + 1;

        // 읽지 않은 알림 개수 계산
        this.unreadCount = this.notifications.filter(notification => !notification.read).length;
      } catch (error) {
        console.error('알림 목록을 불러오는 중 오류가 발생했습니다:', error);
        this.error = '알림 목록을 불러오는 중 오류가 발생했습니다.';
      } finally {
        this.loading = false;
      }
    },

    /**
     * 페이지 이동
     * @param {number} page - 이동할 페이지 번호
     */
    goToPage(page) {
      if (page < 1 || page > this.totalPages) return;

      this.filter.page = page;
      this.loadNotifications();
    },

    /**
     * URL 쿼리 파라미터 업데이트
     */
    updateQueryParams() {
      const query = {};

      if (this.filter.type) query.type = this.filter.type;
      if (this.filter.readStatus) query.readStatus = this.filter.readStatus;
      if (this.filter.sortBy !== 'createdAt') query.sortBy = this.filter.sortBy;
      if (this.filter.page > 1) query.page = this.filter.page;

      this.$router.replace({ query });
    },

    /**
     * 알림을 읽음으로 표시
     * @param {number} notificationId - 알림 ID
     */
    async markAsRead(notificationId) {
      try {
        await this.markNotificationAsRead(notificationId);

        // 알림 상태 업데이트
        const notification = this.notifications.find(n => n.notificationId === notificationId);
        if (notification) {
          if (!notification.read) {
            notification.read = true;
            this.unreadCount--;
          }

          // 알림 링크가 있으면 해당 페이지로 이동
          if (notification.link) {
            this.$router.push(notification.link);
          }
        }
      } catch (error) {
        console.error('알림 읽음 처리 중 오류가 발생했습니다:', error);
        this.error = '알림을 읽음으로 표시하는 중 오류가 발생했습니다.';
      }
    },

    /**
     * 모든 알림을 읽음으로 표시
     */
    async markAllAsRead() {
      if (this.unreadCount === 0) return;

      try {
        await this.markAllNotificationsAsRead();

        // 알림 상태 업데이트
        this.notifications.forEach(notification => {
          notification.read = true;
        });
        this.unreadCount = 0;

        this.message = '모든 알림이 읽음으로 표시되었습니다.';
      } catch (error) {
        console.error('모든 알림 읽음 처리 중 오류가 발생했습니다:', error);
        this.error = '모든 알림을 읽음으로 표시하는 중 오류가 발생했습니다.';
      }
    },

    /**
     * 알림 삭제 확인
     * @param {number} notificationId - 알림 ID
     */
    confirmDelete(notificationId) {
      if (confirm('이 알림을 삭제하시겠습니까?')) {
        this.deleteNotificationItem(notificationId);
      }
    },

    /**
     * 알림 삭제 처리
     * @param {number} notificationId - 알림 ID
     */
    async deleteNotificationItem(notificationId) {
      try {
        await this.deleteNotification(notificationId);

        // 알림 목록에서 삭제
        const index = this.notifications.findIndex(n => n.notificationId === notificationId);
        if (index !== -1) {
          const notification = this.notifications[index];
          if (!notification.read) {
            this.unreadCount--;
          }
          this.notifications.splice(index, 1);
          this.totalItems--;
        }

        this.message = '알림이 삭제되었습니다.';
      } catch (error) {
        console.error('알림 삭제 중 오류가 발생했습니다:', error);
        this.error = '알림 삭제 중 오류가 발생했습니다.';
      }
    },

    /**
     * 모든 알림 삭제 확인
     */
    confirmDeleteAll() {
      if (this.notifications.length === 0) return;

      if (confirm('모든 알림을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        this.deleteAllNotificationsItems();
      }
    },

    /**
     * 모든 알림 삭제 처리
     */
    async deleteAllNotificationsItems() {
      try {
        await this.deleteAllNotifications();

        // 알림 목록 초기화
        this.notifications = [];
        this.totalItems = 0;
        this.unreadCount = 0;

        this.message = '모든 알림이 삭제되었습니다.';
      } catch (error) {
        console.error('모든 알림 삭제 중 오류가 발생했습니다:', error);
        this.error = '모든 알림 삭제 중 오류가 발생했습니다.';
      }
    },

    /**
     * 알림 유형에 따른 아이콘 클래스 반환
     * @param {string} type - 알림 유형
     * @returns {string} 아이콘 클래스
     */
    getNotificationIcon(type) {
      const icons = {
        'RESERVATION': 'bi bi-calendar-check',
        'REVIEW': 'bi bi-star',
        'PAYMENT': 'bi bi-credit-card',
        'SYSTEM': 'bi bi-gear',
        'MESSAGE': 'bi bi-chat-dots'
      };

      return icons[type] || 'bi bi-bell';
    },

    /**
     * 알림 유형 이름 반환
     * @param {string} type - 알림 유형
     * @returns {string} 알림 유형 이름
     */
    getNotificationTypeName(type) {
      const types = {
        'RESERVATION': '예약',
        'REVIEW': '리뷰',
        'PAYMENT': '결제',
        'SYSTEM': '시스템',
        'MESSAGE': '메시지'
      };

      return types[type] || type;
    },

    /**
     * 알림 유형에 따른 클래스 반환
     * @param {string} type - 알림 유형
     * @returns {string} 클래스
     */
    getNotificationTypeClass(type) {
      const classes = {
        'RESERVATION': 'type-reservation',
        'REVIEW': 'type-review',
        'PAYMENT': 'type-payment',
        'SYSTEM': 'type-system',
        'MESSAGE': 'type-message'
      };

      return classes[type] || '';
    },

    /**
     * 날짜 포맷팅
     * @param {string|Date} date - 포맷팅할 날짜
     * @returns {string} 포맷팅된 날짜 문자열
     */
    formatDate(date) {
      if (!date) return '';

      const now = new Date();
      const d = new Date(date);
      const diffMs = now - d;
      const diffSec = Math.floor(diffMs / 1000);
      const diffMin = Math.floor(diffSec / 60);
      const diffHour = Math.floor(diffMin / 60);
      const diffDay = Math.floor(diffHour / 24);

      // 1일 이내: 시간 표시
      if (diffDay < 1) {
        if (diffHour < 1) {
          if (diffMin < 1) {
            return '방금 전';
          }
          return `${diffMin}분 전`;
        }
        return `${diffHour}시간 전`;
      }

      // 7일 이내: n일 전 표시
      if (diffDay < 7) {
        return `${diffDay}일 전`;
      }

      // 그 외: YYYY-MM-DD 형식
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');

      return `${year}-${month}-${day}`;
    }
  }
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
