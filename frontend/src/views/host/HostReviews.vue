<template>
  <div class="container mt-5 mb-5">
    <h1 class="mb-4">
      <i class="bi bi-star"></i> 호스트 리뷰 관리
    </h1>
    <!-- 알림 메시지 -->
    <div
        v-if="message"
        class="alert alert-success alert-dismissible fade show"
        role="alert"
    >
      {{ message }}
      <button
          type="button"
          class="btn-close"
          @click="message = ''"
          aria-label="Close"
      ></button>
    </div>
    <!-- 필터링 옵션 -->
    <div class="card filter-card">
      <div class="card-body">
        <h5 class="card-title">리뷰 필터링</h5>
        <div class="row g-3">
          <div class="col-md-4">
            <label for="accommodationId" class="form-label">숙소 선택</label>
            <select
                class="form-select"
                id="accommodationId"
                v-model="filters.accommodationId"
            >
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
            <select
                class="form-select"
                id="rating"
                v-model="filters.rating"
            >
              <option value="">모든 별점</option>
              <option value="5">5점</option>
              <option value="4">4점</option>
              <option value="3">3점</option>
              <option value="2">2점</option>
              <option value="1">1점</option>
            </select>
          </div>
          <div class="col-md-4 d-flex align-items-end">
            <button
                type="button"
                class="btn btn-primary"
                @click="applyFilters"
            >
              필터 적용
            </button>
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
    <div
        v-if="reviews.length === 0"
        class="alert alert-info"
    >
      <i class="bi bi-info-circle"></i> 리뷰가 없습니다.
    </div>
    <div v-else class="row">
      <div
          v-for="review in reviews"
          :key="review.reviewId"
          class="col-md-6"
      >
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
            <h6
                v-if="review.title"
                class="card-subtitle mb-2"
            >
              {{ review.title }}
            </h6>
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

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const message = ref('')

const accommodations = ref([])
const reviews = ref([])

const totalReviews = ref(0)
const averageRating = ref(0)

const filters = ref({
  accommodationId: '',
  rating: '',
})

// 라우터
const route = useRoute()
const router = useRouter()

// 별점 아이콘 클래스
function getStarClass(index, rating) {
  if (index <= Math.floor(rating)) return 'bi bi-star-fill'
  if (index - rating <= 0.5 && index > rating) return 'bi bi-star-half'
  return 'bi bi-star'
}

// 날짜 포맷팅
function formatDate(dateString) {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(date)
}

// 숙소 목록 불러오기
async function loadAccommodations() {
  try {
    const res = await fetch('/api/host/accommodations/list')
    if (!res.ok) throw new Error('숙소 목록을 불러오는데 실패했습니다.')
    accommodations.value = await res.json()
  } catch (error) {
    console.error('숙소 목록 로드 오류:', error)
  }
}

// 리뷰 목록 불러오기
async function loadReviews() {
  try {
    let url = '/api/host/reviews'
    const params = new URLSearchParams()
    if (filters.value.accommodationId)
      params.append('accommodationId', filters.value.accommodationId)
    if (filters.value.rating)
      params.append('rating', filters.value.rating)
    if (params.toString()) url += `?${params.toString()}`
    const res = await fetch(url)
    if (!res.ok) throw new Error('리뷰 목록을 불러오는데 실패했습니다.')
    const data = await res.json()
    reviews.value = data.reviews
    totalReviews.value = data.totalReviews
    averageRating.value = data.averageRating
  } catch (error) {
    console.error('리뷰 목록 로드 오류:', error)
    message.value = '리뷰 목록을 불러오는데 실패했습니다.'
    reviews.value = []
    totalReviews.value = 0
    averageRating.value = 0
  }
}

// 필터 적용 및 쿼리 파라미터 동기화
function applyFilters() {
  const query = {}
  if (filters.value.accommodationId)
    query.accommodationId = filters.value.accommodationId
  if (filters.value.rating)
    query.rating = filters.value.rating
  router.replace({ query })
  loadReviews()
}

// 라우터 쿼리 동기화
onMounted(() => {
  const query = route.query
  if (query.accommodationId) filters.value.accommodationId = query.accommodationId
  if (query.rating) filters.value.rating = query.rating
  if (query.message) message.value = query.message
  loadAccommodations()
  loadReviews()
})

// 쿼리 파라미터 변화 감지(뒤로가기 등 대응)
watch(
    () => route.query,
    (query) => {
      filters.value.accommodationId = query.accommodationId || ''
      filters.value.rating = query.rating || ''
      loadReviews()
    }
)
</script>

<style scoped>
.review-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}
.review-card:hover {
  transform: translateY(-5px);
}
.stars {
  color: #ffc107;
  font-size: 1.2rem;
}
.filter-card {
  margin-bottom: 20px;
}
</style>
