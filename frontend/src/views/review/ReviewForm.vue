<template>
  <Layout>
    <div class="container mt-5">
      <!-- 제목 및 안내 문구 -->
      <div class="row mb-4">
        <div class="col">
          <h2>리뷰 작성</h2>
          <p class="text-muted">숙소에 대한 솔직한 리뷰를 남겨주세요.</p>
        </div>
      </div>

      <!-- 리뷰 작성 카드 -->
      <div class="card shadow-sm">
        <div class="card-body">
          <form @submit.prevent="submitReview">
            <!-- 평점 선택 -->
            <div class="mb-3">
              <label class="form-label">평점</label>
              <div class="d-flex flex-row-reverse justify-content-end rating">
                <template v-for="star in 5">
                  <input
                      :id="`star${star}`"
                      :key="star"
                      type="radio"
                      v-model="review.rating"
                      :value="star"
                      :required="star === 5"
                  />
                  <label
                      :for="`star${star}`"
                      :title="`${star}점`"
                      class="fs-2 mx-1"
                  >
                    <i
                        class="bi"
                        :class="star <= review.rating ? 'bi-star-fill text-warning' : 'bi-star text-secondary'"
                    ></i>
                  </label>
                </template>
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

            <!-- 버튼 -->
            <div class="d-grid gap-2 d-md-flex justify-content-md-end">
              <router-link
                  :to="`/accommodation/detail/${accommodationId}`"
                  class="btn btn-secondary"
              >
                취소
              </router-link>
              <button type="submit" class="btn btn-primary">리뷰 등록</button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 리뷰 작성 컴포넌트 (Bootstrap)
 *
 * 이 컴포넌트는 숙소에 대한 리뷰를 작성하는 폼을 제공합니다.
 * 별점, 제목, 내용, 숙박 날짜를 입력받아 서버에 전송합니다.
 */
import { mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'ReviewForm',
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
      review: {
        rating: 5, // 기본값 5점
        title: '',
        content: '',
        stayDate: this.getTodayFormatted()
      },
      today: this.getTodayFormatted()
    };
  },
  computed: {
    /**
     * 숙소 ID (숫자 타입)
     */
    numericAccommodationId() {
      return parseInt(this.accommodationId);
    }
  },
  methods: {
    ...mapActions('review', ['addReview']),
    /**
     * 리뷰 제출 처리
     */
    async submitReview() {
      try {
        // 리뷰 데이터 준비
        const reviewData = {
          accommodationId: this.numericAccommodationId,
          rating: parseInt(this.review.rating),
          title: this.review.title,
          content: this.review.content,
          stayDate: this.review.stayDate
        };
        // 리뷰 등록 API 호출
        await this.addReview(reviewData);
        // 성공 시 숙소 상세 페이지로 이동
        this.$router.push(`/accommodation/detail/${this.accommodationId}`);
      } catch (error) {
        console.error('리뷰 등록 중 오류가 발생했습니다:', error);
        alert('리뷰 등록에 실패했습니다. 다시 시도해주세요.');
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
    }
  }
};
</script>

<style scoped>
/* 별점 스타일: Bootstrap 아이콘 + 시멘틱 스타일 */
.rating > input {
  display: none;
}
.rating > label {
  cursor: pointer;
  transition: transform 0.15s;
}
.rating > label:hover,
.rating > label:hover ~ label {
  transform: scale(1.15);
}
</style>
