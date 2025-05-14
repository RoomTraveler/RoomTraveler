<template>
  <Layout>
    <div class="container mt-4">
      <h2 class="mb-4">관리자 대시보드</h2>

      <!-- 로딩 표시 -->
      <div v-if="loading" class="text-center py-5">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">로딩 중...</span>
        </div>
        <p class="mt-2">대시보드 데이터를 불러오는 중입니다...</p>
      </div>

      <div v-else>
        <!-- 주요 지표 카드 -->
        <div class="row mb-4">
          <div class="col-md-3">
            <div class="card bg-primary text-white h-100">
              <div class="card-body">
                <h5 class="card-title">총 사용자</h5>
                <h2 class="display-4">{{ stats.totalUsers }}</h2>
                <p class="card-text">
                  <span class="badge" :class="stats.userGrowth >= 0 ? 'bg-success' : 'bg-danger'">
                    <i :class="stats.userGrowth >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down'"></i>
                    {{ Math.abs(stats.userGrowth) }}%
                  </span>
                  지난 달 대비
                </p>
              </div>
            </div>
          </div>

          <div class="col-md-3">
            <div class="card bg-success text-white h-100">
              <div class="card-body">
                <h5 class="card-title">총 숙소</h5>
                <h2 class="display-4">{{ stats.totalAccommodations }}</h2>
                <p class="card-text">
                  <span class="badge" :class="stats.accommodationGrowth >= 0 ? 'bg-info' : 'bg-danger'">
                    <i :class="stats.accommodationGrowth >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down'"></i>
                    {{ Math.abs(stats.accommodationGrowth) }}%
                  </span>
                  지난 달 대비
                </p>
              </div>
            </div>
          </div>

          <div class="col-md-3">
            <div class="card bg-info text-white h-100">
              <div class="card-body">
                <h5 class="card-title">총 예약</h5>
                <h2 class="display-4">{{ stats.totalReservations }}</h2>
                <p class="card-text">
                  <span class="badge" :class="stats.reservationGrowth >= 0 ? 'bg-success' : 'bg-danger'">
                    <i :class="stats.reservationGrowth >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down'"></i>
                    {{ Math.abs(stats.reservationGrowth) }}%
                  </span>
                  지난 달 대비
                </p>
              </div>
            </div>
          </div>

          <div class="col-md-3">
            <div class="card bg-warning text-dark h-100">
              <div class="card-body">
                <h5 class="card-title">총 매출</h5>
                <h2 class="display-4">{{ formatCurrency(stats.totalRevenue) }}</h2>
                <p class="card-text">
                  <span class="badge" :class="stats.revenueGrowth >= 0 ? 'bg-success' : 'bg-danger'">
                    <i :class="stats.revenueGrowth >= 0 ? 'bi bi-arrow-up' : 'bi bi-arrow-down'"></i>
                    {{ Math.abs(stats.revenueGrowth) }}%
                  </span>
                  지난 달 대비
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- 그래프 및 차트 -->
        <div class="row mb-4">
          <div class="col-md-8">
            <div class="card">
              <div class="card-header">
                <h5 class="mb-0">월별 예약 및 매출 추이</h5>
              </div>
              <div class="card-body">
                <canvas id="monthlyChart" height="300"></canvas>
              </div>
            </div>
          </div>

          <div class="col-md-4">
            <div class="card">
              <div class="card-header">
                <h5 class="mb-0">숙소 유형 분포</h5>
              </div>
              <div class="card-body">
                <canvas id="accommodationTypeChart" height="300"></canvas>
              </div>
            </div>
          </div>
        </div>

        <!-- 최근 활동 및 알림 -->
        <div class="row">
          <div class="col-md-6">
            <div class="card">
              <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0">최근 등록된 숙소</h5>
                <router-link to="/admin/accommodations" class="btn btn-sm btn-outline-primary">모두 보기</router-link>
              </div>
              <div class="card-body p-0">
                <div class="table-responsive">
                  <table class="table table-hover mb-0">
                    <thead>
                      <tr>
                        <th>숙소명</th>
                        <th>호스트</th>
                        <th>상태</th>
                        <th>등록일</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="accommodation in recentAccommodations" :key="accommodation.accommodationId">
                        <td>
                          <router-link :to="`/accommodation/detail/${accommodation.accommodationId}`">
                            {{ accommodation.title }}
                          </router-link>
                        </td>
                        <td>{{ accommodation.hostName }}</td>
                        <td>
                          <span :class="getStatusBadgeClass(accommodation.status)">
                            {{ getStatusName(accommodation.status) }}
                          </span>
                        </td>
                        <td>{{ formatDate(accommodation.createdAt) }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>

          <div class="col-md-6">
            <div class="card">
              <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0">최근 가입한 사용자</h5>
                <router-link to="/admin/users" class="btn btn-sm btn-outline-primary">모두 보기</router-link>
              </div>
              <div class="card-body p-0">
                <div class="table-responsive">
                  <table class="table table-hover mb-0">
                    <thead>
                      <tr>
                        <th>이름</th>
                        <th>이메일</th>
                        <th>역할</th>
                        <th>가입일</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr v-for="user in recentUsers" :key="user.userId">
                        <td>{{ user.username }}</td>
                        <td>{{ user.email }}</td>
                        <td>
                          <span :class="getRoleBadgeClass(user.role)">
                            {{ getRoleName(user.role) }}
                          </span>
                        </td>
                        <td>{{ formatDate(user.createdAt) }}</td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 시스템 상태 -->
        <div class="row mt-4">
          <div class="col-md-12">
            <div class="card">
              <div class="card-header">
                <h5 class="mb-0">시스템 상태</h5>
              </div>
              <div class="card-body">
                <div class="row">
                  <div class="col-md-3">
                    <div class="mb-3">
                      <h6>CPU 사용량</h6>
                      <div class="progress">
                        <div 
                          class="progress-bar" 
                          role="progressbar" 
                          :style="{ width: systemStatus.cpuUsage + '%' }" 
                          :class="getProgressBarClass(systemStatus.cpuUsage)"
                          :aria-valuenow="systemStatus.cpuUsage" 
                          aria-valuemin="0" 
                          aria-valuemax="100"
                        >
                          {{ systemStatus.cpuUsage }}%
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="col-md-3">
                    <div class="mb-3">
                      <h6>메모리 사용량</h6>
                      <div class="progress">
                        <div 
                          class="progress-bar" 
                          role="progressbar" 
                          :style="{ width: systemStatus.memoryUsage + '%' }" 
                          :class="getProgressBarClass(systemStatus.memoryUsage)"
                          :aria-valuenow="systemStatus.memoryUsage" 
                          aria-valuemin="0" 
                          aria-valuemax="100"
                        >
                          {{ systemStatus.memoryUsage }}%
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="col-md-3">
                    <div class="mb-3">
                      <h6>디스크 사용량</h6>
                      <div class="progress">
                        <div 
                          class="progress-bar" 
                          role="progressbar" 
                          :style="{ width: systemStatus.diskUsage + '%' }" 
                          :class="getProgressBarClass(systemStatus.diskUsage)"
                          :aria-valuenow="systemStatus.diskUsage" 
                          aria-valuemin="0" 
                          aria-valuemax="100"
                        >
                          {{ systemStatus.diskUsage }}%
                        </div>
                      </div>
                    </div>
                  </div>

                  <div class="col-md-3">
                    <div class="mb-3">
                      <h6>API 요청 (분당)</h6>
                      <div class="progress">
                        <div 
                          class="progress-bar bg-info" 
                          role="progressbar" 
                          :style="{ width: (systemStatus.apiRequestsPerMinute / systemStatus.maxApiRequestsPerMinute * 100) + '%' }" 
                          :aria-valuenow="systemStatus.apiRequestsPerMinute" 
                          aria-valuemin="0" 
                          :aria-valuemax="systemStatus.maxApiRequestsPerMinute"
                        >
                          {{ systemStatus.apiRequestsPerMinute }}/{{ systemStatus.maxApiRequestsPerMinute }}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="row mt-3">
                  <div class="col-md-6">
                    <h6>서버 정보</h6>
                    <ul class="list-group">
                      <li class="list-group-item d-flex justify-content-between align-items-center">
                        <span>서버 가동 시간</span>
                        <span>{{ formatUptime(systemStatus.uptime) }}</span>
                      </li>
                      <li class="list-group-item d-flex justify-content-between align-items-center">
                        <span>Java 버전</span>
                        <span>{{ systemStatus.javaVersion }}</span>
                      </li>
                      <li class="list-group-item d-flex justify-content-between align-items-center">
                        <span>운영체제</span>
                        <span>{{ systemStatus.osName }}</span>
                      </li>
                    </ul>
                  </div>

                  <div class="col-md-6">
                    <h6>데이터베이스 정보</h6>
                    <ul class="list-group">
                      <li class="list-group-item d-flex justify-content-between align-items-center">
                        <span>데이터베이스 유형</span>
                        <span>{{ systemStatus.dbType }}</span>
                      </li>
                      <li class="list-group-item d-flex justify-content-between align-items-center">
                        <span>활성 연결</span>
                        <span>{{ systemStatus.activeConnections }}/{{ systemStatus.maxConnections }}</span>
                      </li>
                      <li class="list-group-item d-flex justify-content-between align-items-center">
                        <span>데이터베이스 크기</span>
                        <span>{{ formatSize(systemStatus.dbSize) }}</span>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </Layout>
</template>

<script>
/**
 * 관리자 대시보드 컴포넌트
 * 
 * 이 컴포넌트는 관리자를 위한 대시보드를 제공합니다.
 * 주요 지표, 그래프, 최근 활동, 시스템 상태 등을 표시합니다.
 */
import { mapState, mapActions } from 'vuex';
import Layout from '@/components/layout/Layout.vue';
import Chart from 'chart.js/auto';

export default {
  name: 'AdminDashboard',
  components: {
    Layout
  },
  data() {
    return {
      loading: true,
      stats: {
        totalUsers: 0,
        userGrowth: 0,
        totalAccommodations: 0,
        accommodationGrowth: 0,
        totalReservations: 0,
        reservationGrowth: 0,
        totalRevenue: 0,
        revenueGrowth: 0
      },
      recentAccommodations: [],
      recentUsers: [],
      systemStatus: {
        cpuUsage: 0,
        memoryUsage: 0,
        diskUsage: 0,
        apiRequestsPerMinute: 0,
        maxApiRequestsPerMinute: 1000,
        uptime: 0,
        javaVersion: '',
        osName: '',
        dbType: '',
        activeConnections: 0,
        maxConnections: 0,
        dbSize: 0
      },
      monthlyData: {
        labels: [],
        reservations: [],
        revenue: []
      },
      accommodationTypes: {
        labels: [],
        data: []
      },
      charts: {
        monthlyChart: null,
        accommodationTypeChart: null
      }
    };
  },
  computed: {
    ...mapState({
      isAdmin: state => state.user.user?.role === 'ADMIN'
    })
  },
  created() {
    // 관리자 권한 확인
    if (!this.isAdmin) {
      this.$router.push({
        path: '/error/access-denied',
        query: { message: '관리자만 접근할 수 있는 페이지입니다.' }
      });
      return;
    }

    // 대시보드 데이터 로드
    this.loadDashboardData();
  },
  mounted() {
    // 차트는 DOM이 마운트된 후에 초기화
    if (!this.loading) {
      this.initCharts();
    }
  },
  updated() {
    // 데이터가 로드된 후 차트 초기화
    if (!this.loading && !this.charts.monthlyChart) {
      this.initCharts();
    }
  },
  beforeUnmount() {
    // 차트 인스턴스 정리
    this.destroyCharts();
  },
  methods: {
    ...mapActions('admin', ['fetchDashboardData']),

    /**
     * 대시보드 데이터 로드
     */
    async loadDashboardData() {
      this.loading = true;

      try {
        // 실제 구현에서는 API 호출
        // const data = await this.fetchDashboardData();

        // 임시 데이터 (실제로는 API에서 가져와야 함)
        await this.delay(1000); // 로딩 시뮬레이션
        const data = this.generateDummyData();

        // 데이터 설정
        this.stats = data.stats;
        this.recentAccommodations = data.recentAccommodations;
        this.recentUsers = data.recentUsers;
        this.systemStatus = data.systemStatus;
        this.monthlyData = data.monthlyData;
        this.accommodationTypes = data.accommodationTypes;
      } catch (error) {
        console.error('대시보드 데이터를 불러오는 중 오류가 발생했습니다:', error);
      } finally {
        this.loading = false;
      }
    },

    /**
     * 차트 초기화
     */
    initCharts() {
      // 월별 차트
      const monthlyChartCtx = document.getElementById('monthlyChart');
      if (monthlyChartCtx) {
        this.charts.monthlyChart = new Chart(monthlyChartCtx, {
          type: 'bar',
          data: {
            labels: this.monthlyData.labels,
            datasets: [
              {
                label: '예약 수',
                data: this.monthlyData.reservations,
                backgroundColor: 'rgba(54, 162, 235, 0.5)',
                borderColor: 'rgba(54, 162, 235, 1)',
                borderWidth: 1,
                yAxisID: 'y'
              },
              {
                label: '매출 (만원)',
                data: this.monthlyData.revenue.map(val => val / 10000), // 만원 단위로 변환
                type: 'line',
                backgroundColor: 'rgba(255, 99, 132, 0.2)',
                borderColor: 'rgba(255, 99, 132, 1)',
                borderWidth: 2,
                fill: false,
                yAxisID: 'y1'
              }
            ]
          },
          options: {
            responsive: true,
            scales: {
              y: {
                type: 'linear',
                display: true,
                position: 'left',
                title: {
                  display: true,
                  text: '예약 수'
                }
              },
              y1: {
                type: 'linear',
                display: true,
                position: 'right',
                title: {
                  display: true,
                  text: '매출 (만원)'
                },
                grid: {
                  drawOnChartArea: false
                }
              }
            }
          }
        });
      }

      // 숙소 유형 차트
      const accommodationTypeChartCtx = document.getElementById('accommodationTypeChart');
      if (accommodationTypeChartCtx) {
        this.charts.accommodationTypeChart = new Chart(accommodationTypeChartCtx, {
          type: 'doughnut',
          data: {
            labels: this.accommodationTypes.labels,
            datasets: [{
              data: this.accommodationTypes.data,
              backgroundColor: [
                'rgba(255, 99, 132, 0.7)',
                'rgba(54, 162, 235, 0.7)',
                'rgba(255, 206, 86, 0.7)',
                'rgba(75, 192, 192, 0.7)',
                'rgba(153, 102, 255, 0.7)',
                'rgba(255, 159, 64, 0.7)',
                'rgba(199, 199, 199, 0.7)',
                'rgba(83, 102, 255, 0.7)',
                'rgba(40, 159, 64, 0.7)'
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
    },

    /**
     * 차트 인스턴스 정리
     */
    destroyCharts() {
      if (this.charts.monthlyChart) {
        this.charts.monthlyChart.destroy();
      }

      if (this.charts.accommodationTypeChart) {
        this.charts.accommodationTypeChart.destroy();
      }
    },

    /**
     * 임시 데이터 생성 (실제 구현에서는 API에서 가져와야 함)
     */
    generateDummyData() {
      return {
        stats: {
          totalUsers: 1254,
          userGrowth: 12.5,
          totalAccommodations: 876,
          accommodationGrowth: 8.3,
          totalReservations: 3421,
          reservationGrowth: 15.7,
          totalRevenue: 245678900,
          revenueGrowth: 18.2
        },
        recentAccommodations: [
          { accommodationId: 1, title: '서울 시티 호텔', hostName: '김호스트', status: 'APPROVED', createdAt: '2023-05-15T10:30:00' },
          { accommodationId: 2, title: '부산 해변 펜션', hostName: '이호스트', status: 'PENDING', createdAt: '2023-05-14T14:20:00' },
          { accommodationId: 3, title: '제주 풀빌라', hostName: '박호스트', status: 'APPROVED', createdAt: '2023-05-13T09:15:00' },
          { accommodationId: 4, title: '강원도 스키 리조트', hostName: '최호스트', status: 'REJECTED', createdAt: '2023-05-12T16:45:00' },
          { accommodationId: 5, title: '경주 한옥 스테이', hostName: '정호스트', status: 'APPROVED', createdAt: '2023-05-11T11:10:00' }
        ],
        recentUsers: [
          { userId: 1, username: '김사용자', email: 'user1@example.com', role: 'USER', createdAt: '2023-05-15T08:30:00' },
          { userId: 2, username: '이호스트', email: 'host1@example.com', role: 'HOST', createdAt: '2023-05-14T12:20:00' },
          { userId: 3, username: '박사용자', email: 'user2@example.com', role: 'USER', createdAt: '2023-05-13T15:45:00' },
          { userId: 4, username: '최호스트', email: 'host2@example.com', role: 'HOST', createdAt: '2023-05-12T09:10:00' },
          { userId: 5, username: '정관리자', email: 'admin@example.com', role: 'ADMIN', createdAt: '2023-05-11T14:25:00' }
        ],
        systemStatus: {
          cpuUsage: 45,
          memoryUsage: 62,
          diskUsage: 78,
          apiRequestsPerMinute: 350,
          maxApiRequestsPerMinute: 1000,
          uptime: 1234567, // 초 단위
          javaVersion: 'Java 17.0.2',
          osName: 'Linux 5.15.0-1019-aws',
          dbType: 'MySQL 8.0.28',
          activeConnections: 12,
          maxConnections: 100,
          dbSize: 1073741824 // 바이트 단위 (1GB)
        },
        monthlyData: {
          labels: ['1월', '2월', '3월', '4월', '5월', '6월'],
          reservations: [120, 150, 180, 210, 250, 300],
          revenue: [12000000, 15000000, 18000000, 21000000, 25000000, 30000000]
        },
        accommodationTypes: {
          labels: ['호텔', '모텔', '펜션', '게스트하우스', '리조트', '콘도', '한옥', '캠핑/글램핑', '기타'],
          data: [250, 180, 120, 80, 70, 60, 50, 40, 26]
        }
      };
    },

    /**
     * 지연 함수 (로딩 시뮬레이션용)
     * @param {number} ms - 지연 시간 (밀리초)
     */
    delay(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    },

    /**
     * 통화 포맷팅
     * @param {number} amount - 포맷팅할 금액
     * @returns {string} 포맷팅된 금액 문자열
     */
    formatCurrency(amount) {
      return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', maximumFractionDigits: 0 }).format(amount);
    },

    /**
     * 날짜 포맷팅
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
     * 가동 시간 포맷팅
     * @param {number} seconds - 초 단위 가동 시간
     * @returns {string} 포맷팅된 가동 시간 문자열
     */
    formatUptime(seconds) {
      const days = Math.floor(seconds / 86400);
      const hours = Math.floor((seconds % 86400) / 3600);
      const minutes = Math.floor((seconds % 3600) / 60);

      return `${days}일 ${hours}시간 ${minutes}분`;
    },

    /**
     * 파일 크기 포맷팅
     * @param {number} bytes - 바이트 단위 크기
     * @returns {string} 포맷팅된 크기 문자열
     */
    formatSize(bytes) {
      if (bytes === 0) return '0 Bytes';

      const k = 1024;
      const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));

      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    },

    /**
     * 상태 이름 반환
     * @param {string} status - 상태 코드
     * @returns {string} 상태 이름
     */
    getStatusName(status) {
      const statuses = {
        'PENDING': '승인 대기',
        'APPROVED': '승인됨',
        'REJECTED': '거부됨',
        'SUSPENDED': '중지됨'
      };

      return statuses[status] || status;
    },

    /**
     * 상태 배지 클래스 반환
     * @param {string} status - 상태 코드
     * @returns {string} 배지 클래스
     */
    getStatusBadgeClass(status) {
      const classes = {
        'PENDING': 'badge bg-warning',
        'APPROVED': 'badge bg-success',
        'REJECTED': 'badge bg-danger',
        'SUSPENDED': 'badge bg-secondary'
      };

      return classes[status] || 'badge bg-secondary';
    },

    /**
     * 역할 이름 반환
     * @param {string} role - 역할 코드
     * @returns {string} 역할 이름
     */
    getRoleName(role) {
      const roles = {
        'USER': '사용자',
        'HOST': '호스트',
        'ADMIN': '관리자'
      };

      return roles[role] || role;
    },

    /**
     * 역할 배지 클래스 반환
     * @param {string} role - 역할 코드
     * @returns {string} 배지 클래스
     */
    getRoleBadgeClass(role) {
      const classes = {
        'USER': 'badge bg-primary',
        'HOST': 'badge bg-success',
        'ADMIN': 'badge bg-danger'
      };

      return classes[role] || 'badge bg-secondary';
    },

    /**
     * 프로그레스 바 클래스 반환
     * @param {number} value - 값
     * @returns {string} 프로그레스 바 클래스
     */
    getProgressBarClass(value) {
      if (value < 50) {
        return 'bg-success';
      } else if (value < 80) {
        return 'bg-warning';
      } else {
        return 'bg-danger';
      }
    }
  }
};
</script>

<style scoped>
.card {
  margin-bottom: 20px;
}

.card-header {
  background-color: #f8f9fa;
}

.display-4 {
  font-size: 2.5rem;
}

.progress {
  height: 20px;
}

.table th {
  background-color: #f8f9fa;
}

.table td {
  vertical-align: middle;
}

.list-group-item {
  padding: 0.5rem 1rem;
}
</style>
