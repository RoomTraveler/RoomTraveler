<template>
  <div class="register-container">
    <div class="register-form">
      <h1>회원가입</h1>
      
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
      
      <form @submit.prevent="register">
        <div class="form-group">
          <label for="name">이름</label>
          <input 
            type="text" 
            id="name" 
            v-model="name" 
            required 
            placeholder="이름을 입력하세요"
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
        
        <div class="form-group">
          <label for="password">비밀번호</label>
          <input 
            type="password" 
            id="password" 
            v-model="password" 
            required 
            placeholder="비밀번호를 입력하세요"
          />
          <p class="password-hint">8자 이상, 영문, 숫자, 특수문자 포함</p>
        </div>
        
        <div class="form-group">
          <label for="confirmPassword">비밀번호 확인</label>
          <input 
            type="password" 
            id="confirmPassword" 
            v-model="confirmPassword" 
            required 
            placeholder="비밀번호를 다시 입력하세요"
          />
        </div>
        
        <div class="form-group">
          <label for="phone">전화번호</label>
          <input 
            type="tel" 
            id="phone" 
            v-model="phone" 
            placeholder="전화번호를 입력하세요 (선택사항)"
          />
        </div>
        
        <div class="agreement-section">
          <div class="agreement-item">
            <input type="checkbox" id="termsAgreement" v-model="termsAgreement" required />
            <label for="termsAgreement">
              <span class="required">(필수)</span> 이용약관에 동의합니다.
              <a href="#" @click.prevent="showTerms">약관 보기</a>
            </label>
          </div>
          
          <div class="agreement-item">
            <input type="checkbox" id="privacyAgreement" v-model="privacyAgreement" required />
            <label for="privacyAgreement">
              <span class="required">(필수)</span> 개인정보 처리방침에 동의합니다.
              <a href="#" @click.prevent="showPrivacyPolicy">약관 보기</a>
            </label>
          </div>
          
          <div class="agreement-item">
            <input type="checkbox" id="marketingAgreement" v-model="marketingAgreement" />
            <label for="marketingAgreement">
              <span class="optional">(선택)</span> 마케팅 정보 수신에 동의합니다.
            </label>
          </div>
        </div>
        
        <button type="submit" class="register-button" :disabled="loading || !isFormValid">
          {{ loading ? '처리 중...' : '회원가입' }}
        </button>
      </form>
      
      <div class="login-link">
        이미 계정이 있으신가요? <router-link to="/login">로그인</router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RegisterView',
  data() {
    return {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      phone: '',
      termsAgreement: false,
      privacyAgreement: false,
      marketingAgreement: false,
      loading: false,
      error: null
    };
  },
  computed: {
    isFormValid() {
      return (
        this.name.trim() !== '' &&
        this.email.trim() !== '' &&
        this.password.length >= 8 &&
        this.password === this.confirmPassword &&
        this.termsAgreement &&
        this.privacyAgreement
      );
    }
  },
  methods: {
    register() {
      if (!this.isFormValid) {
        return;
      }
      
      this.loading = true;
      this.error = null;
      
      // 비밀번호 유효성 검사
      const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/;
      if (!passwordRegex.test(this.password)) {
        this.error = '비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.';
        this.loading = false;
        return;
      }
      
      // 실제 API 호출 대신 임시 로직 사용
      setTimeout(() => {
        // 이메일 중복 체크 (실제로는 서버에서 처리)
        if (this.email === 'user@example.com') {
          this.error = '이미 사용 중인 이메일 주소입니다.';
          this.loading = false;
          return;
        }
        
        // 회원가입 성공
        alert('회원가입이 완료되었습니다. 로그인 페이지로 이동합니다.');
        this.$router.push('/login');
      }, 1000);
    },
    showTerms() {
      alert('이용약관 내용이 여기에 표시됩니다.');
    },
    showPrivacyPolicy() {
      alert('개인정보 처리방침 내용이 여기에 표시됩니다.');
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
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 80vh;
  padding: 2rem;
}

.register-form {
  width: 100%;
  max-width: 550px;
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

input[type="text"],
input[type="email"],
input[type="password"],
input[type="tel"] {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

input:focus {
  border-color: #42b983;
  outline: none;
}

.password-hint {
  margin-top: 0.5rem;
  font-size: 0.85rem;
  color: #666;
}

.agreement-section {
  margin-bottom: 1.5rem;
}

.agreement-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 0.8rem;
}

.agreement-item input {
  margin-top: 0.3rem;
  margin-right: 0.5rem;
}

.agreement-item label {
  font-weight: normal;
  margin-bottom: 0;
}

.agreement-item a {
  color: #42b983;
  text-decoration: none;
  margin-left: 0.5rem;
  font-size: 0.9rem;
}

.required {
  color: #d32f2f;
}

.optional {
  color: #757575;
}

.register-button {
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

.register-button:hover {
  background-color: #3aa876;
}

.register-button:disabled {
  background-color: #a8d5c2;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  color: #666;
}

.login-link a {
  color: #42b983;
  text-decoration: none;
  font-weight: 500;
}
</style>