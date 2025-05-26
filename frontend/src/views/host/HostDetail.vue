<template>
  <div class="container mt-5 mb-5">
    <h2 class="text-center mb-4">호스트 상세 정보</h2>
    <div class="detail-container card shadow-sm">
      <div class="card-body">
        <div v-if="loading" class="text-center">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">로딩 중...</span>
          </div>
        </div>
        <div v-else-if="error" class="alert alert-danger">{{ error }}</div>
        <div v-else-if="host">
          <div class="d-flex justify-content-between align-items-center mb-4 pb-3 border-bottom">
            <h3 class="mb-0">{{ host.businessName || "호스트 정보 없음" }}</h3>
            <span :class="getStatusBadgeClass(host.status)">
              {{ getStatusText(host.status) }}
            </span>
          </div>

          <div class="info-section mb-4">
            <h5 class="section-title">기본 정보</h5>
            <div class="row mb-2">
              <div class="col-md-4 info-label">상호명</div>
              <div class="col-md-8">{{ host.businessName || "-" }}</div>
            </div>
            <div class="row mb-2">
              <div class="col-md-4 info-label">대표자명</div>
              <div class="col-md-8">{{ host.ceoName || "-" }}</div>
            </div>
            <div class="row mb-2">
              <div class="col-md-4 info-label">호스트 소개</div>
              <div class="col-md-8">
                <pre class="description-pre">{{ host.description || "-" }}</pre>
              </div>
            </div>
          </div>

          <div class="info-section mb-4">
            <h5 class="section-title">사업자 정보</h5>
            <div class="row mb-2">
              <div class="col-md-4 info-label">사업자 등록번호</div>
              <div class="col-md-8">{{ host.businessNumber || "-" }}</div>
            </div>
            <div class="row mb-2">
              <div class="col-md-4 info-label">사업장 주소</div>
              <div class="col-md-8">{{ host.businessAddress || "-" }}</div>
            </div>
            <div class="row mb-2">
              <div class="col-md-4 info-label">사업장 전화번호</div>
              <div class="col-md-8">{{ host.businessPhone || "-" }}</div>
            </div>
            <div class="row mb-2">
              <div class="col-md-4 info-label">사업자 업종</div>
              <div class="col-md-8">{{ host.businessType || "-" }}</div>
            </div>
            <div class="row mb-2">
              <div class="col-md-4 info-label">사업자 등록증 만료일</div>
              <div class="col-md-8">
                {{ host.businessLicenseExpire ? formatDate(host.businessLicenseExpire, false) : "-" }}
              </div>
            </div>
            <div class="row mb-2">
              <div class="col-md-4 info-label">사업자 등록증</div>
              <div class="col-md-8">
                <a
                  v-if="host.businessLicense"
                  :href="host.businessLicense"
                  target="_blank"
                  class="btn btn-sm btn-outline-primary"
                >
                  <i class="bi bi-file-earmark-image"></i> 등록증 보기
                </a>
                <span v-else>-</span>
              </div>
            </div>
            <div v-if="host.licenseResubmitUrl" class="row mb-2">
              <div class="col-md-4 info-label">재제출된 등록증</div>
              <div class="col-md-8">
                <a :href="host.licenseResubmitUrl" target="_blank" class="btn btn-sm btn-outline-info">
                  <i class="bi bi-file-earmark-image"></i> 재제출 등록증 보기
                </a>
                (재제출일: {{ formatDate(host.licenseResubmittedAt) }})
              </div>
            </div>
          </div>

          <div class="info-section mb-4">
            <h5 class="section-title">정산 정보</h5>
            <div class="row mb-2">
              <div class="col-md-4 info-label">은행명</div>
              <div class="col-md-8">{{ host.bankName || "-" }}</div>
            </div>
            <div class="row mb-2">
              <div class="col-md-4 info-label">계좌번호</div>
              <div class="col-md-8">{{ host.bankAccount || "-" }}</div>
            </div>
            <div class="row mb-2">
              <div class="col-md-4 info-label">예금주명</div>
              <div class="col-md-8">{{ host.bankOwner || "-" }}</div>
            </div>
          </div>

          <div class="info-section mb-4">
            <h5 class="section-title">관리 정보</h5>
            <div class="row mb-2">
              <div class="col-md-4 info-label">등록일</div>
              <div class="col-md-8">{{ formatDate(host.createdAt) }}</div>
            </div>
            <div v-if="host.updatedAt" class="row mb-2">
              <div class="col-md-4 info-label">최종 수정일</div>
              <div class="col-md-8">{{ formatDate(host.updatedAt) }}</div>
            </div>
            <div v-if="host.adminComment" class="row mb-2">
              <div class="col-md-4 info-label">관리자 코멘트</div>
              <div class="col-md-8">
                <pre class="description-pre">{{ host.adminComment }}</pre>
              </div>
            </div>
            <div v-if="host.approvedAt" class="row mb-2">
              <div class="col-md-4 info-label">승인일</div>
              <div class="col-md-8">{{ formatDate(host.approvedAt) }}</div>
            </div>
            <div v-if="host.rejectedAt" class="row mb-2">
              <div class="col-md-4 info-label">반려일</div>
              <div class="col-md-8">{{ formatDate(host.rejectedAt) }}</div>
            </div>
          </div>

          <div class="actions-section mt-4 pt-4 border-top">
            <div v-if="isOwner || isAdmin" class="d-flex flex-wrap gap-2 justify-content-end">
              <RouterLink :to="`/host/update-form/${host.hostId}`" class="btn btn-primary">
                <i class="bi bi-pencil-square"></i> 정보 수정
              </RouterLink>
              <template v-if="host.status === 'ACTIVE' && isOwner">
                <RouterLink :to="`/host/accommodations/new`" class="btn btn-success">
                  <i class="bi bi-plus-circle"></i> 새 숙소 등록
                </RouterLink>
                <RouterLink :to="`/host/accommodations`" class="btn btn-info">
                  <i class="bi bi-list-task"></i> 내 숙소 관리
                </RouterLink>
              </template>
              <template v-if="isAdmin && host.status === 'WAIT'">
                <button @click="triggerUpdateStatus('ACTIVE')" class="btn btn-success" :disabled="actionLoading">
                  <i class="bi bi-check-circle"></i> 승인
                </button>
                <button @click="promptAdminCommentAndReject()" class="btn btn-danger" :disabled="actionLoading">
                  <i class="bi bi-x-circle"></i> 반려
                </button>
              </template>
              <template v-if="isOwner && host.status === 'REJECT'">
                <RouterLink :to="`/host/license-resubmit/${host.hostId}`" class="btn btn-warning">
                  <i class="bi bi-arrow-repeat"></i> 사업자 등록증 재제출
                </RouterLink>
              </template>
            </div>
          </div>
        </div>
        <div v-else class="alert alert-warning">호스트 정보를 찾을 수 없습니다.</div>
        <div class="mt-4">
          <RouterLink v-if="isAdmin" to="/admin/hosts" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left-short"></i> 관리자 호스트 목록
          </RouterLink>
          <RouterLink v-else to="/host" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left-short"></i> 내 호스트 정보
          </RouterLink>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRoute, useRouter, RouterLink } from "vue-router";
import { useUserStore } from "../../store/userStore";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const host = ref(null);
const loading = ref(true);
const error = ref("");
const actionLoading = ref(false); // 승인/반려 액션 로딩 상태

const isOwner = computed(() => host.value && userStore.user && userStore.user.userId === host.value.userId);
const isAdmin = computed(() => userStore.user && userStore.user.role === "ADMIN");

const getStatusBadgeClass = (status) => {
  const base = "badge fs-6";
  switch (status) {
    case "WAIT":
      return `${base} bg-warning text-dark`;
    case "ACTIVE":
      return `${base} bg-success`;
    case "REJECT":
      return `${base} bg-danger`;
    default:
      return `${base} bg-secondary`;
  }
};

const getStatusText = (status) => {
  const statusMap = {
    WAIT: "심사중",
    ACTIVE: "승인됨",
    REJECT: "반려됨",
  };
  return statusMap[status] || status || "알 수 없음";
};

const formatDate = (dateString, includeTime = true) => {
  if (!dateString) return "-";
  const date = new Date(dateString);
  const options = {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  };
  if (includeTime) {
    options.hour = "2-digit";
    options.minute = "2-digit";
  }
  return new Intl.DateTimeFormat("ko-KR", options).format(date);
};

async function updateHostStatus(hostId, newStatus, adminComment = null) {
  actionLoading.value = true;
  try {
    const token = userStore.tokens.access_token;
    if (!token) throw new Error("인증 토큰이 없습니다.");

    const queryParams = new URLSearchParams({ status: newStatus });
    if (adminComment) {
      queryParams.append("adminComment", adminComment);
    }

    const response = await fetch(`/api/hosts/${hostId}/status?${queryParams.toString()}`, {
      method: "PUT", // HTTP 메소드를 PUT으로 변경
      headers: {
        "Content-Type": "application/json", // Content-Type은 필요할 수 있음, 백엔드 설정 확인
        Authorization: `Bearer ${token}`,
      },
      // RequestParam을 사용하므로 바디는 비워둘 수 있으나, 명시적으로 빈 객체를 보낼 수도 있음
      // body: JSON.stringify({})
    });
    if (!response.ok) {
      const errData = await response.json().catch(() => ({ message: "상태 변경 실패" }));
      throw new Error(errData.error || errData.message || "호스트 상태 변경에 실패했습니다.");
    }
    await loadHostDetail(hostId); // 성공 후 상세 정보 다시 로드
    alert(`호스트 상태가 성공적으로 ${getStatusText(newStatus)}(으)로 변경되었습니다.`);
  } catch (e) {
    error.value = e.message;
    alert(e.message || "호스트 상태 변경 중 오류가 발생했습니다.");
    console.error(e);
  } finally {
    actionLoading.value = false;
  }
}

async function triggerUpdateStatus(newStatus) {
  if (!host.value) return;
  const confirmMessage = `정말로 이 호스트의 상태를 '${getStatusText(newStatus)}'(으)로 변경하시겠습니까?`;
  if (window.confirm(confirmMessage)) {
    await updateHostStatus(host.value.hostId, newStatus);
  }
}

async function promptAdminCommentAndReject() {
  if (!host.value) return;
  const adminComment = window.prompt("반려 사유를 입력해주세요. (선택사항)");
  const confirmMessage = `정말로 이 호스트의 상태를 '반려' 처리하시겠습니까?${adminComment ? "\n반려사유: " + adminComment : ""}`;
  if (window.confirm(confirmMessage)) {
    await updateHostStatus(host.value.hostId, "REJECT", adminComment);
  }
}

async function loadHostDetail(id) {
  loading.value = true;
  error.value = "";
  try {
    const token = userStore.tokens.access_token;
    const headers = {};
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }
    const res = await fetch(`/api/host/${id}`, { headers });
    if (!res.ok) {
      if (res.status === 404) throw new Error("호스트 정보를 찾을 수 없습니다.");
      throw new Error("호스트 정보를 불러오는데 실패했습니다.");
    }
    host.value = await res.json();
  } catch (e) {
    error.value = e.message;
    console.error("호스트 정보 로드 중 오류:", e);
    host.value = null; // 오류 발생 시 host 데이터 초기화
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  // userStore의 사용자 정보 로드가 완료될 때까지 기다릴 수 있음 (선택적)
  try {
    await userStore.fetchCurrentUser(); // 사용자 정보 및 토큰 로드
  } catch (e) {
    console.error("사용자 정보 로드 실패:", e);
    error.value = "사용자 정보를 불러오는데 실패했습니다. 다시 로그인해주세요.";
    loading.value = false;
    return;
  }

  const hostIdFromRoute = route.params.hostId;
  if (hostIdFromRoute) {
    await loadHostDetail(hostIdFromRoute);
  } else {
    error.value = "호스트 ID를 찾을 수 없습니다.";
    loading.value = false;
  }
});
</script>

<style scoped>
.detail-container {
  max-width: 900px;
  margin: 0 auto;
  /* padding: 25px;
  background-color: #fff; 
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05); */
}
.section-title {
  font-size: 1.2rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 2px solid #eee;
}
.info-label {
  font-weight: 500;
  color: #555;
}
.description-pre {
  white-space: pre-wrap; /* 줄바꿈 및 공백 유지 */
  font-family: inherit; /* 부모 폰트 상속 */
  font-size: inherit;
  word-break: break-word;
}
.actions-section {
  margin-top: 2rem; /* 위쪽 여백 추가 */
}
</style>
