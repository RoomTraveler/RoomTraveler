<template>
  <div class="container mt-5 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="mb-0"><i class="bi bi-collection-fill me-2"></i>내 숙소 관리</h1>
      <RouterLink
        v-if="host?.hostId && host?.status === 'ACTIVE'"
        to="/host/accommodations/new"
        class="btn btn-primary"
      >
        <i class="bi bi-plus-circle-fill me-1"></i> 새 숙소 등록
      </RouterLink>
    </div>

    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status"><span class="visually-hidden">로딩 중...</span></div>
    </div>
    <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
    <div v-else-if="!host?.hostId || host?.status !== 'ACTIVE'" class="alert alert-warning text-center">
      <p v-if="!host?.hostId">호스트 정보를 불러올 수 없거나 아직 호스트로 등록되지 않았습니다.</p>
      <p v-else-if="host?.status === 'WAIT'">호스트 승인 대기 중입니다. 승인 후 숙소를 등록하고 관리할 수 있습니다.</p>
      <p v-else-if="host?.status === 'REJECT'">호스트 등록이 반려되었습니다. 호스트 정보를 확인해주세요.</p>
      <RouterLink v-if="!host?.hostId" to="/host/new" class="btn btn-success mt-2">호스트 등록하기</RouterLink>
      <RouterLink v-else :to="`/host/detail/${host.hostId}`" class="btn btn-info mt-2">내 호스트 정보 보기</RouterLink>
    </div>
    <div v-else>
      <!-- 알림 메시지 -->
      <div v-if="routeMessage" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ routeMessage }}
        <button type="button" class="btn-close" @click="clearRouteMessage" aria-label="Close"></button>
      </div>

      <!-- 숙소 통계 -->
      <div class="row g-3 mb-4">
        <div class="col-md-3 col-6">
          <SummaryCard title="총 숙소" :value="allAccommodations.length" icon="bi-houses" color="primary" />
        </div>
        <div class="col-md-3 col-6">
          <SummaryCard title="운영중 숙소" :value="activeAccommodationsCount" icon="bi-house-check" color="success" />
        </div>
        <div class="col-md-3 col-6">
          <SummaryCard
            title="검토중 숙소"
            :value="pendingAccommodationsCount"
            icon="bi-house-exclamation"
            color="warning"
          />
        </div>
        <div class="col-md-3 col-6">
          <SummaryCard title="총 객실 수" :value="totalRoomsCount" icon="bi-door-open" color="info" />
        </div>
      </div>

      <!-- 숙소 필터 -->
      <div class="card card-body mb-4">
        <div class="row g-2 align-items-end">
          <div class="col-md-4">
            <label for="filterTitle" class="form-label">숙소명 검색</label>
            <input
              type="text"
              id="filterTitle"
              class="form-control"
              v-model="filters.title"
              placeholder="숙소 이름으로 검색..."
            />
          </div>
          <div class="col-md-3">
            <label for="filterStatus" class="form-label">상태</label>
            <select id="filterStatus" class="form-select" v-model="filters.status">
              <option value="">전체 상태</option>
              <option value="ACTIVE">운영중</option>
              <option value="INACTIVE">비활성</option>
              <option value="PENDING_REVIEW">검토중</option>
              <option value="REJECTED">반려됨</option>
            </select>
          </div>
          <div class="col-md-3">
            <label for="filterSort" class="form-label">정렬</label>
            <select id="filterSort" class="form-select" v-model="filters.sortBy">
              <option value="createdAtDesc">최신 등록순</option>
              <option value="titleAsc">이름 오름차순</option>
              <option value="reviewCountDesc">리뷰 많은순</option>
            </select>
          </div>
          <div class="col-md-2">
            <button class="btn btn-secondary w-100" @click="resetFilters">
              <i class="bi bi-arrow-clockwise"></i> 초기화
            </button>
          </div>
        </div>
      </div>

      <!-- 숙소 목록 -->
      <div v-if="paginatedAccommodations.length === 0" class="alert alert-light text-center">
        <i class="bi bi-info-circle me-1"></i> 표시할 숙소가 없습니다.
        <span v-if="filters.title || filters.status">다른 조건으로 검색해보세요.</span>
        <span v-else>새로운 숙소를 등록해보세요!</span>
      </div>
      <div v-else class="row g-4">
        <div
          v-for="accommodation in paginatedAccommodations"
          :key="accommodation.accommodationId"
          class="col-md-6 col-lg-4"
        >
          <AccommodationCard
            :accommodation="accommodation"
            :host-id="host.hostId"
            @deleted="triggerLoadAccommodations"
            @status-updated="triggerLoadAccommodations"
          />
        </div>
      </div>

      <!-- 페이지네이션 -->
      <div v-if="totalPages > 1" class="d-flex justify-content-center mt-4">
        <nav aria-label="Page navigation">
          <ul class="pagination">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">&laquo;</a>
            </li>
            <li v-for="page in totalPages" :key="page" class="page-item" :class="{ active: currentPage === page }">
              <a class="page-link" href="#" @click.prevent="changePage(page)">{{ page }}</a>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">&raquo;</a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { useRoute, useRouter, RouterLink } from "vue-router";
import { useUserStore } from "@/store/userStore.js";
import AccommodationCard from "@/components/host/HostAccommodationCard.vue";
import SummaryCard from "@/components/common/SummaryCard.vue";
import { getHostAccommodations } from "@/api/hostApi.js";
import apiUtils from "@/api/index";
import { ElMessage } from "element-plus";

const { api } = apiUtils;

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const host = ref(null);
const allAccommodations = ref([]);
const loading = ref(true);
const error = ref("");
const routeMessage = ref(route.query.message || "");

const filters = ref({
  title: route.query.title || "",
  status: route.query.status || "",
  sortBy: route.query.sortBy || "createdAtDesc",
});
const currentPage = ref(parseInt(route.query.page) || 1);
const itemsPerPage = 12;
const totalDbItems = ref(0);

const clearRouteMessage = () => {
  routeMessage.value = "";
  const { message, ...queryWithoutMessage } = route.query;
  router.replace({ query: queryWithoutMessage });
};

// 실제 API만 사용하는 함수로 변경
async function fetchHostAndAccommodations() {
  if (!userStore.isAuthenticated || !userStore.user?.id) {
    loading.value = false;
    host.value = null;
    allAccommodations.value = [];
    totalDbItems.value = 0;
    return;
  }

  loading.value = true;
  error.value = "";
  try {
    if (!host.value || host.value.userId !== userStore.user.id) {
      const hostResponse = await api.get(`/api/host/user/${userStore.user.id}`);
      host.value = hostResponse.data;
    }

    if (!host.value) {
      allAccommodations.value = [];
      totalDbItems.value = 0;
      loading.value = false;
      return;
    }

    if (host.value && host.value.status === "ACTIVE") {
      // 실제 API만 호출
      const response = await getHostAccommodations(
        host.value.hostId,
        filters.value.status || null,
        filters.value.title || null,
        currentPage.value,
        itemsPerPage
      );
      allAccommodations.value = response.content || [];
      totalDbItems.value = response.totalElements || 0;
    } else {
      allAccommodations.value = [];
      totalDbItems.value = 0;
    }
  } catch (e) {
    if (e.response && e.response.status === 404 && e.config.url.includes("/api/host/user/")) {
      host.value = null;
      error.value = "호스트 정보를 찾을 수 없습니다. 호스트로 등록해주세요.";
    } else {
      error.value = e.message || "데이터 로드 중 오류 발생";
      console.error("Error fetching host details or accommodations:", e);
    }
    allAccommodations.value = [];
    totalDbItems.value = 0;
  } finally {
    loading.value = false;
  }
}

const triggerLoadAccommodations = () => {
  fetchHostAndAccommodations();
};

const activeAccommodationsCount = computed(
  () => allAccommodations.value.filter((acc) => acc.status === "ACTIVE").length
);
const pendingAccommodationsCount = computed(
  () => allAccommodations.value.filter((acc) => acc.status === "PENDING_REVIEW").length
);
const totalRoomsCount = computed(() => allAccommodations.value.reduce((sum, acc) => sum + (acc.rooms?.length || 0), 0));

const filteredAndSortedAccommodations = computed(() => {
  let accommodationsToDisplay = [...allAccommodations.value];

  accommodationsToDisplay.sort((a, b) => {
    if (filters.value.sortBy === "titleAsc") {
      return a.title.localeCompare(b.title);
    }
    if (filters.value.sortBy === "reviewCountDesc") {
      return (b.reviewCount || 0) - (a.reviewCount || 0);
    }
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
  });
  return accommodationsToDisplay;
});

const totalPages = computed(() => Math.ceil(totalDbItems.value / itemsPerPage));

const paginatedAccommodations = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage;
  const end = start + itemsPerPage;
  return filteredAndSortedAccommodations.value.slice(start, end);
});

function changePage(page) {
  if (page >= 1 && page <= totalPages.value && page !== currentPage.value) {
    currentPage.value = page;
    updateRouterQuery();
  }
}

function resetFilters() {
  filters.value.title = "";
  filters.value.status = "";
  filters.value.sortBy = "createdAtDesc";
  currentPage.value = 1;
  updateRouterQuery();
}

function updateRouterQuery() {
  const query = {};
  if (filters.value.title) query.title = filters.value.title;
  if (filters.value.status) query.status = filters.value.status;
  if (filters.value.sortBy && filters.value.sortBy !== "createdAtDesc") query.sortBy = filters.value.sortBy;
  if (currentPage.value > 1) query.page = currentPage.value.toString();

  if (routeMessage.value && route.query.message) {
    query.message = route.query.message;
  }

  router.push({ query: Object.keys(query).length > 0 ? query : {} });
}

// userStore 또는 쿼리 변경에 따라 호출
watch(
  () => [userStore.isAuthenticated, userStore.user?.id],
  async ([isAuth, userId]) => {
    if (isAuth && userId) {
      if (!route.query.page && !route.query.title && !route.query.status && !route.query.sortBy) {
        await fetchHostAndAccommodations();
      }
    } else {
      loading.value = false;
      host.value = null;
      allAccommodations.value = [];
      totalDbItems.value = 0;
      error.value = isAuth ? "사용자 ID를 찾을 수 없습니다." : "";
    }
  },
  { immediate: true, deep: true }
);

watch(
  () => route.query,
  async (newQuery, oldQuery) => {
    const newPage = parseInt(newQuery.page) || 1;
    const newTitle = newQuery.title || "";
    const newStatus = newQuery.status || "";
    const newSortBy = newQuery.sortBy || "createdAtDesc";

    let needsDataFetch = false;

    if (currentPage.value !== newPage) {
      currentPage.value = newPage;
      needsDataFetch = true;
    }
    if (filters.value.title !== newTitle) {
      filters.value.title = newTitle;
      needsDataFetch = true;
    }
    if (filters.value.status !== newStatus) {
      filters.value.status = newStatus;
      needsDataFetch = true;
    }
    if (filters.value.sortBy !== newSortBy) {
      filters.value.sortBy = newSortBy;
    }

    if (needsDataFetch && userStore.isAuthenticated && userStore.user?.id) {
      await fetchHostAndAccommodations();
    } else if (needsDataFetch && (!userStore.isAuthenticated || !userStore.user?.id)) {
      allAccommodations.value = [];
      totalDbItems.value = 0;
    }
  },
  { deep: true, immediate: true }
);
</script>

<style scoped>
.alert {
  font-size: 0.95rem;
}
</style>
