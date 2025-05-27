<template>
  <div class="container my-5">
    <h2 class="mb-4">예약 결제</h2>

    <!-- 로딩 스피너 -->
    <div v-if="isLoading || isProcessingPayment" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
      <p class="mt-2">{{ isProcessingPayment ? '결제를 진행 중입니다...' : '예약 정보를 준비 중입니다...' }}</p>
    </div>

    <!-- 페이지 에러 메시지 -->
    <div 
      v-if="pageErrorDisplay && !isLoading && !isProcessingPayment"
      class="alert alert-danger alert-dismissible fade show"
      role="alert"
    >
      {{ pageErrorDisplay }}
      <button type="button" class="btn-close" @click="clearPageError" aria-label="Close"></button>
    </div>

    <div v-if="!isLoading && !pageErrorDisplay && reservationDetails" class="row">
      <!-- 예약 정보 및 폼 영역 -->
      <div class="col-lg-8">
        <!-- 예약 정보 카드 -->
        <div class="card mb-4">
          <div class="card-header">
            <h4 class="mb-0">예약 정보</h4>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-md-4">
                <img
                  :src="reservationDetails.roomMainImageUrl || noImage"
                  :alt="reservationDetails.roomName"
                  class="img-fluid rounded w-100"
                  style="height: 150px; object-fit: cover"
                />
              </div>
              <div class="col-md-8">
                <h5 class="fw-bold mb-1">{{ reservationDetails.accommodationTitle }}</h5>
                <h6 class="mb-2 text-muted">{{ reservationDetails.roomName }}</h6>
                <ul class="list-unstyled mb-0 small">
                  <li><strong>체크인:</strong> {{ reservationDetails.checkInDate }} ({{ reservationDetails.accommodationCheckInTime || '정보 없음' }})</li>
                  <li><strong>체크아웃:</strong> {{ reservationDetails.checkOutDate }} ({{ reservationDetails.accommodationCheckOutTime || '정보 없음' }})</li>
                  <li><strong>숙박일수:</strong> {{ nights }}박</li>
                  <li><strong>인원:</strong> {{ reservationDetails.guestCount }}명</li>
                  <li><strong>총 가격:</strong> {{ formatCurrency(totalPrice) }}</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <!-- 예약자 정보 -->
        <div class="card mb-4">
          <div class="card-header"><h4 class="mb-0">예약자 정보</h4></div>
          <div class="card-body">
            <div v-if="currentUser">
              <p><strong>이름:</strong> {{ currentUser.username || currentUser.name || '정보 없음' }}</p>
              <p><strong>이메일:</strong> {{ currentUser.email || '정보 없음' }}</p>
              <p><strong>연락처:</strong> {{ currentUser.phone || "정보 없음" }}</p>
            </div>
            <div v-else-if="!userStore.loading && !isAuthenticated">
                <p class="text-danger">예약자 정보를 불러올 수 없습니다. 로그인이 필요할 수 있습니다.</p>
            </div>
            <div v-else>
              <p class="text-muted">예약자 정보를 불러오는 중입니다...</p>
            </div>
          </div>
        </div>

        <!-- 특별 요청 사항 입력 -->
        <form @submit.prevent="processSinglePayment">
          <div class="card mb-4">
            <div class="card-header">
              <h4 class="mb-0">특별 요청 사항</h4>
            </div>
            <div class="card-body">
              <textarea
                v-model="specialRequests"
                class="form-control"
                rows="4"
                placeholder="호스트에게 전달할 특별 요청 사항이 있으면 입력해주세요. (선택사항)"
                maxlength="500"
              ></textarea>
              <small class="form-text text-muted">{{ specialRequests.length }}/500</small>
            </div>
          </div>

          <!-- 이용 약관 동의 -->
          <div class="card mb-4">
            <div class="card-header">
              <h4 class="mb-0">이용 약관</h4>
            </div>
            <div class="card-body">
              <div class="form-check">
                <input
                  class="form-check-input"
                  type="checkbox"
                  id="termsCheckReservation"
                  v-model="termsAgreed"
                  required
                />
                <label class="form-check-label" for="termsCheckReservation">
                  만 14세 이상이며 <a href="#" @click.prevent="showTermsModal = true">이용약관</a>에 동의합니다.
                </label>
              </div>
            </div>
          </div>

          <!-- 버튼 영역 -->
          <div class="d-flex justify-content-end gap-2">
            <button
              type="button"
              class="btn btn-outline-secondary"
              @click="router.go(-1)"
              :disabled="isProcessingPayment || isLoading"
            >
              이전으로
            </button>
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="!termsAgreed || isProcessingPayment || isLoading || !reservationDetails || !currentUser"
            >
              <span
                v-if="isProcessingPayment"
                class="spinner-border spinner-border-sm me-1"
                role="status"
                aria-hidden="true"
              ></span>
              {{ formatCurrency(totalPrice) }} 결제하기
            </button>
          </div>
        </form>
      </div>

      <!-- 결제 요약 영역 -->
      <div class="col-lg-4">
        <div class="card p-4 position-sticky" style="top: 80px">
          <h4 class="mb-3">결제 요약</h4>
          <div v-if="reservationDetails">
            <p><strong>숙소:</strong> {{ reservationDetails.accommodationTitle }}</p>
            <p><strong>객실:</strong> {{ reservationDetails.roomName }}</p>
            <p><strong>체크인:</strong> {{ reservationDetails.checkInDate }}</p>
            <p><strong>체크아웃:</strong> {{ reservationDetails.checkOutDate }}</p>
            <p><strong>숙박일수:</strong> {{ nights }}박</p>
            <p><strong>인원:</strong> {{ reservationDetails.guestCount }}명</p>
            <hr />
            <h5 class="fw-bold">총 결제 금액: {{ formatCurrency(totalPrice) }}</h5>
          </div>
          <div v-else>
            <p class="text-muted">예약 정보가 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
     <div v-if="!isLoading && !pageErrorDisplay && !reservationDetails" class="text-center py-5">
        <p class="text-muted fs-5">잘못된 접근이거나 예약 정보가 없습니다.</p>
        <p>이전 페이지로 돌아가 다시 시도해주세요.</p>
        <button type="button" class="btn btn-primary mt-2" @click="router.go(-1)">이전 페이지로</button>
    </div>

    <!-- 이용약관 모달 -->
    <div v-if="showTermsModal" class="modal fade show d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-dialog-scrollable modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">이용약관</h5>
            <button type="button" class="btn-close" @click="showTermsModal = false"></button>
          </div>
          <div class="modal-body">
            <p>1. 예약 확정 후 취소 시 환불 규정에 따라 수수료가 부과될 수 있습니다.</p>
            <p>2. 체크인 시간은 {{ reservationDetails?.accommodationCheckInTime || '숙소 정보 확인' }}, 체크아웃 시간은 {{ reservationDetails?.accommodationCheckOutTime || '숙소 정보 확인' }}입니다.</p>
            <p><strong>개인정보 수집 및 이용 동의</strong></p>
            <p>회사는 다음과 같은 목적으로 개인정보를 수집 및 이용합니다.</p>
            <p>- 수집 항목: 이름, 연락처, 이메일 주소</p>
            <p>- 이용 목적: 예약 확인 및 안내, 서비스 제공, 고객 상담</p>
            <p>- 보유 및 이용 기간: 법령에 따른 보존 기간 또는 동의 철회 시까지</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-primary" @click="showTermsModal = false">확인</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { useUserStore } from "@/store/userStore";
import { usePaymentStore } from "@/store/paymentStore";
import { storeToRefs } from "pinia";
import noImage from "@/assets/no-image.jpg";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const paymentStore = usePaymentStore();

const { user: currentUser, isAuthenticated, loading: userLoading } = storeToRefs(userStore);
const { isLoading: isProcessingPayment, error: paymentApiError } = storeToRefs(paymentStore);

const isLoading = ref(true); // 페이지 초기 데이터 로딩 상태
const componentError = ref(null); // 컴포넌트 자체 유효성 검사 오류 등
const reservationDetails = ref(null);
const specialRequests = ref("");
const termsAgreed = ref(false);
const showTermsModal = ref(false);

const VITE_PORTONE_IMP_CODE = import.meta.env.VITE_PORTONE_IMP_CODE;

// 에러 메시지 통합 표시 로직
const pageErrorDisplay = computed(() => componentError.value || paymentApiError.value );

function clearPageError() {
  componentError.value = null;
  paymentStore.clearError();
}

watch(paymentApiError, (newError) => {
  if (newError && !componentError.value) {
    // paymentStore에서 발생한 에러를 pageErrorDisplay를 통해 보여줌
  }
});

const nights = computed(() => {
  if (reservationDetails.value?.checkInDate && reservationDetails.value?.checkOutDate) {
    try {
      const checkIn = new Date(reservationDetails.value.checkInDate);
      const checkOut = new Date(reservationDetails.value.checkOutDate);
      if (isNaN(checkIn.getTime()) || isNaN(checkOut.getTime())) return 0;
      const diffTime = checkOut.getTime() - checkIn.getTime();
      return Math.max(0, Math.ceil(diffTime / (1000 * 60 * 60 * 24)));
    } catch (e) {
      console.error("Error calculating nights:", e);
      return 0;
    }
  }
  return 0;
});

const totalPrice = computed(() => {
  if (reservationDetails.value?.price && nights.value > 0) {
    const pricePerNight = parseFloat(reservationDetails.value.price);
    if (isNaN(pricePerNight)) return 0;
    return pricePerNight * nights.value;
  }
  return 0;
});

const validateReservationDetails = (details) => {
  if (!details) return "예약 정보를 불러올 수 없습니다.";
  if (!details.roomId || !details.accommodationId) return "객실 또는 숙소 정보가 올바르지 않습니다.";
  if (!details.checkInDate || !details.checkOutDate) return "체크인 또는 체크아웃 날짜가 올바르지 않습니다.";
  if (new Date(details.checkInDate) >= new Date(details.checkOutDate)) return "체크아웃 날짜는 체크인 날짜 이후여야 합니다.";
  if (!details.guestCount || parseInt(details.guestCount, 10) <= 0) return "인원 정보가 올바르지 않습니다.";
  if (details.price == null || parseFloat(details.price) < 0) return "가격 정보가 올바르지 않습니다."; // 0원일 수도 있으므로 < 0 검사
  return null;
};

onMounted(async () => {
  isLoading.value = true;
  clearPageError();
  try {
    if (!isAuthenticated.value) {
      await userStore.loadUserFromStorage();
      if (!isAuthenticated.value) {
        router.push({ name: "Login", query: { redirect: route.fullPath } });
        isLoading.value = false;
        return;
      }
    }

    const query = route.query;
    const parsedDetails = {
      roomId: query.roomId ? parseInt(query.roomId, 10) : null,
      accommodationId: query.accommodationId ? parseInt(query.accommodationId, 10) : null,
      checkInDate: query.checkInDate,
      checkOutDate: query.checkOutDate,
      guestCount: query.guestCount ? parseInt(query.guestCount, 10) : null,
      price: query.price ? parseFloat(query.price) : null,
      roomName: query.roomName || "객실 정보 없음",
      accommodationTitle: query.accommodationTitle || "숙소 정보 없음",
      roomMainImageUrl: query.roomMainImageUrl || noImage,
      accommodationCheckInTime: query.accommodationCheckInTime,
      accommodationCheckOutTime: query.accommodationCheckOutTime,
    };

    const validationError = validateReservationDetails(parsedDetails);
    if (validationError) {
      componentError.value = validationError;
      reservationDetails.value = null; // 유효하지 않은 정보는 표시하지 않음
    } else {
      reservationDetails.value = parsedDetails;
    }

  } catch (err) {
    console.error("Error in ReservationForm onMounted:", err);
    componentError.value = "예약 정보를 불러오는 중 오류가 발생했습니다: " + (err.message || "알 수 없는 오류");
  } finally {
    isLoading.value = false;
  }
});

const processSinglePayment = async () => {
  clearPageError();

  if (!VITE_PORTONE_IMP_CODE) {
    componentError.value = "아임포트 가맹점 식별코드가 설정되지 않았습니다. 관리자에게 문의하세요.";
    return;
  }
  if (!termsAgreed.value) {
    componentError.value = "이용약관에 동의해주세요.";
    return;
  }
  if (!reservationDetails.value || !currentUser.value) {
    componentError.value = "결제에 필요한 예약 정보 또는 사용자 정보가 없습니다.";
    return;
  }
  const validationError = validateReservationDetails(reservationDetails.value);
  if (validationError) {
    componentError.value = validationError;
    return;
  }
  if (specialRequests.value.length > 500) {
      componentError.value = "특별 요청 사항은 500자 이내로 작성해주세요.";
      return;
  }

  try {
    const reservationToCreate = {
      roomId: reservationDetails.value.roomId,
      accommodationId: reservationDetails.value.accommodationId,
      checkInDate: reservationDetails.value.checkInDate,
      checkOutDate: reservationDetails.value.checkOutDate,
      guestCount: reservationDetails.value.guestCount,
      totalPrice: totalPrice.value, // nights * price 로 계산된 총액
      // roomName, accommodationTitle 등은 서버에서 필요시 ID로 조회 가능
    };

    const prepareData = {
      reservationsToCreate: [reservationToCreate], // API는 리스트를 받으므로 단일 아이템 리스트로 전달
      specialRequests: specialRequests.value,
    };
    
    const paymentPrepareResponse = await paymentStore.preparePayment(prepareData);
    const { merchantUid, amount, paymentName } = paymentPrepareResponse;
    const buyerEmail = currentUser.value?.email || "guest@example.com";
    const buyerName = currentUser.value?.username || currentUser.value?.name || "비회원";
    const buyerTel = currentUser.value?.phone || "010-0000-0000";

    const { IMP } = window;
    IMP.init(VITE_PORTONE_IMP_CODE);

    IMP.request_pay(
      {
        pg: "html5_inicis.INIpayTest",
        pay_method: "card",
        merchant_uid: merchantUid,
        name: paymentName,
        amount: amount,
        buyer_email: buyerEmail,
        buyer_name: buyerName,
        buyer_tel: buyerTel,
      },
      async (rsp) => {
        if (rsp.success) {
          try {
            const completeData = {
              impUid: rsp.imp_uid,
              merchantUid: rsp.merchant_uid,
            };
            const paymentCompleteResponse = await paymentStore.completePayment(completeData);
            
            router.push({
              name: "PaymentResult",
              query: {
                imp_uid: rsp.imp_uid,
                merchant_uid: rsp.merchant_uid,
                message: paymentCompleteResponse.message || "결제가 성공적으로 완료되었습니다.",
                paymentId: paymentCompleteResponse.paymentId,
              },
            });
          } catch (completeError) {
            console.error("Payment completion error:", completeError);
            // paymentStore.error에 이미 오류가 설정됨
          }
        } else {
          console.error("Iamport payment failed:", rsp);
          let errorMessage = `결제 실패: ${rsp.error_msg || '알 수 없는 사유로 결제가 실패했습니다.'}`;
          if (rsp.imp_uid) errorMessage += ` (거래번호: ${rsp.imp_uid})`;
          componentError.value = errorMessage;
          if (merchantUid) {
             await paymentStore.cancelPreparedReservations(merchantUid);
          }
        }
      }
    );
  } catch (prepareError) {
    console.error("Payment preparation error in component:", prepareError);
    // paymentStore.error에 이미 오류가 설정됨
  }
};

const formatCurrency = (value) => {
  if (typeof value !== "number" || isNaN(value)) return "가격 정보 없음";
  return new Intl.NumberFormat("ko-KR", {
    style: "currency",
    currency: "KRW",
    maximumFractionDigits: 0,
  }).format(value);
};
</script>

<style scoped>
.card {
  border-radius: 0.75rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
}
.position-sticky {
  top: 1rem; /* nav 높이 고려 */
  z-index: 2;
}
.modal.fade.show {
  display: block;
}
/* 추가적인 스타일링은 CartCheckout.vue 또는 전역 스타일 참조 */
</style>
