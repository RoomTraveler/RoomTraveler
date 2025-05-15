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

    <!-- 숙소 정보 & 예약 박스 -->
    <div class="row mb-5">
      <!-- 이미지 & 썸네일 캐러셀 -->
      <div class="col-lg-7">
        <div class="carousel-wrapper">
          <div class="carousel-inner">
            <div
                v-for="(img, idx) in images"
                :key="idx"
                :class="['carousel-item', { active: idx === activeSlide }]"
            >
              <img :src="img" class="d-block w-100" :alt="accommodation.title || '숙소 이미지'" />
            </div>
          </div>
          <button class="carousel-prev" @click="prevSlide"><i class="bi bi-chevron-left"></i></button>
          <button class="carousel-next" @click="nextSlide"><i class="bi bi-chevron-right"></i></button>
        </div>
        <div class="carousel-thumbs mt-2 d-flex gap-2">
          <img
              v-for="(img, idx) in images"
              :key="idx"
              :src="img"
              class="thumb"
              :class="{ active: idx === activeSlide }"
              @click="goToSlide(idx)"
              alt="썸네일"
          />
        </div>
      </div>

      <!-- 우측 예약 박스 & 기본정보 -->
      <div class="col-lg-5">
        <div class="sticky-box p-4">
          <h4 class="price">{{ accommodation.basePrice ? formatPrice(accommodation.basePrice) : '--' }} <small>/박</small></h4>
          <div class="date-picker mb-3">
            <label>체크인</label>
            <input type="date" v-model="checkInDate" class="form-control mb-2" />
            <label>체크아웃</label>
            <input type="date" v-model="checkOutDate" class="form-control" />
          </div>
          <div class="guests mb-3">
            <label>게스트</label>
            <select v-model="guestCount" class="form-select">
              <option v-for="n in (accommodation.maxGuests || 1)" :key="n" :value="n">{{ n }}명</option>
            </select>
          </div>
          <button class="btn btn-yanolja w-100 mb-3" @click="bookNow">예약하기</button>

          <hr />

          <h2 class="mb-2">{{ accommodation.title || '숙소명 없음' }}</h2>
          <p class="text-muted">{{ accommodation.sidoName || '-' }} {{ accommodation.gugunName || '' }}</p>
          <p><i class="bi bi-geo-alt me-1"></i>{{ accommodation.address || '주소 정보 없음' }}</p>
          <p><i class="bi bi-telephone me-1"></i>{{ accommodation.phone || '-' }}</p>
        </div>
      </div>
    </div>

    <!-- 숙소 설명 & 편의시설 -->
    <div class="row mb-5">
      <div class="col-12">
        <div class="card detail-card">
          <div class="card-body">
            <h4 class="card-title">숙소 설명</h4>
            <p class="card-text">{{ accommodation.description || '숙소 설명이 없습니다.' }}</p>
            <h5 class="mt-4">편의시설</h5>
            <ul v-if="amenitiesList.length" class="amenities-list">
              <li v-for="(amenity, i) in amenitiesList" :key="i">
                <i class="bi bi-check-circle-fill text-success me-1"></i>{{ amenity }}
              </li>
            </ul>
            <p v-else class="text-muted">등록된 편의시설 정보가 없습니다.</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 객실 목록 -->
    <h3 class="mb-4">객실 목록</h3>
    <div class="row mb-5">
      <div v-if="rooms.length === 0" class="col-12 text-center py-5">
        <p class="lead">등록된 객실이 없습니다.</p>
      </div>
      <div v-for="room in rooms" :key="room.roomId" class="col-md-4 mb-4">
        <div class="card room-card h-100">
          <img :src="room.mainImageUrl || require('@/assets/no-image.jpg')" class="card-img-top" :alt="room.name || '객실 이미지'" />
          <div class="card-body d-flex flex-column">
            <h5 class="card-title">{{ room.name || '객실명 없음' }}</h5>
            <p class="card-text mb-3">
              <i class="bi bi-people me-1"></i>최대 {{ room.capacity || 1 }}인<br />
              <i class="bi bi-currency-dollar me-1"></i>{{ room.price ? formatPrice(room.price) : '--' }}
            </p>
            <div class="mt-auto d-grid gap-2">
              <router-link :to="`/accommodation/room/${room.roomId}`" class="btn btn-outline-primary btn-sm">
                상세 보기
              </router-link>
              <button class="btn btn-primary btn-sm" @click="showReservationForm(room.roomId)">예약하기</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 리뷰 & 위치 등은 기존과 동일 -->
  </div>
</template>

<script>
import { mapState } from 'vuex';

export default {
  name: 'AccommodationDetail',
  props: { id: { type: [String, Number], required: true } },
  data() {
    return {
      accommodation: {},
      rooms: [],
      reviews: [],
      loading: true,
      message: '',
      isFavorite: false,
      favoriteId: null,
      averageRating: 0,
      reviewCount: 0,
      toasts: [],
      activeSlide: 0,
      checkInDate: '',
      checkOutDate: '',
      guestCount: 1
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: (s) => s.user.isLoggedIn,
      userId: (s) => s.user.user?.id
    }),
    isHost() {
      return this.userId === this.accommodation.hostId;
    },
    amenitiesList() {
      return this.accommodation.amenities
          ? this.accommodation.amenities.split(',').map(i => i.trim())
          : [];
    },
    images() {
      if (this.accommodation.images?.length) {
        return this.accommodation.images.map(i => i.url);
      }
      if (this.accommodation.mainImageUrl) {
        return [this.accommodation.mainImageUrl];
      }
      return [require('@/assets/no-image.jpg')];
    }
  },
  created() {
    this.fetchData();
  },
  methods: {
    async fetchData() {
      this.loading = true;
      try {
        await this.fetchAccommodation();
        await this.fetchRooms();
        await this.fetchReviews();
        if (this.isLoggedIn) await this.checkFavoriteStatus();
      } catch (e) {
        console.error(e);
        this.showToast('데이터를 불러오는데 실패했습니다.');
      } finally {
        this.loading = false;
      }
    },
    prevSlide() {
      this.activeSlide = (this.activeSlide + this.images.length - 1) % this.images.length;
    },
    nextSlide() {
      this.activeSlide = (this.activeSlide + 1) % this.images.length;
    },
    goToSlide(idx) {
      this.activeSlide = idx;
    },
    bookNow() {
      if (!this.isLoggedIn) return this.showToast('로그인이 필요합니다');
      this.$router.push({
        path: '/reservation/form',
        query: {
          accommodationId: this.id,
          checkIn: this.checkInDate,
          checkOut: this.checkOutDate,
          guests: this.guestCount
        }
      });
    },
    formatPrice(price) {
      return new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW' }).format(price);
    },
    showToast(msg) {
      this.toasts.push({ message: msg });
      setTimeout(() => this.toasts.shift(), 3000);
    }
  }
};
</script>

<style scoped>
:root { --yanolja-red: #f0213b; }

.carousel-wrapper { position: relative; overflow: hidden; border-radius: 8px; height: 450px; }
.carousel-inner { position: relative; width: 100%; height: 100%; }
.carousel-item { position: absolute; top: 0; left: 0; width: 100%; height: 100%; opacity: 0; transition: opacity 0.5s ease; }
.carousel-item.active { position: relative; opacity: 1; }
.carousel-wrapper img { object-fit: cover; height: 100%; }
.carousel-prev { position: absolute; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.4); border: none; padding: 8px; border-radius: 50%; color: white; cursor: pointer; left: 10px; }
.carousel-next { position: absolute; top: 50%; transform: translateY(-50%); background: rgba(0,0,0,0.4); border: none; padding: 8px; border-radius: 50%; color: white; cursor: pointer; right: 10px; }
.carousel-thumbs .thumb { width: 80px; height: 60px; object-fit: cover; cursor: pointer; border: 2px solid transparent; border-radius: 4px; transition: border-color 0.3s; }
.carousel-thumbs .thumb.active { border-color: var(--yanolja-red); }
.sticky-box { position: sticky; top: 100px; background: white; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.1); }
.price { font-size: 1.8rem; color: var(--yanolja-red); margin-bottom: 1rem; }
.detail-card { border: none; border-radius: 8px; box-shadow: 0 2px 12px rgba(0,0,0,0.05); }
.room-card { border: none; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); transition: transform 0.3s; }
.room-card:hover { transform: translateY(-5px); }
.room-card .card-img-top { height: 180px; object-fit: cover; }
.amenities-list { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; list-style: none; padding: 0; }
.btn-yanolja { background-color: var(--yanolja-red); color: white; border: none; }
.btn-yanolja:hover { background-color: #d01c33; color: white; }
</style>
