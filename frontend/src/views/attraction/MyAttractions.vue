<template>
  <section class="attractions-section">
    <h2 class="section-title">📍 나의 관광지 목록</h2>

    <div class="attractions-grid">
      <div v-for="item in attractions" :key="item.no" class="attraction-card">
        <img :src="item.image || item.image2" alt="이미지" class="attraction-image" />
        <div class="attraction-info">
          <h3 class="title">{{ item.title }}</h3>
          <p class="addr">{{ item.addr1 }} {{ item.addr2 }}</p>
          <p class="tel" v-if="item.tel">📞 {{ item.tel }}</p>
          <p class="likes">❤️ {{ item.likes }}</p>
        </div>
        <button @click="goToAttraction(item.no)">자세히 보기</button>
      </div>
    </div>

    <div ref="infiniteScrollTrigger" class="loading">
      <p v-if="loading">🔄 불러오는 중...</p>
      <p v-if="finished">✅ 더 이상 관광지가 없습니다.</p>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import api from "@/api/index";

const attractions = ref([]);
const page = ref(0);
const size = 10;
const loading = ref(false);
const finished = ref(false);
const infiniteScrollTrigger = ref(null);
const router = useRouter();

const fetchAttractions = async () => {
  if (loading.value || finished.value) return;

  loading.value = true;
  try {
    const response = await api.api({
      url: "/api/map/users/attractions",
      method: "get",
      params: {
        page: page.value,
        size,
      },
    });

    const data = response.data;

    if (data.length < size) finished.value = true;
    attractions.value.push(...data);
    page.value++;
  } catch (error) {
    console.error("Error fetching attractions:", error);
  } finally {
    loading.value = false;
  }
};

const goToAttraction = (no) => {
  router.push(`/attractions/${no}`);
};

onMounted(() => {
  const observer = new IntersectionObserver(
    ([entry]) => {
      if (entry.isIntersecting) {
        fetchAttractions();
      }
    },
    { threshold: 1.0 }
  );

  if (infiniteScrollTrigger.value) {
    observer.observe(infiniteScrollTrigger.value);
  }

  fetchAttractions(); // 최초 로드
});
</script>

<style scoped>
.attractions-section {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.section-title {
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
  font-weight: bold;
}

.attractions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 1rem;
}

.attraction-card {
  border: 1px solid #ddd;
  border-radius: 12px;
  padding: 1rem;
  background: #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
}

.attraction-image {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 0.5rem;
}

.attraction-info {
  margin-bottom: 0.5rem;
}

.title {
  font-weight: 600;
  font-size: 1.1rem;
}

.addr,
.tel,
.likes {
  font-size: 0.9rem;
  color: #555;
}

button {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 0.5rem;
  border-radius: 8px;
  width: 100%;
  cursor: pointer;
  margin-top: 0.5rem;
}

button:hover {
  background-color: #0056b3;
}

.loading {
  text-align: center;
  padding: 2rem 0;
  color: #888;
}
</style>
