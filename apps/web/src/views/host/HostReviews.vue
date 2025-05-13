<template>
  <div class="container mt-5 mb-5">
    <h1 class="mb-4">
      <i class="bi bi-star"></i> 호스트 리뷰 관리
    </h1>
    
    <!-- 알림 메시지 표시 -->
    <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
      {{ message }}
      <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
    </div>
    
    <!-- 필터링 옵션 -->
    <div class="card filter-card">
      <div class="card-body">
        <h5 class="card-title">리뷰 필터링</h5>
        <div class="row g-3">
          <div class="col-md-4">
            <label for="accommodationId" class="form-label">숙소 선택</label>
            <select class="form-select" id="accommodationId" v-model="filters.accommodationId">
              <option value="">모든 숙소</option>
              <option 
                v-for="accommodation in accommodations" 
                :key="accommodation.accommodationId" 
                :value="accommodation.accommodationId"
              >
                {{ accommodation.title }}
              </option>
            </select>
          </div>
          <div class="col-md-4">
            <label for="rating" class="form-label">별점</label>
            <select class="form-select" id="rating" v-model="filters.rating">
              <option value="">모든 별점</option>
              <option value="5">5점</option>
              <option value="4">4점</option>
              <option value="3">3점</option>
              <option value="2">2점</option>
              <option value="1">1점</option>
            </select>
          </div>
          <div class="col-md-4 d-flex align-items-end">
            <button type="button" class="btn btn-primary" @click="applyFilters">필터 적용</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 리뷰 통계 -->
    <div class="row mb-4">
      <div class="col-md-6">
        <div class="card text-center">
          <div class="card-body">
            <h5 class="card-title">총 리뷰</h5>
            <p class="card-text fs-2">{{ totalReviews }}</p>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card text-center">
          <div class="card-body">
            <h5 class="card-title">평균 별점</h5>
            <p class="card-text fs-2">
              <span class="stars">
                <i 
                  v-for="i in 5" 
                  :key="i" 
                  :class="getStarClass(i, averageRating)"
                ></i>
              </span>
              <span class="ms-2">{{ averageRating }}</span>
            </p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 리뷰 목록 -->
    <div v-if="filteredReviews.length === 0" class="alert alert-info">
      <i class="bi bi-info-circle"></i> 리뷰가 없습니다.
    </div>
    
    <div v-else class="row">
      <div v-for="review in filteredReviews" :key="review.reviewId" class="col-md-6">
        <div class="card review-card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <div>
              <h5 class="mb-0">{{ review.accommodationTitle }}</h5>
            </div>
            <div class="stars">
              <i 
                v-for="i in 5" 
                :key="i" 
                :class="i <= review.rating ? 'bi bi-star-fill' : 'bi bi-star'"
              ></i>
            </div>
          </div>
          <div class="card-body">
            <div class="d-flex justify-content-between mb-3">
              <div>
                <strong>{{ review.username }}</strong>
                <small class="text-muted ms-2">
                  {{ formatDate(review.createdAt) }}
                </small>
              </div>
            </div>
            
            <h6 v-if="review.title" class="card-subtitle mb-2">{{ review.title }}</h6>
            
            <p class="card-text">{{ review.content }}</p>
            
            <div class="mt-3">
              <router-link 
                :to="`/accommodation/detail?accommodationId=${review.accommodationId}`" 
                class="btn btn-sm btn-outline-primary"
              >
                <i class="bi bi-building"></i> 숙소 보기
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HostReviews',
  data() {
    return {
      // 알림 메시지
      message: '',
      
      // 리뷰 목록
      reviews: [],
      
      // 숙소 목록
      accommodations: [],
      
      // 필터 조건
      filters: {
        accommodationId: '',
        rating: ''
      },
      
      // 통계 데이터
      totalReviews: 0,
      averageRating: 0
    };
  },
  computed: {
    // 필터링된 리뷰 목록
    filteredReviews() {
      return this.reviews;
    }
  },
  created() {
    // URL 쿼리 파라미터에서 필터 조건 가져오기
    const query = this.$route.query;
    if (query.accommodationId) this.filters.accommodationId = query.accommodationId;
    if (query.rating) this.filters.rating = query.rating;
    
    // URL 쿼리 파라미터에서 메시지 가져오기
    if (query.message) {
      this.message = query.message;
    }
    
    // 숙소 목록 로드
    this.loadAccommodations();
    
    // 리뷰 목록 로드
    this.loadReviews();
  },
  methods: {
    // 숙소 목록 로드
    async loadAccommodations() {
      try {
        // API 호출
        const response = await fetch('/api/host/accommodations/list');
        if (!response.ok) {
          throw new Error('숙소 목록을 불러오는데 실패했습니다.');
        }
        
        this.accommodations = await response.json();
      } catch (error) {
        console.error('숙소 목록 로드 중 오류가 발생했습니다:', error);
      }
    },
    
    // 리뷰 목록 로드
    async loadReviews() {
      try {
        // API 호출
        let url = '/api/host/reviews';
        
        // 필터 조건이 있는 경우 쿼리 파라미터 추가
        const params = new URLSearchParams();
        if (this.filters.accommodationId) params.append('accommodationId', this.filters.accommodationId);
        if (this.filters.rating) params.append('rating', this.filters.rating);
        
        if (params.toString()) {
          url += `?${params.toString()}`;
        }
        
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error('리뷰 목록을 불러오는데 실패했습니다.');
        }
        
        const data = await response.json();
        this.reviews = data.reviews;
        this.totalReviews = data.totalReviews;
        this.averageRating = data.averageRating;
      } catch (error) {
        console.error('리뷰 목록 로드 중 오류가 발생했습니다:', error);
        this.message = '리뷰 목록을 불러오는데 실패했습니다.';
      }
    },
    
    // 필터 적용
    applyFilters() {
      // URL 쿼리 파라미터 업데이트
      const query = {};
      if (this.filters.accommodationId) query.accommodationId = this.filters.accommodationId;
      if (this.filters.rating) query.rating = this.filters.rating;
      
      this.$router.replace({ query });
      
      // 리뷰 목록 다시 로드
      this.loadReviews();
    },
    
    // 별점에 따른 아이콘 클래스 반환
    getStarClass(index, rating) {
      if (index <= rating) {
        return 'bi bi-star-fill';
      } else if (index <= rating + 0.5) {
        return 'bi bi-star-half';
      } else {
        return 'bi bi-star';
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
/* 리뷰 카드 스타일 */
.review-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}
.review-card:hover {
  transform: translateY(-5px);
}

/* 별점 스타일 */
.stars {
  color: #ffc107;
  font-size: 1.2rem;
}

/* 필터 카드 스타일 */
.filter-card {
  margin-bottom: 20px;
}
</style>