<template>
  <Layout>
    <div class="container my-5">
      <h2 class="mb-4">장바구니</h2>

      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>

      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>

      <!-- 장바구니가 비어있을 때 -->
      <div v-if="!cart.items || cart.items.length === 0" class="empty-cart">
        <i class="bi bi-cart"></i>
        <h3>장바구니가 비어있습니다</h3>
        <p>객실을 장바구니에 추가해보세요!</p>
        <router-link to="/accommodation/list" class="btn btn-primary">객실 둘러보기</router-link>
      </div>

      <!-- 장바구니에 항목이 있을 때 -->
      <div v-else class="row">
        <div class="col-md-8">
          <div v-for="item in cart.items" :key="item.cartItemId" class="cart-item">
            <div class="row">
              <div class="col-md-4">
                <img 
                  :src="item.imageUrl || require('@/assets/no-image.jpg')" 
                  :alt="item.roomName" 
                  class="cart-item-image"
                >
              </div>
              <div class="col-md-8">
                <h4>{{ item.accommodationTitle }}</h4>
                <h5>{{ item.roomName }}</h5>
                <p>
                  <strong>체크인:</strong> {{ item.checkInDate }}<br>
                  <strong>체크아웃:</strong> {{ item.checkOutDate }}<br>
                  <strong>숙박일수:</strong> {{ item.nights }}박<br>
                  <strong>인원:</strong> {{ item.guestCount }}명<br>
                  <strong>가격:</strong> {{ formatPrice(item.price) }}
                </p>
                <div class="d-flex justify-content-end">
                  <button @click="removeItem(item.cartItemId)" class="btn btn-outline-danger">삭제</button>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="col-md-4">
          <div class="cart-summary">
            <h4 class="mb-3">주문 요약</h4>
            <p><strong>총 객실 수:</strong> {{ cart.totalItems }}개</p>
            <p><strong>총 가격:</strong> {{ formatPrice(cart.totalPrice) }}</p>

            <button @click="checkout" class="btn btn-primary w-100 mb-2">예약하기</button>
            <button @click="clearCart" class="btn btn-outline-secondary w-100">장바구니 비우기</button>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 장바구니 컴포넌트
 * 
 * 이 컴포넌트는 사용자의 장바구니를 표시하고 관리합니다.
 * 장바구니 항목 조회, 삭제, 장바구니 비우기, 예약하기 기능을 제공합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'Cart',
  components: {
    Layout
  },
  data() {
    return {
      message: '',
      error: ''
    };
  },
  computed: {
    ...mapState({
      cart: state => state.cart.cart,
      isLoggedIn: state => state.user.isLoggedIn
    })
  },
  created() {
    // 로그인 상태 확인
    if (!this.isLoggedIn) {
      this.$router.push('/user/login');
      return;
    }
    
    // 장바구니 정보 로드
    this.loadCart();
    
    // URL 쿼리 파라미터에서 메시지 또는 에러 확인
    if (this.$route.query.message) {
      this.message = this.$route.query.message;
    }
    if (this.$route.query.error) {
      this.error = this.$route.query.error;
    }
  },
  methods: {
    ...mapActions('cart', ['fetchCart', 'removeCartItem', 'clearCartItems', 'checkoutCart']),
    
    /**
     * 장바구니 정보 로드
     */
    async loadCart() {
      try {
        await this.fetchCart();
      } catch (error) {
        console.error('장바구니 정보를 불러오는 중 오류가 발생했습니다:', error);
        this.error = '장바구니 정보를 불러올 수 없습니다. 다시 시도해주세요.';
      }
    },
    
    /**
     * 장바구니 항목 삭제
     * @param {number} cartItemId - 삭제할 장바구니 항목 ID
     */
    async removeItem(cartItemId) {
      try {
        await this.removeCartItem(cartItemId);
        this.message = '장바구니에서 항목이 삭제되었습니다.';
      } catch (error) {
        console.error('장바구니 항목 삭제 중 오류가 발생했습니다:', error);
        this.error = '장바구니 항목을 삭제할 수 없습니다. 다시 시도해주세요.';
      }
    },
    
    /**
     * 장바구니 비우기
     */
    async clearCart() {
      if (confirm('장바구니를 비우시겠습니까?')) {
        try {
          await this.clearCartItems();
          this.message = '장바구니가 비워졌습니다.';
        } catch (error) {
          console.error('장바구니 비우기 중 오류가 발생했습니다:', error);
          this.error = '장바구니를 비울 수 없습니다. 다시 시도해주세요.';
        }
      }
    },
    
    /**
     * 예약하기
     */
    async checkout() {
      try {
        await this.checkoutCart();
        this.$router.push('/reservation/cart-checkout');
      } catch (error) {
        console.error('예약 처리 중 오류가 발생했습니다:', error);
        this.error = '예약 처리에 실패했습니다. 다시 시도해주세요.';
      }
    },
    
    /**
     * 가격 포맷팅 (₩1,000,000 형식)
     * @param {number} price - 포맷팅할 가격
     * @returns {string} 포맷팅된 가격 문자열
     */
    formatPrice(price) {
      return new Intl.NumberFormat('ko-KR', { 
        style: 'currency', 
        currency: 'KRW',
        maximumFractionDigits: 0 
      }).format(price);
    }
  }
};
</script>

<style scoped>
/* 장바구니 스타일 */
.cart-item {
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
  padding: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.cart-item-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 4px;
}

.cart-summary {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 20px;
  position: sticky;
  top: 20px;
}

.empty-cart {
  text-align: center;
  padding: 50px 0;
}

.empty-cart i {
  font-size: 48px;
  color: #ccc;
  margin-bottom: 20px;
}
</style>