<template>
  <Layout>
    <div class="container py-5">
      <!-- 헤더/탭 -->
      <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-end mb-4">
        <div>
          <h2 class="fw-bold mb-1">이벤트</h2>
          <div class="text-muted">진행 중인 다양한 혜택을 확인하세요!</div>
        </div>
        <div class="d-flex align-items-center mt-3 mt-md-0">
          <ul class="nav nav-pills me-3">
            <li class="nav-item" v-for="tab in tabs" :key="tab.value">
              <button class="nav-link" :class="{ active: selectedTab === tab.value }" @click="selectedTab = tab.value">
                {{ tab.label }}
              </button>
            </li>
          </ul>
          <router-link v-if="isAdmin" :to="{ name: 'EventCreate' }" class="btn btn-primary">이벤트 등록</router-link>
        </div>
      </div>

      <!-- 이벤트 카드 리스트 -->
      <div class="row g-4">
        <div class="col-12 col-md-6 col-lg-4" v-for="event in filteredEvents" :key="event.id">
          <router-link
            :to="{ name: 'EventDetail', params: { eventId: event.id } }"
            class="text-decoration-none text-dark"
          >
            <div class="card h-100 shadow-sm event-card border-0">
              <div class="ratio ratio-4x3">
                <img :src="event.image" :alt="event.title" class="card-img-top" />
              </div>
              <div class="card-body">
                <h5 class="card-title text-truncate" :title="event.title">
                  {{ event.title }}
                </h5>
                <p class="card-text text-truncate-2">
                  {{ event.summary }}
                </p>
              </div>
              <div class="card-footer bg-white border-0 d-flex justify-content-between align-items-center">
                <span class="badge bg-primary">{{ event.status }}</span>
                <small class="text-muted">{{ event.date }}</small>
              </div>
            </div>
          </router-link>
        </div>
      </div>

      <!-- 이벤트 없을 때 -->
      <div v-if="filteredEvents.length === 0" class="py-5 text-center text-muted fs-5">
        현재 표시할 이벤트가 없습니다.
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import Layout from "@/components/layout/Layout.vue";
import { useUserStore } from "@/store/userStore";
import axios from "axios";

// 유저 store
const userStore = useUserStore();

// 로그인, 권한
const isLoggedIn = computed(() => userStore.isAuthenticated);
const isAdmin = computed(() => userStore.userRole === "ADMIN");
const isHost = computed(() => userStore.userRole === "HOST");

// 탭 목록
const tabs = [
  { label: "전체", value: "ALL" },
  { label: "진행중", value: "진행중" },
  { label: "종료", value: "종료" },
];

// 선택된 탭 상태
const selectedTab = ref("ALL");
const allEvents = ref([]);
const isLoading = ref(true);
const fetchError = ref(null);

// 이벤트 목록을 가져오는 함수
const fetchEvents = async () => {
  isLoading.value = true;
  fetchError.value = null;
  try {
    const response = await axios.get("/api/events");
    allEvents.value = (response.data.result || []).map((event) => {
      // HTML 태그 제거를 위한 간단한 함수
      const stripHtml = (html) => {
        if (!html) return "";
        let doc = new DOMParser().parseFromString(html, "text/html");
        return doc.body.textContent || "";
      };
      const plainContent = stripHtml(event.content);

      return {
        id: event.eventId,
        title: event.title,
        summary: plainContent.substring(0, 100) + (plainContent.length > 100 ? "..." : ""),
        image: event.mainImageUrl,
        status:
          event.status === "ONGOING"
            ? "진행중"
            : event.status === "ENDED"
              ? "종료"
              : event.status === "HIDDEN"
                ? "숨김"
                : event.status,
        date: event.startDate && event.endDate ? `${event.startDate}~${event.endDate}` : "상시 진행",
      };
    });
  } catch (error) {
    console.error("Error fetching events:", error);
    fetchError.value = "이벤트 목록을 불러오는데 실패했습니다.";
  }
  isLoading.value = false;
};

onMounted(() => {
  fetchEvents();
});

const filteredEvents = computed(() => {
  if (fetchError.value) return [];
  if (selectedTab.value === "ALL") return allEvents.value;
  return allEvents.value.filter((e) => e.status === selectedTab.value);
});
</script>

<style scoped>
.event-card {
  transition: box-shadow 0.2s;
}
.event-card:hover {
  box-shadow: 0 0.5rem 1.5rem rgba(30, 60, 180, 0.1);
}
.card-img-top {
  object-fit: contain;
  width: 100%;
  height: 100%;
  background-color: #f8f9fa;
}
.text-truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
