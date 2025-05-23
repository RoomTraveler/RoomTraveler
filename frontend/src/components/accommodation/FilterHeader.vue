<template>
  <div class="sticky-top bg-white border-bottom" style="z-index: 1050">
    <!-- AccommodationHeader 사용 -->
    <AccommodationHeader :selected-accommodation-type="selectedAccommodationType" />

    <!-- 필터 트리거 버튼 영역 -->
    <div
      class="d-flex align-items-center justify-content-between px-3"
      style="height: 53px; gap: 8px; border-bottom: 1px solid #f1f1f1; overflow-x: auto"
    >
      <!-- 지역 선택 버튼 -->
      <button
        @click="openRegionModal"
        class="btn btn-outline-danger btn-sm d-flex align-items-center flex-shrink-0"
        type="button"
        style="min-width: 92px"
      >
        <i class="bi bi-geo-alt me-1"></i>
        <span>{{ selectedRegionLabel }}</span>
        <i class="bi bi-chevron-down ms-1"></i>
      </button>

      <!-- 날짜 및 인원 선택 버튼 그룹 -->
      <div class="d-flex align-items-center gap-2 ms-auto">
        <button
          @click="showDateSelectModal = true"
          class="btn btn-outline-danger btn-sm d-flex align-items-center flex-shrink-0"
          type="button"
        >
          <i class="bi bi-calendar-check me-1"></i>
          <span>{{ dateRangeLabel }}</span>
        </button>
        <button
          @click="showGuestSelectModal = true"
          class="btn btn-outline-danger btn-sm d-flex align-items-center flex-shrink-0"
          type="button"
        >
          <i class="bi bi-person me-1"></i>
          <span>{{ guestLabel }}</span>
        </button>
        <!-- 정렬 버튼 추가 -->
        <button
          @click="showSortModal = true"
          class="btn btn-outline-danger btn-sm d-flex align-items-center flex-shrink-0"
          type="button"
        >
          <i class="bi bi-filter-right me-1"></i>
          <span>{{ currentSortLabel }}</span>
        </button>
      </div>
    </div>

    <!-- 호텔 종류 필터 -->
    <div class="px-3 pt-2 pb-2 border-bottom" style="overflow-x: auto">
      <div class="d-flex align-items-center gap-2 flex-nowrap">
        <button
          v-for="type in accommodationTypes"
          :key="type.value === null ? 'type-all' : type.value"
          @click="selectAccommodationType(type.value)"
          class="btn btn-sm flex-shrink-0"
          :class="
            selectedAccommodationType === type.value
              ? 'btn-danger text-white fw-semibold shadow-sm'
              : 'btn-light text-secondary border'
          "
          type="button"
        >
          {{ type.label }}
        </button>
      </div>
    </div>
  </div>

  <!-- 모달 영역 (각각의 컴포넌트는 기존과 동일) -->
  <RegionSelectModal v-model:show="showRegionModal" @selected="handleRegionSelected" :isFullScreen="true" />
  <DateSelectModal v-model:show="showDateSelectModal" :initialDateRange="dateRange" @apply="handleDateApplied" />
  <GuestSelectModal
    v-model:show="showGuestSelectModal"
    :initial-guests="selectedGuests"
    :max-guests="30"
    @apply="handleGuestApplied"
  />
  <SortModal
    v-model:show="showSortModal"
    :current-sort="currentSortValue"
    :options="sortOptions"
    @apply-sort="handleSortApplied"
    @close="showSortModal = false"
  />
</template>

<script setup lang="ts">
import { ref, computed, watch } from "vue";
import AccommodationHeader from "./AccommodationHeader.vue";
import RegionSelectModal from "../modals/RegionSelectModal.vue";
import DateSelectModal from "../modals/DateSelectModal.vue";
import GuestSelectModal from "../modals/GuestSelectModal.vue";
import SortModal from "../modals/SortModal.vue";

interface Region {
  sidoCode: number | null;
  gugunCode: number | null;
  name: string;
}

interface AccommodationType {
  label: string;
  value: string | null;
}

const accommodationTypes: AccommodationType[] = [
  { label: "전체", value: null },
  { label: "모텔", value: "MOTEL" },
  { label: "호텔/리조트", value: "HOTEL" },
  { label: "펜션/풀빌라", value: "PENSION" },
  { label: "프리미엄", value: "PREMIUM" },
  { label: "글램핑/캠핑", value: "CAMPING" },
];

interface FilterValuesFromParent {
  region: Region;
  dateRange: [Date, Date] | null;
  guests: number;
  accommodationType: string | null;
}

interface SortOption {
  label: string;
  value: string;
}

const props = defineProps<{
  initialFilters?: FilterValuesFromParent;
  initialSort?: string;
}>();

const emit = defineEmits(["update-filters"]);

// States
const selectedRegion = ref<Region>(
  props.initialFilters?.region || { sidoCode: null, gugunCode: null, name: "전체 지역" }
);
const dateRange = ref<[Date, Date] | null>(props.initialFilters?.dateRange || null);
const selectedGuests = ref<number>(props.initialFilters?.guests || 2);
const selectedAccommodationType = ref<string | null>(props.initialFilters?.accommodationType || null);
const currentSortValue = ref<string>(props.initialSort || "created_at_desc");
const showSortModal = ref(false);

// 정렬 옵션 업데이트
const sortOptions: Array<SortOption> = [
  { label: "추천순", value: "recommendScoreDesc" },
  { label: "후기 많은 순", value: "reviewCountDesc" },
  { label: "후기 적은 순", value: "reviewCountAsc" },
  { label: "가격 낮은 순", value: "minPriceAsc" },
  { label: "가격 높은 순", value: "maxPriceDesc" },
  { label: "최신 등록순", value: "created_at_desc" },
  { label: "이름순", value: "name" },
];

// Watch props
watch(
  () => props.initialFilters,
  (newFilters) => {
    if (newFilters) {
      selectedRegion.value = newFilters.region || { sidoCode: null, gugunCode: null, name: "전체 지역" };
      dateRange.value = newFilters.dateRange || null;
      selectedGuests.value = newFilters.guests || 2;
      selectedAccommodationType.value = newFilters.accommodationType || null;
    }
  },
  { deep: true }
);
watch(
  () => props.initialSort,
  (newSort) => {
    currentSortValue.value = newSort || "created_at_desc";
  }
);

// Computed labels
const selectedRegionLabel = computed(() => selectedRegion.value.name || "지역을 선택해주세요.");
const dateRangeLabel = computed(() => {
  if (!dateRange.value || !dateRange.value[0] || !dateRange.value[1]) return "날짜를 선택해주세요.";
  const [start, end] = dateRange.value;
  const formatDateForLabel = (d: Date) =>
    d instanceof Date && !isNaN(d.getTime()) ? `${d.getMonth() + 1}.${d.getDate()}` : "";
  return `${formatDateForLabel(start)} ~ ${formatDateForLabel(end)}`;
});
const guestLabel = computed(() => `인원 ${selectedGuests.value}명`);
const currentSortLabel = computed(() => {
  const selected = sortOptions.find((option) => option.value === currentSortValue.value);
  return selected ? selected.label : "정렬";
});

// Modal states
const showRegionModal = ref(false);
const showDateSelectModal = ref(false);
const showGuestSelectModal = ref(false);

let isOpeningRegionModal = false;

// --- Methods ---

function openRegionModal() {
  if (isOpeningRegionModal || showRegionModal.value) return;
  isOpeningRegionModal = true;
  showRegionModal.value = true;
}
function handleRegionSelected(region: Region) {
  selectedRegion.value = region;
  showRegionModal.value = false;
  isOpeningRegionModal = false;
  emitFilters();
}
function handleDateApplied(newDateRange: [Date, Date]) {
  if (newDateRange && newDateRange.length === 2 && newDateRange[0] instanceof Date && newDateRange[1] instanceof Date) {
    if (JSON.stringify(dateRange.value) !== JSON.stringify(newDateRange)) {
      dateRange.value = newDateRange;
      emitFilters();
    }
  }
  showDateSelectModal.value = false;
}
function handleGuestApplied(payload: { guests: number }) {
  if (selectedGuests.value !== payload.guests) {
    selectedGuests.value = payload.guests;
    emitFilters();
  }
  showGuestSelectModal.value = false;
}
function handleSortApplied(sortValue: string) {
  if (currentSortValue.value !== sortValue) {
    currentSortValue.value = sortValue;
    emitFilters();
  }
  showSortModal.value = false;
}
function emitFilters() {
  emit("update-filters", {
    region: selectedRegion.value,
    dateRange: dateRange.value,
    guests: selectedGuests.value,
    accommodationType: selectedAccommodationType.value,
    sortBy: currentSortValue.value,
  });
}
function selectAccommodationType(typeValue: string | null) {
  if (selectedAccommodationType.value !== typeValue) {
    selectedAccommodationType.value = typeValue;
    emitFilters();
  }
}

// Modal watchers for debug/log
watch(showRegionModal, (val) => {
  if (!val) isOpeningRegionModal = false;
});
</script>

<style scoped>
/* 부트스트랩 사용 시 커스텀 스크롤 숨김(크롬/엣지 한정) */
::-webkit-scrollbar {
  display: none;
}
</style>
