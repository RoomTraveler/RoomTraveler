<template>
  <div class="sticky top-0 z-50 bg-white border-b border-gray-200">
    <!-- 기존 헤더 대신 새로운 AccommodationHeader 사용 -->
    <AccommodationHeader title="숙소 목록" />

    <!-- 필터 트리거 버튼 영역 -->
    <div class="flex items-center justify-between px-3 h-[52.8px] gap-2 border-b border-gray-100 overflow-x-auto scrollbar-hide">
      <!-- 지역 선택 버튼 -->
      <button @click="openRegionModal" class="flex-shrink-0 whitespace-nowrap text-xs sm:text-sm px-3 py-2 border border-pink-300 rounded-md text-pink-500 hover:bg-pink-50 transition flex items-center h-full">
        <i class="bi bi-geo-alt mr-1.5"></i>
        <span>{{ selectedRegionLabel }}</span>
        <i class="bi bi-chevron-down text-xs ml-1"></i>
      </button>

      <!-- 날짜 및 인원 선택 버튼 그룹 -->
      <div class="flex items-center gap-2 ml-auto">
        <button @click="showDateSelectModal = true" class="flex-shrink-0 whitespace-nowrap text-xs sm:text-sm px-3 py-2 border border-pink-300 rounded-md text-pink-500 hover:bg-pink-50 transition flex items-center h-full">
          <i class="bi bi-calendar-check mr-1.5"></i>
          <span>{{ dateRangeLabel }}</span>
        </button>
        <button @click="showGuestSelectModal = true" class="flex-shrink-0 whitespace-nowrap text-xs sm:text-sm px-3 py-2 border border-pink-300 rounded-md text-pink-500 hover:bg-pink-50 transition flex items-center h-full">
          <i class="bi bi-person mr-1.5"></i>
          <span>{{ guestLabel }}</span>
        </button>
      </div>
    </div>

    <!-- 호텔 종류 필터 -->
    <div class="px-3 py-2.5 border-b border-gray-100 overflow-x-auto scrollbar-hide">
      <div class="flex items-center gap-2">
        <button
          v-for="type in accommodationTypes"
          :key="type.value === null ? 'type-all' : type.value"
          @click="selectAccommodationType(type.value)"
          :class="[
            'flex-shrink-0 whitespace-nowrap text-xs sm:text-sm px-3.5 py-1.5 rounded-full transition-colors duration-150 ease-in-out',
            selectedAccommodationType === type.value
              ? 'bg-pink-500 text-white font-semibold shadow-md'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200 hover:text-gray-800'
          ]"
        >
          {{ type.label }}
        </button>
      </div>
    </div>
  </div>

  <RegionSelectModal
    v-model:show="showRegionModal"
    @selected="handleRegionSelected"
    :isFullScreen="true"
  />

  <DateSelectModal
    v-model:show="showDateSelectModal"
    :initialDateRange="dateRange"
    @apply="handleDateApplied"
  />

  <GuestSelectModal
    v-model:show="showGuestSelectModal"
    :initialAdults="guestAdults" 
    :initialChildren="guestChildren"
    @apply="handleGuestApplied"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue';
import AccommodationHeader from './AccommodationHeader.vue'; // 새로 만든 헤더 컴포넌트 임포트
import RegionSelectModal from '../../components/modals/RegionSelectModal.vue';
import DateSelectModal from '@/components/modals/DateSelectModal.vue';
import GuestSelectModal from '@/components/modals/GuestSelectModal.vue';

interface Region {
  sidoCode: number | null;
  gugunCode: number | null;
  name: string;
}

interface GuestSelection {
  adults: number;
  children: number;
}

// 호텔 유형 인터페이스 및 상수 추가
interface AccommodationType {
  label: string;
  value: string | null; // 'null' for '전체'
}

const accommodationTypes: AccommodationType[] = [
  { label: '전체', value: null },
  { label: '모텔', value: 'MOTEL' },
  { label: '호텔/리조트', value: 'HOTEL_RESORT' },
  { label: '펜션/풀빌라', value: 'PENSION_POOL_VILLA' },
  { label: '프리미엄', value: 'PREMIUM' },
  { label: '글램핑/캠핑', value: 'GLAMPING_CAMPING' },
];

const emit = defineEmits(['update-filters']);

// State for filters
const selectedRegion = ref<Region>({
  sidoCode: null, gugunCode: null, name: '전체 지역',
});
const dateRange = ref<[Date, Date] | null>(null);
const guestInfo = ref<GuestSelection>({ adults: 2, children: 0 });
const selectedAccommodationType = ref<string | null>(null); // 선택된 호텔 유형 상태

// Computed labels for display
const selectedRegionLabel = computed(() => selectedRegion.value.name || '지역을 선택해주세요.');
const dateRangeLabel = computed(() => {
  if (!dateRange.value || !dateRange.value[0] || !dateRange.value[1]) return '날짜를 선택해주세요.';
  const [start, end] = dateRange.value;
  const formatDateForLabel = (d: Date) => {
    if (!(d instanceof Date) || isNaN(d.getTime())) return ''; // 유효한 Date 객체인지 확인
    return (d.getMonth() + 1) + '.' + d.getDate();
  }
  return formatDateForLabel(start) + ' ~ ' + formatDateForLabel(end);
});
const guestLabel = computed(() => {
  let label = `성인 ${guestInfo.value.adults}명`;
  if (guestInfo.value.children > 0) {
    label += `, 아동 ${guestInfo.value.children}명`;
  }
  return label;
});

// Region Modal state
const showRegionModal = ref(false);

// New Modal States
const showDateSelectModal = ref(false);
const showGuestSelectModal = ref(false);

let isOpeningRegionModal = false;

// --- Methods ---

// Region Modal
function openRegionModal() {
  if (isOpeningRegionModal || showRegionModal.value) {
    return;
  }
  isOpeningRegionModal = true;
  showRegionModal.value = true;
}

function handleRegionSelected(region: Region) {
  selectedRegion.value = region;
  showRegionModal.value = false;
  isOpeningRegionModal = false;
  emitFilters();
}

// DateSelectModal handler
function handleDateApplied(newDateRange: [Date, Date]) {
  if (newDateRange && newDateRange.length === 2 && newDateRange[0] instanceof Date && newDateRange[1] instanceof Date) {
    if (JSON.stringify(dateRange.value) !== JSON.stringify(newDateRange)) {
        dateRange.value = newDateRange;
        emitFilters();
    }
  }
  showDateSelectModal.value = false;
}

// GuestSelectModal handler
function handleGuestApplied(newGuests: GuestSelection) {
  if (guestInfo.value.adults !== newGuests.adults || guestInfo.value.children !== newGuests.children) {
    guestInfo.value = { adults: newGuests.adults, children: newGuests.children };
    emitFilters();
  }
  showGuestSelectModal.value = false;
}

// Emit all current filter states
function emitFilters() {
  console.log('[FilterHeader] Emitting update-filters with:', {
    region: selectedRegion.value,
    dateRange: dateRange.value,
    guestInfo: guestInfo.value,
    accommodationType: selectedAccommodationType.value,
  });
  emit('update-filters', {
    region: selectedRegion.value,
    dateRange: dateRange.value,
    guestInfo: guestInfo.value,
    accommodationType: selectedAccommodationType.value,
  });
}

// 호텔 유형 선택 함수
function selectAccommodationType(typeValue: string | null) {
  if (selectedAccommodationType.value !== typeValue) {
    selectedAccommodationType.value = typeValue;
    emitFilters();
  }
}

// Watchers for modal states
watch(showRegionModal, (newValue) => {
  console.log('[FilterHeader] showRegionModal changed to ' + newValue);
  if (!newValue) {
    isOpeningRegionModal = false;
  }
});

watch(showDateSelectModal, (newValue) => {
  console.log('[FilterHeader] showDateSelectModal changed to ' + newValue);
});

watch(showGuestSelectModal, (newValue) => {
  console.log('[FilterHeader] showGuestSelectModal changed to ' + newValue);
});

// 분리된 guestAdults와 guestChildren computed 속성 (GuestSelectModal에 전달용)
const guestAdults = computed(() => guestInfo.value.adults);
const guestChildren = computed(() => guestInfo.value.children);

</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }
</style> 