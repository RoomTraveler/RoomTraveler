<template>
  <div class="container">
    <!-- 전체 .container 시작 -->
    <header class="d-flex justify-content-center my-5 align-items-center">
      <h1 class="text-center">Welcome To</h1>
      <img src="/img/ssafy_logo.png" id="logo" />
    </header>
    <div class="d-flex justify-content-end">
      <router-link to="/" class="mx-3">메인으로</router-link> |
      <router-link to="/trip/plan" class="mx-3">나의 여행지 리스트</router-link> |
      <router-link to="/host" class="mx-3 text-success">호스트 사이트</router-link>
      <!-- 회원가입을 위한 페이지를 요청하는 링크 -->
      <template v-if="!isLoggedIn">
        | <router-link to="/user/register" class="mx-3">회원가입</router-link> |
        <router-link to="/user/login" class="mx-3">로그인</router-link>
      </template>
      <template v-else>
        | 
        <!-- 알림 아이콘 -->
        <router-link to="/notification" class="mx-3 position-relative">
          <i class="bi bi-bell-fill"></i>
          <!-- 알림 카운트는 AJAX로 로드됨 -->
          <div id="notification-count-container">
            <span v-if="unreadNotificationCount > 0" class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger">
              {{ unreadNotificationCount }}
            </span>
          </div>
        </router-link> |
        <a href="#" @click.prevent="logout" class="mx-3">로그아웃</a>
      </template>
    </div>
    <hr />
  </div>
</template>

<script>
/**
 * 헤더 컴포넌트
 * 
 * 이 컴포넌트는 웹사이트의 공통 헤더 부분을 담당합니다.
 * 로그인 상태에 따라 다른 메뉴를 표시하며, 알림 기능을 포함합니다.
 */
export default {
  name: 'Header',
  data() {
    return {
      unreadNotificationCount: 0
    }
  },
  computed: {
    /**
     * 사용자 로그인 상태 확인
     * @returns {boolean} 로그인 상태 여부
     */
    isLoggedIn() {
      return this.$store.state.user.isLoggedIn
    },
    /**
     * 현재 로그인한 사용자 ID
     * @returns {number|null} 사용자 ID 또는 null
     */
    userId() {
      return this.$store.state.user.userId
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
     * 알림 카운트 로드 함수
     */
    loadNotificationCount() {
      // API 호출로 알림 카운트 가져오기
      this.$axios.get('/api/notification/count/unread')
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
      this.$store.dispatch('user/logout')
        .then(() => {
          this.$router.push('/')
        })
    }
  }
}
</script>

<style scoped>
header > #logo {
  width: 90px;
  margin-bottom: 8px;
  margin-left: 10px;
}

header > h1 {
  line-height: 50px;
  display: inline-block;
  height: 50px;
}
</style>