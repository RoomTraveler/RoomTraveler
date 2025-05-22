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
                <select id="status" class="form-select" v-model="searchParams.status">
                  <option value="">전체</option>
                  <option value="PENDING">승인 대기</option>
                  <option value="APPROVED">승인됨</option>
                  <option value="REJECTED">거부됨</option>
                  <option value="SUSPENDED">중지됨</option>
                </select>
              </div>
              <div class="col-md-3">
                <label for="type" class="form-label">숙소 유형</label>
                <select id="type" class="form-select" v-model="searchParams.type">
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
                <input id="keyword" type="text" class="form-control"
                       v-model="searchParams.keyword" placeholder="숙소명, 주소, 호스트명" />
              </div>
              <div class="col-md-2 d-flex align-items-end">
                <button type="submit" class="btn btn-primary w-100">검색</button>
              </div>
            </div>
          </form>
        </div>
      </div>

      <!-- 로딩 표시 -->
      <div v-if="loading.value" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">숙소 정보를 불러오는 중입니다...</p>
      </div>

      <!-- 숙소 목록 -->
      <div v-else>
        <div v-if="accommodations.value.length === 0" class="alert alert-info">
          검색 결과가 없습니다.
        </div>
        <div v-else class="table-responsive">
          <table class="table table-striped table-hover align-middle">
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
            <tr v-for="item in accommodations.value" :key="item.accommodationId">
              <td>{{ item.accommodationId }}</td>
              <td>
                <router-link :to="`/accommodation/detail/${item.accommodationId}`">
                  {{ item.title }}
                </router-link>
              </td>
              <td>{{ getAccommodationTypeName(item.accommodationType) }}</td>
              <td>{{ item.hostName }}</td>
              <td>{{ item.address }}</td>
              <td>
                  <span :class="getStatusBadgeClass(item.status)">
                    {{ getStatusName(item.status) }}
                  </span>
              </td>
              <td>{{ formatDate(item.createdAt) }}</td>
              <td>
                <div class="btn-group btn-group-sm" role="group">
                  <button v-if="item.status === 'PENDING'"
                          class="btn btn-success"
                          @click="onApprove(item.accommodationId)">승인</button>
                  <button v-if="item.status === 'PENDING'"
                          class="btn btn-danger"
                          @click="onReject(item.accommodationId)">거부</button>
                  <button v-if="item.status === 'APPROVED'"
                          class="btn btn-warning"
                          @click="onSuspend(item.accommodationId)">중지</button>
                  <button v-if="item.status === 'REJECTED' || item.status === 'SUSPENDED'"
                          class="btn btn-success"
                          @click="onApprove(item.accommodationId)">복원</button>
                  <button
                      class="btn btn-outline-danger"
                      @click="onDelete(item.accommodationId)">삭제</button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

        <!-- 페이지네이션 -->
        <nav v-if="totalPages.value > 1" aria-label="Page navigation">
          <ul class="pagination justify-content-center">
            <li class="page-item" :class="{ disabled: currentPage.value === 1 }">
              <a class="page-link" href="#" @click.prevent="goToPage(currentPage.value - 1)">이전</a>
            </li>
            <li v-for="page in paginationItems.value" :key="page"
                class="page-item" :class="{ active: page === currentPage.value, disabled: page === '...' }">
              <a class="page-link" href="#" @click.prevent="page !== '...' && goToPage(page)">{{ page }}</a>
            </li>
            <li class="page-item" :class="{ disabled: currentPage.value === totalPages.value }">
              <a class="page-link" href="#" @click.prevent="goToPage(currentPage.value + 1)">다음</a>
            </li>
          </ul>
        </nav>
      </div>
    </div>
  </Layout>
</template>

<script lang="ts" setup>
// =============================
//   관리자용 숙소 관리 페이지
//   - Bootstrap 기반
//   - Vue3 Composition API
// =============================

import { ref, computed, onMounted, watch } from 'vue';
import { useStore } from 'vuex';
import { useRouter, useRoute } from 'vue-router';
import Layout from '@/components/layout/Layout.vue';

// --- 전역 상태 및 라우터 사용
const store = useStore();
const router = useRouter();
const route = useRoute();

// --- 반응형 데이터 정의
const loading = ref(true);
const accommodations = ref<any[]>([]);
const totalItems = ref(0);
const totalPages = ref(0);
const currentPage = ref(1);

const searchParams = ref({
  status: '',
  type: '',
  keyword: '',
  page: 1,
  size: 10
});

// --- 관리자 권한 확인 (vuex)
const isAdmin = computed(() => store.state.user.user?.role === 'ADMIN');

// --- 숙소 목록 불러오기
const loadAccommodations = async () => {
  loading.value = true;
  try {
    const result = await store.dispatch('admin/fetchAccommodations', searchParams.value);
    accommodations.value = result.content;
    totalItems.value = result.totalElements;
    totalPages.value = result.totalPages;
    currentPage.value = result.number + 1;
    updateQueryParams();
  } catch (err) {
    console.error('숙소 목록 오류:', err);
  } finally {
    loading.value = false;
  }
};

// --- 검색 폼 제출
const searchAccommodations = () => {
  searchParams.value.page = 1;
  loadAccommodations();
};

// --- 페이지 이동
const goToPage = (page: number) => {
  if (page < 1 || page > totalPages.value) return;
  searchParams.value.page = page;
  loadAccommodations();
};

// --- 쿼리 파라미터 업데이트 (URL 주소)
const updateQueryParams = () => {
  const query: any = {};
  if (searchParams.value.status) query.status = searchParams.value.status;
  if (searchParams.value.type) query.type = searchParams.value.type;
  if (searchParams.value.keyword) query.keyword = searchParams.value.keyword;
  if (searchParams.value.page > 1) query.page = searchParams.value.page;
  router.replace({ query });
};

// --- 페이지네이션 계산
const paginationItems = computed(() => {
  const items: (number | string)[] = [];
  const maxVisible = 5;
  if (totalPages.value <= maxVisible) {
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

// --- 숙소 유형 한글 반환
const getAccommodationTypeName = (type: string) => ({
  'HOTEL': '호텔',
  'MOTEL': '모텔',
  'PENSION': '펜션',
  'GUEST_HOUSE': '게스트하우스',
  'RESORT': '리조트',
  'CONDO': '콘도',
  'HANOK': '한옥',
  'CAMPING': '캠핑/글램핑',
  'OTHER': '기타'
}[type] || type);

// --- 상태 한글 반환
const getStatusName = (status: string) => ({
  'PENDING': '승인 대기',
  'APPROVED': '승인됨',
  'REJECTED': '거부됨',
  'SUSPENDED': '중지됨'
}[status] || status);

// --- 상태 뱃지 부트스트랩 클래스 반환
const getStatusBadgeClass = (status: string) => ({
  'PENDING': 'badge bg-warning text-dark',
  'APPROVED': 'badge bg-success',
  'REJECTED': 'badge bg-danger',
  'SUSPENDED': 'badge bg-secondary'
}[status] || 'badge bg-secondary');

// --- 날짜 YYYY-MM-DD 포맷
const formatDate = (date: string | Date) => {
  if (!date) return '';
  const d = new Date(date);
  return `${d.getFullYear()}-${(d.getMonth()+1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`;
};

// --- 승인/거부/중지/삭제(복원) 액션
const onApprove = async (id: number) => {
  if (!window.confirm('이 숙소를 승인/복원하시겠습니까?')) return;
  await store.dispatch('admin/approveAccommodation', id);
  loadAccommodations();
};
const onReject = async (id: number) => {
  const reason = window.prompt('거부 사유를 입력하세요:');
  if (reason === null) return;
  await store.dispatch('admin/rejectAccommodation', { accommodationId: id, reason });
  loadAccommodations();
};
const onSuspend = async (id: number) => {
  const reason = window.prompt('중지 사유를 입력하세요:');
  if (reason === null) return;
  await store.dispatch('admin/suspendAccommodation', { accommodationId: id, reason });
  loadAccommodations();
};
const onDelete = async (id: number) => {
  if (!window.confirm('정말 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return;
  await store.dispatch('admin/deleteAccommodation', id);
  loadAccommodations();
};

// --- 권한 체크 및 초기 데이터 세팅
onMounted(() => {
  if (!isAdmin.value) {
    router.push({ path: '/error/access-denied', query: { message: '관리자만 접근할 수 있는 페이지입니다.' } });
    return;
  }
  // 쿼리 파라미터에서 검색조건 세팅
  const q = route.query;
  if (q.status) searchParams.value.status = q.status as string;
  if (q.type) searchParams.value.type = q.type as string;
  if (q.keyword) searchParams.value.keyword = q.keyword as string;
  if (q.page) searchParams.value.page = parseInt(q.page as string);
  loadAccommodations();
});
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
