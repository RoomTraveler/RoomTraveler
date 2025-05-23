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
      <div v-if="isLoadingEvents" class="p-3 text-center text-muted">이벤트 로딩 중...</div>
      <div v-if="!isLoadingEvents && fetchEventsError" class="p-3 text-center text-danger">{{ fetchEventsError }}</div>
      <div v-if="!isLoadingEvents && !fetchEventsError && homeEvents.length === 0" class="p-3 text-center text-muted">
        진행중인 이벤트가 없습니다.
      </div>
      <swiper
        v-if="!isLoadingEvents && !fetchEventsError && homeEvents.length > 0"
        :modules="swiperEventModules"
        :slides-per-view="2"
        :space-between="15"
        navigation
        :pagination="{ clickable: true }"
        :breakpoints="{
          320: { slidesPerView: 1, spaceBetween: 10 },
          768: { slidesPerView: 2, spaceBetween: 15 },
          1024: { slidesPerView: 2, spaceBetween: 15 },
        }"
        class="event-carousel"
      >
        <swiper-slide v-for="event in homeEvents" :key="event.id">
          <router-link :to="{ name: 'EventDetail', params: { eventId: event.id } }" class="event-card-link">
            <div class="event-card">
              <img :src="event.image" :alt="event.title" class="event-image" />
              <div class="event-info">
                <div class="event-title">{{ event.title }}</div>
                <div class="event-period">{{ event.date }}</div>
              </div>
            </div>
          </router-link>
        </swiper-slide>
      </swiper>
    </section>

    <!-- 인기 추천 숙소 섹션 -->
    <section class="section">
      <div class="section-title">
        인기 추천 숙소
        <!-- 전체보기 링크는 특정 유형으로 한정하기 어려우므로, 일단 AccommodationList의 인기순으로 연결 -->
        <router-link :to="{ name: 'AccommodationList', query: { sort: 'POPULAR' } }" class="view-all">
          전체보기 <i class="bi bi-chevron-right"></i>
        </router-link>
      </div>

      <!-- 숙소 유형 탭 버튼 -->
      <div class="accommodation-type-tabs">
        <button
          v-for="tab in accommodationTypeTabs"
          :key="tab.code"
          class="type-tab"
          :class="{ active: selectedAccommodationTypeCode === tab.code }"
          @click="selectAccommodationType(tab.code)"
        >
          {{ tab.name }}
        </button>
      </div>

      <!-- 선택된 유형의 숙소 목록 캐러셀 -->
      <div v-if="currentPopularAccommodations">
        <div v-if="currentPopularAccommodations.isLoading" class="p-3 text-center text-muted">
          {{ currentPopularAccommodations.name }} 숙소 로딩 중...
        </div>
        <div
          v-if="!currentPopularAccommodations.isLoading && currentPopularAccommodations.error"
          class="p-3 text-center text-danger"
        >
          {{ currentPopularAccommodations.error }}
        </div>
        <div
          v-if="
            !currentPopularAccommodations.isLoading &&
            !currentPopularAccommodations.error &&
            currentPopularAccommodations.accommodations.length === 0
          "
          class="p-3 text-center text-muted"
        >
          해당 유형의 인기 숙소 정보가 없습니다.
        </div>
        <swiper
          v-if="
            !currentPopularAccommodations.isLoading &&
            !currentPopularAccommodations.error &&
            currentPopularAccommodations.accommodations.length > 0
          "
          :modules="swiperAccommodationModules"
          :slides-per-view="4"
          :space-between="15"
          navigation
          :pagination="{ clickable: true }"
          :breakpoints="{
            320: { slidesPerView: 1, spaceBetween: 10 },
            640: { slidesPerView: 2, spaceBetween: 10 },
            768: { slidesPerView: 3, spaceBetween: 15 },
            1024: { slidesPerView: 4, spaceBetween: 15 },
          }"
          :key="selectedAccommodationTypeCode"
          class="accommodation-carousel mt-3"
        >
          <swiper-slide
            v-for="hotelStat in currentPopularAccommodations.accommodations"
            :key="hotelStat.accommodationId"
          >
            <router-link
              :to="{ name: 'AccommodationDetail', params: { accommodationId: hotelStat.accommodationId } }"
              class="region-hotel-item"
            >
              <div class="hotel-image">
                <img
                  :src="hotelStat.accommodationMainImageUrl || 'https://via.placeholder.com/200x150?text=Hotel'"
                  :alt="hotelStat.accommodationTitle || '호텔 이미지'"
                />
              </div>
              <div class="hotel-info">
                <div class="hotel-title">
                  {{ hotelStat.accommodationTitle || "숙소 ID: " + hotelStat.accommodationId }}
                </div>
                <div class="hotel-rating">
                  <i class="bi bi-star-fill"></i>
                  {{ hotelStat.popularityScore ? hotelStat.popularityScore.toFixed(1) : "N/A" }}
                  <span class="rating-count">({{ hotelStat.reviewCount || 0 }})</span>
                </div>
                <div class="hotel-price">가격 정보 필요</div>
              </div>
            </router-link>
          </swiper-slide>
        </swiper>
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
      <div v-if="isLoadingDestinations" class="p-3 text-center text-muted">인기 여행지 로딩 중...</div>
      <div v-if="!isLoadingDestinations && fetchDestinationsError" class="p-3 text-center text-danger">
        {{ fetchDestinationsError }}
      </div>
      <div
        v-if="!isLoadingDestinations && !fetchDestinationsError && destinations.length === 0"
        class="p-3 text-center text-muted"
      >
        인기 여행지 정보가 없습니다.
      </div>
      <swiper
        v-if="!isLoadingDestinations && !fetchDestinationsError && destinations.length > 0"
        :modules="swiperDestinationModules"
        :slides-per-view="4"
        :space-between="20"
        navigation
        :pagination="{ clickable: true }"
        :breakpoints="{
          320: { slidesPerView: 1, spaceBetween: 10 },
          576: { slidesPerView: 2, spaceBetween: 15 },
          768: { slidesPerView: 3, spaceBetween: 20 },
          1024: { slidesPerView: 4, spaceBetween: 20 },
        }"
        class="destination-carousel"
      >
        <swiper-slide v-for="dest in destinations" :key="dest.code">
          <router-link
            :to="{ name: 'AccommodationList', query: { sidoCode: dest.code, regionName: dest.name, sort: 'POPULAR' } }"
            class="destination-card"
          >
            <img
              :src="dest.sidoImgUrl || 'https://via.placeholder.com/300x200?text=' + dest.name"
              :alt="dest.name"
              class="destination-image"
            />
            <div class="destination-name">{{ dest.name }}</div>
          </router-link>
        </swiper-slide>
      </swiper>
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
import { ref, computed, onMounted, watch } from "vue";
import Layout from "@/components/layout/Layout.vue";
import { useUserStore } from "@/store/userStore";
import axios from "axios";
import { format, subDays } from "date-fns";

// Swiper imports
import { Swiper, SwiperSlide } from "swiper/vue";
// import "swiper/css"; // 전역 등록으로 인해 제거
// import "swiper/css/navigation"; // 전역 등록으로 인해 제거
// import "swiper/css/pagination"; // 전역 등록으로 인해 제거
import { Navigation, Pagination } from "swiper/modules";

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
const swiperEventModules = [Navigation, Pagination]; // 이벤트 캐러셀용 Swiper 모듈

// 진행중인 이벤트 중 일부를 가져오는 함수
const fetchHomeEvents = async () => {
  isLoadingEvents.value = true;
  fetchEventsError.value = null;
  console.log("[AccommodationHome.vue] Fetching home events...");
  try {
    const response = await axios.get("/api/events");
    console.log("[AccommodationHome.vue] API response for events:", response.data);

    const allFetchedEvents = (response.data.result || response.data.data || response.data || []).map((event) => {
      const eventStatus =
        event.status === "ONGOING"
          ? "진행중"
          : event.status === "ENDED"
            ? "종료"
            : event.status === "HIDDEN"
              ? "숨김"
              : event.status;
      const eventDate = event.startDate && event.endDate ? `${event.startDate}~${event.endDate}` : "상시 진행";

      return {
        id: event.eventId,
        title: event.title,
        image: event.mainImageUrl || "https://via.placeholder.com/280x200?text=Event",
        date: eventDate,
        status: eventStatus,
        createdAt: event.createdAt,
      };
    });
    console.log(
      "[AccommodationHome.vue] All fetched events (before sort/slice with mapped status/date). 각 이벤트의 ID 값 확인 (event.id -> router-link에서 eventId로 사용됨):",
      allFetchedEvents.map((e) => ({ id: e.id, title: e.title }))
    );

    const sortedEvents = allFetchedEvents.sort((a, b) => {
      const dateA = new Date(a.createdAt || 0);
      const dateB = new Date(b.createdAt || 0);
      return dateB - dateA;
    });

    const topEvents = sortedEvents.slice(0, 6);
    console.log("[AccommodationHome.vue] Top 6 sorted events:", JSON.parse(JSON.stringify(topEvents)));

    homeEvents.value = topEvents.filter((event) => event.status === "진행중");
    console.log(
      "[AccommodationHome.vue] Filtered '진행중' events for home:",
      JSON.parse(JSON.stringify(homeEvents.value))
    );

    if (homeEvents.value.length === 0) {
      console.log("[AccommodationHome.vue] No '진행중' events found after filtering.");
    }
  } catch (error) {
    console.error("[AccommodationHome.vue] Error fetching events for home:", error);
    fetchEventsError.value = "이벤트 정보를 가져오는데 실패했습니다.";
    homeEvents.value = [];
  }
  isLoadingEvents.value = false;
};

onMounted(() => {
  fetchHomeEvents();
});

// 인기 추천 숙소
const accommodationTypeTabs = ref([
  { code: "ALL", name: "전체", accommodations: [], isLoading: true, error: null, fetched: false },
  { code: "MOTEL", name: "모텔", accommodations: [], isLoading: true, error: null, fetched: false },
  { code: "HOTEL", name: "호텔 & 리조트", accommodations: [], isLoading: true, error: null, fetched: false },
  { code: "PENSION", name: "펜션 & 풀빌라", accommodations: [], isLoading: true, error: null, fetched: false },
  { code: "PREMIUM", name: "프리미엄", accommodations: [], isLoading: true, error: null, fetched: false }, // 백엔드 지원 확인 필요
  { code: "CAMPING", name: "글램핑 & 캠핑", accommodations: [], isLoading: true, error: null, fetched: false }, // 백엔드 지원 확인 필요
]);
const selectedAccommodationTypeCode = ref(null);
const swiperAccommodationModules = [Navigation, Pagination];

// 선택된 숙소 유형의 데이터를 반환하는 computed property
const currentPopularAccommodations = computed(() => {
  if (!selectedAccommodationTypeCode.value) return null;
  return accommodationTypeTabs.value.find((tab) => tab.code === selectedAccommodationTypeCode.value);
});

// 특정 숙소 유형의 인기 숙소 통계 가져오기
const fetchPopularAccommodationsByType = async (typeCode) => {
  const typeConfig = accommodationTypeTabs.value.find((tab) => tab.code === typeCode);
  if (!typeConfig || typeConfig.fetched) return;

  typeConfig.isLoading = true;
  typeConfig.error = null;
  try {
    const yesterday = format(subDays(new Date(), 1), "yyyy-MM-dd");
    const params = {
      statsType: "ACCOMMODATION",
      periodType: "DAILY",
      periodValue: yesterday,
      orderBy: "popularity_score DESC",
      limit: 10,
    };
    // "전체" 탭이 아닐 경우에만 accommodationType 파라미터 추가
    if (typeConfig.code !== "ALL") {
      params.accommodationType = typeConfig.code;
    }
    // "프리미엄", "글램핑/캠핑" 등 새로운 유형은 백엔드에서 해당 accommodationType 값으로 필터링을 지원해야 합니다.

    const response = await axios.get("/api/stats/popularity", { params });
    typeConfig.accommodations = (response.data || []).map((acc) => ({ ...acc }));
    typeConfig.fetched = true;
  } catch (error) {
    console.error(`Error fetching popular accommodations for type ${typeConfig.code}:`, error);
    typeConfig.error = "인기 숙소 정보를 가져오는데 실패했습니다.";
    typeConfig.accommodations = [];
  }
  typeConfig.isLoading = false;
};

// 탭 선택 함수
const selectAccommodationType = (typeCode) => {
  selectedAccommodationTypeCode.value = typeCode;
  fetchPopularAccommodationsByType(typeCode); // 선택된 탭의 데이터 로드 (아직 로드 안됐으면)
};

// 인기 여행지
const destinations = ref([]); // API로부터 인기 여행지(시도) 목록을 받아올 ref
const isLoadingDestinations = ref(true);
const fetchDestinationsError = ref(null);
const swiperDestinationModules = [Navigation, Pagination]; // Swiper 모듈

const fetchPopularDestinations = async () => {
  isLoadingDestinations.value = true;
  fetchDestinationsError.value = null;
  try {
    const yesterday = format(subDays(new Date(), 1), "yyyy-MM-dd");
    const popularityParams = {
      statsType: "REGION",
      regionType: "SIDO",
      periodType: "DAILY",
      periodValue: yesterday,
      orderBy: "popularity_score DESC",
      limit: 8,
    };
    const popularityResponse = await axios.get("/api/stats/popularity", { params: popularityParams });
    destinations.value = (popularityResponse.data || []).map((stat) => ({
      code: stat.regionCode,
      name: stat.sidoName || `지역코드 ${stat.regionCode}`,
      sidoImgUrl: stat.sidoImgUrl || "",
    }));
  } catch (error) {
    console.error("Error fetching popular destinations:", error);
    fetchDestinationsError.value = "인기 여행지 정보를 가져오는데 실패했습니다.";
    destinations.value = [];
  }
  isLoadingDestinations.value = false;
};

onMounted(() => {
  fetchHomeEvents();
  if (accommodationTypeTabs.value.length > 0) {
    // 첫 번째 탭을 기본으로 선택하고 데이터 로드
    selectAccommodationType(accommodationTypeTabs.value[0].code);
  }
  fetchPopularDestinations();
});
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

/* 이벤트 슬라이더 스타일 -> 이벤트 캐러셀 스타일 */
.event-carousel .swiper-slide {
  display: flex;
  justify-content: center;
}

.event-carousel .event-card-link {
  display: block;
  width: 100%; /* 슬라이드 너비에 맞게 조정 */
  max-width: 460px; /* 카드 최대 너비 늘림 (기존 300px) */
  text-decoration: none;
  color: inherit;
}

.event-carousel .event-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  transition: box-shadow 0.2s ease-in-out;
  height: 100%; /* 슬라이드 높이에 맞게 카드 높이 조정 */
}

.event-carousel .event-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.event-carousel .event-image {
  width: 100%;
  height: 216px; /* 이미지 높이 유지 또는 조절 */
  object-fit: contain; /* cover -> contain 으로 변경 */
  background-color: #f8f9fa; /* 이미지가 컨테이너보다 작을 경우 배경색 */
}

.event-carousel .event-info {
  padding: 12px;
  background-color: #ffffff;
}

.event-carousel .event-title {
  font-weight: bold;
  font-size: 1rem; /* 폰트 크기 조정 */
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-carousel .event-period {
  color: #666;
  font-size: 0.85rem; /* 폰트 크기 조정 */
}

/* Swiper Navigation/Pagination 버튼 색상 (다른 캐러셀과 일관성 유지) */
:deep(.event-carousel .swiper-button-next),
:deep(.event-carousel .swiper-button-prev) {
  color: var(--yanolja-red);
}

:deep(.event-carousel .swiper-pagination-bullet-active) {
  background-color: var(--yanolja-red);
}

/* 지역별 인기 숙소 -> 인기 추천 숙소 공통 캐러셀 스타일 */
.accommodation-carousel .swiper-slide {
  display: flex;
  justify-content: center;
}

.accommodation-carousel .region-hotel-item {
  width: 100%;
  max-width: 220px; /* 아이템 최대 너비 설정 (선택적) */
  display: block;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  background-color: #fff;
  margin-bottom: 5px; /* 슬라이드 간 약간의 마진 */
}

.accommodation-carousel .region-hotel-item:hover {
  transform: translateY(-5px);
}

.accommodation-carousel .hotel-image {
  width: 100%;
  height: 150px;
  overflow: hidden;
  background-color: #f0f0f0;
}
.accommodation-carousel .hotel-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.accommodation-carousel .hotel-info {
  padding: 10px;
}
.accommodation-carousel .hotel-title {
  font-weight: bold;
  font-size: 1rem;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.accommodation-carousel .hotel-rating {
  font-size: 0.85rem;
  margin-bottom: 4px;
}
.accommodation-carousel .hotel-rating i {
  color: #ffb700;
}
.accommodation-carousel .rating-count {
  color: #666;
  font-size: 0.75rem;
}
.accommodation-carousel .hotel-price {
  font-weight: bold;
  color: var(--yanolja-red);
  font-size: 0.9rem;
}

/* Swiper Navigation/Pagination 버튼 색상 (인기 여행지와 동일하게 적용) */
:deep(.accommodation-carousel .swiper-button-next),
:deep(.accommodation-carousel .swiper-button-prev) {
  color: var(--yanolja-red);
}

:deep(.accommodation-carousel .swiper-pagination-bullet-active) {
  background-color: var(--yanolja-red);
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
  background-color: #f0f0f0; /* 이미지 로딩 전 배경색 */
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

  /* .banner-image 삭제 (위에서 이미 처리됨) */

  .category-grid {
    grid-template-columns: repeat(3, 1fr); /* 모바일에서는 3개씩 */
    gap: 10px;
  }
  .category-item {
    font-size: 14px; /* 모바일에서 카테고리 이름 폰트 약간 줄임 */
  }
  .category-name {
    font-size: 14px;
  }

  .event-carousel {
    /* 슬라이더는 가로 스크롤 유지 */
  }
  .event-card-link {
    min-width: 240px; /* 모바일에서 카드 너비 약간 줄임 */
  }

  .destination-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); /* 모바일에서 너비 줄임 */
  }
  .destination-image {
    height: 120px;
  }

  .user-menu-grid {
    grid-template-columns: repeat(2, 1fr); /* 모바일에서 2개씩 */
  }
  .user-menu-item span {
    font-size: 14px;
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
  /* 범용 카드 스타일 - 이벤트 카드 등 다른 곳에서 이미 사용 중일 수 있음 */
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
  background-color: #ffffff;
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
  background-color: #ffffff;
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

/* .btn, .btn-yanolja 는 이미 잘 정의되어 있음 */

.section {
  padding: 30px 0; /* 섹션 상하 패딩 추가 */
  background-color: #ffffff !important;
}

.section-title {
  font-size: 22px; /* 섹션 타이틀 크기 조정 */
  font-weight: bold;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.section-title .view-all {
  /* 전체보기 링크 스타일 */
  font-size: 14px;
  color: var(--yanolja-dark-gray);
  text-decoration: none;
  font-weight: normal;
}
.section-title .view-all:hover {
  text-decoration: underline;
}
.section-title .view-all i {
  font-size: 12px;
  vertical-align: middle;
}

.loading {
  /* 범용 로딩 스타일 */
  text-align: center;
  padding: 20px;
  background-color: #ffffff;
}

/* Swiper 캐러셀 스타일 추가 */
.destination-carousel .swiper-slide {
  display: flex;
  justify-content: center;
}

.destination-carousel .destination-card {
  width: 100%; /* 슬라이드 너비에 맞춤 */
  display: block; /* 링크 전체 클릭 가능하도록 */
}

/* Swiper Navigation 버튼 색상 (필요시 추가) */
:deep(.swiper-button-next),
:deep(.swiper-button-prev) {
  color: var(--yanolja-red); /* Your preferred color */
}

:deep(.swiper-pagination-bullet-active) {
  background-color: var(--yanolja-red);
}

.accommodation-type-tabs {
  display: flex;
  overflow-x: auto;
  margin-bottom: 20px;
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
}

.type-tab {
  padding: 8px 18px;
  margin-right: 10px;
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 20px; /* 더 둥글게 */
  font-size: 0.9rem;
  font-weight: 500;
  color: #555;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s ease-in-out;
}

.type-tab.active {
  background-color: var(--yanolja-red);
  color: white;
  border-color: var(--yanolja-red);
  font-weight: 600;
}

.type-tab:hover:not(.active) {
  background-color: #f8f9fa;
  border-color: #ccc;
  color: #333;
}
</style>
