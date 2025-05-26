<template>
  <Layout>
    <div v-if="isLoading" class="container py-5 text-center"><p>로딩 중...</p></div>
    <div v-if="fetchError" class="container py-5 text-center text-danger">
      <p>{{ fetchError }}</p>
    </div>
    <div v-if="!isLoading && !fetchError && event" class="container py-5">
      <div class="row justify-content-center">
        <div class="col-lg-8">
          <div class="card shadow">
            <!--
            <img
              :src="event.image"
              class="card-img-top"
              :alt="event.title"
              style="object-fit: cover; max-height: 340px"
              @error="
                (e) => {
                  e.target.src = defaultImage;
                }
              "
            />
            -->
            <div class="card-body">
              <h2 class="card-title mb-2">{{ event.title }}</h2>
              <div class="mb-3">
                <span class="badge bg-primary me-2">{{ event.status }}</span>
                <span class="text-muted">{{ event.date }}</span>
              </div>
              <div class="card-text mb-4" v-html="event.content"></div>
              <div class="d-flex justify-content-between align-items-center">
                <button class="btn btn-outline-secondary" @click="$router.push({ name: 'Event' })">목록으로</button>
                <div v-if="isAdmin">
                  <router-link
                    :to="{ name: 'EventUpdate', params: { eventId: event.id } }"
                    class="btn btn-sm btn-outline-primary me-2"
                    >수정</router-link
                  >
                  <button @click="deleteEvent" class="btn btn-sm btn-outline-danger" :disabled="isDeleting">
                    <span
                      v-if="isDeleting"
                      class="spinner-border spinner-border-sm"
                      role="status"
                      aria-hidden="true"
                    ></span>
                    삭제
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!isLoading && !event && !fetchError" class="container py-5 text-center">
      <p>해당 이벤트를 찾을 수 없습니다.</p>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import Layout from "@/components/layout/Layout.vue";
import apiModule from "@/api";
import { useUserStore } from "@/store/userStore";
import defaultImagePlaceholder from "@/assets/no-image.jpg";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const isAdmin = computed(() => userStore.userRole === "ADMIN");

const event = ref(null);
const isLoading = ref(true);
const fetchError = ref(null);
const isDeleting = ref(false);
const defaultImage = defaultImagePlaceholder;

const api = apiModule.api;
const apiNoAuth = apiModule.apiNoAuth;

const fetchEventDetail = async () => {
  isLoading.value = true;
  fetchError.value = null;
  try {
    const eventId = route.params.eventId;
    const response = await apiNoAuth.get(`/api/events/${eventId}`);

    if (response.data && response.data.success) {
      const actualEventData = response.data.result;

      if (actualEventData) {
        // let imageUrl = defaultImage; // 이전 로직 주석 처리 또는 삭제

        // API 응답 구조에 맞춰 이미지 URL 추출 시도 (이전 로직 주석 처리 또는 삭제)
        // if (actualEventData.images && actualEventData.images.images) { ... }

        event.value = {
          id: actualEventData.eventId,
          title: actualEventData.title,
          content: actualEventData.content,
          image: actualEventData.mainImageUrl || defaultImage, // 수정: mainImageUrl 직접 사용
          status:
            actualEventData.status === "ONGOING"
              ? "진행중"
              : actualEventData.status === "ENDED"
                ? "종료"
                : actualEventData.status === "HIDDEN"
                  ? "숨김"
                  : actualEventData.status,
          date:
            actualEventData.startDate && actualEventData.endDate
              ? `${actualEventData.startDate}~${actualEventData.endDate}`
              : "상시 진행",
        };
      } else {
        fetchError.value = "이벤트 정보를 가져오는데 실패했습니다. (데이터 없음)";
        event.value = null;
      }
    } else {
      fetchError.value = response.data?.message || "이벤트 정보를 가져오는데 실패했습니다.";
      event.value = null;
    }
  } catch (error) {
    console.error("Error fetching event detail:", error);
    if (error.response && error.response.status === 404) {
      fetchError.value = "해당 이벤트를 찾을 수 없습니다.";
    } else {
      fetchError.value = "이벤트 정보를 불러오는데 실패했습니다.";
    }
    event.value = null;
  }
  isLoading.value = false;
};

const deleteEvent = async () => {
  console.log("deleteEvent function called. Event ID:", event.value?.id);
  if (!event.value || !event.value.id) {
    console.log("Event data or ID is missing.");
    return;
  }
  if (confirm("정말로 이 이벤트를 삭제하시겠습니까?")) {
    isDeleting.value = true;
    try {
      await api.delete(`/api/events/${event.value.id}`);
      alert("이벤트가 삭제되었습니다.");
      router.push({ name: "Event" });
    } catch (error) {
      console.error("Error deleting event:", error);
      alert("이벤트 삭제 중 오류가 발생했습니다.");
    }
    isDeleting.value = false;
  } else {
    console.log("User cancelled deletion.");
  }
};

onMounted(() => {
  fetchEventDetail();
});
</script>

<style scoped>
/* 기존 스타일이 있다면 여기에 추가됩니다. */

/* v-html로 삽입되는 컨텐츠 내의 이미지 크기 조절 */
:deep(.card-text img) {
  max-width: 100%;
  height: auto; /* 이미지 비율 유지를 위해 */
  display: block; /* 이미지가 인라인 요소처럼 동작하는 것을 방지 */
  margin-bottom: 0.5rem; /* 이미지 아래 약간의 여백 추가 (선택 사항) */
}
</style>
