<template>
  <div class="container mt-5 mb-5">
    <h2 class="mb-4">호스트 목록</h2>

    <!-- 필터 섹션 (관리자만) -->
    <div v-if="isAdmin" class="filter-section mb-4">
      <div class="row g-3">
        <div class="col-md-4">
          <label for="status" class="form-label">상태</label>
          <select class="form-select" id="status" v-model="filters.status">
            <option value="">전체</option>
            <option value="PENDING">승인 대기 중</option>
            <option value="APPROVED">승인됨</option>
            <option value="REJECTED">거부됨</option>
          </select>
        </div>
        <div class="col-md-4">
          <label for="keyword" class="form-label">검색어</label>
          <input
              type="text"
              class="form-control"
              id="keyword"
              v-model="filters.keyword"
              placeholder="사업자명, 사업자 등록번호"
          />
        </div>
        <div class="col-md-4 d-flex align-items-end">
          <button type="button" class="btn btn-primary w-100" @click="applyFilters">검색</button>
        </div>
      </div>
    </div>

    <!-- 호스트 목록 -->
    <div class="row">
      <div v-if="hosts.length === 0" class="col-12 text-center py-5">
        <p class="lead">등록된 호스트가 없습니다.</p>
        <router-link to="/host/regist-form" class="btn btn-primary">호스트 등록하기</router-link>
      </div>

      <div v-for="host in hosts" :key="host.hostId" class="col-md-4 mb-4">
        <div class="card host-card">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="card-title">{{ host.businessName }}</h5>
              <span :class="getStatusBadgeClass(host.hostStatus)">
                {{ getStatusText(host.hostStatus) }}
              </span>
            </div>
            <p class="card-text"><strong>사업자 등록번호:</strong> {{ host.businessRegNo }}</p>
            <p class="card-text"><strong>등록일:</strong> {{ formatDate(host.createdAt) }}</p>
            <div class="d-grid gap-2">
              <router-link :to="`/host/detail/${host.hostId}`" class="btn btn-outline-primary">상세 보기</router-link>
              <div v-if="isAdmin && host.hostStatus === 'PENDING'" class="d-flex justify-content-between mt-2">
                <button type="button" class="btn btn-success btn-sm" style="width: 48%;" @click="updateHostStatus(host.hostId, 'APPROVED')">승인</button>
                <button type="button" class="btn btn-danger btn-sm" style="width: 48%;" @click="updateHostStatus(host.hostId, 'REJECTED')">거부</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 호스트 등록 버튼 (일반 사용자) -->
    <div v-if="!isAdmin && isLoggedIn" class="d-grid gap-2 col-md-6 mx-auto mt-4">
      <router-link to="/host/regist-form" class="btn btn-primary">호스트 등록하기</router-link>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';

// 상태
const hosts = ref([]);
const filters = ref({
  status: '',
  keyword: ''
});
const isAdmin = ref(false);
const isLoggedIn = ref(false);

const route = useRoute();
const router = useRouter();

// 상태 뱃지 클래스 반환
function getStatusBadgeClass(status) {
  const baseClass = 'badge status-badge';
  switch (status) {
    case 'PENDING': return `${baseClass} status-pending`;
    case 'APPROVED': return `${baseClass} status-approved`;
    case 'REJECTED': return `${baseClass} status-rejected`;
    default: return baseClass;
  }
}

// 상태 한글 텍스트
function getStatusText(status) {
  switch (status) {
    case 'PENDING': return '승인 대기 중';
    case 'APPROVED': return '승인됨';
    case 'REJECTED': return '거부됨';
    default: return status;
  }
}

// 날짜 포맷
function formatDate(dateString) {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  }).format(date);
}

// 사용자 정보 불러오기
async function loadUserInfo() {
  try {
    const response = await fetch('/api/users/me');
    if (!response.ok) {
      isLoggedIn.value = false;
      isAdmin.value = false;
      return;
    }
    const user = await response.json();
    isLoggedIn.value = true;
    isAdmin.value = user.role === 'ADMIN';
  } catch (error) {
    isLoggedIn.value = false;
    isAdmin.value = false;
  }
}

// 호스트 목록 불러오기
async function loadHosts() {
  try {
    let url = '/api/hosts';
    const params = new URLSearchParams();
    if (filters.value.status) params.append('status', filters.value.status);
    if (filters.value.keyword) params.append('keyword', filters.value.keyword);
    if (params.toString()) {
      url += `?${params.toString()}`;
    }
    const response = await fetch(url);
    if (!response.ok) throw new Error('호스트 목록을 불러오는데 실패했습니다.');
    hosts.value = await response.json();
  } catch (error) {
    hosts.value = [];
    // 에러 알림 필요시 여기에 처리
  }
}

// 필터 적용
function applyFilters() {
  const query = {};
  if (filters.value.status) query.status = filters.value.status;
  if (filters.value.keyword) query.keyword = filters.value.keyword;
  router.replace({ query });
  loadHosts();
}

// 호스트 상태 변경
async function updateHostStatus(hostId, status) {
  if (!confirm(`호스트 상태를 ${getStatusText(status)}(으)로 변경하시겠습니까?`)) return;
  try {
    const response = await fetch(`/api/hosts/${hostId}/status`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ hostStatus: status })
    });
    if (!response.ok) throw new Error('호스트 상태 변경에 실패했습니다.');
    loadHosts();
  } catch (error) {
    alert('호스트 상태 변경에 실패했습니다.');
  }
}

// 쿼리파라미터로 초기값 세팅
onMounted(async () => {
  if (route.query.status) filters.value.status = route.query.status;
  if (route.query.keyword) filters.value.keyword = route.query.keyword;
  await loadUserInfo();
  await loadHosts();
});

// 쿼리 변경 감지해서 목록 다시 로드(뒤로가기 등)
watch(() => route.query, () => {
  filters.value.status = route.query.status || '';
  filters.value.keyword = route.query.keyword || '';
  loadHosts();
});
</script>

<style scoped>
.host-card {
  transition: transform 0.3s;
  margin-bottom: 20px;
  height: 100%;
}
.host-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}
.filter-section {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 5px;
  margin-bottom: 20px;
}
.status-badge {
  font-size: 0.8rem;
  padding: 0.4rem 0.6rem;
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
