<template>
  <Layout>
    <div class="container mt-5">
      <div class="row mb-4">
        <div class="col">
          <h2>리뷰 수정</h2>
          <p class="text-muted">리뷰 내용을 수정해주세요.</p>
        </div>
      </div>

      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">리뷰 정보를 불러오는 중입니다...</p>
      </div>

      <div v-else-if="error" class="alert alert-danger" role="alert">
        {{ error }}
      </div>

      <div v-else class="card">
        <div class="card-body">
          <form @submit.prevent="submitReview">
            <!-- 평점 선택 -->
            <div class="mb-3">
              <label class="form-label">평점</label>
              <div class="rating">
                <input type="radio" id="star5" v-model="review.rating" value="5" required />
                <label for="star5" title="5점"></label>
                <input type="radio" id="star4" v-model="review.rating" value="4" />
                <label for="star4" title="4점"></label>
                <input type="radio" id="star3" v-model="review.rating" value="3" />
                <label for="star3" title="3점"></label>
                <input type="radio" id="star2" v-model="review.rating" value="2" />
                <label for="star2" title="2점"></label>
                <input type="radio" id="star1" v-model="review.rating" value="1" />
                <label for="star1" title="1점"></label>
              </div>
              <div class="form-text">별점을 선택해주세요 (필수)</div>
            </div>

            <!-- 제목 입력 -->
            <div class="mb-3">
              <label for="title" class="form-label">제목</label>
              <input 
                type="text" 
                class="form-control" 
                id="title" 
                v-model="review.title" 
                required
                placeholder="리뷰 제목을 입력해주세요"
              >
            </div>

            <!-- 내용 입력 -->
            <div class="mb-3">
              <label for="content" class="form-label">내용</label>
              <textarea 
                class="form-control" 
                id="content" 
                v-model="review.content" 
                rows="5" 
                required
                placeholder="숙소에 대한 경험을 자세히 적어주세요. 다른 여행자들에게 도움이 됩니다."
              ></textarea>
            </div>

            <!-- 숙박 날짜 선택 -->
            <div class="mb-3">
              <label for="stayDate" class="form-label">숙박 날짜</label>
              <input 
                type="date" 
                class="form-control" 
                id="stayDate" 
                v-model="review.stayDate" 
                required
                :max="today"
              >
            </div>

            <div class="d-grid gap-2 d-md-flex justify-content-md-end">
              <router-link 
                :to="`/accommodation/detail/${review.accommodationId}`" 
                class="btn btn-secondary"
              >
                취소
              </router-link>
              <button type="submit" class="btn btn-primary">리뷰 수정</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 리뷰 수정 컴포넌트
 * 
 * 이 컴포넌트는 기존 리뷰를 수정하는 폼을 제공합니다.
 * 기존 리뷰 정보를 불러와 폼에 표시하고, 수정된 내용을 서버에 전송합니다.
 */
import { mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'ReviewEditForm',
  components: {
    Layout
  },
  props: {
    // URL 파라미터로부터 리뷰 ID를 받음
    reviewId: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      loading: true,
      error: null,
      review: {
        reviewId: null,
        accommodationId: null,
        rating: '5',
        title: '',
        content: '',
        stayDate: this.getTodayFormatted()
      },
      today: this.getTodayFormatted()
    };
  },
  created() {
    // 리뷰 정보 로드
    this.loadReview();
  },
  methods: {
    ...mapActions('review', ['fetchReview', 'updateReview']),
    
    /**
     * 리뷰 정보 로드
     */
    async loadReview() {
      this.loading = true;
      this.error = null;
      
      try {
        // 리뷰 정보 가져오기
        const reviewData = await this.fetchReview(this.reviewId);
        
        // 리뷰 데이터 설정
        this.review = {
          ...reviewData,
          rating: reviewData.rating.toString(), // 문자열로 변환 (라디오 버튼 바인딩용)
          stayDate: this.formatDate(reviewData.stayDate) // 날짜 포맷팅
        };
      } catch (error) {
        console.error('리뷰 정보를 불러오는 중 오류가 발생했습니다:', error);
        this.error = '리뷰 정보를 불러올 수 없습니다. 다시 시도해주세요.';
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 리뷰 수정 제출 처리
     */
    async submitReview() {
      try {
        // 리뷰 데이터 준비
        const reviewData = {
          reviewId: parseInt(this.reviewId),
          accommodationId: this.review.accommodationId,
          rating: parseInt(this.review.rating),
          title: this.review.title,
          content: this.review.content,
          stayDate: this.review.stayDate
        };
        
        // 리뷰 수정 API 호출
        await this.updateReview(reviewData);
        
        // 성공 시 숙소 상세 페이지로 이동
        this.$router.push(`/accommodation/detail/${this.review.accommodationId}`);
      } catch (error) {
        console.error('리뷰 수정 중 오류가 발생했습니다:', error);
        alert('리뷰 수정에 실패했습니다. 다시 시도해주세요.');
      }
    },
    
    /**
     * 오늘 날짜를 YYYY-MM-DD 형식으로 반환
     */
    getTodayFormatted() {
      const today = new Date();
      const yyyy = today.getFullYear();
      const mm = String(today.getMonth() + 1).padStart(2, '0');
      const dd = String(today.getDate()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd}`;
    },
    
    /**
     * 날짜 포맷팅 (YYYY-MM-DD)
     */
    formatDate(date) {
      if (!date) return this.getTodayFormatted();
      
      const d = new Date(date);
      if (isNaN(d.getTime())) return this.getTodayFormatted();
      
      const yyyy = d.getFullYear();
      const mm = String(d.getMonth() + 1).padStart(2, '0');
      const dd = String(d.getDate()).padStart(2, '0');
      return `${yyyy}-${mm}-${dd}`;
    }
  }
};
</script>

<style scoped>
/* 별점 선택 스타일 */
.rating {
  display: flex;
  flex-direction: row-reverse;
  justify-content: flex-end;
}

.rating > input {
  display: none;
}

.rating > label {
  position: relative;
  width: 1.1em;
  font-size: 2.5em;
  color: #FFD700;
  cursor: pointer;
}

.rating > label::before {
  content: "\2605";
  position: absolute;
  opacity: 0;
}

.rating > label:hover:before,
.rating > label:hover ~ label:before {
  opacity: 1 !important;
}

.rating > input:checked ~ label:before {
  opacity: 1;
}

.rating > input:checked ~ label:hover:before {
  opacity: 1;
}
</style>