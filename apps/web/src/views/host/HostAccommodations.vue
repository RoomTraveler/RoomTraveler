<template>
  <div class="container mt-5 mb-5">
    <h1 class="mb-4">
      <i class="bi bi-houses"></i> 호스트 숙소 관리
    </h1>
    
    <!-- 알림 메시지 표시 -->
    <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
      {{ message }}
      <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
    </div>
    
    <!-- 숙소 통계 -->
    <div class="row mb-4">
      <div class="col-md-3">
        <div class="card stats-card">
          <div class="card-body">
            <h5 class="card-title">총 숙소</h5>
            <p class="card-text fs-2">{{ accommodations.length }}</p>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card stats-card">
          <div class="card-body">
            <h5 class="card-title">총 객실</h5>
            <p class="card-text fs-2">{{ totalRooms }}</p>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card stats-card">
          <div class="card-body">
            <h5 class="card-title">활성 숙소</h5>
            <p class="card-text fs-2">{{ activeAccommodations }}</p>
          </div>
        </div>
      </div>
      <div class="col-md-3">
        <div class="card stats-card">
          <div class="card-body">
            <h5 class="card-title">평균 평점</h5>
            <p class="card-text fs-2">
              <span class="text-warning">
                <i class="bi bi-star-fill"></i>
              </span>
              <span>{{ averageRating }}</span>
            </p>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 숙소 목록 -->
    <div v-if="accommodations.length === 0" class="alert alert-info">
      <i class="bi bi-info-circle"></i> 등록된 숙소가 없습니다. 아래 + 버튼을 클릭하여 새 숙소를 등록하세요.
    </div>
    
    <div v-else class="row">
      <div v-for="accommodation in accommodations" :key="accommodation.accommodationId" class="col-md-6 col-lg-4 mb-4">
        <div class="card accommodation-card">
          <img 
            :src="accommodation.mainImageUrl || require('@/assets/default-accommodation.jpg')" 
            class="card-img-top" 
            :alt="accommodation.title"
          >
          <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="card-title mb-0">{{ accommodation.title }}</h5>
            <span :class="getStatusBadgeClass(accommodation.status)">
              {{ getStatusText(accommodation.status) }}
            </span>
          </div>
          <div class="card-body">
            <p class="card-text">
              <i class="bi bi-geo-alt"></i> {{ accommodation.address }}
            </p>
            <p class="card-text">
              <i class="bi bi-telephone"></i> {{ accommodation.phone }}
            </p>
            <!-- 객실 정보 -->
            <div class="mt-3">
              <h6>객실</h6>
              <div v-if="!getRooms(accommodation.accommodationId).length">
                <p class="text-muted">등록된 객실이 없습니다.</p>
              </div>
              <div v-else>
                <div 
                  v-for="(room, index) in getRooms(accommodation.accommodationId)" 
                  :key="room.roomId" 
                  class="room-card p-2"
                  :class="{ 'mb-2': index !== getRooms(accommodation.accommodationId).length - 1 }"
                >
                  <div class="d-flex justify-content-between align-items-center">
                    <div>
                      <strong>{{ room.name }}</strong>
                      <span class="ms-2 text-muted">
                        {{ formatCurrency(room.price) }}
                      </span>
                    </div>
                    <div>
                      <router-link 
                        :to="`/accommodation/update-room-form?roomId=${room.roomId}`" 
                        class="btn btn-sm btn-outline-primary"
                      >
                        <i class="bi bi-pencil"></i>
                      </router-link>
                      <button 
                        class="btn btn-sm btn-outline-danger" 
                        @click="deleteRoom(room.roomId)"
                      >
                        <i class="bi bi-trash"></i>
                      </button>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 객실 추가 버튼 -->
              <router-link 
                :to="`/accommodation/register-room-form?accommodationId=${accommodation.accommodationId}`" 
                class="btn btn-sm btn-outline-success w-100 mt-2"
              >
                <i class="bi bi-plus-circle"></i> 객실 추가
              </router-link>
            </div>
          </div>
          <div class="card-footer">
            <div class="d-flex justify-content-between">
              <router-link 
                :to="`/accommodation/detail?accommodationId=${accommodation.accommodationId}`" 
                class="btn btn-sm btn-info"
              >
                <i class="bi bi-eye"></i> 보기
              </router-link>
              <div>
                <router-link 
                  :to="`/accommodation/update-form?accommodationId=${accommodation.accommodationId}`" 
                  class="btn btn-sm btn-primary"
                >
                  <i class="bi bi-pencil"></i> 수정
                </router-link>
                <button 
                  class="btn btn-sm btn-danger" 
                  @click="deleteAccommodation(accommodation.accommodationId)"
                >
                  <i class="bi bi-trash"></i> 삭제
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 숙소 추가 버튼 -->
    <router-link to="/accommodation/register-form" class="add-btn">
      <i class="bi bi-plus-lg"></i>
    </router-link>
  </div>
</template>

<script>
export default {
  name: 'HostAccommodations',
  data() {
    return {
      // 알림 메시지
      message: '',
      
      // 숙소 목록
      accommodations: [],
      
      // 객실 목록 (숙소 ID별로 그룹화)
      roomsByAccommodation: {},
      
      // 통계 데이터
      totalRooms: 0,
      activeAccommodations: 0,
      averageRating: 0
    };
  },
  created() {
    // URL 쿼리 파라미터에서 메시지 가져오기
    if (this.$route.query.message) {
      this.message = this.$route.query.message;
    }
    
    // 숙소 및 객실 데이터 로드
    this.loadAccommodations();
  },
  methods: {
    // 숙소 및 객실 데이터 로드
    async loadAccommodations() {
      try {
        // API 호출
        const response = await fetch('/api/host/accommodations');
        if (!response.ok) {
          throw new Error('숙소 정보를 불러오는데 실패했습니다.');
        }
        
        const data = await response.json();
        this.accommodations = data.accommodations;
        this.roomsByAccommodation = data.roomsByAccommodation;
        
        // 통계 계산
        this.calculateStatistics();
      } catch (error) {
        console.error('숙소 정보 로드 중 오류가 발생했습니다:', error);
        this.message = '숙소 정보를 불러오는데 실패했습니다.';
      }
    },
    
    // 통계 계산
    calculateStatistics() {
      // 총 객실 수 계산
      this.totalRooms = Object.values(this.roomsByAccommodation)
        .reduce((total, rooms) => total + rooms.length, 0);
      
      // 활성 숙소 수 계산
      this.activeAccommodations = this.accommodations
        .filter(acc => acc.status === 'ACTIVE')
        .length;
      
      // 평균 평점 계산
      const totalRating = this.accommodations
        .reduce((sum, acc) => sum + (acc.rating || 0), 0);
      
      this.averageRating = this.accommodations.length > 0
        ? (totalRating / this.accommodations.length).toFixed(1)
        : '0.0';
    },
    
    // 숙소 ID에 해당하는 객실 목록 반환
    getRooms(accommodationId) {
      return this.roomsByAccommodation[accommodationId] || [];
    },
    
    // 객실 삭제
    async deleteRoom(roomId) {
      if (!confirm('정말로 이 객실을 삭제하시겠습니까?')) {
        return;
      }
      
      try {
        // API 호출
        const response = await fetch(`/api/rooms/${roomId}`, {
          method: 'DELETE'
        });
        
        if (!response.ok) {
          throw new Error('객실 삭제에 실패했습니다.');
        }
        
        // 객실 목록 다시 로드
        this.loadAccommodations();
        this.message = '객실이 삭제되었습니다.';
      } catch (error) {
        console.error('객실 삭제 중 오류가 발생했습니다:', error);
        this.message = '객실 삭제에 실패했습니다.';
      }
    },
    
    // 숙소 삭제
    async deleteAccommodation(accommodationId) {
      if (!confirm('정말로 이 숙소를 삭제하시겠습니까? 모든 객실 정보도 함께 삭제됩니다.')) {
        return;
      }
      
      try {
        // API 호출
        const response = await fetch(`/api/accommodations/${accommodationId}`, {
          method: 'DELETE'
        });
        
        if (!response.ok) {
          throw new Error('숙소 삭제에 실패했습니다.');
        }
        
        // 숙소 목록 다시 로드
        this.loadAccommodations();
        this.message = '숙소가 삭제되었습니다.';
      } catch (error) {
        console.error('숙소 삭제 중 오류가 발생했습니다:', error);
        this.message = '숙소 삭제에 실패했습니다.';
      }
    },
    
    // 숙소 상태에 따른 배지 클래스 반환
    getStatusBadgeClass(status) {
      const baseClass = 'status-badge';
      switch (status) {
        case 'ACTIVE': return `${baseClass} status-active`;
        case 'INACTIVE': return `${baseClass} status-inactive`;
        case 'PENDING_REVIEW': return `${baseClass} status-pending`;
        default: return baseClass;
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
/* 숙소 카드 스타일 */
.accommodation-card {
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
  height: 100%;
}
.accommodation-card:hover {
  transform: translateY(-5px);
}

/* 객실 카드 스타일 */
.room-card {
  margin-bottom: 15px;
  border-radius: 8px;
  border: 1px solid #dee2e6;
}

/* 이미지 스타일 */
.card-img-top {
  height: 200px;
  object-fit: cover;
  border-top-left-radius: 10px;
  border-top-right-radius: 10px;
}

/* 상태 배지 스타일 */
.status-badge {
  font-size: 0.8rem;
  padding: 5px 10px;
  border-radius: 20px;
}
.status-active {
  background-color: #198754;
  color: white;
}
.status-inactive {
  background-color: #dc3545;
  color: white;
}
.status-pending {
  background-color: #ffc107;
  color: #212529;
}

/* 통계 카드 스타일 */
.stats-card {
  text-align: center;
  margin-bottom: 20px;
}

/* 추가 버튼 스타일 */
.add-btn {
  position: fixed;
  bottom: 30px;
  right: 30px;
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background-color: #0d6efd;
  color: white;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  font-size: 1.5rem;
  text-decoration: none;
}
.add-btn:hover {
  background-color: #0b5ed7;
  color: white;
}
</style>