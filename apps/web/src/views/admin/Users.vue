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
                      @click="suspendUser(user.userId)"
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
                  <input 
                    type="text" 
                    class="form-control" 
                    id="username" 
                    v-model="currentUser.username" 
                    required
                  >
                </div>
                
                <div class="mb-3">
                  <label for="email" class="form-label">이메일</label>
                  <input 
                    type="email" 
                    class="form-control" 
                    id="email" 
                    v-model="currentUser.email" 
                    required
                  >
                </div>
                
                <div class="mb-3">
                  <label for="phone" class="form-label">전화번호</label>
                  <input 
                    type="tel" 
                    class="form-control" 
                    id="phone" 
                    v-model="currentUser.phone" 
                    placeholder="예: 010-1234-5678"
                  >
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

<script>
/**
 * 사용자 관리 컴포넌트
 * 
 * 이 컴포넌트는 관리자가 사용자 정보를 관리할 수 있는 페이지입니다.
 * 사용자 목록 조회, 수정, 정지, 활성화, 삭제 기능을 제공합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'Users',
  components: {
    Layout
  },
  data() {
    return {
      loading: true,
      message: '',
      error: '',
      users: [],
      totalItems: 0,
      totalPages: 0,
      currentPage: 1,
      searchParams: {
        role: '',
        status: '',
        keyword: '',
        page: 1,
        size: 10
      },
      currentUser: {
        userId: null,
        username: '',
        email: '',
        phone: '',
        role: 'USER',
        status: 'ACTIVE'
      },
      resetPassword: false,
      userModal: null
    };
  },
  computed: {
    ...mapState({
      isAdmin: state => state.user.user?.role === 'ADMIN'
    }),
    
    /**
     * 페이지네이션 아이템 계산
     * @returns {Array} 페이지네이션 아이템 배열
     */
    paginationItems() {
      const items = [];
      const maxVisiblePages = 5;
      
      if (this.totalPages <= maxVisiblePages) {
        // 전체 페이지가 최대 표시 페이지 수보다 작거나 같으면 모든 페이지 표시
        for (let i = 1; i <= this.totalPages; i++) {
          items.push(i);
        }
      } else {
        // 현재 페이지 주변의 페이지만 표시
        items.push(1); // 첫 페이지는 항상 표시
        
        if (this.currentPage > 3) {
          items.push('...'); // 현재 페이지가 3보다 크면 '...' 표시
        }
        
        // 현재 페이지 주변 페이지 표시
        const start = Math.max(2, this.currentPage - 1);
        const end = Math.min(this.totalPages - 1, this.currentPage + 1);
        
        for (let i = start; i <= end; i++) {
          items.push(i);
        }
        
        if (this.currentPage < this.totalPages - 2) {
          items.push('...'); // 현재 페이지가 마지막에서 3번째 이전이면 '...' 표시
        }
        
        items.push(this.totalPages); // 마지막 페이지는 항상 표시
      }
      
      return items;
    }
  },
  created() {
    // 관리자 권한 확인
    if (!this.isAdmin) {
      this.$router.push({
        path: '/error/access-denied',
        query: { message: '관리자만 접근할 수 있는 페이지입니다.' }
      });
      return;
    }
    
    // URL 쿼리 파라미터에서 검색 조건 가져오기
    const query = this.$route.query;
    if (query.role) this.searchParams.role = query.role;
    if (query.status) this.searchParams.status = query.status;
    if (query.keyword) this.searchParams.keyword = query.keyword;
    if (query.page) this.searchParams.page = parseInt(query.page);
    
    // 사용자 목록 로드
    this.loadUsers();
  },
  mounted() {
    // 부트스트랩 모달 초기화
    if (window.bootstrap) {
      this.userModal = new window.bootstrap.Modal(document.getElementById('userModal'));
    }
  },
  methods: {
    ...mapActions('admin', [
      'fetchUsers', 
      'updateUser', 
      'deleteUser', 
      'suspendUser', 
      'activateUser',
      'resetUserPassword'
    ]),
    
    /**
     * 사용자 목록 로드
     */
    async loadUsers() {
      this.loading = true;
      
      try {
        const result = await this.fetchUsers(this.searchParams);
        
        this.users = result.content;
        this.totalItems = result.totalElements;
        this.totalPages = result.totalPages;
        this.currentPage = result.number + 1;
        
        // URL 쿼리 파라미터 업데이트
        this.updateQueryParams();
      } catch (error) {
        console.error('사용자 목록을 불러오는 중 오류가 발생했습니다:', error);
        this.error = '사용자 목록을 불러오는 중 오류가 발생했습니다.';
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 사용자 검색
     */
    searchUsers() {
      this.searchParams.page = 1;
      this.loadUsers();
    },
    
    /**
     * 페이지 이동
     * @param {number} page - 이동할 페이지 번호
     */
    goToPage(page) {
      if (page < 1 || page > this.totalPages) return;
      
      this.searchParams.page = page;
      this.loadUsers();
    },
    
    /**
     * URL 쿼리 파라미터 업데이트
     */
    updateQueryParams() {
      const query = {};
      
      if (this.searchParams.role) query.role = this.searchParams.role;
      if (this.searchParams.status) query.status = this.searchParams.status;
      if (this.searchParams.keyword) query.keyword = this.searchParams.keyword;
      if (this.searchParams.page > 1) query.page = this.searchParams.page;
      
      this.$router.replace({ query });
    },
    
    /**
     * 사용자 수정 모달 표시
     * @param {Object} user - 수정할 사용자 객체
     */
    editUser(user) {
      this.currentUser = { ...user };
      this.resetPassword = false;
      this.userModal.show();
    },
    
    /**
     * 사용자 정보 저장
     */
    async saveUser() {
      try {
        const userData = { ...this.currentUser };
        
        // 비밀번호 초기화 요청
        if (this.resetPassword) {
          await this.resetUserPassword(userData.userId);
        }
        
        // 사용자 정보 업데이트
        await this.updateUser(userData);
        
        this.message = '사용자 정보가 성공적으로 수정되었습니다.';
        
        // 모달 닫기
        this.userModal.hide();
        
        // 사용자 목록 다시 로드
        this.loadUsers();
      } catch (error) {
        console.error('사용자 정보 저장 중 오류가 발생했습니다:', error);
        this.error = '사용자 정보 저장에 실패했습니다. 다시 시도해주세요.';
      }
    },
    
    /**
     * 사용자 삭제 확인
     * @param {Object} user - 삭제할 사용자 객체
     */
    confirmDeleteUser(user) {
      if (confirm(`정말로 "${user.username}" 사용자를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`)) {
        this.deleteUserItem(user.userId);
      }
    },
    
    /**
     * 사용자 삭제 처리
     * @param {number} userId - 삭제할 사용자 ID
     */
    async deleteUserItem(userId) {
      try {
        await this.deleteUser(userId);
        this.message = '사용자가 성공적으로 삭제되었습니다.';
        
        // 사용자 목록 다시 로드
        this.loadUsers();
      } catch (error) {
        console.error('사용자 삭제 중 오류가 발생했습니다:', error);
        this.error = '사용자 삭제에 실패했습니다. 다시 시도해주세요.';
      }
    },
    
    /**
     * 사용자 계정 정지
     * @param {number} userId - 정지할 사용자 ID
     */
    async suspendUser(userId) {
      const reason = prompt('정지 사유를 입력해주세요:');
      if (reason === null) return; // 취소 버튼 클릭 시
      
      try {
        await this.suspendUser({ userId, reason });
        this.message = '사용자 계정이 성공적으로 정지되었습니다.';
        
        // 사용자 목록 다시 로드
        this.loadUsers();
      } catch (error) {
        console.error('사용자 계정 정지 중 오류가 발생했습니다:', error);
        this.error = '사용자 계정 정지에 실패했습니다. 다시 시도해주세요.';
      }
    },
    
    /**
     * 사용자 계정 활성화
     * @param {number} userId - 활성화할 사용자 ID
     */
    async activateUser(userId) {
      try {
        await this.activateUser(userId);
        this.message = '사용자 계정이 성공적으로 활성화되었습니다.';
        
        // 사용자 목록 다시 로드
        this.loadUsers();
      } catch (error) {
        console.error('사용자 계정 활성화 중 오류가 발생했습니다:', error);
        this.error = '사용자 계정 활성화에 실패했습니다. 다시 시도해주세요.';
      }
    },
    
    /**
     * 역할 이름 반환
     * @param {string} role - 역할 코드
     * @returns {string} 역할 이름
     */
    getRoleName(role) {
      const roles = {
        'USER': '일반 사용자',
        'HOST': '호스트',
        'ADMIN': '관리자'
      };
      
      return roles[role] || role;
    },
    
    /**
     * 역할 배지 클래스 반환
     * @param {string} role - 역할 코드
     * @returns {string} 배지 클래스
     */
    getRoleBadgeClass(role) {
      const classes = {
        'USER': 'badge bg-primary',
        'HOST': 'badge bg-success',
        'ADMIN': 'badge bg-danger'
      };
      
      return classes[role] || 'badge bg-secondary';
    },
    
    /**
     * 상태 이름 반환
     * @param {string} status - 상태 코드
     * @returns {string} 상태 이름
     */
    getStatusName(status) {
      const statuses = {
        'ACTIVE': '활성',
        'INACTIVE': '비활성',
        'SUSPENDED': '정지'
      };
      
      return statuses[status] || status;
    },
    
    /**
     * 상태 배지 클래스 반환
     * @param {string} status - 상태 코드
     * @returns {string} 배지 클래스
     */
    getStatusBadgeClass(status) {
      const classes = {
        'ACTIVE': 'badge bg-success',
        'INACTIVE': 'badge bg-secondary',
        'SUSPENDED': 'badge bg-danger'
      };
      
      return classes[status] || 'badge bg-secondary';
    },
    
    /**
     * 날짜 포맷팅
     * @param {string|Date} date - 포맷팅할 날짜
     * @returns {string} 포맷팅된 날짜 문자열
     */
    formatDate(date) {
      if (!date) return '';
      
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      
      return `${year}-${month}-${day}`;
    }
  }
};
</script>

<style scoped>
.card {
  margin-bottom: 20px;
}

.card-header {
  background-color: #f8f9fa;
}

.table th {
  background-color: #f8f9fa;
}

.table td {
  vertical-align: middle;
}

.pagination {
  margin-top: 20px;
}

.btn-group {
  white-space: nowrap;
}
</style>