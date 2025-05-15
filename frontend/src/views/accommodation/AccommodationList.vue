<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 헤더 -->
    <header class="flex items-center justify-between px-4 py-3 bg-white shadow">
      <!-- 뒤로가기 -->
      <button aria-label="뒤로가기" class="p-2">
        <svg class="w-6 h-6 text-gray-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path d="M15 18l-6-6 6-6" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <!-- 페이지 제목 -->
      <h1 class="text-lg font-semibold text-gray-800">호텔/리조트</h1>
      <!-- 마이페이지 & 장바구니 -->
      <div class="flex items-center gap-4">
        <button class="text-sm text-gray-700">마이페이지</button>
        <button class="text-sm text-gray-700">장바구니</button>
      </div>
    </header>

    <!-- 필터 카드 3개 -->
    <div class="flex px-4 py-3 bg-white shadow mt-2 gap-2">
      <!-- 지역 선택 -->
      <Dropdown
          class="flex-1"
          label="지역"
          :options="regions"
          v-model="selectedRegion"
      />
      <!-- 날짜 선택 -->
      <Dropdown
          class="flex-1"
          label="날짜"
          :options="dates"
          v-model="selectedDate"
      />
      <!-- 인원 선택 -->
      <Dropdown
          class="flex-1"
          label="인원"
          :options="guests"
          v-model="selectedGuest"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const regions = ['전체', '강남/역삼', '홍대/연남', '제주', '부산']
const dates = ['날짜 선택', '5/20 - 5/21', '5/21 - 5/22', '5/22 - 5/23']
const guests = ['인원 선택', '1명', '2명', '3명', '4명 이상']

const selectedRegion = ref(regions[0])
const selectedDate   = ref(dates[0])
const selectedGuest  = ref(guests[0])
</script>

<style scoped>
/* 필요 시 스크롤바 숨김 */
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
</style>
