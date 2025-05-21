<template>
  <ul style="list-style: none; padding: 0">
    <li v-for="(item, idx) in items" :key="item.order" class="mb-4">
      <span class="font-semibold">{{ idx + 1 }}.</span>
      <!-- 타이틀 클릭 시 이동 -->
      <a
        :href="`/attractions/${item.attractionId}`"
        class="attraction-title"
        @click.prevent="goToAttraction(item.attractionId)"
      >
        {{ item.title }}
      </a>

      <!-- 이미지 확대 효과 -->
      <div class="image-wrapper">
        <img :src="item.imageUrl.length === 0 ? '/src/assets/no-image.jpg': item.imageUrl" :alt="item.title" class="attraction-image" />
      </div>
    </li>
  </ul>
</template>

<script setup>
import { defineProps } from "vue";
import { useRouter } from "vue-router";

const props = defineProps({
  items: {
    type: Array,
    default: () => [],
  },
});
const router = useRouter();

const goToAttraction = (id) => {
  router.push(`/attractions/${id}`);
};
</script>

<style scoped>
.attraction-title {
  margin-left: 0.5rem;
  color: #007bff;
  cursor: pointer;
  text-decoration: none;
  font-weight: bold;
}
.attraction-title:hover {
  text-decoration: underline;
}

.image-wrapper {
  display: inline-block;
  margin-top: 0.5rem;
  transition: transform 0.3s ease;
  overflow: hidden;
  border-radius: 8px;
  width: 150px;
  height: 150px;
}
.attraction-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}
.image-wrapper:hover .attraction-image {
  transform: scale(1.5);
  z-index: 1;
}
</style>
