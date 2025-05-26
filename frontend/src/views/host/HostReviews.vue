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
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import apiUtils from '@/api/index'
import { useUserStore } from '@/store/userStore'
import { getHostAccommodations } from "@/api/hostApi.js"

const { api } = apiUtils
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

const message = ref('')
const loadingAccommodations = ref(false)
const loadingReviews = ref(false)

const accommodations = ref([])
const reviews = ref([])
const allHostReviews = ref([])

const filters = ref({
  accommodationId: '',
  rating: '',
})

// 별점 아이콘 클래스
function getStarClass(index, rating) {
  const roundedRating = Math.round(rating * 2) / 2
  if (index <= roundedRating) return 'bi bi-star-fill'
  if (index - 0.5 === roundedRating) return 'bi bi-star-half'
  return 'bi bi-star'
}

// 날짜 포맷팅
function formatDate(dateString) {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).format(date)
  } catch (e) {
    return dateString
  }
}

function resetData() {
  accommodations.value = []
  allHostReviews.value = []
  reviews.value = []
}

// 호스트의 숙소 목록 불러오기 (필터 옵션용)
async function loadHostAccommodationsData() {
  const hostId = userStore.user?.hostId
  if (!hostId) {
    accommodations.value = []
    return
  }

  loadingAccommodations.value = true
  try {
    const response = await getHostAccommodations(hostId, 1, 1000)

    if (response && Array.isArray(response.accommodations)) {
      accommodations.value = response.accommodations
    } else if (response && Array.isArray(response.content)) {
      accommodations.value = response.content
    } else if (Array.isArray(response)) {
      accommodations.value = response
    } else {
      accommodations.value = []
      console.warn('숙소 목록 API 응답 형식이 예상과 다릅니다:', response)
    }
  } catch (error) {
    console.error('숙소 목록 로드 오류:', error)
    message.value = '숙소 목록을 불러오는데 실패했습니다.'
    accommodations.value = []
  } finally {
    loadingAccommodations.value = false
  }
}

// 호스트의 모든 리뷰 또는 특정 숙소의 리뷰 불러오기
async function loadHostReviewsData() {
  const hostId = userStore.user?.hostId

  if (!hostId) {
    message.value = '호스트 정보를 불러올 수 없어 리뷰를 로드할 수 없습니다.'
    allHostReviews.value = []
    reviews.value = []
    return
  }

  loadingReviews.value = true
  try {
    const response = await api.get(`/api/reviews/host/${hostId}`)
    allHostReviews.value = Array.isArray(response.data) ? response.data : []
    applyClientSideFilters()
  } catch (error) {
    console.error('리뷰 목록 로드 오류:', error)
    message.value = '리뷰 목록을 불러오는데 실패했습니다.'
    allHostReviews.value = []
    reviews.value = []
  } finally {
    loadingReviews.value = false
  }
}

// 클라이언트 사이드 필터링 (숙소 ID 및 별점)
function applyClientSideFilters() {
  let filtered = [...allHostReviews.value]

  if (filters.value.accommodationId) {
    filtered = filtered.filter(review => review.accommodationId?.toString() === filters.value.accommodationId)
  }

  if (filters.value.rating) {
    filtered = filtered.filter(review => review.rating?.toString() === filters.value.rating)
  }
  reviews.value = filtered
}

// 필터 적용 버튼 클릭 시 (URL 쿼리 업데이트)
function applyFilters() {
  const query = {}
  if (filters.value.accommodationId) query.accommodationId = filters.value.accommodationId
  if (filters.value.rating) query.rating = filters.value.rating
  router.push({ query: Object.keys(query).length > 0 ? query : {} })
}

// 리뷰 통계 계산
const totalReviews = computed(() => reviews.value.length)
const averageRating = computed(() => {
  if (reviews.value.length === 0) return 0
  const sum = reviews.value.reduce((acc, review) => acc + (review.rating || 0), 0)
  return parseFloat((sum / reviews.value.length).toFixed(1))
})

async function initializePageData() {
  if (userStore.isAuthenticated && userStore.user?.hostId) {
    message.value = ''
    await loadHostAccommodationsData()
    await loadHostReviewsData()
  } else if (userStore.isAuthenticated && !userStore.user?.hostId) {
    resetData()
    message.value = '호스트 계정으로 로그인해야 리뷰를 관리할 수 있습니다.'
  } else {
    resetData()
    message.value = '리뷰를 보려면 먼저 로그인해주세요.'
  }
}

// Lifecycle hooks and watchers
onMounted(() => {
  filters.value.accommodationId = route.query.accommodationId || ''
  filters.value.rating = route.query.rating || ''
  initializePageData()
})

watch(() => userStore.user, (newUser, oldUser) => {
  if (newUser?.id !== oldUser?.id || newUser?.hostId !== oldUser?.hostId) {
    initializePageData()
  }
}, { deep: true })

watch(() => route.query, (newQuery, oldQuery) => {
  const newAccommodationId = newQuery.accommodationId || ''
  const newRating = newQuery.rating || ''

  let filtersChanged = false
  if (filters.value.accommodationId !== newAccommodationId) {
    filters.value.accommodationId = newAccommodationId
    filtersChanged = true
  }
  if (filters.value.rating !== newRating) {
    filters.value.rating = newRating
    filtersChanged = true
  }

  if (filtersChanged && userStore.user?.hostId) {
    applyClientSideFilters()
  }
}, { deep: true })
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

