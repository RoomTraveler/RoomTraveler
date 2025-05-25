<template>
  <div class="timeline-container">
    <div
      v-for="(item, idx) in items"
      :key="item.order"
      class="timeline-item d-flex"
    >
      <div class="timeline-left text-center">
        <div class="circle-number" :class="`circle-${idx + 1}`">
          {{ idx + 1 }}
        </div>
        <div class="line"></div>
      </div>

      <div class="timeline-card card shadow-sm">
        <div class="card-body">
          <h5 class="card-title mb-1">
            <a
              :href="`/attractions/${item.attractionId}`"
              class="attraction-title"
              @click.prevent="goToAttraction(item.attractionId)"
            >
              {{ item.title }}
            </a>
          </h5>
          <p class="card-subtitle text-muted small">
            {{ contentTypeMap[item.contentType] }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>


<script setup>
import { defineProps } from "vue";
import { useRouter } from "vue-router";

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

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
});
console.log(props.items)
const router = useRouter();

const goToAttraction = (id) => {
  router.push(`/attractions/${id}`);
};
</script>

<style scoped>
.timeline-container {
  display: flex;
  flex-direction: column;
  align-items: start;
  gap: 1.5rem;
  position: relative;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
}

.timeline-left {
  width: 60px;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.circle-number {
  width: 28px;
  height: 28px;
  background-color: #6c63ff;
  color: white;
  border-radius: 50%;
  font-size: 14px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 8px;
}

.line {
  width: 2px;
  flex-grow: 1;
  background-color: #ddd;
}

.timeline-card {
  flex-grow: 1;
  margin-left: 16px;
  border-radius: 12px;
  padding: 0.75rem 1rem;
  background-color: #fff;
}

.attraction-title {
  color: #212529;
  font-weight: 600;
  text-decoration: none;
}

.attraction-title:hover {
  color: #0d6efd;
  text-decoration: underline;
}
</style>