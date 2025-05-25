<template>
  <Header/>
  <section class="search-result">
    <h2 class="title">🔍 '{{ keyword }}' 검색 결과</h2>

    <div v-if="loading && page === 0" class="loading">검색 중입니다...</div>
    <div v-else-if="attractions.length === 0" class="no-result">검색 결과가 없습니다.</div>

    <ul class="attractions-list" v-else>
      <li
        v-for="(item, index) in attractions"
        :key="item.no"
        class="attraction-card"
        :ref="index === attractions.length - 1 ? setLastElementRef : null"
        @click="goToDetail(item.no)"
      >
        <img
          :src="item.image2 === null || item.image2 === '' ? '/src/assets/no-image.jpg' : item.image2"
          :alt="item.title"
          class="preview-image"
        />
        <h3>{{ item.title }}</h3>
        <p>{{ item.addr1 }}</p>
        <p>{{ item.overview?.slice(0, 20) }}...</p>
      </li>
    </ul>

    <div v-if="loading && page > 0" class="loading">더 불러오는 중...</div>
    <div v-if="!hasMore && attractions.length > 0" class="end-text">모든 결과를 불러왔습니다.</div>
  </section>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from "vue";
import { useRoute, useRouter } from "vue-router";
import api from "@/api/index";
import Header from '@/components/layout/Header.vue'
import { useScrollStore } from '@/store/useScrollStore'

// 상태
const route = useRoute();
const keyword = ref(route.query.keyword || "");
const attractions = ref([]);
const loading = ref(false);
const page = ref(0);
const size = 10;
const hasMore = ref(true);
const lastElement = ref(null);
const router = useRouter();

const scrollStore = useScrollStore()

// API 요청
const fetchSearchResults = async () => {
  if (!keyword.value.trim() || loading.value || !hasMore.value) return;

  loading.value = true;
  try {
    const response = await api.api({
      url: `/api/map/attractions/search/${keyword.value}`,
      method: "get",
      params: {
        page: page.value,
        size,
      },
    });

    const result = response.data;

    if (result.length < size) {
      hasMore.value = false;
    }

    attractions.value.push(...result);
    page.value += 1;
  } catch (err) {
    console.error("검색 실패:", err);
    hasMore.value = false;
  } finally {
    loading.value = false;
  }
};

const goToDetail = (id) => {
scrollStore.saveState({
  keyword: keyword.value,
  page:    page.value,
  items:   attractions.value,
  scrollY: window.scrollY
  })

  router.push(`/attractions/${id}`);
};

// 마지막 요소가 viewport에 들어오면 더 로드
const setLastElementRef = (el) => {
  lastElement.value = el;
};

const observeLastElement = () => {
  const observer = new IntersectionObserver(
    (entries) => {
      const [entry] = entries;
      if (entry.isIntersecting) {
        fetchSearchResults();
      }
    },
    { threshold: 1.0 }
  );

  watch(
    lastElement,
    (newEl, oldEl) => {
      if (oldEl) observer.unobserve(oldEl);
      if (newEl) observer.observe(newEl);
    },
    { flush: "post" }
  );
};

onMounted(() => {
  if (scrollStore.keyword === route.query.keyword) {
    attractions.value = [...scrollStore.items]
    page.value        = scrollStore.page
    hasMore.value     = scrollStore.items.length % size === 0
    
    setTimeout(() => window.scrollTo(0, scrollStore.scrollY), 0)
    } else {
      scrollStore.clearState()
      resetSearch()
    }
  observeLastElement();
});

watch(
  () => route.query.keyword,
  (newKeyword) => {
    scrollStore.clearState()
    keyword.value = newKeyword;
    resetSearch();
  }
);

const resetSearch = () => {
  attractions.value = [];
  page.value = 0;
  hasMore.value = true;
  fetchSearchResults();
};
</script>

<style scoped>
.search-result {
  max-width: 800px;
  margin: 2rem auto;
  padding: 1rem;
}
.title {
  font-size: 1.5rem;
  margin-bottom: 1rem;
}
.loading,
.no-result,
.end-text {
  text-align: center;
  color: #888;
  margin: 2rem 0;
}
.attractions-list {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1rem;
  list-style: none;
  padding: 0;
  margin: 0;
}

.attraction-card {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 1rem;
  background: #f9f9f9;
  cursor: pointer;
}

.preview-image {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: 4px;
  margin-bottom: 0.5rem;
}
</style>
