<template>
    <Layout>
      <div class="container mt-4">
        <h2 class="mb-4">호스트 신청 관리</h2>
  
        <!-- 알림 메시지 -->
        <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
          {{ message }}
          <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
        </div>
        <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
          {{ error }}
          <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
        </div>
  
        <!-- 로딩 표시 -->
        <div v-if="loading" class="text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">로딩 중...</span>
          </div>
          <p class="mt-2">호스트 신청 목록을 불러오는 중입니다...</p>
        </div>
  
        <!-- 호스트 신청 목록 -->
        <div v-else>
          <div v-if="pendingHosts.length === 0" class="alert alert-info">
            승인 대기 중인 호스트 신청이 없습니다.
          </div>
          <div v-else class="table-responsive">
            <table class="table table-striped table-hover align-middle">
              <thead>
                <tr>
                  <th>사용자 ID</th>
                  <th>이름(닉네임)</th>
                  <th>사업자명</th>
                  <th>사업자 번호</th>
                  <th>사업자 등록증</th>
                  <th>신청일</th>
                  <th>관리</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="host in pendingHosts" :key="host.hostId">
                  <td>{{ host.userId }}</td>
                  <td>{{ host.user?.username || 'N/A' }}</td>
                  <td>{{ host.businessName }}</td>
                  <td>{{ host.businessNumber }}</td>
                  <td>
                    <a v-if="host.businessLicense" :href="host.businessLicense" target="_blank" class="btn btn-sm btn-outline-info">
                      <i class="bi bi-file-earmark-arrow-down"></i> 보기
                    </a>
                    <span v-else>-</span>
                  </td>
                  <td>{{ formatDate(host.createdAt) }}</td>
                  <td>
                    <div class="btn-group btn-group-sm" role="group">
                      <button class="btn btn-success" @click="approveHostApplication(host.hostId)">승인</button>
                      <button class="btn btn-danger" @click="promptRejectHostApplication(host.hostId)">거절</button>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
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
  
  const adminStore = useAdminStore();
  const router = useRouter();
  
  const loading = computed(() => adminStore.loading);
  const pendingHosts = computed(() => adminStore.pendingHosts);
  const message = computed({
    get: () => adminStore.message,
    set: (value) => adminStore._setMessage(value)
  });
  const error = computed({
    get: () => adminStore.error,
    set: (value) => adminStore._setError(value)
  });
  const isAdmin = computed(() => adminStore.userRole === 'ADMIN');
  
  function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
  }
  
  async function loadPendingHosts() {
    await adminStore.fetchPendingHosts();
  }
  
  async function approveHostApplication(hostUserId) {
    if (!window.confirm(`사용자 ID [${hostUserId}]의 호스트 신청을 승인하시겠습니까?`)) return;
    try {
      await adminStore.approveHostApplication(hostUserId);
    } catch (err) {
      console.error(`호스트 신청 (ID: ${hostUserId}) 승인 오류 Front:`, err);
    }
  }
  
  async function promptRejectHostApplication(hostUserId) {
    const reason = window.prompt('호스트 신청을 거절하는 사유를 입력해주세요:');
    if (reason === null) return;
    if (reason.trim() === '') {
      window.alert('거절 사유를 반드시 입력해야 합니다.');
      return;
    }
    try {
      await adminStore.rejectHostApplication({ hostUserId, reason });
    } catch (err) {
      console.error(`호스트 신청 (ID: ${hostUserId}) 거절 오류 Front:`, err);
    }
  }
  
  onMounted(() => {
    adminStore._clearMessages();
    if (!isAdmin.value) {
      router.push({ path: '/', query: { error: '접근 권한이 없습니다.' } });
      alert('관리자만 접근할 수 있는 페이지입니다.');
      return;
    }
    loadPendingHosts();
  });
  </script>
  
  <style scoped>
  .table th {
    background-color: #f8f9fa;
  }
  .btn-group {
    white-space: nowrap;
  }
  </style>
  