<template>
  <Header />
  <div class="liked-attractions-container">
    <h2>❤️ 내가 좋아요 누른 관광지</h2>

    <div v-for="attraction in likedAttractions" :key="attraction.attractionLikeId" class="attraction-card">
      <h3 class="title">
        <a href="#" @click.prevent="goToAttraction(attraction.no)">
          {{ attraction.title }}
        </a>
        <small class="content-type"> ({{ contentTypeMap[attraction.contentTypeId] || "기타" }}) </small>
      </h3>

      <p class="address">{{ attraction.addr1 }} {{ attraction.addr2 }}</p>

      <div class="likes">
        <span class="heart" @click="unlikeAttraction(attraction.no)">❤️</span>
        {{ attraction.likes }}
      </div>
    </div>

    <p v-if="likedAttractions.length === 0" class="no-data">좋아요한 관광지가 없습니다.</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import axios from "axios";
import Header from "@/components/layout/Header.vue";

const router = useRouter();
const likedAttractions = ref([]);

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

// 사용자 ID (예: 1)
const userId = 1;

// 좋아요 누른 관광지 데이터 불러오기
const fetchLikedAttractions = async () => {
  try {
    const response = await axios.get(`http://localhost:8080/api/map/likes/attractions`);
    likedAttractions.value = response.data;
  } catch (error) {
    console.error("Error fetching liked attractions:", error);
  }
};

const goToAttraction = (attractionId) => {
  router.push(`/attractions/${attractionId}`);
};

const unlikeAttraction = async (attractionId) => {
  try {
    await axios.post(`http://localhost:8080/api/map/likes/attractions/${attractionId}`);
    likedAttractions.value = likedAttractions.value.filter((attraction) => attraction.no !== attractionId);
  } catch (error) {
    console.error("Failed to unlike attraction:", error);
  }
};

onMounted(() => {
  fetchLikedAttractions();
});
</script>

<style scoped>
.liked-attractions-container {
  max-width: 600px;
  margin: 2rem auto;
  padding: 1rem;
}

.attraction-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  background-color: #f9f9f9;
}

.title {
  font-size: 1.2rem;
  margin-bottom: 0.3rem;
}
.title a {
  text-decoration: none;
  font-weight: bold;
  color: #007bff;
}
.title a:hover {
  text-decoration: underline;
}
.content-type {
  margin-left: 0.5rem;
  color: #888;
  font-size: 0.9rem;
}

.address {
  color: #555;
  margin-bottom: 0.5rem;
}

.likes {
  display: flex;
  align-items: center;
  font-size: 1rem;
}
.heart {
  margin-right: 0.3rem;
}

.no-data {
  text-align: center;
  color: #888;
  font-size: 1.1rem;
  margin-top: 2rem;
}
</style>
