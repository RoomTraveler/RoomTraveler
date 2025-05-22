<template>
  <div class="container my-5">
    <div class="mx-auto" style="max-width: 1000px;">
      <h2 class="mb-4">나의 예약 내역</h2>

      <!-- 알림 메시지 -->
      <div v-if="message" :class="`alert alert-${messageType} alert-dismissible fade show`" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="closeMessage" aria-label="Close"></button>
      </div>

      <!-- 로딩 상태 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">예약 내역을 불러오는 중입니다...</p>
      </div>

      <!-- 예약 내역이 없는 경우 -->
      <div v-else-if="reservations.length === 0" class="text-center py-5">
        <div class="mb-3" style="font-size: 3rem; color: #adb5bd;">
          <i class="bi bi-journal-bookmark-fill"></i>
        </div>
        <h4>예약 내역이 없습니다</h4>
        <p class="text-muted">아직 예약하신 내역이 없습니다. 마음에 드는 숙소를 찾아 예약해보세요.</p>
        <router-link to="/accommodation/list" class="btn btn-primary mt-3">
          <i class="bi bi-search"></i> 숙소 찾아보기
        </router-link>
      </div>

      <!-- 예약 내역 목록 -->
      <div v-else class="row">
        <div
            v-for="reservation in reservations"
            :key="reservation.reservationId"
            class="col-md-6 mb-4"
        >
          <div class="card h-100 shadow-sm border-0">
            <!-- 카드 헤더: 숙소/객실 정보 + 상태 뱃지 -->
            <div class="d-flex justify-content-between align-items-center p-3 bg-light border-bottom">
              <div>
                <h5 class="mb-1 fw-bold">{{ reservation.accommodationTitle }}</h5>
                <small class="text-muted">{{ reservation.roomName }}</small>
              </div>
              <span :class="getStatusBadgeClass(reservation.status)">
                {{ getStatusText(reservation.status) }}
              </span>
            </div>
            <!-- 카드 바디: 상세정보 -->
            <div class="card-body">
              <div class="row mb-2">
                <div class="col-6">
                  <p class="mb-1"><strong>예약 번호:</strong> {{ reservation.reservationId }}</p>
                  <p class="mb-1"><strong>체크인:</strong> {{ formatDate(reservation.checkInDate) }}</p>
                  <p class="mb-1"><strong>숙박 일수:</strong> {{ reservation.nights }}박</p>
                </div>
                <div class="col-6">
                  <p class="mb-1"><strong>결제 상태:</strong> {{ getPaymentStatusText(reservation.paymentStatus) }}</p>
                  <p class="mb-1"><strong>체크아웃:</strong> {{ formatDate(reservation.checkOutDate) }}</p>
                  <p class="mb-1"><strong>인원:</strong> {{ reservation.guestCount }}명</p>
                </div>
              </div>
              <p v-if="reservation.specialRequests" class="mb-0">
                <strong>특별 요청:</strong> {{ reservation.specialRequests }}
              </p>
            </div>
            <!-- 카드 푸터: 금액/상세/취소 -->
            <div class="card-footer d-flex justify-content-between align-items-center bg-light">
              <span class="fw-bold text-primary fs-5">
                {{ formatCurrency(reservation.totalPrice) }}
              </span>
              <div>
                <router-link
                    :to="`/reservation/detail/${reservation.reservationId}`"
                    class="btn btn-sm btn-outline-primary me-2"
                >
                  <i class="bi bi-info-circle"></i> 상세 보기
                </router-link>
                <button
                    v-if="canCancel(reservation.status)"
                    @click="confirmCancelReservation(reservation.reservationId)"
                    class="btn btn-sm btn-outline-danger"
                    :disabled="cancelling === reservation.reservationId"
                >
                  <span
                      v-if="cancelling === reservation.reservationId"
                      class="spinner-border spinner-border-sm"
                      role="status"
                      aria-hidden="true"
                  ></span>
                  <i v-else class="bi bi-calendar-x"></i> 예약 취소
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 예약 취소 확인 모달 -->
      <div class="modal fade" ref="cancelConfirmModalRef" tabindex="-1" aria-labelledby="cancelConfirmModalLabel" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="cancelConfirmModalLabel">예약 취소 확인</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
              정말로 이 예약을 취소하시겠습니까? 이 작업은 되돌릴 수 없습니다.
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
              <button type="button" class="btn btn-danger" @click="executeCancelReservation">예약 취소</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
/* ---------------------------------------------------
   MyReservations.vue - 나의 예약 내역 컴포넌트
   - Bootstrap 5 사용
   - 최신 Vue 3 Composition API 문법 사용
   - 주석은 한국어
--------------------------------------------------- */
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';

// 예약 내역/상태/메시지 등 데이터
const reservations = ref([]);
const loading = ref(false);
const message = ref('');
const messageType = ref('success'); // success, danger 등
const cancelling = ref(null); // 현재 취소 중인 예약 ID
const reservationToCancel = ref(null);
const cancelConfirmModalRef = ref(null);
let cancelConfirmModalInstance = null;

// 라우터 (상세보기 이동용)
const router = useRouter();

// 컴포넌트 마운트 시 데이터 조회 + 모달 인스턴스 생성
onMounted(() => {
  fetchMyReservations();

  // Bootstrap Modal 인스턴스 할당 (vanilla JS 방식)
  const modalEl = cancelConfirmModalRef.value;
  // Bootstrap 모듈이 window.Bootstrap에 attach 되어있음 (vite/webpack 환경)
  if (window.bootstrap && modalEl) {
    cancelConfirmModalInstance = window.bootstrap.Modal.getOrCreateInstance(modalEl);
  }
});

// 나의 예약 내역 불러오기 (비동기)
async function fetchMyReservations() {
  loading.value = true;
  message.value = '';
  try {
    const response = await fetch('/api/reservation/my-reservations');
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({ message: '예약 내역을 불러오는 데 실패했습니다.' }));
      throw new Error(errorData.message || '서버 오류가 발생했습니다.');
    }
    const data = await response.json();
    reservations.value = data.reservations || [];
  } catch (error) {
    message.value = error.message;
    messageType.value = 'danger';
  } finally {
    loading.value = false;
  }
}

// 메시지 닫기
function closeMessage() {
  message.value = '';
}

// 예약 취소 버튼 클릭 시 - 모달 표시
function confirmCancelReservation(reservationId) {
  reservationToCancel.value = reservationId;
  if (cancelConfirmModalInstance) {
    cancelConfirmModalInstance.show();
  }
}

// 실제 예약 취소 실행
async function executeCancelReservation() {
  if (!reservationToCancel.value) return;
  cancelling.value = reservationToCancel.value;
  message.value = '';
  if (cancelConfirmModalInstance) {
    cancelConfirmModalInstance.hide();
  }

  try {
    const response = await fetch(`/api/reservation/cancel/${reservationToCancel.value}`, {
      method: 'POST',
    });
    const responseData = await response.json();
    if (!response.ok) {
      throw new Error(responseData.error || responseData.message || '예약 취소에 실패했습니다.');
    }
    message.value = responseData.message || '예약이 성공적으로 취소되었습니다.';
    messageType.value = 'success';
    // 목록 새로고침
    fetchMyReservations();
  } catch (error) {
    message.value = error.message;
    messageType.value = 'danger';
  } finally {
    cancelling.value = null;
    reservationToCancel.value = null;
  }
}

// 상태 뱃지 class 반환
function getStatusBadgeClass(status) {
  switch (status) {
    case 'CONFIRMED':
      return 'badge bg-success';
    case 'PENDING':
      return 'badge bg-warning text-dark';
    case 'CANCELLED':
      return 'badge bg-secondary';
    case 'COMPLETED':
      return 'badge bg-primary';
    default:
      return 'badge bg-info';
  }
}

// 상태 한글 변환
function getStatusText(status) {
  switch (status) {
    case 'CONFIRMED':
      return '예약 확정';
    case 'PENDING':
      return '예약 대기';
    case 'CANCELLED':
      return '취소됨';
    case 'COMPLETED':
      return '이용 완료';
    default:
      return status;
  }
}

// 결제상태 한글 변환
function getPaymentStatusText(paymentStatus) {
  switch (paymentStatus) {
    case 'PAID':
      return '결제 완료';
    case 'UNPAID':
      return '결제 대기';
    case 'REFUNDED':
      return '환불됨';
    default:
      return paymentStatus;
  }
}

// 취소 가능 상태 확인
function canCancel(status) {
  return ['PENDING', 'CONFIRMED'].includes(status);
}

// 날짜 YYYY-MM-DD 형식 변환
function formatDate(dateString) {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' });
}

// 통화 포맷
function formatCurrency(amount) {
  if (amount === null || amount === undefined) return '-';
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
    maximumFractionDigits: 0,
  }).format(amount);
}
</script>

<!-- 추가적인 커스텀 스타일(필요시) -->
<style scoped>
/* 카드 hover 효과 등 */
.card {
  border-radius: 12px;
  transition: box-shadow 0.2s, transform 0.15s;
}
.card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.10);
  transform: translateY(-2px);
}
</style>
