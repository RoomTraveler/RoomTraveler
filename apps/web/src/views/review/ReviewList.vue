<template>
  <Layout>
    <div class="container mt-5">
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">리뷰 정보를 불러오는 중입니다...</p>
      </div>

      <div v-else-if="error" class="alert alert-danger" role="alert">
        {{ error }}
      </div>

      <div v-else>
        <div class="row mb-4">
          <div class="col">
            <h2>리뷰 목록</h2>
            <p class="text-muted">
              <span v-if="accommodationTitle">{{ accommodationTitle }}에 대한 </span>
              총 {{ reviewCount }}개의 리뷰가 있습니다.
            </p>
          </div>
          <div class="col-auto">
            <router-link :to="`/review/write/${accommodationId}`" class="btn btn-primary">
              <i class="bi bi-pencil-square"></i> 리뷰 작성
            </router-link>
          </div>
        </div>
        
        <!-- 평점 요약 -->
        <div class="rating-summary">
          <div class="row align-items-center">
            <div class="col-md-3 text-center">
              <h1 class="display-4 fw-bold">
                {{ averageRating ? averageRating.toFixed(1) : '0.0' }}
              </h1>
              <div class="star-rating">
                <template v-for="i in 5" :key="i">
                  <i v-if="i <= Math.floor(averageRating)" class="bi bi-star-fill"></i>
                  <i v-else-if="i <= Math.floor(averageRating) + 0.5" class="bi bi-star-half"></i>
                  <i v-else class="bi bi-star"></i>
                </template>
              </div>
              <p class="text-muted">{{ reviewCount }}개 리뷰</p>
            </div>
            <div class="col-md-9">
              <!-- 평점 분포 -->
              <div v-for="rating in [5, 4, 3, 2, 1]" :key="rating" class="row align-items-center mb-2">
                <div class="col-2">{{ rating }}점</div>
                <div class="col-8">
                  <div class="progress">
                    <div 
                      class="progress-bar" 
                      role="progressbar" 
                      :style="{ width: calculatePercentage(rating) + '%' }" 
                      :aria-valuenow="calculatePercentage(rating)" 
                      aria-valuemin="0" 
                      aria-valuemax="100"
                    ></div>
                  </div>
                </div>
                <div class="col-2 text-end">{{ ratingDistribution[rating] || 0 }}개</div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 리뷰가 없는 경우 -->
        <div v-if="reviews.length === 0" class="alert alert-info">
          아직 리뷰가 없습니다. 첫 번째 리뷰를 작성해보세요!
        </div>
        
        <!-- 리뷰 목록 -->
        <div class="row">
          <div v-for="review in reviews" :key="review.reviewId" class="col-md-6">
            <div class="card review-card">
              <div class="card-body">
                <div class="review-header">
                  <h5 class="card-title">{{ review.title }}</h5>
                  <div class="star-rating">
                    <i v-for="n in 5" :key="n" class="bi" 
                       :class="n <= review.rating ? 'bi-star-fill' : 'bi-star'"></i>
                  </div>
                </div>
                <div class="review-meta">
                  <span><i class="bi bi-person-circle"></i> {{ review.username }}</span>
                  <span class="ms-3"><i class="bi bi-calendar3"></i> 
                    {{ formatDate(review.stayDate) }}
                  </span>
                  <span class="ms-3"><i class="bi bi-clock"></i> 
                    {{ formatDate(review.createdAt) }}
                  </span>
                </div>
                <p class="review-content">{{ review.content }}</p>
                
                <!-- 리뷰 작성자 또는 관리자만 수정/삭제 가능 -->
                <div v-if="isAuthorOrAdmin(review)" class="d-flex justify-content-end">
                  <router-link 
                    :to="`/review/edit/${review.reviewId}`" 
                    class="btn btn-sm btn-outline-primary me-2"
                  >
                    수정
                  </router-link>
                  <button 
                    @click="confirmDelete(review.reviewId)" 
                    class="btn btn-sm btn-outline-danger"
                  >
                    삭제
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 리뷰 목록 컴포넌트
 * 
 * 이 컴포넌트는 특정 숙소에 대한 리뷰 목록을 표시합니다.
 * 평점 요약, 평점 분포, 리뷰 목록을 제공하며, 리뷰 작성/수정/삭제 기능을 포함합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'ReviewList',
  components: {
    Layout
  },
  props: {
    // URL 파라미터로부터 숙소 ID를 받음
    accommodationId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      loading: true,
      error: null,
      reviews: [],
      accommodationTitle: '',
      reviewCount: 0,
      averageRating: 0,
      ratingDistribution: {}
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.user.isLoggedIn,
      userId: state => state.user.user?.id,
      userRole: state => state.user.user?.role
    }),
    
    /**
     * 숙소 ID (숫자 타입)
     */
    numericAccommodationId() {
      return parseInt(this.accommodationId);
    },
    
    /**
     * 사용자가 관리자인지 여부
     */
    isAdmin() {
      return this.userRole === 'ADMIN';
    }
  },
  created() {
    // 리뷰 목록 로드
    this.loadReviews();
  },
  methods: {
    ...mapActions('review', ['fetchReviewsByAccommodation', 'deleteReview']),
    
    /**
     * 리뷰 목록 로드
     */
    async loadReviews() {
      this.loading = true;
      this.error = null;
      
      try {
        // 리뷰 정보 가져오기
        const result = await this.fetchReviewsByAccommodation(this.numericAccommodationId);
        
        this.reviews = result.reviews;
        this.accommodationTitle = result.accommodationTitle;
        this.reviewCount = result.reviewCount;
        this.averageRating = result.averageRating;
        this.ratingDistribution = result.ratingDistribution;
      } catch (error) {
        console.error('리뷰 목록을 불러오는 중 오류가 발생했습니다:', error);
        this.error = '리뷰 목록을 불러올 수 없습니다. 다시 시도해주세요.';
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 평점 분포 백분율 계산
     * @param {number} rating - 평점 (1-5)
     * @returns {number} 백분율 (0-100)
     */
    calculatePercentage(rating) {
      const count = this.ratingDistribution[rating] || 0;
      return this.reviewCount > 0 ? (count / this.reviewCount) * 100 : 0;
    },
    
    /**
     * 리뷰 작성자 또는 관리자인지 확인
     * @param {Object} review - 리뷰 객체
     * @returns {boolean} 리뷰 작성자 또는 관리자인지 여부
     */
    isAuthorOrAdmin(review) {
      return this.isLoggedIn && (review.userId === this.userId || this.isAdmin);
    },
    
    /**
     * 리뷰 삭제 확인
     * @param {number} reviewId - 삭제할 리뷰 ID
     */
    confirmDelete(reviewId) {
      if (confirm('정말 삭제하시겠습니까?')) {
        this.deleteReviewItem(reviewId);
      }
    },
    
    /**
     * 리뷰 삭제 처리
     * @param {number} reviewId - 삭제할 리뷰 ID
     */
    async deleteReviewItem(reviewId) {
      try {
        await this.deleteReview(reviewId);
        // 리뷰 목록 새로고침
        this.loadReviews();
      } catch (error) {
        console.error('리뷰 삭제 중 오류가 발생했습니다:', error);
        alert('리뷰 삭제에 실패했습니다. 다시 시도해주세요.');
      }
    },
    
    /**
     * 날짜 포맷팅 (YYYY-MM-DD)
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
/* 리뷰 목록 스타일 */
.star-rating {
  color: #FFD700;
  font-size: 1.2rem;
}

.review-card {
  margin-bottom: 20px;
  transition: transform 0.3s;
}

.review-card:hover {
  transform: translateY(-5px);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.review-meta {
  color: #6c757d;
  font-size: 0.9rem;
}

.review-content {
  margin-top: 15px;
  white-space: pre-line;
}

.rating-summary {
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 30px;
}

.progress {
  height: 10px;
}

.progress-bar {
  background-color: #FFD700;
}
</style>