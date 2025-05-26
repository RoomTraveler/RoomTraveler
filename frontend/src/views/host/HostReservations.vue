<template>
  <div class="container mt-5 mb-5">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h1 class="mb-0"><i class="bi bi-calendar-heart-fill me-2"></i>내 숙소 예약 관리</h1>
      <!-- 추가 기능 버튼 (예: 수동 예약 추가 등)이 필요하면 여기에 -->
    </div>

    <div v-if="loadingInitial" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">데이터 로딩 중...</span>
      </div>
    </div>
    <div v-else-if="initialError" class="alert alert-danger">{{ initialError }}</div>
    <div v-else-if="!hostId || hostStatus !== 'ACTIVE'" class="alert alert-warning text-center">
      <p v-if="!hostId">호스트 정보를 불러올 수 없거나 아직 호스트로 등록되지 않았습니다.</p>
      <p v-else-if="hostStatus === 'WAIT'">호스트 승인 대기 중입니다. 승인 후 예약 관리가 가능합니다.</p>
      <p v-else-if="hostStatus === 'REJECT'">호스트 등록이 반려되었습니다. 호스트 정보를 확인해주세요.</p>
      <RouterLink v-if="!hostId" to="/host/regist-form" class="btn btn-success mt-2">호스트 등록하기</RouterLink>
      <RouterLink v-else :to="`/host/detail/${hostId}`" class="btn btn-info mt-2">내 호스트 정보 보기</RouterLink>
    </div>

    <div v-else>
      <!-- 필터 섹션 -->
      <div class="card card-body mb-4 shadow-sm">
        <h5 class="card-title mb-3"><i class="bi bi-filter-circle me-1"></i>예약 검색 필터</h5>
        <div class="row g-3">
          <div class="col-md-3">
            <label for="filterAccommodation" class="form-label">숙소 선택</label>
            <select id="filterAccommodation" class="form-select form-select-sm" v-model="filters.accommodationId">
              <option value="">모든 숙소</option>
              <option v-for="acc in hostAccommodations" :key="acc.accommodationId" :value="acc.accommodationId">
                {{ acc.title }}
              </option>
            </select>
          </div>
          <div class="col-md-3">
            <label for="filterStatus" class="form-label">예약 상태</label>
            <select id="filterStatus" class="form-select form-select-sm" v-model="filters.status">
              <option value="">모든 상태</option>
              <option value="PENDING">승인대기</option>
              <option value="CONFIRMED">예약확정</option>
              <option value="CANCELLED_REQUEST">취소요청</option>
              <option value="CANCELLED">예약취소</option>
              <option value="COMPLETED">이용완료</option>
              <option value="NO_SHOW">노쇼</option>
            </select>
          </div>
          <div class="col-md-3">
            <label for="filterCheckInDate" class="form-label">체크인 날짜</label>
            <input
              type="date"
              id="filterCheckInDate"
              class="form-control form-control-sm"
              v-model="filters.checkInDate"
            />
          </div>
          <div class="col-md-3">
            <label for="filterSortBy" class="form-label">정렬 기준</label>
            <select id="filterSortBy" class="form-select form-select-sm" v-model="filters.sortBy">
              <option value="createdAtDesc">최신 예약순</option>
              <option value="checkInDateAsc">체크인 빠른순</option>
              <option value="checkInDateDesc">체크인 늦은순</option>
            </select>
          </div>
        </div>
        <div class="row g-3 mt-2">
          <div class="col-md-6">
            <label for="filterGuestName" class="form-label">게스트 이름</label>
            <input
              type="text"
              id="filterGuestName"
              class="form-control form-control-sm"
              v-model="filters.guestName"
              placeholder="게스트 이름으로 검색..."
            />
          </div>
          <div class="col-md-6 d-flex align-items-end">
            <button class="btn btn-sm btn-primary me-2 w-100" @click="applyFiltersAndResetPage">
              <i class="bi bi-search"></i> 검색
            </button>
            <button class="btn btn-sm btn-outline-secondary w-100" @click="resetFiltersAndLoad">
              <i class="bi bi-arrow-clockwise"></i> 초기화
            </button>
          </div>
        </div>
      </div>

      <div v-if="loadingReservations" class="text-center py-4">
        <div class="spinner-border text-secondary" role="status">
          <span class="visually-hidden">예약 목록 로딩 중...</span>
        </div>
      </div>
      <div v-else-if="reservationsError" class="alert alert-warning">{{ reservationsError }}</div>
      <div v-else-if="reservations.length === 0" class="alert alert-light text-center">
        <i class="bi bi-clipboard-x me-1"></i> 해당 조건의 예약 내역이 없습니다.
      </div>

      <div v-else>
        <div class="table-responsive">
          <table class="table table-hover align-middle text-center text-nowrap">
            <thead class="table-light">
              <tr>
                <th scope="col">예약ID</th>
                <th scope="col">숙소명</th>
                <th scope="col">객실명</th>
                <th scope="col">게스트</th>
                <th scope="col">체크인</th>
                <th scope="col">체크아웃</th>
                <th scope="col">금액</th>
                <th scope="col">예약상태</th>
                <th scope="col">결제상태</th>
                <th scope="col">요청일</th>
                <th scope="col">작업</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="reservation in reservations" :key="reservation.reservationId">
                <td>{{ reservation.reservationId }}</td>
                <td>{{ reservation.accommodationTitle }}</td>
                <td>{{ reservation.roomName }}</td>
                <td>{{ reservation.guestName || "N/A" }}</td>
                <td>{{ formatDate(reservation.checkInDate) }}</td>
                <td>{{ formatDate(reservation.checkOutDate) }}</td>
                <td>{{ formatCurrency(reservation.totalPrice) }}</td>
                <td>
                  <span :class="getReservationStatusBadge(reservation.status)">
                    {{ getReservationStatusText(reservation.status) }}
                  </span>
                </td>
                <td>
                  <span :class="getPaymentStatusBadge(reservation.paymentStatus)">
                    {{ getPaymentStatusText(reservation.paymentStatus) }}
                  </span>
                </td>
                <td>{{ formatDate(reservation.createdAt) }}</td>
                <td>
                  <button
                    class="btn btn-sm btn-outline-primary me-1"
                    @click="openUpdateModal(reservation)"
                    title="상태 변경"
                    v-if="canUpdateStatus(reservation.status)"
                  >
                    <i class="bi bi-pencil-square"></i>
                  </button>
                  <RouterLink
                    :to="`/host/reservations/${reservation.reservationId}`"
                    class="btn btn-sm btn-outline-info"
                    title="상세 보기"
                  >
                    <i class="bi bi-search"></i>
                  </RouterLink>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- 페이지네이션 -->
        <nav v-if="totalPages > 1" aria-label="Page navigation" class="d-flex justify-content-center mt-4">
          <ul class="pagination pagination-sm">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <a class="page-link" href="#" @click.prevent="changePage(currentPage - 1)">&laquo;</a>
            </li>
            <li
              v-for="pageNumber in pageRange"
              :key="pageNumber.toString()"
              class="page-item"
              :class="{ active: currentPage === pageNumber, disabled: pageNumber === '...' }"
            >
              <a v-if="pageNumber !== '...'" class="page-link" href="#" @click.prevent="changePage(pageNumber)">{{
                pageNumber
              }}</a>
              <span v-else class="page-link">...</span>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <a class="page-link" href="#" @click.prevent="changePage(currentPage + 1)">&raquo;</a>
            </li>
          </ul>
        </nav>
      </div>
    </div>

    <!-- 예약 상태 변경 모달 -->
    <div
      v-if="selectedReservation"
      class="modal fade show d-block"
      tabindex="-1"
      style="background-color: rgba(0, 0, 0, 0.5)"
      @click.self="closeUpdateModal"
    >
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">예약 #{{ selectedReservation.reservationId }} 상태 변경</h5>
            <button type="button" class="btn-close" @click="closeUpdateModal"></button>
          </div>
          <div class="modal-body">
            <p>
              <strong>숙소:</strong> {{ selectedReservation.accommodationTitle }} ({{
                selectedReservation.roomName
              }})<br />
              <strong>게스트:</strong> {{ selectedReservation.guestName || "N/A" }}<br />
              <strong>현재 상태:</strong> {{ getReservationStatusText(selectedReservation.status) }}
            </p>
            <div class="mb-3">
              <label for="newStatus" class="form-label">새로운 예약 상태:</label>
              <select id="newStatus" class="form-select" v-model="newStatus">
                <option v-if="selectedReservation.status === 'PENDING'" value="CONFIRMED">예약확정</option>
                <option
                  v-if="
                    selectedReservation.status === 'PENDING' ||
                    selectedReservation.status === 'CONFIRMED' ||
                    selectedReservation.status === 'CANCELLED_REQUEST'
                  "
                  value="CANCELLED"
                >
                  예약취소
                </option>
                <option v-if="selectedReservation.status === 'CONFIRMED'" value="COMPLETED">이용완료</option>
                <option v-if="selectedReservation.status === 'CONFIRMED'" value="NO_SHOW">노쇼(No-Show)</option>
                <!-- 추가적인 상태 전환 로직은 필요에 따라 백엔드와 협의 -->
              </select>
            </div>
            <div
              v-if="
                newStatus === 'CANCELLED' &&
                (selectedReservation.status === 'PENDING' ||
                  selectedReservation.status === 'CONFIRMED' ||
                  selectedReservation.status === 'CANCELLED_REQUEST')
              "
              class="mb-3"
            >
              <label for="cancellationReason" class="form-label">취소 사유 (선택)</label>
              <textarea id="cancellationReason" class="form-control" v-model="cancellationReason" rows="2"></textarea>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closeUpdateModal">닫기</button>
            <button
              type="button"
              class="btn btn-primary"
              @click="submitUpdateStatus"
              :disabled="!newStatus || statusUpdateLoading"
            >
              <span
                v-if="statusUpdateLoading"
                class="spinner-border spinner-border-sm"
                role="status"
                aria-hidden="true"
              ></span>
              변경 저장
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRoute, useRouter, RouterLink } from "vue-router";
import { useUserStore } from "@/store/userStore";
import apiUtils from "@/api/index";
import { ElTable, ElTableColumn, ElButton, ElTag, ElMessage } from "element-plus";

const { api } = apiUtils;

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const hostId = ref(null);
const hostStatus = ref(null);
const hostAccommodations = ref([]);
const reservations = ref([]);

const loadingInitial = ref(true);
const initialError = ref("");
const loadingReservations = ref(false);
const reservationsError = ref("");

const filters = ref({
  accommodationId: route.query.accommodationId || "",
  status: route.query.status || "",
  checkInDate: route.query.checkInDate || "",
  guestName: route.query.guestName || "",
  sortBy: route.query.sortBy || "createdAtDesc",
});

const currentPage = ref(parseInt(route.query.page) || 1);
const itemsPerPage = 10;
const totalItems = ref(0);

const selectedReservation = ref(null);
const newStatus = ref("");
const cancellationReason = ref("");
const statusUpdateLoading = ref(false);

// API 호출 로직
async function fetchHostDetails() {
  if (!userStore.isAuthenticated || !userStore.user?.id) {
    loadingInitial.value = false;
    initialError.value = "사용자 인증 정보가 없습니다.";
    return;
  }
  loadingInitial.value = true;
  initialError.value = "";
  try {
    const response = await api.get(`/api/host/user/${userStore.user.id}`);
    const hostData = response.data;
    if (hostData && hostData.hostId) {
      hostId.value = hostData.hostId;
      hostStatus.value = hostData.status;
      if (hostData.status === "ACTIVE") {
        await fetchHostAccommodationsForFilter(hostData.hostId);
        await fetchReservations();
      }
    } else {
      initialError.value = "호스트 정보를 찾을 수 없습니다. 호스트로 등록되어 있는지 확인해주세요.";
    }
  } catch (err) {
    if (err.response && err.response.status === 404) {
      initialError.value = "호스트 정보를 찾을 수 없습니다. (서버 404)";
    } else {
      initialError.value = `호스트 정보 로드 실패: ${err.message || "알 수 없는 오류"}`;
      console.error("Error fetching host details:", err);
    }
    hostId.value = null;
    hostStatus.value = null;
  } finally {
    loadingInitial.value = false;
  }
}

async function fetchHostAccommodationsForFilter(currentHostId) {
  try {
    const response = await api.get(`/api/host/${currentHostId}/accommodations?fields=accommodationId,title`);
    hostAccommodations.value = response.data;
  } catch (e) {
    console.error("Error fetching host accommodations for filter:", e);
    hostAccommodations.value = [];
  }
}

const fetchReservations = async () => {
  if (!hostId.value || hostStatus.value !== "ACTIVE") {
    loadingReservations.value = false;
    reservations.value = [];
    return;
  }
  loadingReservations.value = true;
  try {
    const currentHostId = hostId.value;
    if (!currentHostId) {
      ElMessage.warn("호스트 정보가 유효하지 않아 예약 목록을 불러올 수 없습니다.");
      reservations.value = [];
      loadingReservations.value = false;
      return;
    }
    const params = new URLSearchParams();
    if (filters.value.accommodationId) params.append("accommodationId", filters.value.accommodationId);
    if (filters.value.status) params.append("status", filters.value.status);
    if (filters.value.checkInDate) params.append("checkInDate", filters.value.checkInDate);
    if (filters.value.guestName) params.append("guestName", filters.value.guestName.trim());
    if (filters.value.sortBy) params.append("sortBy", filters.value.sortBy);
    params.append("page", currentPage.value.toString());
    params.append("size", itemsPerPage.toString());

    const response = await api.get(`/api/host/${currentHostId}/reservations?${params.toString()}`);
    reservations.value = response.data.content;
    totalItems.value = response.data.totalElements;
  } catch (e) {
    reservationsError.value = e.message || "예약 목록을 불러오는데 실패했습니다.";
    console.error("Error fetching reservations:", e);
    reservations.value = [];
    totalItems.value = 0;
  } finally {
    loadingReservations.value = false;
    if (loadingInitial.value) loadingInitial.value = false;
  }
};

const applyFiltersAndResetPage = () => {
  currentPage.value = 1;
  updateRouterQuery();
  fetchReservations();
};

const resetFiltersAndLoad = () => {
  filters.value = {
    accommodationId: "",
    status: "",
    checkInDate: "",
    guestName: "",
    sortBy: "createdAtDesc",
  };
  currentPage.value = 1;
  updateRouterQuery();
  fetchReservations();
};

const totalPages = computed(() => Math.ceil(totalItems.value / itemsPerPage));

const pageRange = computed(() => {
  const range = [];
  const delta = 2;
  const left = currentPage.value - delta;
  const right = currentPage.value + delta + 1;
  let l;

  for (let i = 1; i <= totalPages.value; i++) {
    if (i === 1 || i === totalPages.value || (i >= left && i < right)) {
      range.push(i);
    }
  }

  const tempRange = [];
  range.forEach((i) => {
    if (l !== undefined) {
      if (i - l === 2) {
        tempRange.push(l + 1);
      } else if (i - l !== 1) {
        tempRange.push("...");
      }
    }
    tempRange.push(i);
    l = i;
  });
  return tempRange;
});

function changePage(page) {
  if (typeof page === "string" || page < 1 || page > totalPages.value) return;
  currentPage.value = page;
  updateRouterQuery();
  fetchReservations();
}

function updateRouterQuery() {
  const query = {};
  if (filters.value.accommodationId) query.accommodationId = filters.value.accommodationId.toString();
  if (filters.value.status) query.status = filters.value.status;
  if (filters.value.checkInDate) query.checkInDate = filters.value.checkInDate;
  if (filters.value.guestName) query.guestName = filters.value.guestName;
  if (filters.value.sortBy) query.sortBy = filters.value.sortBy;

  query.page = currentPage.value > 1 ? currentPage.value.toString() : undefined;

  router.replace({ query });
}

// 예약 상태 변경 모달 관련
function openUpdateModal(reservation) {
  selectedReservation.value = { ...reservation };
  // 상태에 따른 초기 newStatus 설정 (옵션)
  if (reservation.status === "PENDING") newStatus.value = "CONFIRMED";
  else newStatus.value = "";
  cancellationReason.value = "";
}

function closeUpdateModal() {
  selectedReservation.value = null;
  newStatus.value = "";
  cancellationReason.value = "";
}

async function submitUpdateStatus() {
  if (!selectedReservation.value || !newStatus.value) return;

  statusUpdateLoading.value = true;
  try {
    const payload = { status: newStatus.value };
    if (newStatus.value === "CANCELLED" && cancellationReason.value) {
      payload.cancellationReason = cancellationReason.value;
    }

    await api.put(`/api/reservations/${selectedReservation.value.reservationId}/status`, payload);

    closeUpdateModal();
    await fetchReservations();
    alert("예약 상태가 성공적으로 변경되었습니다.");
  } catch (e) {
    alert(e.response?.data?.message || e.message || "예약 상태 변경에 실패했습니다.");
    console.error("Error updating reservation status:", e);
  } finally {
    statusUpdateLoading.value = false;
  }
}

function canUpdateStatus(currentStatus) {
  return !["CANCELLED", "COMPLETED", "NO_SHOW"].includes(currentStatus);
}

// 헬퍼 함수들 (날짜 포맷, 통화 포맷, 상태 텍스트/배지)
const formatDate = (dateString) => {
  if (!dateString) return "-";
  try {
    return new Intl.DateTimeFormat("ko-KR", { year: "numeric", month: "2-digit", day: "2-digit" }).format(
      new Date(dateString)
    );
  } catch (e) {
    return dateString;
  }
};
const formatCurrency = (amount) =>
  new Intl.NumberFormat("ko-KR", { style: "currency", currency: "KRW", maximumFractionDigits: 0 }).format(amount || 0);

const getReservationStatusText = (status) => {
  const map = {
    PENDING: "승인대기",
    CONFIRMED: "예약확정",
    CANCELLED_REQUEST: "취소요청",
    CANCELLED: "예약취소",
    COMPLETED: "이용완료",
    NO_SHOW: "노쇼(No-Show)",
  };
  return status ? map[status] || status : "N/A";
};

const getReservationStatusBadge = (status) => {
  const map = {
    PENDING: "bg-warning text-dark",
    CONFIRMED: "bg-success",
    CANCELLED_REQUEST: "bg-info text-dark",
    CANCELLED: "bg-danger",
    COMPLETED: "bg-primary",
    NO_SHOW: "bg-secondary",
  };
  return `badge ${status ? map[status] || "bg-light text-dark" : "bg-light text-dark"}`;
};

const getPaymentStatusText = (status) => {
  const map = {
    PENDING: "결제대기",
    PAID: "결제완료",
    FAILED: "결제실패",
    REFUND_REQUESTED: "환불요청",
    REFUNDED: "환불완료",
  };
  return status ? map[status] || status : "N/A";
};

const getPaymentStatusBadge = (status) => {
  const map = {
    PENDING: "bg-warning text-dark",
    PAID: "bg-success",
    FAILED: "bg-danger",
    REFUND_REQUESTED: "bg-info text-dark",
    REFUNDED: "bg-primary",
  };
  return `badge ${status ? map[status] || "bg-light text-dark" : "bg-light text-dark"}`;
};

// Watchers
watch(
  () => userStore.isAuthenticated,
  (isAuthenticated) => {
    if (isAuthenticated && userStore.user?.id) {
      loadingInitial.value = true;
      fetchHostDetails();
    } else {
      hostId.value = null;
      hostStatus.value = null;
      hostAccommodations.value = [];
      reservations.value = [];
      totalItems.value = 0;
      loadingInitial.value = false;
      initialError.value = isAuthenticated ? "사용자 ID를 찾을 수 없습니다." : "";
    }
  },
  { immediate: true }
);

watch(
  () => route.query,
  (newQuery) => {
    filters.value.accommodationId = newQuery.accommodationId || "";
    filters.value.status = newQuery.status || "";
    filters.value.checkInDate = newQuery.checkInDate || "";
    filters.value.guestName = newQuery.guestName || "";
    filters.value.sortBy = newQuery.sortBy || "createdAtDesc";
    currentPage.value = parseInt(newQuery.page) || 1;
  },
  { deep: true }
);

watch(
  () => userStore.user?.hostId,
  (newHostId) => {
    if (newHostId) {
      fetchReservations();
    } else {
      reservations.value = []; // 호스트 ID가 없으면 목록 초기화
      // ElMessage.warn('호스트 ID를 가져올 수 없습니다. 사용자 정보를 확인 중입니다...');
    }
  },
  { immediate: true }
); // immediate: true로 컴포넌트 로드 시 즉시 실행

onMounted(() => {
  // 최초 로딩은 watch에서 isAuthenticated이 true가 될 때 시작됨
});
</script>

<style scoped>
.table th,
.table td {
  vertical-align: middle;
}
.modal.show {
  display: block;
}
</style>
