<template>
  <div class="container py-5">
    <div v-if="attraction" class="card mx-auto shadow-lg" style="max-width: 800px;">
      <div class="position-relative">
    <!-- 이미지 -->
    <img
      :src="attraction.image2 && attraction.image2.trim().length !== 0 ? attraction.image2 : '/src/assets/no-image.jpg'"
      :alt="attraction.title"
      class="card-img-top object-fit-cover"
      @load="imageLoaded = true"
      v-show="imageLoaded"
      style="height: 400px; object-fit: cover;"
    />

    <div
      v-if="!imageLoaded"
      class="position-absolute top-0 start-0 w-100 h-100 d-flex justify-content-center align-items-center bg-light"
    >
      <div class="spinner-border text-secondary" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  </div>

      <div class="card-body">
        <h2 class="card-title fw-bold">{{ attraction.title }}</h2>
        <p class="text-primary small">🏷️ {{ contentTypeMap[attraction.contentTypeId] || '기타' }}</p>
        <p v-if="attraction.homepage" class="mt-2"><a v-html="attraction.homepage" class="text-decoration-underline"></a></p>
        <p class="card-text mt-3">{{ attraction.overview }}</p>
        <p class="text-muted small mt-4">📍 <strong>위치:</strong> {{ attraction.addr1 }} {{ attraction.addr2 }}</p>
      </div>
    </div>

    <div v-else class="text-center text-muted mt-5">
      <p class="fs-5">⏳ 여행지 정보를 불러오는 중...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import axios from "axios";

const props = defineProps({
  id: {
    type: Number,
    required: true,
  },
});

const emit = defineEmits(["location-loaded"]);

const route = useRoute();
const attraction = ref(null);
const imageLoaded = ref(false);

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

onMounted(async () => {
  const { id } = route.params;

  const attractionId = props.id || id;

  try {
    const res = await axios.get(`/api/map/attractions/${attractionId}`);
    attraction.value = res.data;

    const latitude = attraction.value.latitude;
    const longitude = attraction.value.longitude;

    emit("location-loaded", { latitude, longitude });
  } catch (err) {
    console.error("API 호출 실패:", err);
  }
});
</script>

<style scoped>
/* 다크 모드 지원 */
@media (prefers-color-scheme: dark) {
  body {
    background-color: #1a202c;
    color: #edf2f7;
  }
}
.object-fit-cover {
  object-fit: cover;
  height: 400px;
  transition: transform 0.4s ease;
}
.object-fit-cover:hover {
  transform: scale(1.05);
}
</style>


