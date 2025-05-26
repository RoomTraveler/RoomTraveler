<template>
  <Layout>
    <div class="container mt-5">
      <div class="row mb-4">
        <div class="col">
          <h2>리뷰 수정</h2>
          <p class="text-muted">리뷰 내용을 수정해주세요.</p>
        </div>
      </div>

      <!-- 로딩 스피너 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">리뷰 정보를 불러오는 중입니다...</p>
      </div>

      <!-- 에러 메시지 -->
      <div v-else-if="error" class="alert alert-danger" role="alert">
        {{ error }}
      </div>

      <!-- 리뷰 수정 폼 -->
      <div v-else class="card">
        <div class="card-body">
          <form @submit.prevent="submitReview" autocomplete="off">
            <!-- 평점(별점) 선택 -->
            <div class="mb-3">
              <label class="form-label d-block mb-1">평점</label>
              <div class="d-flex flex-row-reverse justify-content-end star-rating">
                <template v-for="n in 5" :key="n">
                  <input
                      class="btn-check"
                      :id="`star${n}`"
                      type="radio"
                      v-model="review.rating"
                      :value="n"
                      required
                  />
                  <label
                      class="btn btn-outline-warning px-2 py-0"
                      :for="`star${n}`"
                      :title="`${n}점`"
                      style="font-size:2rem;"
                  >&#9733;</label>
                </template>
              </div>
              <div class="form-text">별점을 선택해주세요 (필수)</div>
            </div>

            <!-- 제목 입력 -->
            <div class="mb-3">
              <label for="title" class="form-label">제목</label>
              <input
                  id="title"
                  type="text"
                  class="form-control"
                  v-model="review.title"
                  required
                  maxlength="60"
                  placeholder="리뷰 제목을 입력해주세요"
              />
            </div>

            <!-- 내용 입력 -->
            <div class="mb-3">
              <label for="content" class="form-label">내용</label>
              <textarea
                  id="content"
                  class="form-control"
                  v-model="review.content"
                  rows="5"
                  required
                  maxlength="1000"
                  placeholder="숙소에 대한 경험을 자세히 적어주세요. 다른 여행자들에게 도움이 됩니다."
              ></textarea>
            </div>

            <!-- 숙박 날짜 선택 -->
            <div class="mb-3">
              <label for="stayDate" class="form-label">숙박 날짜</label>
              <input
                  id="stayDate"
                  type="date"
                  class="form-control"
                  v-model="review.stayDate"
                  required
                  :max="today"
              />
            </div>

            <!-- 버튼 그룹 -->
            <div class="d-grid gap-2 d-md-flex justify-content-md-end mt-4">
              <router-link
                  :to="`/accommodation/detail/${review.accommodationId}`"
                  class="btn btn-outline-secondary"
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

<script setup>
/**
 * 리뷰 수정 페이지 (Bootstrap5 + Vue3 컴포지션)
 *
 * 기존 리뷰를 로딩해 폼에 바인딩, 수정 후 서버에 저장.
 */
import { ref, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import Layout from '@/components/layout/Layout.vue'

// 현재 라우트, 라우터, Vuex 스토어 사용
const route = useRoute()
const router = useRouter()
const store = useStore()

// 리뷰 ID (props 대신 라우트 파라미터 사용)
const reviewId = computed(() => route.params.reviewId || route.query.reviewId)

// 폼 관련 상태
const loading = ref(true)
const error = ref(null)
const review = ref({
  reviewId: null,
  accommodationId: null,
  rating: 5,
  title: '',
  content: '',
  stayDate: getTodayFormatted()
})

// 오늘 날짜 (max값 용)
const today = getTodayFormatted()

// 최초 렌더시 리뷰 정보 불러오기
onMounted(loadReview)

// 리뷰 정보 불러오기 (비동기)
async function loadReview() {
  loading.value = true
  error.value = null
  try {
    // review 모듈에 fetchReview 액션 필요
    const data = await store.dispatch('review/fetchReview', reviewId.value)
    review.value = {
      ...data,
      rating: parseInt(data.rating), // radio 바인딩용 숫자
      stayDate: formatDate(data.stayDate)
    }
  } catch (e) {
    error.value = '리뷰 정보를 불러올 수 없습니다. 다시 시도해주세요.'
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 리뷰 수정 제출
async function submitReview() {
  try {
    const reviewData = {
      reviewId: parseInt(reviewId.value),
      accommodationId: review.value.accommodationId,
      rating: parseInt(review.value.rating),
      title: review.value.title,
      content: review.value.content,
      stayDate: review.value.stayDate
    }
    // review 모듈에 updateReview 액션 필요
    await store.dispatch('review/updateReview', reviewData)
    router.push(`/accommodation/detail/${review.value.accommodationId}`)
  } catch (e) {
    alert('리뷰 수정에 실패했습니다. 다시 시도해주세요.')
    console.error(e)
  }
}

// 오늘 날짜 반환 (YYYY-MM-DD)
function getTodayFormatted() {
  const today = new Date()
  const yyyy = today.getFullYear()
  const mm = String(today.getMonth() + 1).padStart(2, '0')
  const dd = String(today.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}

// 날짜 포맷 (YYYY-MM-DD)
function formatDate(date) {
  if (!date) return getTodayFormatted()
  const d = new Date(date)
  if (isNaN(d.getTime())) return getTodayFormatted()
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}
</script>

<style scoped>
/* Bootstrap 스타일에 맞춘 별점(Star Rating) - 커스텀 최소화 */
.star-rating label {
  color: #FFD700;
  cursor: pointer;
  transition: color 0.2s;
}
.star-rating input[type="radio"]:checked + label,
.star-rating label:hover,
.star-rating label:hover ~ label {
  color: #FFA500;
}
.star-rating input[type="radio"] {
  display: none;
}
</style>
