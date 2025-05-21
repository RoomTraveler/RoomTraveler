<template>
  <div class="login-container">
    <div class="login-form">
      <!-- 동적 타이틀 -->
      <h1>{{ isHostMode ? "호스트 회원 로그인" : "로그인" }}</h1>

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
            :placeholder="isHostMode ? '호스트 이메일을 입력하세요' : '이메일 주소를 입력하세요'"
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
          {{
            loading ? (isHostMode ? "호스트 로그인 중..." : "로그인 중...") : isHostMode ? "호스트 로그인" : "로그인"
          }}
        </button>
      </form>

      <!-- 기본 모드에서만 소셜 로그인 보여줌 -->
      <div v-if="!isHostMode" class="social-login">
        <p>또는 소셜 계정으로 로그인</p>
        <div class="social-buttons">
          <button class="social-button google">Google로 로그인</button>
          <button class="social-button kakao">카카오로 로그인</button>
          <button class="social-button naver">네이버로 로그인</button>
        </div>
      </div>

      <!-- 회원가입 링크 -->
      <div class="register-link">
        계정이 없으신가요?
        <router-link :to="isHostMode ? '/host/register' : '/register'">
          {{ isHostMode ? "비즈니스 회원가입" : "회원가입" }}
        </router-link>
      </div>

      <!-- 모드 토글 버튼 -->
      <div class="mode-toggle">
        <button type="button" @click="toggleMode">
          {{ isHostMode ? "일반 로그인/회원가입" : "호스트 로그인/회원가입" }}
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import api from "@/api/index";
import { useUserStore } from "@/store/userStore";

export default {
  name: "LoginView",
  data() {
    return {
      email: "",
      password: "",
      rememberMe: false,
      loading: false,
      error: null,
      isHostMode: false,
    };
  },
  methods: {
    async login() {
      this.loading = true;
      this.error = null;

      const useStore = useUserStore();
      useStore.login(this.email, this.password);

      const redirectPath = this.$route.query.redirect || (this.isHostMode ? "/host" : "/");
      this.$router.push(redirectPath);
    },
    toggleMode() {
      this.isHostMode = !this.isHostMode;
      this.error = null;
      this.email = "";
      this.password = "";
    },
  },
  created() {
    const key = this.isHostMode ? "host" : "user";
    if (localStorage.getItem(key)) {
      this.$router.push(this.isHostMode ? "/host" : "/");
    }
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

.social-buttons {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}

.social-button {
  padding: 10px;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  cursor: pointer;
  transition: opacity 0.3s;
}

.social-button:hover {
  opacity: 0.9;
}

.google {
  background-color: #fff;
  color: #444;
  border: 1px solid #ddd;
}

.kakao {
  background-color: #fee500;
  color: #000;
}

.naver {
  background-color: #03c75a;
  color: white;
}

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
