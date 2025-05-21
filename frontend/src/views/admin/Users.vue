<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">사용자 관리</h2>
      <!-- 알림/에러 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>
      <!-- 검색/필터 -->
      <div class="card mb-4">
        <div class="card-body">
          <form @submit.prevent="searchUsers">
            <div class="row g-3">
              <div class="col-md-3">
                <label for="role" class="form-label">역할</label>
                <select class="form-select" id="role" v-model="searchParams.role">
                  <option value="">전체</option>
                  <option value="USER">일반 사용자</option>
                  <option value="HOST">호스트</option>
                  <option value="ADMIN">관리자</option>
                </select>
              </div>
              <div class="col-md-3">
                <label for="status" class="form-label">상태</label>
                <select class="form-select" id="status" v-model="searchParams.status">
                  <option value="">전체</option>
                  <option value="ACTIVE">활성</option>
                  <option value="INACTIVE">비활성</option>
                  <option value="SUSPENDED">정지</option>
                </select>
              </div>
              <div class="col-md-4">
                <label for="keyword" class="form-label">검색어</label>
                <input type="text" class="form-control" id="keyword" v-model="searchParams.keyword" placeholder="이름, 이메일, 전화번호">
              </div>
              <div class="col-md-2 d-flex align-items-end">
                <button type="submit" class="btn btn-primary w-100">검색</button>
              </div>
            </div>
          </form>
        </div>
      </div>
      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">사용자 정보를 불러오는 중입니다...</p>
      </div>
      <!-- 사용자 목록 -->
      <div v-else>
        <div v-if="users.length === 0" class="alert alert-info">
          검색 결과가 없습니다.
        </div>
        <div v-else class="table-responsive">
          <table class="table table-striped table-hover">
            <thead>
              <tr>
                <th>ID</th>
                <th>이름</th>
                <th>이메일</th>
                <th>전화번호</th>
                <th>역할</th>
                <th>상태</th>
                <th>가입일</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in users" :key="user.userId">
                <td>{{ user.userId }}</td>
                <td>{{ user.username }}</td>
                <td>{{ user.email }}</td>
                <td>{{ user.phone || '-' }}</td>
                <td>
                  <span :class="getRoleBadgeClass(user.role)">
                    {{ getRoleName(user.role) }}
                  </span>
                </td>
                <td>
                  <span :class="getStatusBadgeClass(user.status)">
                    {{ getStatusName(user.status) }}
                  </span>
                </td>
                <td>{{ formatDate(user.createdAt) }}</td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button 
                      class="btn btn-outline-primary"
                      @click="editUser(user)"
                      title="사용자 정보 수정">
                      <i class="bi bi-pencil"></i>
                    </button>
                    <button 
                      v-if="user.status === 'ACTIVE'"
                      class="btn btn-outline-warning"
                      @click="suspendUserHandler(user.userId)"
                      title="사용자 계정 정지">
                      <i class="bi bi-pause-fill"></i>
                    </button>
                    <button 
                      v-if="user.status === 'SUSPENDED'"
                      class="btn btn-outline-success"
                      @click="activateUserHandler(user.userId)"
                      title="사용자 계정 활성화">
                      <i class="bi bi-play-fill"></i>
                    </button>
                    <button 
                      class="btn btn-outline-danger"
                      @click="confirmDeleteUser(user)"
                      title="사용자 계정 삭제">
                      <i class="bi bi-trash"></i>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- 페이지네이션 -->
        <nav v-if="totalPages > 1" aria-label="Page navigation">
          <ul class="pagination justify-content-center">
            <li class="page-item" :class="{ disabled: currentPage === 1 }">
              <a class="page-link" href="#" @click.prevent="goToPage(currentPage - 1)">이전</a>
            </li>
            <li v-for="page in paginationItems" :key="page" class="page-item" :class="{ active: page === currentPage, disabled: page === '...' }">
              <a class="page-link" href="#" @click.prevent="page !== '...' && goToPage(page)">{{ page }}</a>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <a class="page-link" href="#" @click.prevent="goToPage(currentPage + 1)">다음</a>
            </li>
          </ul>
        </nav>
      </div>
      <!-- 사용자 수정 모달 -->
      <div class="modal fade" id="userModal" tabindex="-1" aria-labelledby="userModalLabel" aria-hidden="true">
        <div class="modal-dialog">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title" id="userModalLabel">사용자 정보 수정</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
              <form @submit.prevent="saveUser">
                <div class="mb-3">
                  <label for="username" class="form-label">이름</label>
                  <input type="text" class="form-control" id="username" v-model="currentUser.username" required>
                </div>
                <div class="mb-3">
                  <label for="email" class="form-label">이메일</label>
                  <input type="email" class="form-control" id="email" v-model="currentUser.email" required>
                </div>
                <div class="mb-3">
                  <label for="phone" class="form-label">전화번호</label>
                  <input type="tel" class="form-control" id="phone" v-model="currentUser.phone" placeholder="예: 010-1234-5678">
                </div>
                <div class="mb-3">
                  <label for="userRole" class="form-label">역할</label>
                  <select class="form-select" id="userRole" v-model="currentUser.role">
                    <option value="USER">일반 사용자</option>
                    <option value="HOST">호스트</option>
                    <option value="ADMIN">관리자</option>
                  </select>
                </div>
                <div class="mb-3">
                  <label for="userStatus" class="form-label">상태</label>
                  <select class="form-select" id="userStatus" v-model="currentUser.status">
                    <option value="ACTIVE">활성</option>
                    <option value="INACTIVE">비활성</option>
                    <option value="SUSPENDED">정지</option>
                  </select>
                </div>
                <div class="mb-3 form-check">
                  <input type="checkbox" class="form-check-input" id="resetPassword" v-model="resetPassword">
                  <label class="form-check-label" for="resetPassword">비밀번호 초기화</label>
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
import { ref, computed, onMounted, onBeforeMount } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useUserStore } from '@/store/userStore';
import Layout from '@/components/layout/Layout.vue';

// 상태/참조
const loading = ref(true);
const message = ref('');
const error = ref('');
const users = ref([]);
const totalItems = ref(0);
const totalPages = ref(0);
const currentPage = ref(1);
const resetPassword = ref(false);
const userModal = ref(null);

const searchParams = ref({
  role: '',
  status: '',
  keyword: '',
  page: 1,
  size: 10,
});

const currentUser = ref({
  userId: null,
  username: '',
  email: '',
  phone: '',
  role: 'USER',
  status: 'ACTIVE'
});

// 라우터/스토어
const router = useRouter();
const route = useRoute();
const userStore = useUserStore();

const isAdmin = computed(() => userStore.userRole === 'ADMIN');

// 페이지네이션
const paginationItems = computed(() => {
  const items = [];
  const maxVisiblePages = 5;
  if (totalPages.value <= maxVisiblePages) {
    for (let i = 1; i <= totalPages.value; i++) items.push(i);
  } else {
    items.push(1);
    if (currentPage.value > 3) items.push('...');
    const start = Math.max(2, currentPage.value - 1);
    const end = Math.min(totalPages.value - 1, currentPage.value + 1);
    for (let i = start; i <= end; i++) items.push(i);
    if (currentPage.value < totalPages.value - 2) items.push('...');
    items.push(totalPages.value);
  }
  return items;
});

// --------------------------
// Core Methods
// --------------------------
const loadUsers = async () => {
  loading.value = true;
  try {
    // const result = await adminStore.fetchUsers({ ...searchParams.value });
    users.value = result.content;
    totalItems.value = result.totalElements;
    totalPages.value = result.totalPages;
    currentPage.value = result.number + 1;
    updateQueryParams();
  } catch (err) {
    error.value = '사용자 목록을 불러오는 중 오류가 발생했습니다.';
    console.error(err);
  } finally {
    loading.value = false;
  }
};

const searchUsers = () => {
  searchParams.value.page = 1;
  loadUsers();
};

const goToPage = (page) => {
  if (typeof page !== 'number' || page < 1 || page > totalPages.value) return;
  searchParams.value.page = page;
  loadUsers();
};

const updateQueryParams = () => {
  const query = {};
  if (searchParams.value.role) query.role = searchParams.value.role;
  if (searchParams.value.status) query.status = searchParams.value.status;
  if (searchParams.value.keyword) query.keyword = searchParams.value.keyword;
  if (searchParams.value.page > 1) query.page = searchParams.value.page;
  router.replace({ query });
};

// --------------------------
// 사용자 관리
// --------------------------
const editUser = (user) => {
  currentUser.value = { ...user };
  resetPassword.value = false;
  userModal.value.show();
};

const saveUser = async () => {
  try {
    const userData = { ...currentUser.value };
    // if (resetPassword.value) {
    //   await adminStore.resetUserPassword(userData.userId);
    // }
    // await adminStore.updateUser(userData);
    message.value = '사용자 정보가 성공적으로 수정되었습니다.';
    userModal.value.hide();
    loadUsers();
  } catch (err) {
    error.value = '사용자 정보 저장에 실패했습니다. 다시 시도해주세요.';
    console.error(err);
  }
};

const confirmDeleteUser = (user) => {
  if (confirm(`정말로 "${user.username}" 사용자를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
    deleteUserItem(user.userId);
  }
};

const deleteUserItem = async (userId) => {
  try {
    // await adminStore.deleteUser(userId);
    message.value = '사용자가 성공적으로 삭제되었습니다.';
    loadUsers();
  } catch (err) {
    error.value = '사용자 삭제에 실패했습니다. 다시 시도해주세요.';
    console.error(err);
  }
};

const suspendUserHandler = async (userId) => {
  const reason = prompt('정지 사유를 입력해주세요:');
  if (reason === null) return;
  try {
    // await adminStore.suspendUser({ userId, reason });
    message.value = '사용자 계정이 성공적으로 정지되었습니다.';
    loadUsers();
  } catch (err) {
    error.value = '사용자 계정 정지에 실패했습니다. 다시 시도해주세요.';
    console.error(err);
  }
};

const activateUserHandler = async (userId) => {
  try {
    // await adminStore.activateUser(userId);
    message.value = '사용자 계정이 성공적으로 활성화되었습니다.';
    loadUsers();
  } catch (err) {
    error.value = '사용자 계정 활성화에 실패했습니다. 다시 시도해주세요.';
    console.error(err);
  }
};

// --------------------------
// 유틸
// --------------------------
const getRoleName = (role) => ({
  USER: '일반 사용자',
  HOST: '호스트',
  ADMIN: '관리자'
}[role] || role);

const getRoleBadgeClass = (role) => ({
  USER: 'badge bg-primary',
  HOST: 'badge bg-success',
  ADMIN: 'badge bg-danger'
}[role] || 'badge bg-secondary');

const getStatusName = (status) => ({
  ACTIVE: '활성',
  INACTIVE: '비활성',
  SUSPENDED: '정지'
}[status] || status);

const getStatusBadgeClass = (status) => ({
  ACTIVE: 'badge bg-success',
  INACTIVE: 'badge bg-secondary',
  SUSPENDED: 'badge bg-danger'
}[status] || 'badge bg-secondary');

const formatDate = (date) => {
  if (!date) return '';
  const d = new Date(date);
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

// --------------------------
// 생명주기
// --------------------------
onBeforeMount(() => {
  // 관리자 권한 확인
  if (!isAdmin.value) {
    router.push({
      path: '/error/access-denied',
      query: { message: '관리자만 접근할 수 있는 페이지입니다.' }
    });
  }
  // 쿼리 파라미터 반영
  const query = route.query;
  if (query.role) searchParams.value.role = query.role;
  if (query.status) searchParams.value.status = query.status;
  if (query.keyword) searchParams.value.keyword = query.keyword;
  if (query.page) searchParams.value.page = parseInt(query.page);
});

onMounted(() => {
  // Bootstrap 모달
  if (window.bootstrap) {
    userModal.value = new window.bootstrap.Modal(document.getElementById('userModal'));
  }
  loadUsers();
});
</script>

<style scoped>
.card { margin-bottom: 20px; }
.card-header { background-color: #f8f9fa; }
.table th { background-color: #f8f9fa; }
.table td { vertical-align: middle; }
.pagination { margin-top: 20px; }
.btn-group { white-space: nowrap; }
</style>