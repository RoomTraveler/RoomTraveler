<template>
  <div class="plans-container">
    <div
      v-for="plan in plans"
      :key="plan.planId"
      class="plan-card"
    >
      <h3>Plan #{{ plan.planId }}</h3>

      <!-- Likes: 하트 아이콘 + 개수 -->
      <p class="likes">
        <span class="heart">❤️</span>
        {{ plan.likes }}
      </p>

      <!-- Attractions 리스트 -->
      <ul class="attractions-list">
        <li
          v-for="attraction in plan.planAttractions"
          :key="attraction.attractionId"
          class="attraction-item"
        >
          <!-- 클릭하면 /attractions/:id 로 이동 -->
          <a
            href="#"
            class="attraction-link"
            @click.prevent="goToAttraction(attraction.attractionId)"
          >
            {{ attraction.title }}
          </a>
          <!-- contentType 표시 -->
          <small class="content-type">({{ contentTypeMap[attraction.contentType] }})</small>
        </li>
      </ul>

      <!-- Plan 상세보기 버튼 -->
      <button
        class="detail-button"
        @click="goToPlan(plan.planId)"
      >
        자세히 보기
      </button>
    </div>

    <div ref="infiniteScrollTrigger" class="loading">
      <p v-if="loading">Loading more plans...</p>
      <p v-if="finished">No more plans.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()

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

const plans = ref([])
const page = ref(0)
const size = 10
const loading = ref(false)
const finished = ref(false)
const infiniteScrollTrigger = ref(null)

// 플랜 데이터 fetch
const fetchPlans = async () => {
  if (loading.value || finished.value) return

  loading.value = true
  try {
    const response = await axios.get('http://localhost:8080/api/map/users/1/plans', {
      params: { page: page.value, size }
    })
    const data = response.data

    // 불러온 항목 수가 페이지 크기보다 작으면 끝
    if (data.length < size) finished.value = true
    plans.value.push(...data)
    page.value++
  } catch (err) {
    console.error('Error fetching plans:', err)
  } finally {
    loading.value = false
  }
}

// attraction 제목 클릭 시 이동
const goToAttraction = (attractionId) => {
  router.push(`/attractions/${attractionId}`)
}

// Plan 자세히보기 버튼 클릭 시 이동
const goToPlan = (planId) => {
  router.push(`/plans/${planId}`)
}

// IntersectionObserver로 무한 스크롤 트리거
onMounted(() => {
  const observer = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting) {
        fetchPlans()
      }
    },
    { threshold: 1.0 }
  )

  if (infiniteScrollTrigger.value) {
    observer.observe(infiniteScrollTrigger.value)
  }

  // 첫 페이지 로드
  fetchPlans()
})
</script>

<style scoped>
.plans-container {
  max-width: 600px;
  margin: 0 auto;
}

.plan-card {
  padding: 1rem;
  margin-bottom: 1rem;
  border: 1px solid #ccc;
  border-radius: 8px;
}

/* Likes 스타일 */
.likes {
  display: flex;
  align-items: center;
  font-size: 1.1rem;
  margin: 0.5rem 0;
}
.heart {
  margin-right: 0.5rem;
}

/* Attractions 리스트 스타일 */
.attractions-list {
  list-style: none;
  padding: 0;
  margin: 0.5rem 0 1rem;
}
.attraction-item {
  margin-bottom: 0.25rem;
}
.attraction-link {
  text-decoration: none;
  font-weight: 500;
  cursor: pointer;
}
.attraction-link:hover {
  text-decoration: underline;
}
.content-type {
  color: #888;
  margin-left: 0.5rem;
  font-size: 0.9rem;
}

/* 자세히보기 버튼 */
.detail-button {
  display: inline-block;
  padding: 0.5rem 1rem;
  font-size: 0.95rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  background-color: #007bff;
  color: white;
}
.detail-button:hover {
  background-color: #0056b3;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #666;
}
</style>