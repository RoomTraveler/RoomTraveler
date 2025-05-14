<template>
  <div class="container mt-5">
    <!-- 알림 메시지 -->
    <div v-if="message" class="alert alert-success alert-dismissible fade show" role="alert">
      {{ message }}
      <button type="button" class="btn-close" @click="message = ''" aria-label="Close"></button>
    </div>

    <!-- 로딩 표시 -->
    <div v-if="loading" class="text-center py-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">로딩 중...</span>
      </div>
      <p class="mt-2">숙소 정보를 불러오는 중입니다...</p>
    </div>

    <!-- 숙소 정보 -->
    <div v-if="accommodation" class="row mb-5">
      <!-- 이미지 캐러셀 -->
      <div class="col-md-8">
        <div id="accommodationCarousel" class="carousel slide" data-bs-ride="carousel">
          <div class="carousel-inner">
            <div v-if="accommodation.mainImageUrl" class="carousel-item active">
              <img :src="accommodation.mainImageUrl" class="d-block w-100" :alt="accommodation.title">
            </div>
            <div v-else class="carousel-item active">
              <img src="@/assets/no-image.jpg" class="d-block w-100" alt="이미지 없음">
            </div>
          </div>
          <button class="carousel-control-prev" type="button" data-bs-target="#accommodationCarousel" data-bs-slide="prev">
            <span class="carousel-control-prev-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Previous</span>
          </button>
          <button class="carousel-control-next" type="button" data-bs-target="#accommodationCarousel" data-bs-slide="next">
            <span class="carousel-control-next-icon" aria-hidden="true"></span>
            <span class="visually-hidden">Next</span>
          </button>
        </div>
      </div>

      <!-- 숙소 기본 정보 -->
      <div class="col-md-4">
        <div class="d-flex justify-content-between align-items-start">
          <h2>{{ accommodation.title }}</h2>
          <div>
            <!-- 찜하기 버튼 -->
            <button v-if="isLoggedIn" id="favoriteBtn" :class="['btn', isFavorite ? 'btn-danger' : 'btn-outline-danger']" @click="toggleFavorite">
              <i :class="['bi', isFavorite ? 'bi-heart-fill' : 'bi-heart']"></i> 
              <span>{{ isFavorite ? '찜 취소' : '찜하기' }}</span>
            </button>
          </div>
        </div>
        <p class="text-muted">{{ accommodation.sidoName }} {{ accommodation.gugunName }}</p>
        <p><i class="bi bi-geo-alt"></i> {{ accommodation.address }}</p>
        <p><i class="bi bi-telephone"></i> {{ accommodation.phone }}</p>
        <p v-if="accommodation.email"><i class="bi bi-envelope"></i> {{ accommodation.email }}</p>
        <p v-if="accommodation.website"><i class="bi bi-globe"></i> <a :href="accommodation.website" target="_blank">{{ accommodation.website }}</a></p>
        <p><i class="bi bi-clock"></i> 체크인: {{ accommodation.checkInTime }} / 체크아웃: {{ accommodation.checkOutTime }}</p>

        <!-- 호스트 정보 -->
        <div class="mt-4">
          <h5>호스트 정보</h5>
          <p><i class="bi bi-person"></i> {{ accommodation.hostName }}</p>
        </div>

        <!-- 호스트인 경우 수정/삭제 버튼 표시 -->
        <div v-if="isHost" class="mt-4">
          <router-link :to="`/accommodation/update/${accommodation.accommodationId}`" class="btn btn-outline-primary me-2">수정</router-link>
          <button @click="deleteAccommodation" class="btn btn-outline-danger">삭제</button>
          <router-link :to="`/accommodation/register-room/${accommodation.accommodationId}`" class="btn btn-success mt-2 w-100">객실 등록</router-link>
        </div>
      </div>
    </div>

    <!-- 숙소 상세 설명 -->
    <div v-if="accommodation" class="row mb-5">
      <div class="col-12">
        <div class="card">
          <div class="card-body">
            <h4 class="card-title">숙소 설명</h4>
            <p class="card-text">{{ accommodation.description }}</p>

            <h5 class="mt-4">편의시설</h5>
            <ul v-if="accommodation.amenities" class="amenities-list">
              <li v-for="(amenity, index) in amenitiesList" :key="index">
                <i class="bi bi-check-circle-fill text-success"></i> {{ amenity }}
              </li>
            </ul>
            <p v-else>등록된 편의시설 정보가 없습니다.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 객실 목록 -->
    <h3 class="mb-4" v-if="accommodation">객실 목록</h3>
    <div v-if="accommodation" class="row mb-5">
      <div v-if="rooms.length === 0" class="col-12 text-center py-5">
        <p class="lead">등록된 객실이 없습니다.</p>
      </div>

      <div v-for="room in rooms" :key="room.roomId" class="col-md-4 mb-4">
        <div class="card room-card">
          <img :src="room.mainImageUrl || require('@/assets/no-image.jpg')" class="card-img-top" :alt="room.name">
          <div class="card-body">
            <h5 class="card-title">{{ room.name }}</h5>
            <p class="card-text">
              <i class="bi bi-people"></i> 최대 {{ room.capacity }}인<br>
              <i class="bi bi-currency-dollar"></i> {{ formatPrice(room.price) }}
            </p>
            <div class="d-grid gap-2">
              <router-link :to="`/accommodation/room/${room.roomId}`" class="btn btn-outline-primary">상세 보기</router-link>
              <button class="btn btn-primary" @click="showReservationForm(room.roomId)">예약하기</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 리뷰 섹션 -->
    <h3 class="mb-4" v-if="accommodation">리뷰</h3>
    <div v-if="accommodation" class="row mb-5">
      <div class="col-12">
        <div class="d-flex justify-content-between align-items-center mb-3">
          <div>
            <div v-if="averageRating" class="d-flex align-items-center">
              <h4 class="me-2 mb-0">평균 평점: <span class="star-rating">{{ averageRating }}</span> / 5.0</h4>
              <div class="star-rating ms-2">
                <i v-for="i in 5" :key="i" :class="['bi', getStarClass(i)]"></i>
              </div>
            </div>
            <p v-if="averageRating" class="text-muted">총 {{ reviewCount }}개의 리뷰</p>
            <p v-else>아직 리뷰가 없습니다.</p>
          </div>
          <div>
            <router-link :to="`/review/write/${accommodation.accommodationId}`" class="btn btn-primary">
              <i class="bi bi-pencil-square"></i> 리뷰 작성
            </router-link>
            <router-link :to="`/review/accommodation/${accommodation.accommodationId}`" class="btn btn-outline-primary">
              <i class="bi bi-list-ul"></i> 모든 리뷰 보기
            </router-link>
          </div>
        </div>

        <!-- 리뷰 목록 (최근 3개만 표시) -->
        <div id="reviewsContainer">
          <div v-if="reviews.length === 0" class="alert alert-info">
            아직 리뷰가 없습니다. 첫 번째 리뷰를 작성해보세요!
          </div>

          <div v-for="review in reviews.slice(0, 3)" :key="review.reviewId" class="card review-card mb-3">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <h5 class="card-title">{{ review.title }}</h5>
                <div class="star-rating">
                  <i v-for="i in 5" :key="i" :class="['bi', i <= review.rating ? 'bi-star-fill' : 'bi-star']"></i>
                </div>
              </div>
              <div class="review-meta mb-2">
                <span><i class="bi bi-person-circle"></i> {{ review.username }}</span>
                <span class="ms-3"><i class="bi bi-calendar3"></i> {{ formatDate(review.stayDate) }}</span>
                <span class="ms-3"><i class="bi bi-clock"></i> {{ formatDate(review.createdAt) }}</span>
              </div>
              <p class="card-text">{{ review.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 위치 정보 -->
    <h3 class="mb-4" v-if="accommodation">위치</h3>
    <div v-if="accommodation" class="row mb-5">
      <div class="col-12">
        <div id="map" style="width:100%; height:400px;"></div>
      </div>
    </div>

    <!-- 토스트 메시지 -->
    <div class="toast-container position-fixed bottom-0 end-0 p-3">
      <div v-for="(toast, index) in toasts" :key="index" class="toast show" role="alert" aria-live="assertive" aria-atomic="true">
        <div class="toast-header">
          <strong class="me-auto">알림</strong>
          <button type="button" class="btn-close" @click="removeToast(index)"></button>
        </div>
        <div class="toast-body">
          {{ toast.message }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapState } from 'vuex';

export default {
  name: 'AccommodationDetail',
  props: {
    id: {
      type: [String, Number],
      required: true
    }
  },
  data() {
    return {
      accommodation: null,
      rooms: [],
      reviews: [],
      loading: true,
      message: '',
      isFavorite: false,
      favoriteId: null,
      averageRating: 0,
      reviewCount: 0,
      toasts: []
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.user.isLoggedIn,
      userId: state => state.user.user?.id,
      userRole: state => state.user.user?.role
    }),
    isAdmin() {
      return this.userRole === 'ADMIN';
    },
    isHost() {
      return this.accommodation && this.userId === this.accommodation.hostId;
    },
    amenitiesList() {
      if (!this.accommodation || !this.accommodation.amenities) return [];
      return this.accommodation.amenities.split(',').map(item => item.trim());
    }
  },
  created() {
    this.fetchData();
  },
  methods: {
    // 데이터 가져오기
    async fetchData() {
      this.loading = true;
      try {
        await this.fetchAccommodation();
        await this.fetchRooms();
        await this.fetchReviews();
        if (this.isLoggedIn) {
          await this.checkFavoriteStatus();
        }
      } catch (error) {
        console.error('Error fetching data:', error);
        this.showToast('데이터를 불러오는데 실패했습니다.');
      } finally {
        this.loading = false;
      }
    },

    // 숙소 정보 가져오기
    async fetchAccommodation() {
      const response = await fetch(`/api/accommodations/${this.id}`);
      if (!response.ok) throw new Error('숙소 정보를 불러오는데 실패했습니다.');
      this.accommodation = await response.json();
    },

    // 객실 정보 가져오기
    async fetchRooms() {
      const response = await fetch(`/api/accommodations/${this.id}/rooms`);
      if (!response.ok) throw new Error('객실 정보를 불러오는데 실패했습니다.');
      this.rooms = await response.json();
    },

    // 리뷰 정보 가져오기
    async fetchReviews() {
      const response = await fetch(`/api/reviews/accommodation/${this.id}`);
      if (!response.ok) throw new Error('리뷰 정보를 불러오는데 실패했습니다.');
      const data = await response.json();
      this.reviews = data.content || [];
      this.averageRating = data.averageRating || 0;
      this.reviewCount = data.totalElements || 0;
    },

    // 즐겨찾기 상태 확인
    async checkFavoriteStatus() {
      const response = await fetch(`/api/accommodations/check-favorite?accommodationId=${this.id}`);
      if (!response.ok) throw new Error('즐겨찾기 상태 확인에 실패했습니다.');
      const data = await response.json();
      this.isFavorite = data.isFavorite;
      if (this.isFavorite) {
        this.favoriteId = data.favoriteId;
      }
    },

    // 즐겨찾기 토글
    async toggleFavorite() {
      if (!this.isLoggedIn) {
        this.showToast('로그인이 필요한 기능입니다.');
        return;
      }

      try {
        if (this.isFavorite) {
          // 즐겨찾기 삭제
          const response = await fetch('/api/accommodations/remove-favorite', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `favoriteId=${this.favoriteId}`
          });

          if (!response.ok) throw new Error('즐겨찾기 삭제에 실패했습니다.');
          const data = await response.json();

          if (data.success) {
            this.isFavorite = false;
            this.favoriteId = null;
            this.showToast('즐겨찾기에서 삭제되었습니다.');
          }
        } else {
          // 즐겨찾기 추가
          const response = await fetch('/api/accommodations/add-favorite', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: `accommodationId=${this.id}`
          });

          if (!response.ok) throw new Error('즐겨찾기 추가에 실패했습니다.');
          const data = await response.json();

          if (data.success) {
            this.isFavorite = true;
            this.favoriteId = data.favoriteId;
            this.showToast('즐겨찾기에 추가되었습니다.');
          }
        }
      } catch (error) {
        console.error('Error toggling favorite:', error);
        this.showToast('오류가 발생했습니다.');
      }
    },

    // 예약 폼 표시
    showReservationForm(roomId) {
      if (!this.isLoggedIn) {
        this.showToast('로그인이 필요한 기능입니다.');
        return;
      }

      // 예약 페이지로 이동
      this.$router.push({
        path: '/reservation/form',
        query: {
          roomId: roomId,
          accommodationId: this.id
        }
      });
    },

    // 숙소 삭제
    async deleteAccommodation() {
      if (!confirm('정말 삭제하시겠습니까?')) return;

      try {
        const response = await fetch(`/api/accommodations/${this.id}`, {
          method: 'DELETE'
        });

        if (!response.ok) throw new Error('숙소 삭제에 실패했습니다.');

        this.showToast('숙소가 삭제되었습니다.');
        this.$router.push('/accommodation/list');
      } catch (error) {
        console.error('Error deleting accommodation:', error);
        this.showToast('숙소 삭제에 실패했습니다.');
      }
    },

    // 별점 클래스 가져오기
    getStarClass(index) {
      if (index <= Math.floor(this.averageRating)) {
        return 'bi-star-fill';
      } else if (index <= Math.ceil(this.averageRating) && this.averageRating % 1 !== 0) {
        return 'bi-star-half';
      } else {
        return 'bi-star';
      }
    },

    // 날짜 포맷
    formatDate(dateString) {
      if (!dateString) return '';
      const date = new Date(dateString);
      return date.toLocaleDateString('ko-KR');
    },

    // 가격 포맷
    formatPrice(price) {
      return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(price);
    },

    // 토스트 메시지 표시
    showToast(message) {
      this.toasts.push({ message });
      setTimeout(() => this.toasts.shift(), 3000);
    },

    // 토스트 메시지 제거
    removeToast(index) {
      this.toasts.splice(index, 1);
    }
  }
};
</script>

<style scoped>
:root {
  --yanolja-red: #f0213b;
  --yanolja-pink: #ff3478;
  --yanolja-light-gray: #f5f5f5;
  --yanolja-dark-gray: #666;
}

body {
  font-family: 'Noto Sans KR', sans-serif;
  color: #333;
  background-color: #f9f9f9;
}

.room-card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  background-color: white;
  transition: transform 0.3s;
  height: 100%;
}

.room-card:hover {
  transform: translateY(-5px);
}

.card-img-top {
  height: 180px;
  object-fit: cover;
}

.card-body {
  padding: 15px;
}

.star-rating {
  color: #ffb700;
  font-weight: bold;
  margin-right: 5px;
}

.amenities-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.review-card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 15px;
  background-color: white;
}

.review-meta {
  font-size: 0.9rem;
  color: var(--yanolja-dark-gray);
}

.btn-yanolja {
  background-color: var(--yanolja-red);
  color: white;
  border: none;
}

.btn-yanolja:hover {
  background-color: #d01c33;
  color: white;
}

.loading {
  text-align: center;
  padding: 20px;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.pagination button {
  margin: 0 5px;
  padding: 5px 10px;
  background-color: #f5f5f5;
  border: 1px solid #ddd;
  cursor: pointer;
}

.pagination button.active {
  background-color: var(--yanolja-red);
  color: white;
  border-color: var(--yanolja-red);
}
</style>
