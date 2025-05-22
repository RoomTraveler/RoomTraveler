<template>
  <div class="bg-white min-vh-100" style="font-family: 'Pretendard','Noto Sans KR',sans-serif;">
    <div class="container" style="max-width:768px;">
      <!-- 필터 헤더 -->
      <FilterHeader @update-filters="handleFiltersUpdate" :initialFilters="currentFilters" :initialSort="currentSort" />

      <!-- 숙소 리스트 -->
      <div class="pb-5 px-2">
        <div v-if="!loading && totalAccommodationsCount > 0" class="px-2 pt-3 pb-1 text-muted small">
          검색 결과 <span class="fw-semibold text-dark">{{ totalAccommodationsCount }}</span>건
        </div>

        <!-- 로딩 스피너 (초기 로딩) -->
        <div v-if="loading && accommodations.length === 0" class="text-center py-5">
          <div class="spinner-border text-primary mb-3" role="status" style="width:2.5rem; height:2.5rem;"></div>
          <span class="text-muted small">숙소 정보를 찾고 있어요...</span>
        </div>

        <!-- 에러 -->
        <div v-else-if="error" class="text-center py-5 text-danger">
          <i class="bi bi-exclamation-triangle-fill fs-2 mb-2"></i>
          <p class="small">{{ error }}</p>
        </div>

        <!-- 조건 없는 숙소 -->
        <div v-else-if="!loading && accommodations.length === 0" class="text-center py-5">
          <i class="bi bi-search display-4 text-secondary mb-3"></i>
          <p class="text-muted">조건에 맞는 숙소가 없어요.</p>
        </div>

        <!-- 카드 2열 그리드 -->
        <div v-else class="row g-3">
          <div
              v-for="item in accommodations"
              :key="item.accommodationId"
              class="col-12 col-md-6 d-flex"
          >
            <div
                class="card shadow-sm w-100 mb-3 h-100 border border-light rounded-3 cursor-pointer"
                style="min-height: 416.5px;"
                @click="goToDetail(item.accommodationId)"
            >
              <!-- 이미지 -->
              <div class="position-relative mx-auto bg-light mt-4" style="width:calc(100% - 16px); height:167px; padding:8px;">
                <RoundedImage
                    :src="item.mainImageUrl || 'https://via.placeholder.com/300x200.png?text=NOLPLACE'"
                    :alt="item.title"
                    roundedClass="rounded-3"
                    imgClass="object-fit-cover"
                    containerClass="w-100 h-100"
                />
              </div>

              <!-- 설명영역 -->
              <div class="card-body d-flex flex-column" style="padding-top:16px; padding-bottom:12px;">
                <AccommodationCardInfo :item="item" class="flex-grow-1" />

                <!-- 체크인 시간 -->
                <div
                    v-if="item.checkInTime"
                    class="text-end text-secondary small d-flex justify-content-end align-items-center mt-2 mb-1"
                >
                  <i class="bi bi-clock-fill me-2"></i>
                  <span>{{ formatCheckInTime(item.checkInTime) }} ~</span>
                </div>

                <!-- 가격/할인 -->
                <div class="mt-auto pt-2 text-end">
                  <div class="small text-muted mb-1">
                    <template v-if="item.originalPrice && getMinPrice(item.rooms) < item.originalPrice && item.discountRate">
                      <span class="ms-2">{{ item.discountRate }}%</span>
                      <span class="text-decoration-line-through ms-1">{{ formatPrice(item.originalPrice, false, false) }}</span>
                    </template>
                  </div>
                  <div class="d-flex justify-content-end align-items-baseline">
                    <span
                        class="small text-muted me-1"
                        v-if="item.originalPrice && getMinPrice(item.rooms) < item.originalPrice && item.discountRate"
                    >최대할인가</span>
                    <span class="fs-5 fw-bold text-dark">{{ formatPrice(getMinPrice(item.rooms), false, false) }}</span>
                    <span class="ms-1 text-secondary small">원~</span>
                  </div>
                </div>
              </div>

              <!-- DayUse 객실 바 -->
              <div
                  v-if="item.additionalBenefits && item.additionalBenefits.includes('DayUse 객실')"
                  class="px-3 py-2 bg-light border-top border-light"
              >
                <span class="small text-secondary d-flex align-items-center">
                  <i class="bi bi-check-lg text-success me-2"></i>DayUse 객실
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- 무한스크롤: 추가 로딩 스피너 -->
        <div v-if="loadingMore" class="text-center py-4">
          <div class="spinner-border text-secondary" role="status" style="width:1.75rem; height:1.75rem;"></div>
        </div>

        <div
            v-if="!loading && !hasMoreData && accommodations.length > 0 && !error"
            class="text-center text-muted py-4 small"
        >
          마지막 숙소입니다.
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// (이 부분 그대로)
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import accommodationService from "../../api/accommodationApi.js";
import FilterHeader from "../../components/accommodation/FilterHeader.vue";
import AccommodationCardInfo from "../../components/accommodation/AccommodationCardInfo.vue";
import RoundedImage from "../../components/common/RoundedImage.vue";
// import router from "@/router"; // 주석 처리
import { ElMessage } from "element-plus";

interface Room {
  price: number;
  capacity: number;
}

interface Accommodation {
  accommodationId: number;
  title: string;
  address: string;
  mainImageUrl: string;
  rooms: Room[];
  rating?: number;
  reviewCount?: number;
  originalPrice?: number;
  hotelGrade?: string;
  accommodationType?: string;
  couponInfo?: string;
  discountRate?: string;
  checkInTime?: [number, number];
  additionalBenefits?: string[];
  isWished?: boolean;
  distance?: string;
  couponAvailable?: boolean;
  thumbnailImageUrl?: string;
}

// FilterHeader로부터 받는 필터 값 타입 정의
interface FilterValues {
  region: { sidoCode: number | null; gugunCode: number | null; name: string };
  dateRange: [Date, Date] | null;
  guests: number;
  accommodationType: string | null;
}

interface Category {
  label: string;
  value: string | null;
}

interface SortOption {
  label: string;
  value: string;
}

interface UrlFilters {
  sidoCode?: number;
  gugunCode?: number;
  regionName?: string;
  checkInDate?: string;
  checkOutDate?: string;
  guests?: number;
  accommodationType?: string;
  sortBy?: string;
}

const route = useRoute();
const router = useRouter();

const accommodations = ref<Accommodation[]>([]);
const loading = ref(false);
const error = ref<string | null>(null);
const currentPage = ref(1);
const itemsPerPage = ref(15);
const hasMoreData = ref(true);
const loadingMore = ref(false);
const totalAccommodationsCount = ref(0);

// 초기 필터 값 (URL 쿼리로부터 설정될 수 있도록 기본값 제공)
const currentFilters = ref<FilterValues>({
  region: { sidoCode: null, gugunCode: null, name: "전체 지역" },
  dateRange: null,
  guests: 2,
  accommodationType: null,
});
const currentSort = ref<string>("created_at_desc");

// ---- 이하 모든 유틸/핸들러/함수, watch, 무한스크롤, onMounted 등 기존 코드 그대로 ----
const formatDate = (date: Date | string | null): string => {
  if (!date) return "";
  const d = typeof date === "string" ? new Date(date) : date;
  if (!(d instanceof Date) || isNaN(d.getTime())) return "";
  const year = d.getFullYear();
  const month = (d.getMonth() + 1).toString().padStart(2, "0");
  const day = d.getDate().toString().padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const parseDate = (dateStr: string | null | undefined): Date | null => {
  if (!dateStr) return null;
  const date = new Date(dateStr);
  return isNaN(date.getTime()) ? null : date;
};

watch(
    [currentFilters, currentSort],
    () => {
      const query: UrlFilters = {};

      if (currentFilters.value.region.sidoCode) query.sidoCode = currentFilters.value.region.sidoCode;
      if (currentFilters.value.region.gugunCode) query.gugunCode = currentFilters.value.region.gugunCode;
      if (currentFilters.value.region.name && currentFilters.value.region.name !== "전체 지역")
        query.regionName = currentFilters.value.region.name;
      if (currentFilters.value.dateRange && currentFilters.value.dateRange[0] && currentFilters.value.dateRange[1]) {
        query.checkInDate = formatDate(currentFilters.value.dateRange[0]);
        query.checkOutDate = formatDate(currentFilters.value.dateRange[1]);
      }
      if (currentFilters.value.guests > 0) query.guests = currentFilters.value.guests;
      if (currentFilters.value.accommodationType) query.accommodationType = currentFilters.value.accommodationType;
      if (currentSort.value) query.sortBy = currentSort.value;

      router.replace({ query: query as any });
    },
    { deep: true }
);

function handleFiltersUpdate(filters: FilterValues & { sortBy?: string }) {
  currentFilters.value = {
    region: filters.region,
    dateRange: filters.dateRange,
    guests: filters.guests,
    accommodationType: filters.accommodationType,
  };
  if (filters.sortBy) {
    currentSort.value = filters.sortBy;
  }
  resetAndFetchAccommodations();
}

function resetAndFetchAccommodations() {
  currentPage.value = 1;
  accommodations.value = [];
  hasMoreData.value = true;
  totalAccommodationsCount.value = 0;
  if (window) {
    window.scrollTo(0, 0);
  }
  fetchAccommodations();
}

async function fetchAccommodations(loadMore = false) {
  if (loading.value && !loadMore) return;
  if (loadingMore.value && loadMore) return;
  if (loadMore && !hasMoreData.value) return;

  let pageToFetch: number;
  if (loadMore) {
    loadingMore.value = true;
    pageToFetch = currentPage.value + 1;
  } else {
    loading.value = true;
    pageToFetch = 1;
  }
  error.value = null;

  try {
    const params: any = {
      page: pageToFetch,
      size: itemsPerPage.value,
      sortBy: currentSort.value || "created_at_desc",
    };

    if (currentFilters.value.region.sidoCode) params.sidoCode = currentFilters.value.region.sidoCode;
    if (currentFilters.value.region.gugunCode) params.gugunCode = currentFilters.value.region.gugunCode;
    if (currentFilters.value.accommodationType) params.accommodationType = currentFilters.value.accommodationType;
    if (currentFilters.value.guests > 0) params.guests = currentFilters.value.guests;
    if (currentFilters.value.dateRange && currentFilters.value.dateRange[0]) {
      params.checkInDate = formatDate(currentFilters.value.dateRange[0]);
    }
    if (currentFilters.value.dateRange && currentFilters.value.dateRange[1]) {
      params.checkOutDate = formatDate(currentFilters.value.dateRange[1]);
    }

    const filteredParams = Object.fromEntries(
        Object.entries(params).filter(([_, v]) => v !== undefined && v !== null && v !== "")
    );
    console.log("[AccommodationList] Fetching with params:", filteredParams);

    const response = await accommodationService.getAccommodations(filteredParams);
    console.log("API Response Data:", JSON.parse(JSON.stringify(response.data)));
    console.log("Response Status:", response.status);

    if (response.data && Array.isArray(response.data.content)) {
      const newAccommodations = response.data.content;
      if (loadMore) {
        accommodations.value.push(...newAccommodations);
        currentPage.value = pageToFetch;
      } else {
        accommodations.value = newAccommodations;
        currentPage.value = 1;
      }
      totalAccommodationsCount.value = response.data.totalItems || 0;
      hasMoreData.value =
          pageToFetch * itemsPerPage.value < (response.data.totalItems || 0) && newAccommodations.length > 0;
    } else {
      if (!loadMore) accommodations.value = [];
      hasMoreData.value = false;
      if (!loadMore) totalAccommodationsCount.value = 0;
    }
  } catch (err: any) {
    error.value = "숙소 정보를 불러오는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
    if (!loadMore) accommodations.value = [];
    hasMoreData.value = false;
    if (!loadMore) totalAccommodationsCount.value = 0;
  } finally {
    if (loadMore) {
      loadingMore.value = false;
    } else {
      loading.value = false;
    }
  }
}

// 무한 스크롤
const handleScroll = () => {
  if (
      window.innerHeight + window.scrollY >= document.documentElement.offsetHeight - 300 &&
      hasMoreData.value &&
      !loadingMore.value &&
      !loading.value
  ) {
    fetchAccommodations(true);
  }
};

const formatDateForApi = (date: Date | string): string | null => {
  if (!date) return null;
  const d = new Date(date);
  const year = d.getFullYear();
  const month = (d.getMonth() + 1).toString().padStart(2, "0");
  const day = d.getDate().toString().padStart(2, "0");
  return `${year}-${month}-${day}`;
};

const goToDetail = (id: number) => {
  const query: any = {};
  if (currentFilters.value.dateRange && currentFilters.value.dateRange[0] && currentFilters.value.dateRange[1]) {
    query.checkInDate = formatDateForApi(currentFilters.value.dateRange[0]);
    query.checkOutDate = formatDateForApi(currentFilters.value.dateRange[1]);
  }
  if (currentFilters.value.guests > 0) query.guests = currentFilters.value.guests;

  router.push({ name: "AccommodationDetail", params: { id }, query });
};

const formatPrice = (price: number | null | undefined, showSymbol = true, showSuffix = true): string => {
  if (price === null || price === undefined) return "가격문의";
  const formattedPrice = new Intl.NumberFormat("ko-KR").format(price);
  return `${showSymbol ? "" : ""}${formattedPrice}${showSuffix ? "" : ""}`;
};

const getMinPrice = (rooms: Room[] | undefined): number => {
  if (!rooms || rooms.length === 0) {
    return 0;
  }
  return Math.min(...rooms.map((room) => room.price));
};

const formatCheckInTime = (timeArray: [number, number] | undefined | null): string => {
  if (!timeArray || !Array.isArray(timeArray) || timeArray.length < 2) {
    return "-";
  }
  const hour = timeArray[0].toString().padStart(2, "0");
  const minute = timeArray[1].toString().padStart(2, "0");
  return `${hour}:${minute}`;
};

onMounted(() => {
  const query = route.query as UrlFilters;
  if (query.sidoCode) currentFilters.value.region.sidoCode = Number(query.sidoCode);
  if (query.gugunCode) currentFilters.value.region.gugunCode = Number(query.gugunCode);
  if (query.regionName) currentFilters.value.region.name = query.regionName;
  else if (query.sidoCode && !query.gugunCode) {
    // 필요 시 시/도만 있을 때 이름 업데이트
  } else if (!query.sidoCode) {
    currentFilters.value.region.name = "전체 지역";
  }

  const checkIn = parseDate(query.checkInDate);
  const checkOut = parseDate(query.checkOutDate);
  if (checkIn && checkOut) {
    currentFilters.value.dateRange = [checkIn, checkOut];
  }
  if (query.guests) currentFilters.value.guests = Number(query.guests);
  if (query.accommodationType) currentFilters.value.accommodationType = query.accommodationType;
  if (query.sortBy) currentSort.value = query.sortBy;

  fetchAccommodations();
  window.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});
</script>

<style scoped>
.cursor-pointer { cursor: pointer; }
.object-fit-cover { object-fit: cover; }
</style>
