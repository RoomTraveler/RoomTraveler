<template>
  <Layout>
    <div class="container mt-5">
      <div class="row mb-4">
        <div class="col">
          <h2>내 리뷰 목록</h2>
          <p class="text-muted">
            내가 작성한 리뷰 {{ reviews.length }}개
          </p>
        </div>
      </div>
      
      <!-- 리뷰가 없는 경우 -->
      <div v-if="reviews.length === 0" class="alert alert-info">
        작성한 리뷰가 없습니다. 숙소를 이용한 후 리뷰를 작성해보세요!
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
                <router-link 
                  :to="`/accommodation/detail/${review.accommodationId}`" 
                  class="accommodation-link"
                >
                  <i class="bi bi-building"></i> {{ review.accommodationTitle }}
                </router-link>
                <span class="ms-3"><i class="bi bi-calendar3"></i> 
                  {{ formatDate(review.stayDate) }}
                </span>
                <span class="ms-3"><i class="bi bi-clock"></i> 
                  {{ formatDate(review.createdAt) }}
                </span>
              </div>
              <p class="review-content">{{ review.content }}</p>
              
              <div class="d-flex justify-content-end">
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
  </Layout>
</template>

<script>
/**
 * 내 리뷰 목록 컴포넌트
 * 
 * 이 컴포넌트는 사용자가 작성한 리뷰 목록을 표시합니다.
 * 각 리뷰에 대한 상세 정보와 수정/삭제 기능을 제공합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'MyReviews',
  components: {
    Layout
  },
  data() {
    return {
      reviews: []
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.user.isLoggedIn,
      userId: state => state.user.user?.id
    })
  },
  created() {
    // 로그인 상태 확인
    if (!this.isLoggedIn) {
      this.$router.push('/user/login');
      return;
    }
    
    // 리뷰 목록 로드
    this.loadMyReviews();
  },
  methods: {
    ...mapActions('review', ['fetchMyReviews', 'deleteReview']),
    
    /**
     * 내 리뷰 목록 로드
     */
    async loadMyReviews() {
      try {
        this.reviews = await this.fetchMyReviews();
      } catch (error) {
        console.error('리뷰 목록을 불러오는 중 오류가 발생했습니다:', error);
      }
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
        this.loadMyReviews();
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

.accommodation-link {
  color: #0d6efd;
  text-decoration: none;
  font-weight: bold;
}

.accommodation-link:hover {
  text-decoration: underline;
}
</style>