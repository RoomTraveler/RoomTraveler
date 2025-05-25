<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">전체 호스트 관리</h2>

      <!-- 검색/필터 -->
      <div class="card mb-4">
        <div class="card-body">
          <form @submit.prevent="searchAllHosts">
            <div class="row g-3">
              <div class="col-md-3">
                <label for="host-status" class="form-label">호스트 등록 상태</label>
                <select id="host-status" class="form-select" v-model="searchParams.status">
                  <option value="">전체</option>
                  <option value="WAIT">심사중(WAIT)</option>
                  <option value="ACTIVE">승인(ACTIVE)</option> 
                  <option value="REJECTED">반려(REJECTED)</option>
                </select>
              </div>
              <div class="col-md-3">
                <label for="host-keyword" class="form-label">검색어</label>
                <input id="host-keyword" type="text" class="form-control"
                       v-model="searchParams.keyword" placeholder="호스트ID, 사용자명, 사업자명" />
              </div>
              <div class="col-md-2 d-flex align-items-end">
                <el-button type="primary" @click="searchAllHosts" :loading="loading">검색</el-button>
              </div>
            </div>
          </form>
        </div>
      </div>

      <el-alert v-if="message && message !== '성공' && !error" :title="message" type="info" show-icon @close="clearMessage" class="mb-3" />
      <el-alert v-if="error" :title="error" type="error" show-icon @close="clearError" class="mb-3" />

      <div v-if="loading && hostsToDisplay.length === 0" class="text-center py-5">
        <el-skeleton :rows="5" animated />
        <p class="mt-2">호스트 목록을 불러오는 중입니다...</p>
      </div>

      <div v-else>
        <div v-if="!loading && hostsToDisplay.length === 0 && !message && !error" class="alert alert-info">
          조회된 호스트가 없습니다.
        </div>
        <el-table v-else-if="hostsToDisplay.length > 0" :data="hostsToDisplay" stripe style="width: 100%" class="mb-3">
          <el-table-column prop="userId" label="호스트ID" width="90" align="center" />
          <el-table-column label="사용자명(이메일)" min-width="180">
            <template #default="{ row }">
              <div>{{ row.username || 'N/A' }}</div>
              <small class="text-muted">{{ row.email || 'N/A' }}</small>
              <br><small class="text-info">계정: {{ getUserAccountStatusName(row.userAccountStatus) }}</small>
            </template>
          </el-table-column>
          <el-table-column label="사업자명" min-width="150">
            <template #default="{ row }">
              {{ row.businessName || 'N/A' }}
            </template>
          </el-table-column>
          <el-table-column label="사업자번호" width="150">
            <template #default="{ row }">
              {{ row.businessNumber || 'N/A' }}
            </template>
          </el-table-column>
          <el-table-column label="호스트 등록 상태" width="140" align="center"> 
            <template #default="{ row }">
              <el-tag :type="getHostRegistrationStatusTagType(row.hostRegistrationStatus)" size="small">
                {{ getHostRegistrationStatusName(row.hostRegistrationStatus) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="신청(생성)일" width="120" align="center">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="관리" width="200" align="center" fixed="right">
            <template #default="{ row }">
              <el-button-group size="small">
                <el-button
                    v-if="row.hostRegistrationStatus === 'WAIT'"
                    type="success"
                    @click="updateHostRegistrationStatus(row, 'ACTIVE', '승인')">승인</el-button>
                <el-button
                    v-if="row.hostRegistrationStatus === 'WAIT'"
                    type="danger"
                    @click="promptHostStatusReason(row, 'REJECTED', '반려')">반려</el-button>
                 <el-button
                    v-if="row.hostRegistrationStatus === 'REJECTED'"
                    type="primary"
                    @click="updateHostRegistrationStatus(row, 'ACTIVE', '승인(복원)')">승인(복원)</el-button>
                
                <!-- 호스트 계정 상태 관리 버튼 -->
                <el-button
                  v-if="row.userAccountStatus === 'ACTIVE'"
                  type="warning"
                  plain
                  @click="suspendHostUserAccountPrompt(row)"
                  :disabled="isCurrentUser(row.userId)"
                  title="호스트 계정 정지">계정정지</el-button>
                <el-button
                  v-if="row.userAccountStatus === 'SUSPENDED'"
                  type="success"
                  plain
                  @click="activateHostUserAccount(row)"
                  :disabled="isCurrentUser(row.userId)"
                  title="호스트 계정 활성화">계정활성</el-button>
                <el-button
                  type="danger"
                  plain
                  @click="confirmDeleteHostUserAccount(row)"
                  :disabled="isCurrentUser(row.userId)"
                  title="호스트 계정 삭제">계정삭제</el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
        <!--  데이터가 없고, store에 message가 있지만 error는 아닐때 (예: "서비스 로직 구현 필요") -->
         <el-alert 
            v-else-if="!loading && hostsToDisplay.length === 0 && message && message !== '성공' && !error" 
            :title="message" 
            type="info" 
            show-icon 
            @close="clearMessage" 
            class="mb-3" />

        <div class="d-flex justify-content-center mt-4" v-if="totalPages > 1">
          <el-pagination
            background
            layout="prev, pager, next, jumper, ->, total"
            :total="totalElements"
            :page-size="searchParams.size"
            :current-page="currentPageForPaginator"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useAdminStore } from '@/store/adminStore';
import { useUserStore } from '@/store/userStore';
import { useRouter, useRoute } from 'vue-router';
import Layout from '@/components/layout/Layout.vue';
import { ElMessageBox, ElNotification, ElTable, ElTableColumn, ElTag, ElButton, ElButtonGroup, ElPagination, ElAlert, ElSkeleton } from 'element-plus';

const adminStore = useAdminStore();
const userStore = useUserStore();
const router = useRouter();
const route = useRoute();

const loading = computed(() => adminStore.loading);
const hostsToDisplay = computed(() => adminStore.allHosts || []); // 항상 배열 반환 보장
const totalPages = computed(() => adminStore.allHostsTotalPages || 0);
const currentPageFromStore = computed(() => adminStore.allHostsCurrentPage || 1);
const totalElements = computed(() => adminStore.allHostsTotalElements || 0);

const searchParams = ref({
  status: route.query.status || '', 
  keyword: route.query.keyword || '',
  page: route.query.page ? parseInt(route.query.page) : 1,
  size: 10,
  sortBy: 'createdAt', 
  sortDirection: 'DESC'
});

const message = computed(() => adminStore.message);
const error = computed(() => adminStore.error);

const currentPageForPaginator = computed(() => searchParams.value.page);

function clearMessage() { adminStore._setMessage(''); }
function clearError() { adminStore._setError(''); }

function formatDate(dateInput) {
  if (!dateInput) return 'N/A';
  let date;
  if (Array.isArray(dateInput) && dateInput.length >= 3) {
    date = new Date(dateInput[0], dateInput[1] - 1, dateInput[2]);
  } else if (typeof dateInput === 'string' || typeof dateInput === 'number') {
    date = new Date(dateInput);
  } else {
    return 'N/A';
  }
  if (isNaN(date.getTime())) return 'N/A';
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

const getUserAccountStatusName = (status) => ({
  'ACTIVE': '활성',
  'INACTIVE': '비활성',
  'SUSPENDED': '정지',
}[status] || status || '알 수 없음');

const getHostRegistrationStatusName = (status) => ({
  'WAIT': '심사중',
  'ACTIVE': '승인됨',
  'REJECTED': '반려됨',
}[status] || status || '알 수 없음');

const getHostRegistrationStatusTagType = (status) => ({
  'WAIT': 'warning',
  'ACTIVE': 'success',
  'REJECTED': 'danger',
}[status] || 'primary');

const isCurrentUser = (userId) => {
  return userStore.user && userStore.user.userId === userId;
};

async function loadAllHosts(page = searchParams.value.page -1) { 
  // 메시지 초기화는 스토어 액션 내부 또는 여기서 명시적으로 수행 가능
  // adminStore._clearMessages(); // 필요시 추가
  const apiParams = { ...searchParams.value, page };
  await adminStore.fetchAllHosts(apiParams); 
  updateRouterQuery();
}

function searchAllHosts() {
  searchParams.value.page = 1;
  loadAllHosts(0);
}

function handlePageChange(newPage) {
  searchParams.value.page = newPage;
  loadAllHosts(newPage - 1);
}

function updateRouterQuery() {
  const query = {};
  if (searchParams.value.status) query.status = searchParams.value.status;
  if (searchParams.value.keyword) query.keyword = searchParams.value.keyword;
  if (searchParams.value.page > 1) query.page = searchParams.value.page.toString();
  // 빈 쿼리 객체로 라우터가 변경되지 않도록 조건 추가
  if (Object.keys(query).length > 0 || Object.keys(route.query).length > 0) {
    router.replace({ query }).catch(err => {
      if (err.name !== 'NavigationDuplicated' && err.name !== 'NavigationCancelled') { // NavigationCancelled 추가
        console.error('Router replace error:', err);
      }
    });
  }
}

async function updateHostRegistrationStatus(host, newStatus, actionDisplayName, reason = null) {
    try {
        await adminStore.updateHostStatusByAdmin({
            hostUserId: host.userId, 
            status: newStatus, 
            reason
        });
        ElNotification({ title: '성공', message: `호스트(ID: ${host.userId})의 등록 상태가 ${getHostRegistrationStatusName(newStatus)}로 변경되었습니다.`, type: 'success' });
        loadAllHosts(searchParams.value.page - 1); 
    } catch (err) {
        // 스토어에서 설정된 에러 메시지를 우선 사용
        ElNotification({ title: '오류', message: adminStore.error || `호스트 등록 상태 변경 실패 (ID: ${host.userId})`, type: 'error' });
    }
}

async function promptHostStatusReason(host, newStatus, actionName) {
    try {
        const { value } = await ElMessageBox.prompt(
            `호스트(ID: ${host.userId}, 사업자명: ${host.businessName || 'N/A'})를 ${actionName} 처리하는 사유를 입력해주세요.`,
            `${actionName} 사유 입력`,
            {
                confirmButtonText: '제출',
                cancelButtonText: '취소',
                // 입력 값 검증 추가 가능
                // inputValidator: (val) => {
                //   if (newStatus === 'REJECTED' && (!val || val.trim() === '')) {
                //     return '반려 시에는 사유를 반드시 입력해야 합니다.';
                //   }
                //   return true;
                // },
            }
        );
        // ElMessageBox.prompt는 사용자가 취소하면 reject되므로, value가 undefined인 경우는 거의 없음 (확인 버튼을 눌렀으나 입력이 없는 경우)
        // 사용자가 취소 버튼을 누르면 catch 블록으로 바로 이동.
        if (newStatus === 'REJECTED' && (!value || value.trim() === '')) { 
            ElNotification({ title: '주의', message: '반려 시에는 사유를 입력해야 합니다.', type: 'warning' });
            return; // 여기서 중단
        }
        await updateHostRegistrationStatus(host, newStatus, actionName, value || null);
        
    } catch (e) { 
      // 사용자가 '취소'를 누르거나 창을 닫으면 e는 'cancel' 문자열이 됨.
      if (e !== 'cancel') { 
        console.error(`호스트 ${actionName} 처리 중 오류:`, e);
         ElNotification({ title: '오류', message: `처리 중 오류 발생: ${typeof e === 'string' ? e : (e.message || adminStore.error || '알 수 없는 오류')}`, type: 'error' });
      }
    }
}

// 호스트 계정 정지 프롬프트
async function suspendHostUserAccountPrompt(host) {
  if (isCurrentUser(host.userId)) {
    ElNotification({ title: '오류', message: '자신의 계정은 정지할 수 없습니다.', type: 'error' });
    return;
  }
  try {
    const { value } = await ElMessageBox.prompt(
      `호스트(ID: ${host.userId}, 사용자명: ${host.username})의 계정을 정지 처리하시겠습니까? 사유를 입력해주세요. (선택 사항)`,
      '호스트 계정 정지',
      {
        confirmButtonText: '정지',
        cancelButtonText: '취소',
        inputType: 'textarea',
      }
    );
    await adminStore.updateHostUserAccountStatus({ hostUserId: host.userId, status: 'SUSPENDED', reason: value || null });
    ElNotification({ title: '성공', message: `호스트(ID: ${host.userId}) 계정이 정지되었습니다.`, type: 'success' });
    // 목록을 다시 로드할 필요 없음, 스토어 액션에서 처리
  } catch (e) {
    if (e !== 'cancel') {
      console.error('호스트 계정 정지 처리 중 오류:', e);
      ElNotification({ title: '오류', message: adminStore.error || '호스트 계정 정지 실패', type: 'error' });
    }
  }
}

// 호스트 계정 활성화
async function activateHostUserAccount(host) {
  if (isCurrentUser(host.userId)) {
    ElNotification({ title: '오류', message: '자신의 계정은 활성화할 수 없습니다.', type: 'error' });
    return;
  }
  try {
    await adminStore.updateHostUserAccountStatus({ hostUserId: host.userId, status: 'ACTIVE' });
    ElNotification({ title: '성공', message: `호스트(ID: ${host.userId}) 계정이 활성화되었습니다.`, type: 'success' });
  } catch (e) {
    console.error('호스트 계정 활성화 처리 중 오류:', e);
    ElNotification({ title: '오류', message: adminStore.error || '호스트 계정 활성화 실패', type: 'error' });
  }
}

// 호스트 계정 삭제 확인
async function confirmDeleteHostUserAccount(host) {
  if (isCurrentUser(host.userId)) {
    ElNotification({ title: '오류', message: '자신의 계정은 삭제할 수 없습니다.', type: 'error' });
    return;
  }
  try {
    await ElMessageBox.confirm(
      `정말로 호스트(ID: ${host.userId}, 사용자명: ${host.username})의 계정을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다. 해당 호스트의 모든 숙소 정보 등도 함께 처리될 수 있습니다.`,
      '호스트 계정 삭제 확인',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'error',
      }
    );
    await adminStore.deleteHostUserAccount(host.userId);
    ElNotification({ title: '성공', message: `호스트(ID: ${host.userId}) 계정이 삭제되었습니다.`, type: 'success' });
  } catch (e) {
    if (e !== 'cancel') {
      console.error('호스트 계정 삭제 처리 중 오류:', e);
      ElNotification({ title: '오류', message: adminStore.error || '호스트 계정 삭제 실패', type: 'error' });
    }
  }
}

onMounted(() => {
  // adminStore._clearMessages(); // 페이지 로드 시 메시지 초기화 필요시
  searchParams.value.status = route.query.status || '';
  searchParams.value.keyword = route.query.keyword || '';
  searchParams.value.page = route.query.page ? parseInt(route.query.page) : 1;
  loadAllHosts(); // onMounted에서 page는 searchParams.value.page - 1 로 전달
});

// route.query 변경 감지 시, searchParams 업데이트 후 loadAllHosts 호출
// watch의 immediate 옵션은 onMounted와 중복 호출될 수 있으므로 주의
// 현재 onMounted에서 이미 초기 로딩을 수행하므로 immediate: true는 불필요할 수 있음
watch(() => route.query, (newQuery, oldQuery) => {
    // 실제 쿼리 변경이 있을 때만 실행 (값 비교)
    const queryChanged = JSON.stringify(newQuery) !== JSON.stringify(oldQuery);
    if (queryChanged) {
        // adminStore._clearMessages(); // 필요시 메시지 초기화
        searchParams.value.status = newQuery.status || '';
        searchParams.value.keyword = newQuery.keyword || '';
        searchParams.value.page = newQuery.page ? parseInt(newQuery.page) : 1;
        loadAllHosts(); // 여기서도 searchParams.value.page - 1
    }
}, { deep: true });

</script>

<style scoped>
.el-table th {
  background-color: #f8f9fa;
}
.el-button-group {
  white-space: nowrap;
}
.container {
  max-width: 1300px;
}
</style> 