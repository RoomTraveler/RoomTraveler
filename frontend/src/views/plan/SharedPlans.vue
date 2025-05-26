<template>
  <Header />
  <div class="map-container">
    <!-- 왼쪽 사이드바 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>여행 계획</h2>
      </div>

      <div class="plans-list">
        <div
          v-for="plan in plans"
          :key="plan.planId"
          class="plan-card"
          :class="{ active: selectedPlanId === plan.planId }"
          @click="selectPlan(plan)"
        >
          <h3>Plan #{{ plan.planId }}</h3>

          <!-- Likes: 하트 아이콘 + 개수 -->
          <p class="likes">
            <span @click.stop="toggleLike(plan.planId)" class="heart">{{ liked[plan.planId] ? "❤️" : "🤍" }}</span>
            {{ likeCount[plan.planId] }}
          </p>

          <!-- Attractions 리스트 -->
          <ul class="attractions-list">
            <li
              v-for="(attraction, index) in plan.planAttractions"
              :key="attraction.attractionId"
              class="attraction-item"
            >
              <span class="attraction-number">{{ index + 1 }}</span>
              <div class="attraction-info">
                {{ attraction.title }}
                <small class="content-type">({{ contentTypeMap[attraction.contentType] }})</small>
              </div>
            </li>
          </ul>

          <!-- Plan 상세보기 버튼 -->
          <button class="detail-button" @click.stop="goToPlan(plan.planId)">자세히 보기</button>
        </div>

        <div ref="infiniteScrollTrigger" class="loading">
          <p v-if="loading">Loading more plans...</p>
          <p v-if="finished">No more plans.</p>
        </div>
      </div>
    </div>

    <!-- 카카오맵 -->
    <div class="map-wrapper">
      <div id="kakao-map" ref="mapContainer"></div>

      <!-- 선택된 플랜 정보 오버레이 -->
      <div v-if="selectedPlan" class="map-overlay">
        <div class="overlay-content">
          <h4>Plan #{{ selectedPlan.planId }}</h4>
          <p>{{ selectedPlan.planAttractions.length }}개 장소</p>
          <button @click="clearSelection" class="clear-btn">선택 해제</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from "vue";
import { useRouter } from "vue-router";
import apiGroup from "@/api/index";
import Header from "@/components/layout/Header.vue";

const router = useRouter();

const contentTypeMap = {
  12: "관광지",
  14: "문화시설",
  15: "축제공연행사",
  25: "여행코스",
  28: "레포츠",
  32: "숙박",
  38: "쇼핑",
  39: "음식점",
};

const plans = ref([]);
const page = ref(0);
const size = 10;
const loading = ref(false);
const finished = ref(false);
const infiniteScrollTrigger = ref(null);
const mapContainer = ref(null);

const map = ref(null);
const markers = ref([]);
const selectedPlan = ref(null);
const selectedPlanId = ref(null);

const initKakaoMap = async () => {
  // 스크립트 로드 여부 확인
  const loadKakaoMapScript = () => {
    return new Promise((resolve) => {
      if (window.kakao && window.kakao.maps) {
        return resolve();
      }

      const script = document.createElement("script");
      script.src = "https://dapi.kakao.com/v2/maps/sdk.js?appkey=a1b7d43f74e8d7c4fa60d02ce2c13f58&autoload=false";
      script.onload = () => {
        window.kakao.maps.load(() => {
          resolve();
        });
      };
      document.head.appendChild(script);
    });
  };

  await loadKakaoMapScript();

  if (!mapContainer.value) {
    console.warn("mapContainer가 아직 준비되지 않았습니다.");
    return;
  }

  const options = {
    center: new window.kakao.maps.LatLng(37.5665, 126.978),
    level: 5,
  };

  map.value = new window.kakao.maps.Map(mapContainer.value, options);
};

const clearMarkers = () => {
  markers.value.forEach((marker) => marker.setMap(null));
  markers.value = [];
};

const selectPlan = async (plan) => {
  console.log(plan);
  selectedPlan.value = plan;
  selectedPlanId.value = plan.planId;

  clearMarkers();

  if (!plan.planAttractions || plan.planAttractions.length === 0) {
    return;
  }

  const bounds = new kakao.maps.LatLngBounds();

  for (let i = 0; i < plan.planAttractions.length; i++) {
    const attraction = plan.planAttractions[i];

    const lat = plan.planAttractions[i].latitude;
    const lng = plan.planAttractions[i].longitude;

    const position = new kakao.maps.LatLng(lat, lng);

    // 커스텀 마커 (순서 번호 포함)
    const markerContent = `
      <div style="
        background: #ff6b6b;
        color: white;
        border-radius: 50%;
        width: 30px;
        height: 30px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: bold;
        font-size: 14px;
        border: 2px solid white;
        box-shadow: 0 2px 6px rgba(0,0,0,0.3);
      ">
        ${i + 1}
      </div>
    `;

    const customOverlay = new kakao.maps.CustomOverlay({
      position: position,
      content: markerContent,
      yAnchor: 0.5,
    });

    customOverlay.setMap(map.value);
    markers.value.push(customOverlay);

    // 인포윈도우
    const infoWindow = new kakao.maps.InfoWindow({
      content: `
        <div style="padding: 8px; min-width: 150px;">
          <strong>${attraction.title}</strong><br>
          <small>${contentTypeMap[attraction.contentType]}</small>
        </div>
      `,
    });

    bounds.extend(position);
  }

  map.value.setBounds(bounds);
};

const clearSelection = () => {
  selectedPlan.value = null;
  selectedPlanId.value = null;
  clearMarkers();
};

const fetchPlans = async () => {
  if (loading.value || finished.value) return;

  loading.value = true;
  try {
    const response = await apiGroup.api({
      url: "/api/map/plans",
      method: "get",
      params: { page: page.value, size },
    });
    const data = response.data;

    if (data.length < size) finished.value = true;
    data.forEach((plan) => {
      likeCount.value[plan.planId] = plan.likes;
      liked.value[plan.planId] = plan.likedByUser;
    });
    console.log(likeCount.value);
    console.log(liked.value);
    plans.value.push(...data);
    page.value++;
  } catch (err) {
    console.error("Error fetching plans:", err);
  } finally {
    loading.value = false;
  }
};

// attraction 제목 클릭 시 이동
const goToAttraction = (attractionId) => {
  router.push(`/attractions/${attractionId}`);
};

// Plan 자세히보기 버튼 클릭 시 이동
const goToPlan = (planId) => {
  router.push(`/plans/${planId}`);
};

onMounted(async () => {
  await nextTick();
  initKakaoMap();

  const observer = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting) {
        fetchPlans();
      }
    },
    { threshold: 1.0 }
  );

  if (infiniteScrollTrigger.value) {
    observer.observe(infiniteScrollTrigger.value);
  }

  fetchPlans();
});

const likeCount = ref({});
const liked = ref({});

const toggleLike = async (planId) => {
  try {
    const response = await apiGroup.api({
      url: `api/map/likes/plans/${planId}`,
      method: "POST",
    });
    if (response.status === 200) {
      liked.value[planId] = !liked.value[planId];
      likeCount.value[planId] += liked.value[planId] ? 1 : -1;
    }
  } catch (error) {
    console.error("좋아요 토글 실패:", error);
  }
};
</script>

<style scoped>
.map-container {
  display: flex;
  height: 94vh;
  width: 100%;
}

.sidebar {
  width: 400px;
  background: white;
  border-right: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  z-index: 1000;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
  background: #f8f9fa;
}

.sidebar-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.5rem;
}

.plans-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.plan-card {
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.plan-card:hover {
  border-color: #007bff;
  box-shadow: 0 2px 8px rgba(0, 123, 255, 0.1);
}

.plan-card.active {
  border-color: #007bff;
  background: #f0f8ff;
  box-shadow: 0 2px 8px rgba(0, 123, 255, 0.2);
}

.plan-card h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1.1rem;
}

.likes {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 0.9rem;
}

.heart {
  margin-right: 4px;
}

.attractions-list {
  list-style: none;
  padding: 0;
  margin: 0 0 12px 0;
}

.attraction-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
  padding: 6px 0;
}

.attraction-number {
  background: #007bff;
  color: white;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  font-weight: bold;
  margin-right: 8px;
  flex-shrink: 0;
}

.attraction-info {
  flex: 1;
}

.attraction-link {
  text-decoration: none;
  color: #333;
  font-weight: 500;
  display: block;
  margin-bottom: 2px;
}

.attraction-link:hover {
  color: #007bff;
}

.content-type {
  color: #666;
  font-size: 0.8rem;
}

.detail-button {
  background: #007bff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
  width: 100%;
  transition: background-color 0.2s;
}

.detail-button:hover {
  background: #0056b3;
}

.loading {
  text-align: center;
  padding: 20px;
  color: #666;
}

.map-wrapper {
  flex: 1;
  position: relative;
}

#kakao-map {
  width: 100%;
  height: 100%;
}

.map-overlay {
  position: absolute;
  top: 20px;
  right: 20px;
  background: white;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  z-index: 1000;
}

.overlay-content h4 {
  margin: 0 0 8px 0;
  color: #333;
}

.overlay-content p {
  margin: 0 0 12px 0;
  color: #666;
  font-size: 0.9rem;
}

.clear-btn {
  background: #dc3545;
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
}

.clear-btn:hover {
  background: #c82333;
}

/* 스크롤바 스타일링 */
.plans-list::-webkit-scrollbar {
  width: 6px;
}

.plans-list::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.plans-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.plans-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .map-container {
    flex-direction: column;
  }

  .sidebar {
    width: 100%;
    height: 40vh;
  }

  .map-wrapper {
    height: 60vh;
  }
}
</style>
