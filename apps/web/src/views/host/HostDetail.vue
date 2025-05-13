<template>
  <div class="container mt-5 mb-5">
    <h2 class="text-center mb-4">호스트 상세 정보</h2>
    
    <div class="detail-container">
      <div v-if="host">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h3>{{ host.businessName }}</h3>
          <span :class="getStatusBadgeClass(host.hostStatus)">
            {{ getStatusText(host.hostStatus) }}
          </span>
        </div>
        
        <div class="info-group">
          <h5>사업자 정보</h5>
          <div class="row mb-2">
            <div class="col-md-4 info-label">사업자명</div>
            <div class="col-md-8">{{ host.businessName }}</div>
          </div>
          <div class="row mb-2">
            <div class="col-md-4 info-label">사업자 등록번호</div>
            <div class="col-md-8">{{ host.businessRegNo }}</div>
          </div>
        </div>
        
        <div class="info-group">
          <h5>계좌 정보</h5>
          <div class="row mb-2">
            <div class="col-md-4 info-label">정산 계좌 정보</div>
            <div class="col-md-8">{{ host.bankAccount }}</div>
          </div>
        </div>
        
        <div v-if="host.profileText" class="info-group">
          <h5>호스트 소개</h5>
          <p>{{ host.profileText }}</p>
        </div>
        
        <div class="info-group">
          <h5>등록 정보</h5>
          <div class="row mb-2">
            <div class="col-md-4 info-label">등록일</div>
            <div class="col-md-8">{{ formatDate(host.createdAt) }}</div>
          </div>
          <div v-if="host.updatedAt" class="row mb-2">
            <div class="col-md-4 info-label">최종 수정일</div>
            <div class="col-md-8">{{ formatDate(host.updatedAt) }}</div>
          </div>
        </div>
        
        <!-- 호스트 본인 또는 관리자만 수정 가능 -->
        <div v-if="isHostOrAdmin" class="d-grid gap-2 d-md-flex justify-content-md-end mt-4">
          <router-link :to="`/host/update-form/${host.hostId}`" class="btn btn-primary">정보 수정</router-link>
          
          <!-- 승인된 호스트만 숙소 등록 가능 -->
          <template v-if="host.hostStatus === 'APPROVED' && isHostOrAdmin">
            <router-link to="/host/register-accommodation" class="btn btn-success">숙소 등록</router-link>
            <router-link to="/accommodation/host/accommodations" class="btn btn-info">내 숙소 관리</router-link>
          </template>
          
          <!-- 관리자만 상태 변경 가능 -->
          <template v-if="isAdmin && host.hostStatus === 'PENDING'">
            <button @click="updateHostStatus('APPROVED')" class="btn btn-success">승인</button>
            <button @click="updateHostStatus('REJECTED')" class="btn btn-danger">거부</button>
          </template>
        </div>
      </div>
      
      <div v-else class="alert alert-warning">
        호스트 정보를 찾을 수 없습니다.
      </div>
      
      <div class="d-grid gap-2 mt-4">
        <router-link to="/host/list" class="btn btn-secondary">호스트 목록으로</router-link>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HostDetail',
  data() {
    return {
      // 호스트 정보
      host: null,
      // 현재 사용자 정보
      currentUser: {
        userId: null,
        role: null
      }
    };
  },
  computed: {
    // 호스트 본인 또는 관리자 여부
    isHostOrAdmin() {
      return this.currentUser.userId === this.host?.hostId || this.currentUser.role === 'ADMIN';
    },
    // 관리자 여부
    isAdmin() {
      return this.currentUser.role === 'ADMIN';
    }
  },
  created() {
    // 호스트 ID 가져오기
    const hostId = this.$route.params.id;
    
    // 현재 사용자 정보 로드
    this.loadCurrentUser();
    
    // 호스트 정보 로드
    if (hostId) {
      this.loadHostDetail(hostId);
    }
  },
  methods: {
    // 현재 사용자 정보 로드
    async loadCurrentUser() {
      try {
        // API 호출
        const response = await fetch('/api/users/me');
        if (!response.ok) {
          throw new Error('사용자 정보를 불러오는데 실패했습니다.');
        }
        
        const user = await response.json();
        this.currentUser.userId = user.id;
        this.currentUser.role = user.role;
      } catch (error) {
        console.error('사용자 정보 로드 중 오류가 발생했습니다:', error);
      }
    },
    
    // 호스트 상세 정보 로드
    async loadHostDetail(hostId) {
      try {
        // API 호출
        const response = await fetch(`/api/hosts/${hostId}`);
        if (!response.ok) {
          throw new Error('호스트 정보를 불러오는데 실패했습니다.');
        }
        
        this.host = await response.json();
      } catch (error) {
        console.error('호스트 정보 로드 중 오류가 발생했습니다:', error);
      }
    },
    
    // 호스트 상태 업데이트
    async updateHostStatus(status) {
      try {
        // 사용자 확인
        if (!confirm(`호스트 상태를 ${this.getStatusText(status)}(으)로 변경하시겠습니까?`)) {
          return;
        }
        
        // API 호출
        const response = await fetch(`/api/hosts/${this.host.hostId}/status`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            hostStatus: status
          })
        });
        
        if (!response.ok) {
          throw new Error('호스트 상태 변경에 실패했습니다.');
        }
        
        // 호스트 정보 다시 로드
        this.loadHostDetail(this.host.hostId);
      } catch (error) {
        console.error('호스트 상태 변경 중 오류가 발생했습니다:', error);
        alert('호스트 상태 변경에 실패했습니다.');
      }
    },
    
    // 호스트 상태에 따른 배지 클래스 반환
    getStatusBadgeClass(status) {
      const baseClass = 'badge status-badge';
      switch (status) {
        case 'PENDING': return `${baseClass} status-pending`;
        case 'APPROVED': return `${baseClass} status-approved`;
        case 'REJECTED': return `${baseClass} status-rejected`;
        default: return baseClass;
      }
    },
    
    // 호스트 상태 텍스트 반환
    getStatusText(status) {
      switch (status) {
        case 'PENDING': return '승인 대기 중';
        case 'APPROVED': return '승인됨';
        case 'REJECTED': return '거부됨';
        default: return status;
      }
    },
    
    // 날짜 포맷팅
    formatDate(dateString) {
      if (!dateString) return '-';
      
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      }).format(date);
    }
  }
};
</script>

<style scoped>
.detail-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 10px;
  box-shadow: 0 0 10px rgba(0,0,0,0.1);
}
.info-group {
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #dee2e6;
  padding-bottom: 1rem;
}
.info-group:last-child {
  border-bottom: none;
}
.info-label {
  font-weight: bold;
  color: #495057;
}
.status-badge {
  font-size: 0.9rem;
  padding: 0.5rem 0.75rem;
  border-radius: 50px;
}
.status-pending {
  background-color: #ffc107;
  color: #212529;
}
.status-approved {
  background-color: #28a745;
  color: white;
}
.status-rejected {
  background-color: #dc3545;
  color: white;
}
</style>