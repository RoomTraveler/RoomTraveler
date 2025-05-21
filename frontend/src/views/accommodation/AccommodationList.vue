<template>
  <div class="bg-white min-h-screen font-['Pretendard','Noto Sans KR',sans-serif]">
    <!-- 전체 너비 제한 컨테이너 -->
    <div class="max-w-[768px] mx-auto">
      <!-- 새로운 필터 헤더 컴포넌트 사용 -->
      <FilterHeader @update-filters="handleFiltersUpdate" :initialFilters="currentFilters" :initialSort="currentSort" />

      <!-- 숙소 리스트 (가로 최대 768px) -->
      <div class="pb-16 px-2">
        <div v-if="!loading && totalAccommodationsCount > 0" class="px-2 pt-3 pb-1 text-xs text-gray-500">
          검색 결과 <span class="font-semibold text-gray-700">{{ totalAccommodationsCount }}</span
          >건
        </div>

        <div v-if="loading && accommodations.length === 0" class="text-center py-20">
          <div
            class="w-10 h-10 border-4 border-blue-200 border-t-blue-500 rounded-full animate-spin mx-auto mb-3"
          ></div>
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

        <!-- 숙소 카드 그리드 (항상 2열, 카드 디자인 변경) -->
        <div v-else class="grid grid-cols-2 gap-3">
          <div
            v-for="item in accommodations"
            :key="item.accommodationId"
            class="w-full max-w-[367px] min-h-[416.5px] h-auto bg-white border border-gray-200 rounded-lg shadow-md flex flex-col cursor-pointer group overflow-hidden mx-auto"
            @click="goToDetail(item.accommodationId)"
          >
            <!-- 이미지 영역 -->
            <div class="relative w-[calc(100%-16px)] h-[167px] mx-auto bg-gray-100 p-2 mt-[33px]">
              <RoundedImage
                :src="item.mainImageUrl || 'https://via.placeholder.com/300x200.png?text=NOLPLACE'"
                :alt="item.title"
                roundedClass="rounded-xl"
                imgClass="object-cover"
                containerClass="w-full h-full"
              />
            </div>
            <!-- 설명 영역 Wrapper -->
            <div
              class="px-3 sm:px-4 md:px-[24.5px] flex flex-col flex-grow"
              style="padding-top: 16px; padding-bottom: 12px"
            >
              <!-- 텍스트 정보 컴포넌트 사용 -->
              <AccommodationCardInfo :item="item" class="flex-grow" />

              <!-- 체크인 시간 정보 -->
              <div
                v-if="item.checkInTime"
                class="text-sm text-gray-700 mt-2 mb-1 text-right flex items-center justify-end"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="1em"
                  height="1em"
                  fill="currentColor"
                  class="bi bi-clock-fill mr-1.5"
                  viewBox="0 0 16 16"
                >
                  <path
                    d="M16 8A8 8 0 1 1 0 8a8 8 0 0 1 16 0zM8 3.5a.5.5 0 0 0-1 0V9a.5.5 0 0 0 .252.434l3.5 2a.5.5 0 0 0 .496-.868L8 8.71V3.5z"
                  />
                </svg>
                <span>{{ formatCheckInTime(item.checkInTime) }} ~ </span>
              </div>

              <!-- 가격 섹션 (항상 하단에 위치) -->
              <div class="mt-auto pt-2 text-right">
                <div class="text-xs text-gray-500 mb-0.5">
                  <!-- <span v-if="item.earliestCheckInTime" class="inline-block">{{ item.earliestCheckInTime }}</span> 이 부분은 상단에 별도 표시되므로 제거 또는 주석처리 -->
                  <template
                    v-if="item.originalPrice && getMinPrice(item.rooms) < item.originalPrice && item.discountRate"
                  >
                    <span class="ml-2 inline-block">{{ item.discountRate }}%</span>
                    <span class="text-gray-400 line-through ml-1 inline-block">{{
                      formatPrice(item.originalPrice, false, false)
                    }}</span>
                  </template>
                </div>
                <div class="flex justify-end items-baseline">
                  <span
                    class="text-xs text-gray-600 mr-1"
                    v-if="item.originalPrice && getMinPrice(item.rooms) < item.originalPrice && item.discountRate"
                    >최대할인가</span
                  >
                  <span class="text-xl font-bold text-gray-900">{{
                    formatPrice(getMinPrice(item.rooms), false, false)
                  }}</span>
                  <span class="text-sm font-medium text-gray-800">원~</span>
                </div>
              </div>
            </div>
            <!-- DayUse 객실 정보 바 (설명 영역 Wrapper 바깥으로 이동) -->
            <div
              v-if="item.additionalBenefits && item.additionalBenefits.includes('DayUse 객실')"
              class="px-3 py-2 bg-gray-50 border-t border-gray-200"
            >
              <span class="text-xs text-gray-700 flex items-center"
                ><i class="bi bi-check-lg text-green-500 mr-1.5"></i>DayUse 객실</span
              >
            </div>
          </div>
        </div>

        <div v-if="loadingMore" class="text-center py-5">
          <div class="w-7 h-7 border-4 border-gray-200 border-t-gray-500 rounded-full animate-spin mx-auto"></div>
        </div>
        <div
          v-if="!loading && !hasMoreData && accommodations.length > 0 && !error"
          class="text-center text-sm text-gray-500 py-5"
        >
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
import FilterHeader from "../../components/accommodation/FilterHeader.vue";
import AccommodationCardInfo from "../../components/accommodation/AccommodationCardInfo.vue";
import RoundedImage from "../../components/common/RoundedImage.vue";
// import router from "@/router"; // 이 줄을 삭제하거나 주석 처리합니다.
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

// FilterHeader로부터 받는 필터 값 타입 정의 (FilterHeader.vue와 일치)
interface FilterValues {
  region: { sidoCode: number | null; gugunCode: number | null; name: string };
  dateRange: [Date, Date] | null;
  guests: number; // guestInfo -> guests 로 변경
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
  guests?: number; // adults, children -> guests 로 변경
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
  guests: 2, // guestInfo -> guests 로 변경, 기본값 2명
  accommodationType: null,
});

const currentSort = ref<string>("created_at_desc");
// sortLabel은 FilterHeader에서 자체적으로 관리하거나, currentSort 기반으로 computed해도 됨
// 여기서는 currentSort만 URL과 동기화

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

// URL 쿼리 파라미터 업데이트 로직
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
    // guests 값 저장 (0보다 클 경우)
    if (currentFilters.value.guests > 0) query.guests = currentFilters.value.guests;

    if (currentFilters.value.accommodationType) query.accommodationType = currentFilters.value.accommodationType;
    if (currentSort.value) query.sortBy = currentSort.value;

    router.replace({ query: query as any }); // 타입 단언
  },
  { deep: true }
);

// FilterHeader에서 필터 변경 시 호출될 핸들러
function handleFiltersUpdate(filters: FilterValues & { sortBy?: string }) {
  // sortBy도 받을 수 있도록 수정
  console.log("[AccommodationList] Filters updated from FilterHeader:", JSON.parse(JSON.stringify(filters)));
  currentFilters.value = {
    region: filters.region,
    dateRange: filters.dateRange,
    guests: filters.guests,
    accommodationType: filters.accommodationType,
  };
  if (filters.sortBy) {
    // FilterHeader에서 정렬 변경도 전달한다면
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
      sortBy: currentSort.value || "created_at_desc", // 기본값 또는 현재 정렬값
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
      console.log("New Accommodations from response:", JSON.parse(JSON.stringify(newAccommodations)));
      console.log("Total Items from API:", response.data.totalItems);

      if (loadMore) {
        accommodations.value.push(...newAccommodations);
        currentPage.value = pageToFetch;
      } else {
        accommodations.value = newAccommodations;
        currentPage.value = 1; // 새 검색 시 페이지 1로 리셋
      }
      totalAccommodationsCount.value = response.data.totalItems || 0;
      console.log("Updated accommodations ref:", JSON.parse(JSON.stringify(accommodations.value)));
      console.log("Updated totalAccommodationsCount ref:", totalAccommodationsCount.value);
      hasMoreData.value =
        pageToFetch * itemsPerPage.value < (response.data.totalItems || 0) && newAccommodations.length > 0;
    } else {
      console.warn(
        "Invalid data structure in API response. Expected 'content' array and 'totalItems' number.",
        JSON.parse(JSON.stringify(response.data))
      );
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
  if (
    window.innerHeight + window.scrollY >= document.documentElement.offsetHeight - 300 &&
    hasMoreData.value &&
    !loadingMore.value &&
    !loading.value
  ) {
    fetchAccommodations(true);
  }
};

// formatDateForApi 함수 추가
const formatDateForApi = (date: Date | string): string | null => {
  if (!date) return null;
  const d = new Date(date);
  const year = d.getFullYear();
  const month = (d.getMonth() + 1).toString().padStart(2, "0");
  const day = d.getDate().toString().padStart(2, "0");
  return `${year}-${month}-${day}`;
};

// 상세 페이지 이동
const goToDetail = (id: number) => {
  const query: any = {};
  if (currentFilters.value.dateRange && currentFilters.value.dateRange[0] && currentFilters.value.dateRange[1]) {
    query.checkInDate = formatDateForApi(currentFilters.value.dateRange[0]);
    query.checkOutDate = formatDateForApi(currentFilters.value.dateRange[1]);
  }
  // guests 정보 전달
  if (currentFilters.value.guests > 0) query.guests = currentFilters.value.guests;

  router.push({ name: "AccommodationDetail", params: { id }, query });
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

const formatCheckInTime = (timeArray: [number, number] | undefined | null): string => {
  if (!timeArray || !Array.isArray(timeArray) || timeArray.length < 2) {
    return "-"; // 또는 '시간 정보 없음' 등
  }
  const hour = timeArray[0].toString().padStart(2, "0");
  const minute = timeArray[1].toString().padStart(2, "0");
  return `${hour}:${minute}`;
};

onMounted(() => {
  // URL 쿼리 파라미터에서 필터 값 복원
  const query = route.query as UrlFilters;
  let filtersUpdated = false;

  if (query.sidoCode) currentFilters.value.region.sidoCode = Number(query.sidoCode);
  if (query.gugunCode) currentFilters.value.region.gugunCode = Number(query.gugunCode);
  if (query.regionName) currentFilters.value.region.name = query.regionName;
  else if (query.sidoCode && !query.gugunCode) {
    /* 시/도만 있을 경우 이름 업데이트 로직 필요시 추가 */
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

  // 초기 필터값으로 데이터 로드
  // currentFilters, currentSort가 변경되면 watch에 의해 URL이 업데이트되고,
  // FilterHeader에 props로 전달된 값들이 변경되어 FilterHeader 내부 상태도 업데이트 될 것임.
  fetchAccommodations();
  window.addEventListener("scroll", handleScroll);
});

onUnmounted(() => {
  window.removeEventListener("scroll", handleScroll);
});

// categories, sorts 배열 정의는 FilterHeader로 이동하거나, 여기서 유지한다면 FilterHeader에 prop으로 전달 필요
// 현재는 FilterHeader가 자체적으로 types, sorts를 관리하고 있으므로 여기서는 제거하거나 주석 처리해도 무방
// const categories = ref<Category[]>([...]);
// const sorts: SortOption[] = [...];
</script>

<style scoped>
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* VueDatePicker 관련 스타일은 FilterHeader.vue 또는 전역으로 이동 */

.truncate-2-lines {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.animate-spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 모달 관련 트랜지션 스타일 제거 (FilterHeader.vue 또는 전역으로 이동) */
</style>
