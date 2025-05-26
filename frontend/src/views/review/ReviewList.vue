<template>
  <Layout>
    <div class="container mt-5">
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

      <div v-else>
        <!-- 헤더 및 리뷰 작성 버튼 -->
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
        <div class="bg-light rounded p-4 mb-4">
          <div class="row align-items-center">
            <div class="col-md-3 text-center mb-3 mb-md-0">
              <h1 class="display-4 fw-bold mb-2">
                {{ averageRating ? averageRating.toFixed(1) : '0.0' }}
              </h1>
              <div class="star-rating mb-1">
                <template v-for="i in 5" :key="i">
                  <i v-if="i <= Math.floor(averageRating)" class="bi bi-star-fill"></i>
                  <i v-else class="bi bi-star"></i>
                </template>
              </div>
              <p class="text-muted">{{ reviewCount }}개 리뷰</p>
            </div>
            <div class="col-md-9">
              <!-- 평점 분포(Progress Bar) -->
              <div v-for="rating in [5, 4, 3, 2, 1]" :key="rating" class="row align-items-center mb-2">
                <div class="col-2">{{ rating }}점</div>
                <div class="col-8">
                  <div class="progress" style="height:10px;">
                    <div
                        class="progress-bar bg-warning"
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

        <!-- 리뷰 카드 목록 -->
        <div class="row">
          <div v-for="review in reviews" :key="review.reviewId" class="col-md-6 mb-4">
            <div class="card h-100 shadow-sm review-card">
              <div class="card-body">
                <!-- 리뷰 제목, 별점 -->
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <h5 class="card-title mb-0">{{ review.title }}</h5>
                  <div class="star-rating ms-2">
                    <i v-for="n in 5" :key="n" class="bi" :class="n <= review.rating ? 'bi-star-fill' : 'bi-star'"></i>
                  </div>
                </div>
                <!-- 리뷰 메타 정보 -->
                <div class="mb-2 text-secondary small">
                  <span><i class="bi bi-person-circle"></i> {{ review.username }}</span>
                  <span class="ms-3"><i class="bi bi-calendar3"></i> {{ formatDate(review.stayDate) }}</span>
                  <span class="ms-3"><i class="bi bi-clock"></i> {{ formatDate(review.createdAt) }}</span>
                </div>
                <!-- 리뷰 본문 -->
                <p class="review-content mb-3">{{ review.content }}</p>
                <!-- 수정/삭제 버튼 -->
                <div v-if="isAuthorOrAdmin(review)" class="d-flex justify-content-end gap-2">
                  <router-link
                      :to="`/review/edit/${review.reviewId}`"
                      class="btn btn-sm btn-outline-primary"
                  >수정</router-link>
                  <button
                      @click="confirmDelete(review.reviewId)"
                      class="btn btn-sm btn-outline-danger"
                  >삭제</button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
/**
 * 리뷰 목록 컴포넌트 (Bootstrap5 + Vue3 컴포지션)
 *
 * 숙소별 리뷰 목록, 평점, 분포, CRUD 버튼을 제공.
 */
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
import { useRoute } from 'vue-router'
import Layout from '@/components/layout/Layout.vue'

// 라우트, 스토어
const route = useRoute()
const store = useStore()

// 숙소ID (props 대신 라우트에서)
const accommodationId = computed(() => route.params.accommodationId || route.query.accommodationId)

// 상태 변수
const loading = ref(true)
const error = ref(null)
const reviews = ref([])
const accommodationTitle = ref('')
const reviewCount = ref(0)
const averageRating = ref(0)
const ratingDistribution = ref({})

// 유저 정보
const isLoggedIn = computed(() => store.state.user.isLoggedIn)
const userId = computed(() => store.state.user.user?.id)
const userRole = computed(() => store.state.user.user?.role)
const isAdmin = computed(() => userRole.value === 'ADMIN')

// 최초 마운트 시 리뷰 불러오기
onMounted(loadReviews)

// 리뷰 불러오기
async function loadReviews() {
  loading.value = true
  error.value = null
  try {
    // review 모듈에 fetchReviewsByAccommodation 액션 필요
    const result = await store.dispatch('review/fetchReviewsByAccommodation', parseInt(accommodationId.value))
    reviews.value = result.reviews
    accommodationTitle.value = result.accommodationTitle
    reviewCount.value = result.reviewCount
    averageRating.value = result.averageRating
    ratingDistribution.value = result.ratingDistribution
  } catch (e) {
    error.value = '리뷰 목록을 불러올 수 없습니다. 다시 시도해주세요.'
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 평점 분포(%) 계산
function calculatePercentage(rating) {
  const count = ratingDistribution.value[rating] || 0
  return reviewCount.value > 0 ? (count / reviewCount.value) * 100 : 0
}

// 리뷰 작성자 or 관리자 판별
function isAuthorOrAdmin(review) {
  return isLoggedIn.value && (review.userId === userId.value || isAdmin.value)
}

// 리뷰 삭제 확인
function confirmDelete(reviewId) {
  if (confirm('정말 삭제하시겠습니까?')) {
    deleteReviewItem(reviewId)
  }
}

// 리뷰 삭제 처리
async function deleteReviewItem(reviewId) {
  try {
    await store.dispatch('review/deleteReview', reviewId)
    loadReviews()
  } catch (e) {
    alert('리뷰 삭제에 실패했습니다. 다시 시도해주세요.')
    console.error(e)
  }
}

// 날짜 포맷 (YYYY-MM-DD)
function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd}`
}
</script>

<style scoped>
/* 별점 스타일 (부트스트랩 색상 활용) */
.star-rating {
  color: #FFD700;
  font-size: 1.2rem;
}
.review-card {
  transition: transform 0.2s;
}
.review-card:hover {
  transform: translateY(-4px) scale(1.01);
}
.review-content {
  white-space: pre-line;
}
</style>
