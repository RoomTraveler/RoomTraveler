<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">관리자 홈</h2>

      <!-- 숙소 유형별 통계 카드 -->
      <div class="row mb-4">
        <div class="col-6 col-md-3 mb-3" v-for="type in typeCardData" :key="type.key">
          <div class="card h-100 text-center" :class="type.bgClass">
            <div class="card-body">
              <h5 class="card-title">{{ type.label }}</h5>
              <h2>{{ type.count }}</h2>
            </div>
          </div>
        </div>
      </div>

      <!-- 관리 기능 바로가기 -->
      <div class="row">
        <div class="col-md-4 mb-3">
          <router-link to="/admin/accommodations/pending" class="btn btn-info w-100 py-3">
            숙소 신청 관리
          </router-link>
        </div>
        <div class="col-md-4 mb-3">
          <router-link to="/admin/accommodations" class="btn btn-primary w-100 py-3">
            숙소 관리
          </router-link>
        </div>
        <div class="col-md-4 mb-3">
          <router-link to="/admin/hosts" class="btn btn-success w-100 py-3">
            호스트 신청 관리
          </router-link>
        </div>
      </div>
      <div class="row">
        <div class="col-md-4 mb-3">
          <router-link to="/admin/hosts/all" class="btn btn-warning w-100 py-3">
            호스트 관리
          </router-link>
        </div>
        <div class="col-md-4 mb-3">
          <router-link to="/admin/users" class="btn btn-secondary w-100 py-3">
            사용자 관리
          </router-link>
        </div>
        <!-- 필요시 빈 col 추가하여 정렬 -->
        <div class="col-md-4 mb-3"></div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { computed, onMounted } from 'vue';
import Layout from '@/components/layout/Layout.vue';
import { useAdminStore } from '@/store/adminStore';

const adminStore = useAdminStore();

onMounted(() => {
  adminStore.fetchAccommodationTypeCounts();
});

const typeCardData = computed(() => {
  const counts = adminStore.typeCounts || {};
  let totalCount = 0;
  // counts 객체의 모든 값(개수)을 합산하여 totalCount 계산
  for (const key in counts) {
    if (Object.prototype.hasOwnProperty.call(counts, key)) {
      totalCount += counts[key] || 0;
    }
  }

  const hotelCount = (counts.HOTEL || 0) + (counts.RESORT || 0);
  const pensionCount = (counts.PENSION || 0) + (counts.POOLVILLA || 0);
  const motelCount = counts.MOTEL || 0;
  const premiumCount = counts.PREMIUM || 0;
  const campingCount = counts.CAMPING || 0;

  // 명시된 타입들의 합
  const sumOfKnownTypes = motelCount + hotelCount + pensionCount + premiumCount + campingCount;

  // 기타 = 전체 - 명시된 타입들의 합
  const otherCount = totalCount - sumOfKnownTypes;

  return [
    { key: 'ALL', label: '전체', count: totalCount, bgClass: 'bg-light' },
    { key: 'MOTEL', label: '모텔', count: motelCount, bgClass: 'bg-info text-white' },
    { key: 'HOTEL', label: '호텔/리조트', count: hotelCount, bgClass: 'bg-primary text-white' },
    { key: 'PENSION', label: '펜션/풀빌라', count: pensionCount, bgClass: 'bg-success text-white' },
    { key: 'PREMIUM', label: '프리미엄', count: premiumCount, bgClass: 'bg-warning' },
    { key: 'CAMPING', label: '글램핑/캠핑', count: campingCount, bgClass: 'bg-secondary text-white' },
    // 계산된 otherCount 사용
    { key: 'OTHER', label: '기타', count: otherCount >= 0 ? otherCount : 0, bgClass: 'bg-dark text-white' },
  ];
});
</script>

<style scoped>
.card {
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.card-title {
  font-size: 1.2rem;
  margin-bottom: .5rem;
}
h2 {
  font-size: 2.2rem;
  margin-bottom: 0;
}
</style>
