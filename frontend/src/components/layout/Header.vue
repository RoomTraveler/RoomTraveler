<template>
  <div class="yanolja-header">
    <!-- 상단 헤더 영역 -->
    <div class="top-header">
      <div class="container">
        <div class="top-header-content">
          <!-- 로고 영역 -->
          <div class="logo-area">
            <router-link to="/" class="no-underline">
              <h1 class="logo">방구석 여행자</h1>
            </router-link>

            <!-- 토글 버튼 영역 -->
            <div class="toggle-wrapper">
              <div
                class="toggle-button"
                :class="{ active: selected === '숙박' }"
                @click="select('숙박')"
              >
                숙박
              </div>
              <div
                class="toggle-button"
                :class="{ active: selected === '여행' }"
                @click="select('여행')"
              >
                여행
              </div>
              <div class="toggle-indicator" :style="indicatorStyle"></div>
            </div>
          </div>

          <!-- 검색 영역 -->
          <div class="search-area">
            <div class="search-input-wrapper">
              <input type="text" placeholder="지역, 숙소명" class="search-input" />
              <button class="search-button">
                <i class="bi bi-search"></i>
              </button>
            </div>
          </div>

          <!-- 사용자 메뉴 영역 -->
          <div class="user-menu">
            <!-- 찜 목록 버튼 -->
            <router-link to="/accommodation/favorites" class="user-menu-item icon-btn" title="찜 목록">
              <i class="bi bi-heart"></i>
            </router-link>

            <!-- 장바구니 버튼 -->
            <router-link to="/cart" class="user-menu-item icon-btn" title="장바구니">
              <i class="bi bi-cart"></i>
            </router-link>

            <template v-if="!isLoggedIn">
              <router-link to="/login" class="user-menu-item">로그인</router-link>
              <router-link to="/register" class="user-menu-item">회원가입</router-link>
            </template>
            <template v-else>
              <!-- 알림 아이콘 -->
              <router-link to="/notification" class="user-menu-item position-relative icon-btn" title="알림">
                <i class="bi bi-bell-fill"></i>
                <span v-if="unreadNotificationCount > 0" class="notification-badge">
                  {{ unreadNotificationCount }}
                </span>
              </router-link>

              <!-- 마이페이지 드롭다운 -->
              <div class="user-dropdown">
                <button class="user-dropdown-toggle" title="마이페이지">
                  <i class="bi bi-person-circle"></i>
                </button>
                <div class="user-dropdown-menu">
                  <router-link to="/user/profile" class="dropdown-item">마이페이지</router-link>
                  <router-link to="/reservation/my-reservations" class="dropdown-item">예약내역</router-link>
                  <router-link to="/accommodation/favorites" class="dropdown-item">찜 목록</router-link>
                  <template v-if="isAdmin">
                    <router-link to="/admin" class="dropdown-item admin-link">관리자</router-link>
                  </template>
                  <template v-if="isHost || isAdmin">
                    <router-link to="/host" class="dropdown-item host-link">호스트</router-link>
                  </template>
                  <a href="#" @click.prevent="logout" class="dropdown-item">로그아웃</a>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>



  </div>
</template>

<script>
import { useUserStore } from '@/store/userStore';
import api from '@/api/index';

/**
 * 헤더 컴포넌트
 * 
 * 이 컴포넌트는 웹사이트의 공통 헤더 부분을 담당합니다.
 * 로그인 상태에 따라 다른 메뉴를 표시하며, 알림 기능을 포함합니다.
 */
export default {
  name: 'Header',
  setup() {
    // Pinia 스토어 사용
    const userStore = useUserStore();
    return { userStore };
  },
  data() {
    return {
      unreadNotificationCount: 0,
      selected: '숙박'
    }
  },
  computed: {
    /**
     * 토글 버튼 인디케이터 스타일
     * @returns {Object} 인디케이터 위치 스타일 객체
     */
    indicatorStyle() {
      return {
        transform: this.selected === '숙박' ? 'translateX(0%)' : 'translateX(100%)',
      }
    },
    /**
     * 사용자 로그인 상태 확인
     * @returns {boolean} 로그인 상태 여부
     */
    isLoggedIn() {
      return this.userStore.isAuthenticated
    },
    /**
     * 현재 로그인한 사용자 ID
     * @returns {number|null} 사용자 ID 또는 null
     */
    userId() {
      return this.userStore.user?.id
    },
    /**
     * 사용자가 관리자인지 확인
     * @returns {boolean} 관리자 여부
     */
    isAdmin() {
      return this.userStore.userRole === 'ADMIN'
    }
  },
  mounted() {
    // 로그인 상태일 때만 알림 카운트 로드
    if (this.isLoggedIn) {
      this.loadNotificationCount()
      // 30초마다 알림 카운트 갱신
      this.notificationInterval = setInterval(this.loadNotificationCount, 30000)
    }
  },
  beforeUnmount() {
    // 컴포넌트 제거 시 인터벌 정리
    if (this.notificationInterval) {
      clearInterval(this.notificationInterval)
    }
  },
  methods: {
    /**
     * 토글 버튼 선택 함수
     * @param {string} value - 선택된 값 ('숙박' 또는 '여행')
     */
    select(value) {
      this.selected = value
      // 선택된 값에 따라 다른 페이지로 이동
      if (value === '숙박') {
        this.$router.push('/accommodation')
      } else if (value === '여행') {
        this.$router.push('/plan')
      }
    },
    /**
     * 알림 카운트 로드 함수
     */
    loadNotificationCount() {
      // API 호출로 알림 카운트 가져오기
      api.get('/api/notification/count/unread')
        .then(response => {
          this.unreadNotificationCount = response.data
        })
        .catch(error => {
          console.error('알림 카운트 로드 중 오류:', error)
        })
    },
    /**
     * 로그아웃 처리 함수
     */
    logout() {
      this.userStore.logout();
      this.$router.push('/');
    }
  }
}
</script>

<style scoped>
:root {
  --yanolja-red: #f0213b;
  --yanolja-pink: #ff3478;
  --yanolja-light-gray: #f5f5f5;
  --yanolja-dark-gray: #666;
}

.yanolja-header {
  width: 100%;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.top-header {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.top-header .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
}

.top-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo-area {
  display: flex;
  align-items: center;
  flex: 0 0 auto;
}

.logo {
  font-size: 26px;
  font-weight: bold;
  color: var(--yanolja-red);
  margin: 0 0 0 25px;
}

.search-area {
  flex: 1;
  max-width: 600px;
  margin: 0 30px;
}

.search-input-wrapper {
  display: flex;
  border: 2px solid var(--yanolja-red);
  border-radius: 24px;
  overflow: hidden;
}

.search-input {
  flex: 1;
  padding: 12px 18px;
  border: none;
  outline: none;
  font-size: 16px;
}

.search-button {
  background-color: var(--yanolja-red);
  color: white;
  border: none;
  padding: 0 20px;
  cursor: pointer;
  font-size: 18px;
}

.user-menu {
  display: flex;
  align-items: center;
}

.user-menu-item {
  margin-left: 15px;
  color: #333;
  text-decoration: none;
  font-size: 14px;
}

/* 로그인/회원가입 링크 스타일 */
.user-menu-item:not(.icon-btn) {
  margin-left: -15px;
}

/* 회원가입 링크에 왼쪽 마진 추가하여 로그인 링크와 간격 생성 */
.user-menu-item:not(.icon-btn) + .user-menu-item:not(.icon-btn) {
  margin-left: 15px;
}

.user-menu-item:hover {
  color: var(--yanolja-red);
}

.icon-btn {
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background-color: var(--yanolja-red);
  color: white;
  border-radius: 50%;
  width: 18px;
  height: 18px;
  font-size: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.user-dropdown {
  position: relative;
  margin-left: 15px;
}

.user-dropdown-toggle {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 20px;
  color: #333;
}

.user-dropdown-menu {
  position: absolute;
  right: 0;
  top: 100%;
  background-color: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  border-radius: 4px;
  width: 150px;
  display: none;
  z-index: 1001;
}

.user-dropdown:hover .user-dropdown-menu {
  display: block;
}

.dropdown-item {
  display: block;
  padding: 10px 15px;
  color: #333;
  text-decoration: none;
  font-size: 14px;
}

.dropdown-item:hover {
  background-color: #f5f5f5;
}

.admin-link {
  color: var(--yanolja-red);
}

.host-link {
  color: #28a745;
}

.nav-area {
  padding: 10px 0;
  background-color: white;
}

.nav-area .container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0;
}

.main-nav {
  display: flex;
  justify-content: space-between;
  max-width: 800px;
  margin: 0 auto;
}

.nav-item {
  color: #333;
  text-decoration: none;
  font-weight: 500;
  padding: 5px 10px;
  border-radius: 4px;
  transition: all 0.2s;
}

.nav-item:hover, .nav-item.active {
  color: var(--yanolja-red);
}

@media (max-width: 768px) {
  .top-header-content {
    flex-direction: column;
  }

  .logo-area {
    margin-bottom: 15px;
    width: 100%;
    justify-content: space-between;
  }

  .search-area {
    margin: 15px 0;
    width: 100%;
    max-width: 100%;
  }

  .search-input {
    padding: 10px 15px;
    font-size: 15px;
  }

  .main-nav {
    overflow-x: auto;
    justify-content: flex-start;
  }

  .nav-item {
    white-space: nowrap;
    margin-right: 15px;
  }

  /* Reset negative margin for login/signup links on mobile */
  .user-menu-item:not(.icon-btn) {
    margin-left: 0;
  }
}

/* 토글 버튼 스타일 */
.toggle-wrapper {
  position: relative;
  display: flex;
  width: 170px;
  height: 42px;
  border-radius: 21px;
  border: 2px solid #ddd;
  overflow: hidden;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  font-weight: bold;
  font-size: 15px;
  cursor: pointer;
  background-color: #f8f8f8;
  margin: 0 15px 0 20px;
}

.toggle-button {
  flex: 1;
  text-align: center;
  line-height: 42px;
  z-index: 1;
  transition: color 0.3s;
  padding: 0 5px;
}

.toggle-button.active {
  color: white;
}

.toggle-indicator {
  position: absolute;
  top: 0;
  left: 0;
  width: 50%;
  height: 100%;
  background-color: #f53b64;
  border-radius: 30px;
  transition: transform 0.3s ease;
  z-index: 0;
}
</style>
