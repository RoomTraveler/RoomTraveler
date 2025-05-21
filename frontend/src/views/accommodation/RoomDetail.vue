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
        <div id="roomCarousel" class="carousel slide mb-4 position-relative" data-bs-touch="true">
          <div class="carousel-inner">
            <div
              v-for="(imgSrc, index) in imagesForCarousel"
              :key="index"
              class="carousel-item"
              :class="{ active: index === activeImageIndex }"
            >
              <img :src="imgSrc" class="d-block w-100" :alt="`${room.name || '객실 이미지'} ${index + 1}`" />
            </div>
            <!-- 커스텀 컨트롤 버튼을 carousel-inner 내부로 이동 -->
            <template v-if="showCustomCarouselControls">
              <button class="custom-carousel-control prev" @click="prevImage" type="button">
                <i class="bi bi-chevron-left"></i>
              </button>
              <button class="custom-carousel-control next" @click="nextImage" type="button">
                <i class="bi bi-chevron-right"></i>
              </button>
            </template>
          </div>
          <!-- 기존 컨트롤 버튼 삭제 (이미 삭제됨) -->
          <!-- 새로운 커스텀 컨트롤 버튼 (carousel-inner 외부에서 내부로 이동) -->
        </div>
        <!-- Room Info -->
        <div class="room-info">
          <div class="room-type">{{ room.roomType || "스탠다드 룸" }}</div>
          <h1 class="room-title">{{ room.name }}</h1>
          <p class="text-muted">{{ accommodation.title }}</p>
          <hr />

          <!-- 선택된 예약 정보 표시 섹션 -->
          <h3 class="section-title">선택된 예약 정보</h3>
          <div class="selected-reservation-details">
            <div class="detail-item">
              <i class="bi bi-calendar-check"></i>
              <span>체크인: {{ displaySelectedCheckInDate }}</span>
            </div>
            <div class="detail-item">
              <i class="bi bi-calendar-event"></i>
              <span>체크아웃: {{ displaySelectedCheckOutDate }}</span>
            </div>
            <div class="detail-item">
              <i class="bi bi-people-fill"></i>
              <span>인원: {{ displaySelectedGuests }}</span>
            </div>
            <div class="detail-item" v-if="displayNights > 0">
              <i class="bi bi-moon-stars-fill"></i>
              <span>숙박일: {{ displayNights }}박</span>
            </div>
          </div>
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

        <!-- 후기 섹션 (예약하기 섹션 위로 이동) -->
        <hr class="my-4" />
        <!-- 객실 정보와 후기 섹션 사이의 구분선 -->
        <div class="tab-content-section reviews-wrapper mt-4">
          <h3 class="section-title">후기</h3>
          <div class="reviews-container">
            <div v-for="review in visibleReviews" :key="review.id" class="review-card">
              <div class="review-header">
                <div class="review-rating">
                  <i
                    v-for="star in 5"
                    :key="star"
                    class="bi"
                    :class="star <= review.rating ? 'bi-star-fill' : 'bi-star'"
                  ></i>
                </div>
                <div class="review-nickname-date">
                  <span class="review-nickname">{{ review.nickname }}</span>
                  <span class="review-date">{{ review.date }}</span>
                </div>
              </div>
              <h4 class="review-room-name">{{ review.roomName }}</h4>
              <p class="review-content">{{ review.content }}</p>
            </div>
          </div>
          <div v-if="totalReviewCount > displayReviewCount" class="text-center mt-3">
            <button @click="displayReviewCount = totalReviewCount" class="btn btn-outline-secondary w-100">
              {{ totalReviewCount }}개 객실후기 보기
            </button>
          </div>
          <div v-else-if="totalReviewCount > 0 && totalReviewCount === displayReviewCount" class="text-center mt-3">
            <button @click="displayReviewCount = 2" class="btn btn-outline-secondary w-100" v-if="totalReviewCount > 2">
              후기 접기
            </button>
          </div>
          <div v-if="totalReviewCount === 0" class="text-center text-muted mt-3">
            <p>아직 작성된 후기가 없습니다.</p>
          </div>
        </div>

        <hr class="my-4" />

        <!-- 상세 정보 섹션 -->
        <div class="tab-content-section details-wrapper mt-4">
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
          </div>
        </div>

        <hr class="my-4" />

        <!-- 편의시설 섹션 -->
        <div class="tab-content-section amenities-wrapper mt-4">
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
      <div class="booking-actions">
        <button @click="addToCart" class="btn btn-outline-primary me-2">장바구니</button>
        <button @click="submitReservation" class="btn btn-yanolja">바로 예약</button>
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";
import AccommodationHeader from "@/components/accommodation/AccommodationHeader.vue";
import noImagePlaceholder from "@/assets/no-image.jpg";
import { useCartStore } from "@/store/cartStore";
import { useUserStore } from "@/store/userStore";

export default {
  name: "RoomDetail",
  components: { AccommodationHeader },
  props: {
    roomId: { type: [String, Number], required: true },
  },
  setup() {
    const cartStore = useCartStore();
    const userStore = useUserStore();
    return { cartStore, userStore };
  },
  data() {
    const queryParams = this.$route.query;
    let guestsFromQuery = 1; // 기본값 1명
    if (queryParams.guests && !isNaN(parseInt(String(queryParams.guests)))) {
      guestsFromQuery = Math.max(1, parseInt(String(queryParams.guests), 10));
    }

    return {
      loading: true,
      message: "",
      room: {},
      accommodation: {},
      roomImages: [], // room.images가 배열이 아닐 경우 대비
      selectedCheckInDateQuery: queryParams.checkIn || "",
      selectedCheckOutDateQuery: queryParams.checkOut || "",
      selectedGuestsQuery: guestsFromQuery, // 통합된 인원
      reservation: {
        roomId: this.roomId,
        checkInDate: queryParams.checkIn || "",
        checkOutDate: queryParams.checkOut || "",
        guestCount: guestsFromQuery, // 통합된 인원 사용
        totalPrice: 0,
        paymentMethod: "credit_card", // 기본값
        status: "PENDING", // 기본값
      },
      activeImageIndex: 0,
      reviews: [],
      displayReviewCount: 2,
      totalReviewCount: 0,
    };
  },
  computed: {
    isHost() {
      // 호스트 여부 판단 로직 (예시: userStore 활용)
      return this.userStore.isHost; // 실제 구현에 맞게 수정 필요
    },
    userId() {
      return this.userStore.user?.id;
    },
    imagesForCarousel() {
      if (Array.isArray(this.roomImages) && this.roomImages.length > 0) {
        return this.roomImages.map((img) => img.imageUrl || noImagePlaceholder);
      } else if (this.room.mainImageUrl) {
        return [this.room.mainImageUrl, noImagePlaceholder]; // 메인 이미지와 플레이스홀더
      } else {
        return [noImagePlaceholder, noImagePlaceholder]; // 이미지가 전혀 없을 경우
      }
    },
    showCustomCarouselControls() {
      return this.imagesForCarousel.length > 1;
    },
    displaySelectedCheckInDate() {
      return this.formatDisplayDate(this.selectedCheckInDateQuery);
    },
    displaySelectedCheckOutDate() {
      return this.formatDisplayDate(this.selectedCheckOutDateQuery);
    },
    displaySelectedGuests() {
      return `${this.selectedGuestsQuery}명`;
    },
    displayNights() {
      if (this.selectedCheckInDateQuery && this.selectedCheckOutDateQuery) {
        const checkIn = new Date(this.selectedCheckInDateQuery);
        const checkOut = new Date(this.selectedCheckOutDateQuery);
        const diffTime = Math.abs(checkOut.getTime() - checkIn.getTime());
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
        return diffDays > 0 ? diffDays : 0;
      }
      return 0;
    },
    amenitiesList() {
      if (this.room && this.room.amenities && typeof this.room.amenities === "string") {
        return this.room.amenities
          .split(",")
          .map((s) => s.trim())
          .filter(Boolean);
      }
      return []; // 기본 편의시설 대신 빈 배열 반환 또는 다른 처리
    },
    visibleReviews() {
      return this.reviews.slice(0, this.displayReviewCount);
    },
  },
  async created() {
    await this.loadRoomDetail();
    await this.loadReviews();
  },
  methods: {
    async loadRoomDetail() {
      this.loading = true;
      try {
        const response = await axios.get(`/api/accommodations/room/${this.roomId}`);
        this.room = response.data.room;
        this.accommodation = response.data.accommodation;
        // room.images가 문자열이면 파싱, 배열이면 그대로 사용, 없으면 빈 배열
        if (typeof this.room.images === "string") {
          try {
            this.roomImages = JSON.parse(this.room.images);
          } catch (e) {
            console.error("Error parsing room images string:", e);
            this.roomImages = [];
          }
        } else if (Array.isArray(this.room.images)) {
          this.roomImages = this.room.images;
        } else {
          this.roomImages = [];
        }
        // 예약 정보에 객실 가격 반영 (1박 기준)
        this.reservation.totalPrice = this.room.price;
      } catch (error) {
        console.error("Error fetching room details:", error);
        this.message = "객실 정보를 불러오는 데 실패했습니다.";
      } finally {
        this.loading = false;
      }
    },
    async loadReviews() {
      // 임시 후기 데이터 또는 API 호출
      // 실제 API에서 accommodationId 또는 roomId를 기반으로 후기를 가져와야 함
      // this.reviews = await fetchReviewsForRoom(this.roomId);
      this.reviews = [
        // ... (샘플 데이터 또는 API 연동)
      ];
      this.totalReviewCount = this.reviews.length;
    },
    prevImage() {
      this.activeImageIndex =
        (this.activeImageIndex - 1 + this.imagesForCarousel.length) % this.imagesForCarousel.length;
    },
    nextImage() {
      this.activeImageIndex = (this.activeImageIndex + 1) % this.imagesForCarousel.length;
    },
    formatPrice(price) {
      if (price === undefined || price === null) return "N/A";
      return new Intl.NumberFormat("ko-KR", { style: "currency", currency: "KRW" }).format(price);
    },
    formatDisplayDate(dateString) {
      if (!dateString) return "미선택";
      const date = new Date(dateString);
      const options = { year: "numeric", month: "long", day: "numeric", weekday: "short" };
      return date.toLocaleDateString("ko-KR", options);
    },
    async submitReservation() {
      if (!this.userStore.isAuthenticated) {
        this.$router.push({ name: "Login", query: { redirect: this.$route.fullPath } });
        return;
      }
      if (!this.selectedCheckInDateQuery || !this.selectedCheckOutDateQuery || this.selectedGuestsQuery <= 0) {
        this.message = "체크인, 체크아웃 날짜 및 인원을 모두 선택해주세요.";
        // 필요시 관련 UI로 스크롤
        return;
      }

      const reservationData = {
        ...this.reservation,
        roomId: parseInt(String(this.roomId)),
        checkInDate: this.selectedCheckInDateQuery,
        checkOutDate: this.selectedCheckOutDateQuery,
        guestCount: this.selectedGuestsQuery,
        // totalPrice는 1박 기준 가격이므로, 실제로는 (1박 가격 * 숙박일수)로 계산 필요
        totalPrice: this.room.price * this.displayNights,
        userId: this.userStore.user?.id,
      };

      try {
        const response = await axios.post("/api/reservations", reservationData);
        this.message = response.data.message || "예약이 성공적으로 완료되었습니다.";
        // 예약 완료 후 예약 내역 페이지 등으로 이동
        // this.$router.push({ name: 'MyReservations' });
      } catch (error) {
        this.message = error.response?.data?.message || "예약 처리 중 오류가 발생했습니다.";
        console.error("Error submitting reservation:", error);
      }
    },
    async addToCart() {
      if (!this.userStore.isAuthenticated) {
        this.$router.push({ name: "Login", query: { redirect: this.$route.fullPath } });
        return;
      }
      if (!this.selectedCheckInDateQuery || !this.selectedCheckOutDateQuery || this.selectedGuestsQuery <= 0) {
        this.message = "날짜와 인원을 선택해야 장바구니에 담을 수 있습니다.";
        return;
      }

      const itemDetails = {
        roomId: parseInt(String(this.roomId)),
        checkInDate: this.selectedCheckInDateQuery,
        checkOutDate: this.selectedCheckOutDateQuery,
        guestCount: this.selectedGuestsQuery,
        price: this.room.price, // 1박 가격
        // accommodationTitle과 roomName은 cartStore에서 필요하다면 추가하거나, store에서 roomId로 조회
      };

      try {
        await this.cartStore.addToCart(itemDetails);
        this.message = "객실이 장바구니에 추가되었습니다.";
      } catch (error) {
        console.error("RoomDetail - Error adding to cart:", error);
        this.message = error.response?.data?.message || error.message || "장바구니 추가 중 오류가 발생했습니다.";
      }
    },
    confirmDeleteRoom() {
      if (confirm("정말로 이 객실을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
        this.deleteRoom();
      }
    },
    async deleteRoom() {
      try {
        await axios.delete(`/api/accommodations/room/${this.roomId}`);
        this.message = "객실이 성공적으로 삭제되었습니다.";
        // 호스트의 객실 관리 페이지 또는 숙소 상세 페이지로 리디렉션
        this.$router.push({ name: "AccommodationDetail", params: { id: this.room.accommodationId } });
      } catch (error) {
        console.error("Error deleting room:", error);
        this.message = error.response?.data?.message || "객실 삭제 중 오류가 발생했습니다.";
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

/* 전체 컨테이너 너비 제한 및 중앙 정렬 */
.container {
  max-width: 768px;
  margin-left: auto;
  margin-right: auto;
  margin-bottom: 100px; /* Booking Bar로 인해 가려지는 것을 방지하기 위한 하단 여백 추가 */
}

/* Image Carousel 크기 조정 */
#roomCarousel {
  max-width: 768px;
  margin: 0 auto 20px auto; /* 기존 mb-4와 유사한 margin */
}

.carousel-inner {
  height: 507px; /* 이미지 높이와 동일하게 설정 */
  border-radius: 0.25rem; /* Bootstrap 기본 테두리 반경과 유사하게 */
  overflow: hidden; /* 이미지가 넘칠 경우를 대비 */
  position: relative; /* 자식 앱솔루트 요소(컨트롤 버튼)의 기준점 */
}

#roomCarousel .carousel-item img {
  width: 768px;
  height: 507px;
  object-fit: cover;
  display: block;
}

/* 기본 Bootstrap .carousel-item 에 active가 아닐 때 display: none이므로 */
/* 명시적인 height 설정이 필요할 수 있음. .carousel-inner로 제어 */

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

.booking-bar {
  position: fixed;
  bottom: 0;
  /* left: 0; 제거 */
  /* right: 0; 제거 */
  background-color: white;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  padding: 15px;
  z-index: 999;
  display: flex;
  justify-content: space-between;
  align-items: center;

  /* 너비 제한 및 중앙 정렬 */
  max-width: 768px;
  left: 50%;
  transform: translateX(-50%);
  width: 100%; /* max-width 내에서 100% 사용 */
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
    /* margin-bottom: 80px; */ /* 일반 스타일로 이동했으므로 여기서 제거 또는 주석 처리 */
    /* 필요한 경우 모바일 전용 추가 조정 가능 */
  }
}

/* Review Section Styles */
.reviews-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); /* 반응형 그리드 */
  gap: 20px;
  margin-bottom: 20px;
}

.review-card {
  background-color: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start; /* 닉네임/날짜를 별점 아래로 정렬하고 싶으면 center 또는 flex-start */
  margin-bottom: 8px;
}

.review-rating .bi-star-fill {
  color: #fadb14; /* 별점 색상 */
}
.review-rating .bi-star {
  color: #d9d9d9; /* 빈 별 색상 */
}

.review-nickname-date {
  text-align: right;
}

.review-nickname {
  font-size: 0.85rem;
  font-weight: 500;
  color: #555;
  display: block; /* 날짜와 줄바꿈 */
}

.review-date {
  font-size: 0.75rem;
  color: #888;
}

.review-room-name {
  font-size: 1rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.review-content {
  font-size: 0.9rem;
  color: #444;
  line-height: 1.5;
  /* 여러 줄 내용 처리를 위한 스타일 (선택적) */
  display: -webkit-box;
  -webkit-line-clamp: 4; /* 보여줄 최대 줄 수 */
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  min-height: 60px; /* 내용 길이에 따라 조절 */
}

/* Custom Carousel Controls */
.custom-carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background-color: rgba(0, 0, 0, 0.3);
  color: white;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  cursor: pointer;
  transition: background-color 0.2s ease;
  z-index: 10; /* 다른 요소 위에 오도록 */
}

.custom-carousel-control:hover {
  background-color: rgba(0, 0, 0, 0.6);
}

.custom-carousel-control.prev {
  left: 30px;
}

.custom-carousel-control.next {
  right: 30px;
}

/* 선택된 예약 정보 섹션 스타일 */
.selected-reservation-details {
  background-color: #f8f9fa; /* 연한 배경색 */
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  border: 1px solid #e9ecef;
}

.selected-reservation-details .detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  font-size: 0.95rem;
  color: #343a40;
}

.selected-reservation-details .detail-item:last-child {
  margin-bottom: 0;
}

.selected-reservation-details .detail-item i {
  margin-right: 10px;
  color: var(--yanolja-pink); /* 아이콘 색상 */
  font-size: 1.1rem;
}
</style>
