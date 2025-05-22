<template>
  <div class="container mt-5 mb-5">
    <div class="payment-result-container text-center">
      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>

      <div class="payment-success-icon">
        <i class="bi bi-check-circle-fill"></i>
      </div>
      <h2 class="mb-4">결제가 완료되었습니다</h2>
      <p class="lead mb-5">예약이 확정되었습니다. 아래 결제 정보를 확인해주세요.</p>

      <!-- 결제 정보 -->
      <div class="payment-info-card text-start">
        <h4 class="mb-4">결제 정보</h4>
        <div class="payment-detail-row">
          <span>결제 번호</span>
          <span>{{ payment.paymentId }}</span>
        </div>
        <div class="payment-detail-row">
          <span>결제 일시</span>
          <span>{{ formatDate(payment.paidAt) }}</span>
        </div>
        <div class="payment-detail-row">
          <span>결제 방법</span>
          <span>{{ getPaymentMethodText(payment.paymentMethod) }}</span>
        </div>
        <div class="payment-detail-row">
          <span>결제 상태</span>
          <span>
            <span :class="getStatusBadgeClass(payment.status)">
              {{ getStatusText(payment.status) }}
            </span>
          </span>
        </div>
        <div class="payment-detail-row">
          <span>결제 금액</span>
          <span class="total-amount">{{ formatCurrency(payment.amount) }}</span>
        </div>
      </div>

      <!-- 예약 정보 -->
      <div class="payment-info-card text-start">
        <h4 class="mb-4">예약 정보</h4>
        <div class="payment-detail-row">
          <span>예약 번호</span>
          <span>{{ payment.reservationId }}</span>
        </div>
        <div class="payment-detail-row">
          <span>숙소</span>
          <span>{{ payment.accommodationTitle }}</span>
        </div>
        <div class="payment-detail-row">
          <span>객실</span>
          <span>{{ payment.roomName }}</span>
        </div>
        <div class="payment-detail-row">
          <span>체크인</span>
          <span>{{ formatDate(reservation.checkInDate, 'date-only') }}</span>
        </div>
        <div class="payment-detail-row">
          <span>체크아웃</span>
          <span>{{ formatDate(reservation.checkOutDate, 'date-only') }}</span>
        </div>
        <div class="payment-detail-row">
          <span>인원</span>
          <span>{{ reservation.guestCount }}명</span>
        </div>
      </div>

      <!-- 버튼 영역 -->
      <div class="action-buttons d-flex flex-wrap gap-2 justify-content-center">
        <router-link :to="`/reservation/detail/${payment.reservationId}`" class="btn btn-primary">예약 상세 보기</router-link>
        <router-link to="/payment/history" class="btn btn-outline-secondary">결제 내역 보기</router-link>
        <router-link to="/" class="btn btn-outline-dark">홈으로</router-link>
      </div>

      <!-- 알림 영역 -->
      <div class="mt-5">
        <div class="alert alert-info" role="alert">
          <h5 class="alert-heading mb-2"><i class="bi bi-info-circle"></i> 알림</h5>
          <p>결제 내역은 이메일(<strong>{{ userEmail }}</strong>)로도 발송되었습니다.</p>
          <p>예약 확정 및 취소 관련 문의는 고객센터(1234-5678)로 연락해주세요.</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRoute } from 'vue-router';

const payment = ref({
  paymentId: '',
  paidAt: null,
  paymentMethod: '',
  status: '',
  amount: 0,
  reservationId: '',
  accommodationTitle: '',
  roomName: ''
});
const reservation = ref({
  checkInDate: null,
  checkOutDate: null,
  guestCount: 0
});
const userEmail = ref('');
const message = ref('');

const route = useRoute();

// 결제 결과 불러오기
async function loadPaymentResult(paymentId) {
  try {
    const response = await fetch(`/api/payments/${paymentId}`);
    if (!response.ok) throw new Error('결제 정보를 불러오는데 실패했습니다.');
    const data = await response.json();
    payment.value = data.payment;
    reservation.value = data.reservation;
    userEmail.value = data.userEmail;

    if (route.query.message) message.value = route.query.message;
  } catch (error) {
    console.error('결제 결과 조회 중 오류:', error);
    message.value = '결제 정보를 불러오는데 실패했습니다.';
  }
}

// 컴포넌트 마운트 시 실행
if (route.params.id) {
  loadPaymentResult(route.params.id);
} else {
  message.value = '결제 정보를 찾을 수 없습니다.';
}

// 헬퍼 메서드
function getStatusBadgeClass(status) {
  switch (status) {
    case 'COMPLETED': return 'badge bg-success';
    case 'PENDING': return 'badge bg-warning text-dark';
    case 'FAILED': return 'badge bg-danger';
    case 'CANCELLED': return 'badge bg-secondary';
    default: return 'badge bg-info';
  }
}
function getStatusText(status) {
  switch (status) {
    case 'COMPLETED': return '결제 완료';
    case 'PENDING': return '처리 중';
    case 'FAILED': return '결제 실패';
    case 'CANCELLED': return '결제 취소';
    default: return status;
  }
}
function getPaymentMethodText(method) {
  switch (method) {
    case 'CARD': return '신용카드';
    case 'BANK_TRANSFER': return '계좌이체';
    case 'PHONE': return '휴대폰 결제';
    default: return method;
  }
}
function formatDate(dateString, format = 'full') {
  if (!dateString) return '-';
  const date = new Date(dateString);
  if (format === 'date-only') {
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric', month: '2-digit', day: '2-digit'
    }).format(date);
  }
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  }).format(date);
}
function formatCurrency(amount) {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency', currency: 'KRW', maximumFractionDigits: 0
  }).format(amount);
}
</script>

<style scoped>
.payment-result-container {
  max-width: 700px;
  margin: 0 auto;
}
.payment-success-icon {
  font-size: 5rem;
  color: #198754;
  margin-bottom: 20px;
}
.payment-info-card {
  background-color: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}
.payment-detail-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #dee2e6;
}
.payment-detail-row:last-child {
  border-bottom: none;
}
.total-amount {
  font-size: 1.5rem;
  font-weight: bold;
  color: #dc3545;
}
.action-buttons {
  margin-top: 30px;
}
</style>
