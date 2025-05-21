<template>
  <section class="plans-section">
    <h2 class="section-title">&nbsp;&nbsp; 내가 만든 여행 플랜</h2>

    <div class="plans-grid">
      <div v-for="plan in plans" :key="plan.planId" class="plan-card">
        <div class="plan-header">
          <h3>플랜 #{{ plan.planId }}</h3>
          <span class="likes"> ❤️ {{ plan.likes }} </span>
        </div>

        <ul class="attractions-list">
          <li v-for="attraction in plan.planAttractions" :key="attraction.attractionId" class="attraction-item">
            <a href="#" class="attraction-link" @click.prevent="goToAttraction(attraction.attractionId)">
              {{ attraction.title }}
            </a>
            <span class="content-type">
              {{ contentTypeMap[attraction.contentType] || "기타" }}
            </span>
          </li>
        </ul>

        <button class="detail-button" @click="goToPlan(plan.planId)">✨ 자세히 보기</button>
        <button class="delete-button" @click="deletePlan(plan.planId)">🗑️ 삭제</button>
      </div>
    </div>

    <div ref="infiniteScrollTrigger" class="loading">
      <p v-if="loading">🔄 불러오는 중...</p>
      <p v-if="finished">✅ 더 이상 플랜이 없습니다.</p>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";

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

// 플랜 데이터 fetch
const fetchPlans = async () => {
  if (loading.value || finished.value) return;

  loading.value = true;
  try {
    const response = await axios.get("http://localhost:8080/api/map/users/1/plans", {
      params: { page: page.value, size },
    });
    const data = response.data;

    // 불러온 항목 수가 페이지 크기보다 작으면 끝
    if (data.length < size) finished.value = true;
    plans.value.push(...data);
    console.log(plans.value);
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

const deletePlan = async (planId) => {
  if (confirm(`플랜 #${planId}을 삭제하시겠습니까?`)) {
    try {
      await axios.delete(`http://localhost:8080/api/map/plans/${planId}`);
      plans.value = plans.value.filter((plan) => plan.planId !== planId);
      alert("삭제되었습니다.");
    } catch (error) {
      console.error("삭제 실패:", error);
      alert("삭제 중 오류가 발생했습니다.");
    }
  }
};

// IntersectionObserver로 무한 스크롤 트리거
onMounted(() => {
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

  // 첫 페이지 로드
  fetchPlans();
});
</script>

<style scoped>
.plans-section {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.section-title {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 1.5rem;
  color: #333;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 1.5rem;
}

.plan-card {
  background: #ffffff;
  border: 1px solid #ddd;
  border-radius: 12px;
  padding: 1rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  transition: transform 0.2s;
}
.plan-card:hover {
  transform: translateY(-3px);
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.8rem;
}
.likes {
  font-weight: bold;
  color: #ff4d4f;
}

.attractions-list {
  list-style: none;
  padding: 0;
  margin: 0 0 1rem;
}
.attraction-item {
  margin-bottom: 0.4rem;
}
.attraction-link {
  font-weight: 600;
  color: #007bff;
  text-decoration: none;
}
.attraction-link:hover {
  text-decoration: underline;
}
.content-type {
  background: #f0f0f0;
  font-size: 0.8rem;
  padding: 0.2rem 0.5rem;
  border-radius: 6px;
  margin-left: 0.4rem;
  color: #666;
}

.detail-button {
  width: 100%;
  padding: 0.6rem;
  font-size: 0.95rem;
  background-color: #4caf50;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.detail-button:hover {
  background-color: #45a049;
}

.delete-button {
  width: 100%;
  padding: 0.6rem;
  font-size: 0.95rem;
  background-color: #f44336;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  margin-top: 0.5rem;
}
.delete-button:hover {
  background-color: #e53935;
}

.loading {
  text-align: center;
  padding: 2rem 0;
  font-size: 0.95rem;
  color: #888;
}
</style>
