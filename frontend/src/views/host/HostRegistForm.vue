<template>
  <div class="container mt-5 mb-5">
    <h2 class="text-center mb-4">호스트 등록</h2>
    
    <div class="form-container">
      <form @submit.prevent="submitForm">
        <div class="mb-3">
          <label for="businessName" class="form-label required-field">사업자명</label>
          <input 
            type="text" 
            class="form-control" 
            id="businessName" 
            v-model="formData.businessName" 
            required
          >
        </div>
        
        <div class="mb-3">
          <label for="businessRegNo" class="form-label required-field">사업자 등록번호</label>
          <input 
            type="text" 
            class="form-control" 
            id="businessRegNo" 
            v-model="formData.businessRegNo" 
            placeholder="000-00-00000" 
            @blur="validateBusinessRegNo"
            required
          >
          <div class="form-text">하이픈(-)을 포함하여 입력해주세요.</div>
        </div>
        
        <div class="mb-3">
          <label for="bankName" class="form-label required-field">은행명</label>
          <select 
            class="form-select" 
            id="bankName" 
            v-model="formData.bankName" 
            required
          >
            <option value="">은행을 선택하세요</option>
            <option value="KB국민은행">KB국민은행</option>
            <option value="신한은행">신한은행</option>
            <option value="우리은행">우리은행</option>
            <option value="하나은행">하나은행</option>
            <option value="농협은행">농협은행</option>
            <option value="기업은행">기업은행</option>
            <option value="SC제일은행">SC제일은행</option>
            <option value="카카오뱅크">카카오뱅크</option>
            <option value="토스뱅크">토스뱅크</option>
          </select>
        </div>
        
        <div class="mb-3">
          <label for="accountNumber" class="form-label required-field">계좌번호</label>
          <input 
            type="text" 
            class="form-control" 
            id="accountNumber" 
            v-model="formData.accountNumber" 
            placeholder="하이픈(-) 없이 입력해주세요" 
            required
          >
        </div>
        
        <div class="mb-3">
          <label for="accountHolder" class="form-label required-field">예금주</label>
          <input 
            type="text" 
            class="form-control" 
            id="accountHolder" 
            v-model="formData.accountHolder" 
            required
          >
        </div>
        
        <div class="mb-3">
          <label for="profileText" class="form-label">호스트 소개</label>
          <textarea 
            class="form-control" 
            id="profileText" 
            v-model="formData.profileText" 
            rows="4"
            placeholder="호스트로서 자신을 소개해주세요. (선택사항)"
          ></textarea>
        </div>
        
        <div class="alert alert-info">
          <p><strong>호스트 등록 안내:</strong></p>
          <ul>
            <li>호스트 등록 후 관리자 승인이 필요합니다.</li>
            <li>사업자 정보는 실제 등록된 정보와 일치해야 합니다.</li>
            <li>허위 정보 기재 시 호스트 자격이 박탈될 수 있습니다.</li>
          </ul>
        </div>
        
        <!-- 에러 메시지 영역 -->
        <div v-if="error" class="alert alert-danger" role="alert">
          {{ error }}
        </div>
        
        <div class="d-grid gap-2">
          <button type="submit" class="btn btn-primary" :disabled="loading">
            {{ loading ? '처리 중...' : '호스트 등록 신청' }}
          </button>
          <router-link to="/" class="btn btn-secondary">취소</router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HostRegistForm',
  data() {
    return {
      // 폼 데이터
      formData: {
        businessName: '',
        businessRegNo: '',
        bankName: '',
        accountNumber: '',
        accountHolder: '',
        profileText: ''
      },
      
      // 로딩 상태
      loading: false,
      
      // 에러 메시지
      error: ''
    };
  },
  methods: {
    // 사업자 등록번호 형식 검증
    validateBusinessRegNo() {
      const regex = /^\d{3}-\d{2}-\d{5}$/;
      if (!regex.test(this.formData.businessRegNo) && this.formData.businessRegNo !== '') {
        alert('사업자 등록번호 형식이 올바르지 않습니다. (예: 123-45-67890)');
        this.$nextTick(() => {
          document.getElementById('businessRegNo').focus();
        });
        return false;
      }
      return true;
    },
    
    // 폼 제출 처리
    async submitForm() {
      // 사업자 등록번호 형식 검증
      if (!this.validateBusinessRegNo()) {
        return;
      }
      
      this.loading = true;
      this.error = '';
      
      try {
        // 은행 계좌 정보 조합
        const bankAccount = `${this.formData.bankName} ${this.formData.accountNumber} (${this.formData.accountHolder})`;
        
        // API 호출
        const response = await fetch('/api/hosts/register', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            businessName: this.formData.businessName,
            businessRegNo: this.formData.businessRegNo,
            bankAccount: bankAccount,
            profileText: this.formData.profileText
          })
        });
        
        if (!response.ok) {
          // 등록 실패
          const errorData = await response.json();
          throw new Error(errorData.message || '호스트 등록에 실패했습니다.');
        }
        
        // 등록 성공
        this.$router.push({
          path: '/host/index',
          query: { message: '호스트 등록 신청이 완료되었습니다. 관리자 승인 후 호스트 기능을 이용하실 수 있습니다.' }
        });
      } catch (error) {
        console.error('호스트 등록 중 오류가 발생했습니다:', error);
        this.error = error.message || '호스트 등록 중 오류가 발생했습니다. 다시 시도해주세요.';
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.form-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0,0,0,0.1);
}
.required-field::after {
  content: " *";
  color: red;
}
</style>