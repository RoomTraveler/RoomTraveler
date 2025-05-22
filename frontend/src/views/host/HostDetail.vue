<template>
  <div class="container mt-5 mb-5">
    <h2 class="text-center mb-4">호스트 상세 정보</h2>
    <div class="detail-container">
      <div v-if="host">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3>{{ host.businessName }}</h3>
          <span :class="getStatusBadgeClass(host.hostStatus)">
            {{ getStatusText(host.hostStatus) }}
          </span>
        </div>
        <div class="info-group">
          <h5>사업자 정보</h5>
          <div class="row mb-2">
            <div class="col-md-4 info-label">사업자명</div>
            <div class="col-md-8">{{ host.businessName }}</div>
          </div>
          <div class="row mb-2">
            <div class="col-md-4 info-label">사업자 등록번호</div>
            <div class="col-md-8">{{ host.businessRegNo }}</div>
          </div>
        </div>
        <div class="info-group">
          <h5>계좌 정보</h5>
          <div class="row mb-2">
            <div class="col-md-4 info-label">정산 계좌 정보</div>
            <div class="col-md-8">{{ host.bankAccount }}</div>
          </div>
        </div>
        <div v-if="host.profileText" class="info-group">
          <h5>호스트 소개</h5>
          <p>{{ host.profileText }}</p>
        </div>
        <div class="info-group">
          <h5>등록 정보</h5>
          <div class="row mb-2">
            <div class="col-md-4 info-label">등록일</div>
            <div class="col-md-8">{{ formatDate(host.createdAt) }}</div>
          </div>
          <div v-if="host.updatedAt" class="row mb-2">
            <div class="col-md-4 info-label">최종 수정일</div>
            <div class="col-md-8">{{ formatDate(host.updatedAt) }}</div>
          </div>
        </div>
        <div
            v-if="isHostOrAdmin"
            class="d-grid gap-2 d-md-flex justify-content-md-end mt-4"
        >
          <RouterLink :to="`/host/update-form/${host.hostId}`" class="btn btn-primary">정보 수정</RouterLink>
          <template v-if="host.hostStatus === 'APPROVED' && isHostOrAdmin">
            <RouterLink to="/host/register-accommodation" class="btn btn-success">숙소 등록</RouterLink>
            <RouterLink to="/accommodation/host/accommodations" class="btn btn-info">내 숙소 관리</RouterLink>
          </template>
          <template v-if="isAdmin && host.hostStatus === 'PENDING'">
            <button @click="updateHostStatus('APPROVED')" class="btn btn-success">승인</button>
            <button @click="updateHostStatus('REJECTED')" class="btn btn-danger">거부</button>
          </template>
        </div>
      </div>
      <div v-else class="alert alert-warning">호스트 정보를 찾을 수 없습니다.</div>
      <div class="d-grid gap-2 mt-4">
        <RouterLink to="/host/list" class="btn btn-secondary">호스트 목록으로</RouterLink>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter, RouterLink } from "vue-router";

// ----- 상태 변수 -----
const route = useRoute();
const router = useRouter();

const host = ref(null);

const currentUser = ref({
  userId: null,
  role: null,
});

// ----- 권한 계산 -----
const isHostOrAdmin = computed(() => {
  return currentUser.value.userId === host.value?.hostId || currentUser.value.role === "ADMIN";
});
const isAdmin = computed(() => currentUser.value.role === "ADMIN");

// ----- 함수 -----
const getStatusBadgeClass = (status) => {
  const base = "badge status-badge";
  switch (status) {
    case "PENDING":
      return `${base} status-pending`;
    case "APPROVED":
      return `${base} status-approved`;
    case "REJECTED":
      return `${base} status-rejected`;
    default:
      return base;
  }
};

const getStatusText = (status) => {
  switch (status) {
    case "PENDING":
      return "승인 대기 중";
    case "APPROVED":
      return "승인됨";
    case "REJECTED":
      return "거부됨";
    default:
      return status;
  }
};

const formatDate = (dateString) => {
  if (!dateString) return "-";
  const date = new Date(dateString);
  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
  }).format(date);
};

const updateHostStatus = async (status) => {
  try {
    if (!window.confirm(`호스트 상태를 ${getStatusText(status)}(으)로 변경하시겠습니까?`)) return;
    const response = await fetch(`/api/hosts/${host.value.hostId}/status`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ hostStatus: status }),
    });
    if (!response.ok) throw new Error("호스트 상태 변경에 실패했습니다.");
    await loadHostDetail(host.value.hostId);
  } catch (e) {
    alert("호스트 상태 변경에 실패했습니다.");
    console.error(e);
  }
};

const loadCurrentUser = async () => {
  try {
    const res = await fetch("/api/users/me");
    if (!res.ok) throw new Error("사용자 정보 실패");
    const user = await res.json();
    currentUser.value.userId = user.id;
    currentUser.value.role = user.role;
  } catch (e) {
    console.error("사용자 정보 로드 중 오류:", e);
  }
};

const loadHostDetail = async (hostId) => {
  try {
    const res = await fetch(`/api/hosts/${hostId}`);
    if (!res.ok) throw new Error("호스트 정보 실패");
    host.value = await res.json();
  } catch (e) {
    console.error("호스트 정보 로드 중 오류:", e);
  }
};

// ----- 최초 로딩 -----
onMounted(async () => {
  await loadCurrentUser();
  const hostId = route.params.id;
  if (hostId) await loadHostDetail(hostId);
});
</script>

<style scoped>
.detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0,0,0,0.1);
}
.info-group {
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #dee2e6;
  padding-bottom: 1rem;
}
.info-group:last-child {
  border-bottom: none;
}
.info-label {
  font-weight: bold;
  color: #495057;
}
.status-badge {
  font-size: 0.9rem;
  padding: 0.5rem 0.75rem;
  border-radius: 50px;
}
.status-pending {
  background-color: #ffc107;
  color: #212529;
}
.status-approved {
  background-color: #28a745;
  color: white;
}
.status-rejected {
  background-color: #dc3545;
  color: white;
}
</style>
