<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">숙소 신청 관리 (승인 대기)</h2>

      <!-- 알림 메시지 -->
      <el-alert v-if="message" :title="message" type="success" show-icon @close="clearMessage" class="mb-3" />
      <el-alert v-if="error" :title="error" type="error" show-icon @close="clearError" class="mb-3" />

      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <el-skeleton :rows="5" animated />
        <p class="mt-2">숙소 신청 목록을 불러오는 중입니다...</p>
      </div>

      <!-- 숙소 신청 목록 -->
      <div v-else>
        <div v-if="!pendingAccommodations || pendingAccommodations.length === 0" class="alert alert-info">
          승인 대기 중인 숙소 신청이 없습니다.
        </div>
        <div v-else>
          <el-table :data="pendingAccommodations" stripe style="width: 100%" class="mb-3">
            <el-table-column prop="accommodationId" label="숙소ID" width="90" align="center" />
            <el-table-column prop="title" label="숙소명" min-width="200">
              <template #default="{ row }">
                <span>{{ row.title }}</span>
              </template>
            </el-table-column>
            <el-table-column label="호스트ID" width="100" align="center">
                <template #default="{ row }">
                    <span>{{ row.hostId }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="address" label="주소 (간략)" min-width="250">
                <template #default="{ row }">
                    <span>{{ getMainAddress(row.address) }}</span>
                </template>
            </el-table-column>
            <el-table-column prop="accommodationType" label="유형" width="120" />
            <el-table-column label="신청일" width="120" align="center">
              <template #default="{ row }">
                {{ formatDate(row.createdAt) }}
              </template>
            </el-table-column>
            <el-table-column label="관리" width="180" align="center">
              <template #default="{ row }">
                <el-button-group size="small">
                  <el-button type="success" @click="handleApprove(row.accommodationId)">승인</el-button>
                  <el-button type="danger" @click="handleReject(row.accommodationId)">거절</el-button>
                </el-button-group>
              </template>
            </el-table-column>
          </el-table>

          <!-- 페이징 -->
          <div class="d-flex justify-content-center mt-4" v-if="totalPages > 1">
            <el-pagination
              background
              layout="prev, pager, next"
              :total="totalElements"
              :page-size="currentPageSize"
              :current-page="currentPageForPaginator"
              @current-change="handlePageChange"
            />
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { useAdminStore } from '@/store/adminStore';
import { useRouter } from 'vue-router';
import Layout from '@/components/layout/Layout.vue';
import { ElMessageBox, ElNotification } from 'element-plus';

const adminStore = useAdminStore();
const router = useRouter();

const loading = computed(() => adminStore.loading);
const pendingAccommodations = computed(() => adminStore.pendingAccommodations);
const totalPages = computed(() => adminStore.pendingAccommodationsTotalPages);
const currentPage = computed(() => adminStore.pendingAccommodationsCurrentPage); // 0-based
const totalElements = computed(() => adminStore.pendingAccommodationsTotalElements);
const currentPageSize = ref(10); // 페이지 당 항목 수, API 요청 시 size 파라미터와 일치

const message = computed(() => adminStore.message);
const error = computed(() => adminStore.error);

const currentPageForPaginator = computed(() => currentPage.value + 1);

function clearMessage() {
  adminStore._setMessage('');
}
function clearError() {
  adminStore._setError('');
}

function formatDate(dateArray) {
  if (!dateArray || !Array.isArray(dateArray) || dateArray.length < 3) return 'N/A';
  const date = new Date(dateArray[0], dateArray[1] - 1, dateArray[2]);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
}

function getMainAddress(fullAddress) {
    if (!fullAddress) return 'N/A';
    const parts = fullAddress.split(' ');
    if (parts.length > 2) {
        return `${parts[0]} ${parts[1]}`;
    }
    return fullAddress;
}

async function loadPendingAccommodations(page = 0) {
  await adminStore.fetchPendingAccommodations({
    page: page,
    size: currentPageSize.value,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  });
}

async function handleApprove(accommodationId) {
  try {
    await ElMessageBox.confirm(
      `숙소 ID [${accommodationId}]의 등록 신청을 승인하시겠습니까?`,
      '승인 확인',
      {
        confirmButtonText: '승인',
        cancelButtonText: '취소',
        type: 'warning',
      }
    );
    await adminStore.approvePendingAccommodation(accommodationId);
    ElNotification({ title: '성공', message: '숙소 신청이 승인되었습니다.', type: 'success' });
  } catch (e) {
    if (e !== 'cancel') {
      console.error(`숙소 신청 (ID: ${accommodationId}) 승인 오류 Front:`, e);
      ElNotification({ title: '오류', message: adminStore.error || '승인 처리 중 오류 발생', type: 'error' });
    }
  }
}

async function handleReject(accommodationId) {
  try {
    const { value: reason } = await ElMessageBox.prompt(
      '숙소 신청을 거절하는 사유를 입력해주세요:',
      '거절 사유 입력',
      {
        confirmButtonText: '제출',
        cancelButtonText: '취소',
        inputPattern: /.+/,
        inputErrorMessage: '거절 사유를 반드시 입력해야 합니다.',
      }
    );
    if (reason) {
      await adminStore.rejectPendingAccommodation({ accommodationId, reason });
      ElNotification({ title: '성공', message: '숙소 신청이 거절되었습니다.', type: 'success' });
    }
  } catch (e) {
    if (e !== 'cancel') {
      console.error(`숙소 신청 (ID: ${accommodationId}) 거절 오류 Front:`, e);
      ElNotification({ title: '오류', message: adminStore.error || '거절 처리 중 오류 발생', type: 'error' });
    }
  }
}

function handlePageChange(newPage) {
  loadPendingAccommodations(newPage - 1);
}

onMounted(() => {
  adminStore._clearMessages();
  loadPendingAccommodations();
});
</script>

<style scoped>
.el-table th {
  background-color: #f8f9fa;
}
.el-button-group {
  white-space: nowrap;
}
.container {
  max-width: 1200px; 
}
</style> 