<template>
  <Layout>
    <div class="container mt-5 mb-5">
      <h2 class="mb-4">결제하기</h2>

      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>

      <!-- 에러 메시지 -->
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>

      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">예약 정보를 불러오는 중입니다...</p>
      </div>

      <div v-else>
        <!-- 예약 정보 요약 -->
        <div class="payment-summary">
          <div class="row">
            <div class="col-md-6">
              <h4>예약 정보</h4>
              <p><strong>예약 번호:</strong> {{ reservation.reservationId }}</p>
              <p><strong>숙소:</strong> {{ reservation.accommodationTitle }}</p>
              <p><strong>객실:</strong> {{ reservation.roomName }}</p>
              <p><strong>체크인:</strong> {{ formatDate(reservation.checkInDate) }}</p>
              <p><strong>체크아웃:</strong> {{ formatDate(reservation.checkOutDate) }}</p>
              <p><strong>인원:</strong> {{ reservation.guestCount }}명</p>
            </div>
            <div class="col-md-6">
              <h4>결제 금액</h4>
              <p><strong>객실 요금:</strong> {{ formatCurrency(reservation.totalPrice) }}</p>
              <p><strong>세금 및 수수료:</strong> {{ formatCurrency(reservation.totalPrice * 0.1) }}</p>
              <p class="total-price"><strong>총 결제 금액:</strong> {{ formatCurrency(reservation.totalPrice * 1.1) }}</p>
            </div>
          </div>
        </div>

        <!-- 결제 폼 -->
        <form @submit.prevent="processPayment">
          <input type="hidden" name="reservationId" :value="reservation.reservationId">
          <input type="hidden" name="amount" :value="reservation.totalPrice * 1.1">

          <div class="card mb-4">
            <div class="card-body">
              <h4 class="card-title">결제 방법 선택</h4>
              <div class="row">
                <div class="col-md-4">
                  <div 
                    class="payment-method-card" 
                    :class="{ 'selected': paymentMethod === 'CARD' }"
                    @click="selectPaymentMethod('CARD')"
                  >
                    <div class="payment-icon"><i class="bi bi-credit-card"></i></div>
                    <h5>신용카드</h5>
                    <p class="text-muted">모든 신용/체크카드 사용 가능</p>
                  </div>
                </div>
                <div class="col-md-4">
                  <div 
                    class="payment-method-card" 
                    :class="{ 'selected': paymentMethod === 'BANK_TRANSFER' }"
                    @click="selectPaymentMethod('BANK_TRANSFER')"
                  >
                    <div class="payment-icon"><i class="bi bi-bank"></i></div>
                    <h5>계좌이체</h5>
                    <p class="text-muted">실시간 계좌이체로 결제</p>
                  </div>
                </div>
                <div class="col-md-4">
                  <div 
                    class="payment-method-card" 
                    :class="{ 'selected': paymentMethod === 'PHONE' }"
                    @click="selectPaymentMethod('PHONE')"
                  >
                    <div class="payment-icon"><i class="bi bi-phone"></i></div>
                    <h5>휴대폰 결제</h5>
                    <p class="text-muted">통신요금과 함께 청구</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 결제 방법에 따른 추가 입력 필드 -->
          <div v-if="paymentMethod" class="card mb-4">
            <div class="card-body">
              <h4 class="card-title">결제 정보 입력</h4>

              <!-- 카드 결제 폼 -->
              <div v-if="paymentMethod === 'CARD'">
                <div class="row mb-3">
                  <div class="col-md-6">
                    <label for="cardNumber" class="form-label">카드 번호</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      id="cardNumber" 
                      v-model="paymentInfo.cardInfo" 
                      placeholder="0000-0000-0000-0000" 
                      required
                    >
                  </div>
                  <div class="col-md-6">
                    <label for="cardExpiry" class="form-label">유효기간</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      id="cardExpiry" 
                      v-model="paymentInfo.cardExpiry" 
                      placeholder="MM/YY" 
                      required
                    >
                  </div>
                </div>
                <div class="row mb-3">
                  <div class="col-md-6">
                    <label for="cardHolder" class="form-label">카드 소유자</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      id="cardHolder" 
                      v-model="paymentInfo.cardHolder" 
                      placeholder="카드에 표시된 이름" 
                      required
                    >
                  </div>
                  <div class="col-md-6">
                    <label for="cardCvv" class="form-label">보안코드 (CVV)</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      id="cardCvv" 
                      v-model="paymentInfo.cardCvv" 
                      placeholder="카드 뒷면 3자리" 
                      required
                    >
                  </div>
                </div>
              </div>

              <!-- 계좌이체 폼 -->
              <div v-if="paymentMethod === 'BANK_TRANSFER'">
                <div class="row mb-3">
                  <div class="col-md-6">
                    <label for="bankName" class="form-label">은행 선택</label>
                    <select 
                      class="form-select" 
                      id="bankName" 
                      v-model="paymentInfo.bankName" 
                      required
                    >
                      <option value="">은행을 선택하세요</option>
                      <option value="KB국민은행">KB국민은행</option>
                      <option value="신한은행">신한은행</option>
                      <option value="우리은행">우리은행</option>
                      <option value="하나은행">하나은행</option>
                      <option value="NH농협은행">NH농협은행</option>
                      <option value="IBK기업은행">IBK기업은행</option>
                    </select>
                  </div>
                  <div class="col-md-6">
                    <label for="accountNumber" class="form-label">계좌번호</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      id="accountNumber" 
                      v-model="paymentInfo.bankInfo" 
                      placeholder="'-' 없이 입력" 
                      required
                    >
                  </div>
                </div>
                <div class="mb-3">
                  <label for="accountHolder" class="form-label">예금주</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    id="accountHolder" 
                    v-model="paymentInfo.accountHolder" 
                    placeholder="예금주 이름" 
                    required
                  >
                </div>
              </div>

              <!-- 휴대폰 결제 폼 -->
              <div v-if="paymentMethod === 'PHONE'">
                <div class="row mb-3">
                  <div class="col-md-6">
                    <label for="phoneNumber" class="form-label">휴대폰 번호</label>
                    <input 
                      type="text" 
                      class="form-control" 
                      id="phoneNumber" 
                      v-model="paymentInfo.phoneInfo" 
                      placeholder="010-0000-0000" 
                      required
                    >
                  </div>
                  <div class="col-md-6">
                    <label for="carrier" class="form-label">통신사</label>
                    <select 
                      class="form-select" 
                      id="carrier" 
                      v-model="paymentInfo.carrier" 
                      required
                    >
                      <option value="">통신사를 선택하세요</option>
                      <option value="SKT">SKT</option>
                      <option value="KT">KT</option>
                      <option value="LG U+">LG U+</option>
                      <option value="알뜰폰">알뜰폰</option>
                    </select>
                  </div>
                </div>
                <div class="mb-3">
                  <label for="birthDate" class="form-label">생년월일</label>
                  <input 
                    type="text" 
                    class="form-control" 
                    id="birthDate" 
                    v-model="paymentInfo.birthDate" 
                    placeholder="YYMMDD" 
                    required
                  >
                </div>
              </div>
            </div>
          </div>

          <!-- 약관 동의 -->
          <div class="card mb-4">
            <div class="card-body">
              <h4 class="card-title">약관 동의</h4>
              <div class="form-check mb-2">
                <input 
                  class="form-check-input" 
                  type="checkbox" 
                  id="agreeAll" 
                  v-model="agreements.all" 
                  @change="toggleAllAgreements"
                >
                <label class="form-check-label fw-bold" for="agreeAll">
                  전체 동의
                </label>
              </div>
              <hr>
              <div class="form-check mb-2">
                <input 
                  class="form-check-input" 
                  type="checkbox" 
                  id="agreeTerms" 
                  v-model="agreements.terms" 
                  @change="checkAllAgreements" 
                  required
                >
                <label class="form-check-label" for="agreeTerms">
                  <span class="text-danger">[필수]</span> 결제 서비스 이용약관 동의
                </label>
                <button 
                  type="button" 
                  class="btn btn-sm btn-link" 
                  data-bs-toggle="modal" 
                  data-bs-target="#termsModal"
                >
                  보기
                </button>
              </div>
              <div class="form-check mb-2">
                <input 
                  class="form-check-input" 
                  type="checkbox" 
                  id="agreePrivacy" 
                  v-model="agreements.privacy" 
                  @change="checkAllAgreements" 
                  required
                >
                <label class="form-check-label" for="agreePrivacy">
                  <span class="text-danger">[필수]</span> 개인정보 수집 및 이용 동의
                </label>
                <button 
                  type="button" 
                  class="btn btn-sm btn-link" 
                  data-bs-toggle="modal" 
                  data-bs-target="#privacyModal"
                >
                  보기
                </button>
              </div>
              <div class="form-check mb-2">
                <input 
                  class="form-check-input" 
                  type="checkbox" 
                  id="agreeRefund" 
                  v-model="agreements.refund" 
                  @change="checkAllAgreements" 
                  required
                >
                <label class="form-check-label" for="agreeRefund">
                  <span class="text-danger">[필수]</span> 환불 규정 동의
                </label>
                <button 
                  type="button" 
                  class="btn btn-sm btn-link" 
                  data-bs-toggle="modal" 
                  data-bs-target="#refundModal"
                >
                  보기
                </button>
              </div>
            </div>
          </div>

          <!-- 결제 버튼 -->
          <div class="d-grid gap-2">
            <button 
              type="submit" 
              class="btn btn-primary btn-lg" 
              :disabled="!isPaymentFormValid"
            >
              {{ formatCurrency(reservation.totalPrice * 1.1) }} 결제하기
            </button>
            <router-link 
              :to="`/reservation/detail/${reservation.reservationId}`" 
              class="btn btn-outline-secondary"
            >
              취소
            </router-link>
          </div>
        </form>
      </div>
    </div>

    <!-- 약관 모달 -->
    <div class="modal fade" id="termsModal" tabindex="-1" aria-labelledby="termsModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="termsModalLabel">결제 서비스 이용약관</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <h5>제1조 (목적)</h5>
            <p>이 약관은 회사가 제공하는 결제 서비스의 이용과 관련하여 회사와 이용자 간의 권리, 의무 및 책임사항을 규정함을 목적으로 합니다.</p>

            <h5>제2조 (정의)</h5>
            <p>1. "서비스"라 함은 회사가 제공하는 결제 서비스를 의미합니다.</p>
            <p>2. "이용자"라 함은 이 약관에 따라 회사가 제공하는 서비스를 이용하는 자를 말합니다.</p>

            <h5>제3조 (약관의 효력 및 변경)</h5>
            <p>1. 이 약관은 서비스를 이용하고자 하는 모든 이용자에게 적용됩니다.</p>
            <p>2. 회사는 필요한 경우 약관을 변경할 수 있으며, 변경된 약관은 서비스 내에 공지함으로써 효력이 발생합니다.</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="privacyModal" tabindex="-1" aria-labelledby="privacyModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="privacyModalLabel">개인정보 수집 및 이용 동의</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <h5>1. 수집하는 개인정보 항목</h5>
            <p>- 결제 정보: 카드번호, 유효기간, 카드 소유자 이름, 계좌번호, 예금주, 휴대폰 번호, 통신사, 생년월일</p>

            <h5>2. 수집 및 이용 목적</h5>
            <p>- 결제 서비스 제공 및 결제 처리</p>
            <p>- 결제 관련 민원 처리 및 분쟁 해결</p>

            <h5>3. 보유 및 이용 기간</h5>
            <p>- 관련 법령에 따라 5년간 보관 후 파기</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
          </div>
        </div>
      </div>
    </div>

    <div class="modal fade" id="refundModal" tabindex="-1" aria-labelledby="refundModalLabel" aria-hidden="true">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="refundModalLabel">환불 규정</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <h5>환불 정책</h5>
            <p>- 체크인 7일 전 취소: 100% 환불</p>
            <p>- 체크인 5일 전 취소: 70% 환불</p>
            <p>- 체크인 3일 전 취소: 50% 환불</p>
            <p>- 체크인 1일 전 취소: 환불 불가</p>
            <p>- 노쇼(No-show): 환불 불가</p>

            <h5>환불 처리 기간</h5>
            <p>- 카드 결제: 취소 후 3~5일 이내 환불</p>
            <p>- 계좌이체: 취소 후 3~7일 이내 환불</p>
            <p>- 휴대폰 결제: 취소 후 최대 2개월 이내 환불 (통신사 정책에 따름)</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 결제 폼 컴포넌트
 * 
 * 이 컴포넌트는 예약에 대한 결제를 처리하는 폼을 제공합니다.
 * 결제 방법 선택, 결제 정보 입력, 약관 동의 등의 기능을 포함합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'PaymentForm',
  components: {
    Layout
  },
  props: {
    // URL 파라미터로부터 예약 ID를 받음
    reservationId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      loading: true,
      message: '',
      error: '',
      reservation: {
        reservationId: null,
        accommodationTitle: '',
        roomName: '',
        checkInDate: null,
        checkOutDate: null,
        guestCount: 0,
        totalPrice: 0
      },
      paymentMethod: '',
      paymentInfo: {
        // 카드 결제 정보
        cardInfo: '',
        cardExpiry: '',
        cardHolder: '',
        cardCvv: '',

        // 계좌이체 정보
        bankName: '',
        bankInfo: '',
        accountHolder: '',

        // 휴대폰 결제 정보
        phoneInfo: '',
        carrier: '',
        birthDate: ''
      },
      agreements: {
        all: false,
        terms: false,
        privacy: false,
        refund: false
      },
      modals: {
        terms: null,
        privacy: null,
        refund: null
      }
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.user.isLoggedIn
    }),

    /**
     * 결제 폼 유효성 검사
     * @returns {boolean} 폼이 유효한지 여부
     */
    isPaymentFormValid() {
      // 결제 방법 선택 확인
      if (!this.paymentMethod) return false;

      // 약관 동의 확인
      if (!this.agreements.terms || !this.agreements.privacy || !this.agreements.refund) return false;

      // 결제 방법에 따른 정보 입력 확인
      switch (this.paymentMethod) {
        case 'CARD':
          return this.paymentInfo.cardInfo && this.paymentInfo.cardExpiry && 
                 this.paymentInfo.cardHolder && this.paymentInfo.cardCvv;

        case 'BANK_TRANSFER':
          return this.paymentInfo.bankName && this.paymentInfo.bankInfo && 
                 this.paymentInfo.accountHolder;

        case 'PHONE':
          return this.paymentInfo.phoneInfo && this.paymentInfo.carrier && 
                 this.paymentInfo.birthDate;

        default:
          return false;
      }
    },

    /**
     * 예약 ID (숫자 타입)
     */
    numericReservationId() {
      return parseInt(this.reservationId);
    }
  },
  created() {
    // 로그인 상태 확인
    if (!this.isLoggedIn) {
      this.$router.push('/user/login');
      return;
    }

    // 예약 정보 로드
    this.loadReservation();
  },
  mounted() {
    // 부트스트랩 모달 초기화
    this.initModals();
  },
  methods: {
    ...mapActions('payment', ['fetchReservationForPayment', 'processPayment']),

    /**
     * 부트스트랩 모달 초기화
     */
    initModals() {
      if (window.bootstrap) {
        this.modals.terms = new window.bootstrap.Modal(document.getElementById('termsModal'));
        this.modals.privacy = new window.bootstrap.Modal(document.getElementById('privacyModal'));
        this.modals.refund = new window.bootstrap.Modal(document.getElementById('refundModal'));
      }
    },

    /**
     * 예약 정보 로드
     */
    async loadReservation() {
      this.loading = true;

      try {
        // 결제를 위한 예약 정보 가져오기
        this.reservation = await this.fetchReservationForPayment(this.numericReservationId);
      } catch (error) {
        console.error('예약 정보를 불러오는 중 오류가 발생했습니다:', error);
        this.error = '예약 정보를 불러올 수 없습니다. 다시 시도해주세요.';
      } finally {
        this.loading = false;
      }
    },

    /**
     * 결제 방법 선택
     * @param {string} method - 선택한 결제 방법
     */
    selectPaymentMethod(method) {
      this.paymentMethod = method;
    },

    /**
     * 전체 동의 체크박스 토글
     */
    toggleAllAgreements() {
      const allChecked = this.agreements.all;
      this.agreements.terms = allChecked;
      this.agreements.privacy = allChecked;
      this.agreements.refund = allChecked;
    },

    /**
     * 개별 약관 동의 체크박스 변경 시 전체 동의 체크박스 상태 확인
     */
    checkAllAgreements() {
      this.agreements.all = this.agreements.terms && 
                           this.agreements.privacy && 
                           this.agreements.refund;
    },

    /**
     * 결제 처리
     */
    async processPayment() {
      try {
        // 결제 정보 구성
        const paymentData = {
          reservationId: this.numericReservationId,
          amount: this.reservation.totalPrice * 1.1,
          paymentMethod: this.paymentMethod,
          paymentInfo: {}
        };

        // 결제 방법에 따른 정보 추가
        switch (this.paymentMethod) {
          case 'CARD':
            paymentData.paymentInfo = {
              cardInfo: this.paymentInfo.cardInfo,
              cardExpiry: this.paymentInfo.cardExpiry,
              cardHolder: this.paymentInfo.cardHolder,
              cardCvv: this.paymentInfo.cardCvv
            };
            break;

          case 'BANK_TRANSFER':
            paymentData.paymentInfo = {
              bankName: this.paymentInfo.bankName,
              bankInfo: this.paymentInfo.bankInfo,
              accountHolder: this.paymentInfo.accountHolder
            };
            break;

          case 'PHONE':
            paymentData.paymentInfo = {
              phoneInfo: this.paymentInfo.phoneInfo,
              carrier: this.paymentInfo.carrier,
              birthDate: this.paymentInfo.birthDate
            };
            break;
        }

        // 결제 API 호출
        const result = await this.processPayment(paymentData);

        // 결제 성공 시 결제 상세 페이지로 이동
        this.$router.push({
          path: `/payment/detail/${result.paymentId}`,
          query: { message: '결제가 성공적으로 완료되었습니다.' }
        });
      } catch (error) {
        console.error('결제 처리 중 오류가 발생했습니다:', error);
        this.error = '결제 처리에 실패했습니다. 다시 시도해주세요.';
      }
    },

    /**
     * 날짜 포맷팅 (YYYY-MM-DD)
     * @param {string|Date} date - 포맷팅할 날짜
     * @returns {string} 포맷팅된 날짜 문자열
     */
    formatDate(date) {
      if (!date) return '';

      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');

      return `${year}-${month}-${day}`;
    },

    /**
     * 금액 포맷팅 (₩1,000,000 형식)
     * @param {number} amount - 포맷팅할 금액
     * @returns {string} 포맷팅된 금액 문자열
     */
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
.payment-method-card {
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid #dee2e6;
  border-radius: 10px;
  padding: 20px;
  text-align: center;
  margin-bottom: 20px;
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
