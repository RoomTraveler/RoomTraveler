<template>
  <div class="container mt-5 mb-5">
    <div class="payment-result-container text-center">
      <!-- 로딩 중 -->
      <div v-if="isLoading" class="py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">결제 결과 확인 중...</span>
        </div>
        <p class="mt-3">결제 결과를 확인하고 있습니다. 잠시만 기다려주세요.</p>
      </div>

      <!-- 에러 발생 시 -->
      <div v-else-if="errorLoading" class="payment-error-icon py-5">
        <i class="bi bi-x-circle-fill text-danger"></i>
        <h2 class="mb-3 mt-3">오류 발생</h2>
        <p class="lead mb-4">{{ errorLoading }}</p>
        <router-link to="/" class="btn btn-primary">홈으로 돌아가기</router-link>
      </div>

      <!-- 결제 결과 표시 -->
      <div v-else-if="paymentDetails">
        <div class="payment-icon mb-4" :class="isSuccess ? 'text-success' : 'text-warning'">
          <i :class="isSuccess ? 'bi bi-check-circle-fill' : 'bi bi-exclamation-triangle-fill'"></i>
        </div>
        <h2 class="mb-3">{{ isSuccess ? '결제가 완료되었습니다' : '결제 상태를 확인해주세요' }}</h2>
        <p class="lead mb-4">{{ displayMessage }}</p>

        <div class="payment-info-card text-start mb-4">
          <h4 class="mb-3">결제 정보</h4>
          <div class="payment-detail-row"><span>결제 번호 (TID)</span><span>{{ paymentDetails.paymentId }}</span></div>
          <div v-if="route.query.merchant_uid" class="payment-detail-row"><span>주문 번호</span><span>{{ route.query.merchant_uid }}</span></div>
          <div v-if="route.query.imp_uid" class="payment-detail-row"><span>아임포트 번호</span><span>{{ route.query.imp_uid }}</span></div>
          <div class="payment-detail-row"><span>결제 일시</span><span>{{ formatDate(paymentDetails.paidAt) }}</span></div>
          <div class="payment-detail-row"><span>결제 수단</span><span>{{ getPaymentMethodText(paymentDetails.paymentMethod) }}</span></div>
          <div class="payment-detail-row"><span>결제 상태</span><span><span :class="getStatusBadgeClass(paymentStatus)">{{ getStatusText(paymentStatus) }}</span></span></div>
          <div class="payment-detail-row fw-bold"><span>총 결제 금액</span><span class="total-amount">{{ formatCurrency(paymentDetails.amount) }}</span></div>
        </div>

        <!-- 예약 정보 (paymentDetails에 관련 정보가 있다면 표시) -->
        <div v-if="paymentDetails.accommodationTitle" class="payment-info-card text-start mb-4">
            <h4 class="mb-3">예약 정보</h4>
            <div class="payment-detail-row"><span>예약 번호</span><span>{{ paymentDetails.reservationId }}</span></div>
            <div class="payment-detail-row"><span>숙소명</span><span>{{ paymentDetails.accommodationTitle }}</span></div>
            <div class="payment-detail-row"><span>객실명</span><span>{{ paymentDetails.roomName }}</span></div>
            <!-- 더 상세한 예약 정보(체크인/아웃 날짜 등)는 Reservation 모델을 직접 조회하거나 Payment에 포함해야 함 -->
        </div>

        <div class="action-buttons d-flex flex-wrap gap-3 justify-content-center mt-4">
          <button v-if="paymentDetails.reservationId" @click="navigateToReservationDetail" class="btn btn-primary">나의 예약 상세 보기</button>
          <router-link to="/payment/history" class="btn btn-outline-secondary">전체 결제 내역</router-link>
          <router-link to="/" class="btn btn-outline-dark">홈으로</router-link>
        </div>

        <div class="alert alert-info mt-5" role="alert">
          <h5 class="alert-heading mb-2"><i class="bi bi-info-circle"></i> 안내</h5>
          <p class="mb-1">결제 및 예약 관련 문의사항은 고객센터(1234-5678)로 연락주시기 바랍니다.</p>
          <p v-if="isSuccess">정상적으로 처리된 경우, {{ userEmail }} 주소로도 안내 메일이 발송됩니다.</p>
        </div>
      </div>
      
      <!-- 그 외 경우 (예: paymentId도 없고 imp_uid도 없는 경우) -->
       <div v-else class="py-5">
        <i class="bi bi-emoji-frown-fill text-secondary fs-1 mb-3"></i>
        <h2 class="mb-3">알 수 없는 결과</h2>
        <p class="lead mb-4">{{ displayMessage || '결제 결과를 표시할 수 없습니다.' }}</p>
        <router-link to="/" class="btn btn-primary">홈으로 돌아가기</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from "@/api/index"; // API 모듈 임포트
import { useUserStore } from "@/store/userStore"; // UserStore 임포트

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const paymentDetails = ref(null); // 백엔드에서 받은 결제 상세 정보 전체를 저장
const reservationDetails = ref(null); // paymentDetails에서 파생되거나 함께 받을 예약 정보
const isLoading = ref(true);
const errorLoading = ref(null);
const displayMessage = ref(route.query.message || '');

const userEmail = computed(() => userStore.user?.email || '고객님의 이메일');

// 결제 ID로 상세 정보 조회
async function loadPaymentDetails(paymentId) {
  isLoading.value = true;
  errorLoading.value = null;
  try {
    // API 엔드포인트는 PaymentController의 GetMapping("/{paymentId}")에 맞춰야 함
    const response = await api.api.get(`/api/v1/payments/${paymentId}`);
    paymentDetails.value = response.data; 

    // 백엔드가 Payment 객체 내에 Reservation 정보도 포함해서 준다고 가정
    // 또는 별도의 Reservation 조회 API를 호출할 수도 있음
    // 예시: paymentDetails.value.reservation 이 Reservation 객체라고 가정
    if (paymentDetails.value && paymentDetails.value.reservationId) {
        // 실제 Reservation 객체를 가져오려면 추가 API 호출 또는 Payment 객체 내 포함 필요
        // 여기서는 Payment 객체에 reservation 관련 정보가 일부 포함되어 있다고 가정 (예: accommodationTitle)
        // 더 상세한 예약 정보가 필요하면 reservationService.getReservationById(paymentDetails.value.reservationId) 호출
    }

    if (!displayMessage.value) {
        displayMessage.value = paymentDetails.value?.status === 'COMPLETED' || paymentDetails.value?.status === 'PAID' 
                                ? '결제가 성공적으로 완료되었습니다.' 
                                : '결제 상태를 확인해주세요.';
    }

  } catch (err) {
    console.error('결제 상세 정보 조회 중 오류:', err);
    errorLoading.value = err.response?.data?.error || err.message || '결제 정보를 불러오는데 실패했습니다. 주문번호 또는 결제내역에서 다시 확인해주세요.';
    displayMessage.value = errorLoading.value;
  } finally {
    isLoading.value = false;
  }
}

const paymentStatus = computed(() => paymentDetails.value?.status);
const isSuccess = computed(() => paymentStatus.value === 'COMPLETED' || paymentStatus.value === 'PAID'); // 백엔드 Payment 모델의 성공 상태값에 따라 수정

onMounted(() => {
  const paymentIdFromQuery = route.query.paymentId;
  const impUidFromQuery = route.query.imp_uid;
  // const merchantUidFromQuery = route.query.merchant_uid;

  if (paymentIdFromQuery) {
    loadPaymentDetails(paymentIdFromQuery);
  } else if (impUidFromQuery) {
    // paymentId가 없고 imp_uid만 있다면, imp_uid로 paymentId를 찾는 API가 필요할 수 있음
    // 또는 사용자에게 주문번호로 조회하라는 안내 메시지 표시
    displayMessage.value = '결제 ID를 찾을 수 없습니다. 주문번호로 결제 내역을 확인해주세요.';
    errorLoading.value = displayMessage.value;
    isLoading.value = false;
  } else {
    displayMessage.value = '잘못된 접근입니다. 결제 정보를 확인할 수 없습니다.';
    errorLoading.value = displayMessage.value;
    isLoading.value = false;
  }
});

// Helper functions (기존 코드 유지 또는 개선)
function getStatusBadgeClass(status) {
  if (!status) return 'badge bg-secondary';
  switch (status.toUpperCase()) {
    case 'COMPLETED':
    case 'PAID': 
      return 'badge bg-success';
    case 'PENDING_PAYMENT':
    case 'PENDING':
      return 'badge bg-warning text-dark';
    case 'FAILED': return 'badge bg-danger';
    case 'CANCELLED': return 'badge bg-secondary';
    default: return 'badge bg-info';
  }
}

function getStatusText(status) {
  if (!status) return '알 수 없음';
  switch (status.toUpperCase()) {
    case 'COMPLETED':
    case 'PAID': 
      return '결제 완료';
    case 'PENDING_PAYMENT':
    case 'PENDING': 
      return '처리 중';
    case 'FAILED': return '결제 실패';
    case 'CANCELLED': return '결제 취소';
    default: return status;
  }
}

function getPaymentMethodText(method) {
  if (!method) return '정보 없음';
  // 백엔드 Payment 모델의 paymentMethod 값에 따라 변경
  // 예시: 'CARD', 'BANK_TRANSFER', 'PHONE_PAYMENT' 등
  const methods = {
    'card': '신용카드',
    'trans': '실시간계좌이체',
    'vbank': '가상계좌',
    'phone': '휴대폰 소액결제',
    // 아임포트에서 사용하는 다른 PG사의 결제수단 코드를 추가할 수 있음
  };
  return methods[method.toLowerCase()] || method;
}

function formatDate(dateString, format = 'full') {
  if (!dateString) return '-';
  const date = new Date(dateString);
  if (isNaN(date.getTime())) return '유효하지 않은 날짜';

  if (format === 'date-only') {
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric', month: '2-digit', day: '2-digit'
    }).format(date);
  }
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    hour12: false
  }).format(date);
}

function formatCurrency(amount) {
  if (typeof amount !== 'number' || isNaN(amount)) return '가격 정보 없음';
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency', currency: 'KRW', maximumFractionDigits: 0
  }).format(amount);
}

function navigateToReservationDetail() {
  if (paymentDetails.value && paymentDetails.value.reservationId) {
    router.push({ name: 'ReservationDetail', params: { id: paymentDetails.value.reservationId } });
  }
}

</script>

<style scoped>
.payment-result-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px;
}
.payment-icon {
  font-size: 5rem; /* 기존 success 아이콘 크기와 유사하게 */
  margin-bottom: 1.5rem;
}
.payment-error-icon i {
  font-size: 5rem;
  margin-bottom: 1.5rem;
}
.payment-info-card {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: .75rem; /* 10px -> 0.75rem Bootstrap 스타일과 유사하게 */
  padding: 1.5rem; /* 20px -> 1.5rem */
  margin-bottom: 1.5rem; /* 기존 간격 유지 */
}
.payment-info-card h4 {
  border-bottom: 1px solid #e9ecef;
  padding-bottom: .75rem; /* 10px -> 0.75rem */
  margin-bottom: 1rem !important; /* 기존보다 약간 더 간격 */
}
.payment-detail-row {
  display: flex;
  justify-content: space-between;
  padding: .5rem 0; /* 8px -> 0.5rem */
  border-bottom: 1px dashed #e0e0e0;
}
.payment-detail-row:last-child {
  border-bottom: none;
}
.payment-detail-row span:first-child {
  color: #6c757d; /* text-muted */
}
.payment-detail-row span:last-child {
  font-weight: 500;
}
.total-amount {
  color: #0d6efd; /* Bootstrap primary color */
  font-size: 1.1rem;
}
.action-buttons .btn {
  min-width: 150px;
  padding: .75rem 1rem; /* 10px 20px -> .75rem 1rem */
}
.alert-heading {
  font-weight: 500;
}
.fs-1 {
    font-size: 5rem !important; /* 기존 아이콘 크기 대응 */
}
</style>
