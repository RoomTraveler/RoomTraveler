<template>
  <div class="min-h-screen bg-gray-50 pb-24">
    <!-- 상단 필터바 -->
    <div class="sticky top-0 z-30 bg-white border-b px-4 py-3">
      <div class="flex gap-3 items-center overflow-x-auto no-scrollbar mb-2">
        <button
            v-for="cat in categories"
            :key="cat"
            :class="[
            'px-4 py-1.5 rounded-full text-sm whitespace-nowrap',
            selectedCategory === cat
              ? 'bg-blue-600 text-white font-bold'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
            @click="selectedCategory = cat"
        >
          {{ cat }}
        </button>
      </div>
      <div class="flex items-center gap-4 mt-2">
        <button class="flex items-center gap-1 text-sm font-medium">
          <span>지역</span>
          <svg class="w-4 h-4 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path d="M19 9l-7 7-7-7" strokeWidth="2" strokeLinecap="round"/>
          </svg>
        </button>
        <button class="flex items-center gap-1 text-sm font-medium">
          <span>날짜</span>
          <svg class="w-4 h-4 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path d="M19 9l-7 7-7-7" strokeWidth="2" strokeLinecap="round"/>
          </svg>
        </button>
        <button class="flex items-center gap-1 text-sm font-medium">
          <span>인원</span>
          <svg class="w-4 h-4 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path d="M19 9l-7 7-7-7" strokeWidth="2" strokeLinecap="round"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 숙소 카드 리스트 -->
    <div class="max-w-7xl mx-auto px-4 py-6">
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <div
            v-for="hotel in hotels"
            :key="hotel.id"
            class="bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow"
        >
          <div class="relative aspect-[4/3]">
            <img
                :src="hotel.image"
                :alt="hotel.name"
                class="w-full h-full object-cover"
            />
            <div class="absolute top-3 left-3 flex gap-1.5">
              <span v-if="hotel.special"
                    class="bg-rose-500 text-white px-2 py-0.5 rounded-sm text-xs font-bold">특가</span>
              <span v-if="hotel.new"
                    class="bg-yellow-500 text-white px-2 py-0.5 rounded-sm text-xs font-bold">신규</span>
            </div>
          </div>
          <div class="p-4">
            <div class="flex items-center gap-2 text-xs mb-1.5">
              <span class="text-gray-500">{{ hotel.type }}</span>
              <span class="text-blue-600 font-medium">{{ hotel.region }}</span>
            </div>
            <h3 class="font-bold text-base mb-2 truncate">{{ hotel.name }}</h3>
            <div class="flex flex-wrap gap-1 mb-3">
              <span
                  v-for="tag in hotel.tags"
                  :key="tag"
                  class="bg-gray-100 text-gray-700 px-2 py-0.5 rounded text-xs"
              >{{ tag }}</span>
            </div>
            <div class="flex items-center justify-between">
              <div>
                <span class="text-lg font-bold text-rose-500">{{ hotel.price.toLocaleString() }}원</span>
                <span class="text-xs text-gray-500">/1박</span>
              </div>
              <div class="flex items-center gap-1 text-yellow-500">
                <span>★</span>
                <span class="text-sm font-medium">{{ hotel.rating }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 지도 버튼 -->
    <button
        class="fixed bottom-6 left-1/2 -translate-x-1/2 bg-blue-600 text-white shadow-lg rounded-full
             px-6 py-3 font-bold hover:bg-blue-700 transition-colors flex items-center gap-2"
    >
      <span class="text-xl">🗺</span>
      지도 보기
    </button>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const categories = ['전체', '호텔', '리조트', '펜션', '풀빌라', '게스트하우스', '한옥']
const selectedCategory = ref('호텔')

const hotels = [
  {
    id: 1,
    name: '그랜드 호텔 서울',
    type: '호텔',
    region: '강남/역삼',
    image: 'https://via.placeholder.com/400x300',
    price: 89000,
    rating: 4.8,
    tags: ['조식포함', '수영장', '피트니스'],
    special: true,
    new: false,
  },
  // ... 더 많은 호텔 데이터
]
</script>

<style scoped>
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
</style><template>
  <div class="min-h-screen bg-gray-50 pb-24">
    <!-- 상단 필터바 -->
    <div class="sticky top-0 z-30 bg-white border-b px-4 py-3">
      <div class="flex gap-3 items-center overflow-x-auto no-scrollbar mb-2">
        <button
          v-for="cat in categories"
          :key="cat"
          :class="[
            'px-4 py-1.5 rounded-full text-sm whitespace-nowrap',
            selectedCategory === cat
              ? 'bg-blue-600 text-white font-bold'
              : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
          ]"
          @click="selectedCategory = cat"
          :aria-label="`필터: ${cat}`"
        >
          {{ cat }}
        </button>
      </div>
    </div>

    <!-- 숙소 카드 리스트 -->
    <div class="max-w-7xl mx-auto px-4 py-6">
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <div
          v-for="hotel in hotels"
          :key="hotel.id"
          class="bg-white rounded-2xl overflow-hidden shadow-sm hover:shadow-md transition-shadow"
        >
          <div class="relative aspect-[4/3]">
            <img
              :src="hotel.image"
              :alt="hotel.name"
              class="w-full h-full object-cover"
              loading="lazy"
            />
            <div class="absolute top-3 left-3 flex gap-1.5">
              <span v-if="hotel.special"
                class="bg-rose-500 text-white px-2 py-0.5 rounded-sm text-xs font-bold">특가</span>
              <span v-if="hotel.new"
                class="bg-yellow-500 text-white px-2 py-0.5 rounded-sm text-xs font-bold">신규</span>
            </div>
          </div>
          <div class="p-4">
            <h3 class="font-bold text-base mb-2 truncate">{{ hotel.name }}</h3>
            <div class="flex items-center justify-between">
              <div>
                <span class="text-lg font-bold text-rose-500">{{ hotel.price.toLocaleString() }}원</span>
                <span v-if="hotel.discount" class="text-xs text-gray-500 line-through ml-2">
                  {{ hotel.originalPrice.toLocaleString() }}원
                </span>
              </div>
              <div class="flex items-center gap-1 text-yellow-500">
                <span>★</span>
                <span class="text-sm font-medium">{{ hotel.rating }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 지도 버튼 -->
    <button
      class="fixed bottom-6 left-1/2 -translate-x-1/2 bg-blue-600 text-white shadow-lg rounded-full
             px-6 py-3 font-bold hover:bg-blue-700 transition-transform transform hover:scale-105 flex items-center gap-2"
    >
      <span class="text-xl">🗺</span>
      지도 보기
    </button>
  </div>
</template>