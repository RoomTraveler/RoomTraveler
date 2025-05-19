<template>
  <div>
    <AccommodationHeader title="객실 상세 정보" />
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
              <img :src="room.mainImageUrl" class="d-block w-100" :alt="room.name" />
            </div>
            <div
              v-for="(imageUrl, index) in room.imageUrls"
              :key="index"
              class="carousel-item"
              :class="{ active: !room.mainImageUrl && index === 0 }"
            >
              <img :src="imageUrl" class="d-block w-100" :alt="room.name" />
            </div>
            <div
              v-if="!room.mainImageUrl && (!room.imageUrls || room.imageUrls.length === 0)"
              class="carousel-item active"
            >
              <img src="@/assets/no-image.jpg" class="d-block w-100" alt="이미지 없음" />
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
          <div class="room-type">{{ room.roomType || "스탠다드 룸" }}</div>
          <h1 class="room-title">{{ room.name }}</h1>
          <p class="text-muted">{{ accommodation.title }}</p>
          <hr />
          <h3 class="section-title">객실 정보</h3>
          <div class="info-item">
            <div class="info-icon"><i class="bi bi-people"></i></div>
            <div class="info-text">최대 {{ room.capacity }}인</div>
          </div>
          <div class="info-item">
            <div class="info-icon"><i class="bi bi-rulers"></i></div>
            <div class="info-text">객실 크기: {{ room.roomSize }}㎡</div>
          </div>
          <div class="info-item">
            <div class="info-icon"><i class="bi bi-door-closed"></i></div>
            <div class="info-text">객실 수: {{ room.roomCount }}개</div>
          </div>
          <div class="info-item">
            <div class="info-icon"><i class="bi bi-currency-dollar"></i></div>
            <div class="info-text">가격: {{ formatPrice(room.price) }}/박</div>
          </div>
          <!-- 호스트인 경우 수정/삭제 버튼 표시 -->
          <div v-if="isHost && userId === accommodation.hostId" class="mt-4">
            <router-link :to="`/accommodation/update-room/${room.roomId}`" class="btn btn-outline-primary me-2"
              >수정</router-link
            >
            <button @click="confirmDeleteRoom" class="btn btn-outline-danger">삭제</button>
          </div>
        </div>
        <!-- Tab Bar -->
        <div class="tab-bar">
          <div
            v-for="tab in tabs"
            :key="tab.id"
            class="tab-item"
            :class="{ active: activeTab === tab.id }"
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
                      {{ day.day > 0 ? day.day : "" }}
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
            <form @submit.prevent="submitReservation" class="mt-4">
              <div class="row mb-3">
                <div class="col-md-6">
                  <label for="checkInDate" class="form-label">체크인 날짜</label>
                  <input
                    type="date"
                    class="form-control"
                    id="checkInDate"
                    v-model="reservation.checkInDate"
                    required
                    @change="validateDates"
                  />
                </div>
                <div class="col-md-6">
                  <label for="checkOutDate" class="form-label">체크아웃 날짜</label>
                  <input
                    type="date"
                    class="form-control"
                    id="checkOutDate"
                    v-model="reservation.checkOutDate"
                    required
                    @change="validateDates"
                  />
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
            <hr />
            <h3 class="section-title">침대 유형</h3>
            <p>{{ room.bedType }}</p>
            <hr />
            <h3 class="section-title">체크인/체크아웃 정보</h3>
            <div class="info-item">
              <div class="info-icon"><i class="bi bi-clock"></i></div>
              <div class="info-text">
                체크인: {{ accommodation.checkInTime }} / 체크아웃: {{ accommodation.checkOutTime }}
              </div>
            </div>
            <hr />
            <h3 class="section-title">숙소 정보</h3>
            <div class="info-item">
              <div class="info-icon"><i class="bi bi-geo-alt"></i></div>
              <div class="info-text">{{ accommodation.address }}</div>
            </div>
            <div class="info-item">
              <div class="info-icon"><i class="bi bi-telephone"></i></div>
              <div class="info-text">{{ accommodation.phone }}</div>
            </div>
            <router-link
              :to="`/accommodation/detail/${accommodation.accommodationId}`"
              class="btn btn-outline-primary mt-3"
              >숙소 상세 정보 보기</router-link
            >
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
  </div>
</template>

<script>
import axios from "axios";
import AccommodationHeader from "@/views/accommodation/AccommodationHeader.vue";

export default {
  name: "RoomDetail",
  components: { AccommodationHeader },
  props: {
    roomId: { type: [String, Number], required: true },
  },
  data() {
    return {
      loading: true,
      message: "",
      room: {},
      accommodation: {},
      activeTab: "reservation",
      tabs: [
        { id: "reservation", name: "예약하기" },
        { id: "details", name: "상세 정보" },
        { id: "amenities", name: "편의시설" },
      ],
      reservation: {
        checkInDate: "",
        checkOutDate: "",
        guestCount: 1,
      },
      currentMonth: new Date().getMonth(),
      currentYear: new Date().getFullYear(),
      availability: {},
      selectedCheckInDate: null,
      selectedCheckOutDate: null,
      // 사용자 정보
      userId: null,
      isHost: false,
      isLoggedIn: false,
    };
  },
  computed: {
    currentMonthText() {
      const monthNames = ["1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월"];
      return `${this.currentYear}년 ${monthNames[this.currentMonth]}`;
    },
    calendarDays() {
      const daysInMonth = new Date(this.currentYear, this.currentMonth + 1, 0).getDate();
      const firstDay = new Date(this.currentYear, this.currentMonth, 1).getDay();
      const days = [];
      let dayCounter = 1;
      for (let week = 0; week < 6; week++) {
        const weekDays = [];
        for (let day = 0; day < 7; day++) {
          if ((week === 0 && day < firstDay) || dayCounter > daysInMonth) {
            weekDays.push({ day: 0, date: null, available: false });
          } else {
            const date = new Date(this.currentYear, this.currentMonth, dayCounter);
            const dateString = this.formatDateString(date);
            const isToday = this.isToday(date);
            const isPast = this.isPastDate(date);
            const isAvailable = !isPast && this.checkAvailability(dateString);
            weekDays.push({
              day: dayCounter,
              date,
              dateString,
              isToday,
              isPast,
              available: isAvailable,
            });
            dayCounter++;
          }
        }
        days.push(weekDays);
        if (dayCounter > daysInMonth) break;
      }
      return days;
    },
    roomPrice() {
      if (!this.reservation.checkInDate || !this.reservation.checkOutDate) return 0;
      const nights = this.calculateNights(this.reservation.checkInDate, this.reservation.checkOutDate);
      return nights * (this.room.price || 0);
    },
    taxFee() {
      return Math.round(this.roomPrice * 0.1);
    },
    totalPrice() {
      return this.roomPrice + this.taxFee;
    },
    amenitiesList() {
      if (!this.room.amenities) return [];
      return this.room.amenities.split(",").map((item) => item.trim());
    },
  },
  async created() {
    // 사용자 정보 (localStorage에서 가져옴)
    const userStr = localStorage.getItem("user");
    if (userStr) {
      const user = JSON.parse(userStr);
      this.userId = user.id;
      this.isHost = user.role === "HOST";
      this.isLoggedIn = true;
    }
    if (this.$route.query.message) this.message = this.$route.query.message;
    await this.loadRoomDetail();
    this.initDates();
  },
  mounted() {
    if (window.bootstrap) {
      new window.bootstrap.Carousel(document.getElementById("roomCarousel"));
    }
  },
  methods: {
    async loadRoomDetail() {
      this.loading = true;
      try {
        const { data } = await axios.get(`/api/accommodations/room/${this.roomId}`);
        this.room = data.room || {};
        this.accommodation = data.accommodation || {};
      } catch (error) {
        this.message = error.response?.data?.error || error.message || "객실 정보를 불러올 수 없습니다.";
        console.error("객실 정보를 불러오는 중 오류가 발생했습니다:", error);
      } finally {
        this.loading = false;
      }
    },
    initDates() {
      const today = new Date();
      const tomorrow = new Date(today);
      tomorrow.setDate(tomorrow.getDate() + 1);
      const dayAfterTomorrow = new Date(today);
      dayAfterTomorrow.setDate(dayAfterTomorrow.getDate() + 2);
      this.reservation.checkInDate = this.formatDateString(tomorrow);
      this.reservation.checkOutDate = this.formatDateString(dayAfterTomorrow);
    },
    prevMonth() {
      this.currentMonth--;
      if (this.currentMonth < 0) {
        this.currentMonth = 11;
        this.currentYear--;
      }
    },
    nextMonth() {
      this.currentMonth++;
      if (this.currentMonth > 11) {
        this.currentMonth = 0;
        this.currentYear++;
      }
    },
    selectDate(date) {
      if (!date) return;
      const dateString = this.formatDateString(date);
      if (!this.selectedCheckInDate || this.selectedCheckOutDate) {
        this.selectedCheckInDate = dateString;
        this.selectedCheckOutDate = null;
        this.reservation.checkInDate = dateString;
        const nextDay = new Date(date);
        nextDay.setDate(nextDay.getDate() + 1);
        this.reservation.checkOutDate = this.formatDateString(nextDay);
      } else if (this.selectedCheckInDate && !this.selectedCheckOutDate) {
        if (date < new Date(this.selectedCheckInDate)) {
          this.selectedCheckInDate = dateString;
          this.reservation.checkInDate = dateString;
          const nextDay = new Date(date);
          nextDay.setDate(nextDay.getDate() + 1);
          this.reservation.checkOutDate = this.formatDateString(nextDay);
        } else {
          this.selectedCheckOutDate = dateString;
          this.reservation.checkOutDate = dateString;
        }
      }
    },
    validateDates() {
      const checkInDate = new Date(this.reservation.checkInDate);
      const checkOutDate = new Date(this.reservation.checkOutDate);
      if (checkOutDate <= checkInDate) {
        const nextDay = new Date(checkInDate);
        nextDay.setDate(nextDay.getDate() + 1);
        this.reservation.checkOutDate = this.formatDateString(nextDay);
      }
      this.selectedCheckInDate = this.reservation.checkInDate;
      this.selectedCheckOutDate = this.reservation.checkOutDate;
    },
    getDayClass(day) {
      if (!day.day) return {};
      const classes = {};
      if (day.isToday) classes.today = true;
      if (day.isPast) classes.unavailable = true;
      else classes.available = true;
      if (day.dateString === this.selectedCheckInDate) classes.selected = true;
      if (day.dateString === this.selectedCheckOutDate) classes.selected = true;
      return classes;
    },
    isToday(date) {
      const today = new Date();
      return (
        date.getDate() === today.getDate() &&
        date.getMonth() === today.getMonth() &&
        date.getFullYear() === today.getFullYear()
      );
    },
    isPastDate(date) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return date < today;
    },
    checkAvailability(dateString) {
      return true;
    },
    formatDateString(date) {
      if (!date) return "";
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      return `${year}-${month}-${day}`;
    },
    formatPrice(price) {
      return new Intl.NumberFormat("ko-KR", { style: "currency", currency: "KRW", maximumFractionDigits: 0 }).format(
        price || 0
      );
    },
    calculateNights(checkInDate, checkOutDate) {
      const start = new Date(checkInDate);
      const end = new Date(checkOutDate);
      const diffTime = Math.abs(end - start);
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays;
    },
    async submitReservation() {
      if (!this.isLoggedIn) {
        this.$router.push({ path: "/user/login", query: { redirect: this.$route.fullPath } });
        return;
      }
      try {
        const reservationData = {
          roomId: this.roomId,
          accommodationId: this.accommodation.accommodationId,
          userId: this.userId,
          checkInDate: this.reservation.checkInDate,
          checkOutDate: this.reservation.checkOutDate,
          guestCount: this.reservation.guestCount,
          totalPrice: this.totalPrice,
        };
        alert(
          "예약 기능은 백엔드 API 구현 후 연동 예정입니다.\n선택된 정보:\n" + JSON.stringify(reservationData, null, 2)
        );
      } catch (error) {
        console.error("Error submitting reservation:", error);
        alert(error.response?.data?.message || error.message || "예약에 실패했습니다. 다시 시도해주세요.");
      }
    },
    async addToCart() {
      if (!this.isLoggedIn) {
        this.$router.push({ path: "/user/login", query: { redirect: this.$route.fullPath } });
        return;
      }
      try {
        const cartItem = {
          roomId: this.roomId,
          userId: this.userId,
          accommodationId: this.accommodation.accommodationId,
          checkInDate: this.reservation.checkInDate,
          checkOutDate: this.reservation.checkOutDate,
          guestCount: this.reservation.guestCount,
          price: this.room.price,
        };
        this.message =
          "장바구니 추가 기능은 백엔드 API 구현 후 연동 예정입니다.\n선택된 정보:\n" +
          JSON.stringify(cartItem, null, 2);
        setTimeout(() => {
          this.message = "";
        }, 5000);
      } catch (error) {
        console.error("Error adding to cart:", error);
        alert(error.response?.data?.message || error.message || "장바구니 추가에 실패했습니다. 다시 시도해주세요.");
      }
    },
    confirmDeleteRoom() {
      if (confirm("정말로 이 객실을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
        this.deleteRoomItem();
      }
    },
    async deleteRoomItem() {
      try {
        // 객실 삭제 API 연동 필요 (예시)
        alert("객실이 삭제되었습니다. (API 연동 필요)");
        this.$router.push({ path: `/accommodation/detail/${this.accommodation.accommodationId}` });
      } catch (error) {
        alert("객실 삭제에 실패했습니다. 다시 시도해주세요.");
      }
    },
    scrollToReservation() {
      this.activeTab = "reservation";
      const tabBar = document.querySelector(".tab-bar");
      if (tabBar) {
        window.scrollTo({ top: tabBar.offsetTop - 20, behavior: "smooth" });
      }
    },
  },
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
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

.calendar th,
.calendar td {
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
