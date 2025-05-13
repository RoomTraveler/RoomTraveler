<template>
  <div class="container mt-5 mb-5">
    <div class="payment-history-container">
      <h2 class="mb-4">결제 내역</h2>
      
      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>
      
      <!-- 필터 섹션 -->
      <div class="filter-section">
        <div class="row g-3">
          <div class="col-md-3">
            <label for="startDate" class="form-label">시작일</label>
            <input type="date" class="form-control" id="startDate" v-model="filters.startDate">
          </div>
          <div class="col-md-3">
            <label for="endDate" class="form-label">종료일</label>
            <input type="date" class="form-control" id="endDate" v-model="filters.endDate">
          </div>
          <div class="col-md-3">
            <label for="status" class="form-label">결제 상태</label>
            <select class="form-select" id="status" v-model="filters.status">
              <option value="">전체</option>
              <option value="COMPLETED">결제 완료</option>
              <option value="PENDING">처리 중</option>
              <option value="CANCELLED">취소됨</option>
              <option value="FAILED">실패</option>
            </select>
          </div>
          <div class="col-md-3">
            <label for="paymentMethod" class="form-label">결제 방법</label>
            <select class="form-select" id="paymentMethod" v-model="filters.paymentMethod">
              <option value="">전체</option>
              <option value="CARD">신용카드</option>
              <option value="BANK_TRANSFER">계좌이체</option>
              <option value="PHONE">휴대폰 결제</option>
            </select>
          </div>
          <div class="col-12 d-flex justify-content-end">
            <button type="button" class="btn btn-primary" @click="searchPayments">
              <i class="bi bi-search"></i> 검색
            </button>
            <button type="button" class="btn btn-outline-secondary ms-2" @click="resetFilters">
              <i class="bi bi-arrow-clockwise"></i> 초기화
            </button>
          </div>
        </div>
      </div>
      
      <!-- 결제 내역 목록 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">결제 내역을 불러오는 중입니다...</p>
      </div>
      
      <div v-else-if="payments.length === 0" class="empty-state">
        <div class="empty-state-icon">
          <i class="bi bi-credit-card"></i>
        </div>
        <h4>결제 내역이 없습니다</h4>
        <p class="text-muted">아직 결제 내역이 없습니다. 숙소를 예약하고 결제해보세요.</p>
        <router-link to="/accommodation/list" class="btn btn-primary mt-3">
          <i class="bi bi-search"></i> 숙소 찾아보기
        </router-link>
      </div>
      
      <div v-else class="row">
        <div v-for="payment in payments" :key="payment.paymentId" class="col-md-6">
          <div class="card payment-card">
            <div class="payment-header">
              <div>
                <h5 class="mb-0">{{ payment.accommodationTitle }}</h5>
                <small class="text-muted">{{ payment.roomName }}</small>
              </div>
              <div>
                <span :class="getStatusBadgeClass(payment.status)">
                  {{ getStatusText(payment.status) }}
                </span>
              </div>
            </div>
            <div class="payment-body">
              <div class="row mb-2">
                <div class="col-md-6">
                  <p><strong>결제 번호:</strong> {{ payment.paymentId }}</p>
                  <p><strong>결제 방법:</strong> {{ getPaymentMethodText(payment.paymentMethod) }}</p>
                  <p><strong>결제 일시:</strong> {{ formatDate(payment.paidAt) }}</p>
                </div>
                <div class="col-md-6">
                  <p><strong>예약 번호:</strong> {{ payment.reservationId }}</p>
                  <p><strong>체크인:</strong> {{ formatDate(payment.reservation?.checkInDate) }}</p>
                  <p><strong>체크아웃:</strong> {{ formatDate(payment.reservation?.checkOutDate) }}</p>
                </div>
              </div>
            </div>
            <div class="payment-footer">
              <div class="payment-amount">
                {{ formatCurrency(payment.amount) }}
              </div>
              <div>
                <router-link :to="`/payment/detail/${payment.paymentId}`" class="btn btn-sm btn-outline-primary">
                  상세 보기
                </router-link>
                <router-link 
                  v-if="payment.status === 'COMPLETED'" 
                  :to="`/payment/cancel/${payment.paymentId}`" 
                  class="btn btn-sm btn-outline-danger"
                >
                  결제 취소
                </router-link>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PaymentHistory',
  data() {
    return {
      // 결제 내역 목록
      payments: [],
      // 로딩 상태
      loading: false,
      // 알림 메시지
      message: '',
      // 필터 조건
      filters: {
        startDate: '',
        endDate: '',
        status: '',
        paymentMethod: ''
      }
    };
  },
  created() {
    // 컴포넌트 생성 시 날짜 필터 초기화
    this.initDateFilters();
    
    // URL 쿼리 파라미터에서 필터 조건 가져오기
    const query = this.$route.query;
    if (query.startDate) this.filters.startDate = query.startDate;
    if (query.endDate) this.filters.endDate = query.endDate;
    if (query.status) this.filters.status = query.status;
    if (query.paymentMethod) this.filters.paymentMethod = query.paymentMethod;
    
    // 결제 내역 조회
    this.searchPayments();
  },
  methods: {
    // 결제 내역 조회
    async searchPayments() {
      this.loading = true;
      
      try {
        // URL 쿼리 파라미터 업데이트
        this.updateQueryParams();
        
        // API 호출
        const response = await fetch(`/api/payments/history?${this.getQueryString()}`);
        if (!response.ok) {
          throw new Error('결제 내역을 불러오는데 실패했습니다.');
        }
        
        this.payments = await response.json();
      } catch (error) {
        console.error('결제 내역 조회 중 오류가 발생했습니다:', error);
        this.message = '결제 내역을 불러오는데 실패했습니다.';
      } finally {
        this.loading = false;
      }
    },
    
    // 필터 초기화
    resetFilters() {
      this.initDateFilters();
      this.filters.status = '';
      this.filters.paymentMethod = '';
      this.searchPayments();
    },
    
    // 날짜 필터 초기화
    initDateFilters() {
      // 시작일: 3개월 전
      const threeMonthsAgo = new Date();
      threeMonthsAgo.setMonth(threeMonthsAgo.getMonth() - 3);
      this.filters.startDate = this.formatDateForInput(threeMonthsAgo);
      
      // 종료일: 오늘
      const today = new Date();
      this.filters.endDate = this.formatDateForInput(today);
    },
    
    // URL 쿼리 파라미터 업데이트
    updateQueryParams() {
      const query = {};
      if (this.filters.startDate) query.startDate = this.filters.startDate;
      if (this.filters.endDate) query.endDate = this.filters.endDate;
      if (this.filters.status) query.status = this.filters.status;
      if (this.filters.paymentMethod) query.paymentMethod = this.filters.paymentMethod;
      
      this.$router.replace({ query });
    },
    
    // 쿼리 스트링 생성
    getQueryString() {
      const params = new URLSearchParams();
      if (this.filters.startDate) params.append('startDate', this.filters.startDate);
      if (this.filters.endDate) params.append('endDate', this.filters.endDate);
      if (this.filters.status) params.append('status', this.filters.status);
      if (this.filters.paymentMethod) params.append('paymentMethod', this.filters.paymentMethod);
      
      return params.toString();
    },
    
    // 결제 상태에 따른 배지 클래스 반환
    getStatusBadgeClass(status) {
      switch (status) {
        case 'COMPLETED': return 'badge bg-success';
        case 'PENDING': return 'badge bg-warning text-dark';
        case 'FAILED': return 'badge bg-danger';
        case 'CANCELLED': return 'badge bg-secondary';
        default: return 'badge bg-info';
      }
    },
    
    // 결제 상태 텍스트 반환
    getStatusText(status) {
      switch (status) {
        case 'COMPLETED': return '결제 완료';
        case 'PENDING': return '처리 중';
        case 'FAILED': return '결제 실패';
        case 'CANCELLED': return '결제 취소';
        default: return status;
      }
    },
    
    // 결제 방법 텍스트 반환
    getPaymentMethodText(method) {
      switch (method) {
        case 'CARD': return '신용카드';
        case 'BANK_TRANSFER': return '계좌이체';
        case 'PHONE': return '휴대폰 결제';
        default: return method;
      }
    },
    
    // 날짜 포맷팅
    formatDate(dateString) {
      if (!dateString) return '-';
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).format(date);
    },
    
    // 날짜를 input[type=date]용으로 포맷팅
    formatDateForInput(date) {
      return date.toISOString().split('T')[0];
    },
    
    // 금액 포맷팅
    formatCurrency(amount) {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
        maximumFractionDigits: 0
      }).format(amount);
    }
  }
};
</script>

<style scoped>
.payment-history-container {
  max-width: 1000px;
  margin: 0 auto;
}
.payment-card {
  transition: transform 0.3s;
  margin-bottom: 20px;
  border-radius: 10px;
  overflow: hidden;
}
.payment-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background-color: #f8f9fa;
  border-bottom: 1px solid #dee2e6;
}
.payment-body {
  padding: 15px;
}
.payment-footer {
  padding: 15px;
  background-color: #f8f9fa;
  border-top: 1px solid #dee2e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.payment-amount {
  font-size: 1.2rem;
  font-weight: bold;
  color: #dc3545;
}
.filter-section {
  background-color: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}
.empty-state {
  text-align: center;
  padding: 50px 0;
}
.empty-state-icon {
  font-size: 4rem;
  color: #6c757d;
  margin-bottom: 20px;
}
</style>