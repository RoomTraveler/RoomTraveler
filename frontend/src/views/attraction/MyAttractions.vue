<template>
  <Header />
  <div class="liked-attractions-container">
    <h2>❤️ 내가 좋아요 누른 관광지</h2>

    <div class="attractions-grid">
      <div v-for="attraction in likedAttractions" :key="attraction.attractionLikeId" class="attraction-card">
        <!-- 이미지 영역 (샘플 이미지 사용) -->
        <div class="card-image">
          <div class="image-placeholder">
            <div v-if="attraction.image2">
              <img :src="attraction.image2"/>
            </div>
            <div v-else>
              <span class="image-icon">🏞️</span>
            </div>
          </div>
          <button class="heart-button" @click="unlikeAttraction(attraction.no)">
            ❤️
          </button>
        </div>

        <!-- 카드 내용 -->
        <div class="card-content">
          <h3 class="attraction-title" @click="goToAttraction(attraction.no)">
            {{ attraction.title }}
          </h3>
          
          <p class="attraction-location">
            {{ attraction.addr1 }}
          </p>
          
          <div class="card-tags">
            <span class="tag">{{ contentTypeMap[attraction.contentTypeId] || "기타" }}</span>
            <span class="hashtag">#{{ attraction.title.replace(/\s/g, '') }}</span>
          </div>
        </div>
      </div>
    </div>

    <p v-if="likedAttractions.length === 0" class="no-data">좋아요한 관광지가 없습니다.</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import apiGroup from "@/api/index";
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
    const response = await apiGroup.api({
      url: "/api/map/likes/attractions",
      method: "GET", 
    });
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
    await apiGroup.api({
      url: `/api/map/likes/attractions/${attractionId}`,
      method: "POST",
    });
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
  max-width: 1200px;
  margin: 2rem auto;
  padding: 1rem;
}

h2 {
  text-align: center;
  margin-bottom: 2rem;
  font-size: 1.5rem;
  color: #333;
}

.attractions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.attraction-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.attraction-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.card-image {
  position: relative;
  height: 200px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
}

.image-icon {
  font-size: 3rem;
  opacity: 0.7;
}

.heart-button {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.heart-button:hover {
  background: white;
  transform: scale(1.1);
}

.card-content {
  padding: 1.2rem;
}

.attraction-title {
  font-size: 1.1rem;
  font-weight: bold;
  color: #333;
  margin: 0 0 0.5rem 0;
  cursor: pointer;
  transition: color 0.2s ease;
  line-height: 1.4;
}

.attraction-title:hover {
  color: #007bff;
}

.attraction-location {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 1rem 0;
  line-height: 1.4;
}

.card-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.tag {
  background: #e3f2fd;
  color: #1976d2;
  padding: 0.3rem 0.8rem;
  border-radius: 16px;
  font-size: 0.8rem;
  font-weight: 500;
}

.hashtag {
  color: #999;
  font-size: 0.8rem;
  font-weight: 500;
}

.no-data {
  text-align: center;
  color: #888;
  font-size: 1.1rem;
  margin-top: 3rem;
  padding: 2rem;
  background: #f8f9fa;
  border-radius: 8px;
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .liked-attractions-container {
    padding: 0.5rem;
  }
  
  .attractions-grid {
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 1rem;
  }
  
  .card-content {
    padding: 1rem;
  }
}

@media (max-width: 480px) {
  .attractions-grid {
    grid-template-columns: 1fr;
  }
}
</style>