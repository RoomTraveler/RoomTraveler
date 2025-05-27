<template>
  <div class="login-container">
    <div class="login-form">
      <!-- 동적 타이틀 -->
      <h1>로그인</h1>

      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <form @submit.prevent="login">
        <div class="form-group">
          <label for="email">이메일</label>
          <input
            type="email"
            id="email"
            v-model="email"
            required
            placeholder="이메일 주소를 입력하세요"
          />
        </div>

        <div class="form-group">
          <label for="password">비밀번호</label>
          <input type="password" id="password" v-model="password" required placeholder="비밀번호를 입력하세요" />
        </div>

        <div class="form-options">
          <div class="remember-me">
            <input type="checkbox" id="remember" v-model="rememberMe" />
            <label for="remember">로그인 상태 유지</label>
          </div>
          <a href="#" class="forgot-password">비밀번호 찾기</a>
        </div>

        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? "로그인 중..." : "로그인" }}
        </button>
      </form>


      <!-- 회원가입 링크 -->
      <div class="register-link">
        계정이 없으신가요?
        <router-link to="/register">
          회원가입
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick } from "vue";
import { useUserStore } from "@/store/userStore";
import { useRouter, useRoute } from "vue-router";
// api 임포트는 현재 사용되지 않으므로 주석 처리 또는 제거 가능
// import api from "@/api/index";

export default {
  name: "LoginView",
  setup() {
    const userStore = useUserStore();
    const router = useRouter();
    const route = useRoute();

    const email = ref("");
    const password = ref("");
    const rememberMe = ref(false);
    // isHostMode 제거
    const fromTab = ref(null); // Header에서 전달된 탭 정보 저장

    const loading = computed(() => userStore.loading);
    const componentError = ref(null);

    async function handleLogin() {
      componentError.value = null;
      // login 메소드 호출 시 isHostMode 인자 제거 (userStore.login 시그니처 확인 필요)
      const result = await userStore.login(email.value, password.value);

      if (result && result.success) {
        const userRole = result.user?.role; // 역할은 계속 사용될 수 있음
        let redirectPath = route.query.redirect?.toString() || "/";

        if (fromTab.value === "숙박") {
          redirectPath = "/accommodation";
        } else if (fromTab.value === "여행") {
          redirectPath = "/plan";
        }

        // 역할별 리다이렉션은 유지하되, HOST 관련 특별 처리는 일반 사용자 경로로 변경하거나 제거
        // 예시: ADMIN만 특별 취급하고, HOST는 일반 사용자와 동일하게 처리
        if (userRole === "ADMIN") {
          redirectPath = route.query.redirect?.toString() || "/admin";
        } else if (userRole === "HOST") {
          // 호스트도 일반 사용자처럼 메인 페이지 또는 이전 페이지로 리다이렉션
          // 혹은 호스트 전용 대시보드가 있다면 그 경로로 설정
           redirectPath = route.query.redirect?.toString() || "/"; // 예: 일반 사용자와 동일
        }


        console.log(
          "[Login.vue] Login successful. Preparing to redirect to:",
          redirectPath,
          "From Tab:",
          fromTab.value,
          "User Role:",
          userRole
        );

        await nextTick();

        console.log("[Login.vue] Redirecting now...");
        router.push(redirectPath);
      } else {
        // 에러 메시지 일반화
        componentError.value = result.error || "이메일 또는 비밀번호가 올바르지 않습니다.";
      }
    }

    // toggleMode 함수 제거

    onMounted(() => {
      if (route.query.from) {
        fromTab.value = route.query.from;
        console.log("[Login.vue] Mounted. Received fromTab:", fromTab.value);
      }

      // isHostMode 관련 로직 제거
      // if (route.path.toLowerCase().includes("host")) {
      //   isHostMode.value = true;
      // }

      if (userStore.loading) {
        userStore.loading = false;
      }
    });

    return {
      email,
      password,
      rememberMe,
      loading,
      error: componentError,
      // isHostMode 제거
      login: handleLogin,
      // toggleMode 제거
      userStore, // 디버깅용
    };
  },
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 2rem;
}

.login-form {
  width: 100%;
  max-width: 450px;
  padding: 2rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

h1 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.error-message {
  background-color: #ffebee;
  color: #d32f2f;
  padding: 0.8rem;
  border-radius: 4px;
  margin-bottom: 1.5rem;
  text-align: center;
}

.form-group {
  margin-bottom: 1.5rem;
}

label {
  display: block;
  margin-bottom: 0.5rem;
  color: #555;
  font-weight: 500;
}

input[type="email"],
input[type="password"] {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

input[type="email"]:focus,
input[type="password"]:focus {
  border-color: #42b983;
  outline: none;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.remember-me {
  display: flex;
  align-items: center;
}

.remember-me input {
  margin-right: 0.5rem;
}

.forgot-password {
  color: #42b983;
  text-decoration: none;
  font-size: 0.9rem;
}

.login-button {
  width: 100%;
  padding: 12px;
  background-color: #42b983;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background-color 0.3s;
  margin-bottom: 1.5rem;
}

.login-button:hover {
  background-color: #3aa876;
}

.login-button:disabled {
  background-color: #a8d5c2;
  cursor: not-allowed;
}

.social-login {
  margin-top: 2rem;
  text-align: center;
}



/* .mode-toggle 관련된 스타일은 제거하거나 주석 처리 */
/*
.mode-toggle {
  margin-top: 1.5rem;
  text-align: center;
}

.mode-toggle button {
  background: none;
  border: none;
  color: #42b983;
  cursor: pointer;
  font-size: 0.95rem;
  text-decoration: underline;
}
*/

.register-link {
  margin-top: 1rem;
  text-align: center;
  color: #666;
}

.register-link a {
  color: #42b983;
  text-decoration: none;
  font-weight: 500;
}
</style>
