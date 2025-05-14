<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">숙소 통계</h2>
      
      <!-- 알림 메시지 -->
      <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
        {{ message }}
        <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
      </div>
      
      <!-- 에러 메시지 -->
      <div v-if="error" class="alert alert-danger alert-dismissible fade show" role="alert">
        {{ error }}
        <button type="button" class="btn-close" @click="error = ''" aria-label="Close"></button>
      </div>
      
      <!-- 숙소 선택 -->
      <div class="card mb-4">
        <div class="card-body">
          <div class="row g-3">
            <div class="col-md-6">
              <label for="accommodationSelect" class="form-label">숙소 선택</label>
              <select 
                class="form-select" 
                id="accommodationSelect" 
                v-model="selectedAccommodationId"
                @change="loadStats"
              >
                <option value="">숙소를 선택하세요</option>
                <option 
                  v-for="accommodation in accommodations" 
                  :key="accommodation.accommodationId" 
                  :value="accommodation.accommodationId"
                >
                  {{ accommodation.title }}
                </option>
              </select>
            </div>
            <div class="col-md-3">
              <label for="startDate" class="form-label">시작일</label>
              <input 
                type="date" 
                class="form-control" 
                id="startDate" 
                v-model="dateRange.startDate"
                @change="loadStats"
              >
            </div>
            <div class="col-md-3">
              <label for="endDate" class="form-label">종료일</label>
              <input 
                type="date" 
                class="form-control" 
                id="endDate" 
                v-model="dateRange.endDate"
                @change="loadStats"
              >
            </div>
          </div>
        </div>
      </div>
      
      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">통계 정보를 불러오는 중입니다...</p>
      </div>
      
      <!-- 숙소가 선택되지 않은 경우 -->
      <div v-else-if="!selectedAccommodationId" class="alert alert-info">
        통계를 확인할 숙소를 선택해주세요.
      </div>
      
      <!-- 통계 정보 -->
      <div v-else-if="!loading && stats">
        <div class="row">
          <!-- 요약 통계 카드 -->
          <div class="col-md-3 mb-4">
            <div class="card h-100 border-primary">
              <div class="card-body text-center">
                <h5 class="card-title">총 예약 수</h5>
                <p class="display-4">{{ stats.totalReservations }}</p>
              </div>
            </div>
          </div>
          
          <div class="col-md-3 mb-4">
            <div class="card h-100 border-success">
              <div class="card-body text-center">
                <h5 class="card-title">총 매출</h5>
                <p class="display-4">{{ formatCurrency(stats.totalRevenue) }}</p>
              </div>
            </div>
          </div>
          
          <div class="col-md-3 mb-4">
            <div class="card h-100 border-info">
              <div class="card-body text-center">
                <h5 class="card-title">평균 평점</h5>
                <p class="display-4">
                  {{ stats.averageRating ? stats.averageRating.toFixed(1) : '0.0' }}
                  <small class="text-muted">/5</small>
                </p>
              </div>
            </div>
          </div>
          
          <div class="col-md-3 mb-4">
            <div class="card h-100 border-warning">
              <div class="card-body text-center">
                <h5 class="card-title">객실 점유율</h5>
                <p class="display-4">{{ stats.occupancyRate ? stats.occupancyRate.toFixed(1) : '0.0' }}%</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 차트 -->
        <div class="row">
          <!-- 월별 매출 차트 -->
          <div class="col-md-6 mb-4">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">월별 매출</h5>
              </div>
              <div class="card-body">
                <canvas id="revenueChart" height="300"></canvas>
              </div>
            </div>
          </div>
          
          <!-- 객실별 예약 차트 -->
          <div class="col-md-6 mb-4">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">객실별 예약</h5>
              </div>
              <div class="card-body">
                <canvas id="roomChart" height="300"></canvas>
              </div>
            </div>
          </div>
          
          <!-- 예약 상태 차트 -->
          <div class="col-md-6 mb-4">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">예약 상태</h5>
              </div>
              <div class="card-body">
                <canvas id="statusChart" height="300"></canvas>
              </div>
            </div>
          </div>
          
          <!-- 평점 분포 차트 -->
          <div class="col-md-6 mb-4">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">평점 분포</h5>
              </div>
              <div class="card-body">
                <canvas id="ratingChart" height="300"></canvas>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 최근 예약 목록 -->
        <div class="card mb-4">
          <div class="card-header">
            <h5 class="card-title mb-0">최근 예약</h5>
          </div>
          <div class="card-body">
            <div v-if="stats.recentReservations && stats.recentReservations.length > 0" class="table-responsive">
              <table class="table table-striped">
                <thead>
                  <tr>
                    <th>예약 ID</th>
                    <th>객실</th>
                    <th>게스트</th>
                    <th>체크인</th>
                    <th>체크아웃</th>
                    <th>금액</th>
                    <th>상태</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="reservation in stats.recentReservations" :key="reservation.reservationId">
                    <td>{{ reservation.reservationId }}</td>
                    <td>{{ reservation.roomName }}</td>
                    <td>{{ reservation.guestName }}</td>
                    <td>{{ formatDate(reservation.checkInDate) }}</td>
                    <td>{{ formatDate(reservation.checkOutDate) }}</td>
                    <td>{{ formatCurrency(reservation.totalPrice) }}</td>
                    <td>
                      <span :class="getStatusBadgeClass(reservation.status)">
                        {{ getStatusName(reservation.status) }}
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
          <div class="card-header">
            <h5 class="card-title mb-0">최근 리뷰</h5>
          </div>
          <div class="card-body">
            <div v-if="stats.recentReviews && stats.recentReviews.length > 0">
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

<script>
/**
 * 숙소 통계 컴포넌트
 * 
 * 이 컴포넌트는 호스트가 자신의 숙소에 대한 통계 정보를 볼 수 있는 페이지입니다.
 * 예약 수, 매출, 평점, 객실 점유율 등의 통계 정보를 차트와 함께 제공합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';
import Chart from 'chart.js/auto';

export default {
  name: 'AccommodationStats',
  components: {
    Layout
  },
  data() {
    return {
      loading: false,
      message: '',
      error: '',
      accommodations: [],
      selectedAccommodationId: '',
      dateRange: {
        startDate: this.getDefaultStartDate(),
        endDate: this.getDefaultEndDate()
      },
      stats: null,
      charts: {
        revenueChart: null,
        roomChart: null,
        statusChart: null,
        ratingChart: null
      }
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.user.isLoggedIn,
      isHost: state => state.user.user?.role === 'HOST'
    })
  },
  created() {
    // 호스트 권한 확인
    if (!this.isLoggedIn || !this.isHost) {
      this.$router.push({
        path: '/error/access-denied',
        query: { message: '호스트만 접근할 수 있는 페이지입니다.' }
      });
      return;
    }
    
    // URL 쿼리 파라미터에서 숙소 ID 가져오기
    const query = this.$route.query;
    if (query.accommodationId) {
      this.selectedAccommodationId = query.accommodationId;
    }
    
    // 호스트의 숙소 목록 로드
    this.loadAccommodations();
  },
  methods: {
    ...mapActions('host', ['fetchHostAccommodations', 'fetchAccommodationStats']),
    
    /**
     * 호스트의 숙소 목록 로드
     */
    async loadAccommodations() {
      try {
        this.accommodations = await this.fetchHostAccommodations();
        
        // 선택된 숙소가 있으면 통계 로드
        if (this.selectedAccommodationId) {
          this.loadStats();
        }
      } catch (error) {
        console.error('숙소 목록을 불러오는 중 오류가 발생했습니다:', error);
        this.error = '숙소 목록을 불러오는 중 오류가 발생했습니다.';
      }
    },
    
    /**
     * 선택된 숙소의 통계 정보 로드
     */
    async loadStats() {
      if (!this.selectedAccommodationId) return;
      
      this.loading = true;
      
      try {
        // URL 쿼리 파라미터 업데이트
        this.$router.replace({
          query: { accommodationId: this.selectedAccommodationId }
        });
        
        // 통계 정보 가져오기
        this.stats = await this.fetchAccommodationStats({
          accommodationId: this.selectedAccommodationId,
          startDate: this.dateRange.startDate,
          endDate: this.dateRange.endDate
        });
        
        // 차트 초기화
        this.$nextTick(() => {
          this.initCharts();
        });
      } catch (error) {
        console.error('통계 정보를 불러오는 중 오류가 발생했습니다:', error);
        this.error = '통계 정보를 불러오는 중 오류가 발생했습니다.';
      } finally {
        this.loading = false;
      }
    },
    
    /**
     * 차트 초기화
     */
    initCharts() {
      // 기존 차트 제거
      Object.keys(this.charts).forEach(key => {
        if (this.charts[key]) {
          this.charts[key].destroy();
          this.charts[key] = null;
        }
      });
      
      // 월별 매출 차트
      if (this.stats.monthlyRevenue) {
        const revenueCtx = document.getElementById('revenueChart').getContext('2d');
        this.charts.revenueChart = new Chart(revenueCtx, {
          type: 'bar',
          data: {
            labels: this.stats.monthlyRevenue.map(item => item.month),
            datasets: [{
              label: '매출 (원)',
              data: this.stats.monthlyRevenue.map(item => item.revenue),
              backgroundColor: 'rgba(54, 162, 235, 0.5)',
              borderColor: 'rgba(54, 162, 235, 1)',
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  callback: value => this.formatCurrency(value)
                }
              }
            }
          }
        });
      }
      
      // 객실별 예약 차트
      if (this.stats.roomReservations) {
        const roomCtx = document.getElementById('roomChart').getContext('2d');
        this.charts.roomChart = new Chart(roomCtx, {
          type: 'pie',
          data: {
            labels: this.stats.roomReservations.map(item => item.roomName),
            datasets: [{
              data: this.stats.roomReservations.map(item => item.reservationCount),
              backgroundColor: [
                'rgba(255, 99, 132, 0.5)',
                'rgba(54, 162, 235, 0.5)',
                'rgba(255, 206, 86, 0.5)',
                'rgba(75, 192, 192, 0.5)',
                'rgba(153, 102, 255, 0.5)',
                'rgba(255, 159, 64, 0.5)'
              ],
              borderColor: [
                'rgba(255, 99, 132, 1)',
                'rgba(54, 162, 235, 1)',
                'rgba(255, 206, 86, 1)',
                'rgba(75, 192, 192, 1)',
                'rgba(153, 102, 255, 1)',
                'rgba(255, 159, 64, 1)'
              ],
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            plugins: {
              legend: {
                position: 'right'
              }
            }
          }
        });
      }
      
      // 예약 상태 차트
      if (this.stats.reservationStatus) {
        const statusCtx = document.getElementById('statusChart').getContext('2d');
        this.charts.statusChart = new Chart(statusCtx, {
          type: 'doughnut',
          data: {
            labels: this.stats.reservationStatus.map(item => this.getStatusName(item.status)),
            datasets: [{
              data: this.stats.reservationStatus.map(item => item.count),
              backgroundColor: [
                'rgba(40, 167, 69, 0.5)',  // 확정
                'rgba(255, 193, 7, 0.5)',  // 대기
                'rgba(220, 53, 69, 0.5)',  // 취소
                'rgba(108, 117, 125, 0.5)' // 기타
              ],
              borderColor: [
                'rgba(40, 167, 69, 1)',
                'rgba(255, 193, 7, 1)',
                'rgba(220, 53, 69, 1)',
                'rgba(108, 117, 125, 1)'
              ],
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            plugins: {
              legend: {
                position: 'right'
              }
            }
          }
        });
      }
      
      // 평점 분포 차트
      if (this.stats.ratingDistribution) {
        const ratingCtx = document.getElementById('ratingChart').getContext('2d');
        this.charts.ratingChart = new Chart(ratingCtx, {
          type: 'bar',
          data: {
            labels: ['1점', '2점', '3점', '4점', '5점'],
            datasets: [{
              label: '리뷰 수',
              data: [
                this.stats.ratingDistribution['1'] || 0,
                this.stats.ratingDistribution['2'] || 0,
                this.stats.ratingDistribution['3'] || 0,
                this.stats.ratingDistribution['4'] || 0,
                this.stats.ratingDistribution['5'] || 0
              ],
              backgroundColor: 'rgba(255, 193, 7, 0.5)',
              borderColor: 'rgba(255, 193, 7, 1)',
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            scales: {
              y: {
                beginAtZero: true,
                ticks: {
                  stepSize: 1
                }
              }
            }
          }
        });
      }
    },
    
    /**
     * 기본 시작일 반환 (3개월 전)
     */
    getDefaultStartDate() {
      const date = new Date();
      date.setMonth(date.getMonth() - 3);
      return this.formatDateForInput(date);
    },
    
    /**
     * 기본 종료일 반환 (오늘)
     */
    getDefaultEndDate() {
      return this.formatDateForInput(new Date());
    },
    
    /**
     * 날짜를 input[type="date"] 형식으로 포맷팅
     * @param {Date} date - 포맷팅할 날짜
     * @returns {string} YYYY-MM-DD 형식의 문자열
     */
    formatDateForInput(date) {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    },
    
    /**
     * 날짜 포맷팅 (YYYY-MM-DD)
     * @param {string|Date} date - 포맷팅할 날짜
     * @returns {string} 포맷팅된 날짜 문자열
     */
    formatDate(date) {
      if (!date) return '';
      
      const d = new Date(date);
      const year = d.getFullYear();
      const month = String(d.getMonth() + 1).padStart(2, '0');
      const day = String(d.getDate()).padStart(2, '0');
      
      return `${year}-${month}-${day}`;
    },
    
    /**
     * 금액 포맷팅 (₩1,000,000 형식)
     * @param {number} amount - 포맷팅할 금액
     * @returns {string} 포맷팅된 금액 문자열
     */
    formatCurrency(amount) {
      return new Intl.NumberFormat('ko-KR', { 
        style: 'currency', 
        currency: 'KRW',
        maximumFractionDigits: 0 
      }).format(amount);
    },
    
    /**
     * 예약 상태 이름 반환
     * @param {string} status - 상태 코드
     * @returns {string} 상태 이름
     */
    getStatusName(status) {
      const statuses = {
        'CONFIRMED': '확정',
        'PENDING': '대기',
        'CANCELLED': '취소',
        'COMPLETED': '완료',
        'NO_SHOW': '노쇼'
      };
      
      return statuses[status] || status;
    },
    
    /**
     * 예약 상태 배지 클래스 반환
     * @param {string} status - 상태 코드
     * @returns {string} 배지 클래스
     */
    getStatusBadgeClass(status) {
      const classes = {
        'CONFIRMED': 'badge bg-success',
        'PENDING': 'badge bg-warning',
        'CANCELLED': 'badge bg-danger',
        'COMPLETED': 'badge bg-info',
        'NO_SHOW': 'badge bg-secondary'
      };
      
      return classes[status] || 'badge bg-secondary';
    }
  }
};
</script>

<style scoped>
.card {
  box-shadow: 0 0.125rem 0.25rem rgba(0, 0, 0, 0.075);
  margin-bottom: 1.5rem;
}

.card-header {
  background-color: rgba(0, 0, 0, 0.03);
  padding: 0.75rem 1.25rem;
}

.display-4 {
  font-size: 2.5rem;
  font-weight: 300;
  line-height: 1.2;
}

.table th {
  font-weight: 500;
}

.badge {
  font-weight: 500;
}
</style>