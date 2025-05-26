<template>
  <div class="container mt-4 mb-5">
    <div v-if="loading && !stats" class="text-center py-5">
      <!-- stats가 없을 때만 전체 로딩 표시 -->
      <div class="spinner-border text-primary" style="width: 3rem; height: 3rem" role="status">
        <span class="visually-hidden">대시보드 로딩 중...</span>
      </div>
      <p class="mt-2 text-muted">데이터를 불러오고 있습니다.</p>
    </div>
    <div v-else-if="error" class="alert alert-danger">
      <h4 class="alert-heading">오류 발생</h4>
      <p>{{ error }}</p>
      <hr />
      <p class="mb-0">페이지를 새로고침하거나 잠시 후 다시 시도해주세요.</p>
    </div>
    <div v-else-if="stats">
      <h2 class="mb-4">호스트 대시보드: {{ stats.host?.businessName || "호스트" }}</h2>

      <!-- 통계 요약 -->
      <div class="row g-4 mb-4">
        <div class="col-md-3 col-6">
          <SummaryCard title="총 숙소 수" :value="stats.accommodationCount" icon="bi-house-door-fill" color="primary" />
        </div>
        <div class="col-md-3 col-6">
          <SummaryCard
            title="총 예약 건수"
            :value="stats.totalReservations"
            icon="bi-calendar-check-fill"
            color="info"
          />
        </div>
        <div class="col-md-3 col-6">
          <SummaryCard
            title="확정/완료된 예약"
            :value="(stats.confirmedReservations || 0) + (stats.completedReservations || 0)"
            icon="bi-patch-check-fill"
            color="success"
          />
        </div>
        <div class="col-md-3 col-6">
          <SummaryCard
            title="총 예상 수익"
            :value="formatCurrency(stats.totalRevenue)"
            icon="bi-cash-coin"
            color="warning"
          />
        </div>
      </div>

      <!-- 숙소 목록 -->
      <div class="card shadow-sm">
        <div class="card-header bg-light">
          <h5 class="mb-0"><i class="bi bi-list-ul me-2"></i>내 숙소 목록 ({{ accommodations.length }}개)</h5>
        </div>
        <div class="card-body">
          <div v-if="accommodationsLoading" class="text-center">
            <div class="spinner-border spinner-border-sm text-secondary" role="status"></div>
            <span class="ms-2 text-muted">숙소 목록 로딩 중...</span>
          </div>
          <div v-else-if="accommodationsError" class="alert alert-light text-danger py-2">
            {{ accommodationsError }}
          </div>
          <div v-else-if="accommodations.length === 0" class="text-center text-muted py-3">
            등록된 숙소가 없습니다.
            <!-- 호스트 상태가 ACTIVE일 때만 새 숙소 등록 버튼 표시 (stats.host.status 확인) -->
            <RouterLink
              v-if="stats.host && stats.host.status === 'ACTIVE'"
              to="/host/accommodations/new"
              class="btn btn-sm btn-primary mt-2"
              >새 숙소 등록하기</RouterLink
            >
          </div>
          <div class="table-responsive" v-else>
            <table class="table table-hover align-middle">
              <thead class="table-light">
                <tr>
                  <th scope="col">숙소명</th>
                  <th scope="col">주소</th>
                  <th scope="col">상태</th>
                  <th scope="col">총 예약</th>
                  <!-- 이 부분은 stats.reservations 를 파싱해야 함 -->
                  <th scope="col">확정/완료</th>
                  <!-- 이 부분은 stats.reservations 를 파싱해야 함 -->
                  <th scope="col">객실 관리</th>
                  <th scope="col">통계/수정</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="acc in accommodations" :key="acc.accommodationId">
                  <td>
                    <strong>{{ acc.title }}</strong>
                  </td>
                  <td>{{ acc.address }}</td>
                  <td>
                    <span :class="getAccommodationStatusBadge(acc.status)">{{
                      getAccommodationStatusText(acc.status)
                    }}</span>
                  </td>
                  <td>{{ getReservationCountForAccommodation(acc.accommodationId, null) }}건</td>
                  <td>{{ getReservationCountForAccommodation(acc.accommodationId, ["CONFIRMED", "COMPLETED"]) }}건</td>
                  <td>
                    <RouterLink
                      :to="{ name: 'HostRoomList', params: { accommodationId: acc.accommodationId } }"
                      class="btn btn-sm btn-outline-info"
                    >
                      <i class="bi bi-door-open"></i> 객실
                    </RouterLink>
                  </td>
                  <td>
                    <RouterLink
                      :to="`/host/dashboard/accommodation/${acc.accommodationId}`"
                      class="btn btn-sm btn-outline-primary me-1"
                    >
                      <i class="bi bi-bar-chart-steps"></i> 통계
                    </RouterLink>
                    <RouterLink
                      :to="{ name: 'HostAccommodationEdit', params: { accommodationId: acc.accommodationId } }"
                      class="btn btn-sm btn-outline-warning"
                    >
                      <i class="bi bi-pencil-square"></i> 수정
                    </RouterLink>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="!loading && !stats && !error" class="alert alert-info">
      대시보드 정보를 표시할 수 없습니다. 호스트로 등록되어 있고 승인되었는지 확인해주세요.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useUserStore } from "@/store/userStore";
import { useRouter } from "vue-router"; // Vue Router import
import SummaryCard from "@/components/common/SummaryCard.vue";
import "bootstrap-icons/font/bootstrap-icons.css";
import { ElRow, ElCol, ElCard, ElButton, ElIcon, ElMessage } from "element-plus";
import { OfficeBuilding, List, CircleCheckFilled, Coin } from "@element-plus/icons-vue";
// Element Plus 컴포넌트는 현재 직접 사용하지 않으므로 주석 처리 또는 필요시 추가
// import { ElRow, ElCol, ElCard, ElSkeleton, ElEmpty, ElButton, ElProgress, ElAvatar } from "element-plus";

const userStore = useUserStore();
const router = useRouter(); // router 인스턴스 사용

// 상태 변수들
const loading = ref(true); // 전체 대시보드 로딩 상태
const error = ref(""); // 전체 대시보드 에러 메시지
const stats = ref(null); // 대시보드 통계 데이터 (DashboardStats 객체)

const accommodations = ref([]); // 숙소 목록
const accommodationsLoading = ref(false); // 숙소 목록 로딩 상태
const accommodationsError = ref(""); // 숙소 목록 에러 메시지

const hostId = computed(() => userStore.user?.hostId);

// 헬퍼 함수: 통화 형식 변환
const formatCurrency = (amount) =>
  new Intl.NumberFormat("ko-KR", { style: "currency", currency: "KRW", maximumFractionDigits: 0 }).format(amount || 0);

// 헬퍼 함수: 숙소 상태에 따른 배지 클래스 반환
const getAccommodationStatusBadge = (status) => {
  const map = {
    ACTIVE: "bg-success text-white",
    INACTIVE: "bg-secondary text-white",
    PENDING_REVIEW: "bg-warning text-dark",
    REJECTED: "bg-danger text-white",
    USER_REQUESTED_DELETION: "bg-dark text-white",
    ADMIN_DELETED: "bg-dark text-white",
  };
  return `badge ${map[status] || "bg-light text-dark"}`;
};

// 헬퍼 함수: 숙소 상태 텍스트 반환
const getAccommodationStatusText = (status) => {
  const map = {
    ACTIVE: "운영중",
    INACTIVE: "비활성",
    PENDING_REVIEW: "검토중",
    REJECTED: "반려됨",
    USER_REQUESTED_DELETION: "삭제 요청됨",
    ADMIN_DELETED: "관리자 삭제",
  };
  return map[status] || status;
};

// 특정 숙소의 예약 건수 계산 (stats.reservations가 API 응답에 포함되어야 함)
function getReservationCountForAccommodation(accommodationId, targetStatuses = null) {
  if (!stats.value || !stats.value.reservations || !Array.isArray(stats.value.reservations)) {
    // console.warn("[HostDashboard] stats.reservations is not available or not an array.");
    return 0;
  }
  return stats.value.reservations.filter(
    (r) => r.accommodationId === accommodationId && (!targetStatuses || targetStatuses.includes(r.status))
  ).length;
}

// 대시보드 데이터 로드 함수
async function loadDashboardData() {
  console.log("[HostDashboard] loadDashboardData 진입, authStore:", userStore.isAuthenticated, userStore.user?.id);
  if (!userStore.isAuthenticated || !userStore.user?.id) {
    error.value = "로그인이 필요합니다. 다시 로그인해주세요.";
    loading.value = false;
    console.log("[HostDashboard] loadDashboardData 종료 - 로그인 필요");
    return;
  }
  loading.value = true;
  stats.value = null;
  accommodations.value = [];
  error.value = "";
  accommodationsError.value = "";
  console.log("[HostDashboard] loadDashboardData 시작 - loading.value: true");

  try {
    const accessToken = userStore.tokens?.access_token; // Pinia 스토어에서 직접 접근
    if (!accessToken) {
      throw new Error("액세스 토큰이 없습니다. 다시 로그인해주세요.");
    }

    const response = await fetch("/api/host/dashboard", {
      // vite.config.js 프록시 사용 가정
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    console.log("[HostDashboard] fetch /api/host/dashboard 응답 상태:", response.status);

    if (!response.ok) {
      const errData = await response
        .json()
        .catch(() => ({ error: "서버 응답 JSON 파싱 실패: " + response.statusText }));
      throw new Error(
        errData.error || errData.message || `대시보드 데이터를 불러오는데 실패했습니다. 상태: ${response.status}`
      );
    }
    stats.value = await response.json();
    console.log("[HostDashboard] stats.value 할당됨:", JSON.parse(JSON.stringify(stats.value)));

    if (stats.value && stats.value.host && stats.value.host.hostId) {
      console.log("[HostDashboard] loadAccommodations 호출 예정, hostId:", stats.value.host.hostId);
      await loadAccommodations(stats.value.host.hostId);
    } else {
      console.warn("[HostDashboard] stats.value.host.hostId 없음. stats:", JSON.parse(JSON.stringify(stats.value)));
      // 호스트 정보가 없으면 숙소 로딩을 시도하지 않음.
      accommodationsLoading.value = false;
    }
  } catch (e) {
    console.error("[HostDashboard] Error loading dashboard data:", e);
    error.value = e.message;
  } finally {
    loading.value = false; // 전체 대시보드 로딩 완료
    console.log(
      "[HostDashboard] loadDashboardData finally 종료 - loading.value:",
      loading.value,
      "error:",
      error.value
    );
  }
}

// 호스트의 숙소 목록 로드 함수
async function loadAccommodations(hostId) {
  console.log("[HostDashboard] loadAccommodations 호출됨, hostId:", hostId);
  accommodationsLoading.value = true;
  accommodationsError.value = "";
  console.log("[HostDashboard] loadAccommodations 시작 - accommodationsLoading.value: true");

  try {
    const accessToken = userStore.tokens?.access_token;
    if (!accessToken) {
      throw new Error("숙소 목록 조회 중 액세스 토큰이 없습니다. 다시 로그인해주세요.");
    }
    // 백엔드 HostController.java의 페이징 미지원 버전에 맞춰 URL 사용
    const apiUrl = `/api/host/${hostId}/accommodations`;
    console.log("[HostDashboard] Fetching accommodations from:", apiUrl);

    const response = await fetch(apiUrl, {
      headers: { Authorization: `Bearer ${accessToken}` },
    });
    console.log("[HostDashboard] fetch accommodations 응답 상태:", response.status);

    if (!response.ok) {
      const errData = await response
        .json()
        .catch(() => ({ error: "서버 응답 JSON 파싱 실패: " + response.statusText }));
      throw new Error(
        errData.error || errData.message || `숙소 목록을 불러오는데 실패했습니다. 상태: ${response.status}`
      );
    }
    const data = await response.json(); // { accommodations: [...], roomsByAccommodation: {}, ... } 등 예상
    console.log("[HostDashboard] Accommodations API response data:", JSON.parse(JSON.stringify(data)));

    // API 응답에서 accommodations 배열을 가져옴 (HostController.java 응답 구조에 따라 `data.accommodations` 또는 `data` 자체가 배열일 수 있음)
    // HostController의 getHostAccommodations는 Map<String, Object>를 반환하며, 그 안에 "accommodations" 키로 List<Accommodation>이 있음.
    if (data && Array.isArray(data.accommodations)) {
      accommodations.value = data.accommodations;
    } else if (data && Array.isArray(data)) {
      // 혹시 API가 List<Accommodation>을 직접 반환하는 경우
      accommodations.value = data;
    } else {
      console.warn("[HostDashboard] 숙소 목록 API 응답 형식이 예상과 다릅니다. data:", data);
      accommodations.value = [];
    }
  } catch (e) {
    console.error("[HostDashboard] Error loading accommodations:", e);
    accommodationsError.value = e.message;
    accommodations.value = [];
  } finally {
    accommodationsLoading.value = false;
    console.log(
      "[HostDashboard] loadAccommodations finally 종료 - accommodationsLoading.value:",
      accommodationsLoading.value,
      "error:",
      accommodationsError.value
    );
  }
}

onMounted(() => {
  console.log("[HostDashboard] onMounted - Component is mounted. Attempting to load data.");
  loadDashboardData();
});

const goToHostDetail = () => {
  if (hostId.value) {
    router.push({ name: "HostDetailInfo", params: { hostId: hostId.value } });
  } else {
    ElMessage.error("호스트 정보를 찾을 수 없어 상세 페이지로 이동할 수 없습니다.");
  }
};
</script>

<style scoped>
.table th,
.table td {
  /* data-v-... 제거하여 전역 스타일로 적용될 수 있게 하거나, 필요시 유지 */
  vertical-align: middle;
}
.card .icon {
  /* data-v-... 제거 */
  font-size: 2rem;
}
/* 추가된 스타일 */
.badge {
  font-size: 0.85em;
  padding: 0.4em 0.6em;
}
.table-hover tbody tr:hover {
  background-color: #f8f9fa;
}
.card-header h5 .bi {
  font-size: 1.1rem;
}
</style>
