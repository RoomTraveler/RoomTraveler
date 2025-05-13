<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">결제 상세 정보</h2>

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
        <!-- 결제 상태 배너 -->
        <div class="status-banner" :class="getStatusBannerClass(payment.status)">
          <div class="status-icon">
            <i :class="getStatusIcon(payment.status)"></i>
          </div>
          <div class="status-text">
            <h4>{{ getStatusTitle(payment.status) }}</h4>
            <p>{{ getStatusDescription(payment.status) }}</p>
          </div>
        </div>

        <!-- 결제 정보 -->
        <div class="card mb-4">
          <div class="card-header">
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
                <p><strong>결제일:</strong> {{ formatDateTime(payment.paymentDate) }}</p>
                <p><strong>예약 ID:</strong> {{ payment.reservationId }}</p>
                <p><strong>숙소:</strong> {{ payment.accommodationTitle }}</p>
                <p><strong>객실:</strong> {{ payment.roomName }}</p>
              </div>
            </div>

            <!-- 결제 취소 정보 (취소된 경우) -->
            <div v-if="payment.status === 'CANCELLED' || payment.status === 'REFUNDED'" class="mt-3 p-3 bg-light rounded">
              <h6>취소 정보</h6>
              <div class="row">
                <div class="col-md-6">
                  <p><strong>취소일:</strong> {{ formatDateTime(payment.cancelledAt) }}</p>
                  <p><strong>취소 사유:</strong> {{ getCancelReasonName(payment.cancelReason) }}</p>
                </div>
                <div class="col-md-6">
                  <p><strong>환불 금액:</strong> {{ formatCurrency(payment.refundAmount) }}</p>
                  <p v-if="payment.refundAmount < payment.amount">
                    <strong>취소 수수료:</strong> 
                    <span class="text-danger">{{ formatCurrency(payment.amount - payment.refundAmount) }}</span>
                  </p>
                </div>
              </div>
              <p v-if="payment.cancelReasonDetail"><strong>상세 사유:</strong> {{ payment.cancelReasonDetail }}</p>
            </div>
          </div>
        </div>

        <!-- 예약 정보 -->
        <div class="card mb-4">
          <div class="card-header">
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
                <p><strong>예약일:</strong> {{ formatDateTime(reservation.createdAt) }}</p>
              </div>
            </div>

            <div class="mt-3">
              <router-link :to="`/reservation/detail/${reservation.reservationId}`" class="btn btn-outline-primary">
                예약 상세 보기
              </router-link>
            </div>
          </div>
        </div>

        <!-- 결제 상세 내역 -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">결제 상세 내역</h5>
          </div>
          <div class="card-body">
            <div class="table-responsive">
              <table class="table">
                <thead>
                  <tr>
                    <th>항목</th>
                    <th class="text-end">금액</th>
                  </tr>
                </thead>
                <tbody>
                  <tr>
                    <td>객실 요금 ({{ reservation.nights }}박)</td>
                    <td class="text-end">{{ formatCurrency(payment.roomCharge) }}</td>
                  </tr>
                  <tr v-if="payment.taxAmount > 0">
                    <td>세금</td>
                    <td class="text-end">{{ formatCurrency(payment.taxAmount) }}</td>
                  </tr>
                  <tr v-if="payment.serviceCharge > 0">
                    <td>서비스 수수료</td>
                    <td class="text-end">{{ formatCurrency(payment.serviceCharge) }}</td>
                  </tr>
                  <tr v-if="payment.discountAmount > 0">
                    <td>할인</td>
                    <td class="text-end text-danger">-{{ formatCurrency(payment.discountAmount) }}</td>
                  </tr>
                  <tr class="table-active fw-bold">
                    <td>총 결제 금액</td>
                    <td class="text-end">{{ formatCurrency(payment.amount) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- 영수증 및 취소 버튼 -->
        <div class="d-flex justify-content-between mb-4">
          <button class="btn btn-outline-secondary" @click="downloadReceipt">
            <i class="bi bi-download me-2"></i>영수증 다운로드
          </button>

          <div>
            <router-link 
              v-if="payment.status === 'COMPLETED' && reservation.status === 'CONFIRMED'" 
              :to="`/payment/cancel/${payment.paymentId}`" 
              class="btn btn-danger"
            >
              결제 취소 요청
            </router-link>
          </div>
        </div>

        <!-- 결제 이력 -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="mb-0">결제 이력</h5>
          </div>
          <div class="card-body">
            <div v-if="paymentHistory.length > 0">
              <ul class="timeline">
                <li v-for="(history, index) in paymentHistory" :key="index" class="timeline-item">
                  <div class="timeline-badge" :class="getHistoryBadgeClass(history.action)">
                    <i :class="getHistoryIcon(history.action)"></i>
                  </div>
                  <div class="timeline-content">
                    <h6 class="mb-1">{{ getHistoryActionName(history.action) }}</h6>
                    <p class="text-muted mb-0">{{ formatDateTime(history.timestamp) }}</p>
                    <p v-if="history.description" class="mb-0">{{ history.description }}</p>
                  </div>
                </li>
              </ul>
            </div>
            <div v-else class="text-center py-3">
              <p>결제 이력이 없습니다.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 결제 상세 정보 컴포넌트
 * 
 * 이 컴포넌트는 결제 상세 정보를 표시합니다.
 * 결제 정보, 예약 정보, 결제 상세 내역, 결제 이력을 제공하며,
 * 결제 상태에 따라 영수증 다운로드 및 결제 취소 기능을 제공합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'PaymentDetail',
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
      message: '',
      error: '',
      payment: {
        paymentId: null,
        paymentMethod: '',
        status: '',
        amount: 0,
        roomCharge: 0,
        taxAmount: 0,
        serviceCharge: 0,
        discountAmount: 0,
        paymentDate: null,
        cancelledAt: null,
        cancelReason: '',
        cancelReasonDetail: '',
        refundAmount: 0,
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
      paymentHistory: []
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

    // URL 쿼리 파라미터에서 메시지 가져오기
    if (this.$route.query.message) {
      this.message = this.$route.query.message;
    }

    // 결제 정보 로드
    this.loadPaymentDetails();
  },
  methods: {
    ...mapActions('payment', ['fetchPaymentDetails', 'fetchPaymentHistory', 'downloadPaymentReceipt']),

    /**
     * 결제 정보 로드
     */
    async loadPaymentDetails() {
      this.loading = true;

      try {
        // 결제 상세 정보 가져오기
        const result = await this.fetchPaymentDetails(this.numericPaymentId);

        this.payment = result.payment;
        this.reservation = result.reservation;

        // 결제 이력 가져오기
        this.paymentHistory = await this.fetchPaymentHistory(this.numericPaymentId);
      } catch (error) {
        console.error('결제 정보를 불러오는 중 오류가 발생했습니다:', error);
        this.error = '결제 정보를 불러올 수 없습니다. 다시 시도해주세요.';
      } finally {
        this.loading = false;
      }
    },

    /**
     * 영수증 다운로드
     */
    async downloadReceipt() {
      try {
        await this.downloadPaymentReceipt(this.numericPaymentId);
        this.message = '영수증이 다운로드되었습니다.';
      } catch (error) {
        console.error('영수증 다운로드 중 오류가 발생했습니다:', error);
        this.error = '영수증 다운로드에 실패했습니다. 다시 시도해주세요.';
      }
    },

    /**
     * 결제 상태에 따른 배너 클래스 반환
     * @param {string} status - 결제 상태 코드
     * @returns {string} 배너 클래스
     */
    getStatusBannerClass(status) {
      const classes = {
        'PENDING': 'status-banner-warning',
        'COMPLETED': 'status-banner-success',
        'CANCELLED': 'status-banner-danger',
        'REFUNDED': 'status-banner-info',
        'FAILED': 'status-banner-secondary'
      };

      return classes[status] || 'status-banner-secondary';
    },

    /**
     * 결제 상태에 따른 아이콘 클래스 반환
     * @param {string} status - 결제 상태 코드
     * @returns {string} 아이콘 클래스
     */
    getStatusIcon(status) {
      const icons = {
        'PENDING': 'bi bi-hourglass-split',
        'COMPLETED': 'bi bi-check-circle',
        'CANCELLED': 'bi bi-x-circle',
        'REFUNDED': 'bi bi-arrow-counterclockwise',
        'FAILED': 'bi bi-exclamation-triangle'
      };

      return icons[status] || 'bi bi-question-circle';
    },

    /**
     * 결제 상태에 따른 제목 반환
     * @param {string} status - 결제 상태 코드
     * @returns {string} 상태 제목
     */
    getStatusTitle(status) {
      const titles = {
        'PENDING': '결제 대기 중',
        'COMPLETED': '결제 완료',
        'CANCELLED': '결제 취소됨',
        'REFUNDED': '환불 완료',
        'FAILED': '결제 실패'
      };

      return titles[status] || status;
    },

    /**
     * 결제 상태에 따른 설명 반환
     * @param {string} status - 결제 상태 코드
     * @returns {string} 상태 설명
     */
    getStatusDescription(status) {
      const descriptions = {
        'PENDING': '결제가 진행 중입니다. 잠시만 기다려주세요.',
        'COMPLETED': '결제가 성공적으로 완료되었습니다.',
        'CANCELLED': '결제가 취소되었습니다.',
        'REFUNDED': '결제 금액이 환불되었습니다.',
        'FAILED': '결제 처리 중 오류가 발생했습니다.'
      };

      return descriptions[status] || '';
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
     * 취소 사유 이름 반환
     * @param {string} reason - 취소 사유 코드
     * @returns {string} 취소 사유 이름
     */
    getCancelReasonName(reason) {
      const reasons = {
        'SCHEDULE_CHANGE': '일정 변경',
        'PERSONAL_REASON': '개인 사정',
        'FOUND_BETTER_OPTION': '더 좋은 옵션 발견',
        'WEATHER': '날씨 문제',
        'OTHER': '기타'
      };

      return reasons[reason] || reason;
    },

    /**
     * 결제 이력 액션 이름 반환
     * @param {string} action - 이력 액션 코드
     * @returns {string} 이력 액션 이름
     */
    getHistoryActionName(action) {
      const actions = {
        'PAYMENT_CREATED': '결제 생성',
        'PAYMENT_COMPLETED': '결제 완료',
        'PAYMENT_FAILED': '결제 실패',
        'CANCEL_REQUESTED': '취소 요청',
        'CANCEL_APPROVED': '취소 승인',
        'CANCEL_REJECTED': '취소 거부',
        'REFUND_PROCESSED': '환불 처리'
      };

      return actions[action] || action;
    },

    /**
     * 결제 이력 액션 아이콘 클래스 반환
     * @param {string} action - 이력 액션 코드
     * @returns {string} 아이콘 클래스
     */
    getHistoryIcon(action) {
      const icons = {
        'PAYMENT_CREATED': 'bi bi-plus-circle',
        'PAYMENT_COMPLETED': 'bi bi-check-circle',
        'PAYMENT_FAILED': 'bi bi-x-circle',
        'CANCEL_REQUESTED': 'bi bi-arrow-left-circle',
        'CANCEL_APPROVED': 'bi bi-check-circle',
        'CANCEL_REJECTED': 'bi bi-x-circle',
        'REFUND_PROCESSED': 'bi bi-arrow-counterclockwise'
      };

      return icons[action] || 'bi bi-circle';
    },

    /**
     * 결제 이력 배지 클래스 반환
     * @param {string} action - 이력 액션 코드
     * @returns {string} 배지 클래스
     */
    getHistoryBadgeClass(action) {
      const classes = {
        'PAYMENT_CREATED': 'bg-primary',
        'PAYMENT_COMPLETED': 'bg-success',
        'PAYMENT_FAILED': 'bg-danger',
        'CANCEL_REQUESTED': 'bg-warning',
        'CANCEL_APPROVED': 'bg-success',
        'CANCEL_REJECTED': 'bg-danger',
        'REFUND_PROCESSED': 'bg-info'
      };

      return classes[action] || 'bg-secondary';
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
     * 날짜 및 시간 포맷팅 (YYYY-MM-DD HH:MM)
     * @param {string|Date} date - 포맷팅할 날짜
     * @returns {string} 포맷팅된 날짜 및 시간 문자열
     */
    formatDateTime(date) {
      if (!date) return '';

      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      const hours = String(d.getHours()).padStart(2, '0');
      const minutes = String(d.getMinutes()).padStart(2, '0');

      return `${year}-${month}-${day} ${hours}:${minutes}`;
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
  background-color: #f8f9fa;
  border-bottom: none;
}

.badge {
  font-weight: 500;
  padding: 0.35em 0.65em;
}

.status-banner {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
  color: white;
}

.status-banner-success {
  background-color: #28a745;
}

.status-banner-warning {
  background-color: #ffc107;
  color: #212529;
}

.status-banner-danger {
  background-color: #dc3545;
}

.status-banner-info {
  background-color: #17a2b8;
}

.status-banner-secondary {
  background-color: #6c757d;
}

.status-icon {
  font-size: 2rem;
  margin-right: 20px;
}

.status-text h4 {
  margin-bottom: 5px;
}

.status-text p {
  margin-bottom: 0;
}

.timeline {
  position: relative;
  padding-left: 40px;
  list-style: none;
  margin-bottom: 0;
}

.timeline-item {
  position: relative;
  margin-bottom: 25px;
}

.timeline-item:last-child {
  margin-bottom: 0;
}

.timeline-badge {
  position: absolute;
  left: -40px;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.timeline-content {
  padding-left: 10px;
}

.timeline:before {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  left: -25px;
  width: 2px;
  background-color: #e9ecef;
}
</style>
