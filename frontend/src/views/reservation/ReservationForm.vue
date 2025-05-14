<template>
  <div class="container mt-5 mb-5">
    <h2 class="mb-4">예약하기</h2>
    
    <!-- 예약 정보 요약 -->
    <div class="row mb-4">
      <div class="col-md-8">
        <div class="card">
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
                <p><i class="bi bi-calendar-check"></i> 체크인: <strong>{{ checkInDate }}</strong> ({{ accommodation.checkInTime }})</p>
                <p><i class="bi bi-calendar-x"></i> 체크아웃: <strong>{{ checkOutDate }}</strong> ({{ accommodation.checkOutTime }})</p>
                <p><i class="bi bi-people"></i> 인원: <strong>{{ guestCount }}명</strong> (최대 {{ room.capacity }}명)</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="col-md-4">
        <div class="card">
          <div class="card-body">
            <h4 class="card-title">가격 정보</h4>
            <p><i class="bi bi-currency-dollar"></i> 1박 요금: {{ formatCurrency(room.price) }}</p>
            <p><i class="bi bi-calendar-week"></i> 숙박 일수: {{ nights }}박</p>
            <div class="price-detail">
              <p>객실 요금: {{ formatCurrency(room.price * nights) }}</p>
              <p>세금 및 수수료: {{ formatCurrency(room.price * nights * 0.1) }}</p>
              <p class="total-price">총 요금: {{ formatCurrency(totalPrice) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 예약자 정보 및 결제 정보 -->
    <form @submit.prevent="createReservation">
      <input type="hidden" name="roomId" v-model="roomId">
      <input type="hidden" name="checkInDate" v-model="checkInDate">
      <input type="hidden" name="checkOutDate" v-model="checkOutDate">
      <input type="hidden" name="guestCount" v-model="guestCount">
      <input type="hidden" name="totalPrice" v-model="totalPrice">
      
      <div class="row">
        <div class="col-md-8">
          <!-- 예약자 정보 -->
          <div class="card mb-4">
            <div class="card-body">
              <h4 class="card-title">예약자 정보</h4>
              <div class="mb-3">
                <label for="guestName" class="form-label">이름</label>
                <input type="text" class="form-control" id="guestName" v-model="guestName" required>
              </div>
              <div class="mb-3">
                <label for="guestEmail" class="form-label">이메일</label>
                <input type="email" class="form-control" id="guestEmail" v-model="guestEmail" required>
              </div>
              <div class="mb-3">
                <label for="guestPhone" class="form-label">전화번호</label>
                <input type="tel" class="form-control" id="guestPhone" v-model="guestPhone" placeholder="010-0000-0000" required>
              </div>
            </div>
          </div>
          
          <!-- 결제 방법 -->
          <div class="card mb-4">
            <div class="card-body">
              <h4 class="card-title">결제 방법</h4>
              <div class="row">
                <div class="col-md-4 mb-3">
                  <div 
                    class="card payment-method-card" 
                    :class="{ 'selected': paymentMethod === 'CARD' }"
                    @click="selectPaymentMethod('CARD')"
                  >
                    <div class="card-body text-center">
                      <i class="bi bi-credit-card fs-1"></i>
                      <h5 class="mt-2">신용카드</h5>
                    </div>
                  </div>
                </div>
                <div class="col-md-4 mb-3">
                  <div 
                    class="card payment-method-card" 
                    :class="{ 'selected': paymentMethod === 'BANK_TRANSFER' }"
                    @click="selectPaymentMethod('BANK_TRANSFER')"
                  >
                    <div class="card-body text-center">
                      <i class="bi bi-bank fs-1"></i>
                      <h5 class="mt-2">계좌이체</h5>
                    </div>
                  </div>
                </div>
                <div class="col-md-4 mb-3">
                  <div 
                    class="card payment-method-card" 
                    :class="{ 'selected': paymentMethod === 'PHONE' }"
                    @click="selectPaymentMethod('PHONE')"
                  >
                    <div class="card-body text-center">
                      <i class="bi bi-phone fs-1"></i>
                      <h5 class="mt-2">휴대폰 결제</h5>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 결제 방법에 따른 추가 입력 필드 -->
              <div v-if="paymentMethod" class="mt-3">
                <!-- 신용카드 -->
                <div v-if="paymentMethod === 'CARD'">
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <label for="cardNumber" class="form-label">카드 번호</label>
                      <input type="text" class="form-control" id="cardNumber" placeholder="0000-0000-0000-0000" required>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label for="cardName" class="form-label">카드 소유자 이름</label>
                      <input type="text" class="form-control" id="cardName" required>
                    </div>
                  </div>
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <label for="expiryDate" class="form-label">유효기간</label>
                      <input type="text" class="form-control" id="expiryDate" placeholder="MM/YY" required>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label for="cvv" class="form-label">CVV</label>
                      <input type="text" class="form-control" id="cvv" placeholder="123" required>
                    </div>
                  </div>
                </div>
                
                <!-- 계좌이체 -->
                <div v-if="paymentMethod === 'BANK_TRANSFER'">
                  <div class="mb-3">
                    <label for="bankName" class="form-label">은행명</label>
                    <select class="form-select" id="bankName" required>
                      <option value="">은행을 선택하세요</option>
                      <option value="KB">KB국민은행</option>
                      <option value="Shinhan">신한은행</option>
                      <option value="Woori">우리은행</option>
                      <option value="Hana">하나은행</option>
                      <option value="IBK">기업은행</option>
                    </select>
                  </div>
                  <div class="mb-3">
                    <label for="accountNumber" class="form-label">계좌번호</label>
                    <input type="text" class="form-control" id="accountNumber" required>
                  </div>
                  <div class="mb-3">
                    <label for="accountHolder" class="form-label">예금주</label>
                    <input type="text" class="form-control" id="accountHolder" required>
                  </div>
                </div>
                
                <!-- 휴대폰 결제 -->
                <div v-if="paymentMethod === 'PHONE'">
                  <div class="mb-3">
                    <label for="phoneNumber" class="form-label">휴대폰 번호</label>
                    <input type="tel" class="form-control" id="phoneNumber" placeholder="010-0000-0000" required>
                  </div>
                  <div class="mb-3">
                    <label for="carrier" class="form-label">통신사</label>
                    <select class="form-select" id="carrier" required>
                      <option value="">통신사를 선택하세요</option>
                      <option value="SKT">SKT</option>
                      <option value="KT">KT</option>
                      <option value="LGU+">LGU+</option>
                      <option value="알뜰폰">알뜰폰</option>
                    </select>
                  </div>
                  <div class="mb-3">
                    <label for="birthDate" class="form-label">생년월일</label>
                    <input type="text" class="form-control" id="birthDate" placeholder="YYMMDD" required>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 요청 사항 -->
          <div class="card mb-4">
            <div class="card-body">
              <h4 class="card-title">요청 사항</h4>
              <div class="mb-3">
                <label for="specialRequests" class="form-label">호스트에게 전달할 메시지</label>
                <textarea 
                  class="form-control" 
                  id="specialRequests" 
                  v-model="specialRequests" 
                  rows="3" 
                  placeholder="특별한 요청 사항이 있으면 입력해주세요."
                ></textarea>
              </div>
            </div>
          </div>
        </div>
        
        <div class="col-md-4">
          <!-- 예약 요약 및 결제 버튼 -->
          <div class="card sticky-top" style="top: 20px;">
            <div class="card-body">
              <h4 class="card-title">예약 요약</h4>
              <p><i class="bi bi-building"></i> {{ accommodation.title }}</p>
              <p><i class="bi bi-door-closed"></i> {{ room.name }}</p>
              <p><i class="bi bi-calendar-check"></i> 체크인: {{ checkInDate }}</p>
              <p><i class="bi bi-calendar-x"></i> 체크아웃: {{ checkOutDate }}</p>
              <p><i class="bi bi-people"></i> 인원: {{ guestCount }}명</p>
              <div class="price-detail">
                <p class="total-price">총 요금: {{ formatCurrency(totalPrice) }}</p>
              </div>
              <div class="form-check mb-3">
                <input class="form-check-input" type="checkbox" id="agreeTerms" v-model="termsAgreed" required>
                <label class="form-check-label" for="agreeTerms">
                  <a href="#" data-bs-toggle="modal" data-bs-target="#termsModal">이용약관</a>에 동의합니다.
                </label>
              </div>
              <div class="d-grid">
                <button 
                  type="submit" 
                  class="btn btn-primary btn-lg" 
                  :disabled="!paymentMethod || !termsAgreed"
                >
                  결제하기
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </form>
    
    <!-- 이용약관 모달 -->
    <div class="modal fade" id="termsModal" tabindex="-1" aria-labelledby="termsModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="termsModalLabel">이용약관</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <h5>예약 및 결제 약관</h5>
            <p>1. 예약 확정 후 취소 시 환불 규정에 따라 수수료가 부과될 수 있습니다.</p>
            <p>2. 체크인 시간은 {{ accommodation.checkInTime }}, 체크아웃 시간은 {{ accommodation.checkOutTime }}입니다.</p>
            <p>3. 최대 인원을 초과하는 경우 추가 요금이 발생하거나 입실이 거부될 수 있습니다.</p>
            <p>4. 객실 내 흡연은 금지되어 있으며, 위반 시 추가 청소비가 청구될 수 있습니다.</p>
            <p>5. 예약자와 실제 투숙객의 정보가 일치해야 합니다.</p>
            
            <h5 class="mt-4">환불 정책</h5>
            <p>- 체크인 7일 전 취소: 100% 환불</p>
            <p>- 체크인 5일 전 취소: 70% 환불</p>
            <p>- 체크인 3일 전 취소: 50% 환불</p>
            <p>- 체크인 1일 전 취소: 환불 불가</p>
            <p>- 노쇼(No-show): 환불 불가</p>
            
            <h5 class="mt-4">개인정보 수집 및 이용</h5>
            <p>1. 수집항목: 이름, 이메일, 전화번호</p>
            <p>2. 수집목적: 예약 확인 및 서비스 제공</p>
            <p>3. 보유기간: 예약 완료 후 3년</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReservationForm',
  data() {
    return {
      // 예약 정보
      roomId: '',
      checkInDate: '',
      checkOutDate: '',
      guestCount: 0,
      nights: 0,
      totalPrice: 0,
      
      // 숙소 정보
      accommodation: {
        title: '',
        address: '',
        checkInTime: '',
        checkOutTime: ''
      },
      
      // 객실 정보
      room: {
        name: '',
        price: 0,
        capacity: 0,
        mainImageUrl: ''
      },
      
      // 예약자 정보
      guestName: '',
      guestEmail: '',
      guestPhone: '',
      
      // 결제 방법
      paymentMethod: '',
      
      // 요청 사항
      specialRequests: '',
      
      // 이용약관 동의 여부
      termsAgreed: false
    };
  },
  created() {
    // URL 쿼리 파라미터에서 예약 정보 가져오기
    const query = this.$route.query;
    this.roomId = query.roomId;
    this.checkInDate = query.checkInDate;
    this.checkOutDate = query.checkOutDate;
    this.guestCount = parseInt(query.guestCount || '1');
    
    // 사용자 정보 가져오기
    this.loadUserInfo();
    
    // 객실 및 숙소 정보 로드
    this.loadRoomInfo();
  },
  methods: {
    // 사용자 정보 로드
    async loadUserInfo() {
      try {
        // API 호출
        const response = await fetch('/api/users/me');
        if (!response.ok) {
          throw new Error('사용자 정보를 불러오는데 실패했습니다.');
        }
        
        const user = await response.json();
        this.guestName = user.name;
        this.guestEmail = user.email;
        this.guestPhone = user.phone || '';
      } catch (error) {
        console.error('사용자 정보 로드 중 오류가 발생했습니다:', error);
      }
    },
    
    // 객실 및 숙소 정보 로드
    async loadRoomInfo() {
      try {
        if (!this.roomId) {
          throw new Error('객실 ID가 없습니다.');
        }
        
        // API 호출
        const response = await fetch(`/api/rooms/${this.roomId}`);
        if (!response.ok) {
          throw new Error('객실 정보를 불러오는데 실패했습니다.');
        }
        
        const data = await response.json();
        this.room = data.room;
        this.accommodation = data.accommodation;
        
        // 숙박 일수 계산
        this.calculateNights();
        
        // 총 가격 계산
        this.calculateTotalPrice();
      } catch (error) {
        console.error('객실 정보 로드 중 오류가 발생했습니다:', error);
      }
    },
    
    // 숙박 일수 계산
    calculateNights() {
      if (this.checkInDate && this.checkOutDate) {
        const checkIn = new Date(this.checkInDate);
        const checkOut = new Date(this.checkOutDate);
        const diffTime = Math.abs(checkOut - checkIn);
        this.nights = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      }
    },
    
    // 총 가격 계산
    calculateTotalPrice() {
      // 객실 요금 + 세금 및 수수료(10%)
      this.totalPrice = this.room.price * this.nights * 1.1;
    },
    
    // 결제 방법 선택
    selectPaymentMethod(method) {
      this.paymentMethod = method;
    },
    
    // 예약 생성
    async createReservation() {
      try {
        if (!this.paymentMethod) {
          alert('결제 방법을 선택해주세요.');
          return;
        }
        
        if (!this.termsAgreed) {
          alert('이용약관에 동의해주세요.');
          return;
        }
        
        // API 호출
        const response = await fetch('/api/reservations/create', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            roomId: this.roomId,
            checkInDate: this.checkInDate,
            checkOutDate: this.checkOutDate,
            guestCount: this.guestCount,
            guestName: this.guestName,
            guestEmail: this.guestEmail,
            guestPhone: this.guestPhone,
            paymentMethod: this.paymentMethod,
            specialRequests: this.specialRequests,
            totalPrice: this.totalPrice
          })
        });
        
        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.message || '예약 생성에 실패했습니다.');
        }
        
        const data = await response.json();
        
        // 결제 결과 페이지로 이동
        this.$router.push(`/payment/result/${data.paymentId}?message=예약이 완료되었습니다.`);
      } catch (error) {
        console.error('예약 생성 중 오류가 발생했습니다:', error);
        alert(error.message || '예약 생성에 실패했습니다.');
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
.reservation-summary {
  background-color: #f8f9fa;
  border-radius: 5px;
  padding: 20px;
  margin-bottom: 20px;
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
.payment-method-card {
  cursor: pointer;
  transition: all 0.3s;
}
.payment-method-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
.payment-method-card.selected {
  border-color: #0d6efd;
  background-color: #f0f7ff;
}
</style>