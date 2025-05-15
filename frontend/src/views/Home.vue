<template>
  <Layout>
    <!-- 메인 배너 영역 -->
    <div class="main-banner">
      <img src="/img/accommodationBanner.png" alt="메인 배너 이미지" class="banner-image" />
    </div>

    <!-- 숙소 카테고리 섹션 -->
    <section class="section">
      <div class="section-title">
        숙소 유형
      </div>
      <div class="category-grid">
        <!-- 모텔 카테고리 -->
        <router-link to="/accommodation/motel" class="category-item">
          <div class="category-image-container">
            <img src="/img/accommodationMotel.png" alt="모텔" class="category-image" />
          </div>
          <div class="category-name">모텔</div>
        </router-link>

        <!-- 호텔/리조트 카테고리 -->
        <router-link to="/accommodation/hotel" class="category-item">
          <div class="category-image-container">
            <img src="/img/accommodationHotel.png" alt="호텔/리조트" class="category-image" />
          </div>
          <div class="category-name">호텔/리조트</div>
        </router-link>

        <!-- 펜션/풀빌라 카테고리 -->
        <router-link to="/accommodation/pension" class="category-item">
          <div class="category-image-container">
            <img src="/img/accommodationPension.png" alt="펜션/풀빌라" class="category-image" />
          </div>
          <div class="category-name">펜션/풀빌라</div>
        </router-link>

        <!-- 프리미엄 카테고리 -->
        <router-link to="/accommodation/premium" class="category-item">
          <div class="category-image-container">
            <img src="/img/accommodationPremium.png" alt="프리미엄" class="category-image" />
          </div>
          <div class="category-name">프리미엄</div>
        </router-link>

        <!-- 글램핑/캠핑 카테고리 -->
        <router-link to="/accommodation/camping" class="category-item">
          <div class="category-image-container">
            <img src="/img/accommodationCamping.png" alt="글램핑/캠핑" class="category-image" />
          </div>
          <div class="category-name">글램핑/캠핑</div>
        </router-link>
      </div>
    </section>

    <!-- 이벤트 섹션 -->
    <section class="section">
      <div class="section-title">
        진행중인 이벤트
        <router-link to="/events" class="view-all">전체보기 <i class="bi bi-chevron-right"></i></router-link>
      </div>
      <div class="event-slider">
        <!-- 이벤트 1 -->
        <div class="event-card">
          <img src="https://via.placeholder.com/800x300?text=Event+1" alt="이벤트 1" class="event-image" />
          <div class="event-info">
            <div class="event-title">여름 휴가 특별 할인</div>
            <div class="event-period">2025.05.15 ~ 2025.06.30</div>
          </div>
        </div>

        <!-- 이벤트 2 -->
        <div class="event-card">
          <img src="https://via.placeholder.com/800x300?text=Event+2" alt="이벤트 2" class="event-image" />
          <div class="event-info">
            <div class="event-title">신규 회원 첫 예약 50% 할인</div>
            <div class="event-period">2025.05.01 ~ 2025.05.31</div>
          </div>
        </div>
      </div>
    </section>



    <!-- 지역별 인기 숙소 섹션 -->
    <section class="section">
      <div class="section-title">
        지역별 인기 숙소
        <router-link to="/accommodation/popular-by-region" class="view-all">전체보기 <i class="bi bi-chevron-right"></i></router-link>
      </div>

      <!-- 지역 탭 -->
      <div class="region-tabs">
        <button class="region-tab active">서울</button>
        <button class="region-tab">부산</button>
        <button class="region-tab">제주</button>
        <button class="region-tab">강원</button>
        <button class="region-tab">경기</button>
      </div>

      <!-- 인라인 호텔 리스트 -->
      <div class="region-hotel-list">
        <!-- 호텔 1 -->
        <div class="region-hotel-item">
          <div class="hotel-image">
            <img src="https://via.placeholder.com/200x150?text=Hotel+1" alt="호텔 이미지" />
          </div>
          <div class="hotel-info">
            <div class="hotel-rating">
              <i class="bi bi-star-fill"></i> 4.9 <span class="rating-count">(412)</span>
            </div>
            <div class="hotel-price">220,000원</div>
          </div>
        </div>

        <!-- 호텔 2 -->
        <div class="region-hotel-item">
          <div class="hotel-image">
            <img src="https://via.placeholder.com/200x150?text=Hotel+2" alt="호텔 이미지" />
          </div>
          <div class="hotel-info">
            <div class="hotel-rating">
              <i class="bi bi-star-fill"></i> 4.8 <span class="rating-count">(356)</span>
            </div>
            <div class="hotel-price">180,000원</div>
          </div>
        </div>

        <!-- 호텔 3 -->
        <div class="region-hotel-item">
          <div class="hotel-image">
            <img src="https://via.placeholder.com/200x150?text=Hotel+3" alt="호텔 이미지" />
          </div>
          <div class="hotel-info">
            <div class="hotel-rating">
              <i class="bi bi-star-fill"></i> 4.7 <span class="rating-count">(289)</span>
            </div>
            <div class="hotel-price">170,000원</div>
          </div>
        </div>

        <!-- 호텔 4 -->
        <div class="region-hotel-item">
          <div class="hotel-image">
            <img src="https://via.placeholder.com/200x150?text=Hotel+4" alt="호텔 이미지" />
          </div>
          <div class="hotel-info">
            <div class="hotel-rating">
              <i class="bi bi-star-fill"></i> 4.9 <span class="rating-count">(198)</span>
            </div>
            <div class="hotel-price">250,000원</div>
          </div>
        </div>

        <!-- 호텔 5 -->
        <div class="region-hotel-item">
          <div class="hotel-image">
            <img src="https://via.placeholder.com/200x150?text=Hotel+5" alt="호텔 이미지" />
          </div>
          <div class="hotel-info">
            <div class="hotel-rating">
              <i class="bi bi-star-fill"></i> 4.6 <span class="rating-count">(245)</span>
            </div>
            <div class="hotel-price">195,000원</div>
          </div>
        </div>
      </div>
    </section>

    <!-- 인기 여행지 섹션 -->
    <section class="section">
      <div class="section-title">
        인기 여행지
        <router-link to="/tourapi/popular" class="view-all">전체보기 <i class="bi bi-chevron-right"></i></router-link>
      </div>
      <div class="destination-grid">
        <div class="destination-card">
          <img src="https://via.placeholder.com/300x200?text=Seoul" alt="서울" class="destination-image" />
          <div class="destination-name">서울</div>
        </div>
        <div class="destination-card">
          <img src="https://via.placeholder.com/300x200?text=Busan" alt="부산" class="destination-image" />
          <div class="destination-name">부산</div>
        </div>
        <div class="destination-card">
          <img src="https://via.placeholder.com/300x200?text=Jeju" alt="제주" class="destination-image" />
          <div class="destination-name">제주</div>
        </div>
        <div class="destination-card">
          <img src="https://via.placeholder.com/300x200?text=Gangneung" alt="강릉" class="destination-image" />
          <div class="destination-name">강릉</div>
        </div>
        <div class="destination-card">
          <img src="https://via.placeholder.com/300x200?text=Gyeongju" alt="경주" class="destination-image" />
          <div class="destination-name">경주</div>
        </div>
      </div>
    </section>

    <!-- 사용자 계정 섹션 (로그인 시에만 표시) -->
    <section v-if="isLoggedIn" class="section">
      <div class="section-title">
        내 계정
      </div>
      <div class="user-menu-grid">
        <router-link to="/user/profile" class="user-menu-item">
          <i class="bi bi-person-circle"></i>
          <span>내 정보</span>
        </router-link>
        <router-link to="/reservation/my-reservations" class="user-menu-item">
          <i class="bi bi-calendar-check"></i>
          <span>예약 내역</span>
        </router-link>
        <router-link to="/accommodation/favorites" class="user-menu-item">
          <i class="bi bi-heart"></i>
          <span>찜 목록</span>
        </router-link>
        <router-link to="/review/my-reviews" class="user-menu-item">
          <i class="bi bi-star"></i>
          <span>내 리뷰</span>
        </router-link>
        <router-link v-if="isHost || isAdmin" to="/accommodation/my-accommodations" class="user-menu-item">
          <i class="bi bi-house"></i>
          <span>내 숙소</span>
        </router-link>
        <router-link v-if="isAdmin" to="/admin/dashboard" class="user-menu-item">
          <i class="bi bi-gear"></i>
          <span>관리자</span>
        </router-link>
      </div>
    </section>
  </Layout>
</template>

<script>
import Layout from '@/components/layout/Layout.vue';
import { useUserStore } from '@/store/userStore';

export default {
  name: 'HomeView',
  components: {
    Layout
  },
  setup() {
    const userStore = useUserStore();
    return { userStore };
  },
  computed: {
    isLoggedIn() {
      return this.userStore.isAuthenticated;
    },
    user() {
      return this.userStore.user;
    },
    userId() {
      return this.userStore.user?.id;
    },
    isAdmin() {
      return this.userStore.userRole === 'ADMIN';
    },
    isHost() {
      return this.userStore.userRole === 'HOST';
    }
  }
};
</script>

<style scoped>
/* 메인 배너 스타일 */
.main-banner {
  position: relative;
  height: 500px;
  margin-bottom: 40px;
  border-radius: 8px;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 8px;
}

/* Banner styles removed as they are no longer needed */

/* 카드 이미지 컨테이너 스타일 */
.card-image-container {
  position: relative;
  overflow: hidden;
}

.like-button {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: rgba(255, 255, 255, 0.8);
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
}

.like-button:hover {
  background-color: white;
  transform: scale(1.1);
}

.like-button i {
  color: #999;
  font-size: 16px;
}

.like-button:hover i {
  color: var(--yanolja-red);
}

/* 카드 내용 추가 스타일 */
.yanolja-card-rating {
  margin-bottom: 8px;
  color: #666;
  font-size: 14px;
}

.yanolja-card-rating i {
  color: #ffb700;
  margin-right: 3px;
}

/* 카테고리 그리드 스타일 */
.category-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 15px;
  margin-bottom: 30px;
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-decoration: none;
  color: #333;
  transition: transform 0.3s ease;
}

.category-item:hover {
  transform: translateY(-5px);
}

.category-image-container {
  width: 100%;
  border-radius: 50%;
  overflow: hidden;
  margin-bottom: 10px;
  aspect-ratio: 1 / 1;
}

.category-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.category-name {
  font-weight: bold;
  text-align: center;
  font-size: 16px;
}

/* 이벤트 슬라이더 스타일 */
.event-slider {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.event-card {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
  cursor: pointer;
}

.event-card:hover {
  transform: translateY(-5px);
}

.event-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.event-info {
  padding: 15px;
}

.event-title {
  font-weight: bold;
  font-size: 18px;
  margin-bottom: 5px;
}

.event-period {
  color: #666;
  font-size: 14px;
}

/* 지역 탭 스타일 */
.region-tabs {
  display: flex;
  overflow-x: auto;
  margin-bottom: 20px;
  padding-bottom: 5px;
}

.region-tab {
  padding: 8px 20px;
  margin-right: 10px;
  background-color: white;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.2s;
}

.region-tab.active {
  background-color: var(--yanolja-red);
  color: white;
  border-color: var(--yanolja-red);
}

.region-tab:hover:not(.active) {
  background-color: #f5f5f5;
}

/* 지역별 인기 숙소 인라인 스타일 */
.region-hotel-list {
  display: flex;
  overflow-x: auto;
  gap: 15px;
  padding: 5px 0;
  scrollbar-width: thin;
}

.region-hotel-item {
  flex: 0 0 auto;
  width: 200px;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
  background-color: white;
}

.region-hotel-item:hover {
  transform: translateY(-5px);
}

.hotel-image {
  width: 100%;
  height: 150px;
  overflow: hidden;
}

.hotel-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hotel-info {
  padding: 10px;
}

.hotel-rating {
  font-size: 14px;
  margin-bottom: 5px;
}

.hotel-rating i {
  color: #ffb700;
  margin-right: 3px;
}

.rating-count {
  color: #666;
  font-size: 12px;
}

.hotel-price {
  font-weight: bold;
  color: var(--yanolja-red);
  font-size: 16px;
}

/* 목적지 그리드 스타일 */
.destination-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.destination-card {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
  cursor: pointer;
}

.destination-card:hover {
  transform: translateY(-5px);
}

.destination-image {
  width: 100%;
  height: 150px;
  object-fit: cover;
}

.destination-name {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.6);
  color: white;
  padding: 10px;
  font-weight: bold;
  text-align: center;
}

/* 사용자 메뉴 그리드 스타일 */
.user-menu-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

.user-menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  text-decoration: none;
  color: #333;
}

.user-menu-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0,0,0,0.1);
  color: var(--yanolja-red);
}

.user-menu-item i {
  font-size: 24px;
  margin-bottom: 10px;
}

/* 반응형 스타일 */
@media (max-width: 768px) {
  .main-banner {
    height: 300px;
  }

  .banner-image {
    height: 100%;
  }

  .category-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .event-slider {
    grid-template-columns: 1fr;
  }

  .region-tabs {
    padding-bottom: 10px;
  }

  .region-tab {
    padding: 6px 15px;
    font-size: 13px;
  }

  .region-hotel-list {
    gap: 10px;
  }

  .region-hotel-item {
    width: 160px;
  }

  .hotel-image {
    height: 120px;
  }
}

/* 기존 스타일 유지 */
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

.card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  background-color: white;
  transition: transform 0.3s ease-in-out, box-shadow 0.3s ease-in-out;
}

.card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
}

.card-body {
  padding: 15px;
}

.card-title {
  font-weight: bold;
  margin-bottom: 5px;
  font-size: 1.1rem;
}

.card-text {
  font-size: 0.9rem;
  color: var(--yanolja-dark-gray);
  margin-bottom: 10px;
}

.btn {
  margin-top: 10px;
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

.section-title {
  font-weight: bold;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading {
  text-align: center;
  padding: 20px;
}
</style>
