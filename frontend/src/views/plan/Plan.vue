<template>
  <Layout>
    <div class="main-banner">
      <div class="banner-wrapper" :style="{ transform: `translateX(-${currentIndex * 100}%)` }">
        <img v-for="(image, index) in images" :key="index" :src="image" class="banner-image" alt="배너 이미지" />
      </div>
    </div>

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

    <section class="popular-section">
      <h2 class="section-title">인기 있는 관광지</h2>
      <div class="card-list">
        <div class="place-card" v-for="(place, index) in popularPlaces" :key="index" @click="goToPlaceDetail(place.no)">
          <img
            :src="place.image === '' || place.image === null ? '/src/assets/no-image.jpg' : place.image"
            :alt="place.title"
            class="place-image"
          />
          <div class="place-info">
            <h3>{{ place.title }}</h3>
            <p class="address">
              {{ place.addr1 }} <span v-if="place.addr2">{{ place.addr2 }}</span>
            </p>
            <p v-if="place.tel" class="tel">📞 {{ place.tel }}</p>
            <span class="likes">❤️ {{ place.likes }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 인기 여행 플랜 섹션 -->
    <section class="popular-section">
      <div class="section-header">
        <h2 class="section-title">인기 있는 여행 플랜</h2>
        <button class="view-all-button" @click="goToAllPlans">전체 보기</button>
      </div>

      <div class="card-list">
        <div class="plan-card" v-for="(plan, index) in popularPlans" :key="index" @click="goToPlanDetail(plan.planId)">
          <img :src="plan.image" :alt="plan.title" class="plan-image" />
          <h3>{{ plan.title }}</h3>
          <p>{{ plan.summary }}</p>
          <span class="likes">❤️ {{ plan.likes }}</span>
        </div>
      </div>
    </section>

    <div class="travel-options">
      <div class="option-card solo">
        <router-link to="/plan/alone" class="router-link">
          <div class="card-image">
            <img src="/img/alone.jpg" alt="혼자 여행" />
          </div>
          <div class="card-content">
            <h2 class="card-title">혼자 여행 계획 짜기</h2>
            <p class="card-description">
              나만의 페이스로 자유롭게 여행하세요. 원하는 곳을 원하는 시간에 방문할 수 있는 완벽한 자유 여행을
              계획해보세요.
            </p>
          </div>
        </router-link>
      </div>

      <div class="option-card with-friends">
        <router-link to="/plan/room" class="router-link">
          <div class="card-image">
            <img src="/img/withFriends.jpg" alt="친구와 여행" />
          </div>
          <div class="card-content">
            <h2 class="card-title">친구랑 여행 계획 짜기</h2>
            <p class="card-description">
              친구들과 함께하는 특별한 여행을 계획해보세요. 모두가 만족할 수 있는 완벽한 일정을 만들어보세요.
            </p>
          </div>
        </router-link>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import api from "@/api/index";
import Layout from "@/components/layout/Layout.vue";
import { ref, onMounted, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { Swiper, SwiperSlide } from "swiper/vue";
import { Navigation, Pagination } from "swiper/modules";

const router = useRouter();

const images = ref(["/img/planBanner1.png", "/img/planBanner2.png"]);

// 가져와서 띄우기
const popularPlaces = ref([]);
const popularPlans = ref([]);

const currentIndex = ref(0);
let intervalId;

const homeEvents = ref([]);
const isLoadingEvents = ref(true);
const fetchEventsError = ref(null);
const swiperEventModules = [Navigation, Pagination]; // 이벤트 캐러셀용 Swiper 모듈

const fetchHomeEvents = async () => {
  isLoadingEvents.value = true;
  fetchEventsError.value = null;
  try {
    const response = await api.apiNoAuth({
      url: "/api/events",
      method: "GET",
    });
    console.log(response.data);
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

    // 최신순 정렬 후 상위 6개만
    const sortedEvents = allFetchedEvents.sort((a, b) => {
      const dateA = new Date(a.createdAt || 0);
      const dateB = new Date(b.createdAt || 0);
      return dateB - dateA;
    });
    const topEvents = sortedEvents.slice(0, 6);

    // "진행중" 이벤트만 표시
    homeEvents.value = topEvents.filter((event) => event.status === "진행중");
  } catch (error) {
    fetchEventsError.value = "이벤트 정보를 가져오는데 실패했습니다.";
    homeEvents.value = [];
  }
  isLoadingEvents.value = false;
};

onMounted(() => {
  intervalId = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % images.value.length;
  }, 5000);
  fetchHomeEvents();
  popularFiveAttractions();
  popularFivePlans();
});

const popularFiveAttractions = async () => {
  try {
    const res = await api.apiNoAuth({
      url: "/api/map/attractions?size=5",
      method: "GET",
    });
    popularPlaces.value = res.data;
  } catch (error) {
    console.error("플랜 불러오기 실패:", error);
  }
};

const popularFivePlans = async () => {
  try {
    const res = await api.apiNoAuth({
      url: "/api/map/plans?size=5",
      method: "GET",
    });
    popularPlans.value = res.data.map((plan) => {
      const attractions = plan.planAttractions || [];
      const titles = attractions.map((a) => a.title);
      const summary = titles.length > 1 ? `${titles[0]} 외 ${titles.length - 1}곳` : titles[0] || "장소 없음";

      const image = attractions[0]?.imageUrl || "/src/assets/no-image.jpg";
      const title = `여행 플랜 #${plan.planId}`; // 혹은 plan.title 사용

      return {
        ...plan,
        summary,
        image,
        title,
      };
    });
  } catch (error) {
    console.error("플랜 불러오기 실패:", error);
  }
};

function goToPlanDetail(planId) {
  router.push(`/plans/${planId}`);
}

function goToPlaceDetail(attracionId) {
  router.push(`/attractions/${attracionId}`);
}

function goToAllPlans() {
  router.push("/plans/shared");
}

onUnmounted(() => {
  clearInterval(intervalId);
});
</script>

<style scoped>
/* 메인 배너 스타일 */
.main-banner {
  position: relative;
  aspect-ratio: 16 / 9; /* 16:9 비율 */
  margin-bottom: 40px;
  border-radius: 8px;
  overflow: hidden;
}

.banner-wrapper {
  display: flex;
  transition: transform 0.5s ease-in-out;
  height: 100%;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 또는 contain */
  flex-shrink: 0;
}

.travel-options {
  display: flex;
  justify-content: space-between;
  gap: 30px;
  margin-top: 40px;
}

.option-card {
  flex: 1;
  background-color: #ffffff;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  transition:
    transform 0.3s ease,
    box-shadow 0.3s ease;
  cursor: pointer;
}

.option-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.15);
}

.card-image {
  height: 240px;
  overflow: hidden;
}

.card-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.option-card:hover .card-image img {
  transform: scale(1.05);
}

.card-content {
  padding: 30px;
  text-align: center;
}

.card-title {
  font-size: 24px;
  font-weight: 700;
  color: #2c3e50;
  margin-bottom: 16px;
}

.card-description {
  font-size: 16px;
  color: #7f8c8d;
  line-height: 1.6;
  margin-bottom: 24px;
}

.solo {
  border-top: 5px solid #3498db;
}

.with-friends {
  border-top: 5px solid #e74c3c;
}

.popular-section {
  margin-top: 60px;
}

.card-list {
  display: flex;
  flex-wrap: nowrap;
  gap: 20px;
  overflow-x: auto;
  padding-bottom: 10px;
}

.place-card,
.plan-card {
  min-width: 218px;
  width: 218px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
  transition: transform 0.3s ease;
}

.place-card:hover,
.plan-card:hover {
  transform: translateY(-8px);
}

.place-card img,
.plan-card img {
  width: 100%;
  height: 140px;
  object-fit: cover;
}

.place-card h3,
.plan-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 10px;
}

.plan-card p {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0 10px 15px;
}
.router-link {
  text-decoration: none;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0;
}

.view-all-button {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
}

.view-all-button:hover {
  background-color: #0056b3;
}

.event-carousel .swiper-slide {
  display: flex;
  justify-content: center;
}

.event-carousel .event-card-link {
  display: block;
  width: 100%;
  max-width: 460px;
  text-decoration: none;
  color: inherit;
}

.event-carousel .event-card {
  border: 1px solid #eee;
  border-radius: 8px;
  overflow: hidden;
  background-color: #fff;
  transition: box-shadow 0.2s ease-in-out;
  height: 100%;
}

.event-carousel .event-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.event-carousel .event-image {
  width: 100%;
  height: 216px;
  object-fit: contain;
  background-color: #f8f9fa;
}

.event-carousel .event-info {
  padding: 12px;
  background-color: #ffffff;
}

.event-carousel .event-title {
  font-weight: bold;
  font-size: 1rem;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.event-carousel .event-period {
  color: #666;
  font-size: 0.85rem;
}

:deep(.event-carousel .swiper-button-next),
:deep(.event-carousel .swiper-button-prev) {
  color: var(--yanolja-red);
}
:deep(.event-carousel .swiper-pagination-bullet-active) {
  background-color: var(--yanolja-red);
}

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
</style>
