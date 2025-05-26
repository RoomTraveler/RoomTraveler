<template>
  <Layout>
    <div class="container mt-5 mb-5">
      <h2 class="mb-4">결제하기</h2>

      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>

      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>

      <div v-if="loadingReservation" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">예약 정보를 불러오는 중입니다...</p>
      </div>

      <div v-else-if="reservation">
        <!-- 예약 정보 요약 -->
        <div class="payment-summary mb-4">
          <div class="row">
            <div class="col-md-6">
              <h4>예약 정보</h4>
              <p><strong>숙소:</strong> {{ reservation.accommodationTitle }}</p>
              <p><strong>객실:</strong> {{ reservation.roomName }}</p>
              <p><strong>체크인:</strong> {{ formatDate(reservation.checkInDate) }}</p>
              <p><strong>체크아웃:</strong> {{ formatDate(reservation.checkOutDate) }}</p>
              <p><strong>인원:</strong> {{ reservation.guestCount }}명</p>
            </div>
            <div class="col-md-6">
              <h4>결제 금액</h4>
              <!-- 실제 결제될 금액은 preparePaymentForCheckout 응답을 사용하는 것이 더 정확합니다. -->
              <p class="total-price"><strong>총 결제 예정 금액:</strong> {{ formatCurrency(reservation.totalPrice) }}</p>
            </div>
          </div>
        </div>

        <!-- 결제 폼 (아임포트 버튼) -->
        <form @submit.prevent="requestPayment">
          <!-- PG사 선택 등 간단한 옵션은 유지 가능 -->
          <div class="card mb-4">
            <div class="card-body">
              <h4 class="card-title mb-3">결제 방법 선택</h4>
              <div class="row">
                <div class="col-md-4 mb-3">
                  <div
                      class="payment-method-card"
                      :class="{ 'selected': selectedPg === 'kakaopay' }"
                      @click="selectedPg = 'kakaopay'"
                  >
                    <div class="payment-icon"><i class="bi bi-credit-card"></i></div> <!-- 아이콘은 적절히 변경 -->
                    <h5>카카오페이</h5>
                  </div>
                </div>
                <div class="col-md-4 mb-3">
                  <div
                      class="payment-method-card"
                      :class="{ 'selected': selectedPg === 'tosspayments' }"
                      @click="selectedPg = 'tosspayments'"
                  >
                    <div class="payment-icon"><i class="bi bi-credit-card-2-front"></i></div>
                    <h5>토스페이먼츠</h5>
                  </div>
                </div>
                 <!-- 다른 PG사 추가 가능 -->
              </div>
               <p v-if="!selectedPg" class="text-danger">결제 수단을 선택해주세요.</p>
            </div>
          </div>

          <div class="card mb-4">
            <div class="card-body">
              <h4 class="card-title">결제 동의</h4>
              <div class="form-check mb-2">
                <input class="form-check-input" type="checkbox" id="agreeTerms" v-model="agreedToTerms" required>
                <label class="form-check-label" for="agreeTerms">
                  결제 진행에 필요한 약관에 모두 동의합니다. (필수)
                </label>
                 <!-- 실제 약관 내용은 모달 등으로 제공 -->
              </div>
            </div>
          </div>

          <div class="d-grid gap-2">
            <button type="submit" class="btn btn-primary btn-lg" :disabled="loadingPayment || !agreedToTerms || !selectedPg">
              <span v-if="loadingPayment" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              결제하기
            </button>
            <router-link :to="`/reservation/detail/${props.reservationId}`" class="btn btn-outline-secondary">
              취소
            </router-link>
          </div>
        </form>
      </div>
      <div v-else class="alert alert-warning">
        예약 정보를 찾을 수 없습니다.
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useCartStore } from '@/store/cartStore'; // Pinia 스토어
import { useUserStore } from '@/store/userStore'; // User 스토어 (구매자 정보용)
import api from '@/api/index'; // API 호출용 (예약 정보 로드 등)
import Layout from '@/components/layout/Layout.vue';

const props = defineProps({
  reservationId: {
    type: [String, Number],
    required: true
  }
});

const router = useRouter();
const route = useRoute(); // route.query.message 등 처리용
const cartStore = useCartStore();
const userStore = useUserStore();

const IMP = window.IMP; // 아임포트 객체

const loadingReservation = ref(true);
const loadingPayment = ref(false);
const message = ref(route.query.message || '');
const error = ref('');

const reservation = ref(null); // 단일 예약 정보
const selectedPg = ref(''); // 예: 'kakaopay', 'tosspayments'
const agreedToTerms = ref(false);

// 예약 정보 로드 함수 (PaymentForm 진입 시 호출)
async function loadReservationDetails() {
  loadingReservation.value = true;
  error.value = '';
  try {
    // 이 API는 userId를 사용하지 않고 reservationId만으로 예약 정보를 가져와야 함
    // 또는 현재 로그인한 사용자의 예약인지 백엔드에서 검증
    const response = await api.api.get(`/api/v1/reservation/${props.reservationId}`); 
    // 위 API 경로는 예시이며, 실제 ReservationController의 상세 조회 API 경로로 수정 필요
    // 백엔드 ReservationController getReservationDetail은 userId, userRole도 받음.
    // 여기서는 reservationId로만 조회하는 API가 있다고 가정.
    // const response = await api.get(`/api/v1/reservations/${props.reservationId}/form-data`); // 이전에 사용하던 API 형태라면 이것일수도
    reservation.value = response.data.reservation; // 응답 구조에 따라 조정

    if (!reservation.value) {
        throw new Error('예약 정보를 찾을 수 없습니다.');
    }

    // 아임포트 SDK 초기화 (실제 IMP 코드로 교체)
    IMP.init('imp00000000'); // TODO: 실제 IMP 코드로 교체

  } catch (e) {
    console.error("Error loading reservation details:", e);
    error.value = e.response?.data?.error || e.message || '예약 정보를 불러올 수 없습니다.';
    reservation.value = null; // 에러 시 reservation 비움
  } finally {
    loadingReservation.value = false;
  }
}

onMounted(() => {
  loadReservationDetails();
});

async function requestPayment() {
  if (!agreedToTerms.value) {
    error.value = '결제 약관에 동의해주세요.';
    return;
  }
  if (!selectedPg.value) {
    error.value = '결제 수단을 선택해주세요.';
    return;
  }
  if (!reservation.value) {
    error.value = '결제할 예약 정보가 없습니다.';
    return;
  }

  loadingPayment.value = true;
  error.value = '';
  message.value = '';

  try {
    // 1. 결제 준비 DTO 생성
    const prepareRequestDto = {
      reservationsToCreate: [{
        // ReservationService.prepareReservationsForPayment에서 필요한 필드들:
        // roomId, accommodationId, checkInDate, checkOutDate, guestCount, totalPrice
        roomId: reservation.value.roomId, 
        accommodationId: reservation.value.accommodationId,
        checkInDate: reservation.value.checkInDate,
        checkOutDate: reservation.value.checkOutDate,
        guestCount: reservation.value.guestCount,
        totalPrice: reservation.value.totalPrice 
      }],
      specialRequests: reservation.value.specialRequests || ""
    };

    // 2. 서버에 결제 준비 요청 (merchant_uid 받기)
    const preparedData = await cartStore.preparePaymentForCheckout(prepareRequestDto);

    // 3. 아임포트 결제 요청
    IMP.request_pay({
      pg: selectedPg.value, // 예: "kakaopay", "tosspayments"
      pay_method: 'card', // 'card', 'trans', 'phone', 'vbank' 등 PG사에서 지원하는 방식
      merchant_uid: preparedData.merchantUid,
      name: preparedData.paymentName, // 예: "숙소 A 외 1건"
      amount: preparedData.amount, // 실제 결제될 금액
      buyer_email: userStore.userInfo?.email || 'test@example.com', // userStore에서 가져오거나 기본값
      buyer_name: userStore.userInfo?.username || '구매자',
      buyer_tel: userStore.userInfo?.phone || '010-0000-0000',
      // buyer_addr: '주소',
      // buyer_postcode: '우편번호',
      // m_redirect_url: `{YOUR_DOMAIN}/payment/result?merchant_uid=${preparedData.merchantUid}` // 모바일 결제 후 돌아올 URL
    }, async (rsp) => { // 콜백 함수
      if (rsp.success) {
        // 결제 성공
        try {
          const paymentResult = await cartStore.completePaymentAfterIamport({
            impUid: rsp.imp_uid,
            merchantUid: rsp.merchant_uid,
          });
          message.value = paymentResult.message || '결제가 성공적으로 처리되었습니다.';
          // 결제 결과 페이지로 이동 (paymentId를 query로 전달)
          router.push({ name: 'PaymentResult', query: { paymentId: paymentResult.paymentId } });
        } catch (completeError) {
          console.error("Error completing payment after Iamport success:", completeError);
          error.value = completeError.message || '결제 완료 처리에 실패했습니다. 관리자에게 문의하세요.';
          // TODO: 여기서 아임포트 환불 로직 호출 (rsp.imp_uid 사용)
          // 예: await api.api.post(`/api/v1/payments/cancel/imp/${rsp.imp_uid}`);
          // 위 API는 현재 GET 방식의 PaymentController.cancelIamportPayment와 충돌하므로, 별도 API 필요 가능성
        }
      } else {
        // 결제 실패
        error.value = rsp.error_msg || '결제에 실패했습니다.';
        // 예: 사용자가 창을 닫거나, 잔액 부족 등
      }
      loadingPayment.value = false;
    });

  } catch (e) {
    console.error("Error during payment process:", e);
    error.value = e.response?.data?.error || e.message || '결제 진행 중 오류가 발생했습니다.';
    loadingPayment.value = false;
  }
}

// 날짜, 금액 포맷 유틸리티
function formatDate(dateStr) {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('ko-KR');
}

function formatCurrency(amount) {
  if (typeof amount !== 'number') return '';
  return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(amount);
}

</script>

<style scoped>
.payment-method-card {
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid #dee2e6;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  background: #fff;
}
.payment-method-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
.payment-method-card.selected {
  border-color: #0d6efd;
  background-color: #f0f7ff;
}
.payment-icon {
  font-size: 2.5rem;
  margin-bottom: 10px;
  color: #0d6efd;
}
.payment-summary {
  background-color: #f8f9fa;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
}
.total-price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #dc3545;
}
</style>
