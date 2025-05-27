<template>
  <div class="container mt-5">
    <h1 class="text-center mb-4">여행 계획 상세</h1>
    <div v-if="loaded">
      <div class="detail-wrapper">
        <PlanDetailList :items="plan.planAttractions" />
      </div>
      <PlanMapView :items="plan.planAttractions" />
    </div>
    <div v-else class="text-center text-muted">
      <p>로딩 중...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import apiGroup from "@/api/index";
import PlanDetailList from "@/components/plan/PlanDetailList.vue";
import PlanMapView from "@/components/plan/PlanMap.vue";

const route = useRoute();
const plan = ref({});
const loaded = ref(false);

onMounted(async () => {
  try {
    const token = route.params.token;
    const res = await apiGroup.apiNoAuth({
      url: `/api/map/plans/public/${token}`,
      method: "GET",
    });
    plan.value = res.data;
    loaded.value = true;
  } catch (err) {
    console.error("계획 불러오기 실패:", err);
  }
});
</script>

<style scoped>
.detail-wrapper {
  display: flex;
  justify-content: center;
}
</style>
