<template>
  <div class="container">
    <div class="login-container">
      <div class="login-header">
        <h2>호스트 로그인</h2>
        <p>숙소 관리를 위해 로그인해주세요</p>
      </div>
      
      <form @submit.prevent="login">
        <div class="form-group">
          <label for="email" class="form-label">이메일</label>
          <input 
            type="email" 
            id="email" 
            v-model="email" 
            class="form-control" 
            required 
          />
        </div>
        
        <div class="form-group">
          <label for="password" class="form-label">비밀번호</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            class="form-control" 
            required 
          />
        </div>
        
        <div class="d-grid gap-2">
          <button type="submit" class="btn btn-host" :disabled="loading">
            {{ loading ? '로그인 중...' : '로그인' }}
          </button>
        </div>
      </form>
      
      <!-- 에러 메시지 영역 -->
      <div v-if="error" class="alert alert-danger mt-3" role="alert">
        {{ error }}
      </div>
      
      <div class="text-center mt-4">
        <p>아직 호스트 계정이 없으신가요? <router-link to="/host/regist-user-form">호스트로 가입하기</router-link></p>
        <p><router-link to="/user/login-form">일반 사용자 로그인</router-link></p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HostLoginForm',
  data() {
    return {
      // 로그인 폼 데이터
      email: '',
      password: '',
      
      // 로딩 상태
      loading: false,
      
      // 에러 메시지
      error: ''
    };
  },
  created() {
    // URL 쿼리 파라미터에서 에러 메시지 가져오기
    if (this.$route.query.error) {
      this.error = this.$route.query.error;
    }
  },
  methods: {
    // 로그인 처리
    async login() {
      // 입력값 검증
      if (!this.email || !this.password) {
        this.error = '이메일과 비밀번호를 모두 입력해주세요.';
        return;
      }
      
      this.loading = true;
      this.error = '';
      
      try {
        // API 호출
        const response = await fetch('/api/host/login', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            email: this.email,
            password: this.password
          })
        });
        
        if (!response.ok) {
          // 로그인 실패
          const errorData = await response.json();
          throw new Error(errorData.message || '로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.');
        }
        
        // 로그인 성공
        const data = await response.json();
        
        // 로컬 스토리지에 토큰 저장 (필요한 경우)
        if (data.token) {
          localStorage.setItem('token', data.token);
        }
        
        // 호스트 대시보드 페이지로 이동
        this.$router.push('/host/dashboard');
      } catch (error) {
        console.error('로그인 중 오류가 발생했습니다:', error);
        this.error = error.message || '로그인 중 오류가 발생했습니다. 다시 시도해주세요.';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.login-container {
  max-width: 500px;
  margin: 50px auto;
  padding: 30px;
  background-color: #f8f9fa;
  border-radius: 10px;
  box-shadow: 0 0 15px rgba(0,0,0,0.1);
}
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.login-header h2 {
  color: #343a40;
}
.login-header p {
  color: #6c757d;
}
.form-group {
  margin-bottom: 20px;
}
.btn-host {
  background-color: #28a745;
  color: white;
}
.btn-host:hover {
  background-color: #218838;
  color: white;
}
</style>