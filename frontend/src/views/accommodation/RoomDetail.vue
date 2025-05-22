<template>
  <div>
    <AccommodationHeader title="객실 상세 정보" />

    <div class="container mt-4">
      <div
          v-if="message"
          class="alert alert-success alert-dismissible fade show"
          role="alert"
      >
        {{ message }}
        <button
            type="button"
            class="btn-close"
            @click="message = ''"
            aria-label="Close"
        ></button>
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
        <div
            id="roomCarousel"
            class="carousel slide mb-4 position-relative"
            data-bs-touch="true"
        >
          <div class="carousel-inner">
            <div
                v-for="(imgSrc, index) in imagesForCarousel"
                :key="index"
                class="carousel-item"
                :class="{ active: index === activeImageIndex }"
            >
              <img
                  :src="imgSrc"
                  class="d-block w-100"
                  :alt="`객실 이미지 ${index + 1}`"
              />
            </div>
            <template v-if="showCustomCarouselControls">
              <button
                  class="custom-carousel-control prev"
                  @click="prevImage"
                  type="button"
              >
                <i class="bi bi-chevron-left"></i>
              </button>
              <button
                  class="custom-carousel-control next"
                  @click="nextImage"
                  type="button"
              >
                <i class="bi bi-chevron-right"></i>
              </button>
            </template>
          </div>
        </div>

        <!-- Room Info -->
        <div class="room-info">
          <div class="room-type">{{ room.roomType || '스탠다드 룸' }}</div>
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
            <div v-if="displayNights > 0" class="detail-item">
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
            <div class="info-text">
              가격: {{ formatPrice(room.price) }}/박
            </div>
          </div>

          <div v-if="isHost && userId === accommodation.hostId" class="mt-4">
            <router-link
                :to="`/accommodation/update-room/${props.roomId}`"
                class="btn btn-outline-primary me-2"
            >
              수정
            </router-link>
            <button @click="confirmDeleteRoom" class="btn btn-outline-danger">
              삭제
            </button>
          </div>
        </div>

        <hr class="my-4" />

        <!-- 후기 섹션 -->
        <div class="reviews-wrapper mt-4">
          <h3 class="section-title">후기</h3>
          <div class="reviews-container">
            <div
                v-for="review in visibleReviews"
                :key="review.id"
                class="review-card"
            >
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
            <button
                @click="displayReviewCount = totalReviewCount"
                class="btn btn-outline-secondary w-100"
            >
              {{ totalReviewCount }}개 객실후기 보기
            </button>
          </div>
          <div
              v-else-if="totalReviewCount > 2 && displayReviewCount === totalReviewCount"
              class="text-center mt-3"
          >
            <button
                @click="displayReviewCount = 2"
                class="btn btn-outline-secondary w-100"
            >
              후기 접기
            </button>
          </div>
          <div v-if="totalReviewCount === 0" class="text-center text-muted mt-3">
            <p>아직 작성된 후기가 없습니다.</p>
          </div>
        </div>

        <hr class="my-4" />

        <!-- 상세 정보 섹션 -->
        <div class="details-wrapper mt-4">
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
              체크인: {{ accommodation.checkInTime }} /
              체크아웃: {{ accommodation.checkOutTime }}
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

        <hr class="my-4" />

        <!-- 편의시설 섹션 -->
        <div class="amenities-wrapper mt-4">
          <h3 class="section-title">객실 내 시설</h3>
          <div class="amenities-list">
            <template v-if="amenitiesList.length">
              <div
                  v-for="(amenity, idx) in amenitiesList"
                  :key="idx"
                  class="amenity-item"
              >
                <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                <div>{{ amenity }}</div>
              </div>
            </template>
            <template v-else>
              <div class="amenity-item" v-for="demo in defaultAmenities" :key="demo">
                <div class="amenity-icon"><i class="bi bi-check-circle"></i></div>
                <div>{{ demo }}</div>
              </div>
            </template>
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
        <button @click="addToCart" class="btn btn-outline-primary me-2">
          장바구니
        </button>
        <button @click="submitReservation" class="btn btn-yanolja">
          바로 예약
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import AccommodationHeader from '@/components/accommodation/AccommodationHeader.vue';
import noImagePlaceholder from '@/assets/no-image.jpg';
import { useCartStore } from '@/store/cartStore';
import { useUserStore } from '@/store/userStore';

const props = defineProps({
  roomId: { type: [String, Number], required: true }
});

const router = useRouter();
const route = useRoute();
const cartStore = useCartStore();
const userStore = useUserStore();

const loading = ref(true);
const message = ref('');
const room = ref({});
const accommodation = ref({});
const roomImages = ref([]);
const activeImageIndex = ref(0);
const reviews = ref([]);
const displayReviewCount = ref(2);
const totalReviewCount = ref(0);

const selectedCheckInDateQuery = ref(String(route.query.checkIn || ''));
const selectedCheckOutDateQuery = ref(String(route.query.checkOut || ''));
const guestsFromQuery = Number(route.query.guests) > 0 ? Number(route.query.guests) : 1;
const selectedGuestsQuery = ref(guestsFromQuery);

const reservation = reactive({
  roomId: Number(props.roomId),
  checkInDate: selectedCheckInDateQuery.value,
  checkOutDate: selectedCheckOutDateQuery.value,
  guestCount: selectedGuestsQuery.value,
  totalPrice: 0,
  paymentMethod: 'credit_card',
  status: 'PENDING'
});

const defaultAmenities = [
  '무료 Wi-Fi',
  '에어컨',
  'TV',
  '미니바',
  '욕실용품',
  '헤어드라이어'
];

onMounted(async () => {
  await loadRoomDetail();
  await loadReviews();
});

async function loadRoomDetail() {
  loading.value = true;
  try {
    const { data } = await axios.get(`/api/accommodations/room/${props.roomId}`);
    room.value = data.room;
    accommodation.value = data.accommodation;

    // images
    if (Array.isArray(data.room.images)) {
      roomImages.value = data.room.images;
    } else if (typeof data.room.images === 'string') {
      try {
        roomImages.value = JSON.parse(data.room.images);
      } catch {
        roomImages.value = [];
      }
    } else {
      roomImages.value = [];
    }

    reservation.totalPrice = data.room.price;
  } catch {
    message.value = '객실 정보를 불러오는 데 실패했습니다.';
  } finally {
    loading.value = false;
  }
}

async function loadReviews() {
  // 여기에 실제 API 호출 로직 추가
  reviews.value = []; // 예시로 빈 배열
  totalReviewCount.value = reviews.value.length;
}

const isHost = computed(() => userStore.isHost);
const userId = computed(() => userStore.user?.id);

const imagesForCarousel = computed(() => {
  if (roomImages.value.length) {
    return roomImages.value.map(img => img.imageUrl || noImagePlaceholder);
  }
  if (room.value.mainImageUrl) {
    return [room.value.mainImageUrl, noImagePlaceholder];
  }
  return [noImagePlaceholder, noImagePlaceholder];
});

const showCustomCarouselControls = computed(() => imagesForCarousel.value.length > 1);

const displaySelectedCheckInDate = computed(() =>
    formatDisplayDate(selectedCheckInDateQuery.value)
);
const displaySelectedCheckOutDate = computed(() =>
    formatDisplayDate(selectedCheckOutDateQuery.value)
);
const displaySelectedGuests = computed(() => `${selectedGuestsQuery.value}명`);
const displayNights = computed(() => {
  if (selectedCheckInDateQuery.value && selectedCheckOutDateQuery.value) {
    const ci = new Date(selectedCheckInDateQuery.value);
    const co = new Date(selectedCheckOutDateQuery.value);
    const diff = co.getTime() - ci.getTime();
    return diff > 0 ? Math.ceil(diff / (1000 * 60 * 60 * 24)) : 0;
  }
  return 0;
});

const amenitiesList = computed(() => {
  if (room.value.amenities && typeof room.value.amenities === 'string') {
    return room.value.amenities
        .split(',')
        .map(s => s.trim())
        .filter(Boolean);
  }
  return [];
});

const visibleReviews = computed(() =>
    reviews.value.slice(0, displayReviewCount.value)
);

function prevImage() {
  activeImageIndex.value =
      (activeImageIndex.value - 1 + imagesForCarousel.value.length) %
      imagesForCarousel.value.length;
}

function nextImage() {
  activeImageIndex.value =
      (activeImageIndex.value + 1) % imagesForCarousel.value.length;
}

function formatPrice(p) {
  if (p == null) return 'N/A';
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW'
  }).format(p);
}

function formatDisplayDate(str) {
  if (!str) return '미선택';
  const d = new Date(str);
  return d.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'short'
  });
}

async function submitReservation() {
  if (!userStore.isAuthenticated) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } });
    return;
  }
  if (
      !selectedCheckInDateQuery.value ||
      !selectedCheckOutDateQuery.value ||
      selectedGuestsQuery.value <= 0
  ) {
    message.value = '체크인, 체크아웃 날짜 및 인원을 모두 선택해주세요.';
    return;
  }
  const dto = {
    ...reservation,
    checkInDate: selectedCheckInDateQuery.value,
    checkOutDate: selectedCheckOutDateQuery.value,
    guestCount: selectedGuestsQuery.value,
    totalPrice: room.value.price * displayNights.value,
    userId: userStore.user?.id
  };
  try {
    const res = await axios.post('/api/reservations', dto);
    message.value = res.data.message || '예약이 완료되었습니다.';
  } catch (err) {
    message.value =
        err.response?.data?.message || '예약 처리 중 오류가 발생했습니다.';
  }
}

async function addToCart() {
  if (!userStore.isAuthenticated) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } });
    return;
  }
  if (
      !selectedCheckInDateQuery.value ||
      !selectedCheckOutDateQuery.value ||
      selectedGuestsQuery.value <= 0
  ) {
    message.value = '날짜와 인원을 선택해야 장바구니에 담을 수 있습니다.';
    return;
  }
  const item = {
    roomId: Number(props.roomId),
    checkInDate: selectedCheckInDateQuery.value,
    checkOutDate: selectedCheckOutDateQuery.value,
    guestCount: selectedGuestsQuery.value,
    price: room.value.price
  };
  try {
    await cartStore.addToCart(item);
    message.value = '객실이 장바구니에 추가되었습니다.';
  } catch (err) {
    message.value =
        err.response?.data?.message || '장바구니 추가 중 오류가 발생했습니다.';
  }
}

function confirmDeleteRoom() {
  if (confirm('정말로 이 객실을 삭제하시겠습니까?')) {
    deleteRoom();
  }
}

async function deleteRoom() {
  try {
    await axios.delete(`/api/accommodations/room/${props.roomId}`);
    message.value = '객실이 삭제되었습니다.';
    router.push({
      name: 'AccommodationDetail',
      params: { id: accommodation.value.accommodationId }
    });
  } catch {
    message.value = '삭제 중 오류가 발생했습니다.';
  }
}
</script>

<style scoped>
:root {
  --yanolja-red: #f0213b;
  --yanolja-pink: #ff3478;
  --yanolja-light-gray: #f5f5fa;
  --yanolja-dark-gray: #666;
}

.container {
  max-width: 768px;
  margin: auto;
  margin-bottom: 100px;
}

#roomCarousel {
  max-width: 768px;
  margin: 0 auto 20px;
}

.carousel-inner {
  height: 507px;
  border-radius: 0.25rem;
  overflow: hidden;
  position: relative;
}

#roomCarousel .carousel-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.room-info {
  background: #fff;
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
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.info-icon {
  color: var(--yanolja-dark-gray);
  margin-right: 10px;
  font-size: 1.1rem;
  width: 24px;
  text-align: center;
}

.booking-bar {
  position: fixed;
  bottom: 0;
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  padding: 15px;
  z-index: 999;
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 768px;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
}

.booking-price {
  font-weight: bold;
  font-size: 1.2rem;
}

.booking-price-unit {
  font-size: 0.8rem;
  color: var(--yanolja-dark-gray);
}

.btn-yanolja {
  background: var(--yanolja-red);
  color: #fff;
  border: none;
}

.btn-yanolja:hover {
  background: #d01c33;
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

.selected-reservation-details {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
}

.selected-reservation-details .detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.selected-reservation-details .detail-item i {
  color: var(--yanolja-pink);
  margin-right: 10px;
}

.reviews-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.review-card {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.review-rating .bi-star-fill {
  color: #fadb14;
}

.review-rating .bi-star {
  color: #d9d9d9;
}

.review-nickname-date {
  text-align: right;
}

.review-nickname {
  font-size: 0.85rem;
  font-weight: 500;
  color: #555;
  display: block;
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
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.custom-carousel-control {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  background: rgba(0, 0, 0, 0.3);
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  cursor: pointer;
  z-index: 10;
}

.custom-carousel-control:hover {
  background: rgba(0, 0, 0, 0.6);
}

.custom-carousel-control.prev {
  left: 30px;
}

.custom-carousel-control.next {
  right: 30px;
}
</style>
