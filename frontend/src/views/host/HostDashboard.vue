<template>
  <div class="container mt-4">
    <h2 class="mb-4">호스트 대시보드</h2>

    <!-- 호스트 정보 -->
    <div v-if="host" class="row mb-4">
      <div class="col-md-12">
        <div class="card dashboard-card">
          <div class="card-body">
            <h3 class="card-title">안녕하세요, {{ host.businessName }}님!</h3>
            <p class="card-text">호스트 대시보드에서 숙소 및 예약 현황을 확인하세요.</p>
            <p class="card-text">
              <strong>사업자명:</strong> {{ host.businessName }}<br>
              <strong>사업자 등록번호:</strong> {{ host.businessRegNo }}<br>
              <strong>호스트 상태:</strong> {{ host.hostStatus }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 통계 요약 -->
    <div class="row">
      <div class="col-md-3">
        <div class="stat-card bg-light-blue dashboard-card">
          <div class="stat-number">{{ accommodationCount }}</div>
          <div class="stat-label">등록된 숙소</div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stat-card bg-light-green dashboard-card">
          <div class="stat-number">{{ totalReservations }}</div>
          <div class="stat-label">총 예약 수</div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stat-card bg-light-yellow dashboard-card">
          <div class="stat-number">{{ confirmedReservations + completedReservations }}</div>
          <div class="stat-label">확정/완료된 예약</div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="stat-card bg-light-red dashboard-card">
          <div class="stat-number">{{ formatCurrency(totalRevenue) }}</div>
          <div class="stat-label">총 수익</div>
        </div>
      </div>
    </div>

    <!-- 차트 -->
    <div class="row mt-4">
      <div class="col-md-6">
        <div class="card dashboard-card">
          <div class="card-body">
            <h5 class="card-title">월별 예약 현황</h5>
            <div class="chart-container">
              <canvas ref="reservationsChart"></canvas>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card dashboard-card">
          <div class="card-body">
            <h5 class="card-title">월별 수익 현황</h5>
            <div class="chart-container">
              <canvas ref="revenueChart"></canvas>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 예약 상태 분포 -->
    <div class="row mt-4">
      <div class="col-md-6">
        <div class="card dashboard-card">
          <div class="card-body">
            <h5 class="card-title">예약 상태 분포</h5>
            <div class="chart-container">
              <canvas ref="reservationStatusChart"></canvas>
            </div>
          </div>
        </div>
      </div>
      <div class="col-md-6">
        <div class="card dashboard-card">
          <div class="card-body">
            <h5 class="card-title">숙소별 예약 현황</h5>
            <div class="chart-container">
              <canvas ref="accommodationReservationsChart"></canvas>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 숙소 목록 -->
    <div class="row mt-4">
      <div class="col-md-12">
        <div class="card dashboard-card">
          <div class="card-body">
            <h5 class="card-title">내 숙소 목록</h5>
            <div class="table-responsive">
              <table class="table table-striped">
                <thead>
                <tr>
                  <th>숙소명</th>
                  <th>주소</th>
                  <th>상태</th>
                  <th>예약 수</th>
                  <th>상세 통계</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="accommodation in accommodations" :key="accommodation.accommodationId">
                  <td>{{ accommodation.title }}</td>
                  <td>{{ accommodation.address }}</td>
                  <td>
                      <span :class="getStatusBadgeClass(accommodation.status)">
                        {{ getStatusText(accommodation.status) }}
                      </span>
                  </td>
                  <td>
                    {{ getReservationCount(accommodation.accommodationId) }}
                  </td>
                  <td>
                    <router-link
                        :to="`/host/dashboard/accommodation?accommodationId=${accommodation.accommodationId}`"
                        class="btn btn-sm btn-primary"
                    >상세 통계</router-link>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import Chart from 'chart.js/auto'

const host = ref(null)

const accommodationCount = ref(0)
const totalReservations = ref(0)
const confirmedReservations = ref(0)
const pendingReservations = ref(0)
const cancelledReservations = ref(0)
const completedReservations = ref(0)
const totalRevenue = ref(0)

const monthlyReservations = ref({})
const monthlyRevenue = ref({})
const accommodations = ref([])
const reservations = ref([])

// 차트 레퍼런스
const reservationsChart = ref(null)
const revenueChart = ref(null)
const reservationStatusChart = ref(null)
const accommodationReservationsChart = ref(null)

// 차트 인스턴스 저장
let charts = {
  reservationsChart: null,
  revenueChart: null,
  reservationStatusChart: null,
  accommodationReservationsChart: null
}

// 공통 함수: 금액 포맷팅
const formatCurrency = (amount) =>
    new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      maximumFractionDigits: 0
    }).format(amount ?? 0)

// 숙소별 예약 수 계산
const getReservationCount = (accommodationId) =>
    reservations.value.filter(r => r.accommodationId === accommodationId).length

// 숙소 상태에 따른 배지 클래스
const getStatusBadgeClass = (status) => {
  switch (status) {
    case 'ACTIVE': return 'badge bg-success'
    case 'INACTIVE': return 'badge bg-secondary'
    case 'PENDING_REVIEW': return 'badge bg-warning'
    default: return 'badge bg-secondary'
  }
}
const getStatusText = (status) => {
  switch (status) {
    case 'ACTIVE': return '활성'
    case 'INACTIVE': return '비활성'
    case 'PENDING_REVIEW': return '검토중'
    default: return status
  }
}

// 차트 초기화
const initCharts = async () => {
  // 기존 차트 제거
  Object.values(charts).forEach(chart => {
    if (chart) chart.destroy()
  })

  // 월별 예약 차트
  charts.reservationsChart = new Chart(reservationsChart.value, {
    type: 'line',
    data: {
      labels: Object.keys(monthlyReservations.value),
      datasets: [{
        label: '월별 예약 수',
        data: Object.values(monthlyReservations.value),
        borderColor: 'rgba(75, 192, 192, 1)',
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        tension: 0.1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: { beginAtZero: true, ticks: { precision: 0 } }
      }
    }
  })

  // 월별 수익 차트
  charts.revenueChart = new Chart(revenueChart.value, {
    type: 'bar',
    data: {
      labels: Object.keys(monthlyRevenue.value),
      datasets: [{
        label: '월별 수익 (원)',
        data: Object.values(monthlyRevenue.value),
        backgroundColor: 'rgba(54, 162, 235, 0.5)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: { beginAtZero: true }
      }
    }
  })

  // 예약 상태 분포 차트
  charts.reservationStatusChart = new Chart(reservationStatusChart.value, {
    type: 'pie',
    data: {
      labels: ['확정', '대기중', '취소', '완료'],
      datasets: [{
        data: [
          confirmedReservations.value,
          pendingReservations.value,
          cancelledReservations.value,
          completedReservations.value
        ],
        backgroundColor: [
          'rgba(54, 162, 235, 0.5)',
          'rgba(255, 206, 86, 0.5)',
          'rgba(255, 99, 132, 0.5)',
          'rgba(75, 192, 192, 0.5)'
        ],
        borderColor: [
          'rgba(54, 162, 235, 1)',
          'rgba(255, 206, 86, 1)',
          'rgba(255, 99, 132, 1)',
          'rgba(75, 192, 192, 1)'
        ],
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false
    }
  })

  // 숙소별 예약 현황 차트
  charts.accommodationReservationsChart = new Chart(accommodationReservationsChart.value, {
    type: 'bar',
    data: {
      labels: accommodations.value.map(acc => acc.title),
      datasets: [{
        label: '예약 수',
        data: accommodations.value.map(acc => getReservationCount(acc.accommodationId)),
        backgroundColor: 'rgba(153, 102, 255, 0.5)',
        borderColor: 'rgba(153, 102, 255, 1)',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: { beginAtZero: true, ticks: { precision: 0 } }
      }
    }
  })
}

// 대시보드 데이터 로드
const loadDashboardData = async () => {
  try {
    const res = await fetch('/api/host/dashboard')
    if (!res.ok) throw new Error('대시보드 데이터를 불러오는데 실패했습니다.')
    const data = await res.json()
    host.value = data.host
    accommodationCount.value = data.accommodationCount
    totalReservations.value = data.totalReservations
    confirmedReservations.value = data.confirmedReservations
    pendingReservations.value = data.pendingReservations
    cancelledReservations.value = data.cancelledReservations
    completedReservations.value = data.completedReservations
    totalRevenue.value = data.totalRevenue
    monthlyReservations.value = data.monthlyReservations
    monthlyRevenue.value = data.monthlyRevenue
    accommodations.value = data.accommodations
    reservations.value = data.reservations

    // nextTick 후 차트 초기화 (DOM 반영 보장)
    await nextTick()
    await initCharts()
  } catch (e) {
    // 실제 서비스에서는 Toast나 에러 바인딩 필요
    alert('대시보드 데이터 로드 중 오류: ' + e.message)
    console.error(e)
  }
}

// 최초 진입 시 데이터 로드
onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped>
.dashboard-card {
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  transition: transform 0.3s;
}
.dashboard-card:hover {
  transform: translateY(-5px);
}
.stat-card {
  text-align: center;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
}
.stat-number {
  font-size: 2.5rem;
  font-weight: bold;
}
.stat-label {
  font-size: 1rem;
  color: #6c757d;
}
.bg-light-blue {
  background-color: #e3f2fd;
}
.bg-light-green {
  background-color: #e8f5e9;
}
.bg-light-yellow {
  background-color: #fffde7;
}
.bg-light-red {
  background-color: #ffebee;
}
.chart-container {
  position: relative;
  height: 300px;
  margin-bottom: 30px;
}
</style>
