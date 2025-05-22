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
                    :src="room.value.mainImageUrl || noImage"
                    class="img-fluid rounded"
                    :alt="room.value.name"
                >
              </div>
              <div class="col-md-8">
                <h5>{{ room.value.name }}</h5>
                <p class="text-muted">{{ accommodation.value.title }}</p>
                <p><i class="bi bi-geo-alt"></i> {{ accommodation.value.address }}</p>
                <p><i class="bi bi-calendar-check"></i> 체크인: <strong>{{ checkInDate.value }}</strong> ({{ accommodation.value.checkInTime }})</p>
                <p><i class="bi bi-calendar-x"></i> 체크아웃: <strong>{{ checkOutDate.value }}</strong> ({{ accommodation.value.checkOutTime }})</p>
                <p><i class="bi bi-people"></i> 인원: <strong>{{ guestCount.value }}명</strong> (최대 {{ room.value.capacity }}명)</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-4">
        <div class="card">
          <div class="card-body">
            <h4 class="card-title">가격 정보</h4>
            <p><i class="bi bi-currency-dollar"></i> 1박 요금: {{ formatCurrency(room.value.price) }}</p>
            <p><i class="bi bi-calendar-week"></i> 숙박 일수: {{ nights.value }}박</p>
            <div class="price-detail">
              <p>객실 요금: {{ formatCurrency(room.value.price * nights.value) }}</p>
              <p>세금 및 수수료: {{ formatCurrency(room.value.price * nights.value * 0.1) }}</p>
              <p class="total-price">총 요금: {{ formatCurrency(totalPrice.value) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 예약자 정보 및 결제 정보 -->
    <form @submit.prevent="createReservation">
      <input type="hidden" v-model="roomId.value" />
      <input type="hidden" v-model="checkInDate.value" />
      <input type="hidden" v-model="checkOutDate.value" />
      <input type="hidden" v-model="guestCount.value" />
      <input type="hidden" v-model="totalPrice.value" />

      <div class="row">
        <div class="col-md-8">
          <!-- 예약자 정보 -->
          <div class="card mb-4">
            <div class="card-body">
              <h4 class="card-title">예약자 정보</h4>
              <div class="mb-3">
                <label for="guestName" class="form-label">이름</label>
                <input type="text" class="form-control" id="guestName" v-model="guestName.value" required>
              </div>
              <div class="mb-3">
                <label for="guestEmail" class="form-label">이메일</label>
                <input type="email" class="form-control" id="guestEmail" v-model="guestEmail.value" required>
              </div>
              <div class="mb-3">
                <label for="guestPhone" class="form-label">전화번호</label>
                <input type="tel" class="form-control" id="guestPhone" v-model="guestPhone.value" placeholder="010-0000-0000" required>
              </div>
            </div>
          </div>

          <!-- 결제 방법 -->
          <div class="card mb-4">
            <div class="card-body">
              <h4 class="card-title">결제 방법</h4>
              <div class="row">
                <div class="col-md-4 mb-3" v-for="(item, idx) in paymentTypes" :key="idx">
                  <div
                      class="card payment-method-card"
                      :class="{ selected: paymentMethod.value === item.value }"
                      @click="selectPaymentMethod(item.value)"
                      style="cursor:pointer;"
                  >
                    <div class="card-body text-center">
                      <i :class="item.icon + ' fs-1'"></i>
                      <h5 class="mt-2">{{ item.label }}</h5>
                    </div>
                  </div>
                </div>
              </div>
              <!-- 결제 방법별 추가 입력 -->
              <div v-if="paymentMethod.value" class="mt-3">
                <!-- 신용카드 -->
                <div v-if="paymentMethod.value === 'CARD'">
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <label for="cardNumber" class="form-label">카드 번호</label>
                      <input type="text" class="form-control" id="cardNumber" v-model="cardInfo.cardNumber" placeholder="0000-0000-0000-0000" required>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label for="cardName" class="form-label">카드 소유자 이름</label>
                      <input type="text" class="form-control" id="cardName" v-model="cardInfo.cardName" required>
                    </div>
                  </div>
                  <div class="row">
                    <div class="col-md-6 mb-3">
                      <label for="expiryDate" class="form-label">유효기간</label>
                      <input type="text" class="form-control" id="expiryDate" v-model="cardInfo.expiryDate" placeholder="MM/YY" required>
                    </div>
                    <div class="col-md-6 mb-3">
                      <label for="cvv" class="form-label">CVV</label>
                      <input type="text" class="form-control" id="cvv" v-model="cardInfo.cvv" placeholder="123" required>
                    </div>
                  </div>
                </div>
                <!-- 계좌이체 -->
                <div v-if="paymentMethod.value === 'BANK_TRANSFER'">
                  <div class="mb-3">
                    <label for="bankName" class="form-label">은행명</label>
                    <select class="form-select" id="bankName" v-model="bankInfo.bankName" required>
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
                    <input type="text" class="form-control" id="accountNumber" v-model="bankInfo.accountNumber" required>
                  </div>
                  <div class="mb-3">
                    <label for="accountHolder" class="form-label">예금주</label>
                    <input type="text" class="form-control" id="accountHolder" v-model="bankInfo.accountHolder" required>
                  </div>
                </div>
                <!-- 휴대폰 결제 -->
                <div v-if="paymentMethod.value === 'PHONE'">
                  <div class="mb-3">
                    <label for="phoneNumber" class="form-label">휴대폰 번호</label>
                    <input type="tel" class="form-control" id="phoneNumber" v-model="phonePayInfo.phoneNumber" placeholder="010-0000-0000" required>
                  </div>
                  <div class="mb-3">
                    <label for="carrier" class="form-label">통신사</label>
                    <select class="form-select" id="carrier" v-model="phonePayInfo.carrier" required>
                      <option value="">통신사를 선택하세요</option>
                      <option value="SKT">SKT</option>
                      <option value="KT">KT</option>
                      <option value="LGU+">LGU+</option>
                      <option value="알뜰폰">알뜰폰</option>
                    </select>
                  </div>
                  <div class="mb-3">
                    <label for="birthDate" class="form-label">생년월일</label>
                    <input type="text" class="form-control" id="birthDate" v-model="phonePayInfo.birthDate" placeholder="YYMMDD" required>
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
                    v-model="specialRequests.value"
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
              <p><i class="bi bi-building"></i> {{ accommodation.value.title }}</p>
              <p><i class="bi bi-door-closed"></i> {{ room.value.name }}</p>
              <p><i class="bi bi-calendar-check"></i> 체크인: {{ checkInDate.value }}</p>
              <p><i class="bi bi-calendar-x"></i> 체크아웃: {{ checkOutDate.value }}</p>
              <p><i class="bi bi-people"></i> 인원: {{ guestCount.value }}명</p>
              <div class="price-detail">
                <p class="total-price">총 요금: {{ formatCurrency(totalPrice.value) }}</p>
              </div>
              <div class="form-check mb-3">
                <input class="form-check-input" type="checkbox" id="agreeTerms" v-model="termsAgreed.value" required>
                <label class="form-check-label" for="agreeTerms">
                  <a href="#" data-bs-toggle="modal" data-bs-target="#termsModal">이용약관</a>에 동의합니다.
                </label>
              </div>
              <div class="d-grid">
                <button
                    type="submit"
                    class="btn btn-primary btn-lg"
                    :disabled="!paymentMethod.value || !termsAgreed.value"
                >결제하기</button>
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
            <p>2. 체크인 시간은 {{ accommodation.value.checkInTime }}, 체크아웃 시간은 {{ accommodation.value.checkOutTime }}입니다.</p>
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

<script setup>
// 한국어 주석
import { ref, reactive, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// 이미지 없을 때 대체 이미지
const noImage = require('@/assets/no-image.jpg');

// 라우터/라우트 객체
const route = useRoute();
const router = useRouter();

// 예약 관련 변수들 (ref/반응형)
const roomId = ref('');
const checkInDate = ref('');
const checkOutDate = ref('');
const guestCount = ref(1);
const nights = ref(0);
const totalPrice = ref(0);

// 숙소 정보
const accommodation = ref({
  title: '',
  address: '',
  checkInTime: '',
  checkOutTime: ''
});

// 객실 정보
const room = ref({
  name: '',
  price: 0,
  capacity: 0,
  mainImageUrl: ''
});

// 예약자 정보
const guestName = ref('');
const guestEmail = ref('');
const guestPhone = ref('');

// 결제 방법
const paymentMethod = ref('');

// 결제별 입력 정보
const cardInfo = reactive({ cardNumber: '', cardName: '', expiryDate: '', cvv: '' });
const bankInfo = reactive({ bankName: '', accountNumber: '', accountHolder: '' });
const phonePayInfo = reactive({ phoneNumber: '', carrier: '', birthDate: '' });

// 요청 사항
const specialRequests = ref('');

// 약관 동의
const termsAgreed = ref(false);

// 결제 방법 목록
const paymentTypes = [
  { value: 'CARD', icon: 'bi bi-credit-card', label: '신용카드' },
  { value: 'BANK_TRANSFER', icon: 'bi bi-bank', label: '계좌이체' },
  { value: 'PHONE', icon: 'bi bi-phone', label: '휴대폰 결제' }
];

// 페이지 첫 진입 시 데이터 세팅
onMounted(() => {
  // 쿼리로 받은 값
  roomId.value = route.query.roomId || '';
  checkInDate.value = route.query.checkInDate || '';
  checkOutDate.value = route.query.checkOutDate || '';
  guestCount.value = parseInt(route.query.guestCount || '1');

  // 사용자 정보, 객실/숙소 정보 로드
  loadUserInfo();
  loadRoomInfo();
});

// 사용자 정보 로드
async function loadUserInfo() {
  try {
    const response = await fetch('/api/users/me');
    if (!response.ok) throw new Error('사용자 정보를 불러오는데 실패했습니다.');
    const user = await response.json();
    guestName.value = user.name;
    guestEmail.value = user.email;
    guestPhone.value = user.phone || '';
  } catch (e) {
    console.error('사용자 정보 로드 오류:', e);
  }
}

// 객실 및 숙소 정보 로드
async function loadRoomInfo() {
  try {
    if (!roomId.value) throw new Error('객실 ID가 없습니다.');
    const response = await fetch(`/api/rooms/${roomId.value}`);
    if (!response.ok) throw new Error('객실 정보를 불러오는데 실패했습니다.');
    const data = await response.json();
    room.value = data.room;
    accommodation.value = data.accommodation;
    calculateNights();
    calculateTotalPrice();
  } catch (e) {
    console.error('객실 정보 로드 오류:', e);
  }
}

// 숙박 일수 계산
function calculateNights() {
  if (checkInDate.value && checkOutDate.value) {
    const checkIn = new Date(checkInDate.value);
    const checkOut = new Date(checkOutDate.value);
    const diffTime = Math.abs(checkOut - checkIn);
    nights.value = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  }
}

// 총 가격 계산
function calculateTotalPrice() {
  totalPrice.value = room.value.price * nights.value * 1.1; // 세금 10%
}

// 결제 방법 선택
function selectPaymentMethod(method) {
  paymentMethod.value = method;
}

// 예약 생성
async function createReservation() {
  try {
    if (!paymentMethod.value) {
      alert('결제 방법을 선택해주세요.');
      return;
    }
    if (!termsAgreed.value) {
      alert('이용약관에 동의해주세요.');
      return;
    }
    // 실제 결제 정보 유효성 체크는 추가적으로 필요

    const response = await fetch('/api/reservations/create', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        roomId: roomId.value,
        checkInDate: checkInDate.value,
        checkOutDate: checkOutDate.value,
        guestCount: guestCount.value,
        guestName: guestName.value,
        guestEmail: guestEmail.value,
        guestPhone: guestPhone.value,
        paymentMethod: paymentMethod.value,
        specialRequests: specialRequests.value,
        totalPrice: totalPrice.value
      })
    });
    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || '예약 생성에 실패했습니다.');
    }
    const data = await response.json();
    // 결제 결과 페이지로 이동
    router.push(`/payment/result/${data.paymentId}?message=예약이 완료되었습니다.`);
  } catch (e) {
    console.error('예약 생성 오류:', e);
    alert(e.message || '예약 생성에 실패했습니다.');
  }
}

// 금액 포맷팅
function formatCurrency(amount) {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
    maximumFractionDigits: 0
  }).format(amount);
}
</script>

<style scoped>
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
