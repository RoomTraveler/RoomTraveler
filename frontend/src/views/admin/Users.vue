<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">사용자 관리</h2>

      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>
      <!-- 에러 메시지 -->
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>

      <!-- 검색 및 필터링 -->
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
                      title="사용자 정보 수정"
                  >
                    <i class="bi bi-pencil"></i>
                  </button>
                  <button
                      v-if="user.status === 'ACTIVE'"
                      class="btn btn-outline-warning"
                      @click="suspendUserPrompt(user)"
                      title="사용자 계정 정지"
                  >
                    <i class="bi bi-pause-fill"></i>
                  </button>
                  <button
                      v-if="user.status === 'SUSPENDED'"
                      class="btn btn-outline-success"
                      @click="activateUser(user.userId)"
                      title="사용자 계정 활성화"
                  >
                    <i class="bi bi-play-fill"></i>
                  </button>
                  <button
                      class="btn btn-outline-danger"
                      @click="confirmDeleteUser(user)"
                      title="사용자 계정 삭제"
                  >
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
            <li
                v-for="page in paginationItems"
                :key="page"
                class="page-item"
                :class="{ active: page === currentPage, disabled: page === '...' }"
            >
              <a class="page-link" href="#" @click.prevent="page !== '...' && goToPage(page)">{{ page }}</a>
            </li>
            <li class="page-item" :class="{ disabled: currentPage === totalPages }">
              <a class="page-link" href="#" @click.prevent="goToPage(currentPage + 1)">다음</a>
            </li>
          </ul>
        </nav>
      </div>

      <!-- 사용자 수정 모달 -->
      <div class="modal fade" ref="userModalRef" id="userModal" tabindex="-1" aria-labelledby="userModalLabel" aria-hidden="true">
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
/**
 * 사용자 관리 (Vue3 Composition API + Bootstrap)
 */
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Layout from '@/components/layout/Layout.vue'

// 예시로 제공하는 store/dispatch (실제 adminStore로 교체하거나 API 연동)
import { useAdminStore } from '@/stores/adminStore' // Pinia 예시, Vuex 쓰면 actions 방식으로 변경
const adminStore = useAdminStore()

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const message = ref('')
const error = ref('')
const users = ref([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(1)
const searchParams = reactive({
  role: '',
  status: '',
  keyword: '',
  page: 1,
  size: 10,
})

const currentUser = reactive({
  userId: null,
  username: '',
  email: '',
  phone: '',
  role: 'USER',
  status: 'ACTIVE'
})
const resetPassword = ref(false)
const userModalRef = ref(null)
let userModalInstance = null

// 페이지네이션 계산
const paginationItems = computed(() => {
  const items = []
  const maxVisiblePages = 5
  if (totalPages.value <= maxVisiblePages) {
    for (let i = 1; i <= totalPages.value; i++) items.push(i)
  } else {
    items.push(1)
    if (currentPage.value > 3) items.push('...')
    const start = Math.max(2, currentPage.value - 1)
    const end = Math.min(totalPages.value - 1, currentPage.value + 1)
    for (let i = start; i <= end; i++) items.push(i)
    if (currentPage.value < totalPages.value - 2) items.push('...')
    items.push(totalPages.value)
  }
  return items
})

// 역할/상태 이름, 배지
function getRoleName(role) {
  return { USER: '일반 사용자', HOST: '호스트', ADMIN: '관리자' }[role] || role
}
function getRoleBadgeClass(role) {
  return { USER: 'badge bg-primary', HOST: 'badge bg-success', ADMIN: 'badge bg-danger' }[role] || 'badge bg-secondary'
}
function getStatusName(status) {
  return { ACTIVE: '활성', INACTIVE: '비활성', SUSPENDED: '정지' }[status] || status
}
function getStatusBadgeClass(status) {
  return { ACTIVE: 'badge bg-success', INACTIVE: 'badge bg-secondary', SUSPENDED: 'badge bg-danger' }[status] || 'badge bg-secondary'
}
// 날짜 포맷
function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

// 사용자 목록 불러오기
async function loadUsers() {
  loading.value = true
  try {
    const result = await adminStore.fetchUsers({ ...searchParams, page: searchParams.page, size: searchParams.size })
    users.value = result.content
    totalItems.value = result.totalElements
    totalPages.value = result.totalPages
    currentPage.value = result.number + 1
    updateQueryParams()
  } catch (e) {
    error.value = '사용자 목록을 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

// 검색
function searchUsers() {
  searchParams.page = 1
  loadUsers()
}

// 페이지 이동
function goToPage(page) {
  if (typeof page !== 'number' || page < 1 || page > totalPages.value) return
  searchParams.page = page
  loadUsers()
}

// 쿼리 파라미터 업데이트
function updateQueryParams() {
  const query = {}
  if (searchParams.role) query.role = searchParams.role
  if (searchParams.status) query.status = searchParams.status
  if (searchParams.keyword) query.keyword = searchParams.keyword
  if (searchParams.page > 1) query.page = searchParams.page
  router.replace({ query })
}

// 모달 제어
function showModal() {
  if (!userModalInstance && window.bootstrap) {
    userModalInstance = new window.bootstrap.Modal(userModalRef.value)
  }
  userModalInstance?.show()
}
function hideModal() {
  userModalInstance?.hide()
}

// 사용자 수정 모달 표시
function editUser(user) {
  Object.assign(currentUser, user)
  resetPassword.value = false
  showModal()
}

// 사용자 정보 저장
async function saveUser() {
  try {
    const userData = { ...currentUser }
    if (resetPassword.value) {
      await adminStore.resetUserPassword(userData.userId)
    }
    await adminStore.updateUser(userData)
    message.value = '사용자 정보가 성공적으로 수정되었습니다.'
    hideModal()
    loadUsers()
  } catch (e) {
    error.value = '사용자 정보 저장에 실패했습니다. 다시 시도해주세요.'
  }
}

// 삭제/정지/활성화
function confirmDeleteUser(user) {
  if (confirm(`정말로 "${user.username}" 사용자를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
    deleteUserItem(user.userId)
  }
}
async function deleteUserItem(userId) {
  try {
    await adminStore.deleteUser(userId)
    message.value = '사용자가 성공적으로 삭제되었습니다.'
    loadUsers()
  } catch (e) {
    error.value = '사용자 삭제에 실패했습니다. 다시 시도해주세요.'
  }
}
function suspendUserPrompt(user) {
  const reason = prompt('정지 사유를 입력해주세요:')
  if (reason !== null) suspendUser(user.userId, reason)
}
async function suspendUser(userId, reason) {
  try {
    await adminStore.suspendUser({ userId, reason })
    message.value = '사용자 계정이 성공적으로 정지되었습니다.'
    loadUsers()
  } catch (e) {
    error.value = '사용자 계정 정지에 실패했습니다. 다시 시도해주세요.'
  }
}
async function activateUser(userId) {
  try {
    await adminStore.activateUser(userId)
    message.value = '사용자 계정이 성공적으로 활성화되었습니다.'
    loadUsers()
  } catch (e) {
    error.value = '사용자 계정 활성화에 실패했습니다. 다시 시도해주세요.'
  }
}

// onMounted: 관리자 권한 체크, 초기 쿼리 파싱 및 유저 목록
onMounted(() => {
  // 관리자 권한 체크(예시: 실제 프로젝트에 맞게 변경)
  if (adminStore.role !== 'ADMIN') {
    router.push({ path: '/error/access-denied', query: { message: '관리자만 접근할 수 있는 페이지입니다.' } })
    return
  }
  // 쿼리 파라미터에서 검색 조건 초기화
  const query = route.query
  if (query.role) searchParams.role = query.role
  if (query.status) searchParams.status = query.status
  if (query.keyword) searchParams.keyword = query.keyword
  if (query.page) searchParams.page = parseInt(query.page)
  loadUsers()
})
</script>

<style scoped>
.card { margin-bottom: 20px; }
.card-header { background-color: #f8f9fa; }
.table th { background-color: #f8f9fa; }
.table td { vertical-align: middle; }
.pagination { margin-top: 20px; }
.btn-group { white-space: nowrap; }
</style>
