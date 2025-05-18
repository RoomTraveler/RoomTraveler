<template>
  <div class="bg-white min-h-screen font-['Pretendard','Noto Sans KR',sans-serif]">
    <!-- 전체 너비 제한 컨테이너 -->
    <div class="max-w-[768px] mx-auto">
      <!-- 새로운 필터 헤더 컴포넌트 사용 -->
      <FilterHeader @update-filters="handleFiltersUpdate" />

      <!-- 숙소 리스트 (가로 최대 768px) -->
      <div class="pb-16"> 
        <div v-if="!loading && totalAccommodationsCount > 0" class="px-4 pt-3 pb-1 text-xs text-gray-500">
          검색 결과 <span class="font-semibold text-gray-700">{{ totalAccommodationsCount }}</span>건
        </div>
        
        <div v-if="loading && accommodations.length === 0" class="text-center py-20">
          <div class="w-10 h-10 border-4 border-blue-200 border-t-blue-500 rounded-full animate-spin mx-auto mb-3"></div>
          <span class="text-gray-500 text-sm">숙소 정보를 찾고 있어요...</span>
        </div>
        <div v-else-if="error" class="text-center py-16 text-red-500">
          <i class="bi bi-exclamation-triangle-fill text-3xl mb-2"></i>
          <p class="text-sm">{{ error }}</p>
        </div>
        <div v-else-if="!loading && accommodations.length === 0" class="text-center py-20">
          <i class="bi bi-search text-5xl text-gray-300 mb-3"></i>
          <p class="text-gray-500 text-base">조건에 맞는 숙소가 없어요.</p>
        </div>

        <!-- 숙소 카드 그리드 (항상 2열) -->
        <div v-else class="grid grid-cols-2 gap-x-0 gap-y-0 px-0">
          <div
            v-for="item in accommodations"
            :key="item.accommodationId"
            class="w-full h-[209px] bg-white border border-gray-200 flex overflow-hidden cursor-pointer group relative"
            @click="goToDetail(item.accommodationId)"
          >
            <!-- 이미지: 140x168 -->
            <div class="relative w-[140px] h-[168px] bg-gray-200 flex-shrink-0 self-center ml-2">
              <img
                :src="item.mainImageUrl || 'https://via.placeholder.com/140x168.png?text=NOLPLACE'"
                class="w-full h-full object-cover"
                :alt="item.title"
                loading="lazy"
              />
            </div>
            <!-- 설명: 나머지 공간을 채우도록 width 조정 -->
            <div class="h-[168px] p-3 flex flex-col justify-between flex-grow self-center ml-2 mr-2 overflow-hidden">
              <div>
                <h3 class="font-semibold text-sm text-gray-800 group-hover:underline truncate-2-lines mb-1">
                  {{ item.title }}
                </h3>
                <div v-if="item.rating" class="flex items-center text-xs text-gray-600 mb-1">
                  <i class="bi bi-star-fill text-yellow-400 text-[11px] mr-0.5"></i>
                  <span class="font-bold">{{ item.rating.toFixed(1) }}</span>
                  <span v-if="item.reviewCount" class="ml-0.5">({{ item.reviewCount.toLocaleString() }})</span>
                </div>
                <!-- <p class="text-[10px] text-gray-500 truncate">{{ item.address }}</p> -->
              </div>
              <div class="text-right">
                 <div v-if="item.originalPrice && getMinPrice(item.rooms) < item.originalPrice && item.discountRate" class="text-xs mb-0.5">
                    <span class="text-red-500 font-bold">{{ item.discountRate }}%</span>
                    <span class="text-gray-400 line-through ml-1">{{ formatPrice(item.originalPrice, true, false) }}</span>
                </div>
                <span class="text-base font-bold text-gray-800">
                  {{ formatPrice(getMinPrice(item.rooms), true, false) }}
                </span>
                <span class="text-xs font-medium text-gray-700">원~</span>
              </div>
            </div>
             <button
                @click.stop="toggleWishlist(item.accommodationId)"
                class="absolute top-2 right-2 p-1.5 bg-black/30 rounded-full text-white hover:text-red-400 focus:outline-none z-10"
                :class="{ 'text-red-500': item.isWished }"
              >
                <i class="bi text-lg" :class="item.isWished ? 'bi-heart-fill' : 'bi-heart'"></i>
              </button>
          </div>
        </div>

        <div v-if="loadingMore" class="text-center py-5">
          <div class="w-7 h-7 border-4 border-gray-200 border-t-gray-500 rounded-full animate-spin mx-auto"></div>
        </div>
        <div v-if="!loading && !hasMoreData && accommodations.length > 0 && !error" class="text-center text-sm text-gray-500 py-5">
          마지막 숙소입니다.
        </div>
      </div>
    </div>

    <!-- 통합 필터 모달 (전체 화면 스타일) -->
    <!-- 이 모달은 FilterHeader.vue로 이동되었거나, 다른 방식으로 처리될 예정이므로 여기서는 제거 또는 주석 처리합니다. -->
    <!-- <Transition name="modal-full-screen"> ... </Transition> -->

    <!-- RegionSelectModal은 FilterHeader.vue로 이동되었습니다. -->
    <!-- <RegionSelectModal ... /> -->

    <!-- 날짜/인원 선택 중앙 모달은 FilterHeader.vue로 이동되었습니다. -->
    <!-- <div v-if="showDateTimeGuestModal" ... > ... </div> -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import accommodationService from "../../api/accommodationApi.js";
import FilterHeader from './FilterHeader.vue';

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
  checkInInfo?: string;
  additionalBenefits?: string[];
  isWished?: boolean;
  distance?: string;
  couponAvailable?: boolean;
}

// FilterHeader로부터 받는 필터 값 타입 정의 (FilterHeader.vue와 일치)
interface FilterValues {
  region: { sidoCode: number | null; gugunCode: number | null; name: string };
  dateRange: [Date, Date] | null;
  guestInfo: { adults: number; children: number }; // guestCount -> guestInfo
  accommodationType: string | null; // 새로 추가된 호텔 유형
}

interface Category {
  label: string;
  value: string | null;
}

interface SortOption {
  label: string;
  value: string;
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

// FilterHeader로부터 받을 필터 값들 (초기값 설정)
const currentFilters = ref<FilterValues>({
  region: { sidoCode: null, gugunCode: null, name: "전체 지역" },
  dateRange: null,
  guestInfo: { adults: 2, children: 0 }, // guestCount -> guestInfo
  accommodationType: null, // 호텔 유형 초기값
});

// 숙소 유형 및 정렬은 AccommodationList에 유지 (야놀자 사이트 참고)
const selectedCategory = ref<string | null>(null);
const currentSort = ref<string>("created_at_desc"); 
const sortLabel = ref<string>("추천순"); // 정렬 레이블도 필요 시 FilterHeader에서 관리 가능

const categories = ref<Category[]>([
    { label: "전체", value: null },
    { label: "호텔", value: "HOTEL" },
    { label: "리조트", value: "RESORT" },
    { label: "펜션", value: "PENSION" },
    { label: "모텔", value: "MOTEL" },
]);

const sorts: SortOption[] = [
  { label: "추천순", value: "created_at_desc" },
  { label: "이름순", value: "name" },
  { label: "가격 낮은순", value: "priceAsc" },
  { label: "가격 높은순", value: "priceDesc" },
];

// FilterHeader에서 필터 변경 시 호출될 핸들러
function handleFiltersUpdate(filters: FilterValues) {
  console.log('[AccommodationList] Filters updated from FilterHeader:', JSON.parse(JSON.stringify(filters)));
  currentFilters.value = { ...filters }; // 전체 필터 객체를 업데이트
  resetAndFetchAccommodations();
}

const formatDate = (date: Date): string => {
  if (!(date instanceof Date) || isNaN(date.getTime())) return '';
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, "0");
  const day = date.getDate().toString().padStart(2, "0");
  return `${year}-${month}-${day}`;
};

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
      gugunCode: currentFilters.value.region.gugunCode,
      accommodationType: currentFilters.value.accommodationType,
      adults: currentFilters.value.guestInfo.adults > 0 ? currentFilters.value.guestInfo.adults : undefined,
      children: currentFilters.value.guestInfo.children > 0 ? currentFilters.value.guestInfo.children : undefined,
      sortBy: currentSort.value,
      page: pageToFetch,
      size: itemsPerPage.value,
      checkInDate: currentFilters.value.dateRange && currentFilters.value.dateRange[0] ? formatDate(currentFilters.value.dateRange[0]) : undefined,
      checkOutDate: currentFilters.value.dateRange && currentFilters.value.dateRange[1] ? formatDate(currentFilters.value.dateRange[1]) : undefined,
    };

    if (currentFilters.value.region.sidoCode !== null && currentFilters.value.region.sidoCode !== 0) {
      params.sidoCode = currentFilters.value.region.sidoCode;
    }

    const filteredParams = Object.fromEntries(Object.entries(params).filter(([_, v]) => v !== undefined && v !== null && v !== ""));

    console.log("[AccommodationList] Fetching with params (modified for sidoCode=0):", filteredParams);
    const response = await accommodationService.getAccommodations(filteredParams);
    console.log("API Response Data:", JSON.parse(JSON.stringify(response.data)));
    console.log("Response Status:", response.status);

    if (response.data && Array.isArray(response.data.content)) {
      const newAccommodations = response.data.content;
      console.log("New Accommodations from response:", JSON.parse(JSON.stringify(newAccommodations)));
      console.log("Total Items from API:", response.data.totalItems);

      if (loadMore) {
        accommodations.value.push(...newAccommodations);
        currentPage.value = pageToFetch;
      } else {
        accommodations.value = newAccommodations;
      }
      totalAccommodationsCount.value = response.data.totalItems || 0;
      console.log("Updated accommodations ref:", JSON.parse(JSON.stringify(accommodations.value)));
      console.log("Updated totalAccommodationsCount ref:", totalAccommodationsCount.value);
      hasMoreData.value = (pageToFetch * itemsPerPage.value) < (response.data.totalItems || 0) && newAccommodations.length > 0;
    } else {
      console.warn("Invalid data structure in API response. Expected 'content' array and 'totalItems' number.", JSON.parse(JSON.stringify(response.data)));
      if (!loadMore) accommodations.value = [];
      hasMoreData.value = false;
      if (!loadMore) totalAccommodationsCount.value = 0;
    }
  } catch (err: any) {
    console.error("Error fetching accommodations:", err);
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
  if (window.innerHeight + window.scrollY >= document.documentElement.offsetHeight - 300 && hasMoreData.value && !loadingMore.value && !loading.value) {
    fetchAccommodations(true);
  }
};

// 상세 페이지 이동
const goToDetail = (id: number) => {
  router.push({ name: "AccommodationDetail", params: { id } });
};

// 찜하기 기능 (간단한 토글)
const toggleWishlist = (id: number) => {
  const item = accommodations.value.find(acc => acc.accommodationId === id);
  if (item) {
    item.isWished = !item.isWished;
    // TODO: API 연동 (찜하기/찜취소)
    console.log(`Accommodation ID ${id} wished status: ${item.isWished}`);
  }
};

// 가격 포맷팅 함수
const formatPrice = (price: number | null | undefined, showSymbol = true, showSuffix = true): string => {
  if (price === null || price === undefined) return "가격문의";
  const formattedPrice = new Intl.NumberFormat("ko-KR").format(price);
  return `${showSymbol ? "" : ""}${formattedPrice}${showSuffix ? "" : ""}`;
};

// 객실 중 최소 가격 가져오기 (또는 기본 가격)
const getMinPrice = (rooms: Room[] | undefined): number => {
  if (!rooms || rooms.length === 0) {
    // rooms 정보가 없거나 비어있으면, accommodation.originalPrice 또는 다른 기본 가격을 사용하거나, 0 또는 특정 값을 반환
    // 여기서는 임의로 0을 반환하지만, 실제 정책에 맞게 수정 필요
    return 0; 
  }
  return Math.min(...rooms.map((room) => room.price));
};

onMounted(() => {
  // Query Parameter에서 필터 값 로드 (예시, 실제 구현 필요)
  // if (route.query.sidoCode) currentFilters.value.region.sidoCode = Number(route.query.sidoCode);
  // ... 다른 필터 값들도 유사하게 로드
  
  fetchAccommodations();
  window.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});

// 라우트 변경 감지하여 필터 초기화 및 데이터 다시 로드 (선택적)
// watch(route, () => { 
//   resetAndFetchAccommodations();
// }, { deep: true });

</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar { display: none; }
.scrollbar-hide { -ms-overflow-style: none; scrollbar-width: none; }

/* VueDatePicker 관련 스타일은 FilterHeader.vue 또는 전역으로 이동 */

.truncate-2-lines {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.animate-spin { animation: spin 1s linear infinite; }
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 모달 관련 트랜지션 스타일 제거 (FilterHeader.vue 또는 전역으로 이동) */
</style>
