<template>
  <div class="bg-[#f2f2f2] min-h-screen font-['Pretendard','Noto Sans KR',sans-serif]">
    <!-- 상단 고정 영역 -->
    <div class="sticky top-0 z-50 bg-white shadow-sm">
      <!-- 1. 지역 / 타이틀 / 아이콘 바 -->
      <header class="flex items-center justify-between px-4 h-[52px] border-b">
        <div class="flex items-center">
          <button class="p-0 mr-1" @click="goBack">
            <span class="p-1 hover:bg-gray-100 rounded-full transition-colors duration-150">
              <i class="bi bi-arrow-left-short text-3xl"></i>
            </span>
          </button>
          <span
            @click="showRegionModal = true"
            class="font-bold text-lg cursor-pointer hover:text-blue-600 transition-colors duration-150"
            >{{ selectedRegionLabel || "지역 선택" }} <i class="bi bi-chevron-down text-xs"></i
          ></span>
        </div>
        <span class="absolute left-1/2 transform -translate-x-1/2 font-semibold text-xl">호텔·리조트</span>
        <div class="flex gap-3 items-center">
          <button @click="goToHome" class="p-1 hover:bg-gray-100 rounded-full transition-colors duration-150">
            <i class="bi bi-house text-xl"></i>
          </button>
          <button class="p-1 hover:bg-gray-100 rounded-full transition-colors duration-150">
            <i class="bi bi-cart3 text-xl"></i>
          </button>
          <!-- 장바구니 아이콘 -->
        </div>
      </header>

      <!-- 2. 날짜 / 인원 선택 바 -->
      <div class="flex justify-between px-4 py-3 border-b bg-white">
        <button
          @click="showDateModal = true"
          class="flex-1 text-left text-sm font-medium p-2.5 rounded-md hover:bg-gray-100 transition-colors duration-150"
        >
          <i class="bi bi-calendar-check mr-1.5 text-gray-500"></i> {{ dateRangeLabel || "날짜 선택" }}
        </button>
        <div class="border-r mx-2.5"></div>
        <button
          @click="showGuestModal = true"
          class="flex-1 text-left text-sm font-medium p-2.5 rounded-md hover:bg-gray-100 transition-colors duration-150"
        >
          <i class="bi bi-person mr-1.5 text-gray-500"></i> 성인 {{ guestLabel }}명
        </button>
      </div>

      <!-- 3. 태그/필터 스트립 - 기존 카테고리 활용 -->
      <div class="px-4 py-2.5 border-b bg-white overflow-x-auto whitespace-nowrap flex items-center scrollbar-hide">
        <button
          v-for="cat in categories"
          :key="cat.value"
          @click="selectCategory(cat.value)"
          :class="[
            'mr-2 px-3 py-1 text-sm rounded-full transition-colors duration-200',
            selectedCategory === cat.value
              ? 'bg-black text-white font-semibold'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200',
          ]"
        >
          {{ cat.label }}
        </button>
        <!-- TODO: 이미지의 #추천리조트 같은 태그 필터는 현재 API 미지원으로 보류 -->
      </div>

      <!-- 4. 하위 필터 버튼 및 정렬 -->
      <div class="flex items-center justify-between px-4 py-2.5 bg-white border-b">
        <div class="flex items-center gap-2 overflow-x-auto scrollbar-hide whitespace-nowrap">
          <button
            class="flex items-center text-xs px-2.5 py-1.5 border rounded-md text-gray-700 bg-gray-50 hover:bg-gray-200 transition-colors duration-150"
          >
            <i class="bi bi-funnel mr-1"></i> 필터
          </button>
          <!-- 위 버튼들의 실제 필터링 기능은 백엔드 API 지원 필요 -->
        </div>
        <div class="relative ml-2 whitespace-nowrap">
          <button
            @click="sortOpen = !sortOpen"
            class="flex items-center text-gray-600 text-xs px-2.5 py-1.5 border rounded-md bg-gray-50 hover:bg-gray-200 transition-colors duration-150"
          >
            <i class="bi bi-arrow-up-down mr-1 text-sm"></i> {{ sortLabel }}
          </button>
          <ul
            v-if="sortOpen"
            class="absolute right-0 mt-1.5 bg-white border border-gray-200 rounded-md shadow-lg w-36 z-20 overflow-hidden"
          >
            <li
              v-for="sortOpt in sorts"
              :key="sortOpt.value"
              class="px-3.5 py-2 hover:bg-gray-100 cursor-pointer text-xs text-gray-700 flex justify-between items-center transition-colors duration-150"
              :class="{ 'font-semibold text-blue-600 bg-blue-50': sortOpt.value === currentSort }"
              @click="selectSort(sortOpt)"
            >
              {{ sortOpt.label }}
              <i v-if="sortOpt.value === currentSort" class="bi bi-check-lg text-blue-600"></i>
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- "NOL 초이스+" 같은 광고/추천 섹션 타이틀 (필요시 추가) -->
    <!-- <div class="px-4 py-3">
      <h2 class="text-lg font-bold text-gray-800">NOL 초이스+</h2>
    </div> -->

    <!-- 모달들 (RegionSelectModal, DatePicker, GuestModal) -->
    <RegionSelectModal :show="showRegionModal" @close="showRegionModal = false" @selected="onRegionSelected" />
    <div
      v-if="showDateModal"
      class="fixed inset-0 z-[999] bg-black/60 flex items-center justify-center p-4 backdrop-blur-sm"
    >
      <div
        class="bg-white rounded-xl w-full max-w-md mx-auto shadow-xl transform transition-all duration-300 ease-out scale-100 opacity-100"
      >
        <div class="flex justify-between items-center p-5 border-b border-gray-200">
          <h3 class="font-bold text-lg text-gray-800">날짜 선택</h3>
          <button
            class="text-2xl text-gray-400 hover:text-gray-600 transition-colors duration-150"
            @click="showDateModal = false"
          >
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="p-5">
          <VueDatePicker
            v-model="dateRange"
            range
            locale="ko"
            :format="formatDatePicker"
            :enable-time-picker="false"
            :min-date="today"
            auto-apply
            month-name-format="long"
            input-class-name="dp-custom-input"
            calendar-cell-class-name="dp-custom-cell"
            menu-class-name="dp-custom-menu"
          />
        </div>
        <div class="p-5 border-t border-gray-200">
          <button
            class="w-full py-3 rounded-lg bg-blue-600 text-white font-bold hover:bg-blue-700 transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50"
            @click="applyDate"
          >
            선택 완료
          </button>
        </div>
      </div>
    </div>
    <div
      v-if="showGuestModal"
      class="fixed inset-0 z-[999] bg-black/60 flex items-center justify-center p-4 backdrop-blur-sm"
    >
      <div
        class="bg-white rounded-xl w-full max-w-xs mx-auto shadow-xl transform transition-all duration-300 ease-out scale-100 opacity-100"
      >
        <div class="flex justify-between items-center p-5 border-b border-gray-200">
          <h3 class="font-bold text-lg text-gray-800">인원 선택</h3>
          <button
            class="text-2xl text-gray-400 hover:text-gray-600 transition-colors duration-150"
            @click="showGuestModal = false"
          >
            <i class="bi bi-x-lg"></i>
          </button>
        </div>
        <div class="p-5">
          <div class="flex justify-between items-center gap-4 my-6">
            <button
              @click="guestCount = Math.max(1, guestCount - 1)"
              class="w-14 h-14 bg-gray-100 rounded-full text-3xl font-bold text-gray-700 hover:bg-gray-200 disabled:opacity-50 transition-colors duration-150 flex items-center justify-center"
              :disabled="guestCount <= 1"
            >
              <i class="bi bi-dash"></i>
            </button>
            <span class="text-4xl font-bold w-16 text-center text-gray-800">{{ guestCount }}</span>
            <button
              @click="guestCount = guestCount + 1"
              class="w-14 h-14 bg-gray-100 rounded-full text-3xl font-bold text-gray-700 hover:bg-gray-200 transition-colors duration-150 flex items-center justify-center"
            >
              <i class="bi bi-plus"></i>
            </button>
          </div>
        </div>
        <div class="p-5 border-t border-gray-200">
          <button
            class="w-full py-3 rounded-lg bg-blue-600 text-white font-bold hover:bg-blue-700 transition-colors duration-150 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50"
            @click="applyGuest"
          >
            선택 완료
          </button>
        </div>
      </div>
    </div>

    <!-- 숙소 리스트 -->
    <div class="w-full max-w-[768px] mx-auto px-3 pt-4 pb-24">
      <!-- 하단 지도 버튼 고려하여 pb 증가 -->
      <div
        v-if="!loading && totalAccommodationsCount > 0 && accommodations.length > 0"
        class="text-sm text-gray-600 mb-3"
      >
        총 <span class="font-bold text-blue-600">{{ totalAccommodationsCount }}</span
        >개의 숙소 중 <span class="font-bold text-blue-600">{{ accommodations.length }}</span
        >개를 보고 있어요.
      </div>
      <div
        v-else-if="!loading && accommodations.length > 0 && totalAccommodationsCount === 0"
        class="text-sm text-gray-600 mb-3"
      >
        <span class="font-bold text-blue-600">{{ accommodations.length }}</span
        >개의 숙소가 검색되었어요.
        <!-- 백엔드에서 totalItems를 못받았을 경우 대비 -->
      </div>

      <section class="grid grid-cols-1 gap-y-5">
        <!-- 모바일 우선 1열, 데스크탑은 추후 조정 가능 -->
        <div v-if="loading && accommodations.length === 0" class="col-span-full text-center py-16">
          <div
            class="w-12 h-12 border-4 border-blue-300 border-t-blue-600 rounded-full animate-spin mx-auto mb-4"
          ></div>
          <span class="text-gray-500 text-base">숙소 정보를 찾고 있어요...</span>
        </div>
        <div v-else-if="error" class="col-span-full text-center py-12 text-red-600 bg-red-50 p-6 rounded-lg shadow">
          <i class="bi bi-exclamation-triangle-fill text-3xl mr-2"></i>
          <p class="mt-2 text-base">{{ error }}</p>
        </div>
        <div v-else-if="!loading && accommodations.length === 0" class="col-span-full text-center py-20">
          <div class="text-6xl text-gray-300 mb-4">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="64"
              height="64"
              fill="currentColor"
              class="bi bi-exclamation-circle inline-block"
              viewBox="0 0 16 16"
            >
              <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14zm0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16z" />
              <path
                d="M7.002 11a1 1 0 1 1 2 0 1 1 0 0 1-2 0zM7.1 4.995a.905.905 0 1 1 1.8 0l-.35 3.507a.552.552 0 0 1-1.1 0L7.1 4.995z"
              />
            </svg>
          </div>
          <p class="text-gray-500 text-lg">
            아쉽지만, 조건에 맞는 숙소가 없어요.<br />다른 조건으로 다시 검색해보세요!
          </p>
        </div>

        <!-- 변경될 숙소 카드 시작 (야놀자 스타일 참고) -->
        <div
          v-for="item in accommodations"
          :key="item.accommodationId"
          class="bg-white rounded-lg shadow-md overflow-hidden flex flex-col md:flex-row cursor-pointer hover:shadow-lg transition-shadow duration-200 border border-gray-100"
          @click="goToDetail(item.accommodationId)"
        >
          <div class="relative w-full md:w-[130px] md:h-[130px] aspect-[4/3] md:aspect-auto flex-shrink-0 bg-gray-200">
            <img
              :src="item.mainImageUrl || 'https://via.placeholder.com/260x195.png?text=NOLPLACE'"
              class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
              :alt="item.title"
              loading="lazy"
            />
            <button
              @click.stop="toggleWishlist(item.accommodationId)"
              class="absolute top-2 right-2 p-1.5 bg-black/30 rounded-full text-white hover:bg-red-500 hover:text-white focus:outline-none transition-colors duration-200 z-10"
              :class="{ '!bg-red-500 !text-white': item.isWished }"
            >
              <i class="bi text-base" :class="item.isWished ? 'bi-heart-fill' : 'bi-heart'"></i>
            </button>
          </div>

          <div class="p-3 md:p-4 flex flex-col flex-grow justify-between w-full">
            <div>
              <div class="flex justify-between items-start mb-0.5">
                <span class="text-[11px] text-gray-500 font-medium uppercase tracking-wide">{{
                  item.hotelGrade || "숙소유형"
                }}</span>
                <!-- 야놀자처럼 평점 표시 (오른쪽 위) -->
                <div v-if="item.rating" class="flex items-center text-xs">
                  <i class="bi bi-star-fill text-yellow-400 mr-0.5"></i>
                  <span class="font-bold text-gray-700">{{ item.rating.toFixed(1) }}</span>
                  <span v-if="item.reviewCount" class="text-gray-500 ml-1"
                    >({{ item.reviewCount.toLocaleString() }})</span
                  >
                </div>
              </div>
              <h3
                class="font-semibold text-base text-gray-800 truncate-2-lines leading-snug mb-1 group-hover:text-blue-600 transition-colors"
              >
                {{ item.title }}
              </h3>
              <p class="text-xs text-gray-500 truncate mb-1 flex items-center">
                <i class="bi bi-geo-alt mr-1 text-gray-400"></i> {{ item.address }}
              </p>
              <!-- 추가 태그 (예: 쿠폰 정보, 거리 등) - 야놀자 스타일 -->
              <div class="flex flex-wrap gap-1.5 mt-1.5 mb-2">
                <span
                  v-if="item.couponInfo"
                  class="text-[10px] px-1.5 py-0.5 rounded-sm font-medium bg-orange-100 text-orange-600 border border-orange-200"
                >
                  {{ item.couponInfo }}
                </span>
                <!-- <span class="text-[10px] px-1.5 py-0.5 rounded-sm font-medium bg-gray-100 text-gray-600">도보 5분</span> -->
              </div>
            </div>

            <div class="mt-auto flex justify-between items-end">
              <div class="text-xs text-gray-600">
                <div v-if="item.additionalBenefits && item.additionalBenefits.length > 0" class="space-y-0.5">
                  <div
                    v-for="(benefit, index) in item.additionalBenefits.slice(0, 1)"
                    :key="index"
                    class="flex items-center"
                  >
                    <i class="bi bi-check-circle text-blue-500 mr-1 text-[11px]"></i>
                    <span class="truncate">{{ benefit }}</span>
                  </div>
                </div>
              </div>
              <div class="text-right">
                <div
                  v-if="item.originalPrice && getMinPrice(item.rooms) < item.originalPrice"
                  class="text-gray-400 line-through text-[11px]"
                >
                  {{ formatPrice(item.originalPrice, true, false) }}원
                </div>
                <div class="flex items-baseline justify-end">
                  <span v-if="item.discountRate" class="text-red-500 font-bold text-sm mr-1.5"
                    >{{ item.discountRate }}%</span
                  >
                  <span class="text-sm text-gray-500 mr-0.5">{{ item.checkInInfo || "1박" }}</span>
                  <span class="text-lg text-black font-extrabold">
                    {{ formatPrice(getMinPrice(item.rooms), false, false) }}
                  </span>
                  <span class="font-medium text-gray-800 text-sm">원</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <!-- 숙소 카드 끝 -->
      </section>

      <div v-if="loadingMore" class="mt-8 text-center py-4">
        <div class="w-8 h-8 border-4 border-blue-300 border-t-blue-600 rounded-full animate-spin mx-auto"></div>
        <p class="text-sm text-gray-500 mt-2">숙소를 더 불러오는 중...</p>
      </div>
      <div
        v-if="!loading && !hasMoreData && accommodations.length > 0 && !error"
        class="mt-8 text-center text-sm text-gray-500 py-4"
      >
        모든 숙소를 다 둘러보셨어요!
      </div>
    </div>

    <!-- 하단 지도 플로팅 버튼 -->
    <div class="fixed bottom-8 left-1/2 transform -translate-x-1/2 z-40">
      <button
        class="flex items-center justify-center px-5 py-3 bg-gray-900 text-white rounded-full shadow-2xl hover:bg-black focus:outline-none focus:ring-2 focus:ring-gray-900 focus:ring-opacity-50 transition-all duration-200 transform hover:scale-105"
      >
        <i class="bi bi-map-fill mr-2.5"></i>
        <span class="font-semibold text-sm">지도 보기</span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch, onBeforeUnmount } from "vue";
import { useRouter, useRoute } from "vue-router";
import axios from "axios";
import RegionSelectModal from "./RegionSelectModal.vue";

import VueDatePicker from "@vuepic/vue-datepicker";
import "@vuepic/vue-datepicker/dist/main.css";
import { ko } from "date-fns/locale";

interface Room {
  price: number;
  capacity: number;
  // roomName?: string;
  // ... 기타 객실 관련 필드
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
  couponInfo?: string;
  discountRate?: string;
  checkInInfo?: string;
  additionalBenefits?: string[];
  isWished?: boolean; // 찜 상태 추가
}

// 모달 상태
const showRegionModal = ref(false);
const showDateModal = ref(false);
const showGuestModal = ref(false);

// 지역 관련 상태
const selectedRegionLabel = ref("전체 지역"); // 기본값 변경
const selectedSidoCode = ref<number | null>(null);
const selectedGugunCode = ref<number | null>(null);

// 날짜 선택
const today = new Date();
const dateRange = ref<[Date, Date] | null>(null);
const dateRangeLabel = computed(() => {
  if (!dateRange.value || !dateRange.value[0] || !dateRange.value[1]) return "날짜 선택"; // 기본값 변경
  const [start, end] = dateRange.value;
  const nights = Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
  return `${formatDate(start)} ~ ${formatDate(end)}, ${nights}박`;
});

function formatDate(date: Date | null): string {
  if (!date) return "";
  // YY.MM.DD 형식으로 변경
  return `${date.getFullYear().toString().slice(-2)}.${(date.getMonth() + 1).toString().padStart(2, "0")}.${date.getDate().toString().padStart(2, "0")}`;
}

function formatDatePicker(dates: [Date, Date] | Date): string {
  // 반환 타입 명시
  if (Array.isArray(dates)) {
    if (dates[0] && dates[1]) {
      return `${formatDate(dates[0])} - ${formatDate(dates[1])}`;
    }
    if (dates[0]) {
      return `${formatDate(dates[0])} - `;
    }
  }
  return formatDate(dates as Date);
}

function applyDate() {
  showDateModal.value = false;
  resetAndFetchAccommodations(); // 날짜 변경 시 목록 새로고침
}

// 인원 선택
const guestCount = ref(2);
const guestLabel = computed(() => guestCount.value);
function applyGuest() {
  showGuestModal.value = false;
  resetAndFetchAccommodations(); // 인원 변경 시 목록 새로고침
}

// 카테고리, 정렬
const categories = [
  { label: "호텔·리조트", value: "HOTEL" }, // 이미지에 맞춰 기본 선택 카테고리 변경
  { label: "모텔", value: "MOTEL" },
  { label: "펜션", value: "PENSION" }, // "펜션·풀빌라"에서 "펜션"으로 단순화 (API 값 매칭 고려)
  { label: "프리미엄", value: "PREMIUM" }, // API에 "PREMIUM" 타입이 없다면 매칭되는 다른 값으로 변경 필요
  { label: "캠핑", value: "CAMPING" }, // "캠핑·글램핑"에서 "캠핑"으로 단순화
  { label: "전체", value: "ALL" },
];
const selectedCategory = ref("HOTEL"); // 기본 선택 카테고리 변경
const sortOpen = ref(false);
const sorts = [
  { label: "추천순", value: "RECOMMENDED" },
  { label: "인기순", value: "POPULAR" },
  { label: "가격낮은순", value: "PRICE_ASC" },
  { label: "가격높은순", value: "PRICE_DESC" },
  { label: "리뷰많은순", value: "REVIEW_DESC" },
];
const currentSort = ref("RECOMMENDED");
const sortLabel = computed(() => sorts.find((s) => s.value === currentSort.value)?.label || "추천순");

const accommodations = ref<Accommodation[]>([]);
const loading = ref(false);
const error = ref("");
const router = useRouter();
const route = useRoute();

const currentPage = ref(1);
const itemsPerPage = ref(10); // 한 번에 불러올 아이템 수 증가 (백엔드 페이징에 맞춰 조정)
const hasMoreData = ref(true);
const totalAccommodationsCount = ref(0); // 총 숙소 개수 상태 추가
const loadingMore = ref(false); // 더보기 로딩 상태

function onRegionSelected(region: { sidoCode: number | null; gugunCode: number | null; name: string }) {
  selectedRegionLabel.value = region.name || "지역 선택";
  selectedSidoCode.value = region.sidoCode;
  selectedGugunCode.value = region.gugunCode;
  showRegionModal.value = false;
  resetAndFetchAccommodations();
}

function selectSort(sort: { label: string; value: string }) {
  currentSort.value = sort.value;
  sortOpen.value = false;
  resetAndFetchAccommodations();
}

function selectCategory(categoryValue: string) {
  selectedCategory.value = categoryValue;
  resetAndFetchAccommodations();
}

function resetAndFetchAccommodations() {
  currentPage.value = 1;
  accommodations.value = [];
  hasMoreData.value = true; // 초기화 시 더보기 가능 상태로
  totalAccommodationsCount.value = 0; // 무한 스크롤 시에는 첫 로드에서 전체 카운트를 알 수 없을 수 있으므로, 초기화.
  // 스크롤 위치 초기화 (필요한 경우)
  // window.scrollTo(0, 0); // 이 부분은 UX에 따라 결정
  fetchAccommodations();
}

async function fetchAccommodations(loadMore = false) {
  console.log(
    "fetchAccommodations called. loadMore:",
    loadMore,
    "current page before fetch logic:",
    currentPage.value,
    "loading:",
    loading.value,
    "loadingMore:",
    loadingMore.value,
    "hasMoreData:",
    hasMoreData.value
  );

  let pageToFetch: number;

  if (loadMore) {
    if (loadingMore.value || !hasMoreData.value) {
      console.log("Cannot load more. loadingMore:", loadingMore.value, "hasMoreData:", hasMoreData.value);
      return;
    }
    loadingMore.value = true;
    pageToFetch = currentPage.value + 1;
  } else {
    if (loading.value && accommodations.value.length === 0) {
      console.log("Initial load/filter already in progress and no items loaded yet.");
      return;
    }
    loading.value = true;
    // accommodations.value = []; // resetAndFetchAccommodations에서 이미 처리
    // currentPage.value = 1;     // resetAndFetchAccommodations에서 이미 처리
    pageToFetch = 1;
    // hasMoreData.value = true;  // resetAndFetchAccommodations에서 이미 처리
    // totalAccommodationsCount.value = 0; // resetAndFetchAccommodations에서 이미 처리
  }
  error.value = "";
  console.log("Preparing to fetch accommodations for page:", pageToFetch);

  try {
    const params: any = {
      sidoCode: selectedSidoCode.value,
      gugunCode: selectedGugunCode.value,
      accommodationType: selectedCategory.value === "ALL" ? null : selectedCategory.value,
      guestCount: guestCount.value,
      sortBy: currentSort.value,
      page: pageToFetch, // 계산된 pageToFetch 사용 (1-indexed)
      size: itemsPerPage.value,
    };

    const filteredParams = Object.entries(params)
      .filter(([_, value]) => value !== null && value !== undefined && value !== "")
      .reduce(
        (obj, [key, value]) => {
          obj[key] = value as any;
          return obj;
        },
        {} as Record<string, any>
      );

    console.log("Fetching with params:", filteredParams);
    const response = await axios.get("/api/accommodations/filter", { params: filteredParams });
    console.log("API Response received:", response.data);

    // 백엔드 응답이 { content: [], totalItems: number, totalPages: number, currentPage: number (1-indexed) } 형태라고 가정
    const responseData = response.data;
    const newItems: Accommodation[] = responseData.content || [];

    if (responseData.totalItems !== undefined) {
      totalAccommodationsCount.value = responseData.totalItems;
    }

    if (newItems.length > 0) {
      if (loadMore) {
        accommodations.value = [...accommodations.value, ...newItems];
      } else {
        accommodations.value = newItems;
      }
      // currentPage는 실제 백엔드에서 응답받은 페이지 번호로 업데이트 (일관성 유지)
      currentPage.value = responseData.currentPage || pageToFetch;
    } else {
      // 요청한 페이지에 새로운 아이템이 없는 경우
      if (!loadMore) {
        // 초기 로드였고 아이템이 없다면 목록은 비어있음
        accommodations.value = [];
      }
      // newItems.length === 0 이면, 해당 페이지에서는 더 이상 아이템이 없음을 의미
      // hasMoreData는 아래 totalPages 기준으로 설정됨
    }

    // hasMoreData 업데이트
    if (responseData.totalPages !== undefined && responseData.currentPage !== undefined) {
      hasMoreData.value = responseData.currentPage < responseData.totalPages;
    } else if (responseData.totalItems !== undefined) {
      // totalPages 정보가 없을 경우 totalItems 기준으로 판단
      hasMoreData.value = accommodations.value.length < responseData.totalItems;
    } else {
      // 페이징 정보가 전혀 없는 경우, 현재 받아온 아이템이 요청한 사이즈보다 작으면 마지막 페이지로 간주
      hasMoreData.value = newItems.length === itemsPerPage.value;
    }

    // 찜 상태 초기화 (isWished 필드가 있다면)
    // accommodations.value.forEach(item => {
    //   if (item.isWished === undefined) item.isWished = false;
    // });

    console.log(
      "Fetch successful. Accommodations count:",
      accommodations.value.length,
      "Current page recorded:",
      currentPage.value,
      "Total server items:",
      totalAccommodationsCount.value,
      "Total server pages:",
      responseData.totalPages,
      "Has more data:",
      hasMoreData.value
    );
  } catch (e: any) {
    error.value = e.response?.data?.error || e.message || "숙소 정보를 불러오는 중 오류가 발생했습니다.";
    console.error("Error fetching accommodations:", e);
    if (e.response) {
      console.error("Error response data:", e.response.data);
    }
    // pageToFetch가 currentPage.value + 1로 설정되었으므로, 에러 발생 시 currentPage.value를 직접 변경할 필요는 없음.
    // 다음 '더보기' 시도는 이전 currentPage.value를 기준으로 다시 pageToFetch를 계산함.
    if (!loadMore) accommodations.value = []; // 초기 로드 실패 시 목록 비우기
    hasMoreData.value = false; // 에러 발생 시 더 이상 데이터 없는 것으로 처리
  } finally {
    if (loadMore) {
      loadingMore.value = false;
    } else {
      loading.value = false;
    }
    console.log("fetchAccommodations finished. loading:", loading.value, "loadingMore:", loadingMore.value);
  }
}

function getMinPrice(rooms: Room[] = []): number {
  if (!rooms || rooms.length === 0) return 0;
  const validPrices = rooms
    .map((room) => room.price)
    .filter((price) => price !== null && price !== undefined && price > 0);
  if (validPrices.length === 0) return 0;
  return Math.min(...validPrices);
}

function getMinCapacity(rooms: Room[] = []): number | null {
  if (!rooms || rooms.length === 0) return null;
  const validCapacities = rooms
    .map((room) => room.capacity)
    .filter((cap) => cap !== null && cap !== undefined && cap > 0);
  if (validCapacities.length === 0) return null;
  return Math.min(...validCapacities);
}

function formatPrice(num?: number, noSuffix = false, addSuffixTilde = true) {
  if (num === undefined || num === null || num === Infinity || num === 0) return "가격문의";
  const formattedNum = num.toLocaleString();
  if (noSuffix) return formattedNum;
  return addSuffixTilde ? formattedNum + "원~" : formattedNum; // tilde suffix 제어
}

function goBack() {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push({ name: "Home" });
  }
}

function goToHome() {
  router.push({ name: "Home" });
}

function goToDetail(accommodationId: number) {
  router.push({ name: "AccommodationDetail", params: { id: accommodationId } });
}

// 찜하기 기능 (예시)
function toggleWishlist(accommodationId: number) {
  console.log("Toggle wishlist for:", accommodationId);
  const item = accommodations.value.find((acc) => acc.accommodationId === accommodationId);
  if (item) {
    item.isWished = !item.isWished;
    // TODO: API 호출로 서버에 찜 상태 업데이트
  }
}

// 스크롤 이벤트 핸들러
const handleScroll = () => {
  // document.documentElement.scrollTop: 현재 스크롤된 Y축 위치 (뷰포트 상단 기준)
  // document.documentElement.scrollHeight: 전체 문서의 높이 (스크롤 가능한 전체 영역)
  // document.documentElement.clientHeight: 뷰포트의 높이 (현재 보이는 창의 높이)

  // 스크롤이 페이지 하단에 거의 도달했는지 확인 (예: 하단에서 200px 이내)
  const nearBottom =
    document.documentElement.scrollTop + document.documentElement.clientHeight >=
    document.documentElement.scrollHeight - 200;

  if (nearBottom && hasMoreData.value && !loadingMore.value && !loading.value) {
    console.log("Scrolled near bottom, fetching more accommodations...");
    fetchAccommodations(true);
  }
};

watch(
  route,
  (newRoute, oldRoute) => {
    const queryChanged = JSON.stringify(newRoute?.query) !== JSON.stringify(oldRoute?.query);
    if (queryChanged || !oldRoute) {
      // 쿼리 변경 또는 첫 로드 시
      if (newRoute.query.category && typeof newRoute.query.category === "string") {
        const foundCategory = categories.find((c) => c.value.toUpperCase() === newRoute.query.category.toUpperCase());
        if (foundCategory) selectedCategory.value = foundCategory.value;
        else selectedCategory.value = "HOTEL"; // 기본값 또는 오류 처리
      }
      if (newRoute.query.sort && typeof newRoute.query.sort === "string") {
        const foundSort = sorts.find((s) => s.value.toUpperCase() === newRoute.query.sort.toUpperCase());
        if (foundSort) currentSort.value = foundSort.value;
      }
      // 지역, 날짜, 인원 등의 파라미터 처리 로직 추가 가능
      resetAndFetchAccommodations();
    }
  },
  { immediate: true, deep: true } // deep: true 추가하여 query 내부 변경도 감지 (주의해서 사용)
);

onMounted(() => {
  // watch의 immediate: true로 인해 초기 데이터 로드는 이미 처리될 수 있음.
  // 만약 query 파라미터가 없는 경우(예: 직접 /accommodations 경로로 접근)에도 초기 로드가 필요하면 아래 로직 사용.
  if (Object.keys(route.query).length === 0 && accommodations.value.length === 0) {
    // accommodations.value.length === 0 조건 추가
    selectedCategory.value = "HOTEL"; // 기본 카테고리 설정
    resetAndFetchAccommodations(); // onMounted 시점에는 resetAndFetch를 직접 호출
  }
  window.addEventListener("scroll", handleScroll);
});

onBeforeUnmount(() => {
  window.removeEventListener("scroll", handleScroll);
});
</script>

<style scoped>
/* 스크롤바 숨김 유틸리티 */
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
.scrollbar-hide {
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
}

/* VueDatePicker 스타일 커스텀 */
:deep(.dp-custom-input) {
  /* input-class-name으로 지정한 클래스 사용 */
  font-size: 1rem; /* 16px */
  padding: 0.75rem 1rem; /* 패딩 증가 */
  border-radius: 0.5rem; /* rounded-lg */
  border: 1px solid #d1d5db; /* gray-300 */
  transition:
    border-color 0.2s ease-in-out,
    box-shadow 0.2s ease-in-out;
}
:deep(.dp-custom-input:focus) {
  border-color: #3b82f6; /* blue-500 */
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.3);
}
:deep(.dp-custom-menu) {
  /* menu-class-name으로 지정한 클래스 사용 */
  font-size: 0.9rem;
  border-radius: 0.5rem;
  box-shadow:
    0 10px 15px -3px rgba(0, 0, 0, 0.1),
    0 4px 6px -2px rgba(0, 0, 0, 0.05);
}
:deep(.dp__calendar_header_item),
:deep(.dp__month_year_select) {
  font-size: 0.9rem;
  font-weight: 600;
}
:deep(.dp__action_button) {
  /* 적용, 선택 완료 등 버튼 */
  padding: 8px 16px;
  font-size: 0.9rem;
}
:deep(.dp__active_date),
:deep(.dp__cell_inner:hover) {
  background-color: #3b82f6 !important; /* blue-500 */
  color: white !important;
}
:deep(.dp__today) {
  border: 1px solid #3b82f6; /* blue-500 */
}

/* 추가된 스타일 */
.truncate-2-lines {
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2; /* 원하는 라인 수 */
  -webkit-box-orient: vertical;
  line-height: 1.4; /* 줄 간격에 따라 조정 */
  max-height: calc(1.4em * 2); /* line-height * 라인 수 */
}

/* 모달 트랜지션 (필요시 추가) */
.modal-enter-active,
.modal-leave-active {
  transition:
    opacity 0.3s ease,
    transform 0.3s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

/* 더보기 버튼 로딩 스피너 */
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
</style>
