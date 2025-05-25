<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">사용자 관리</h2>

      <!-- 알림 메시지 -->
      <el-alert v-if="message && message !== '성공' && !error" :title="message" type="info" show-icon @close="clearMessage" class="mb-3" />
      <el-alert v-if="error" :title="error" type="error" show-icon @close="clearError" class="mb-3" />

      <!-- 검색 및 필터링 -->
      <div class="card mb-4">
        <div class="card-body">
          <form @submit.prevent="searchUsers">
            <div class="row g-3">
              <div class="col-md-3">
                <label for="role-filter" class="form-label">역할</label>
                <el-select id="role-filter" v-model="searchParams.role" placeholder="전체 역할" clearable class="w-100">
                  <el-option label="전체" value="" />
                  <el-option label="일반 사용자" value="USER" />
                  <el-option label="호스트" value="HOST" />
                  <el-option label="관리자" value="ADMIN" />
                </el-select>
              </div>
              <div class="col-md-3">
                <label for="status-filter" class="form-label">상태</label>
                <el-select id="status-filter" v-model="searchParams.status" placeholder="전체 상태" clearable class="w-100">
                  <el-option label="전체" value="" />
                  <el-option label="활성" value="ACTIVE" />
                  <el-option label="비활성" value="INACTIVE" />
                  <el-option label="정지" value="SUSPENDED" />
                </el-select>
              </div>
              <div class="col-md-4">
                <label for="keyword" class="form-label">검색어</label>
                <input type="text" class="form-control" id="keyword" v-model="searchParams.keyword" placeholder="이름, 이메일, 전화번호">
              </div>
              <div class="col-md-2 d-flex align-items-end">
                <el-button type="primary" native-type="submit" :loading="loading" class="w-100">검색</el-button>
              </div>
            </div>
          </form>
        </div>
      </div>

      <!-- 로딩 표시 -->
      <div v-if="loading && users.length === 0" class="text-center py-5">
        <el-skeleton :rows="5" animated />
        <p class="mt-2">사용자 정보를 불러오는 중입니다...</p>
      </div>

      <!-- 사용자 목록 -->
      <div v-else>
        <div v-if="!loading && users.length === 0 && (message || error)" class="my-3">
             <el-alert v-if="message && message !== '성공' && !error" :title="message" type="info" show-icon />
             <el-alert v-if="error" :title="error" type="error" show-icon />
        </div>
        <div v-else-if="!loading && users.length === 0 && !message && !error" class="alert alert-info">
          검색 결과가 없습니다.
        </div>

        <el-table v-else :data="users" stripe style="width: 100%" class="mb-3">
          <el-table-column prop="userId" label="ID" width="80" align="center" />
          <el-table-column prop="username" label="이름" min-width="120" />
          <el-table-column prop="email" label="이메일" min-width="180" />
          <el-table-column prop="phone" label="전화번호" min-width="130">
            <template #default="{ row }">{{ row.phone || '-' }}</template>
          </el-table-column>
          <el-table-column label="역할" width="150">
            <template #default="{ row }">
              <el-select 
                :model-value="row.role" 
                placeholder="역할 선택" 
                size="small" 
                @change="(newRole) => handleRoleChange(row, newRole)"
                :disabled="row.userId === userStore.user?.userId"
              >
                <el-option label="일반 사용자" value="USER" />
                <el-option label="호스트" value="HOST" />
                <el-option label="관리자" value="ADMIN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="상태" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusTagType(row.userAccountStatus || row.status)" size="small">
                {{ getStatusName(row.userAccountStatus || row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="가입일" width="120" align="center">
            <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="관리" width="150" align="center" fixed="right">
            <template #default="{ row }">
              <div class="btn-group btn-group-sm" role="group" aria-label="User Actions">
                <button
                    v-if="(row.userAccountStatus || row.status) === 'ACTIVE'"
                    type="button"
                    class="btn btn-outline-warning"
                    @click="suspendUserPrompt(row)"
                    title="사용자 계정 정지"
                >
                    <i class="bi bi-pause-fill"></i>
                </button>
                <el-button
                    v-if="(row.userAccountStatus || row.status) === 'SUSPENDED'"
                    type="success"
                    :icon="VideoPlay"
                    size="small"
                    @click="activateUser(row.userId)"
                    title="사용자 계정 활성화"
                />
                <button
                    type="button"
                    class="btn btn-outline-danger"
                    @click="confirmDeleteUser(row)"
                    title="사용자 계정 삭제"
                    :disabled="row.userId === userStore.user?.userId"
                >
                    <i class="bi bi-trash"></i>
                </button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 페이지네이션 -->
        <div class="d-flex justify-content-center mt-4" v-if="totalPages > 0">
            <el-pagination
                background
                layout="prev, pager, next, jumper, ->, total"
                :total="totalElements"
                :page-size="searchParams.size"
                :current-page="currentPage"
                @current-change="goToPage"
            />
        </div>
      </div>

      <!-- 사용자 수정 모달 (기존 Bootstrap Modal 유지 또는 Element Plus Dialog로 변경 고려) -->
      <div class="modal fade" ref="userModalRef" id="userModal" tabindex="-1" aria-labelledby="userModalLabel" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="userModalLabel">사용자 정보 수정</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" @click="hideModal"></button>
            </div>
            <div class="modal-body">
              <form @submit.prevent="saveUser">
                <div class="mb-3">
                  <label for="username" class="form-label">이름</label>
                  <input type="text" class="form-control" id="username" v-model="currentUser.username" required>
                </div>
                <div class="mb-3">
                  <label for="email" class="form-label">이메일 (수정 불가)</label>
                  <input type="email" class="form-control" id="email" v-model="currentUser.email" required readonly>
                </div>
                <div class="mb-3">
                  <label for="phone" class="form-label">전화번호</label>
                  <input type="tel" class="form-control" id="phone" v-model="currentUser.phone" placeholder="예: 010-1234-5678">
                </div>
                <div class="mb-3">
                  <label for="userStatusModal" class="form-label">상태</label>
                  <select class="form-select" id="userStatusModal" v-model="currentUser.userAccountStatus">
                    <option value="ACTIVE">활성</option>
                    <option value="INACTIVE">비활성</option>
                    <option value="SUSPENDED">정지</option>
                  </select>
                </div>
                <div class="mb-3 form-check">
                  <input type="checkbox" class="form-check-input" id="resetPassword" v-model="resetPassword">
                  <label class="form-check-label" for="resetPassword">비밀번호 초기화 (새 무작위 비밀번호로 변경)</label>
                </div>
                <div class="d-grid">
                  <button type="submit" class="btn btn-primary">저장</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import Layout from '@/components/layout/Layout.vue';
import { useAdminStore } from '@/store/adminStore';
import { useUserStore } from '@/store/userStore';
import { ElMessage, ElMessageBox, ElNotification, ElTable, ElTableColumn, ElTag, ElPagination, ElAlert, ElSelect, ElOption, ElSkeleton, ElButton } from 'element-plus';
import { Delete, VideoPlay } from '@element-plus/icons-vue';

const adminStore = useAdminStore();
const route = useRoute();
const userStore = useUserStore();
const router = useRouter();

const loading = computed(() => adminStore.loading);
const message = computed({ get: () => adminStore.message, set: (v) => adminStore._setMessage(v) });
const error = computed({ get: () => adminStore.error, set: (v) => adminStore._setError(v) });

function clearMessage() { adminStore._setMessage(''); }
function clearError() { adminStore._setError(''); }

const users = computed(() => adminStore.users || []);
const totalPages = computed(() => adminStore.userTotalPages || 0);
const currentPage = computed(() => adminStore.userCurrentPage || 1);
const totalElements = computed(() => adminStore.userTotalElements || 0);

const searchParams = reactive({
  role: route.query.role || '',
  status: route.query.status || '',
  keyword: route.query.keyword || '',
  page: route.query.page ? parseInt(route.query.page) : 1,
  size: 10,
});

const currentUser = reactive({
  userId: null,
  username: '',
  email: '',
  phone: '',
  role: 'USER',
  userAccountStatus: 'ACTIVE'
});
const resetPassword = ref(false);
const userModalRef = ref(null);
let userModalInstance = null;

const isAdmin = computed(() => userStore.user?.role === 'ADMIN');

function getRoleName(role) {
  return { USER: '일반 사용자', HOST: '호스트', ADMIN: '관리자' }[role] || role || '-';
}
function getStatusName(status) {
  return { ACTIVE: '활성', INACTIVE: '비활성', SUSPENDED: '정지' }[status] || status || '-';
}
function getStatusTagType(status) {
  return { ACTIVE: 'success', INACTIVE: 'info', SUSPENDED: 'danger' }[status] || 'primary';
}

function formatDate(dateInput) {
  if (!dateInput) return '-';
  let d;
  if (typeof dateInput === 'string') {
    d = new Date(dateInput);
  } else if (Array.isArray(dateInput) && dateInput.length >= 3) {
    try {
      d = new Date(dateInput[0], dateInput[1] - 1, dateInput[2],
                   dateInput[3] || 0, dateInput[4] || 0, dateInput[5] || 0);
    } catch (e) { console.error('[formatDate] Error parsing date array:', dateInput, e); return '-'; }
  } else if (dateInput && typeof dateInput === 'object' && dateInput.hasOwnProperty('year') && dateInput.hasOwnProperty('monthValue') && dateInput.hasOwnProperty('dayOfMonth')) {
    try {
        d = new Date(dateInput.year, dateInput.monthValue -1, dateInput.dayOfMonth,
        dateInput.hour || 0, dateInput.minute || 0, dateInput.second || 0);
    } catch (e) { console.error('[formatDate] Error parsing date object:', dateInput, e); return '-'; }
  } else { console.warn('[formatDate] Unknown date format for input:', dateInput); return '-'; }
  if (isNaN(d.getTime())) { console.error('[formatDate] Invalid date object created from input:', dateInput); return '-';}
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}

async function loadUsers() {
  adminStore._clearMessages();
  await adminStore.fetchAdminUsers({ ...searchParams, page: searchParams.page -1, size: searchParams.size });
  updateQueryParams();
}

function searchUsers() {
  searchParams.page = 1;
  loadUsers();
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value || page === currentPage.value) return;
  searchParams.page = page;
  loadUsers();
}

function updateQueryParams() {
  const query = {};
  if (searchParams.role) query.role = searchParams.role;
  if (searchParams.status) query.status = searchParams.status;
  if (searchParams.keyword) query.keyword = searchParams.keyword;
  if (searchParams.page > 1) query.page = searchParams.page.toString();
  
  if (Object.keys(query).length > 0 || Object.keys(route.query).length > 0) {
      router.replace({ query }).catch(err => {
        if (err.name !== 'NavigationDuplicated' && err.name !== 'NavigationCancelled') {
            console.error('Router replace error:', err);
        }
    });
  }
}

function showModal() {
  if (!userModalInstance && window.bootstrap && userModalRef.value) {
    userModalInstance = new window.bootstrap.Modal(userModalRef.value);
  }
  userModalInstance?.show();
}
function hideModal() {
  userModalInstance?.hide();
  Object.assign(currentUser, {
    userId: null, username: '', email: '', phone: '', role: 'USER', userAccountStatus: 'ACTIVE'
  });
  resetPassword.value = false;
}

function editUser(user) {
  Object.assign(currentUser, { ...user, userAccountStatus: user.userAccountStatus || user.status });
  resetPassword.value = false;
  showModal();
}

async function saveUser() {
  try {
    const userDataToSave = {
        userId: currentUser.userId,
        username: currentUser.username,
        phone: currentUser.phone,
        userAccountStatus: currentUser.userAccountStatus
    };

    await adminStore.updateAdminUser(userDataToSave);
    
    if (resetPassword.value) {
      await adminStore.resetAdminUserPassword(currentUser.userId);
    }

    hideModal();
    loadUsers();
     ElNotification({ title: '성공', message: '사용자 정보가 저장되었습니다.', type: 'success' });
  } catch (e) {
    console.error('사용자 정보 저장에 실패했습니다.', e);
    ElNotification({ title: '오류', message: adminStore.error || '사용자 정보 저장 실패', type: 'error' });
  }
}

async function handleRoleChange(user, newRole) {
  if (user.userId === userStore.user?.userId) {
    ElMessage.warning('자신의 역할은 변경할 수 없습니다.');
    loadUsers(); 
    return;
  }
  try {
    await ElMessageBox.confirm(
      `사용자 "${user.username}"(ID: ${user.userId})의 역할을 ${getRoleName(user.role)}에서 ${getRoleName(newRole)}(으)로 변경하시겠습니까?`,
      '역할 변경 확인',
      {
        confirmButtonText: '변경',
        cancelButtonText: '취소',
        type: 'warning',
      }
    );
    await adminStore.updateAdminUserRole(user.userId, newRole);
    ElNotification({ title: '성공', message: `사용자 역할이 ${getRoleName(newRole)}(으)로 변경되었습니다.`, type: 'success' });
    loadUsers();
  } catch (e) {
    if (e !== 'cancel') {
      console.error("사용자 역할 변경 오류:", e);
      ElNotification({ title: '오류', message: adminStore.error || '사용자 역할 변경 실패', type: 'error' });
      loadUsers();
    } else {
      loadUsers();
    }
  }
}

function confirmDeleteUser(user) {
  if (user.userId === userStore.user?.userId) {
    ElMessage.warning('자신의 계정은 삭제할 수 없습니다.');
    return;
  }
  ElMessageBox.confirm(
    `정말로 "${user.username}" (ID: ${user.userId}) 사용자를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`,
    '삭제 확인',
    {
      confirmButtonText: '삭제',
      cancelButtonText: '취소',
      type: 'error',
    }
  ).then(async () => {
    await deleteUserItem(user.userId);
  }).catch(() => { /* 취소 */ });
}

async function deleteUserItem(userId) {
  try {
    await adminStore.deleteAdminUser(userId);
    ElNotification({ title: '성공', message: '사용자가 삭제되었습니다.', type: 'success' });
    loadUsers();
  } catch (e) {
    ElNotification({ title: '오류', message: adminStore.error || '사용자 삭제 실패', type: 'error' });
  }
}

function suspendUserPrompt(user) {
 ElMessageBox.prompt(
    `"${user.username}" (ID: ${user.userId}) 사용자를 정지 처리하시겠습니까? 사유를 입력해주세요 (선택 사항):`,
    '사용자 정지',
    {
      confirmButtonText: '정지',
      cancelButtonText: '취소',
      inputType: 'textarea',
    }
  ).then(async ({ value }) => {
    await suspendUser(user.userId, value || null);
  }).catch(() => { /* 취소 */ });
}

async function suspendUser(userId, reason) {
  try {
    await adminStore.updateAdminUserStatus({ userId, status: 'SUSPENDED', reason });
    ElNotification({ title: '성공', message: '사용자 계정이 정지되었습니다.', type: 'success' });
    loadUsers();
  } catch (e) {
     ElNotification({ title: '오류', message: adminStore.error || '사용자 계정 정지 실패', type: 'error' });
  }
}

async function activateUser(userId) {
  try {
    await adminStore.updateAdminUserStatus({ userId, status: 'ACTIVE' });
    ElNotification({ title: '성공', message: '사용자 계정이 활성화되었습니다.', type: 'success' });
    loadUsers();
  } catch (e) {
    ElNotification({ title: '오류', message: adminStore.error || '사용자 계정 활성화 실패', type: 'error' });
  }
}

watch(() => isAdmin.value, (newIsAdmin, oldIsAdmin) => {
  console.log(`[AdminUsers] Watch isAdmin: changed from ${oldIsAdmin} to ${newIsAdmin}. Current store user: ${userStore.user ? JSON.stringify(userStore.user) : userStore.user}`);

  if (newIsAdmin) {
    console.log('[AdminUsers] Watch isAdmin: isAdmin is now true, calling loadUsers().');
    loadUsers();
  } else {
    if (oldIsAdmin === true && userStore.user && router.currentRoute.value.name === 'AdminUsers') {
      console.log('[AdminUsers] Watch isAdmin: isAdmin changed from true to false while on page. Redirecting to AccessDenied.');
      router.push({ name: 'AccessDenied', query: { message: '접근 권한이 없어졌거나 변경되었습니다.' } });
    } else if (oldIsAdmin === undefined && newIsAdmin === false && userStore.user && userStore.user.role && userStore.user.role !== 'ADMIN') {
        console.log('[AdminUsers] Watch isAdmin: Initial load, user exists but not admin. Redirecting.');
        router.push({ name: 'AccessDenied', query: { message: '관리자 계정이 아닙니다.' } });
    }
  }
}, { immediate: true });

onMounted(() => {
  adminStore._clearMessages();
  console.log('[AdminUsers] onMounted - userStore.user (at mount):', userStore.user ? JSON.stringify(userStore.user) : userStore.user);
  console.log('[AdminUsers] onMounted - isAdmin.value (at mount):', isAdmin.value);

  if (window.bootstrap && userModalRef.value) {
    userModalInstance = new window.bootstrap.Modal(userModalRef.value);
  }
});

watch(() => route.query, (newQuery, oldQuery) => {
    const queryChanged = JSON.stringify(newQuery) !== JSON.stringify(oldQuery);
    if (queryChanged) {
        console.log('[AdminUsers] Route query changed, reloading users.');
        searchParams.role = newQuery.role || '';
        searchParams.status = newQuery.status || '';
        searchParams.keyword = newQuery.keyword || '';
        searchParams.page = newQuery.page ? parseInt(newQuery.page) : 1;
        if(isAdmin.value) {
            loadUsers();
        } else {
            console.log('[AdminUsers] Route query changed, but isAdmin is false. Not loading users.');
        }
    }
}, { deep: true });

</script>

<style scoped>
.card { margin-bottom: 20px; }
.table td, .table th { vertical-align: middle; }
.modal-body .form-control[readonly] {
  background-color: #e9ecef;
  opacity: 1;
}
.w-100 {
  width: 100% !important;
}
.btn-group .btn i {
    vertical-align: middle;
}
.el-button + .btn, .btn + .el-button {
    margin-left: -1px;
}
</style>
