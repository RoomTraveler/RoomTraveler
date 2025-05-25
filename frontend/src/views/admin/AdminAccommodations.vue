<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">숙소 관리</h2>

      <!-- 검색/필터 -->
      <div class="card mb-4">
        <div class="card-body">
          <form @submit.prevent="searchAccommodations">
            <div class="row g-3">
              <div class="col-md-3">
                <label for="status" class="form-label">상태</label>
                <select id="status" class="form-select" v-model="searchParams.status">
                  <option value="">전체</option>
                  <option value="PENDING_REVIEW">승인 대기</option>
                  <option value="ACTIVE">운영 중</option>
                  <option value="REJECTED">거절됨</option>
                  <option value="INACTIVE">비활성(중지됨)</option>
                </select>
              </div>
              <div class="col-md-3">
                <label for="type" class="form-label">숙소 유형</label>
                <select id="type" class="form-select" v-model="searchParams.type">
                  <option value="">전체</option>
                  <option value="MOTEL">모텔</option>
                  <option value="HOTEL">호텔/리조트</option>
                  <option value="PENSION">펜션/풀빌라</option>
                  <option value="PREMIUM">프리미엄</option>
                  <option value="CAMPING">캠핑/글램핑</option>
                </select>
              </div>
              <div class="col-md-4">
                <label for="keyword" class="form-label">검색어</label>
                <input id="keyword" type="text" class="form-control"
                       v-model="searchParams.keyword" placeholder="숙소명, 주소, 호스트ID/명" />
              </div>
              <div class="col-md-2 d-flex align-items-end">
                <button type="submit" class="btn btn-primary w-100">검색</button>
              </div>
            </div>
          </form>
        </div>
      </div>

      <!-- 알림 메시지 -->
      <el-alert v-if="message" :title="message" type="success" show-icon @close="clearMessage" class="mb-3" />
      <el-alert v-if="error" :title="error" type="error" show-icon @close="clearError" class="mb-3" />

      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <el-skeleton :rows="5" animated />
        <p class="mt-2">숙소 정보를 불러오는 중입니다...</p>
      </div>

      <!-- 숙소 목록 -->
      <div v-else>
        <div v-if="!accommodations || accommodations.length === 0" class="alert alert-info">
          검색 결과가 없습니다.
        </div>
        <el-table v-else :data="accommodations" stripe style="width: 100%" class="mb-3">
            <el-table-column prop="accommodationId" label="ID" width="80" align="center"/>
            <el-table-column label="숙소명" min-width="200">
                <template #default="{ row }">
                    <router-link :to="`/accommodation/${row.accommodationId}`" class="text-decoration-none">
                        {{ row.title }}
                    </router-link>
                </template>
            </el-table-column>
            <el-table-column prop="accommodationType" label="유형" width="120">
                <template #default="{ row }">
                    {{ getAccommodationTypeName(row.accommodationType) }}
                </template>
            </el-table-column>
            <el-table-column label="호스트ID" width="100" align="center">
                 <template #default="{ row }">
                    <!-- 호스트 상세 페이지가 있다면 링크 추가 -->
                    <span>{{ row.hostId }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="address" label="주소 (간략)" min-width="180">
                <template #default="{ row }">
                    {{ getMainAddress(row.address) }}
                </template>
            </el-table-column>
            <el-table-column label="상태" width="120" align="center">
                <template #default="{ row }">
                    <el-tag :type="getStatusTagType(row.status)" size="small">
                        {{ getStatusName(row.status) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="등록일" width="120" align="center">
                 <template #default="{ row }">
                    {{ formatDate(row.createdAt) }}
                </template>
            </el-table-column>
            <el-table-column label="관리" width="180" align="center" fixed="right">
                <template #default="{ row }">
                    <el-button-group size="small">
                        <el-button 
                            v-if="row.status === 'PENDING_REVIEW'" 
                            type="success" 
                            @click="handleStatusUpdate(row.accommodationId, 'ACTIVE')">승인</el-button>
                        <el-button 
                            v-if="row.status === 'PENDING_REVIEW'" 
                            type="danger" 
                            @click="promptRejectReason(row.accommodationId)">거절</el-button>
                        <el-button 
                            v-if="row.status === 'ACTIVE'" 
                            type="warning" 
                            @click="promptSuspendReason(row.accommodationId)">운영중지</el-button>
                        <el-button 
                            v-if="row.status === 'REJECTED' || row.status === 'INACTIVE'" 
                            type="primary" 
                            @click="handleStatusUpdate(row.accommodationId, 'PENDING_REVIEW')">재검토요청</el-button>
                        <el-button 
                            type="danger" 
                            outline 
                            @click="onDelete(row.accommodationId)">삭제</el-button>
                    </el-button-group>
                </template>
            </el-table-column>
        </el-table>

        <!-- 페이지네이션 -->
        <div class="d-flex justify-content-center mt-4" v-if="totalPages > 1">
            <el-pagination
                background
                layout="prev, pager, next, jumper, ->, total"
                :total="totalElements"
                :page-size="searchParams.size"
                :current-page="currentPageForPaginator"
                @current-change="goToPage"
            />
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { useAdminStore } from '@/store/adminStore';
import { useRouter, useRoute } from 'vue-router';
import Layout from '@/components/layout/Layout.vue';
import { ElMessageBox, ElNotification, ElTable, ElTableColumn, ElTag, ElButton, ElButtonGroup, ElPagination, ElAlert, ElSkeleton } from 'element-plus';

const adminStore = useAdminStore();
const router = useRouter();
const route = useRoute();

const loading = computed(() => adminStore.loading);
const accommodations = computed(() => adminStore.accommodations);
const totalPages = computed(() => adminStore.accommodationTotalPages);
const currentPageFromStore = computed(() => adminStore.accommodationCurrentPage);
const totalElements = computed(() => adminStore.accommodations.length > 0 ? adminStore.accommodationTotalElements : 0);

const searchParams = ref({
  status: route.query.status || '',
  type: route.query.type || '',
  keyword: route.query.keyword || '',
  page: route.query.page ? Math.max(1, parseInt(route.query.page, 10) || 1) : 1,
  size: 10,
  sortBy: 'createdAt',
  sortDirection: 'DESC'
});

const message = computed(() => adminStore.message);
const error = computed(() => adminStore.error);

const currentPageForPaginator = computed(() => searchParams.value.page);

function clearMessage() { adminStore._setMessage(''); }
function clearError() { adminStore._setError(''); }

const loadAccommodations = async () => {
  adminStore._clearMessages();
  const apiParams = { ...searchParams.value, page: searchParams.value.page - 1 };
  console.log('[AdminAccommodations] loadAccommodations - apiParams:', apiParams);
  console.log('[AdminAccommodations] BEFORE fetch - loading:', loading.value, 'totalPages:', totalPages.value, 'totalElements:', totalElements.value, 'currentPage:', currentPageForPaginator.value);
  try {
    await adminStore.fetchAdminAccommodations(apiParams);
    console.log('[AdminAccommodations] AFTER fetch - loading:', loading.value, 'totalPages:', totalPages.value, 'totalElements:', totalElements.value, 'currentPage:', currentPageForPaginator.value);
    console.log('[AdminAccommodations] AFTER fetch - accommodations length:', accommodations.value?.length);
    updateRouterQuery();
  } catch (e) {
    console.error("[AdminAccommodations] 숙소 목록 로딩 실패 Front:", e);
    console.log('[AdminAccommodations] CATCH fetch - loading:', loading.value, 'totalPages:', totalPages.value, 'totalElements:', totalElements.value, 'currentPage:', currentPageForPaginator.value);
  }
};

const searchAccommodations = () => {
  searchParams.value.page = 1;
  loadAccommodations();
};

const goToPage = (page) => {
  if (page < 1 || page > totalPages.value) return;
  searchParams.value.page = page;
  loadAccommodations();
};

const updateRouterQuery = () => {
  const query = {};
  if (searchParams.value.status) query.status = searchParams.value.status;
  if (searchParams.value.type) query.type = searchParams.value.type;
  if (searchParams.value.keyword) query.keyword = searchParams.value.keyword;
  if (searchParams.value.page > 1) query.page = searchParams.value.page.toString();
  router.replace({ query }).catch(err => {
    if (err.name !== 'NavigationDuplicated') {
      console.error('Router replace error:', err);
    }
  });
};

const getAccommodationTypeName = (type) => ({
  'MOTEL': '모텔',
  'HOTEL': '호텔',
  'RESORT': '리조트',
  'PENSION': '펜션',
  'POOLVILLA': '풀빌라',
  'GUEST_HOUSE': '게스트하우스',
  'HANOK': '한옥',
  'CAMPING': '캠핑/글램핑',
  'PREMIUM': '프리미엄',
  'OTHER': '기타'
}[type] || type || 'N/A');

const getStatusName = (status) => ({
  'PENDING_REVIEW': '승인대기',
  'ACTIVE': '운영중',
  'INACTIVE': '운영중지',
  'REJECTED': '거절됨',
}[status] || status || 'N/A');

const getStatusTagType = (status) => ({
  'PENDING_REVIEW': 'warning',
  'ACTIVE': 'success',
  'INACTIVE': 'info',
  'REJECTED': 'danger',
}[status] || 'primary');

function formatDate(dateArray) {
  if (!dateArray || !Array.isArray(dateArray)) {
      if (typeof dateArray === 'string') {
          const date = new Date(dateArray);
          if (!isNaN(date)) {
              return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
          }
      }
      return 'N/A';
  }
  if (dateArray.length < 3) return 'N/A';
  const date = new Date(dateArray[0], dateArray[1] - 1, dateArray[2]);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

const handleStatusUpdate = async (accommodationId, newStatus, reason = null) => {
  try {
    await adminStore.updateAccommodationStatus({ accommodationId, status: newStatus, reason });
    ElNotification({ title: '성공', message: `숙소 상태가 ${getStatusName(newStatus)}로 변경되었습니다.`, type: 'success' });
    loadAccommodations();
  } catch (err) {
    console.error("숙소 상태 변경 오류 Front:", err);
    ElNotification({ title: '오류', message: adminStore.error || '숙소 상태 변경 실패', type: 'error' });
  }
};

const promptRejectReason = async (accommodationId) => {
  try {
    const { value } = await ElMessageBox.prompt('숙소 등록 신청을 거절하는 사유를 입력해주세요.', '거절 사유 입력', {
      confirmButtonText: '제출',
      cancelButtonText: '취소',
      inputPattern: /.+/,
      inputErrorMessage: '거절 사유를 반드시 입력해야 합니다.',
    });
    if (value) {
      await handleStatusUpdate(accommodationId, 'REJECTED', value);
    }
  } catch (e) { /* cancel */ }
};

const promptSuspendReason = async (accommodationId) => {
  try {
    const { value } = await ElMessageBox.prompt('숙소 운영을 중지하는 사유를 입력해주세요.', '운영 중지 사유 입력', {
      confirmButtonText: '제출',
      cancelButtonText: '취소',
    });
    if (value !== undefined) {
        await handleStatusUpdate(accommodationId, 'INACTIVE', value || '관리자에 의한 운영 중지');
    }
  } catch (e) { /* cancel */ }
};

const onDelete = async (id) => {
  try {
    await ElMessageBox.confirm(
      `숙소 ID [${id}]을(를) 정말 삭제하시겠습니까? 관련된 객실, 예약 등 모든 정보가 삭제되며 복구할 수 없습니다.`,
      '삭제 확인',
      {
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        type: 'error',
      }
    );
    await adminStore.deleteAdminAccommodation(id);
    ElNotification({ title: '성공', message: '숙소가 삭제되었습니다.', type: 'success' });
    loadAccommodations();
  } catch (e) {
    if (e !== 'cancel') {
      console.error("숙소 삭제 오류 Front:", e);
      ElNotification({ title: '오류', message: adminStore.error || '숙소 삭제 실패', type: 'error' });
    }
  }
};

onMounted(() => {
  searchParams.value.status = route.query.status || '';
  searchParams.value.type = route.query.type || '';
  searchParams.value.keyword = route.query.keyword || '';
  searchParams.value.page = route.query.page ? Math.max(1, parseInt(route.query.page, 10) || 1) : 1;
  loadAccommodations();
});

watch(() => route.query, (newQuery) => {
    searchParams.value.status = newQuery.status || '';
    searchParams.value.type = newQuery.type || '';
    searchParams.value.keyword = newQuery.keyword || '';
    searchParams.value.page = newQuery.page ? Math.max(1, parseInt(newQuery.page, 10) || 1) : 1;
    loadAccommodations();
}, { deep: true });

function getMainAddress(fullAddress) {
    if (!fullAddress) return 'N/A';
    const parts = fullAddress.split(' ');
    if (parts.length > 2) {
        return `${parts[0]} ${parts[1]}`;
    }
    return fullAddress;
}
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
.text-decoration-none {
    text-decoration: none;
}
.text-decoration-none:hover {
    text-decoration: underline;
}
</style>
