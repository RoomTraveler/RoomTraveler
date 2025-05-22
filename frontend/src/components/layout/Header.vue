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
              <div class="toggle-button" :class="{ active: selected === '숙박' }" @click="select('숙박')">숙박</div>
              <div class="toggle-button" :class="{ active: selected === '여행' }" @click="select('여행')">여행</div>
              <div class="toggle-indicator" :style="indicatorStyle"></div>
            </div>
          </div>

          <!-- 검색 영역 -->
          <div class="search-area">
            <div class="search-input-wrapper">
              <template v-if="selected !== '숙박'">
                <input
                  type="text"
                  v-model="searchKeyword"
                  placeholder="관광지"
                  class="search-input"
                  @keyup.enter="searchAttractions"
                />
                <button class="search-button" @click="searchAttractions">
                  <i class="bi bi-search"></i>
                </button>
              </template>
              <template v-else>
                <input type="text" placeholder="지역, 숙소명" class="search-input" />
                <button class="search-button">
                  <i class="bi bi-search"></i>
                </button>
              </template>
            </div>
          </div>

          <!-- 사용자 메뉴 영역 -->
          <div class="user-menu">
            <!-- 찜 목록 버튼 (로그인 시에만 보이도록) -->
            <router-link
              v-if="isLoggedIn && selected === '숙박'"
              to="/accommodation/favorites"
              class="user-menu-item icon-btn"
              title="찜 목록"
            >
              <i class="bi bi-heart"></i>
            </router-link>

            <div v-if="isLoggedIn && selected !== '숙박'" class="user-dropdown">
              <button class="user-dropdown-toggle" title="찜 목록">
                <i class="bi bi-heart"></i>
              </button>
              <div class="user-dropdown-menu">
                <li><router-link to="/attractions" class="dropdown-item">관광지</router-link></li>
                <li><router-link to="/plans" class="dropdown-item">여행 플랜</router-link></li>
              </div>
            </div>

            <!-- 장바구니 버튼 (로그인 시에만 보이도록) -->
            <router-link
              v-if="isLoggedIn"
              to="/accommodation/cart"
              class="user-menu-item icon-btn position-relative"
              title="장바구니"
            >
              <i class="bi bi-cart"></i>
              <span v-if="cartItemCount > 0" class="notification-badge cart-badge">
                {{ cartItemCount }}
              </span>
            </router-link>

            <template v-if="!isLoggedIn">
              <router-link :to="{ path: '/login', query: { from: selected } }" class="user-menu-item"
                >로그인</router-link
              >
              <router-link to="/register" class="user-menu-item">회원가입</router-link>
            </template>
            <template v-else>
              <!-- 알림 아이콘 -->
              <router-link to="/notification/my" class="user-menu-item position-relative icon-btn" title="알림">
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
                  <li><router-link to="/user/profile" class="dropdown-item">마이페이지</router-link></li>
                  <li><router-link to="/reservation/my-reservations" class="dropdown-item">나의 예약</router-link></li>
                  <li><router-link to="/accommodation/favorites" class="dropdown-item">찜 목록</router-link></li>
                  <template v-if="isAdmin">
                    <li><router-link to="/admin" class="dropdown-item admin-link">관리자</router-link></li>
                  </template>
                  <template v-if="isHost || isAdmin">
                    <li><router-link to="/host" class="dropdown-item host-link">호스트</router-link></li>
                  </template>
                  <li><hr class="dropdown-divider" /></li>
                  <li><a href="#" @click.prevent="logout" class="dropdown-item">로그아웃</a></li>
                </div>
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { storeToRefs } from "pinia";
import { useUserStore } from "@/store/userStore";
import { useNotificationStore } from "@/store/notificationStore";
import { useCartStore } from "@/store/cartStore";
import api from "@/api/index"; // Axios wrapper

// 📦 스토어
const userStore = useUserStore();
const notificationStore = useNotificationStore();
const cartStore = useCartStore();

// 📦 스토어에서 필요한 state 꺼내기
const { unreadCount: unreadNotificationCount } = storeToRefs(notificationStore);
const { cart } = storeToRefs(cartStore);
const { fetchUnreadCount } = notificationStore;
const { fetchCart: fetchCartItems } = cartStore;

// 📍 Router 관련
const router = useRouter();
const route = useRoute();

// 📌 상태 변수
const selected = ref("숙박");
const notificationInterval = ref(null);
const searchKeyword = ref("");
const attractions = ref([]); // 검색 결과 저장

// ✅ Computed
const isLoggedIn = computed(() => userStore.isAuthenticated);
const cartItemCount = computed(() => cart.value?.totalItems || 0);
const userId = computed(() => userStore.user?.id || null);
const isAdmin = computed(() => userStore.userRole === "ADMIN");
const isHost = computed(() => userStore.userRole === "HOST");

const indicatorStyle = computed(() => ({
  transform: selected.value === "숙박" ? "translateX(0%)" : "translateX(100%)",
}));

// ✅ 초기 알림 카운트 로드
const loadInitialNotificationCount = async () => {
  try {
    await fetchUnreadCount();
  } catch (error) {
    console.error("초기 알림 카운트 로드 중 오류:", error);
  }
};

// ✅ 장바구니 데이터 로드
const loadCartData = async () => {
  try {
    await fetchCartItems();
  } catch (error) {
    console.error("장바구니 데이터 로드 중 오류:", error);
  }
};

// ✅ 알림 카운트 주기적 로드
const loadNotificationCount = async () => {
  if (!isLoggedIn.value) {
    if (notificationInterval.value) {
      clearInterval(notificationInterval.value);
      notificationInterval.value = null;
    }
    return;
  }

  try {
    await fetchUnreadCount();
  } catch (error) {
    console.error("알림 카운트 주기 로드 중 오류:", error);
  }
};

// ✅ 로그아웃
const logout = () => {
  userStore.logout();
  router.push("/");
};

// ✅ 탭 전환
const select = (value) => {
  selected.value = value;
  setTimeout(() => {
    router.push(value === "숙박" ? "/accommodation" : "/plan");
  }, 200);
};

// ✅ 검색 함수 (Enter 또는 버튼 클릭)
const searchAttractions = () => {
  if (!searchKeyword.value.trim()) {
    alert("검색어를 입력해주세요.");
    return;
  }

  router.push({
    path: "/attraction/search",
    query: { keyword: searchKeyword.value.trim() },
  });
};

// ✅ 마운트
onMounted(() => {
  if (isLoggedIn.value) {
    loadInitialNotificationCount();
    loadCartData();
    notificationInterval.value = setInterval(loadNotificationCount, 30000);
  }

  if (route.path.includes("/accommodation")) {
    selected.value = "숙박";
  } else if (route.path.includes("/plan")) {
    selected.value = "여행";
  }
});

// ✅ 언마운트
onBeforeUnmount(() => {
  if (notificationInterval.value) {
    clearInterval(notificationInterval.value);
  }
});

// ✅ 로그인 여부 변경 감지
watch(isLoggedIn, (newVal) => {
  if (newVal) {
    loadInitialNotificationCount();
    loadCartData();
    if (!notificationInterval.value) {
      notificationInterval.value = setInterval(loadNotificationCount, 30000);
    }
  } else {
    if (notificationInterval.value) {
      clearInterval(notificationInterval.value);
      notificationInterval.value = null;
    }
  }
});
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
  line-height: 1;
}

.cart-badge {
  top: -6px;
  right: -8px;
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

.nav-item:hover,
.nav-item.active {
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
  background-color: #ffffff;
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

/* 드롭다운 메뉴 li 스타일 추가 */
.user-dropdown-menu > li {
  list-style-type: none; /* 기본 리스트 스타일(점) 제거 */
  margin: 0; /* 기본 마진 제거 */
  padding: 0; /* 기본 패딩 제거 */
}
</style>
