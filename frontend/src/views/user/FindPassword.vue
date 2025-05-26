<template>
  <div class="password-reset-container">
    <div class="password-reset-form">
      <h1>비밀번호 찾기</h1>
      
      <div class="description">
        <p>가입 시 사용한 닉네임과 이메일을 입력하시면<br>새로운 임시 비밀번호를 이메일로 발송해드립니다.</p>
      </div>

      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <div v-if="success" class="success-message">
        {{ success }}
      </div>

      <form @submit.prevent="findPassword" v-if="!isSuccess">
        <div class="form-group">
          <label for="nickname">닉네임</label>
          <input
            type="text"
            id="nickname"
            v-model="nickname"
            required
            placeholder="닉네임을 입력하세요"
          />
        </div>

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

        <button type="submit" class="reset-button" :disabled="loading">
          {{ loading ? "전송 중..." : "이메일로 새로운 비밀번호 받기" }}
        </button>
      </form>

      <!-- 성공 후 표시될 내용 -->
      <div v-if="isSuccess" class="success-actions">
        <div class="success-icon">✓</div>
        <p>새로운 임시 비밀번호가 이메일로 전송되었습니다.</p>
        <p class="notice">이메일을 확인하신 후 로그인해주세요.</p>
        <router-link to="/login" class="login-link-button">
          로그인 페이지로 이동
        </router-link>
      </div>

      <!-- 로그인 링크 -->
      <div class="login-link" v-if="!isSuccess">
        로그인 정보가 기억나셨나요?
        <router-link to="/login">
          로그인하기
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import apiGroup from '@/api/index'
// API 호출을 위한 import (실제 API 경로에 맞게 수정 필요)
// import api from "@/api/index";

export default {
  name: "PasswordResetView",
  setup() {
    const router = useRouter();

    const nickname = ref("");
    const email = ref("");
    const loading = ref(false);
    const error = ref(null);
    const success = ref(null);
    const isSuccess = ref(false);

    async function findPassword() {
      if (!nickname.value.trim() || !email.value.trim()) {
        error.value = "닉네임과 이메일을 모두 입력해주세요.";
        return;
      }

      loading.value = true;
      error.value = null;
      success.value = null;

      try {

        const response = await apiGroup.apiNoAuth({
          url: "/api/user/reset-password",
          method: "POST",
          data: {
            username: nickname.value,
            email: email.value,
          }
        })

        if (response.status === 200) {
          success.value = response.data.message || "새로운 임시 비밀번호가 이메일로 전송되었습니다.";
          isSuccess.value = true;
          console.log("[PasswordReset] Success:", response.data);
        }

      } catch (err) {
        console.error("Password reset error:", err);
        error.value = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
      } finally {
        loading.value = false;
      }
    }

    return {
      nickname,
      email,
      loading,
      error,
      success,
      isSuccess,
      findPassword
    };
  },
};
</script>

<style scoped>
.password-reset-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 2rem;
}

.password-reset-form {
  width: 100%;
  max-width: 450px;
  padding: 2rem;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

h1 {
  text-align: center;
  margin-bottom: 1rem;
  color: #333;
}

.description {
  text-align: center;
  margin-bottom: 2rem;
  color: #666;
  line-height: 1.5;
}

.description p {
  margin: 0;
  font-size: 0.9rem;
}

.error-message {
  background-color: #ffebee;
  color: #d32f2f;
  padding: 0.8rem;
  border-radius: 4px;
  margin-bottom: 1.5rem;
  text-align: center;
}

.success-message {
  background-color: #e8f5e8;
  color: #2e7d32;
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

input[type="text"],
input[type="email"] {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

input[type="text"]:focus,
input[type="email"]:focus {
  border-color: #42b983;
  outline: none;
}

.reset-button {
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

.reset-button:hover {
  background-color: #3aa876;
}

.reset-button:disabled {
  background-color: #a8d5c2;
  cursor: not-allowed;
}

.success-actions {
  text-align: center;
  padding: 1rem 0;
}

.success-icon {
  font-size: 3rem;
  color: #2e7d32;
  margin-bottom: 1rem;
}

.success-actions p {
  margin: 0.5rem 0;
  color: #333;
}

.notice {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 1.5rem !important;
}

.login-link-button {
  display: inline-block;
  padding: 10px 20px;
  background-color: #42b983;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.login-link-button:hover {
  background-color: #3aa876;
}

.login-link {
  text-align: center;
  color: #666;
  margin-top: 1rem;
}

.login-link a {
  color: #42b983;
  text-decoration: none;
  font-weight: 500;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>