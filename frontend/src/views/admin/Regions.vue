<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">지역 데이터 관리</h2>

      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="() => (message = '')" aria-label="Close"></button>
      </div>
      <!-- 에러 메시지 -->
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="() => (error = '')" aria-label="Close"></button>
      </div>

      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">지역 데이터를 불러오는 중입니다...</p>
      </div>

      <div v-else class="row">
        <!-- 시도 관리 -->
        <div class="col-md-6">
          <div class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5 class="mb-0">시/도 관리</h5>
              <button class="btn btn-sm btn-primary" @click="showAddSidoModal">
                <i class="bi bi-plus-circle"></i> 시/도 추가
              </button>
            </div>
            <div class="card-body">
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead>
                  <tr>
                    <th>코드</th>
                    <th>이름</th>
                    <th>구/군 수</th>
                    <th>관리</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="sido in sidos" :key="sido.sidoCode">
                    <td>{{ sido.sidoCode }}</td>
                    <td>{{ sido.sidoName }}</td>
                    <td>{{ sido.gugunCount || 0 }}</td>
                    <td>
                      <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-primary" @click="editSido(sido)">
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-outline-danger" @click="confirmDeleteSido(sido)">
                          <i class="bi bi-trash"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <!-- 구군 관리 -->
        <div class="col-md-6">
          <div class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
              <h5 class="mb-0">구/군 관리</h5>
              <button class="btn btn-sm btn-primary" @click="showAddGugunModal">
                <i class="bi bi-plus-circle"></i> 구/군 추가
              </button>
            </div>
            <div class="card-body">
              <div class="mb-3">
                <label for="sidoFilter" class="form-label">시/도 필터</label>
                <select class="form-select" id="sidoFilter" v-model="selectedSidoCode" @change="loadGuguns">
                  <option value="">전체 시/도</option>
                  <option v-for="sido in sidos" :key="sido.sidoCode" :value="sido.sidoCode">
                    {{ sido.sidoName }}
                  </option>
                </select>
              </div>
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead>
                  <tr>
                    <th>코드</th>
                    <th>시/도</th>
                    <th>이름</th>
                    <th>관리</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="gugun in filteredGuguns" :key="gugun.gugunCode">
                    <td>{{ gugun.gugunCode }}</td>
                    <td>{{ getSidoName(gugun.sidoCode) }}</td>
                    <td>{{ gugun.gugunName }}</td>
                    <td>
                      <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-primary" @click="editGugun(gugun)">
                          <i class="bi bi-pencil"></i>
                        </button>
                        <button class="btn btn-outline-danger" @click="confirmDeleteGugun(gugun)">
                          <i class="bi bi-trash"></i>
                        </button>
                      </div>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 시도 추가/수정 모달 -->
      <div class="modal fade" id="sidoModal" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">{{ isEditMode.value ? '시/도 수정' : '시/도 추가' }}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <form @submit.prevent="saveSido">
                <div class="mb-3">
                  <label for="sidoCode" class="form-label">시/도 코드</label>
                  <input type="text" class="form-control" id="sidoCode"
                         v-model="currentSido.value.sidoCode"
                         :readonly="isEditMode.value" required>
                  <div class="form-text">시/도 코드는 숫자 2자리로 입력</div>
                </div>
                <div class="mb-3">
                  <label for="sidoName" class="form-label">시/도 이름</label>
                  <input type="text" class="form-control" id="sidoName" v-model="currentSido.value.sidoName" required>
                </div>
                <div class="d-grid">
                  <button type="submit" class="btn btn-primary">저장</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>

      <!-- 구군 추가/수정 모달 -->
      <div class="modal fade" id="gugunModal" tabindex="-1">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">{{ isEditMode.value ? '구/군 수정' : '구/군 추가' }}</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <form @submit.prevent="saveGugun">
                <div class="mb-3">
                  <label for="gugunSidoCode" class="form-label">시/도</label>
                  <select class="form-select" id="gugunSidoCode"
                          v-model="currentGugun.value.sidoCode" :disabled="isEditMode.value" required>
                    <option value="">시/도 선택</option>
                    <option v-for="sido in sidos" :key="sido.sidoCode" :value="sido.sidoCode">
                      {{ sido.sidoName }}
                    </option>
                  </select>
                </div>
                <div class="mb-3">
                  <label for="gugunCode" class="form-label">구/군 코드</label>
                  <input type="text" class="form-control" id="gugunCode"
                         v-model="currentGugun.value.gugunCode" :readonly="isEditMode.value" required>
                  <div class="form-text">구/군 코드는 시/도 코드 + 숫자 3자리 (예: 11010)</div>
                </div>
                <div class="mb-3">
                  <label for="gugunName" class="form-label">구/군 이름</label>
                  <input type="text" class="form-control" id="gugunName"
                         v-model="currentGugun.value.gugunName" required>
                </div>
                <div class="d-grid">
                  <button type="submit" class="btn btn-primary">저장</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>

      <!-- 데이터 동기화 섹션 -->
      <div class="row mt-4">
        <div class="col-md-12">
          <div class="card">
            <div class="card-header">
              <h5 class="mb-0">지역 데이터 동기화</h5>
            </div>
            <div class="card-body">
              <div class="row">
                <div class="col-md-6">
                  <p>한국관광공사 API에서 지역 데이터를 가져와 데이터베이스를 업데이트합니다.</p>
                  <button class="btn btn-success" @click="syncRegionData" :disabled="syncing.value">
                    <span v-if="syncing.value" class="spinner-border spinner-border-sm me-2"></span>
                    지역 데이터 동기화
                  </button>
                </div>
                <div class="col-md-6">
                  <div v-if="syncStatus.value" class="alert" :class="syncSuccess.value ? 'alert-success' : 'alert-warning'">
                    {{ syncStatus.value }}
                  </div>
                  <div v-if="syncing.value && syncProgress.value > 0" class="progress mt-2">
                    <div class="progress-bar progress-bar-striped progress-bar-animated"
                         role="progressbar"
                         :style="{ width: syncProgress.value + '%' }"
                         :aria-valuenow="syncProgress.value"
                         aria-valuemin="0" aria-valuemax="100">
                      {{ syncProgress.value }}%
                    </div>
                  </div>
                  <div v-if="lastSyncDate.value" class="mt-3">
                    <strong>마지막 동기화:</strong> {{ formatDateTime(lastSyncDate.value) }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
/**
 * 지역 데이터 관리 - Bootstrap 5 + Vue 3 Composition API
 * - 시도/구군 CRUD 및 동기화
 * - Bootstrap Modal 활용
 * - Vuex store의 액션을 사용(예시)
 * - 관리자만 접근 가능
 */
import { ref, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import Layout from '@/components/layout/Layout.vue';

const store = useStore();
const router = useRouter();

const loading = ref(true);
const message = ref('');
const error = ref('');
const sidos = ref([]);
const guguns = ref([]);
const selectedSidoCode = ref('');
const isEditMode = ref(false);
const currentSido = ref({ sidoCode: '', sidoName: '' });
const currentGugun = ref({ sidoCode: '', gugunCode: '', gugunName: '' });
const syncing = ref(false);
const syncStatus = ref('');
const syncSuccess = ref(false);
const syncProgress = ref(0);
const lastSyncDate = ref(null);

const isAdmin = computed(() => store.state.user.user?.role === 'ADMIN');

// 필터링된 구군
const filteredGuguns = computed(() =>
    selectedSidoCode.value ? guguns.value.filter(g => g.sidoCode === selectedSidoCode.value) : guguns.value
);

// --- 모달 제어 (Bootstrap 5) ---
let sidoModalInstance = null;
let gugunModalInstance = null;

// --- 생명주기 ---
onMounted(async () => {
  // 관리자만 접근 가능
  if (!isAdmin.value) {
    router.push({
      path: '/error/access-denied',
      query: { message: '관리자만 접근할 수 있는 페이지입니다.' }
    });
    return;
  }
  await loadRegionData();
  // 부트스트랩 모달 인스턴스 초기화
  if (window.bootstrap) {
    sidoModalInstance = new window.bootstrap.Modal(document.getElementById('sidoModal'));
    gugunModalInstance = new window.bootstrap.Modal(document.getElementById('gugunModal'));
  }
});

// --- 지역 데이터 전체 로딩 ---
async function loadRegionData() {
  loading.value = true;
  try {
    sidos.value = await store.dispatch('admin/fetchSidos');
    guguns.value = await store.dispatch('admin/fetchGuguns');
    await loadLastSyncInfo();
  } catch (e) {
    error.value = '지역 데이터를 불러오는 중 오류가 발생했습니다.';
  } finally {
    loading.value = false;
  }
}

// --- 시도 추가/수정 모달 표시 ---
function showAddSidoModal() {
  isEditMode.value = false;
  currentSido.value = { sidoCode: '', sidoName: '' };
  sidoModalInstance?.show();
}
function editSido(sido) {
  isEditMode.value = true;
  currentSido.value = { ...sido };
  sidoModalInstance?.show();
}

// --- 시도 저장 ---
async function saveSido() {
  try {
    if (isEditMode.value) {
      await store.dispatch('admin/updateSido', currentSido.value);
      message.value = '시/도 정보가 성공적으로 수정되었습니다.';
    } else {
      await store.dispatch('admin/createSido', currentSido.value);
      message.value = '시/도가 성공적으로 추가되었습니다.';
    }
    sidoModalInstance?.hide();
    sidos.value = await store.dispatch('admin/fetchSidos');
  } catch (e) {
    error.value = '시도 저장에 실패했습니다. 다시 시도해주세요.';
  }
}

// --- 시도 삭제 ---
function confirmDeleteSido(sido) {
  if (confirm(`정말로 "${sido.sidoName}" 시/도를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
    deleteSidoItem(sido.sidoCode);
  }
}
async function deleteSidoItem(sidoCode) {
  try {
    await store.dispatch('admin/deleteSido', sidoCode);
    message.value = '시/도가 성공적으로 삭제되었습니다.';
    sidos.value = await store.dispatch('admin/fetchSidos');
    guguns.value = await store.dispatch('admin/fetchGuguns');
  } catch (e) {
    error.value = '시도 삭제에 실패했습니다. 다시 시도해주세요.';
  }
}

// --- 구군 추가/수정 모달 표시 ---
function showAddGugunModal() {
  isEditMode.value = false;
  currentGugun.value = { sidoCode: selectedSidoCode.value || '', gugunCode: '', gugunName: '' };
  gugunModalInstance?.show();
}
function editGugun(gugun) {
  isEditMode.value = true;
  currentGugun.value = { ...gugun };
  gugunModalInstance?.show();
}

// --- 구군 저장 ---
async function saveGugun() {
  try {
    if (isEditMode.value) {
      await store.dispatch('admin/updateGugun', currentGugun.value);
      message.value = '구/군 정보가 성공적으로 수정되었습니다.';
    } else {
      await store.dispatch('admin/createGugun', currentGugun.value);
      message.value = '구/군이 성공적으로 추가되었습니다.';
    }
    gugunModalInstance?.hide();
    guguns.value = await store.dispatch('admin/fetchGuguns');
    sidos.value = await store.dispatch('admin/fetchSidos');
  } catch (e) {
    error.value = '구군 저장에 실패했습니다. 다시 시도해주세요.';
  }
}

// --- 구군 삭제 ---
function confirmDeleteGugun(gugun) {
  if (confirm(`정말로 "${gugun.gugunName}" 구/군을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
    deleteGugunItem(gugun.gugunCode);
  }
}
async function deleteGugunItem(gugunCode) {
  try {
    await store.dispatch('admin/deleteGugun', gugunCode);
    message.value = '구/군이 성공적으로 삭제되었습니다.';
    guguns.value = await store.dispatch('admin/fetchGuguns');
    sidos.value = await store.dispatch('admin/fetchSidos');
  } catch (e) {
    error.value = '구군 삭제에 실패했습니다. 다시 시도해주세요.';
  }
}

// --- 구군 목록 필터
async function loadGuguns() {
  try {
    guguns.value = selectedSidoCode.value
        ? await store.dispatch('admin/fetchGuguns', selectedSidoCode.value)
        : await store.dispatch('admin/fetchGuguns');
  } catch {
    error.value = '구군 목록을 불러오는 중 오류가 발생했습니다.';
  }
}

// --- 동기화
async function syncRegionData() {
  syncing.value = true;
  syncStatus.value = '동기화 시작 중...';
  syncSuccess.value = true;
  syncProgress.value = 0;

  try {
    // 실제 구현 시: syncId 반환받아야 함
    const syncId = await store.dispatch('admin/syncRegions');
    pollSyncStatus(syncId);
  } catch (e) {
    syncStatus.value = '동기화 시작에 실패했습니다.';
    syncSuccess.value = false;
    syncing.value = false;
  }
}

// --- 동기화 상태 폴링
function pollSyncStatus(syncId) {
  const checkStatus = async () => {
    try {
      const status = await fetchSyncStatus(syncId);
      syncProgress.value = status.progress;
      syncStatus.value = status.message;

      if (status.status === 'COMPLETED') {
        syncSuccess.value = true;
        syncing.value = false;
        lastSyncDate.value = new Date();
        loadRegionData();
      } else if (status.status === 'FAILED') {
        syncSuccess.value = false;
        syncing.value = false;
      } else {
        setTimeout(checkStatus, 2000);
      }
    } catch {
      syncStatus.value = '동기화 상태 확인에 실패했습니다.';
      syncSuccess.value = false;
      syncing.value = false;
    }
  };
  setTimeout(checkStatus, 1000);
}

// --- 동기화 상태 확인 (임시)
async function fetchSyncStatus(syncId) {
  return new Promise(resolve => {
    const progress = syncProgress.value + Math.floor(Math.random() * 20);
    const finalProgress = Math.min(progress, 100);

    let status = 'IN_PROGRESS';
    let message = `지역 데이터 동기화 중... (${finalProgress}%)`;

    if (finalProgress >= 100) {
      status = 'COMPLETED';
      message = '지역 데이터 동기화가 완료되었습니다.';
    }
    resolve({ status, progress: finalProgress, message });
  });
}

// --- 마지막 동기화 날짜
async function loadLastSyncInfo() {
  // API에서 불러오면 됨 (여긴 임시)
  lastSyncDate.value = new Date(Date.now() - 1000 * 60 * 60 * 24 * 7);
}

// --- 시도 이름 반환
function getSidoName(sidoCode) {
  const sido = sidos.value.find(s => s.sidoCode === sidoCode);
  return sido ? sido.sidoName : sidoCode;
}

// --- 날짜 포맷
function formatDateTime(dt) {
  if (!dt) return '';
  const d = new Date(dt);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const h = String(d.getHours()).padStart(2, '0');
  const min = String(d.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${day} ${h}:${min}`;
}
</script>

<style scoped>
.card-header {
  background-color: #f8f9fa;
}
.table th {
  background-color: #f8f9fa;
}
.table td {
  vertical-align: middle;
}
.progress {
  height: 20px;
}
.btn-group {
  white-space: nowrap;
}
</style>
