<template>
  <div class="container mt-5 mb-5">
    <!-- 알림 메시지 -->
    <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
      {{ message }}
      <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
    </div>

    <!-- 상단 타이틀 및 목록 버튼 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>예약 상세</h2>
      <router-link to="/reservation/my-reservations" class="btn btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> 예약 목록으로
      </router-link>
    </div>

    <!-- 예약 상태 카드 -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center">
              <h4 class="card-title">예약 번호: {{ reservation.value.reservationId }}</h4>
              <div>
                <span :class="getStatusBadgeClass(reservation.value.status)">
                  {{ getStatusText(reservation.value.status) }}
                </span>
                <span :class="getPaymentStatusBadgeClass(reservation.value.paymentStatus)" class="ms-2">
                  {{ getPaymentStatusText(reservation.value.paymentStatus) }}
                </span>
              </div>
            </div>
            <p class="text-muted">예약일: {{ formatDate(reservation.value.createdAt) }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 본문 정보 영역 -->
    <div class="row mb-4">
      <div class="col-md-8">
        <!-- 예약 정보 -->
        <div class="card reservation-card">
          <div class="card-body">
            <h4 class="card-title">예약 정보</h4>
            <div class="row">
              <div class="col-md-4">
                <img
                    :src="room.value.mainImageUrl || noImage"
                    class="img-fluid rounded"
                    :alt="room.value.name"
                />
              </div>
              <div class="col-md-8">
                <h5>{{ room.value.name }}</h5>
                <p class="text-muted">{{ accommodation.value.title }}</p>
                <p><i class="bi bi-geo-alt"></i> {{ accommodation.value.address }}</p>
                <p><i class="bi bi-calendar-check"></i>
                  체크인: <strong>{{ formatDate(reservation.value.checkInDate, 'date-only') }}</strong>
                  ({{ accommodation.value.checkInTime }})
                </p>
                <p><i class="bi bi-calendar-x"></i>
                  체크아웃: <strong>{{ formatDate(reservation.value.checkOutDate, 'date-only') }}</strong>
                  ({{ accommodation.value.checkOutTime }})
                </p>
                <p><i class="bi bi-people"></i>
                  인원: <strong>{{ reservation.value.guestCount }}명</strong>
                </p>
                <p><i class="bi bi-telephone"></i> 숙소 연락처: {{ accommodation.value.phone }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 예약자 정보 -->
        <div class="card reservation-card">
          <div class="card-body">
            <h4 class="card-title">예약자 정보</h4>
            <p><i class="bi bi-person"></i> 이름: {{ reservation.value.userName }}</p>
            <p><i class="bi bi-envelope"></i> 이메일: {{ reservation.value.userEmail }}</p>
            <p><i class="bi bi-telephone"></i> 전화번호: {{ reservation.value.userPhone }}</p>
          </div>
        </div>

        <!-- 요청사항 -->
        <div v-if="reservation.value.specialRequests" class="card reservation-card">
          <div class="card-body">
            <h4 class="card-title">요청 사항</h4>
            <p>{{ reservation.value.specialRequests }}</p>
          </div>
        </div>

        <!-- 리뷰 영역 -->
        <div v-if="review.value" class="card review-card">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h4 class="card-title">내 리뷰</h4>
              <div>
                <i
                    v-for="i in 5"
                    :key="i"
                    :class="i <= review.value.rating ? 'bi bi-star-fill star-rating' : 'bi bi-star star-rating'"
                ></i>
                <span class="ms-2">{{ review.value.rating }}/5</span>
              </div>
            </div>
            <p>{{ review.value.comment }}</p>
            <p class="text-muted">작성일: {{ formatDate(review.value.createdAt, 'date-only') }}</p>
            <div class="d-flex justify-content-end">
              <router-link
                  :to="`/reservation/update-review-form/${review.value.reviewId}`"
                  class="btn btn-outline-primary me-2"
              >수정</router-link>
              <button @click="deleteReview" class="btn btn-outline-danger">삭제</button>
            </div>
          </div>
        </div>
        <!-- 리뷰 작성 안내 -->
        <div v-else-if="reservation.value.status === 'COMPLETED'" class="card review-card">
          <div class="card-body">
            <h4 class="card-title">리뷰 작성</h4>
            <p>숙박은 어떠셨나요? 다른 사용자들에게 도움이 될 수 있는 리뷰를 작성해주세요.</p>
            <div class="d-grid">
              <router-link :to="`/reservation/review-form/${reservation.value.reservationId}`" class="btn btn-primary">
                리뷰 작성하기
              </router-link>
            </div>
          </div>
        </div>
      </div>

      <div class="col-md-4">
        <!-- 가격 정보 -->
        <div class="card mb-4">
          <div class="card-body">
            <h4 class="card-title">가격 정보</h4>
            <p><i class="bi bi-currency-dollar"></i> 1박 요금: {{ formatCurrency(room.value.price) }}</p>
            <p><i class="bi bi-calendar-week"></i> 숙박 일수: {{ reservation.value.nights }}박</p>
            <div class="price-detail">
              <p>객실 요금: {{ formatCurrency(room.value.price * reservation.value.nights) }}</p>
              <p>세금 및 수수료: {{ formatCurrency(room.value.price * reservation.value.nights * 0.1) }}</p>
              <p class="total-price">총 요금: {{ formatCurrency(reservation.value.totalPrice) }}</p>
            </div>
          </div>
        </div>

        <!-- 예약 관리 -->
        <div class="card mb-4">
          <div class="card-body">
            <h4 class="card-title">예약 관리</h4>
            <div v-if="reservation.value.paymentStatus === 'UNPAID' && reservation.value.status !== 'CANCELLED'" class="d-grid mb-3">
              <button @click="updatePayment" class="btn btn-success">결제하기</button>
            </div>
            <div v-if="reservation.value.status === 'PENDING' || reservation.value.status === 'CONFIRMED'" class="d-grid mb-3">
              <button @click="cancelReservation" class="btn btn-danger">예약 취소</button>
            </div>
            <div class="d-grid">
              <router-link :to="`/accommodation/detail/${accommodation.value.accommodationId}`" class="btn btn-outline-primary">
                숙소 상세 보기
              </router-link>
            </div>
          </div>
        </div>

        <!-- 환불 정책 -->
        <div class="card">
          <div class="card-body">
            <h4 class="card-title">환불 정책</h4>
            <ul class="list-unstyled">
              <li><i class="bi bi-check-circle text-success"></i> 체크인 7일 전 취소: 100% 환불</li>
              <li><i class="bi bi-check-circle text-success"></i> 체크인 5일 전 취소: 70% 환불</li>
              <li><i class="bi bi-check-circle text-warning"></i> 체크인 3일 전 취소: 50% 환불</li>
              <li><i class="bi bi-x-circle text-danger"></i> 체크인 1일 전 취소: 환불 불가</li>
              <li><i class="bi bi-x-circle text-danger"></i> 노쇼(No-show): 환불 불가</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
// Vue3 Composition API 및 ref/reactive 활용
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// 이미지 대체 경로
import noImage from '@/assets/no-image.jpg';

// 상태 선언
const route = useRoute();
const router = useRouter();

const reservation = ref({
  reservationId: '',
  status: '',
  paymentStatus: '',
  createdAt: null,
  checkInDate: null,
  checkOutDate: null,
  guestCount: 0,
  nights: 0,
  totalPrice: 0,
  userName: '',
  userEmail: '',
  userPhone: '',
  specialRequests: ''
});
const accommodation = ref({
  accommodationId: '',
  title: '',
  address: '',
  phone: '',
  checkInTime: '',
  checkOutTime: ''
});
const room = ref({
  roomId: '',
  name: '',
  price: 0,
  mainImageUrl: ''
});
const review = ref(null);
const message = ref('');

// 예약 상세 정보 불러오기
const loadReservationDetail = async (reservationId) => {
  try {
    const response = await fetch(`/api/reservations/${reservationId}`);
    if (!response.ok) throw new Error('예약 정보를 불러오는데 실패했습니다.');
    const data = await response.json();
    reservation.value = data.reservation;
    accommodation.value = data.accommodation;
    room.value = data.room;
    review.value = data.review;
  } catch (error) {
    console.error('예약 상세 정보 로드 중 오류:', error);
    message.value = '예약 정보를 불러오는데 실패했습니다.';
  }
};

// 결제 처리
const updatePayment = async () => {
  if (!confirm('결제를 진행하시겠습니까?')) return;
  try {
    const response = await fetch(`/api/reservations/${reservation.value.reservationId}/payment`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ paymentStatus: 'PAID' })
    });
    if (!response.ok) throw new Error('결제 처리에 실패했습니다.');
    router.push(`/payment/form/${reservation.value.reservationId}`);
  } catch (error) {
    console.error('결제 처리 오류:', error);
    message.value = '결제 처리에 실패했습니다.';
  }
};

// 예약 취소
const cancelReservation = async () => {
  if (!confirm('정말 취소하시겠습니까? 환불 정책에 따라 수수료가 부과될 수 있습니다.')) return;
  try {
    const response = await fetch(`/api/reservations/${reservation.value.reservationId}/cancel`, { method: 'POST' });
    if (!response.ok) throw new Error('예약 취소에 실패했습니다.');
    await loadReservationDetail(reservation.value.reservationId);
    message.value = '예약이 취소되었습니다.';
  } catch (error) {
    console.error('예약 취소 오류:', error);
    message.value = '예약 취소에 실패했습니다.';
  }
};

// 리뷰 삭제
const deleteReview = async () => {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  try {
    const response = await fetch(`/api/reviews/${review.value.reviewId}`, { method: 'DELETE' });
    if (!response.ok) throw new Error('리뷰 삭제에 실패했습니다.');
    review.value = null;
    message.value = '리뷰가 삭제되었습니다.';
  } catch (error) {
    console.error('리뷰 삭제 오류:', error);
    message.value = '리뷰 삭제에 실패했습니다.';
  }
};

// 예약 상태 배지 클래스 반환
const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'PENDING': return 'badge bg-warning status-badge';
    case 'CONFIRMED': return 'badge bg-success status-badge';
    case 'CANCELLED': return 'badge bg-danger status-badge';
    case 'COMPLETED': return 'badge bg-info status-badge';
    default: return 'badge bg-secondary status-badge';
  }
};
// 예약 상태 텍스트 반환
const getStatusText = (status) => {
  switch (status) {
    case 'PENDING': return '대기중';
    case 'CONFIRMED': return '확정';
    case 'CANCELLED': return '취소됨';
    case 'COMPLETED': return '완료';
    default: return status;
  }
};
// 결제 상태 배지 클래스 반환
const getPaymentStatusBadgeClass = (status) => {
  switch (status) {
    case 'UNPAID': return 'badge bg-secondary status-badge';
    case 'PAID': return 'badge bg-success status-badge';
    case 'REFUNDED': return 'badge bg-warning status-badge';
    default: return 'badge bg-secondary status-badge';
  }
};
// 결제 상태 텍스트 반환
const getPaymentStatusText = (status) => {
  switch (status) {
    case 'UNPAID': return '미결제';
    case 'PAID': return '결제완료';
    case 'REFUNDED': return '환불됨';
    default: return status;
  }
};
// 날짜 포맷 함수
const formatDate = (dateString, format = 'full') => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  if (format === 'date-only') {
    return new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: '2-digit', day: '2-digit' }).format(date);
  }
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit', second: '2-digit'
  }).format(date);
};
// 금액 포맷 함수
const formatCurrency = (amount) => {
  return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 }).format(amount);
};

// 컴포넌트 마운트시 데이터 불러오기
onMounted(() => {
  const reservationId = route.params.id;
  if (reservationId) {
    loadReservationDetail(reservationId);
  } else {
    message.value = '예약 정보를 찾을 수 없습니다.';
  }
  // 쿼리 메시지 처리
  if (route.query.message) {
    message.value = route.query.message;
  }
});
</script>

<style scoped>
/* 카드 마진 */
.reservation-card {
  margin-bottom: 20px;
}
.status-badge {
  font-size: 0.9rem;
  padding: 0.5rem 0.75rem;
}
.price-detail {
  border-top: 1px solid #dee2e6;
  padding-top: 15px;
  margin-top: 15px;
}
.total-price {
  font-size: 1.2rem;
  font-weight: bold;
}
.review-card {
  margin-top: 20px;
}
.star-rating {
  color: #ffc107;
}
</style>
