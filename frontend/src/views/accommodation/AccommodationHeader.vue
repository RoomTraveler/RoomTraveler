<template>
  <header class="flex items-center justify-between px-3 h-[44px] bg-white border-b border-gray-200 sticky top-0 z-50">
    <button class="p-1" @click="goBack">
      <i class="bi bi-arrow-left-short text-2xl text-gray-700"></i>
    </button>
    <span class="font-semibold text-lg text-gray-800">{{ title }}</span>
    <div class="flex items-center gap-3">
      <button @click="goToHome" class="p-1">
        <i class="bi bi-house text-xl text-gray-700"></i>
      </button>
      <button @click="goToCart" class="p-1">
        <i class="bi bi-cart3 text-xl text-gray-700"></i>
      </button>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';

defineProps<{
  title: string;
}>();

const router = useRouter();

function goBack() {
  if (window.history.length > 1 && 
      document.referrer && 
      new URL(document.referrer).hostname === window.location.hostname) {
    router.back();
  } else {
    // document.referrer가 없거나 외부 사이트에서 온 경우 홈으로 이동
    router.push({ name: 'Home' }); // 'Home' 라우트 이름이 있다고 가정
  }
}

function goToHome() {
  router.push({ name: 'Home' }); // 'Home' 라우트 이름이 있다고 가정
}

function goToCart() {
  router.push({ name: 'Cart' }); // 'Cart' 라우트 이름이 있다고 가정
}
</script>

<style scoped>
/* 필요한 경우 여기에 스타일 추가 */
</style> 