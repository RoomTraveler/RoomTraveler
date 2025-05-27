<template>
  <div class="container my-5">
    <h2 class="mb-4">예약 확인 및 결제</h2>

    <!-- 로딩 스피너 -->
    <div v-if="cartLoadingState || isProcessingPayment" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
      <p class="mt-2">{{ isProcessingPayment ? '결제를 진행 중입니다...' : '장바구니 정보를 불러오는 중입니다...' }}</p>
    </div>

    <!-- 페이지 에러 메시지 (paymentStore.error 또는 자체 componentError 사용) -->
    <div
      v-if="pageErrorDisplay && !cartLoadingState && !isProcessingPayment"
      class="alert alert-danger alert-dismissible fade show"
      role="alert"
    >
      {{ pageErrorDisplay }}
      <button type="button" class="btn-close" @click="clearPageError" aria-label="Close"></button>
    </div>

    <div v-if="!cartLoadingState && cartItems.length > 0" class="row">
      <!-- 예약 정보 및 폼 영역 -->
      <div class="col-lg-8">
        <!-- 예약 정보 카드 -->
        <div class="card mb-4">
          <div class="card-header">
            <h4 class="mb-0">예약 정보</h4>
          </div>
          <div class="card-body">
            <div
              v-for="(item, index) in cartItems"
              :key="item.cartItemId || item.roomId || index"
              class="mb-4 pb-3 border-bottom"
              :class="{ 'border-bottom-0': index === cartItems.length - 1 }"
            >
              <div class="row">
                <div class="col-md-4">
                  <img
                    :src="item.imageUrl || item.roomMainImageUrl || noImage"
                    :alt="item.roomName"
                    class="img-fluid rounded w-100"
                    style="height: 150px; object-fit: cover"
                  />
                </div>
                <div class="col-md-8">
                  <h5 class="fw-bold mb-1">{{ item.accommodationTitle }}</h5>
                  <h6 class="mb-2 text-muted">{{ item.roomName }}</h6>
                  <ul class="list-unstyled mb-0 small">
                    <li><strong>체크인:</strong> {{ item.checkInDate }}</li>
                    <li><strong>체크아웃:</strong> {{ item.checkOutDate }}</li>
                    <li><strong>숙박일수:</strong> {{ calculateNights(item.checkInDate, item.checkOutDate) }}박</li>
                    <li><strong>인원:</strong> {{ item.guestCount }}명</li>
                    <li><strong>가격:</strong> {{ formatCurrency(item.price) }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 특별 요청 사항 입력 -->
        <form @submit.prevent="processPaymentFromCart">
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
                  id="termsCheckCart"
                  v-model="termsAgreed"
                  required
                />
                <label class="form-check-label" for="termsCheckCart">
                  만 14세 이상이며 <a href="#" @click.prevent="showTermsModal = true">이용약관</a>에 동의합니다.
                </label>
              </div>
            </div>
          </div>

          <!-- 버튼 영역 -->
          <div class="d-flex justify-content-end gap-2">
            <router-link to="/cart" class="btn btn-outline-secondary" :class="{ 'disabled': isProcessingPayment }">장바구니로 돌아가기</router-link>
            <button
              type="submit"
              class="btn btn-primary"
              :disabled="!termsAgreed || cartItems.length === 0 || isProcessingPayment || cartLoadingState"
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
          <div v-if="cartItems && cartItems.length > 0">
            <p><strong>총 예약 건수:</strong> {{ cartItems.length }}건</p>
            <hr/>
            <h5 class="fw-bold">총 결제 금액: {{ formatCurrency(totalPrice) }}</h5>
          </div>
          <div v-else class="text-muted">
            <p>결제할 항목이 없습니다.</p>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!cartLoadingState && cartItems.length === 0 && !pageErrorDisplay" class="text-center py-5">
        <p class="text-muted fs-5">장바구니가 비어있습니다.</p>
        <p>숙소를 둘러보고 마음에 드는 객실을 담아보세요!</p>
        <router-link to="/accommodation/list" class="btn btn-primary mt-2">숙소 목록 보러가기</router-link>
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
            <!-- 약관 내용 (ReservationForm.vue와 동일하게 또는 별도 컴포넌트 사용) -->
            <p>1. 예약 확정 후 취소 시 환불 규정에 따라 수수료가 부과될 수 있습니다.</p>
            <p>2. 체크인 시간 및 체크아웃 시간은 각 숙소/객실 정보를 따릅니다.</p>
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
/**
 * 예약 확인/체크아웃 컴포넌트
 * - 장바구니 목록 및 총 가격 확인
 * - 특별 요청 입력, 약관 동의 및 결제(예약) 버튼 제공
 * - Bootstrap 기반 스타일
 */

import { ref, onMounted, computed, watch } from "vue";
import { useRouter } from "vue-router";
import { useCartStore } from "@/store/cartStore";
import { useUserStore } from "@/store/userStore";
import { usePaymentStore } from "@/store/paymentStore";
import { storeToRefs } from "pinia";


const noImage = "/img/no-image-icon.png";


const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();
const paymentStore = usePaymentStore();

const { cart, loading: cartStoreLoading, error: cartStoreError } = storeToRefs(cartStore);
const { user: currentUser, isAuthenticated } = storeToRefs(userStore);
const { isLoading: isProcessingPayment, error: paymentApiError } = storeToRefs(paymentStore);

const specialRequests = ref("");
const termsAgreed = ref(false);
const componentError = ref("");
const showTermsModal = ref(false);

const cartLoadingState = computed(() => cartStoreLoading.value);

const pageErrorDisplay = computed(() => componentError.value || paymentApiError.value || cartStoreError.value);

function clearPageError() {
  componentError.value = null;
  paymentStore.clearError();
  cartStore.clearError();
}

watch(cartStoreError, (newError) => {
  if (newError && !componentError.value && !paymentApiError.value) {
  }
});
watch(paymentApiError, (newError) => {
  if (newError && !componentError.value) {
  }
});

const cartItems = computed(() => cart.value?.items || []);
const totalPrice = computed(() => cart.value?.totalPrice || 0);

const VITE_PORTONE_IMP_CODE = import.meta.env.VITE_PORTONE_IMP_CODE;

const loadCartAndUser = async () => {
  clearPageError();
  try {
    if (!isAuthenticated.value) {
      await userStore.loadUserFromStorage();
      if (!isAuthenticated.value) {
        router.push({ name: "Login", query: { redirect: router.currentRoute.value.fullPath } });
        return;
      }
    }
    if (cartItems.value.length === 0) {
      await cartStore.fetchCart();
    }
  } catch (err) {
    console.error("Error in loadCartAndUser:", err);
  }
};

const validateReservations = (reservations) => {
  if (!reservations || reservations.length === 0) {
    return "결제할 항목이 없습니다.";
  }
  for (const item of reservations) {
    if (!item.roomId || !item.accommodationId || !item.checkInDate || !item.checkOutDate || item.guestCount == null || item.price == null) {
      return `'${item.roomName || item.accommodationTitle || '알 수 없는 항목'}'의 정보가 올바르지 않습니다. 장바구니를 확인해주세요.`;
    }
    if (new Date(item.checkInDate) >= new Date(item.checkOutDate)) {
      return `'${item.roomName || item.accommodationTitle}'의 체크아웃 날짜는 체크인 날짜 이후여야 합니다.`;
    }
    if (item.guestCount <= 0) {
      return `'${item.roomName || item.accommodationTitle}'의 인원수는 1명 이상이어야 합니다.`;
    }
    if (item.price <= 0) {
      return `'${item.roomName || item.accommodationTitle}'의 가격 정보가 올바르지 않습니다.`;
    }
  }
  return null;
};

const processPaymentFromCart = async () => {
  console.log('[CheckoutComponent] processPaymentFromCart function called.');

  clearPageError();

  if (!VITE_PORTONE_IMP_CODE) {
    componentError.value = "아임포트 가맹점 식별코드가 설정되지 않았습니다. 관리자에게 문의하세요.";
    console.error("[CheckoutComponent] Missing VITE_PORTONE_IMP_CODE");
    return;
  }
  if (!termsAgreed.value) {
    componentError.value = "이용약관에 동의해주세요.";
    console.error("[CheckoutComponent] Terms not agreed.");
    return;
  }

  const reservationsToCreate = cartItems.value.map(item => ({
    roomId: item.roomId,
    accommodationId: item.accommodationId,
    checkInDate: item.checkInDate,
    checkOutDate: item.checkOutDate,
    guestCount: item.guestCount,
    price: item.price,
    totalPrice: item.price,
    roomName: item.roomName || item.name,
  }));

  console.log("[CheckoutComponent] Generated reservationsToCreate (price field check):", JSON.parse(JSON.stringify(reservationsToCreate)));

  const validationError = validateReservations(reservationsToCreate);
  console.log("[CheckoutComponent] Validation result from validateReservations:", validationError);

  if (validationError) {
    componentError.value = validationError;
    console.error("[CheckoutComponent] Validation failed:", validationError);
    return;
  }
  
  if (specialRequests.value.length > 500) {
    componentError.value = "특별 요청 사항은 500자 이내로 작성해주세요.";
    console.error("[CheckoutComponent] Special requests too long.");
    return;
  }

  try {
    const prepareData = {
      reservationsToCreate: reservationsToCreate,
      specialRequests: specialRequests.value,
    };
    
    console.log('[CheckoutComponent] Data prepared for paymentStore.preparePayment (this log should now appear if validation passes):', JSON.parse(JSON.stringify(prepareData)));

    const paymentPrepareResponse = await paymentStore.preparePayment(prepareData);
    console.log('[CheckoutComponent] paymentStore.preparePayment raw response:', paymentPrepareResponse);

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
            
            await cartStore.fetchCart();
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

const calculateNights = (checkIn, checkOut) => {
  if (!checkIn || !checkOut) return 0;
  const date1 = new Date(checkIn);
  const date2 = new Date(checkOut);
  const diffTime = Math.abs(date2 - date1);
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
};

onMounted(loadCartAndUser);
</script>

<style scoped>
.card {
  border-radius: 0.75rem;
  box-shadow: 0 2px 10px rgba(0,0,0,0.07);
}
.position-sticky {
  top: 1rem;
  z-index: 2;
}
.modal.fade.show {
  display: block;
}
</style>
