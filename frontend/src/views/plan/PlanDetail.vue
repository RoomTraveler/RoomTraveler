<template>
  <div class="p-4">
    <h1 class="text-xl font-bold mb-4">여행 계획 상세</h1>
    <div v-if="loaded">
      <PlanDetailList :items="plan.planAttractions" />
      <PlanMapView :items="plan.planAttractions" />
    </div>
    <div v-else>
      <p>로딩 중...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import PlanDetailList from '@/components/plan/PlanDetailList.vue'
import PlanMapView from '@/components/plan/PlanMap.vue'

const route = useRoute()
const plan = ref({})
const loaded = ref(false)

onMounted(async () => {
  try {
    const id = route.params.planId
    const res = await axios.get(`/api/map/plans/${id}`)
    plan.value = res.data
    loaded.value = true
  } catch (err) {
    console.error('계획 불러오기 실패:', err)
  }
})
</script>
