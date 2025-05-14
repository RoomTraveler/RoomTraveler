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

<script>
/**
 * 관리자용 숙소 관리 컴포넌트
 * 
 * 이 컴포넌트는 관리자가 모든 숙소를 관리할 수 있는 페이지입니다.
 * 숙소 목록 조회, 승인/거부, 삭제 등의 기능을 제공합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'AdminAccommodations',
  components: {
    Layout
  },
  data() {
    return {
      loading: true,
      accommodations: [],
      totalItems: 0,
      totalPages: 0,
      currentPage: 1,
      searchParams: {
        status: '',
        type: '',
        keyword: '',
        page: 1,
        size: 10
      }
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
    if (query.status) this.searchParams.status = query.status;
    if (query.type) this.searchParams.type = query.type;
    if (query.keyword) this.searchParams.keyword = query.keyword;
    if (query.page) this.searchParams.page = parseInt(query.page);
    
    // 숙소 목록 로드
    this.loadAccommodations();
  },
  methods: {
    ...mapActions('admin', [
      'fetchAccommodations', 
      'approveAccommodation', 
      'rejectAccommodation', 
      'suspendAccommodation', 
      'deleteAccommodation'
    ]),
    
    /**
     * 숙소 목록 로드
     */
    async loadAccommodations() {
      this.loading = true;
      
      try {
        const result = await this.fetchAccommodations(this.searchParams);
        
        this.accommodations = result.content;
        this.totalItems = result.totalElements;
        this.totalPages = result.totalPages;
        this.currentPage = result.number + 1;
        
        // URL 쿼리 파라미터 업데이트
        this.updateQueryParams();
      } catch (error) {
        console.error('숙소 목록을 불러오는 중 오류가 발생했습니다:', error);
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 숙소 검색
     */
    searchAccommodations() {
      this.searchParams.page = 1;
      this.loadAccommodations();
    },
    
    /**
     * 페이지 이동
     * @param {number} page - 이동할 페이지 번호
     */
    goToPage(page) {
      if (page < 1 || page > this.totalPages) return;
      
      this.searchParams.page = page;
      this.loadAccommodations();
    },
    
    /**
     * URL 쿼리 파라미터 업데이트
     */
    updateQueryParams() {
      const query = {};
      
      if (this.searchParams.status) query.status = this.searchParams.status;
      if (this.searchParams.type) query.type = this.searchParams.type;
      if (this.searchParams.keyword) query.keyword = this.searchParams.keyword;
      if (this.searchParams.page > 1) query.page = this.searchParams.page;
      
      this.$router.replace({ query });
    },
    
    /**
     * 숙소 승인
     * @param {number} accommodationId - 숙소 ID
     */
    async approveAccommodation(accommodationId) {
      if (!confirm('이 숙소를 승인하시겠습니까?')) return;
      
      try {
        await this.approveAccommodation(accommodationId);
        this.loadAccommodations();
      } catch (error) {
        console.error('숙소 승인 중 오류가 발생했습니다:', error);
        alert('숙소 승인에 실패했습니다. 다시 시도해주세요.');
      }
    },
    
    /**
     * 숙소 거부
     * @param {number} accommodationId - 숙소 ID
     */
    async rejectAccommodation(accommodationId) {
      const reason = prompt('거부 사유를 입력해주세요:');
      if (reason === null) return; // 취소 버튼 클릭 시
      
      try {
        await this.rejectAccommodation({ accommodationId, reason });
        this.loadAccommodations();
      } catch (error) {
        console.error('숙소 거부 중 오류가 발생했습니다:', error);
        alert('숙소 거부에 실패했습니다. 다시 시도해주세요.');
      }
    },
    
    /**
     * 숙소 중지
     * @param {number} accommodationId - 숙소 ID
     */
    async suspendAccommodation(accommodationId) {
      const reason = prompt('중지 사유를 입력해주세요:');
      if (reason === null) return; // 취소 버튼 클릭 시
      
      try {
        await this.suspendAccommodation({ accommodationId, reason });
        this.loadAccommodations();
      } catch (error) {
        console.error('숙소 중지 중 오류가 발생했습니다:', error);
        alert('숙소 중지에 실패했습니다. 다시 시도해주세요.');
      }
    },
    
    /**
     * 숙소 삭제
     * @param {number} accommodationId - 숙소 ID
     */
    async deleteAccommodation(accommodationId) {
      if (!confirm('이 숙소를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) return;
      
      try {
        await this.deleteAccommodation(accommodationId);
        this.loadAccommodations();
      } catch (error) {
        console.error('숙소 삭제 중 오류가 발생했습니다:', error);
        alert('숙소 삭제에 실패했습니다. 다시 시도해주세요.');
      }
    },
    
    /**
     * 숙소 유형 이름 반환
     * @param {string} type - 숙소 유형 코드
     * @returns {string} 숙소 유형 이름
     */
    getAccommodationTypeName(type) {
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
      };
      
      return types[type] || type;
    },
    
    /**
     * 상태 이름 반환
     * @param {string} status - 상태 코드
     * @returns {string} 상태 이름
     */
    getStatusName(status) {
      const statuses = {
        'PENDING': '승인 대기',
        'APPROVED': '승인됨',
        'REJECTED': '거부됨',
        'SUSPENDED': '중지됨'
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
        'PENDING': 'badge bg-warning',
        'APPROVED': 'badge bg-success',
        'REJECTED': 'badge bg-danger',
        'SUSPENDED': 'badge bg-secondary'
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