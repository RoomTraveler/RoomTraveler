<template>
  <div class="container my-5">
    <h2 class="mb-4">예약 확인</h2>
    
    <!-- 에러 메시지 -->
    <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
      {{ error }}
      <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
    </div>
    
    <div class="row">
      <div class="col-md-8">
        <div class="card mb-4">
          <div class="card-header">
            <h4>예약 정보</h4>
          </div>
          <div class="card-body">
            <div v-for="(item, index) in cartItems" :key="index" class="checkout-item">
              <div class="row">
                <div class="col-md-4">
                  <img 
                    :src="item.imageUrl || require('@/assets/no-image.jpg')" 
                    :alt="item.roomName" 
                    class="checkout-item-image"
                  >
                </div>
                <div class="col-md-8">
                  <h5>{{ item.accommodationTitle }}</h5>
                  <h6>{{ item.roomName }}</h6>
                  <p>
                    <strong>체크인:</strong> {{ item.checkInDate }}<br>
                    <strong>체크아웃:</strong> {{ item.checkOutDate }}<br>
                    <strong>숙박일수:</strong> {{ item.nights }}박<br>
                    <strong>인원:</strong> {{ item.guestCount }}명<br>
                    <strong>가격:</strong> {{ formatCurrency(item.price) }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <form @submit.prevent="createReservation">
          <div class="card mb-4">
            <div class="card-header">
              <h4>특별 요청 사항</h4>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <textarea 
                  v-model="specialRequests" 
                  class="form-control" 
                  rows="4" 
                  placeholder="호스트에게 전달할 특별 요청 사항이 있으면 입력해주세요."
                ></textarea>
              </div>
            </div>
          </div>
          
          <div class="card mb-4">
            <div class="card-header">
              <h4>이용 약관</h4>
            </div>
            <div class="card-body">
              <div class="form-check mb-3">
                <input 
                  class="form-check-input" 
                  type="checkbox" 
                  id="termsCheck" 
                  v-model="termsAgreed" 
                  required
                >
                <label class="form-check-label" for="termsCheck">
                  만 14세 이상이며 이용약관에 동의합니다.
                </label>
              </div>
            </div>
          </div>
          
          <div class="d-grid gap-2 d-md-flex justify-content-md-end">
            <router-link to="/cart" class="btn btn-outline-secondary me-md-2">장바구니로 돌아가기</router-link>
            <button type="submit" class="btn btn-primary" :disabled="!termsAgreed">결제하기</button>
          </div>
        </form>
      </div>
      
      <div class="col-md-4">
        <div class="checkout-summary">
          <h4 class="mb-3">결제 요약</h4>
          <p><strong>총 객실 수:</strong> {{ cartItems.length }}개</p>
          <p><strong>총 가격:</strong> {{ formatCurrency(totalPrice) }}</p>
          <hr>
          <p class="mb-0"><small>* 결제는 예약 완료 후 진행됩니다.</small></p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CartCheckout',
  data() {
    return {
      // 장바구니 아이템 목록
      cartItems: [],
      // 총 가격
      totalPrice: 0,
      // 특별 요청 사항
      specialRequests: '',
      // 이용약관 동의 여부
      termsAgreed: false,
      // 에러 메시지
      error: ''
    };
  },
  created() {
    // 장바구니 아이템 로드
    this.loadCartItems();
  },
  methods: {
    // 장바구니 아이템 로드
    async loadCartItems() {
      try {
        // API 호출
        const response = await fetch('/api/cart/items');
        if (!response.ok) {
          throw new Error('장바구니 정보를 불러오는데 실패했습니다.');
        }
        
        const data = await response.json();
        this.cartItems = data.items;
        this.totalPrice = data.totalPrice;
        
        // 장바구니가 비어있는 경우
        if (this.cartItems.length === 0) {
          this.$router.push('/cart');
        }
      } catch (error) {
        console.error('장바구니 정보 로드 중 오류가 발생했습니다:', error);
        this.error = '장바구니 정보를 불러오는데 실패했습니다.';
      }
    },
    
    // 예약 생성
    async createReservation() {
      if (!this.termsAgreed) {
        this.error = '이용약관에 동의해주세요.';
        return;
      }
      
      try {
        // API 호출
        const response = await fetch('/api/reservations/create-from-cart', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            specialRequests: this.specialRequests
          })
        });
        
        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.message || '예약 생성에 실패했습니다.');
        }
        
        const data = await response.json();
        
        // 결제 페이지로 이동
        this.$router.push(`/payment/form/${data.reservationId}`);
      } catch (error) {
        console.error('예약 생성 중 오류가 발생했습니다:', error);
        this.error = error.message || '예약 생성에 실패했습니다.';
      }
    },
    
    // 금액 포맷팅
    formatCurrency(amount) {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
        maximumFractionDigits: 0
      }).format(amount);
    }
  }
};
</script>

<style scoped>
.checkout-item {
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
  padding: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}
.checkout-summary {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  position: sticky;
  top: 20px;
}
.checkout-item-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 4px;
}
</style>