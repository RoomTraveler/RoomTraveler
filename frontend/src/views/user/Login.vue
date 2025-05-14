<template>
  <div class="login-container">
    <div class="login-form">
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
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            required 
            placeholder="비밀번호를 입력하세요"
          />
        </div>
        
        <div class="form-options">
          <div class="remember-me">
            <input type="checkbox" id="remember" v-model="rememberMe" />
            <label for="remember">로그인 상태 유지</label>
          </div>
          <a href="#" class="forgot-password">비밀번호 찾기</a>
        </div>
        
        <button type="submit" class="login-button" :disabled="loading">
          {{ loading ? '로그인 중...' : '로그인' }}
        </button>
      </form>
      
      <div class="social-login">
        <p>또는 소셜 계정으로 로그인</p>
        <div class="social-buttons">
          <button class="social-button google">Google로 로그인</button>
          <button class="social-button kakao">카카오로 로그인</button>
          <button class="social-button naver">네이버로 로그인</button>
        </div>
      </div>
      
      <div class="register-link">
        계정이 없으신가요? <router-link to="/register">회원가입</router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginView',
  data() {
    return {
      email: '',
      password: '',
      rememberMe: false,
      loading: false,
      error: null
    };
  },
  methods: {
    login() {
      this.loading = true;
      this.error = null;
      
      // 실제 API 호출 대신 임시 로직 사용
      setTimeout(() => {
        // 간단한 유효성 검사 (실제로는 서버에서 처리)
        if (this.email === 'user@example.com' && this.password === 'password') {
          // 로그인 성공
          const user = {
            id: 1,
            email: this.email,
            name: '홍길동',
            role: 'USER'
          };
          
          // 로컬 스토리지에 사용자 정보 저장
          localStorage.setItem('user', JSON.stringify(user));
          
          // 리디렉션 처리
          const redirectPath = this.$route.query.redirect || '/';
          this.$router.push(redirectPath);
        } else {
          // 로그인 실패
          this.error = '이메일 또는 비밀번호가 올바르지 않습니다.';
        }
        
        this.loading = false;
      }, 1000);
    }
  },
  created() {
    // 이미 로그인된 사용자는 홈으로 리디렉션
    const user = localStorage.getItem('user');
    if (user) {
      this.$router.push('/');
    }
  }
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

.social-login p {
  color: #666;
  margin-bottom: 1rem;
  position: relative;
}

.social-login p::before,
.social-login p::after {
  content: "";
  position: absolute;
  top: 50%;
  width: 25%;
  height: 1px;
  background-color: #ddd;
}

.social-login p::before {
  left: 0;
}

.social-login p::after {
  right: 0;
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

.register-link {
  margin-top: 2rem;
  text-align: center;
  color: #666;
}

.register-link a {
  color: #42b983;
  text-decoration: none;
  font-weight: 500;
}
</style>