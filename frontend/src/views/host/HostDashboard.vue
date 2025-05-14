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
                      <span 
                        :class="getStatusBadgeClass(accommodation.status)"
                      >
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
                      >
                        상세 통계
                      </router-link>
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

<script>
import Chart from 'chart.js/auto';

export default {
  name: 'HostDashboard',
  data() {
    return {
      // 호스트 정보
      host: null,
      
      // 통계 데이터
      accommodationCount: 0,
      totalReservations: 0,
      confirmedReservations: 0,
      pendingReservations: 0,
      cancelledReservations: 0,
      completedReservations: 0,
      totalRevenue: 0,
      
      // 월별 데이터
      monthlyReservations: {},
      monthlyRevenue: {},
      
      // 숙소 및 예약 데이터
      accommodations: [],
      reservations: [],
      
      // 차트 인스턴스
      charts: {
        reservationsChart: null,
        revenueChart: null,
        reservationStatusChart: null,
        accommodationReservationsChart: null
      }
    };
  },
  mounted() {
    // 데이터 로드
    this.loadDashboardData();
  },
  methods: {
    // 대시보드 데이터 로드
    async loadDashboardData() {
      try {
        // API 호출
        const response = await fetch('/api/host/dashboard');
        if (!response.ok) {
          throw new Error('대시보드 데이터를 불러오는데 실패했습니다.');
        }
        
        const data = await response.json();
        
        // 데이터 설정
        this.host = data.host;
        this.accommodationCount = data.accommodationCount;
        this.totalReservations = data.totalReservations;
        this.confirmedReservations = data.confirmedReservations;
        this.pendingReservations = data.pendingReservations;
        this.cancelledReservations = data.cancelledReservations;
        this.completedReservations = data.completedReservations;
        this.totalRevenue = data.totalRevenue;
        this.monthlyReservations = data.monthlyReservations;
        this.monthlyRevenue = data.monthlyRevenue;
        this.accommodations = data.accommodations;
        this.reservations = data.reservations;
        
        // 차트 초기화
        this.initCharts();
      } catch (error) {
        console.error('대시보드 데이터 로드 중 오류가 발생했습니다:', error);
      }
    },
    
    // 차트 초기화
    initCharts() {
      // 기존 차트 제거
      Object.values(this.charts).forEach(chart => {
        if (chart) {
          chart.destroy();
        }
      });
      
      // 월별 예약 차트
      this.charts.reservationsChart = new Chart(this.$refs.reservationsChart, {
        type: 'line',
        data: {
          labels: Object.keys(this.monthlyReservations),
          datasets: [{
            label: '월별 예약 수',
            data: Object.values(this.monthlyReservations),
            borderColor: 'rgba(75, 192, 192, 1)',
            backgroundColor: 'rgba(75, 192, 192, 0.2)',
            tension: 0.1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                precision: 0
              }
            }
          }
        }
      });
      
      // 월별 수익 차트
      this.charts.revenueChart = new Chart(this.$refs.revenueChart, {
        type: 'bar',
        data: {
          labels: Object.keys(this.monthlyRevenue),
          datasets: [{
            label: '월별 수익 (원)',
            data: Object.values(this.monthlyRevenue),
            backgroundColor: 'rgba(54, 162, 235, 0.5)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });
      
      // 예약 상태 분포 차트
      this.charts.reservationStatusChart = new Chart(this.$refs.reservationStatusChart, {
        type: 'pie',
        data: {
          labels: ['확정', '대기중', '취소', '완료'],
          datasets: [{
            data: [
              this.confirmedReservations,
              this.pendingReservations,
              this.cancelledReservations,
              this.completedReservations
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
      });
      
      // 숙소별 예약 현황 차트
      this.charts.accommodationReservationsChart = new Chart(this.$refs.accommodationReservationsChart, {
        type: 'bar',
        data: {
          labels: this.accommodations.map(acc => acc.title),
          datasets: [{
            label: '예약 수',
            data: this.accommodations.map(acc => this.getReservationCount(acc.accommodationId)),
            backgroundColor: 'rgba(153, 102, 255, 0.5)',
            borderColor: 'rgba(153, 102, 255, 1)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                precision: 0
              }
            }
          }
        }
      });
    },
    
    // 숙소별 예약 수 계산
    getReservationCount(accommodationId) {
      return this.reservations.filter(reservation => reservation.accommodationId === accommodationId).length;
    },
    
    // 숙소 상태에 따른 배지 클래스 반환
    getStatusBadgeClass(status) {
      switch (status) {
        case 'ACTIVE': return 'badge bg-success';
        case 'INACTIVE': return 'badge bg-secondary';
        case 'PENDING_REVIEW': return 'badge bg-warning';
        default: return 'badge bg-secondary';
      }
    },
    
    // 숙소 상태 텍스트 반환
    getStatusText(status) {
      switch (status) {
        case 'ACTIVE': return '활성';
        case 'INACTIVE': return '비활성';
        case 'PENDING_REVIEW': return '검토중';
        default: return status;
      }
    },
    
    // 금액 포맷팅
    formatCurrency(amount) {
      return new Intl.NumberFormat('ko-KR', {
        style: 'currency',
        currency: 'KRW',
        maximumFractionDigits: 0
      }).format(amount);
    }
  }
};
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