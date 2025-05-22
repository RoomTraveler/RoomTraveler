<template>
  <Layout>
    <!-- 메인 배너 영역 -->
    <div class="main-banner">
      <img src="/img/accommodationBanner.png" alt="메인 배너 이미지" class="banner-image" />
    </div>

    <!-- 숙소 카테고리 섹션 -->
    <section class="section">
      <div class="section-title">숙소 유형</div>
      <div class="category-grid">
        <router-link
          v-for="cat in categories"
          :key="cat.value"
          :to="{ name: 'AccommodationList', query: { accommodationType: cat.value } }"
          class="category-item"
        >
          <div class="category-image-container">
            <img :src="cat.img" :alt="cat.label" class="category-image" />
          </div>
          <div class="category-name">{{ cat.label }}</div>
        </router-link>
      </div>
    </section>

    <!-- 이벤트 섹션 -->
    <section class="section">
      <div class="section-title">
        진행중인 이벤트
        <router-link to="/event" class="view-all"> 전체보기 <i class="bi bi-chevron-right"></i> </router-link>
      </div>
      <div class="event-slider">
        <router-link
          v-for="event in homeEvents"
          :key="event.id"
          :to="{ name: 'EventDetail', params: { eventId: event.id } }"
          class="event-card-link"
        >
          <div class="event-card">
            <img :src="event.image" :alt="event.title" class="event-image" />
            <div class="event-info">
              <div class="event-title">{{ event.title }}</div>
              <div class="event-period">{{ event.date }}</div>
            </div>
          </div>
        </router-link>
      </div>
      <div v-if="isLoadingEvents" class="p-3 text-center text-muted">이벤트 로딩 중...</div>
      <div v-if="!isLoadingEvents && fetchEventsError" class="p-3 text-center text-danger">{{ fetchEventsError }}</div>
      <div v-if="!isLoadingEvents && !fetchEventsError && homeEvents.length === 0" class="p-3 text-center text-muted">
        진행중인 이벤트가 없습니다.
      </div>
    </section>

    <!-- 지역별 인기 숙소 섹션 -->
    <section class="section">
      <div class="section-title">
        지역별 인기 숙소
        <router-link :to="{ name: 'AccommodationList', query: { sort: 'POPULAR' } }" class="view-all">
          전체보기 <i class="bi bi-chevron-right"></i>
        </router-link>
      </div>
      <div class="region-tabs">
        <button
          v-for="region in regions"
          :key="region"
          class="region-tab"
          :class="{ active: selectedRegion === region }"
          @click="selectRegion(region)"
        >
          {{ region }}
        </button>
      </div>
      <div class="region-hotel-list">
        <div class="region-hotel-item" v-for="hotel in popularHotels" :key="hotel.title">
          <div class="hotel-image">
            <img :src="hotel.img" alt="호텔 이미지" />
          </div>
          <div class="hotel-info">
            <div class="hotel-rating">
              <i class="bi bi-star-fill"></i>
              {{ hotel.rating }} <span class="rating-count">({{ hotel.count }})</span>
            </div>
            <div class="hotel-price">{{ hotel.price }}원</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 인기 여행지 섹션 -->
    <section class="section">
      <div class="section-title">
        인기 여행지
        <router-link :to="{ name: 'AccommodationList', query: { sort: 'POPULAR' } }" class="view-all">
          전체보기 <i class="bi bi-chevron-right"></i>
        </router-link>
      </div>
      <div class="destination-grid">
        <div class="destination-card" v-for="dest in destinations" :key="dest.name">
          <img :src="dest.img" :alt="dest.name" class="destination-image" />
          <div class="destination-name">{{ dest.name }}</div>
        </div>
      </div>
    </section>

    <!-- 사용자 계정 섹션 (로그인 시에만 표시) -->
    <section v-if="isLoggedIn" class="section">
      <div class="section-title">내 계정</div>
      <div class="user-menu-grid">
        <router-link to="/user/profile" class="user-menu-item">
          <i class="bi bi-person-circle"></i>
          <span>내 정보</span>
        </router-link>
        <router-link to="/reservation/my-reservations" class="user-menu-item">
          <i class="bi bi-calendar-check"></i>
          <span>예약 내역</span>
        </router-link>
        <router-link to="/accommodation/favorites" class="user-menu-item">
          <i class="bi bi-heart"></i>
          <span>찜 목록</span>
        </router-link>
        <router-link to="/review/my-reviews" class="user-menu-item">
          <i class="bi bi-star"></i>
          <span>내 리뷰</span>
        </router-link>
        <router-link v-if="isHost || isAdmin" to="/accommodation/my-accommodations" class="user-menu-item">
          <i class="bi bi-house"></i>
          <span>내 숙소</span>
        </router-link>
        <router-link v-if="isAdmin" to="/admin/dashboard" class="user-menu-item">
          <i class="bi bi-gear"></i>
          <span>관리자</span>
        </router-link>
      </div>
    </section>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import Layout from "@/components/layout/Layout.vue";
import { useUserStore } from "@/store/userStore";
import axios from "axios";

// 유저 store
const userStore = useUserStore();

// 로그인, 권한
const isLoggedIn = computed(() => userStore.isAuthenticated);
const isAdmin = computed(() => userStore.userRole === "ADMIN");
const isHost = computed(() => userStore.userRole === "HOST");

// 숙소 카테고리
const categories = [
  { label: "모텔", value: "MOTEL", img: "/img/accommodationMotel.png" },
  { label: "호텔/리조트", value: "HOTEL", img: "/img/accommodationHotel.png" },
  { label: "펜션/풀빌라", value: "PENSION", img: "/img/accommodationPension.png" },
  { label: "프리미엄", value: "PREMIUM", img: "/img/accommodationPremium.png" },
  { label: "글램핑/캠핑", value: "CAMPING", img: "/img/accommodationCamping.png" },
];

const homeEvents = ref([]);
const isLoadingEvents = ref(true);
const fetchEventsError = ref(null);

// 진행중인 이벤트 중 일부를 가져오는 함수 (예: 최신 2개)
const fetchHomeEvents = async () => {
  isLoadingEvents.value = true;
  fetchEventsError.value = null;
  try {
    const response = await axios.get("/api/events");
    // 백엔드 EventBoard 필드명에 맞게 매핑 필요 (EventList.vue와 동일하게)
    const fetchedEvents = (response.data.result || []).map((event) => ({
      id: event.eventId,
      title: event.title,
      image: event.mainImageUrl,
      date: event.startDate && event.endDate ? `${event.startDate}~${event.endDate}` : "상시 진행",
      status:
        event.status === "ONGOING"
          ? "진행중"
          : event.status === "ENDED"
            ? "종료"
            : event.status === "HIDDEN"
              ? "숨김"
              : event.status,
    }));
    // 진행중인 이벤트만 필터링하고, 최신 3개만 선택 (또는 다른 기준으로 정렬/선택)
    homeEvents.value = fetchedEvents
      .filter((event) => event.status === "진행중") // "진행중" 상태 값은 백엔드와 일치해야 함
      .slice(0, 3); // 예시로 처음 3개만 가져옴 (최신순 정렬이 백엔드에서 된다면 더 좋음)
  } catch (error) {
    console.error("Error fetching events for home:", error);
    fetchEventsError.value = "이벤트 정보를 가져오는데 실패했습니다.";
  }
  isLoadingEvents.value = false;
};

onMounted(() => {
  fetchHomeEvents();
});

// 지역별 인기 숙소
const regions = ["서울", "부산", "제주", "강원", "경기"];
const selectedRegion = ref("서울");
const hotelsByRegion = {
  서울: [
    {
      img: "https://via.placeholder.com/200x150?text=Hotel+1",
      title: "호텔1",
      rating: "4.9",
      count: 412,
      price: "220,000",
    },
    {
      img: "https://via.placeholder.com/200x150?text=Hotel+2",
      title: "호텔2",
      rating: "4.8",
      count: 356,
      price: "180,000",
    },
    {
      img: "https://via.placeholder.com/200x150?text=Hotel+3",
      title: "호텔3",
      rating: "4.7",
      count: 289,
      price: "170,000",
    },
    {
      img: "https://via.placeholder.com/200x150?text=Hotel+4",
      title: "호텔4",
      rating: "4.9",
      count: 198,
      price: "250,000",
    },
    {
      img: "https://via.placeholder.com/200x150?text=Hotel+5",
      title: "호텔5",
      rating: "4.6",
      count: 245,
      price: "195,000",
    },
  ],
  부산: [
    {
      img: "https://via.placeholder.com/200x150?text=Hotel+1",
      title: "호텔1",
      rating: "4.7",
      count: 300,
      price: "155,000",
    },
    {
      img: "https://via.placeholder.com/200x150?text=Hotel+2",
      title: "호텔2",
      rating: "4.8",
      count: 272,
      price: "210,000",
    },
  ],
  제주: [
    {
      img: "https://via.placeholder.com/200x150?text=Hotel+1",
      title: "호텔1",
      rating: "4.8",
      count: 460,
      price: "120,000",
    },
  ],
  강원: [
    {
      img: "https://via.placeholder.com/200x150?text=Hotel+1",
      title: "호텔1",
      rating: "4.7",
      count: 133,
      price: "130,000",
    },
  ],
  경기: [
    {
      img: "https://via.placeholder.com/200x150?text=Hotel+1",
      title: "호텔1",
      rating: "4.9",
      count: 98,
      price: "110,000",
    },
  ],
};
const popularHotels = computed(() => hotelsByRegion[selectedRegion.value] || []);
function selectRegion(region) {
  selectedRegion.value = region;
}

// 인기 여행지
const destinations = [
  { img: "https://via.placeholder.com/300x200?text=Seoul", name: "서울" },
  { img: "https://via.placeholder.com/300x200?text=Busan", name: "부산" },
  { img: "https://via.placeholder.com/300x200?text=Jeju", name: "제주" },
  { img: "https://via.placeholder.com/300x200?text=Gangneung", name: "강릉" },
  { img: "https://via.placeholder.com/300x200?text=Gyeongju", name: "경주" },
];
</script>

<style scoped>
/* 메인 배너 스타일 */
.main-banner {
  position: relative;
  height: 500px;
  margin-bottom: 40px;
  border-radius: 8px;
  overflow: hidden;
  background-color: #ffffff !important; /* 이미 흰색 배경 */
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

/* Banner styles removed as they are no longer needed */

/* 카드 이미지 컨테이너 스타일 */
.card-image-container {
  position: relative;
  overflow: hidden;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.like-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(221, 221, 221, 0.8); /* 기존 배경 유지 또는 투명도 조정된 흰색으로 변경 가능 */
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.like-button:hover {
  background-color: #ffffff; /* 흰색 배경 */
  transform: scale(1.1);
}

.like-button i {
  color: #999;
  font-size: 16px;
}

.like-button:hover i {
  color: var(--yanolja-red);
}

/* 카드 내용 추가 스타일 */
.yanolja-card-rating {
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.yanolja-card-rating i {
  color: #ffb700;
  margin-right: 3px;
}

/* 카테고리 그리드 스타일 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;
  margin-bottom: 30px;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #333;
  transition: transform 0.3s ease;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.category-item:hover {
  transform: translateY(-5px);
}

.category-image-container {
  width: 100%;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 10px;
  aspect-ratio: 1 / 1;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.category-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-name {
  font-weight: bold;
  text-align: center;
  font-size: 16px;
}

/* 이벤트 슬라이더 스타일 */
.event-slider {
  display: flex;
  overflow-x: auto;
  gap: 16px;
  padding-bottom: 10px;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.event-card-link {
  text-decoration: none;
  color: inherit;
  min-width: 280px;
}

.event-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  transition: box-shadow 0.2s ease-in-out;
  height: 100%;
}

.event-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.event-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.event-info {
  padding: 15px;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.event-title {
  font-weight: bold;
  font-size: 18px;
  margin-bottom: 5px;
}

.event-period {
  color: #666;
  font-size: 14px;
}

/* 지역 탭 스타일 */
.region-tabs {
  display: flex;
  overflow-x: auto;
  margin-bottom: 20px;
  padding-bottom: 5px;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.region-tab {
  padding: 8px 20px;
  margin-right: 10px;
  background-color: #ffffff; /* 기본 배경 흰색으로 변경 */
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.region-tab.active {
  background-color: var(--yanolja-red); /* 활성 탭은 기존 색상 유지 */
  color: white;
  border-color: var(--yanolja-red);
}

.region-tab:hover:not(.active) {
  background-color: #f0f0f0; /* hover 시 약간 어두운 흰색 계열로 변경 */
}

/* 지역별 인기 숙소 인라인 스타일 */
.region-hotel-list {
  display: flex;
  overflow-x: auto;
  gap: 15px;
  padding: 5px 0;
  scrollbar-width: thin;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.region-hotel-item {
  flex: 0 0 auto;
  width: 200px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  background-color: #ffffff; /* 이미 흰색 배경 */
}

.region-hotel-item:hover {
  transform: translateY(-5px);
}

.hotel-image {
  width: 100%;
  height: 150px;
  overflow: hidden;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.hotel-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hotel-info {
  padding: 10px;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.hotel-rating {
  font-size: 14px;
  margin-bottom: 5px;
}

.hotel-rating i {
  color: #ffb700;
  margin-right: 3px;
}

.rating-count {
  color: #666;
  font-size: 12px;
}

.hotel-price {
  font-weight: bold;
  color: var(--yanolja-red);
  font-size: 16px;
}

/* 목적지 그리드 스타일 */
.destination-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.destination-card {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  cursor: pointer;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.destination-card:hover {
  transform: translateY(-5px);
}

.destination-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.destination-name {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.6); /* 텍스트 가독성을 위해 유지 */
  color: white;
  padding: 10px;
  font-weight: bold;
  text-align: center;
}

/* 사용자 메뉴 그리드 스타일 */
.user-menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.user-menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: #ffffff; /* 이미 흰색 배경 */
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  text-decoration: none;
  color: #333;
}

.user-menu-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
  color: var(--yanolja-red);
  background-color: #ffffff; /* hover 시에도 흰색 배경 유지 */
}

.user-menu-item i {
  font-size: 24px;
  margin-bottom: 10px;
}

/* 반응형 스타일 */
@media (max-width: 768px) {
  .main-banner {
    height: 300px;
  }

  .banner-image {
    height: 100%;
  }

  .category-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .event-slider {
    grid-template-columns: 1fr;
  }

  .region-tabs {
    padding-bottom: 10px;
  }

  .region-tab {
    padding: 6px 15px;
    font-size: 13px;
  }

  .region-hotel-list {
    gap: 10px;
  }

  .region-hotel-item {
    width: 160px;
  }

  .hotel-image {
    height: 120px;
  }
}

/* 기존 스타일 유지 */
:root {
  --yanolja-red: #f0213b;
  --yanolja-pink: #ff3478;
  --yanolja-light-gray: #f5f5f5;
  --yanolja-dark-gray: #666;
}

body {
  font-family: "Noto Sans KR", sans-serif;
  color: #333;
  background-color: #ffffff !important; /* 이미 흰색 배경 */
}

.card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  background-color: #ffffff; /* 흰색 배경 추가 또는 확인 */
  transition:
    transform 0.3s ease-in-out,
    box-shadow 0.3s ease-in-out;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-body {
  padding: 15px;
  background-color: #ffffff; /* 흰색 배경 추가 */
}

.card-title {
  font-weight: bold;
  margin-bottom: 5px;
  font-size: 1.1rem;
}

.card-text {
  font-size: 0.9rem;
  color: var(--yanolja-dark-gray);
  margin-bottom: 10px;
}

.btn {
  margin-top: 10px;
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

.section {
  background-color: #ffffff !important; /* 이미 흰색 배경 */
}

.section-title {
  font-weight: bold;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading {
  text-align: center;
  padding: 20px;
  background-color: #ffffff; /* 흰색 배경 추가 */
}
</style>
