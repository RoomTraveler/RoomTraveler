<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">숙소 관리</h2>

      <!-- 검색 및 필터링 -->
      <div class="card mb-4">
        <div class="card-body">
          <form @submit.prevent="searchAccommodations">
            <div class="row g-3">
              <div class="col-md-3">
                <label for="status" class="form-label">상태</label>
                <select class="form-select" id="status" v-model="searchParams.status">
                  <option value="">전체</option>
                  <option value="PENDING">승인 대기</option>
                  <option value="APPROVED">승인됨</option>
                  <option value="REJECTED">거부됨</option>
                  <option value="SUSPENDED">중지됨</option>
                </select>
              </div>
              <div class="col-md-3">
                <label for="type" class="form-label">숙소 유형</label>
                <select class="form-select" id="type" v-model="searchParams.type">
                  <option value="">전체</option>
                  <option value="HOTEL">호텔</option>
                  <option value="MOTEL">모텔</option>
                  <option value="PENSION">펜션</option>
                  <option value="GUEST_HOUSE">게스트하우스</option>
                  <option value="RESORT">리조트</option>
                  <option value="CONDO">콘도</option>
                  <option value="HANOK">한옥</option>
                  <option value="CAMPING">캠핑/글램핑</option>
                  <option value="OTHER">기타</option>
                </select>
              </div>
              <div class="col-md-4">
                <label for="keyword" class="form-label">검색어</label>
                <input type="text" class="form-control" id="keyword" v-model="searchParams.keyword" placeholder="숙소명, 주소, 호스트명">
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
        <p class="mt-2">숙소 정보를 불러오는 중입니다...</p>
      </div>

      <!-- 숙소 목록 -->
      <div v-else>
        <div v-if="accommodations.length === 0" class="alert alert-info">
          검색 결과가 없습니다.
        </div>
        <div v-else class="table-responsive">
          <table class="table table-striped table-hover">
            <thead>
              <tr>
                <th>ID</th>
                <th>숙소명</th>
                <th>유형</th>
                <th>호스트</th>
                <th>주소</th>
                <th>상태</th>
                <th>등록일</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="accommodation in accommodations" :key="accommodation.accommodationId">
                <td>{{ accommodation.accommodationId }}</td>
                <td>
                  <router-link :to="`/accommodation/detail/${accommodation.accommodationId}`">
                    {{ accommodation.title }}
                  </router-link>
                </td>
                <td>{{ getAccommodationTypeName(accommodation.accommodationType) }}</td>
                <td>{{ accommodation.hostName }}</td>
                <td>{{ accommodation.address }}</td>
                <td>
                  <span :class="getStatusBadgeClass(accommodation.status)">
                    {{ getStatusName(accommodation.status) }}
                  </span>
                </td>
                <td>{{ formatDate(accommodation.createdAt) }}</td>
                <td>
                  <div class="btn-group btn-group-sm">
                    <button 
                      v-if="accommodation.status === 'PENDING'" 
                      @click="approveAccommodation(accommodation.accommodationId)" 
                      class="btn btn-success"
                    >
                      승인
                    </button>
                    <button 
                      v-if="accommodation.status === 'PENDING'" 
                      @click="rejectAccommodation(accommodation.accommodationId)" 
                      class="btn btn-danger"
                    >
                      거부
                    </button>
                    <button 
                      v-if="accommodation.status === 'APPROVED'" 
                      @click="suspendAccommodation(accommodation.accommodationId)" 
                      class="btn btn-warning"
                    >
                      중지
                    </button>
                    <button 
                      v-if="accommodation.status === 'REJECTED' || accommodation.status === 'SUSPENDED'" 
                      @click="approveAccommodation(accommodation.accommodationId)" 
                      class="btn btn-success"
                    >
                      복원
                    </button>
                    <button 
                      @click="deleteAccommodation(accommodation.accommodationId)" 
                      class="btn btn-outline-danger"
                    >
                      삭제
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
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/userStore'
import Layout from '@/components/layout/Layout.vue'

// Pinia store
// const adminStore = useAdminStore()
const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

// 상태
const loading = ref(true)
const accommodations = ref([])
const totalItems = ref(0)
const totalPages = ref(0)
const currentPage = ref(1)

// 검색 파라미터
const searchParams = ref({
  status: '',
  type: '',
  keyword: '',
  page: 1,
  size: 10,
})

// 관리자 권한 체크
const isAdmin = computed(() => userStore.userRole === 'ADMIN')

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

// 유틸 함수
function getAccommodationTypeName(type) {
  const types = {
    'HOTEL': '호텔',
    'MOTEL': '모텔',
    'PENSION': '펜션',
    'GUEST_HOUSE': '게스트하우스',
    'RESORT': '리조트',
    'CONDO': '콘도',
    'HANOK': '한옥',
    'CAMPING': '캠핑/글램핑',
    'OTHER': '기타'
  }
  return types[type] || type
}
function getStatusName(status) {
  const statuses = {
    'PENDING': '승인 대기',
    'APPROVED': '승인됨',
    'REJECTED': '거부됨',
    'SUSPENDED': '중지됨'
  }
  return statuses[status] || status
}
function getStatusBadgeClass(status) {
  const classes = {
    'PENDING': 'badge bg-warning',
    'APPROVED': 'badge bg-success',
    'REJECTED': 'badge bg-danger',
    'SUSPENDED': 'badge bg-secondary'
  }
  return classes[status] || 'badge bg-secondary'
}
function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 숙소 목록 불러오기
async function loadAccommodations() {
  loading.value = true
  try {
    // const result = await adminStore.fetchAccommodations(searchParams.value)
    accommodations.value = result.content
    totalItems.value = result.totalElements
    totalPages.value = result.totalPages
    currentPage.value = result.number + 1
    updateQueryParams()
  } catch (e) {
    console.error('숙소 목록을 불러오는 중 오류가 발생했습니다:', e)
  } finally {
    loading.value = false
  }
}

// URL 쿼리 파라미터 반영
function updateQueryParams() {
  const query = {}
  if (searchParams.value.status) query.status = searchParams.value.status
  if (searchParams.value.type) query.type = searchParams.value.type
  if (searchParams.value.keyword) query.keyword = searchParams.value.keyword
  if (searchParams.value.page > 1) query.page = searchParams.value.page
  router.replace({ query })
}

// 검색
function searchAccommodations() {
  searchParams.value.page = 1
  loadAccommodations()
}

// 페이지 이동
function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  searchParams.value.page = page
  loadAccommodations()
}

// 승인/거부/중지/삭제
async function approveAccommodation(accommodationId) {
  if (!window.confirm('이 숙소를 승인하시겠습니까?')) return
  try {
    // await adminStore.approveAccommodation(accommodationId)
    loadAccommodations()
  } catch (e) {
    alert('숙소 승인에 실패했습니다. 다시 시도해주세요.')
  }
}
async function rejectAccommodation(accommodationId) {
  const reason = window.prompt('거부 사유를 입력해주세요:')
  if (reason === null) return
  try {
    // await adminStore.rejectAccommodation({ accommodationId, reason })
    loadAccommodations()
  } catch (e) {
    alert('숙소 거부에 실패했습니다. 다시 시도해주세요.')
  }
}
async function suspendAccommodation(accommodationId) {
  const reason = window.prompt('중지 사유를 입력해주세요:')
  if (reason === null) return
  try {
    // await adminStore.suspendAccommodation({ accommodationId, reason })
    loadAccommodations()
  } catch (e) {
    alert('숙소 중지에 실패했습니다. 다시 시도해주세요.')
  }
}
async function deleteAccommodation(accommodationId) {
  if (!window.confirm('이 숙소를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return
  try {
    // await adminStore.deleteAccommodation(accommodationId)
    loadAccommodations()
  } catch (e) {
    alert('숙소 삭제에 실패했습니다. 다시 시도해주세요.')
  }
}

// onMounted - 권한체크 & 쿼리 적용 & 데이터 불러오기
onMounted(() => {
  if (!isAdmin.value) {
    router.push({
      path: '/error/access-denied',
      query: { message: '관리자만 접근할 수 있는 페이지입니다.' }
    })
    return
  }
  const query = route.query
  if (query.status) searchParams.value.status = query.status
  if (query.type) searchParams.value.type = query.type
  if (query.keyword) searchParams.value.keyword = query.keyword
  if (query.page) searchParams.value.page = parseInt(query.page)
  loadAccommodations()
})
</script>

<style scoped>
.table th {
  background-color: #f8f9fa;
}
.pagination {
  margin-top: 20px;
}
.btn-group {
  white-space: nowrap;
}
</style>