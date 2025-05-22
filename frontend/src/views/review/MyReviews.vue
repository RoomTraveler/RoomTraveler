<template>
  <Layout>
    <div class="container mt-5">
      <!-- 제목 및 개수 표시 -->
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
        <div v-for="review in reviews" :key="review.reviewId" class="col-md-6 mb-4">
          <div class="card h-100 shadow-sm">
            <div class="card-body d-flex flex-column">
              <!-- 리뷰 헤더 -->
              <div class="d-flex justify-content-between align-items-center mb-2">
                <h5 class="card-title mb-0">{{ review.title }}</h5>
                <div>
                  <span v-for="n in 5" :key="n">
                    <i
                        class="bi"
                        :class="n <= review.rating ? 'bi-star-fill text-warning' : 'bi-star text-secondary'"
                    ></i>
                  </span>
                </div>
              </div>

              <!-- 리뷰 메타 정보 -->
              <div class="mb-2 small text-muted">
                <router-link
                    :to="`/accommodation/detail/${review.accommodationId}`"
                    class="text-decoration-none text-primary fw-semibold"
                >
                  <i class="bi bi-building"></i> {{ review.accommodationTitle }}
                </router-link>
                <span class="ms-3">
                  <i class="bi bi-calendar3"></i> {{ formatDate(review.stayDate) }}
                </span>
                <span class="ms-3">
                  <i class="bi bi-clock"></i> {{ formatDate(review.createdAt) }}
                </span>
              </div>

              <!-- 리뷰 내용 -->
              <p class="flex-grow-1 mt-2 mb-3">{{ review.content }}</p>

              <!-- 수정/삭제 버튼 -->
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
 * 내 리뷰 목록 컴포넌트 (Bootstrap 적용)
 * - 사용자가 작성한 리뷰 목록을 Bootstrap 카드 스타일로 보여줍니다.
 * - 각 리뷰는 수정/삭제 기능을 가집니다.
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
    // 내 리뷰 목록 로드
    async loadMyReviews() {
      try {
        this.reviews = await this.fetchMyReviews();
      } catch (error) {
        console.error('리뷰 목록을 불러오는 중 오류가 발생했습니다:', error);
      }
    },
    // 리뷰 삭제 확인 및 처리
    confirmDelete(reviewId) {
      if (confirm('정말 삭제하시겠습니까?')) {
        this.deleteReviewItem(reviewId);
      }
    },
    async deleteReviewItem(reviewId) {
      try {
        await this.deleteReview(reviewId);
        // 삭제 후 목록 새로고침
        this.loadMyReviews();
      } catch (error) {
        console.error('리뷰 삭제 중 오류:', error);
        alert('리뷰 삭제에 실패했습니다. 다시 시도해주세요.');
      }
    },
    // 날짜 포맷팅 (YYYY-MM-DD)
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
/* 별점 색상은 Bootstrap 유틸리티 활용 */
.card-title {
  font-size: 1.15rem;
}
</style>
