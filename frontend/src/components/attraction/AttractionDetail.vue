<template>
  <div class="min-h-screen bg-gradient-to-b from-white to-gray-100 dark:from-gray-900 dark:to-gray-800 py-10 px-4">
    <div
      v-if="attraction"
      class="max-w-4xl mx-auto bg-white dark:bg-gray-900 rounded-3xl shadow-2xl p-8 space-y-8 transition-all duration-300"
    >
      <!-- 제목 및 유형 -->
      <div>
        <h1 class="text-4xl font-extrabold text-gray-900 dark:text-white mb-2">
          {{ attraction.title }}
        </h1>
        <p class="text-sm text-indigo-600 dark:text-indigo-400">
          🏷️ {{ contentTypeMap[attraction.contentTypeId] || "기타" }}
        </p>
      </div>

      <!-- 이미지 -->
      <div class="overflow-hidden rounded-2xl shadow-lg">
        <div v-if="attraction.image2.trim().length !== 0">
          <img
            v-show="imageLoaded"
            :src="attraction.image2"
            alt="Attraction Image"
            class="w-full h-80 object-cover transform hover:scale-105 transition-transform duration-500"
            @load="imageLoaded = true"
          />

          <!-- 로딩 스피너 -->
          <div v-if="!imageLoaded" class="w-full h-80 flex items-center justify-center bg-gray-200 dark:bg-gray-700">
            <span class="text-gray-500 dark:text-gray-300 animate-pulse">🖼️ 이미지 로딩 중...</span>
          </div>
        </div>
      </div>

      <!-- 개요 및 상세 정보 -->
      <div class="space-y-4 text-gray-700 dark:text-gray-300">
        <div v-if="attraction.homepage" class="text-blue-600 dark:text-blue-400 underline text-sm">
          <span v-html="attraction.homepage"></span>
        </div>
        <p class="leading-relaxed text-lg">
          {{ attraction.overview }}
        </p>
        <p class="text-sm text-gray-500 dark:text-gray-400">
          📍 <span class="font-medium">위치:</span> {{ attraction.addr1 + " " + attraction.addr2 }}
        </p>
      </div>
    </div>

    <!-- 로딩 상태 -->
    <div v-else class="text-center text-gray-500 dark:text-gray-400 mt-10">
      <p class="text-lg animate-pulse">⏳ 여행지 정보를 불러오는 중...</p>
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
</style>
