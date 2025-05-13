<template>
  <div class="container mt-5 mb-5">
    <h2 class="mb-4">호스트 목록</h2>
    
    <!-- 필터 섹션 (관리자용) -->
    <div v-if="isAdmin" class="filter-section mb-4">
      <div class="row g-3">
        <div class="col-md-4">
          <label for="status" class="form-label">상태</label>
          <select class="form-select" id="status" v-model="filters.status">
            <option value="">전체</option>
            <option value="PENDING">승인 대기 중</option>
            <option value="APPROVED">승인됨</option>
            <option value="REJECTED">거부됨</option>
          </select>
        </div>
        <div class="col-md-4">
          <label for="keyword" class="form-label">검색어</label>
          <input 
            type="text" 
            class="form-control" 
            id="keyword" 
            v-model="filters.keyword" 
            placeholder="사업자명, 사업자 등록번호"
          >
        </div>
        <div class="col-md-4 d-flex align-items-end">
          <button type="button" class="btn btn-primary w-100" @click="applyFilters">검색</button>
        </div>
      </div>
    </div>
    
    <!-- 호스트 목록 -->
    <div class="row">
      <div v-if="hosts.length === 0" class="col-12 text-center py-5">
        <p class="lead">등록된 호스트가 없습니다.</p>
        <router-link to="/host/regist-form" class="btn btn-primary">호스트 등록하기</router-link>
      </div>
      
      <div v-for="host in hosts" :key="host.hostId" class="col-md-4 mb-4">
        <div class="card host-card">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <h5 class="card-title">{{ host.businessName }}</h5>
              <span :class="getStatusBadgeClass(host.hostStatus)">
                {{ getStatusText(host.hostStatus) }}
              </span>
            </div>
            
            <p class="card-text"><strong>사업자 등록번호:</strong> {{ host.businessRegNo }}</p>
            <p class="card-text"><strong>등록일:</strong> {{ formatDate(host.createdAt) }}</p>
            
            <div class="d-grid gap-2">
              <router-link :to="`/host/detail/${host.hostId}`" class="btn btn-outline-primary">상세 보기</router-link>
              
              <!-- 관리자만 볼 수 있는 빠른 승인/거부 버튼 -->
              <div v-if="isAdmin && host.hostStatus === 'PENDING'" class="d-flex justify-content-between mt-2">
                <button 
                  type="button" 
                  class="btn btn-success btn-sm" 
                  style="width: 48%;" 
                  @click="updateHostStatus(host.hostId, 'APPROVED')"
                >
                  승인
                </button>
                <button 
                  type="button" 
                  class="btn btn-danger btn-sm" 
                  style="width: 48%;" 
                  @click="updateHostStatus(host.hostId, 'REJECTED')"
                >
                  거부
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 호스트 등록 버튼 (일반 사용자용) -->
    <div v-if="!isAdmin && isLoggedIn" class="d-grid gap-2 col-md-6 mx-auto mt-4">
      <router-link to="/host/regist-form" class="btn btn-primary">호스트 등록하기</router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HostList',
  data() {
    return {
      // 호스트 목록
      hosts: [],
      
      // 필터 조건
      filters: {
        status: '',
        keyword: ''
      },
      
      // 사용자 정보
      isAdmin: false,
      isLoggedIn: false
    };
  },
  created() {
    // URL 쿼리 파라미터에서 필터 조건 가져오기
    const query = this.$route.query;
    if (query.status) this.filters.status = query.status;
    if (query.keyword) this.filters.keyword = query.keyword;
    
    // 사용자 정보 로드
    this.loadUserInfo();
    
    // 호스트 목록 로드
    this.loadHosts();
  },
  methods: {
    // 사용자 정보 로드
    async loadUserInfo() {
      try {
        // API 호출
        const response = await fetch('/api/users/me');
        if (!response.ok) {
          // 로그인되지 않은 상태
          this.isLoggedIn = false;
          this.isAdmin = false;
          return;
        }
        
        const user = await response.json();
        this.isLoggedIn = true;
        this.isAdmin = user.role === 'ADMIN';
      } catch (error) {
        console.error('사용자 정보 로드 중 오류가 발생했습니다:', error);
        this.isLoggedIn = false;
        this.isAdmin = false;
      }
    },
    
    // 호스트 목록 로드
    async loadHosts() {
      try {
        // API 호출
        let url = '/api/hosts';
        
        // 필터 조건이 있는 경우 쿼리 파라미터 추가
        const params = new URLSearchParams();
        if (this.filters.status) params.append('status', this.filters.status);
        if (this.filters.keyword) params.append('keyword', this.filters.keyword);
        
        if (params.toString()) {
          url += `?${params.toString()}`;
        }
        
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error('호스트 목록을 불러오는데 실패했습니다.');
        }
        
        this.hosts = await response.json();
      } catch (error) {
        console.error('호스트 목록 로드 중 오류가 발생했습니다:', error);
      }
    },
    
    // 필터 적용
    applyFilters() {
      // URL 쿼리 파라미터 업데이트
      const query = {};
      if (this.filters.status) query.status = this.filters.status;
      if (this.filters.keyword) query.keyword = this.filters.keyword;
      
      this.$router.replace({ query });
      
      // 호스트 목록 다시 로드
      this.loadHosts();
    },
    
    // 호스트 상태 업데이트
    async updateHostStatus(hostId, status) {
      try {
        // 사용자 확인
        if (!confirm(`호스트 상태를 ${this.getStatusText(status)}(으)로 변경하시겠습니까?`)) {
          return;
        }
        
        // API 호출
        const response = await fetch(`/api/hosts/${hostId}/status`, {
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
        
        // 호스트 목록 다시 로드
        this.loadHosts();
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
        day: '2-digit'
      }).format(date);
    }
  }
};
</script>

<style scoped>
.host-card {
  transition: transform 0.3s;
  margin-bottom: 20px;
  height: 100%;
}
.host-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.2);
}
.filter-section {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 5px;
  margin-bottom: 20px;
}
.status-badge {
  font-size: 0.8rem;
  padding: 0.4rem 0.6rem;
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