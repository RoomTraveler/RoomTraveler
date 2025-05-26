<template>
  <div class="container my-5">
    <h2 class="mb-4">예약 확인</h2>

    <!-- 로딩 스피너 -->
    <div v-if="cartLoading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
      <p class="mt-2">장바구니 정보를 불러오는 중입니다...</p>
    </div>

    <!-- 에러 메시지 -->
    <div
        vif="componentError"
        class="alert alert-danger alert-dismissible fade show"
        role="alert"
    >
      {{ componentError }}
      <button type="button" class="btn-close" @click="componentError = ''" aria-label="Close"></button>
    </div>

    <div v-if="!cartLoading && !componentError" class="row">
      <!-- 예약 정보 및 폼 영역 -->
      <div class="col-lg-8">
        <!-- 예약 정보 카드 -->
        <div class="card mb-4">
          <div class="card-header">
            <h4 class="mb-0">예약 정보</h4>
          </div>
          <div class="card-body">
            <div v-if="!cartItems || cartItems.length === 0" class="text-center py-3">
              <p class="text-muted">예약할 항목이 장바구니에 없습니다. 먼저 객실을 추가해주세요.</p>
              <router-link to="/accommodation/list" class="btn btn-sm btn-outline-primary">객실 둘러보기</router-link>
            </div>
            <div
                v-for="(item, index) in cartItems"
                :key="item.cartItemId || index"
                class="mb-4 pb-3 border-bottom"
                :class="{ 'border-bottom-0': index === cartItems.length - 1 }"
            >
              <div class="row">
                <div class="col-md-4">
                  <img
                      :src="item.imageUrl || noImage"
                      :alt="item.roomName"
                      class="img-fluid rounded w-100"
                      style="height: 150px; object-fit: cover;"
                  />
                </div>
                <div class="col-md-8">
                  <h5 class="fw-bold mb-1">{{ item.accommodationTitle }}</h5>
                  <h6 class="mb-2 text-muted">{{ item.roomName }}</h6>
                  <ul class="list-unstyled mb-0 small">
                    <li><strong>체크인:</strong> {{ item.checkInDate }}</li>
                    <li><strong>체크아웃:</strong> {{ item.checkOutDate }}</li>
                    <li><strong>숙박일수:</strong> {{ item.nights }}박</li>
                    <li><strong>인원:</strong> {{ item.guestCount }}명</li>
                    <li><strong>가격:</strong> {{ formatCurrency(item.price) }}</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 특별 요청 사항 입력 -->
        <form @submit.prevent="processPayment">
          <div class="card mb-4">
            <div class="card-header">
              <h4 class="mb-0">특별 요청 사항</h4>
            </div>
            <div class="card-body">
              <textarea
                  v-model="specialRequests"
                  class="form-control"
                  rows="4"
                  placeholder="호스트에게 전달할 특별 요청 사항이 있으면 입력해주세요."
                  :disabled="!cartItems || cartItems.length === 0"
              ></textarea>
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
                    id="termsCheck"
                    v-model="termsAgreed"
                    required
                    :disabled="!cartItems || cartItems.length === 0"
                />
                <label class="form-check-label" for="termsCheck">
                  만 14세 이상이며 이용약관에 동의합니다.
                </label>
              </div>
            </div>
          </div>

          <!-- 버튼 영역 -->
          <div class="d-flex justify-content-end gap-2">
            <router-link to="/cart" class="btn btn-outline-secondary">장바구니로 돌아가기</router-link>
            <button 
              type="submit" 
              class="btn btn-primary" 
              :disabled="!termsAgreed || !cartItems || cartItems.length === 0 || isLoadingPayment"
            >
              <span v-if="isLoadingPayment" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
              결제하기
            </button>
          </div>
        </form>
      </div>

      <!-- 결제 요약 영역 -->
      <div class="col-lg-4">
        <div class="card p-4 position-sticky" style="top: 80px;">
          <h4 class="mb-3">결제 요약</h4>
          <div v-if="cartItems && cartItems.length > 0">
          <p><strong>총 객실 수:</strong> {{ cartItems.length }}개</p>
          <p><strong>총 가격:</strong> {{ formatCurrency(totalPrice) }}</p>
          </div>
          <div v-else class="text-muted">
            <p>결제할 항목이 없습니다.</p>
          </div>
          <hr />
          <p class="mb-0"><small>* 결제는 예약 완료 후 진행됩니다.</small></p>
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

import { ref, onMounted, computed } from "vue";
import { useRouter } from "vue-router";
import { useCartStore } from "@/store/cartStore";
import { useUserStore } from "@/store/userStore";
import { storeToRefs } from "pinia";
import noImage from "@/assets/no-image.jpg";

const router = useRouter();
const cartStore = useCartStore();
const userStore = useUserStore();

const { cart, loading: cartLoading, selectedCartItemIds } = storeToRefs(cartStore);
const { user: currentUser, isAuthenticated } = storeToRefs(userStore);

const specialRequests = ref("");
const termsAgreed = ref(false);
const componentError = ref("");
const isLoadingPayment = ref(false);

const cartItems = computed(() => cart.value.items || []);
const totalPrice = computed(() => cart.value.totalPrice || 0);

const VITE_PORTONE_IMP_CODE = import.meta.env.VITE_PORTONE_IMP_CODE;

const loadCartAndUser = async () => {
  componentError.value = "";
  isLoadingPayment.value = true;
  try {
    if (!isAuthenticated.value) {
      router.push({ name: "Login", query: { redirect: router.currentRoute.value.fullPath } });
      return;
    }
    await cartStore.fetchCart();
    if (cartStore.error) {
      componentError.value = cartStore.error;
    }
  } catch (err) {
    componentError.value = "데이터 로딩 중 오류가 발생했습니다: " + (err.message || "");
  } finally {
    isLoadingPayment.value = false;
  }
};

const processPayment = async () => {
  if (!VITE_PORTONE_IMP_CODE) {
    componentError.value = "아임포트 가맹점 식별코드가 설정되지 않았습니다. 관리자에게 문의하세요.";
    return;
  }

  if (!termsAgreed.value) {
    componentError.value = "이용약관에 동의해주세요.";
    return;
  }
  if (!cartItems.value || cartItems.value.length === 0) {
    componentError.value = "결제할 항목이 장바구니에 없습니다.";
    return;
  }

  componentError.value = "";
  isLoadingPayment.value = true;

  try {
    const prepareData = {
      cartItemIds: selectedCartItemIds.value,
      specialRequests: specialRequests.value,
    };
    const paymentPrepareResponse = await cartStore.preparePaymentForCheckout(prepareData);

    const { merchantUid, amount, paymentName } = paymentPrepareResponse;

    const buyerEmail = currentUser.value?.email || 'guest@example.com';
    const buyerName = currentUser.value?.username || currentUser.value?.name || '비회원';
    const buyerTel = currentUser.value?.phone || '010-0000-0000';

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
            const paymentCompleteResponse = await cartStore.completePaymentAfterIamport(completeData);
            
            await cartStore.fetchCart();
            router.push({
              name: "PaymentResult",
              query: {
                imp_uid: rsp.imp_uid,
                merchant_uid: rsp.merchant_uid,
                message: paymentCompleteResponse.message || "결제가 성공적으로 완료되었습니다.",
                paymentId: paymentCompleteResponse.paymentId
              },
            });
          } catch (completeError) {
            console.error("Payment completion error:", completeError);
            componentError.value = `결제는 성공했지만 서버 처리 중 오류 발생: ${completeError.message || completeError}. 관리자에게 문의하거나 잠시 후 다시 시도해주세요. (주문번호: ${rsp.merchant_uid}, 아임포트 거래번호: ${rsp.imp_uid})`;
          }
        } else {
          console.error("Iamport payment failed:", rsp.error_msg);
          let errorMessage = `결제 실패: ${rsp.error_msg}`;
          if (rsp.imp_uid) {
            errorMessage += ` (아임포트 거래번호: ${rsp.imp_uid})`;
          }
          componentError.value = errorMessage;
        }
        isLoadingPayment.value = false;
      }
    );
  } catch (prepareError) {
    console.error("Payment preparation error:", prepareError);
    componentError.value = `결제 준비 중 오류가 발생했습니다: ${prepareError.message || prepareError}`;
    isLoadingPayment.value = false;
  }
};

const formatCurrency = (value) => {
  if (typeof value !== 'number') return '가격 정보 없음';
  return new Intl.NumberFormat("ko-KR", {
    style: "currency",
    currency: "KRW",
    maximumFractionDigits: 0,
  }).format(value);
};

onMounted(loadCartAndUser);
</script>

<style scoped>
/* Bootstrap 위에 필요한 최소한의 커스텀만 보강 */
.card {
  border-radius: 0.75rem;
}
.position-sticky {
  z-index: 2;
}
</style>
