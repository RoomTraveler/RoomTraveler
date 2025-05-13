<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">결제 취소 요청</h2>
      
      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>
      
      <!-- 에러 메시지 -->
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>
      
      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">결제 정보를 불러오는 중입니다...</p>
      </div>
      
      <div v-else>
        <!-- 결제 정보 -->
        <div class="card mb-4">
          <div class="card-header bg-primary text-white">
            <h5 class="mb-0">결제 정보</h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-6">
                <p><strong>결제 ID:</strong> {{ payment.paymentId }}</p>
                <p><strong>결제 방법:</strong> {{ getPaymentMethodName(payment.paymentMethod) }}</p>
                <p><strong>결제 상태:</strong> 
                  <span :class="getPaymentStatusClass(payment.status)">
                    {{ getPaymentStatusName(payment.status) }}
                  </span>
                </p>
                <p><strong>결제 금액:</strong> {{ formatCurrency(payment.amount) }}</p>
              </div>
              <div class="col-md-6">
                <p><strong>결제일:</strong> {{ formatDate(payment.paymentDate) }}</p>
                <p><strong>예약 ID:</strong> {{ payment.reservationId }}</p>
                <p><strong>숙소:</strong> {{ payment.accommodationTitle }}</p>
                <p><strong>객실:</strong> {{ payment.roomName }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 예약 정보 -->
        <div class="card mb-4">
          <div class="card-header bg-info text-white">
            <h5 class="mb-0">예약 정보</h5>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-6">
                <p><strong>체크인:</strong> {{ formatDate(reservation.checkInDate) }}</p>
                <p><strong>체크아웃:</strong> {{ formatDate(reservation.checkOutDate) }}</p>
                <p><strong>숙박 일수:</strong> {{ reservation.nights }}박</p>
              </div>
              <div class="col-md-6">
                <p><strong>게스트 수:</strong> {{ reservation.guestCount }}명</p>
                <p><strong>예약 상태:</strong> 
                  <span :class="getReservationStatusClass(reservation.status)">
                    {{ getReservationStatusName(reservation.status) }}
                  </span>
                </p>
                <p><strong>예약일:</strong> {{ formatDate(reservation.createdAt) }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 취소 정책 -->
        <div class="card mb-4">
          <div class="card-header bg-warning">
            <h5 class="mb-0">취소 정책</h5>
          </div>
          <div class="card-body">
            <div class="alert alert-warning">
              <h6 class="alert-heading">취소 수수료 안내</h6>
              <ul>
                <li>체크인 7일 전 취소: 전액 환불</li>
                <li>체크인 5-7일 전 취소: 결제 금액의 10% 차감 후 환불</li>
                <li>체크인 3-5일 전 취소: 결제 금액의 30% 차감 후 환불</li>
                <li>체크인 1-3일 전 취소: 결제 금액의 50% 차감 후 환불</li>
                <li>체크인 당일 취소: 환불 불가</li>
              </ul>
              <p class="mb-0">
                <strong>예상 환불 금액:</strong> {{ formatCurrency(refundAmount) }}
                <span v-if="refundAmount < payment.amount" class="text-danger">
                  (취소 수수료: {{ formatCurrency(payment.amount - refundAmount) }})
                </span>
              </p>
            </div>
          </div>
        </div>
        
        <!-- 취소 폼 -->
        <div class="card mb-4">
          <div class="card-header bg-danger text-white">
            <h5 class="mb-0">취소 요청</h5>
          </div>
          <div class="card-body">
            <form @submit.prevent="submitCancelRequest">
              <div class="mb-3">
                <label for="cancelReason" class="form-label">취소 사유</label>
                <select class="form-select" id="cancelReason" v-model="cancelRequest.reason" required>
                  <option value="">취소 사유 선택</option>
                  <option value="SCHEDULE_CHANGE">일정 변경</option>
                  <option value="PERSONAL_REASON">개인 사정</option>
                  <option value="FOUND_BETTER_OPTION">더 좋은 옵션 발견</option>
                  <option value="WEATHER">날씨 문제</option>
                  <option value="OTHER">기타</option>
                </select>
              </div>
              
              <div v-if="cancelRequest.reason === 'OTHER'" class="mb-3">
                <label for="otherReason" class="form-label">기타 사유 상세 설명</label>
                <textarea 
                  class="form-control" 
                  id="otherReason" 
                  v-model="cancelRequest.otherReason" 
                  rows="3" 
                  required
                  placeholder="취소 사유를 상세히 설명해주세요."
                ></textarea>
              </div>
              
              <div class="mb-3 form-check">
                <input 
                  type="checkbox" 
                  class="form-check-input" 
                  id="agreePolicy" 
                  v-model="cancelRequest.agreePolicy" 
                  required
                >
                <label class="form-check-label" for="agreePolicy">
                  취소 정책 및 환불 금액을 확인하였으며 이에 동의합니다.
                </label>
              </div>
              
              <div class="d-flex justify-content-between">
                <router-link :to="`/payment/detail/${payment.paymentId}`" class="btn btn-secondary">
                  취소
                </router-link>
                <button 
                  type="submit" 
                  class="btn btn-danger" 
                  :disabled="!cancelRequest.agreePolicy || isSubmitting"
                >
                  <span v-if="isSubmitting" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                  결제 취소 요청
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 결제 취소 요청 폼 컴포넌트
 * 
 * 이 컴포넌트는 사용자가 결제를 취소할 수 있는 폼을 제공합니다.
 * 결제 정보, 예약 정보, 취소 정책을 표시하고, 취소 사유를 입력받아 취소 요청을 처리합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'CancelForm',
  components: {
    Layout
  },
  props: {
    /**
     * 결제 ID
     */
    paymentId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      loading: true,
      isSubmitting: false,
      message: '',
      error: '',
      payment: {
        paymentId: null,
        paymentMethod: '',
        status: '',
        amount: 0,
        paymentDate: null,
        reservationId: null,
        accommodationTitle: '',
        roomName: ''
      },
      reservation: {
        reservationId: null,
        checkInDate: null,
        checkOutDate: null,
        nights: 0,
        guestCount: 0,
        status: '',
        createdAt: null
      },
      refundAmount: 0,
      cancelRequest: {
        reason: '',
        otherReason: '',
        agreePolicy: false
      }
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.user.isLoggedIn
    }),
    
    /**
     * 결제 ID (숫자 타입)
     */
    numericPaymentId() {
      return parseInt(this.paymentId);
    }
  },
  created() {
    // 로그인 상태 확인
    if (!this.isLoggedIn) {
      this.$router.push('/user/login');
      return;
    }
    
    // 결제 정보 로드
    this.loadPaymentDetails();
  },
  methods: {
    ...mapActions('payment', ['fetchPaymentDetails', 'cancelPayment']),
    
    /**
     * 결제 정보 로드
     */
    async loadPaymentDetails() {
      this.loading = true;
      
      try {
        const result = await this.fetchPaymentDetails(this.numericPaymentId);
        
        this.payment = result.payment;
        this.reservation = result.reservation;
        
        // 환불 금액 계산
        this.calculateRefundAmount();
        
        // 결제 상태 확인
        if (this.payment.status !== 'COMPLETED') {
          this.error = '취소할 수 있는 결제가 아닙니다.';
        }
        
        // 예약 상태 확인
        if (this.reservation.status !== 'CONFIRMED') {
          this.error = '이미 취소되었거나 완료된 예약입니다.';
        }
      } catch (error) {
        console.error('결제 정보를 불러오는 중 오류가 발생했습니다:', error);
        this.error = '결제 정보를 불러올 수 없습니다. 다시 시도해주세요.';
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 환불 금액 계산
     */
    calculateRefundAmount() {
      // 체크인 날짜와 현재 날짜의 차이 계산
      const checkInDate = new Date(this.reservation.checkInDate);
      const today = new Date();
      
      // 날짜 차이를 일 단위로 계산
      const diffTime = checkInDate.getTime() - today.getTime();
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      
      // 취소 수수료 정책에 따라 환불 금액 계산
      if (diffDays >= 7) {
        // 체크인 7일 전 취소: 전액 환불
        this.refundAmount = this.payment.amount;
      } else if (diffDays >= 5) {
        // 체크인 5-7일 전 취소: 결제 금액의 10% 차감 후 환불
        this.refundAmount = this.payment.amount * 0.9;
      } else if (diffDays >= 3) {
        // 체크인 3-5일 전 취소: 결제 금액의 30% 차감 후 환불
        this.refundAmount = this.payment.amount * 0.7;
      } else if (diffDays >= 1) {
        // 체크인 1-3일 전 취소: 결제 금액의 50% 차감 후 환불
        this.refundAmount = this.payment.amount * 0.5;
      } else {
        // 체크인 당일 취소: 환불 불가
        this.refundAmount = 0;
      }
      
      // 소수점 이하 반올림
      this.refundAmount = Math.round(this.refundAmount);
    },
    
    /**
     * 취소 요청 제출
     */
    async submitCancelRequest() {
      if (!this.cancelRequest.agreePolicy) {
        this.error = '취소 정책에 동의해주세요.';
        return;
      }
      
      if (this.cancelRequest.reason === 'OTHER' && !this.cancelRequest.otherReason) {
        this.error = '기타 사유를 입력해주세요.';
        return;
      }
      
      this.isSubmitting = true;
      
      try {
        // 취소 요청 데이터 준비
        const cancelData = {
          paymentId: this.numericPaymentId,
          reason: this.cancelRequest.reason,
          reasonDetail: this.cancelRequest.reason === 'OTHER' ? this.cancelRequest.otherReason : '',
          refundAmount: this.refundAmount
        };
        
        // 취소 요청 API 호출
        await this.cancelPayment(cancelData);
        
        // 성공 시 결제 상세 페이지로 이동
        this.$router.push({
          path: `/payment/detail/${this.numericPaymentId}`,
          query: { message: '결제 취소 요청이 성공적으로 처리되었습니다.' }
        });
      } catch (error) {
        console.error('결제 취소 요청 중 오류가 발생했습니다:', error);
        this.error = '결제 취소 요청에 실패했습니다. 다시 시도해주세요.';
        this.isSubmitting = false;
      }
    },
    
    /**
     * 결제 방법 이름 반환
     * @param {string} method - 결제 방법 코드
     * @returns {string} 결제 방법 이름
     */
    getPaymentMethodName(method) {
      const methods = {
        'CREDIT_CARD': '신용카드',
        'BANK_TRANSFER': '계좌이체',
        'VIRTUAL_ACCOUNT': '가상계좌',
        'MOBILE_PAYMENT': '모바일결제',
        'POINT': '포인트'
      };
      
      return methods[method] || method;
    },
    
    /**
     * 결제 상태 이름 반환
     * @param {string} status - 결제 상태 코드
     * @returns {string} 결제 상태 이름
     */
    getPaymentStatusName(status) {
      const statuses = {
        'PENDING': '대기중',
        'COMPLETED': '완료',
        'CANCELLED': '취소됨',
        'REFUNDED': '환불됨',
        'FAILED': '실패'
      };
      
      return statuses[status] || status;
    },
    
    /**
     * 결제 상태 배지 클래스 반환
     * @param {string} status - 결제 상태 코드
     * @returns {string} 배지 클래스
     */
    getPaymentStatusClass(status) {
      const classes = {
        'PENDING': 'badge bg-warning',
        'COMPLETED': 'badge bg-success',
        'CANCELLED': 'badge bg-danger',
        'REFUNDED': 'badge bg-info',
        'FAILED': 'badge bg-secondary'
      };
      
      return classes[status] || 'badge bg-secondary';
    },
    
    /**
     * 예약 상태 이름 반환
     * @param {string} status - 예약 상태 코드
     * @returns {string} 예약 상태 이름
     */
    getReservationStatusName(status) {
      const statuses = {
        'PENDING': '대기중',
        'CONFIRMED': '확정',
        'CANCELLED': '취소됨',
        'COMPLETED': '완료',
        'NO_SHOW': '노쇼'
      };
      
      return statuses[status] || status;
    },
    
    /**
     * 예약 상태 배지 클래스 반환
     * @param {string} status - 예약 상태 코드
     * @returns {string} 배지 클래스
     */
    getReservationStatusClass(status) {
      const classes = {
        'PENDING': 'badge bg-warning',
        'CONFIRMED': 'badge bg-success',
        'CANCELLED': 'badge bg-danger',
        'COMPLETED': 'badge bg-info',
        'NO_SHOW': 'badge bg-secondary'
      };
      
      return classes[status] || 'badge bg-secondary';
    },
    
    /**
     * 날짜 포맷팅 (YYYY-MM-DD)
     * @param {string|Date} date - 포맷팅할 날짜
     * @returns {string} 포맷팅된 날짜 문자열
     */
    formatDate(date) {
      if (!date) return '';
      
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      
      return `${year}-${month}-${day}`;
    },
    
    /**
     * 금액 포맷팅 (₩1,000,000 형식)
     * @param {number} amount - 포맷팅할 금액
     * @returns {string} 포맷팅된 금액 문자열
     */
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
.card {
  border: none;
  border-radius: 10px;
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  overflow: hidden;
}

.card-header {
  padding: 1rem;
  font-weight: 600;
}

.badge {
  font-weight: 500;
  padding: 0.35em 0.65em;
}

.alert-warning {
  background-color: #fff3cd;
  border-color: #ffecb5;
  color: #664d03;
}

.form-check-input:checked {
  background-color: #dc3545;
  border-color: #dc3545;
}
</style>