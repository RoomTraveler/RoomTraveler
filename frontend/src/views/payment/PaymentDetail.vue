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
        <div class="status-banner mb-3" :class="getStatusBannerClass(payment.status)">
          <div class="status-icon">
            <i :class="getStatusIcon(payment.status)"></i>
          </div>
          <div class="status-text">
            <h4>{{ getStatusTitle(payment.status) }}</h4>
            <p>{{ getStatusDescription(payment.status) }}</p>
          </div>
        </div>

        <!-- 결제 정보 카드 -->
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
            <!-- 결제 취소 정보 -->
            <div v-if="['CANCELLED','REFUNDED'].includes(payment.status)" class="mt-3 p-3 bg-light rounded">
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

        <!-- 예약 정보 카드 -->
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

        <!-- 영수증/취소 버튼 -->
        <div class="d-flex justify-content-between mb-4">
          <button class="btn btn-outline-secondary" @click="downloadReceipt">
            <i class="bi bi-download me-2"></i>영수증 다운로드
          </button>
          <router-link
              v-if="payment.status === 'COMPLETED' && reservation.status === 'CONFIRMED'"
              :to="`/payment/cancel/${payment.paymentId}`"
              class="btn btn-danger"
          >
            결제 취소 요청
          </router-link>
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

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import Layout from '@/components/layout/Layout.vue';
import api from '@/api/index';

const props = defineProps({
  paymentId: {
    type: [String, Number],
    required: true,
  }
});
const router = useRouter();
const route = useRoute();

const loading = ref(true);
const message = ref('');
const error = ref('');
const payment = reactive({
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
});
const reservation = reactive({
  reservationId: null,
  checkInDate: null,
  checkOutDate: null,
  nights: 0,
  guestCount: 0,
  status: '',
  createdAt: null
});
const paymentHistory = ref([]);

// 마운트 시 데이터 불러오기
onMounted(async () => {
  if (route.query.message) message.value = route.query.message;
  await loadPaymentDetails();
});

// 결제 상세 정보, 이력 불러오기
async function loadPaymentDetails() {
  loading.value = true;
  error.value = '';
  message.value = route.query.message || ''; // 페이지 진입 시 메시지 다시 설정

  try {
    const response = await api.api.get(`/api/v1/payments/detail/${props.paymentId}`);
    const data = response.data;

    if (data) { // 백엔드에서 Payment 객체와 Reservation 객체를 함께 보내준다고 가정
      if (data.payment) {
        Object.assign(payment, data.payment);
      } else {
        // payment 정보가 없는 경우, props.paymentId만 사용하고 나머지는 기본값 유지
        payment.paymentId = props.paymentId;
        // 또는 에러 처리
        // error.value = '결제 정보를 찾을 수 없습니다.';
        // return;
      }
      if (data.reservation) {
        Object.assign(reservation, data.reservation);
      } else {
         // reservation 정보가 없는 경우 (예: 결제는 있으나 예약 정보 누락)
         // 필요시 에러 처리 또는 사용자 안내
      }
    } else {
      throw new Error('결제 상세 정보를 가져오는데 실패했습니다.');
    }

    // 결제 이력 불러오기 (API 예시 - 실제 API 엔드포인트 및 응답 형식에 맞게 수정 필요)
    // 현재는 결제 이력 API가 명확하지 않으므로 주석 처리
    /*
    try {
      const historyResponse = await api.api.get(`/api/v1/payments/${props.paymentId}/history`); // 예시 경로
      if (historyResponse.data) {
        paymentHistory.value = historyResponse.data;
      }
    } catch (historyError) {
      console.warn("결제 이력을 불러오는데 실패했습니다:", historyError);
      // 이력 로딩 실패는 전체 페이지 로딩을 막지 않도록 처리
    }
    */

  } catch (e) {
    console.error("Error loading payment details:", e);
    error.value = e.response?.data?.error || e.message || '결제 정보를 불러올 수 없습니다. 다시 시도해주세요.';
  } finally {
    loading.value = false;
  }
}

// 영수증 다운로드 (API 예시)
async function downloadReceipt() {
  try {
    // 실제 구현 필요
    // window.open(`/api/payment/receipt/${payment.paymentId}`); 등
    message.value = '영수증이 다운로드되었습니다.'; // 실제 구현시 수정
  } catch (e) {
    error.value = '영수증 다운로드에 실패했습니다. 다시 시도해주세요.';
  }
}

// 상태별 헬퍼 함수
const getStatusBannerClass = (status) => ({
  PENDING: 'status-banner-warning',
  COMPLETED: 'status-banner-success',
  CANCELLED: 'status-banner-danger',
  REFUNDED: 'status-banner-info',
  FAILED: 'status-banner-secondary'
})[status] || 'status-banner-secondary';

const getStatusIcon = (status) => ({
  PENDING: 'bi bi-hourglass-split',
  COMPLETED: 'bi bi-check-circle',
  CANCELLED: 'bi bi-x-circle',
  REFUNDED: 'bi bi-arrow-counterclockwise',
  FAILED: 'bi bi-exclamation-triangle'
})[status] || 'bi bi-question-circle';

const getStatusTitle = (status) => ({
  PENDING: '결제 대기 중',
  COMPLETED: '결제 완료',
  CANCELLED: '결제 취소됨',
  REFUNDED: '환불 완료',
  FAILED: '결제 실패'
})[status] || status;

const getStatusDescription = (status) => ({
  PENDING: '결제가 진행 중입니다. 잠시만 기다려주세요.',
  COMPLETED: '결제가 성공적으로 완료되었습니다.',
  CANCELLED: '결제가 취소되었습니다.',
  REFUNDED: '결제 금액이 환불되었습니다.',
  FAILED: '결제 처리 중 오류가 발생했습니다.'
})[status] || '';

// 기타 정보 표시 헬퍼
const getPaymentMethodName = (method) => ({
  CREDIT_CARD: '신용카드',
  BANK_TRANSFER: '계좌이체',
  VIRTUAL_ACCOUNT: '가상계좌',
  MOBILE_PAYMENT: '모바일결제',
  POINT: '포인트'
})[method] || method;

const getPaymentStatusName = (status) => ({
  PENDING: '대기중',
  COMPLETED: '완료',
  CANCELLED: '취소됨',
  REFUNDED: '환불됨',
  FAILED: '실패'
})[status] || status;

const getPaymentStatusClass = (status) => ({
  PENDING: 'badge bg-warning',
  COMPLETED: 'badge bg-success',
  CANCELLED: 'badge bg-danger',
  REFUNDED: 'badge bg-info',
  FAILED: 'badge bg-secondary'
})[status] || 'badge bg-secondary';

const getReservationStatusName = (status) => ({
  PENDING: '대기중',
  CONFIRMED: '확정',
  CANCELLED: '취소됨',
  COMPLETED: '완료',
  NO_SHOW: '노쇼'
})[status] || status;

const getReservationStatusClass = (status) => ({
  PENDING: 'badge bg-warning',
  CONFIRMED: 'badge bg-success',
  CANCELLED: 'badge bg-danger',
  COMPLETED: 'badge bg-info',
  NO_SHOW: 'badge bg-secondary'
})[status] || 'badge bg-secondary';

const getCancelReasonName = (reason) => ({
  SCHEDULE_CHANGE: '일정 변경',
  PERSONAL_REASON: '개인 사정',
  FOUND_BETTER_OPTION: '더 좋은 옵션 발견',
  WEATHER: '날씨 문제',
  OTHER: '기타'
})[reason] || reason;

// 결제 이력 관련
const getHistoryActionName = (action) => ({
  PAYMENT_CREATED: '결제 생성',
  PAYMENT_COMPLETED: '결제 완료',
  PAYMENT_FAILED: '결제 실패',
  CANCEL_REQUESTED: '취소 요청',
  CANCEL_APPROVED: '취소 승인',
  CANCEL_REJECTED: '취소 거부',
  REFUND_PROCESSED: '환불 처리'
})[action] || action;

const getHistoryIcon = (action) => ({
  PAYMENT_CREATED: 'bi bi-plus-circle',
  PAYMENT_COMPLETED: 'bi bi-check-circle',
  PAYMENT_FAILED: 'bi bi-x-circle',
  CANCEL_REQUESTED: 'bi bi-arrow-left-circle',
  CANCEL_APPROVED: 'bi bi-check-circle',
  CANCEL_REJECTED: 'bi bi-x-circle',
  REFUND_PROCESSED: 'bi bi-arrow-counterclockwise'
})[action] || 'bi bi-circle';

const getHistoryBadgeClass = (action) => ({
  PAYMENT_CREATED: 'bg-primary',
  PAYMENT_COMPLETED: 'bg-success',
  PAYMENT_FAILED: 'bg-danger',
  CANCEL_REQUESTED: 'bg-warning',
  CANCEL_APPROVED: 'bg-success',
  CANCEL_REJECTED: 'bg-danger',
  REFUND_PROCESSED: 'bg-info'
})[action] || 'bg-secondary';

// 날짜 및 금액 포맷
function formatDate(date) {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
function formatDateTime(date) {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}`;
}
function formatCurrency(amount) {
  return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 }).format(amount);
}
</script>

<style scoped>
.card {
  border: none;
  border-radius: 10px;
  box-shadow: 0 0.125rem 0.25rem rgba(0,0,0,0.075);
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
  color: white;
}
.status-banner-success { background-color: #28a745; }
.status-banner-warning { background-color: #ffc107; color: #212529; }
.status-banner-danger { background-color: #dc3545; }
.status-banner-info { background-color: #17a2b8; }
.status-banner-secondary { background-color: #6c757d; }
.status-icon { font-size: 2rem; margin-right: 20px; }
.status-text h4 { margin-bottom: 5px; }
.status-text p { margin-bottom: 0; }
.timeline { position: relative; padding-left: 40px; list-style: none; margin-bottom: 0; }
.timeline-item { position: relative; margin-bottom: 25px; }
.timeline-item:last-child { margin-bottom: 0; }
.timeline-badge { position: absolute; left: -40px; width: 30px; height: 30px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; }
.timeline-content { padding-left: 10px; }
.timeline:before { content: ''; position: absolute; top: 0; bottom: 0; left: -25px; width: 2px; background-color: #e9ecef; }
</style>
