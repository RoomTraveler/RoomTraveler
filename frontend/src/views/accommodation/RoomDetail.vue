<template>
  <Layout>
    <div class="container mt-4">
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>

      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">객실 정보를 불러오는 중입니다...</p>
      </div>

      <template v-else>
        <!-- Image Carousel -->
        <div id="roomCarousel" class="carousel slide mb-4" data-bs-ride="carousel">
          <div class="carousel-inner">
            <div v-if="room.mainImageUrl" class="carousel-item active">
              <img :src="room.mainImageUrl" class="d-block w-100" :alt="room.name">
            </div>
            <div v-for="(imageUrl, index) in room.imageUrls" :key="index" class="carousel-item" :class="{ 'active': !room.mainImageUrl && index === 0 }">
              <img :src="imageUrl" class="d-block w-100" :alt="room.name">
            </div>
            <div v-if="!room.mainImageUrl && (!room.imageUrls || room.imageUrls.length === 0)" class="carousel-item active">
              <img src="@/assets/no-image.jpg" class="d-block w-100" alt="이미지 없음">
            </div>
          </div>
          <button class="carousel-control-prev" type="button" data-bs-target="#roomCarousel" data-bs-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Previous</span>
          </button>
          <button class="carousel-control-next" type="button" data-bs-target="#roomCarousel" data-bs-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Next</span>
          </button>
        </div>

        <!-- Room Info -->
        <div class="room-info">
          <div class="room-type">
            {{ room.roomType || '스탠다드 룸' }}
          </div>
          <h1 class="room-title">{{ room.name }}</h1>
          <p class="text-muted">{{ accommodation.title }}</p>

          <hr>

          <h3 class="section-title">객실 정보</h3>
          <div class="info-item">
            <div class="info-icon"><i class="bi bi-people"></i></div>
            <div class="info-text">
              최대 {{ room.capacity }}인
            </div>
          </div>
          <div class="info-item">
            <div class="info-icon"><i class="bi bi-rulers"></i></div>
            <div class="info-text">
              객실 크기: {{ room.roomSize }}㎡
            </div>
          </div>
          <div class="info-item">
            <div class="info-icon"><i class="bi bi-door-closed"></i></div>
            <div class="info-text">
              객실 수: {{ room.roomCount }}개
            </div>
          </div>
          <div class="info-item">
            <div class="info-icon"><i class="bi bi-currency-dollar"></i></div>
            <div class="info-text">
              가격: {{ formatPrice(room.price) }}/박
            </div>
          </div>

          <!-- 호스트인 경우 수정/삭제 버튼 표시 -->
          <div v-if="isHost && userId === accommodation.hostId" class="mt-4">
            <router-link :to="`/accommodation/update-room/${room.roomId}`" class="btn btn-outline-primary me-2">수정</router-link>
            <button @click="confirmDeleteRoom" class="btn btn-outline-danger">삭제</button>
          </div>
        </div>

        <!-- Tab Bar -->
        <div class="tab-bar">
          <div 
            v-for="tab in tabs" 
            :key="tab.id" 
            class="tab-item" 
            :class="{ 'active': activeTab === tab.id }"
            @click="activeTab = tab.id"
          >
            {{ tab.name }}
          </div>
        </div>

        <!-- Tab Contents -->
        <div v-show="activeTab === 'reservation'" class="tab-content">
          <div class="reservation-form">
            <h3 class="section-title">날짜 선택</h3>

            <div class="d-flex justify-content-between mb-3">
              <button class="btn btn-outline-secondary" @click="prevMonth">
                <i class="bi bi-chevron-left"></i> 이전 달
              </button>
              <h5 class="mb-0 align-self-center">{{ currentMonthText }}</h5>
              <button class="btn btn-outline-secondary" @click="nextMonth">
                다음 달 <i class="bi bi-chevron-right"></i>
              </button>
            </div>

            <div class="calendar-container">
              <table class="calendar">
                <thead>
                  <tr>
                    <th>일</th>
                    <th>월</th>
                    <th>화</th>
                    <th>수</th>
                    <th>목</th>
                    <th>금</th>
                    <th>토</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="(week, weekIndex) in calendarDays" :key="'week-' + weekIndex">
                    <td 
                      v-for="(day, dayIndex) in week" 
                      :key="'day-' + dayIndex"
                      :class="getDayClass(day)"
                      @click="day.available && selectDate(day.date)"
                    >
                      {{ day.day > 0 ? day.day : '' }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>

            <form @submit.prevent="submitReservation" class="mt-4">
              <div class="row mb-3">
                <div class="col-md-6">
                  <label for="checkInDate" class="form-label">체크인 날짜</label>
                  <input type="date" class="form-control" id="checkInDate" v-model="reservation.checkInDate" required @change="validateDates">
                </div>
                <div class="col-md-6">
                  <label for="checkOutDate" class="form-label">체크아웃 날짜</label>
                  <input type="date" class="form-control" id="checkOutDate" v-model="reservation.checkOutDate" required @change="validateDates">
                </div>
              </div>

              <div class="mb-3">
                <label for="guestCount" class="form-label">인원 수</label>
                <select class="form-select" id="guestCount" v-model="reservation.guestCount">
                  <option v-for="i in room.capacity" :key="i" :value="i">{{ i }}명</option>
                </select>
              </div>

              <div class="price-breakdown">
                <div class="price-item">
                  <div>객실 요금</div>
                  <div>{{ formatPrice(roomPrice) }}</div>
                </div>
                <div class="price-item">
                  <div>세금 및 봉사료</div>
                  <div>{{ formatPrice(taxFee) }}</div>
                </div>
                <div class="price-total">
                  <div>총 결제 금액</div>
                  <div>{{ formatPrice(totalPrice) }}</div>
                </div>
              </div>

              <button type="submit" class="btn btn-yanolja w-100 mt-3">예약하기</button>
            </form>

            <!-- 장바구니 추가 폼 -->
            <form @submit.prevent="addToCart" class="mt-2">
              <div class="d-grid">
                <button type="submit" class="btn btn-outline-primary">장바구니에 담기</button>
              </div>
            </form>
          </div>
        </div>

        <div v-show="activeTab === 'details'" class="tab-content">
          <div class="room-info">
            <h3 class="section-title">객실 설명</h3>
            <p>{{ room.description }}</p>

            <hr>

            <h3 class="section-title">침대 유형</h3>
            <p>{{ room.bedType }}</p>

            <hr>

            <h3 class="section-title">체크인/체크아웃 정보</h3>
            <div class="info-item">
              <div class="info-icon"><i class="bi bi-clock"></i></div>
              <div class="info-text">
                체크인: {{ accommodation.checkInTime }} / 체크아웃: {{ accommodation.checkOutTime }}
              </div>
            </div>

            <hr>

            <h3 class="section-title">숙소 정보</h3>
            <div class="info-item">
              <div class="info-icon"><i class="bi bi-geo-alt"></i></div>
              <div class="info-text">
                {{ accommodation.address }}
              </div>
            </div>
            <div class="info-item">
              <div class="info-icon"><i class="bi bi-telephone"></i></div>
              <div class="info-text">
                {{ accommodation.phone }}
              </div>
            </div>

            <router-link :to="`/accommodation/detail/${accommodation.accommodationId}`" class="btn btn-outline-primary mt-3">숙소 상세 정보 보기</router-link>
          </div>
        </div>

        <div v-show="activeTab === 'amenities'" class="tab-content">
          <div class="room-info">
            <h3 class="section-title">객실 내 시설</h3>
            <div class="amenities-list">
              <template v-if="room.amenities && room.amenities.length > 0">
                <div v-for="(amenity, index) in amenitiesList" :key="index" class="amenity-item">
                  <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                  <div>{{ amenity }}</div>
                </div>
              </template>
              <template v-else>
                <!-- 샘플 편의시설 -->
                <div class="amenity-item">
                  <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                  <div>무료 Wi-Fi</div>
                </div>
                <div class="amenity-item">
                  <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                  <div>에어컨</div>
                </div>
                <div class="amenity-item">
                  <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                  <div>TV</div>
                </div>
                <div class="amenity-item">
                  <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                  <div>미니바</div>
                </div>
                <div class="amenity-item">
                  <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                  <div>욕실용품</div>
                </div>
                <div class="amenity-item">
                  <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                  <div>헤어드라이어</div>
                </div>
              </template>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Booking Bar -->
    <div v-if="!loading" class="booking-bar">
      <div class="booking-price">
        {{ formatPrice(room.price) }}
        <span class="booking-price-unit">/ 1박</span>
      </div>
      <a href="#" @click.prevent="scrollToReservation" class="btn btn-yanolja">예약하기</a>
    </div>
  </Layout>
</template>

<script>
/**
 * 객실 상세 컴포넌트
 * 
 * 이 컴포넌트는 특정 객실의 상세 정보를 표시합니다.
 * 객실 정보, 예약 기능, 장바구니 담기 기능을 제공합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'RoomDetail',
  components: {
    Layout
  },
  props: {
    roomId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      loading: true,
      message: '',
      room: {},
      accommodation: {},
      activeTab: 'reservation',
      tabs: [
        { id: 'reservation', name: '예약하기' },
        { id: 'details', name: '상세 정보' },
        { id: 'amenities', name: '편의시설' }
      ],
      reservation: {
        checkInDate: '',
        checkOutDate: '',
        guestCount: 1
      },
      currentMonth: new Date().getMonth(),
      currentYear: new Date().getFullYear(),
      availability: {}, // 날짜별 가용성 정보
      selectedCheckInDate: null,
      selectedCheckOutDate: null
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.user.isLoggedIn,
      username: state => state.user.user?.username,
      userId: state => state.user.user?.id,
      isHost: state => state.user.user?.role === 'HOST'
    }),

    /**
     * 현재 표시 중인 월 텍스트
     */
    currentMonthText() {
      const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];
      return `${this.currentYear}년 ${monthNames[this.currentMonth]}`;
    },

    /**
     * 캘린더 날짜 배열
     */
    calendarDays() {
      const daysInMonth = new Date(this.currentYear, this.currentMonth + 1, 0).getDate();
      const firstDay = new Date(this.currentYear, this.currentMonth, 1).getDay();

      const days = [];
      let dayCounter = 1;

      // 6주 캘린더 생성
      for (let week = 0; week < 6; week++) {
        const weekDays = [];

        for (let day = 0; day < 7; day++) {
          if ((week === 0 && day < firstDay) || dayCounter > daysInMonth) {
            // 이전 달 또는 다음 달 날짜
            weekDays.push({ day: 0, date: null, available: false });
          } else {
            // 현재 달 날짜
            const date = new Date(this.currentYear, this.currentMonth, dayCounter);
            const dateString = this.formatDateString(date);
            const isToday = this.isToday(date);
            const isPast = this.isPastDate(date);

            // 가용성 확인 (실제로는 API에서 가져온 데이터 사용)
            const isAvailable = !isPast && this.checkAvailability(dateString);

            weekDays.push({
              day: dayCounter,
              date,
              dateString,
              isToday,
              isPast,
              available: isAvailable
            });

            dayCounter++;
          }
        }

        days.push(weekDays);

        // 모든 날짜를 표시했으면 종료
        if (dayCounter > daysInMonth) {
          break;
        }
      }

      return days;
    },

    /**
     * 객실 요금 계산
     */
    roomPrice() {
      if (!this.reservation.checkInDate || !this.reservation.checkOutDate) return 0;

      const nights = this.calculateNights(this.reservation.checkInDate, this.reservation.checkOutDate);
      return nights * this.room.price;
    },

    /**
     * 세금 및 봉사료 계산 (10%)
     */
    taxFee() {
      return Math.round(this.roomPrice * 0.1);
    },

    /**
     * 총 결제 금액
     */
    totalPrice() {
      return this.roomPrice + this.taxFee;
    },

    /**
     * 편의시설 목록
     */
    amenitiesList() {
      if (!this.room.amenities) return [];
      return this.room.amenities.split(',').map(item => item.trim());
    }
  },
  async created() {
    // URL 쿼리 파라미터에서 메시지 확인
    if (this.$route.query.message) {
      this.message = this.$route.query.message;
    }

    // 객실 정보 로드
    await this.loadRoomDetail();

    // 초기 날짜 설정
    this.initDates();

    // 가용성 정보 로드
    this.loadAvailability();
  },
  mounted() {
    // 부트스트랩 캐러셀 초기화
    if (window.bootstrap) {
      new window.bootstrap.Carousel(document.getElementById('roomCarousel'));
    }
  },
  methods: {
    ...mapActions('accommodation', ['fetchRoomDetail', 'deleteRoom']),
    ...mapActions('reservation', ['createReservation']),
    ...mapActions('cart', ['addCartItem']),

    /**
     * 객실 상세 정보 로드
     */
    async loadRoomDetail() {
      this.loading = true;
      try {
        const data = await this.fetchRoomDetail(this.roomId);
        this.room = data.room;
        this.accommodation = data.accommodation;
      } catch (error) {
        console.error('객실 정보를 불러오는 중 오류가 발생했습니다:', error);
      } finally {
        this.loading = false;
      }
    },

    /**
     * 초기 날짜 설정
     */
    initDates() {
      const today = new Date();
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);

      const dayAfterTomorrow = new Date(today);
      dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 2);

      this.reservation.checkInDate = this.formatDateString(tomorrow);
      this.reservation.checkOutDate = this.formatDateString(dayAfterTomorrow);
    },

    /**
     * 가용성 정보 로드
     */
    async loadAvailability() {
      try {
        // 실제 구현에서는 API 호출
        // const response = await fetch(`/api/rooms/${this.roomId}/availability?year=${this.currentYear}&month=${this.currentMonth + 1}`);
        // this.availability = await response.json();

        // 임시 가용성 데이터 생성 (실제 구현에서는 API에서 가져옴)
        this.generateDummyAvailability();
      } catch (error) {
        console.error('가용성 정보를 불러오는 중 오류가 발생했습니다:', error);
      }
    },

    /**
     * 임시 가용성 데이터 생성
     */
    generateDummyAvailability() {
      const daysInMonth = new Date(this.currentYear, this.currentMonth + 1, 0).getDate();
      const availability = {};

      for (let day = 1; day <= daysInMonth; day++) {
        const date = new Date(this.currentYear, this.currentMonth, day);
        const dateString = this.formatDateString(date);

        // 과거 날짜는 예약 불가
        if (this.isPastDate(date)) {
          availability[dateString] = false;
        } else {
          // 임의로 70%의 날짜는 예약 가능하게 설정
          availability[dateString] = Math.random() > 0.3;
        }
      }

      this.availability = availability;
    },

    /**
     * 이전 달로 이동
     */
    prevMonth() {
      this.currentMonth--;
      if (this.currentMonth < 0) {
        this.currentMonth = 11;
        this.currentYear--;
      }
      this.loadAvailability();
    },

    /**
     * 다음 달로 이동
     */
    nextMonth() {
      this.currentMonth++;
      if (this.currentMonth > 11) {
        this.currentMonth = 0;
        this.currentYear++;
      }
      this.loadAvailability();
    },

    /**
     * 날짜 선택
     * @param {Date} date - 선택한 날짜
     */
    selectDate(date) {
      if (!date) return;

      const dateString = this.formatDateString(date);

      // 체크인 날짜가 선택되지 않았거나, 체크아웃 날짜가 이미 선택된 경우
      if (!this.selectedCheckInDate || this.selectedCheckOutDate) {
        this.selectedCheckInDate = dateString;
        this.selectedCheckOutDate = null;
        this.reservation.checkInDate = dateString;

        // 체크아웃 날짜는 체크인 다음날로 설정
        const nextDay = new Date(date);
        nextDay.setDate(nextDay.getDate() + 1);
        this.reservation.checkOutDate = this.formatDateString(nextDay);
      } 
      // 체크인 날짜만 선택된 경우
      else if (this.selectedCheckInDate && !this.selectedCheckOutDate) {
        // 선택한 날짜가 체크인 날짜보다 이전이면 체크인 날짜를 변경
        if (date < new Date(this.selectedCheckInDate)) {
          this.selectedCheckInDate = dateString;
          this.reservation.checkInDate = dateString;

          // 체크아웃 날짜는 체크인 다음날로 설정
          const nextDay = new Date(date);
          nextDay.setDate(nextDay.getDate() + 1);
          this.reservation.checkOutDate = this.formatDateString(nextDay);
        } else {
          this.selectedCheckOutDate = dateString;
          this.reservation.checkOutDate = dateString;
        }
      }
    },

    /**
     * 날짜 유효성 검사
     */
    validateDates() {
      const checkInDate = new Date(this.reservation.checkInDate);
      const checkOutDate = new Date(this.reservation.checkOutDate);

      // 체크아웃 날짜가 체크인 날짜보다 이전이면 체크인 다음날로 설정
      if (checkOutDate <= checkInDate) {
        const nextDay = new Date(checkInDate);
        nextDay.setDate(nextDay.getDate() + 1);
        this.reservation.checkOutDate = this.formatDateString(nextDay);
      }

      this.selectedCheckInDate = this.reservation.checkInDate;
      this.selectedCheckOutDate = this.reservation.checkOutDate;
    },

    /**
     * 날짜 클래스 반환
     * @param {Object} day - 날짜 객체
     * @returns {Object} 클래스 객체
     */
    getDayClass(day) {
      if (!day.day) return {};

      const classes = {};

      if (day.isToday) classes.today = true;
      if (day.isPast || !day.available) classes.unavailable = true;
      if (day.available) classes.available = true;

      // 선택된 날짜 표시
      if (day.dateString === this.selectedCheckInDate) classes.selected = true;
      if (day.dateString === this.selectedCheckOutDate) classes.selected = true;

      return classes;
    },

    /**
     * 날짜가 오늘인지 확인
     * @param {Date} date - 확인할 날짜
     * @returns {boolean} 오늘 여부
     */
    isToday(date) {
      const today = new Date();
      return date.getDate() === today.getDate() &&
             date.getMonth() === today.getMonth() &&
             date.getFullYear() === today.getFullYear();
    },

    /**
     * 날짜가 과거인지 확인
     * @param {Date} date - 확인할 날짜
     * @returns {boolean} 과거 여부
     */
    isPastDate(date) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return date < today;
    },

    /**
     * 날짜 가용성 확인
     * @param {string} dateString - 확인할 날짜 문자열
     * @returns {boolean} 가용성 여부
     */
    checkAvailability(dateString) {
      return this.availability[dateString] !== false;
    },

    /**
     * 날짜를 YYYY-MM-DD 형식으로 변환
     * @param {Date} date - 변환할 날짜
     * @returns {string} 변환된 날짜 문자열
     */
    formatDateString(date) {
      if (!date) return '';

      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');

      return `${year}-${month}-${day}`;
    },

    /**
     * 가격 포맷팅
     * @param {number} price - 포맷팅할 가격
     * @returns {string} 포맷팅된 가격 문자열
     */
    formatPrice(price) {
      return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 }).format(price);
    },

    /**
     * 숙박 일수 계산
     * @param {string} checkInDate - 체크인 날짜
     * @param {string} checkOutDate - 체크아웃 날짜
     * @returns {number} 숙박 일수
     */
    calculateNights(checkInDate, checkOutDate) {
      const start = new Date(checkInDate);
      const end = new Date(checkOutDate);
      const diffTime = Math.abs(end - start);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays;
    },

    /**
     * 예약 제출
     */
    async submitReservation() {
      if (!this.isLoggedIn) {
        // 로그인 페이지로 리다이렉트
        this.$router.push({
          path: '/user/login',
          query: { redirect: this.$route.fullPath }
        });
        return;
      }

      try {
        const reservationData = {
          roomId: this.roomId,
          checkInDate: this.reservation.checkInDate,
          checkOutDate: this.reservation.checkOutDate,
          guestCount: this.reservation.guestCount,
          price: this.totalPrice
        };

        const result = await this.createReservation(reservationData);

        // 예약 성공 시 예약 상세 페이지로 이동
        this.$router.push({
          path: `/reservation/detail/${result.reservationId}`,
          query: { message: '예약이 성공적으로 완료되었습니다.' }
        });
      } catch (error) {
        console.error('예약 중 오류가 발생했습니다:', error);
        alert('예약에 실패했습니다. 다시 시도해주세요.');
      }
    },

    /**
     * 장바구니에 추가
     */
    async addToCart() {
      if (!this.isLoggedIn) {
        // 로그인 페이지로 리다이렉트
        this.$router.push({
          path: '/user/login',
          query: { redirect: this.$route.fullPath }
        });
        return;
      }

      try {
        const cartItem = {
          roomId: this.roomId,
          checkInDate: this.reservation.checkInDate,
          checkOutDate: this.reservation.checkOutDate,
          guestCount: this.reservation.guestCount,
          price: this.totalPrice
        };

        await this.addCartItem(cartItem);

        // 성공 메시지 표시
        this.message = '장바구니에 추가되었습니다.';

        // 3초 후 메시지 숨기기
        setTimeout(() => {
          this.message = '';
        }, 3000);
      } catch (error) {
        console.error('장바구니 추가 중 오류가 발생했습니다:', error);
        alert('장바구니 추가에 실패했습니다. 다시 시도해주세요.');
      }
    },

    /**
     * 객실 삭제 확인
     */
    confirmDeleteRoom() {
      if (confirm('정말로 이 객실을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        this.deleteRoomItem();
      }
    },

    /**
     * 객실 삭제 처리
     */
    async deleteRoomItem() {
      try {
        await this.deleteRoom(this.roomId);

        // 삭제 성공 시 숙소 상세 페이지로 이동
        this.$router.push({
          path: `/accommodation/detail/${this.accommodation.accommodationId}`,
          query: { message: '객실이 성공적으로 삭제되었습니다.' }
        });
      } catch (error) {
        console.error('객실 삭제 중 오류가 발생했습니다:', error);
        alert('객실 삭제에 실패했습니다. 다시 시도해주세요.');
      }
    },

    /**
     * 예약 탭으로 스크롤
     */
    scrollToReservation() {
      this.activeTab = 'reservation';

      // 탭 바로 스크롤
      const tabBar = document.querySelector('.tab-bar');
      if (tabBar) {
        window.scrollTo({
          top: tabBar.offsetTop - 20,
          behavior: 'smooth'
        });
      }
    }
  }
};
</script>

<style scoped>
:root {
  --yanolja-red: #f0213b;
  --yanolja-pink: #ff3478;
  --yanolja-light-gray: #f5f5f5;
  --yanolja-dark-gray: #666;
}

.room-info {
  background-color: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.room-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 5px;
}

.room-type {
  font-size: 0.9rem;
  color: var(--yanolja-dark-gray);
  margin-bottom: 10px;
}

.section-title {
  font-weight: bold;
  margin-bottom: 15px;
  font-size: 1.2rem;
}

.info-item {
  margin-bottom: 10px;
  display: flex;
  align-items: flex-start;
}

.info-icon {
  color: var(--yanolja-dark-gray);
  margin-right: 10px;
  font-size: 1.1rem;
  width: 20px;
  text-align: center;
}

.info-text {
  flex: 1;
}

.tab-bar {
  display: flex;
  background-color: white;
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.tab-item {
  flex: 1;
  text-align: center;
  padding: 15px 0;
  font-weight: bold;
  cursor: pointer;
  border-bottom: 3px solid transparent;
}

.tab-item.active {
  color: var(--yanolja-red);
  border-bottom-color: var(--yanolja-red);
}

.tab-content {
  display: block;
}

.booking-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: white;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  padding: 15px;
  z-index: 999;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.booking-price {
  font-weight: bold;
  font-size: 1.2rem;
}

.booking-price-unit {
  font-size: 0.8rem;
  font-weight: normal;
  color: var(--yanolja-dark-gray);
}

.amenities-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.amenity-item {
  display: flex;
  align-items: center;
}

.amenity-icon {
  color: var(--yanolja-dark-gray);
  margin-right: 10px;
  font-size: 1.1rem;
}

.calendar {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.calendar th, .calendar td {
  border: 1px solid #dee2e6;
  padding: 10px;
  text-align: center;
}

.calendar th {
  background-color: var(--yanolja-light-gray);
  font-weight: bold;
}

.calendar .available {
  background-color: #d4edda;
  cursor: pointer;
}

.calendar .unavailable {
  background-color: #f8d7da;
  color: #721c24;
  text-decoration: line-through;
}

.calendar .selected {
  background-color: var(--yanolja-red);
  color: white;
  font-weight: bold;
}

.calendar .today {
  font-weight: bold;
  border: 2px solid #0d6efd;
}

.reservation-form {
  background-color: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.price-breakdown {
  background-color: var(--yanolja-light-gray);
  border-radius: 10px;
  padding: 15px;
  margin-top: 20px;
}

.price-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.price-total {
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #dee2e6;
}

.btn-yanolja {
  background-color: var(--yanolja-red);
  color: white;
  border: none;
}

.btn-yanolja:hover {
  background-color: #d01c33;
  color: white;
}

/* 모바일 화면에서 하단 여백 추가 */
@media (max-width: 768px) {
  .container {
    margin-bottom: 80px;
  }
}
</style>
