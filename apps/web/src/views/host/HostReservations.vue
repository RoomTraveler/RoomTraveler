<template>
  <div class="container mt-5 mb-5">
    <h1 class="mb-4">
      <i class="bi bi-calendar-check"></i> 호스트 예약 관리
    </h1>
    
    <!-- 알림 메시지 표시 -->
    <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
      {{ message }}
      <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
    </div>
    
    <!-- 필터링 옵션 -->
    <div class="card mb-4">
      <div class="card-body">
        <h5 class="card-title">예약 필터링</h5>
        <div class="row g-3">
          <div class="col-md-3">
            <label for="status" class="form-label">예약 상태</label>
            <select class="form-select" id="status" v-model="filters.status">
              <option value="">모든 상태</option>
              <option value="PENDING">대기 중</option>
              <option value="CONFIRMED">확정됨</option>
              <option value="CANCELLED">취소됨</option>
              <option value="COMPLETED">완료됨</option>
            </select>
          </div>
          <div class="col-md-3">
            <label for="accommodationId" class="form-label">숙소 선택</label>
            <select class="form-select" id="accommodationId" v-model="filters.accommodationId">
              <option value="">모든 숙소</option>
              <option 
                v-for="accommodation in accommodations" 
                :key="accommodation.accommodationId" 
                :value="accommodation.accommodationId"
              >
                {{ accommodation.name }}
              </option>
            </select>
          </div>
          <div class="col-md-3 d-flex align-items-end">
            <button type="button" class="btn btn-primary" @click="applyFilters">필터 적용</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 예약 목록 -->
    <div v-if="filteredReservations.length === 0" class="alert alert-info">
      <i class="bi bi-info-circle"></i> 예약 내역이 없습니다.
    </div>
    
    <div v-else class="row">
      <div v-for="reservation in filteredReservations" :key="reservation.reservationId" class="col-md-6">
        <div class="card reservation-card">
          <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0">예약 #{{ reservation.reservationId }}</h5>
            <span :class="getStatusBadgeClass(reservation.status)">
              {{ getStatusText(reservation.status) }}
            </span>
          </div>
          <div class="card-body">
            <div class="mb-3">
              <strong>숙소:</strong> {{ reservation.roomName }} ({{ reservation.accommodationName }})
            </div>
            <div class="mb-3">
              <strong>게스트:</strong> {{ reservation.userName }} ({{ reservation.userEmail }})
            </div>
            <div class="mb-3">
              <strong>체크인:</strong> {{ formatDate(reservation.checkInDate) }}
              <strong class="ms-3">체크아웃:</strong> {{ formatDate(reservation.checkOutDate) }}
            </div>
            <div class="mb-3">
              <strong>인원수:</strong> {{ reservation.guestCount }}명
            </div>
            <div class="mb-3">
              <strong>총 가격:</strong> {{ formatCurrency(reservation.totalPrice) }}
            </div>
            <div class="mb-3">
              <strong>결제 상태:</strong> {{ getPaymentStatusText(reservation.paymentStatus) }}
            </div>
            <div v-if="reservation.specialRequests" class="mb-3">
              <strong>특별 요청:</strong>
              <p class="mb-0">{{ reservation.specialRequests }}</p>
            </div>
            
            <!-- 예약 상태 업데이트 폼 -->
            <div v-if="reservation.status !== 'CANCELLED' && reservation.status !== 'COMPLETED'" class="mt-3">
              <div class="input-group">
                <select class="form-select" v-model="reservation.newStatus">
                  <option value="PENDING">대기 중</option>
                  <option value="CONFIRMED">확정</option>
                  <option value="CANCELLED">취소</option>
                  <option value="COMPLETED">완료</option>
                </select>
                <button class="btn btn-outline-primary" type="button" @click="updateReservationStatus(reservation)">상태 변경</button>
              </div>
            </div>
          </div>
          <div class="card-footer text-end">
            <router-link :to="`/reservation/detail/${reservation.reservationId}`" class="btn btn-sm btn-info">
              <i class="bi bi-eye"></i> 상세 보기
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HostReservations',
  data() {
    return {
      // 알림 메시지
      message: '',
      
      // 예약 목록
      reservations: [],
      
      // 숙소 목록
      accommodations: [],
      
      // 필터 조건
      filters: {
        status: '',
        accommodationId: ''
      }
    };
  },
  computed: {
    // 필터링된 예약 목록
    filteredReservations() {
      return this.reservations.map(reservation => ({
        ...reservation,
        newStatus: reservation.status // 상태 변경을 위한 임시 필드
      }));
    }
  },
  created() {
    // URL 쿼리 파라미터에서 필터 조건 가져오기
    const query = this.$route.query;
    if (query.status) this.filters.status = query.status;
    if (query.accommodationId) this.filters.accommodationId = query.accommodationId;
    
    // URL 쿼리 파라미터에서 메시지 가져오기
    if (query.message) {
      this.message = query.message;
    }
    
    // 숙소 목록 로드
    this.loadAccommodations();
    
    // 예약 목록 로드
    this.loadReservations();
  },
  methods: {
    // 숙소 목록 로드
    async loadAccommodations() {
      try {
        // API 호출
        const response = await fetch('/api/host/accommodations/list');
        if (!response.ok) {
          throw new Error('숙소 목록을 불러오는데 실패했습니다.');
        }
        
        this.accommodations = await response.json();
      } catch (error) {
        console.error('숙소 목록 로드 중 오류가 발생했습니다:', error);
      }
    },
    
    // 예약 목록 로드
    async loadReservations() {
      try {
        // API 호출
        let url = '/api/host/reservations';
        
        // 필터 조건이 있는 경우 쿼리 파라미터 추가
        const params = new URLSearchParams();
        if (this.filters.status) params.append('status', this.filters.status);
        if (this.filters.accommodationId) params.append('accommodationId', this.filters.accommodationId);
        
        if (params.toString()) {
          url += `?${params.toString()}`;
        }
        
        const response = await fetch(url);
        if (!response.ok) {
          throw new Error('예약 목록을 불러오는데 실패했습니다.');
        }
        
        this.reservations = await response.json();
      } catch (error) {
        console.error('예약 목록 로드 중 오류가 발생했습니다:', error);
        this.message = '예약 목록을 불러오는데 실패했습니다.';
      }
    },
    
    // 필터 적용
    applyFilters() {
      // URL 쿼리 파라미터 업데이트
      const query = {};
      if (this.filters.status) query.status = this.filters.status;
      if (this.filters.accommodationId) query.accommodationId = this.filters.accommodationId;
      
      this.$router.replace({ query });
      
      // 예약 목록 다시 로드
      this.loadReservations();
    },
    
    // 예약 상태 업데이트
    async updateReservationStatus(reservation) {
      try {
        // 상태가 변경되지 않은 경우 무시
        if (reservation.status === reservation.newStatus) {
          return;
        }
        
        // API 호출
        const response = await fetch(`/api/reservations/${reservation.reservationId}/status`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify({
            status: reservation.newStatus
          })
        });
        
        if (!response.ok) {
          throw new Error('예약 상태 변경에 실패했습니다.');
        }
        
        // 예약 목록 다시 로드
        this.loadReservations();
        this.message = '예약 상태가 변경되었습니다.';
      } catch (error) {
        console.error('예약 상태 변경 중 오류가 발생했습니다:', error);
        this.message = '예약 상태 변경에 실패했습니다.';
      }
    },
    
    // 예약 상태에 따른 배지 클래스 반환
    getStatusBadgeClass(status) {
      const baseClass = 'status-badge';
      switch (status) {
        case 'PENDING': return `${baseClass} status-pending`;
        case 'CONFIRMED': return `${baseClass} status-confirmed`;
        case 'CANCELLED': return `${baseClass} status-cancelled`;
        case 'COMPLETED': return `${baseClass} status-completed`;
        default: return baseClass;
      }
    },
    
    // 예약 상태 텍스트 반환
    getStatusText(status) {
      switch (status) {
        case 'PENDING': return '대기 중';
        case 'CONFIRMED': return '확정됨';
        case 'CANCELLED': return '취소됨';
        case 'COMPLETED': return '완료됨';
        default: return status;
      }
    },
    
    // 결제 상태 텍스트 반환
    getPaymentStatusText(status) {
      switch (status) {
        case 'UNPAID': return '미결제';
        case 'PAID': return '결제완료';
        case 'REFUNDED': return '환불됨';
        default: return status;
      }
    },
    
    // 날짜 포맷팅
    formatDate(dateString) {
      if (!dateString) return '-';
      
      const date = new Date(dateString);
      return new Intl.DateTimeFormat('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      }).format(date);
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
.reservation-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}
.reservation-card:hover {
  transform: translateY(-5px);
}
.status-badge {
  font-size: 0.9rem;
  padding: 5px 10px;
  border-radius: 20px;
}
.status-pending {
  background-color: #ffc107;
  color: #212529;
}
.status-confirmed {
  background-color: #198754;
  color: white;
}
.status-cancelled {
  background-color: #dc3545;
  color: white;
}
.status-completed {
  background-color: #0d6efd;
  color: white;
}
</style>