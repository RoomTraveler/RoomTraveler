<template>
  <header class="yanolja-header">
    <div class="top-header">
      <div class="container">
        <div class="top-header-content">
          <!-- 왼쪽: 뒤로가기 -->
          <div class="left-area">
            <button @click="goBack" class="back-btn" title="뒤로가기">
              <i class="bi bi-arrow-left-short"></i>
            </button>
          </div>
          <!-- 중앙: 타이틀 -->
          <div class="center-area">
            <template v-if="showStaticTitle">
              <span class="header-title fixed-title">{{ staticHeaderTitle }}</span>
            </template>
            <template v-else>
              <!-- 기존 토글 UI 전체를 단일 타이틀로 변경 -->
              <span class="header-title fixed-title">{{ currentAccommodationLabel }}</span>
            </template>
          </div>
          <!-- 오른쪽: 홈, 장바구니 -->
          <div class="user-menu">
            <button @click="goToHome" class="user-menu-item icon-btn" title="홈">
              <i class="bi bi-house"></i>
            </button>
            <button @click="goToCart" class="user-menu-item icon-btn" title="장바구니">
              <i class="bi bi-cart3"></i>
            </button>
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter, useRoute } from "vue-router";
import { ref, computed, PropType, watch } from "vue";

interface AccommodationType {
  label: string;
  value: string | null;
}

// FilterHeader.vue에서 가져온 숙소 유형 데이터 (실제로는 props로 받는 것이 이상적)
const accommodationTypes: AccommodationType[] = [
  { label: '전체', value: null },
  { label: '모텔', value: 'MOTEL' },
  { label: '호텔/리조트', value: 'HOTEL' },
  { label: '펜션/풀빌라', value: 'PENSION' },
  { label: '프리미엄', value: 'PREMIUM' },
  { label: '글램핑/캠핑', value: 'CAMPING' },
];

const props = defineProps({
  selectedAccommodationType: {
    type: String as PropType<string | null>,
    default: null,
  },
  accommodationTitle: { // 숙소 상세 페이지용 타이틀 prop
    type: String,
    default: '',
  }
});

const router = useRouter();
const route = useRoute();
const isAccommodation = ref(true);

const currentAccommodationLabel = computed(() => {
  if (props.selectedAccommodationType === null || props.selectedAccommodationType === '') {
    const defaultType = accommodationTypes.find(type => type.value === null);
    return defaultType ? defaultType.label : '숙소';
  }
  const foundType = accommodationTypes.find(
    (type) => type.value === props.selectedAccommodationType
  );
  return foundType ? foundType.label : '숙소';
});

// 현재 라우트에 따라 고정 타이틀을 표시할지 여부
const showStaticTitle = computed(() => {
  return route.name === 'AccommodationDetail' || route.name === 'RoomDetail';
});

// 실제 표시될 고정 타이틀
const staticHeaderTitle = computed(() => {
  if (route.name === 'AccommodationDetail' && props.accommodationTitle) {
    return props.accommodationTitle;
  }
  if (route.name === 'RoomDetail') {
    return "객실 상세";
  }
  return ''; // 기본값 (실제로는 showStaticTitle이 false면 표시되지 않음)
});

watch(() => props.selectedAccommodationType, (newValue, oldValue) => {
  console.log(`[AccommodationHeader] selectedAccommodationType changed: from '${oldValue}\' to '${newValue}\'`);
  console.log(`[AccommodationHeader] currentAccommodationLabel is now: ${currentAccommodationLabel.value}`);
}, { immediate: true });

// 토글 UI의 시각적 상태만 변경, 라우팅 X
function toggleView() {
  // console.log("Toggle view clicked. Current route:", route.name);
  // AccommodationList 페이지 등에서 토글 클릭 시 isAccommodation 상태만 변경
  isAccommodation.value = !isAccommodation.value;
  // 여기에 router.push 로직이 있었으나 제거됨
}

// 애니메이션 완료 후 라우팅 로직 제거
function handleTransitionEnd() {
  // console.log("Transition ended. Current route:", route.name);
  // 여기에 router.push 로직이 있었으나 제거됨
}

function goBack() {
  if (
    window.history.length > 1 &&
    document.referrer &&
    new URL(document.referrer).hostname === window.location.hostname
  ) {
    router.back();
  } else {
    // 현재 라우트가 AccommodationList 또는 PlanList고, isAccommodation 상태에 따라 홈으로 갈지 결정하던 로직 수정 가능성
    // 단순 뒤로가기가 홈으로 가는 것보다 나을 수 있음
    router.push({ name: "Home" });
  }
}

function goToHome() {
  router.push({ name: "Home" });
}

function goToCart() {
  router.push({ name: "Cart" });
}
</script>

<style scoped>
:root {
  --yanolja-red: #f0213b;
}

.yanolja-header {
  width: 100%;
  max-width: 768px;
  margin: 0 auto;
  background-color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 1000;
}
.top-header {
  padding: 0;
  border-bottom: 1px solid #eee;
}
.top-header .container {
  width: 100%;
  margin: 0 auto;
  padding: 0;
}
.top-header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 48px;
  height: 48px;
  position: relative;
}
.left-area {
  flex: 0 0 60px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}
.back-btn {
  background: none;
  border: none;
  font-size: 30px;
  color: var(--yanolja-red);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 8px;
  border-radius: 50%;
  transition: background 0.15s;
}
.back-btn:hover {
  background: #f9f2f5;
}
.center-area {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 0;
}
.toggle-container {
  position: relative;
  width: 120px;
  height: 36px;
  background: #f5f5f5;
  border-radius: 18px;
  cursor: pointer;
  overflow: hidden;
}
.toggle-slider {
  position: relative;
  display: flex;
  width: 200%;
  height: 100%;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}
.toggle-slider.slide-right {
  transform: translateX(-50%);
}
.toggle-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 12px;
  transition: all 0.3s ease;
}
.toggle-item .header-title {
  font-size: 16px;
  font-weight: 600;
  color: #666;
  transition: all 0.3s ease;
}
.toggle-item.active .header-title {
  color: var(--yanolja-red);
  font-weight: 700;
}
.user-menu {
  flex: 0 0 90px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}
.user-menu-item {
  margin-left: 8px;
  color: #333;
  background: none;
  border: none;
  text-decoration: none;
  font-size: 22px;
  cursor: pointer;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition:
    background 0.15s,
    color 0.15s;
}
.user-menu-item:hover {
  color: var(--yanolja-red);
  background: #f9f2f5;
}
.icon-btn {
  font-size: 22px;
}

/* RoomDetail 등에서 고정 타이틀일 경우 스타일 */
.center-area > .header-title.fixed-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--yanolja-red); /* 기본 액티브 색상 사용 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: calc(100% - 120px); /* 좌우 영역 제외한 최대 너비, 필요시 조정 */
  text-align: center;
}

@media (max-width: 768px) {
  .top-header-content {
    min-height: 44px;
    gap: 0;
  }
  .left-area,
  .user-menu {
    flex: 0 0 44px;
  }
  .toggle-container {
    width: 100px;
    height: 32px;
  }
  .toggle-item .header-title {
    font-size: 14px;
  }
}
</style>
