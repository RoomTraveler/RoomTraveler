<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">숙소 통계</h2>

      <!-- 알림 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>

      <!-- 숙소 선택/기간 -->
      <div class="card mb-4">
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-6">
              <label for="accommodationSelect" class="form-label">숙소 선택</label>
              <select
                  class="form-select"
                  id="accommodationSelect"
                  v-model="selectedAccommodationId"
              >
                <option value="">숙소를 선택하세요</option>
                <option v-for="a in accommodations" :key="a.accommodationId" :value="a.accommodationId">
                  {{ a.title }}
                </option>
              </select>
            </div>
            <div class="col-md-3">
              <label for="startDate" class="form-label">시작일</label>
              <input type="date" class="form-control" id="startDate" v-model="dateRange.startDate" />
            </div>
            <div class="col-md-3">
              <label for="endDate" class="form-label">종료일</label>
              <input type="date" class="form-control" id="endDate" v-model="dateRange.endDate" />
            </div>
          </div>
        </div>
      </div>

      <!-- 로딩 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status"></div>
        <p class="mt-2">통계 정보를 불러오는 중입니다...</p>
      </div>

      <!-- 숙소 선택 X -->
      <div v-else-if="!selectedAccommodationId" class="alert alert-info">
        통계를 확인할 숙소를 선택해주세요.
      </div>

      <!-- 통계 -->
      <div v-else-if="stats" key="stats-render">
        <div class="row">
          <div class="col-md-3 mb-4" v-for="card in summaryCards" :key="card.title">
            <div class="card h-100" :class="card.borderClass">
              <div class="card-body text-center">
                <h5 class="card-title">{{ card.title }}</h5>
                <p class="display-4" v-html="card.value"></p>
              </div>
            </div>
          </div>
        </div>

        <div class="row">
          <div class="col-md-6 mb-4">
            <div class="card">
              <div class="card-header"><h5 class="mb-0">월별 매출</h5></div>
              <div class="card-body"><canvas ref="revenueChartRef" height="300"></canvas></div>
            </div>
          </div>
          <div class="col-md-6 mb-4">
            <div class="card">
              <div class="card-header"><h5 class="mb-0">객실별 예약</h5></div>
              <div class="card-body"><canvas ref="roomChartRef" height="300"></canvas></div>
            </div>
          </div>
          <div class="col-md-6 mb-4">
            <div class="card">
              <div class="card-header"><h5 class="mb-0">예약 상태</h5></div>
              <div class="card-body"><canvas ref="statusChartRef" height="300"></canvas></div>
            </div>
          </div>
          <div class="col-md-6 mb-4">
            <div class="card">
              <div class="card-header"><h5 class="mb-0">평점 분포</h5></div>
              <div class="card-body"><canvas ref="ratingChartRef" height="300"></canvas></div>
            </div>
          </div>
        </div>

        <!-- 최근 예약 목록 -->
        <div class="card mb-4">
          <div class="card-header"><h5 class="mb-0">최근 예약</h5></div>
          <div class="card-body">
            <div v-if="stats.recentReservations?.length" class="table-responsive">
              <table class="table table-striped">
                <thead>
                <tr>
                  <th>예약 ID</th><th>객실</th><th>게스트</th><th>체크인</th><th>체크아웃</th><th>금액</th><th>상태</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="r in stats.recentReservations" :key="r.reservationId">
                  <td>{{ r.reservationId }}</td>
                  <td>{{ r.roomName }}</td>
                  <td>{{ r.guestName }}</td>
                  <td>{{ formatDate(r.checkInDate) }}</td>
                  <td>{{ formatDate(r.checkOutDate) }}</td>
                  <td>{{ formatCurrency(r.totalPrice) }}</td>
                  <td>
                      <span :class="getStatusBadgeClass(r.status)">
                        {{ getStatusName(r.status) }}
                      </span>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
            <div v-else class="text-center py-3">
              <p>최근 예약 내역이 없습니다.</p>
            </div>
          </div>
        </div>

        <!-- 최근 리뷰 목록 -->
        <div class="card mb-4">
          <div class="card-header"><h5 class="mb-0">최근 리뷰</h5></div>
          <div class="card-body">
            <div v-if="stats.recentReviews?.length">
              <div v-for="review in stats.recentReviews" :key="review.reviewId" class="mb-3 p-3 border rounded">
                <div class="d-flex justify-content-between align-items-center mb-2">
                  <div>
                    <strong>{{ review.guestName }}</strong>
                    <span class="ms-2 text-muted">{{ formatDate(review.createdAt) }}</span>
                  </div>
                  <div class="text-warning">
                    <i v-for="n in 5" :key="n" class="bi" :class="n <= review.rating ? 'bi-star-fill' : 'bi-star'"></i>
                  </div>
                </div>
                <h6>{{ review.title }}</h6>
                <p class="mb-0">{{ review.content }}</p>
              </div>
            </div>
            <div v-else class="text-center py-3">
              <p>최근 리뷰가 없습니다.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import Layout from '@/components/layout/Layout.vue'
import Chart from 'chart.js/auto'

// ---- 가짜 데이터 API (실제에 맞게 바꿔 사용) ----
async function fetchHostAccommodations() {
  // 실제로는 API 호출
  return [
    { accommodationId: '1', title: '호캉스 호텔' },
    { accommodationId: '2', title: '펜션 2호점' }
  ]
}
async function fetchAccommodationStats({ accommodationId, startDate, endDate }) {
  // 실제로는 API 호출
  return {
    totalReservations: 14,
    totalRevenue: 2500000,
    averageRating: 4.35,
    occupancyRate: 79.2,
    monthlyRevenue: [
      { month: '2024-03', revenue: 700000 },
      { month: '2024-04', revenue: 800000 },
      { month: '2024-05', revenue: 1000000 }
    ],
    roomReservations: [
      { roomName: 'Deluxe', reservationCount: 6 },
      { roomName: 'Suite', reservationCount: 8 }
    ],
    reservationStatus: [
      { status: 'CONFIRMED', count: 7 },
      { status: 'PENDING', count: 3 },
      { status: 'CANCELLED', count: 4 }
    ],
    ratingDistribution: { '1': 1, '2': 0, '3': 2, '4': 3, '5': 8 },
    recentReservations: [
      {
        reservationId: '1001',
        roomName: 'Deluxe',
        guestName: '홍길동',
        checkInDate: '2024-05-20',
        checkOutDate: '2024-05-21',
        totalPrice: 140000,
        status: 'CONFIRMED'
      }
    ],
    recentReviews: [
      {
        reviewId: '1',
        guestName: '김철수',
        createdAt: '2024-05-19',
        rating: 5,
        title: '정말 좋아요!',
        content: '깔끔하고 직원 친절함'
      }
    ]
  }
}

// ---- 상태/반응형 변수 ----
const router = useRouter()
const route = useRoute()

const message = ref('')
const error = ref('')
const loading = ref(false)
const accommodations = ref([])
const selectedAccommodationId = ref('')
const stats = ref(null)
const dateRange = reactive({
  startDate: getDefaultStartDate(),
  endDate: getDefaultEndDate()
})

// ---- 권한 체크 (Pinia/store 등 실제로 대체 필요) ----
const isLoggedIn = true   // 실제 store 등에서 불러와야 함
const isHost = true       // 실제 store 등에서 불러와야 함

// ---- 차트 관련 ref ----
const revenueChartRef = ref(null)
const roomChartRef = ref(null)
const statusChartRef = ref(null)
const ratingChartRef = ref(null)
let chartInstances = {
  revenue: null, room: null, status: null, rating: null
}

// ---- 요약 카드 데이터 ----
const summaryCards = computed(() => [
  {
    title: '총 예약 수',
    value: stats.value ? stats.value.totalReservations : '-',
    borderClass: 'border-primary'
  },
  {
    title: '총 매출',
    value: stats.value ? formatCurrency(stats.value.totalRevenue) : '-',
    borderClass: 'border-success'
  },
  {
    title: '평균 평점',
    value: stats.value ? `${stats.value.averageRating?.toFixed(1) || '0.0'} <small class="text-muted">/5</small>` : '-',
    borderClass: 'border-info'
  },
  {
    title: '객실 점유율',
    value: stats.value ? `${stats.value.occupancyRate?.toFixed(1) || '0.0'}%` : '-',
    borderClass: 'border-warning'
  }
])

// ---- API 로드
async function loadAccommodations() {
  try {
    accommodations.value = await fetchHostAccommodations()
    // 쿼리로 미리 선택된 숙소 적용
    if (route.query.accommodationId) {
      selectedAccommodationId.value = route.query.accommodationId
    }
  } catch (e) {
    error.value = '숙소 목록을 불러오는 중 오류가 발생했습니다.'
  }
}
async function loadStats() {
  if (!selectedAccommodationId.value) return
  loading.value = true
  try {
    await router.replace({ query: { accommodationId: selectedAccommodationId.value } })
    stats.value = await fetchAccommodationStats({
      accommodationId: selectedAccommodationId.value,
      startDate: dateRange.startDate,
      endDate: dateRange.endDate
    })
    await nextTick()
    initCharts()
  } catch (e) {
    error.value = '통계 정보를 불러오는 중 오류가 발생했습니다.'
  } finally {
    loading.value = false
  }
}

// ---- 차트 초기화
function destroyCharts() {
  for (const k in chartInstances) {
    if (chartInstances[k]) {
      chartInstances[k].destroy()
      chartInstances[k] = null
    }
  }
}
function initCharts() {
  destroyCharts()
  // 매출
  if (stats.value?.monthlyRevenue && revenueChartRef.value) {
    chartInstances.revenue = new Chart(revenueChartRef.value.getContext('2d'), {
      type: 'bar',
      data: {
        labels: stats.value.monthlyRevenue.map(item => item.month),
        datasets: [{
          label: '매출 (원)',
          data: stats.value.monthlyRevenue.map(item => item.revenue)
        }]
      },
      options: { responsive: true }
    })
  }
  // 객실별 예약
  if (stats.value?.roomReservations && roomChartRef.value) {
    chartInstances.room = new Chart(roomChartRef.value.getContext('2d'), {
      type: 'pie',
      data: {
        labels: stats.value.roomReservations.map(item => item.roomName),
        datasets: [{
          data: stats.value.roomReservations.map(item => item.reservationCount)
        }]
      },
      options: { responsive: true }
    })
  }
  // 예약 상태
  if (stats.value?.reservationStatus && statusChartRef.value) {
    chartInstances.status = new Chart(statusChartRef.value.getContext('2d'), {
      type: 'doughnut',
      data: {
        labels: stats.value.reservationStatus.map(item => getStatusName(item.status)),
        datasets: [{
          data: stats.value.reservationStatus.map(item => item.count)
        }]
      },
      options: { responsive: true }
    })
  }
  // 평점 분포
  if (stats.value?.ratingDistribution && ratingChartRef.value) {
    chartInstances.rating = new Chart(ratingChartRef.value.getContext('2d'), {
      type: 'bar',
      data: {
        labels: ['1점', '2점', '3점', '4점', '5점'],
        datasets: [{
          label: '리뷰 수',
          data: [1, 2, 3, 4, 5].map(n => stats.value.ratingDistribution[n + ''] || 0)
        }]
      },
      options: { responsive: true }
    })
  }
}

// ---- 날짜 및 포맷 유틸 ----
function getDefaultStartDate() {
  const date = new Date()
  date.setMonth(date.getMonth() - 3)
  return formatDateForInput(date)
}
function getDefaultEndDate() {
  return formatDateForInput(new Date())
}
function formatDateForInput(date) {
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const da = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${da}`
}
function formatDate(date) {
  if (!date) return ''
  const d = new Date(date)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const da = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${da}`
}
function formatCurrency(amount) {
  if (typeof amount !== 'number') return '-'
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
    maximumFractionDigits: 0
  }).format(amount)
}
function getStatusName(status) {
  const statuses = {
    CONFIRMED: '확정', PENDING: '대기', CANCELLED: '취소', COMPLETED: '완료', NO_SHOW: '노쇼'
  }
  return statuses[status] || status
}
function getStatusBadgeClass(status) {
  const classes = {
    CONFIRMED: 'badge bg-success',
    PENDING: 'badge bg-warning',
    CANCELLED: 'badge bg-danger',
    COMPLETED: 'badge bg-info',
    NO_SHOW: 'badge bg-secondary'
  }
  return classes[status] || 'badge bg-secondary'
}

// ---- Watchers/이벤트 ----
watch([selectedAccommodationId, () => dateRange.startDate, () => dateRange.endDate], ([id, s, e]) => {
  if (id) loadStats()
})
onMounted(() => {
  if (!isLoggedIn || !isHost) {
    router.push({ path: '/error/access-denied', query: { message: '호스트만 접근할 수 있는 페이지입니다.' } })
    return
  }
  loadAccommodations()
})
</script>

<style scoped>
.card { box-shadow: 0 0.125rem 0.25rem rgba(0,0,0,0.075); margin-bottom: 1.5rem; }
.card-header { background-color: rgba(0,0,0,0.03); padding: 0.75rem 1.25rem; }
.display-4 { font-size: 2.5rem; font-weight: 300; line-height: 1.2; }
.table th { font-weight: 500; }
.badge { font-weight: 500; }
</style>
