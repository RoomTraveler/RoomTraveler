<template>
  <div class="container mt-5 mb-5">
    <!-- 알림 메시지 -->
    <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
      {{ message }}
      <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
    </div>
    
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h2>예약 상세</h2>
      <div>
        <router-link to="/reservation/my-reservations" class="btn btn-outline-secondary">
          <i class="bi bi-arrow-left"></i> 예약 목록으로
        </router-link>
      </div>
    </div>
    
    <!-- 예약 상태 -->
    <div class="row mb-4">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center">
              <h4 class="card-title">예약 번호: {{ reservation.reservationId }}</h4>
              <div>
                <span :class="getStatusBadgeClass(reservation.status)">
                  {{ getStatusText(reservation.status) }}
                </span>
                
                <span :class="getPaymentStatusBadgeClass(reservation.paymentStatus)" class="ms-2">
                  {{ getPaymentStatusText(reservation.paymentStatus) }}
                </span>
              </div>
            </div>
            <p class="text-muted">예약일: {{ formatDate(reservation.createdAt) }}</p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 예약 정보 -->
    <div class="row mb-4">
      <div class="col-md-8">
        <div class="card reservation-card">
          <div class="card-body">
            <h4 class="card-title">예약 정보</h4>
            <div class="row">
              <div class="col-md-4">
                <img 
                  :src="room.mainImageUrl || require('@/assets/no-image.jpg')" 
                  class="img-fluid rounded" 
                  :alt="room.name"
                >
              </div>
              <div class="col-md-8">
                <h5>{{ room.name }}</h5>
                <p class="text-muted">{{ accommodation.title }}</p>
                <p><i class="bi bi-geo-alt"></i> {{ accommodation.address }}</p>
                <p><i class="bi bi-calendar-check"></i> 체크인: <strong>{{ formatDate(reservation.checkInDate, 'date-only') }}</strong> ({{ accommodation.checkInTime }})</p>
                <p><i class="bi bi-calendar-x"></i> 체크아웃: <strong>{{ formatDate(reservation.checkOutDate, 'date-only') }}</strong> ({{ accommodation.checkOutTime }})</p>
                <p><i class="bi bi-people"></i> 인원: <strong>{{ reservation.guestCount }}명</strong></p>
                <p><i class="bi bi-telephone"></i> 숙소 연락처: {{ accommodation.phone }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 예약자 정보 -->
        <div class="card reservation-card">
          <div class="card-body">
            <h4 class="card-title">예약자 정보</h4>
            <p><i class="bi bi-person"></i> 이름: {{ reservation.userName }}</p>
            <p><i class="bi bi-envelope"></i> 이메일: {{ reservation.userEmail }}</p>
            <p><i class="bi bi-telephone"></i> 전화번호: {{ reservation.userPhone }}</p>
          </div>
        </div>
        
        <!-- 요청 사항 -->
        <div v-if="reservation.specialRequests" class="card reservation-card">
          <div class="card-body">
            <h4 class="card-title">요청 사항</h4>
            <p>{{ reservation.specialRequests }}</p>
          </div>
        </div>
        
        <!-- 리뷰 -->
        <div v-if="review" class="card review-card">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h4 class="card-title">내 리뷰</h4>
              <div>
                <i 
                  v-for="i in 5" 
                  :key="i" 
                  :class="i <= review.rating ? 'bi bi-star-fill star-rating' : 'bi bi-star star-rating'"
                ></i>
                <span class="ms-2">{{ review.rating }}/5</span>
              </div>
            </div>
            <p>{{ review.comment }}</p>
            <p class="text-muted">작성일: {{ formatDate(review.createdAt, 'date-only') }}</p>
            <div class="d-flex justify-content-end">
              <router-link :to="`/reservation/update-review-form/${review.reviewId}`" class="btn btn-outline-primary me-2">수정</router-link>
              <button @click="deleteReview" class="btn btn-outline-danger">삭제</button>
            </div>
          </div>
        </div>
        
        <div v-else-if="reservation.status === 'COMPLETED'" class="card review-card">
          <div class="card-body">
            <h4 class="card-title">리뷰 작성</h4>
            <p>숙박은 어떠셨나요? 다른 사용자들에게 도움이 될 수 있는 리뷰를 작성해주세요.</p>
            <div class="d-grid">
              <router-link :to="`/reservation/review-form/${reservation.reservationId}`" class="btn btn-primary">리뷰 작성하기</router-link>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-md-4">
        <!-- 가격 정보 -->
        <div class="card mb-4">
          <div class="card-body">
            <h4 class="card-title">가격 정보</h4>
            <p><i class="bi bi-currency-dollar"></i> 1박 요금: {{ formatCurrency(room.price) }}</p>
            <p><i class="bi bi-calendar-week"></i> 숙박 일수: {{ reservation.nights }}박</p>
            <div class="price-detail">
              <p>객실 요금: {{ formatCurrency(room.price * reservation.nights) }}</p>
              <p>세금 및 수수료: {{ formatCurrency(room.price * reservation.nights * 0.1) }}</p>
              <p class="total-price">총 요금: {{ formatCurrency(reservation.totalPrice) }}</p>
            </div>
          </div>
        </div>
        
        <!-- 예약 관리 -->
        <div class="card mb-4">
          <div class="card-body">
            <h4 class="card-title">예약 관리</h4>
            
            <!-- 결제 상태에 따른 버튼 -->
            <div v-if="reservation.paymentStatus === 'UNPAID' && reservation.status !== 'CANCELLED'" class="d-grid mb-3">
              <button @click="updatePayment" class="btn btn-success">결제하기</button>
            </div>
            
            <!-- 예약 상태에 따른 버튼 -->
            <div v-if="reservation.status === 'PENDING' || reservation.status === 'CONFIRMED'" class="d-grid mb-3">
              <button @click="cancelReservation" class="btn btn-danger">예약 취소</button>
            </div>
            
            <!-- 숙소 상세 페이지 링크 -->
            <div class="d-grid">
              <router-link :to="`/accommodation/detail/${accommodation.accommodationId}`" class="btn btn-outline-primary">숙소 상세 보기</router-link>
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

<script>
export default {
  name: 'ReservationDetail',
  data() {
    return {
      // 예약 정보
      reservation: {
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
      },
      // 숙소 정보
      accommodation: {
        accommodationId: '',
        title: '',
        address: '',
        phone: '',
        checkInTime: '',
        checkOutTime: ''
      },
      // 객실 정보
      room: {
        roomId: '',
        name: '',
        price: 0,
        mainImageUrl: ''
      },
      // 리뷰 정보
      review: null,
      // 알림 메시지
      message: ''
    };
  },
  created() {
    // 예약 ID 가져오기
    const reservationId = this.$route.params.id;
    
    // 예약 정보 로드
    if (reservationId) {
      this.loadReservationDetail(reservationId);
    } else {
      this.message = '예약 정보를 찾을 수 없습니다.';
    }
    
    // URL 쿼리 파라미터에서 메시지 가져오기
    if (this.$route.query.message) {
      this.message = this.$route.query.message;
    }
  },
  methods: {
    // 예약 상세 정보 로드
    async loadReservationDetail(reservationId) {
      try {
        // API 호출
        const response = await fetch(`/api/reservations/${reservationId}`);
        if (!response.ok) {
          throw new Error('예약 정보를 불러오는데 실패했습니다.');
        }
        
        const data = await response.json();
        this.reservation = data.reservation;
        this.accommodation = data.accommodation;
        this.room = data.room;
        this.review = data.review;
      } catch (error) {
        console.error('예약 상세 정보 로드 중 오류가 발생했습니다:', error);
        this.message = '예약 정보를 불러오는데 실패했습니다.';
      }
    },
    
    // 결제 상태 업데이트
    async updatePayment() {
      try {
        // 사용자 확인
        if (!confirm('결제를 진행하시겠습니까?')) {
          return;
        }
        
        // API 호출
        const response = await fetch(`/api/reservations/${this.reservation.reservationId}/payment`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            paymentStatus: 'PAID'
          })
        });
        
        if (!response.ok) {
          throw new Error('결제 처리에 실패했습니다.');
        }
        
        // 결제 페이지로 이동
        this.$router.push(`/payment/form/${this.reservation.reservationId}`);
      } catch (error) {
        console.error('결제 처리 중 오류가 발생했습니다:', error);
        this.message = '결제 처리에 실패했습니다.';
      }
    },
    
    // 예약 취소
    async cancelReservation() {
      try {
        // 사용자 확인
        if (!confirm('정말 취소하시겠습니까? 환불 정책에 따라 수수료가 부과될 수 있습니다.')) {
          return;
        }
        
        // API 호출
        const response = await fetch(`/api/reservations/${this.reservation.reservationId}/cancel`, {
          method: 'POST'
        });
        
        if (!response.ok) {
          throw new Error('예약 취소에 실패했습니다.');
        }
        
        // 예약 정보 다시 로드
        this.loadReservationDetail(this.reservation.reservationId);
        this.message = '예약이 취소되었습니다.';
      } catch (error) {
        console.error('예약 취소 중 오류가 발생했습니다:', error);
        this.message = '예약 취소에 실패했습니다.';
      }
    },
    
    // 리뷰 삭제
    async deleteReview() {
      try {
        // 사용자 확인
        if (!confirm('정말 삭제하시겠습니까?')) {
          return;
        }
        
        // API 호출
        const response = await fetch(`/api/reviews/${this.review.reviewId}`, {
          method: 'DELETE'
        });
        
        if (!response.ok) {
          throw new Error('리뷰 삭제에 실패했습니다.');
        }
        
        // 리뷰 정보 초기화
        this.review = null;
        this.message = '리뷰가 삭제되었습니다.';
      } catch (error) {
        console.error('리뷰 삭제 중 오류가 발생했습니다:', error);
        this.message = '리뷰 삭제에 실패했습니다.';
      }
    },
    
    // 예약 상태에 따른 배지 클래스 반환
    getStatusBadgeClass(status) {
      switch (status) {
        case 'PENDING': return 'badge bg-warning status-badge';
        case 'CONFIRMED': return 'badge bg-success status-badge';
        case 'CANCELLED': return 'badge bg-danger status-badge';
        case 'COMPLETED': return 'badge bg-info status-badge';
        default: return 'badge bg-secondary status-badge';
      }
    },
    
    // 예약 상태 텍스트 반환
    getStatusText(status) {
      switch (status) {
        case 'PENDING': return '대기중';
        case 'CONFIRMED': return '확정';
        case 'CANCELLED': return '취소됨';
        case 'COMPLETED': return '완료';
        default: return status;
      }
    },
    
    // 결제 상태에 따른 배지 클래스 반환
    getPaymentStatusBadgeClass(status) {
      switch (status) {
        case 'UNPAID': return 'badge bg-secondary status-badge';
        case 'PAID': return 'badge bg-success status-badge';
        case 'REFUNDED': return 'badge bg-warning status-badge';
        default: return 'badge bg-secondary status-badge';
      }
    },
    
    // 결제 상태 텍스트 반환
    getPaymentStatusText(status) {
      switch (status) {
        case 'UNPAID': return '미결제';
        case 'PAID': return '결제완료';
        case 'REFUNDED': return '환불됨';
        default: return status;
      }
    },
    
    // 날짜 포맷팅
    formatDate(dateString, format = 'full') {
      if (!dateString) return '-';
      
      const date = new Date(dateString);
      
      if (format === 'date-only') {
        return new Intl.DateTimeFormat('ko-KR', {
          year: 'numeric',
          month: '2-digit',
          day: '2-digit'
        }).format(date);
      }
      
      return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).format(date);
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