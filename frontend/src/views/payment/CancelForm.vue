<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">결제 취소 요청</h2>

      <!-- 성공 메시지 -->
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
        <!-- 결제 정보 카드 -->
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

        <!-- 예약 정보 카드 -->
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
              <ul class="mb-2">
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

        <!-- 취소 요청 폼 -->
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

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import Layout from '@/components/layout/Layout.vue';
import api from '@/api/index';

const props = defineProps({
  paymentId: {
    type: [String, Number],
    required: true,
  },
});

// 상태 정의
const loading = ref(true);
const isSubmitting = ref(false);
const message = ref('');
const error = ref('');

// 결제/예약/환불 데이터
const payment = reactive({
  paymentId: null,
  paymentMethod: '',
  status: '',
  amount: 0,
  paymentDate: null,
  reservationId: null,
  accommodationTitle: '',
  roomName: '',
  paymentKey: null,
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
const refundAmount = ref(0);

const cancelRequest = reactive({
  reason: '',
  otherReason: '',
  agreePolicy: false
});

const router = useRouter();

// 마운트시 결제 정보 로딩
onMounted(async () => {
  // (로그인 상태 확인은 필요한 경우 별도 추가)
  await loadPaymentDetails();
});

// 결제 정보 및 예약 정보 로드 (예시: 실제 구현은 API에 맞게 수정)
async function loadPaymentDetails() {
  loading.value = true;
  error.value = '';
  try {
    const response = await api.api.get(`/api/v1/payments/detail/${props.paymentId}`);
    const data = response.data;

    // payment와 reservation에 값 세팅
    if (data.payment) {
      Object.assign(payment, data.payment);
    } else {
      payment.paymentId = props.paymentId;
    }
    
    if (data.reservation) {
      Object.assign(reservation, data.reservation);
    }

    if (!payment.paymentKey && data.payment?.status === 'COMPLETED') {
      console.warn('PaymentKey (imp_uid) is missing for a completed payment. Cancellation might not work as expected.');
    }
    
    // 환불 금액 계산
    calculateRefundAmount();

    // 상태 검사
    if (payment.status !== 'COMPLETED' && payment.status !== 'PAID') {
      error.value = '취소할 수 있는 결제 상태가 아닙니다. 현재 상태: ' + getPaymentStatusName(payment.status);
    }
  } catch (e) {
    console.error("Error loading payment details:", e);
    error.value = e.response?.data?.error || e.message || '결제 정보를 불러올 수 없습니다. 다시 시도해주세요.';
  } finally {
    loading.value = false;
  }
}

// 환불 금액 계산 함수
function calculateRefundAmount() {
  if (!reservation.checkInDate) return (refundAmount.value = 0);
  const checkInDate = new Date(reservation.checkInDate);
  const today = new Date();
  const diffTime = checkInDate.getTime() - today.getTime();
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

  if (diffDays >= 7) refundAmount.value = payment.amount;
  else if (diffDays >= 5) refundAmount.value = Math.round(payment.amount * 0.9);
  else if (diffDays >= 3) refundAmount.value = Math.round(payment.amount * 0.7);
  else if (diffDays >= 1) refundAmount.value = Math.round(payment.amount * 0.5);
  else refundAmount.value = 0;
}

// 취소 요청 제출 함수
async function submitCancelRequest() {
  if (!cancelRequest.agreePolicy) {
    error.value = '취소 정책에 동의해주세요.';
    return;
  }
  if (cancelRequest.reason === 'OTHER' && !cancelRequest.otherReason) {
    error.value = '기타 사유를 입력해주세요.';
    return;
  }

  if (!payment.paymentKey) {
    error.value = '결제 식별자(paymentKey)가 없어 아임포트 취소를 진행할 수 없습니다. 내부 취소를 시도하거나 관리자에게 문의하세요.';
    isSubmitting.value = false;
    return;
  }

  isSubmitting.value = true;
  error.value = '';
  message.value = '';

  try {
    const response = await api.api.post(`/api/v1/payments/cancel/imp/${payment.paymentKey}`, {
    });

    const cancelledPaymentData = response.data;
    message.value = `결제(ID: ${cancelledPaymentData.paymentId})가 성공적으로 취소되었습니다. 상태: ${getPaymentStatusName(cancelledPaymentData.status)}`;
    
    setTimeout(() => {
      router.push({
        name: 'PaymentDetail',
        params: { id: payment.paymentId },
        query: { message: message.value }
      });
    }, 3000);

  } catch (e) {
    console.error("Error submitting cancel request:", e);
    error.value = e.response?.data?.error || e.message || '결제 취소 요청에 실패했습니다. 다시 시도해주세요.';
  } finally {
    isSubmitting.value = false;
  }
}

// Helper 함수
const getPaymentMethodName = (method) => ({
  CREDIT_CARD: '신용카드',
  BANK_TRANSFER: '계좌이체',
  VIRTUAL_ACCOUNT: '가상계좌',
  MOBILE_PAYMENT: '모바일결제',
  POINT: '포인트',
})[method] || method;

const getPaymentStatusName = (status) => ({
  PENDING: '대기중',
  COMPLETED: '완료',
  CANCELLED: '취소됨',
  REFUNDED: '환불됨',
  FAILED: '실패',
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

function formatDate(date) {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}
function formatCurrency(amount) {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
    maximumFractionDigits: 0,
  }).format(amount);
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
}
.badge {
  font-weight: 500;
  padding: 0.35em 0.65em;
}
</style>
