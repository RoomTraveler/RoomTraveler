<template>
  <div class="container my-5">
    <h2 class="mb-4">예약 확인</h2>

    <!-- 에러 메시지 -->
    <div
        v-if="error"
        class="alert alert-danger alert-dismissible fade show"
        role="alert"
    >
      {{ error }}
      <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
    </div>

    <div class="row">
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
                :key="index"
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
        <form @submit.prevent="createReservation">
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
            <button type="submit" class="btn btn-primary" :disabled="!termsAgreed">결제하기</button>
          </div>
        </form>
      </div>

      <!-- 결제 요약 영역 -->
      <div class="col-lg-4">
        <div class="card p-4 position-sticky" style="top: 80px;">
          <h4 class="mb-3">결제 요약</h4>
          <p><strong>총 객실 수:</strong> {{ cartItems.length }}개</p>
          <p><strong>총 가격:</strong> {{ formatCurrency(totalPrice) }}</p>
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

import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

// 기본 no-image 경로
import noImage from "@/assets/no-image.jpg";

// 반응형 상태
const cartItems = ref([]);
const totalPrice = ref(0);
const specialRequests = ref("");
const termsAgreed = ref(false);
const error = ref("");

const router = useRouter();

// 장바구니 데이터 불러오기
const loadCartItems = async () => {
  try {
    const response = await fetch("/api/cart/items");
    if (!response.ok) {
      throw new Error("장바구니 정보를 불러오는데 실패했습니다.");
    }
    const data = await response.json();
    cartItems.value = data.items || [];
    totalPrice.value = data.totalPrice || 0;

    // 장바구니가 비었을 경우
    if (!cartItems.value.length) {
      router.push("/cart");
    }
  } catch (err) {
    error.value = "장바구니 정보를 불러오는데 실패했습니다.";
  }
};

// 예약 생성(결제 버튼 클릭 시)
const createReservation = async () => {
  if (!termsAgreed.value) {
    error.value = "이용약관에 동의해주세요.";
    return;
  }

  try {
    const response = await fetch("/api/reservations/create-from-cart", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ specialRequests: specialRequests.value }),
    });
    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || "예약 생성에 실패했습니다.");
    }
    const data = await response.json();
    // 결제 페이지로 이동
    router.push(`/payment/form/${data.reservationId}`);
  } catch (err) {
    error.value = err.message || "예약 생성에 실패했습니다.";
  }
};

// 가격 포맷 함수
const formatCurrency = (amount) => {
  return new Intl.NumberFormat("ko-KR", {
    style: "currency",
    currency: "KRW",
    maximumFractionDigits: 0,
  }).format(amount);
};

// 컴포넌트 마운트 시 장바구니 불러오기
onMounted(loadCartItems);
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
