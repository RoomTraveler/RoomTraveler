<template>
  <div>
    <!-- Navbar -->
    <nav class="navbar navbar-expand-lg navbar-light navbar-yanolja">
      <div class="container">
        <router-link class="navbar-brand" to="/">방구석 여행자</router-link>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
          <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
          <ul class="navbar-nav ms-auto">
            <template v-if="!isLoggedIn">
              <li class="nav-item">
                <router-link class="nav-link" to="/user/login">로그인</router-link>
              </li>
              <li class="nav-item">
                <router-link class="nav-link" to="/user/register">회원가입</router-link>
              </li>
            </template>
            <template v-else>
              <li class="nav-item">
                <router-link class="nav-link" to="/user/profile">{{ username }}님</router-link>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="#" @click.prevent="logout">로그아웃</a>
              </li>
            </template>
          </ul>
        </div>
      </div>
    </nav>

    <div class="container main-content mt-4">
      <!-- 검색 바 -->
      <div class="search-bar">
        <div class="row g-3">
          <div class="col-md-3">
            <label for="sidoCode" class="form-label">지역</label>
            <select class="form-select" id="sidoCode" v-model="searchParams.sidoCode" @change="loadGuguns">
              <option value="">전체</option>
              <option v-for="sido in sidos" :key="sido.code" :value="sido.code">{{ sido.name }}</option>
            </select>
          </div>
          <div class="col-md-3">
            <label for="gugunCode" class="form-label">시군구</label>
            <select class="form-select" id="gugunCode" v-model="searchParams.gugunCode">
              <option value="">전체</option>
              <option v-for="gugun in guguns" :key="gugun.code" :value="gugun.code">{{ gugun.name }}</option>
            </select>
          </div>
          <div class="col-md-3">
            <label for="keyword" class="form-label">검색어</label>
            <input type="text" class="form-control" id="keyword" v-model="searchParams.keyword" placeholder="숙소명, 주소 등">
          </div>
          <div class="col-md-3">
            <label for="sortBy" class="form-label">정렬</label>
            <select class="form-select" id="sortBy" v-model="searchParams.sortBy">
              <option value="createdAt">최신순</option>
              <option value="price">가격순</option>
              <option value="rating">평점순</option>
            </select>
          </div>
          <div class="col-md-3">
            <label for="minPrice" class="form-label">최소 가격</label>
            <input type="number" class="form-control" id="minPrice" v-model="searchParams.minPrice" placeholder="최소 가격">
          </div>
          <div class="col-md-3">
            <label for="maxPrice" class="form-label">최대 가격</label>
            <input type="number" class="form-control" id="maxPrice" v-model="searchParams.maxPrice" placeholder="최대 가격">
          </div>
          <div class="col-md-3 d-flex align-items-end">
            <button type="button" class="btn btn-yanolja w-100" @click="searchAccommodations">검색</button>
          </div>
        </div>
      </div>

      <!-- 카테고리 -->
      <div class="row mb-4">
        <div class="col-12">
          <h5 class="section-title">카테고리</h5>
        </div>
        <div class="col-3" v-for="category in categories" :key="category.id">
          <div class="category-item" @click="selectCategory(category.id)">
            <div class="category-icon" :class="{ 'active': selectedCategory === category.id }">
              <i :class="category.icon"></i>
            </div>
            <div class="category-name">{{ category.name }}</div>
          </div>
        </div>
      </div>

      <!-- 배너 -->
      <div class="banner">
        <img src="https://via.placeholder.com/800x200/f0213b/ffffff?text=특가+프로모션" alt="프로모션 배너">
      </div>

      <!-- 숙소 목록 -->
      <div class="row mt-4">
        <div class="col-12">
          <h5 class="section-title">숙소 목록</h5>
        </div>

        <div v-if="loading" class="col-12 text-center py-5">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">로딩 중...</span>
          </div>
          <p class="mt-2">숙소 정보를 불러오는 중입니다...</p>
        </div>

        <div v-else-if="accommodations.length === 0" class="col-12 text-center py-5">
          <p class="lead">검색 결과가 없습니다.</p>
        </div>

        <div v-else class="col-md-4 mb-4" v-for="accommodation in accommodations" :key="accommodation.accommodationId">
          <div class="card accommodation-card">
            <img 
              :src="accommodation.mainImageUrl || require('@/assets/no-image.jpg')" 
              class="card-img-top" 
              :alt="accommodation.title"
            >
            <div class="card-body">
              <h5 class="card-title">{{ accommodation.title }}</h5>
              <p class="card-text text-muted">{{ accommodation.sidoName }} {{ accommodation.gugunName }}</p>
              <p class="card-text">{{ accommodation.address }}</p>
              <div class="d-flex justify-content-between align-items-center">
                <router-link 
                  :to="`/accommodation/detail/${accommodation.accommodationId}`" 
                  class="btn btn-outline-primary"
                >
                  상세 보기
                </router-link>
                <div v-if="accommodation.rating" class="rating">
                  <i class="bi bi-star-fill"></i> {{ accommodation.rating.toFixed(1) }}
                  <span class="review-count">({{ accommodation.reviewCount || 0 }})</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 페이지네이션 -->
      <div class="pagination mt-4" v-if="totalPages > 1">
        <button 
          v-for="page in totalPages" 
          :key="page" 
          :class="{ 'active': page === currentPage }"
          @click="goToPage(page)"
        >
          {{ page }}
        </button>
      </div>
    </div>

    <!-- 하단 네비게이션 -->
    <div class="bottom-nav">
      <div class="container">
        <div class="row">
          <div class="col-3">
            <div class="bottom-nav-item active">
              <div class="bottom-nav-icon"><i class="bi bi-house"></i></div>
              <div class="bottom-nav-text">홈</div>
            </div>
          </div>
          <div class="col-3">
            <div class="bottom-nav-item">
              <div class="bottom-nav-icon"><i class="bi bi-search"></i></div>
              <div class="bottom-nav-text">검색</div>
            </div>
          </div>
          <div class="col-3">
            <div class="bottom-nav-item">
              <div class="bottom-nav-icon"><i class="bi bi-heart"></i></div>
              <div class="bottom-nav-text">찜</div>
            </div>
          </div>
          <div class="col-3">
            <div class="bottom-nav-item">
              <div class="bottom-nav-icon"><i class="bi bi-person"></i></div>
              <div class="bottom-nav-text">마이</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 숙박 지역 선택 컴포넌트
 * 
 * 이 컴포넌트는 숙박 지역 선택 페이지를 구현합니다.
 * 지역, 시군구, 검색어, 정렬, 가격 범위 등의 검색 조건을 설정하고
 * 숙소 목록을 표시합니다.
 */
import { mapState, mapActions } from 'vuex';

export default {
  name: 'AccommodationIndex',
  data() {
    return {
      searchParams: {
        sidoCode: '',
        gugunCode: '',
        keyword: '',
        sortBy: 'createdAt',
        minPrice: '',
        maxPrice: '',
        page: 1,
        size: 9
      },
      sidos: [],
      guguns: [],
      accommodations: [],
      loading: false,
      totalPages: 0,
      currentPage: 1,
      selectedCategory: null,
      categories: [
        { id: 'hotel', name: '호텔', icon: 'bi bi-building' },
        { id: 'pension', name: '펜션', icon: 'bi bi-house' },
        { id: 'poolvilla', name: '풀빌라', icon: 'bi bi-water' },
        { id: 'motel', name: '모텔', icon: 'bi bi-shop' }
      ]
    };
  },
  computed: {
    ...mapState({
      isLoggedIn: state => state.user.isLoggedIn,
      username: state => state.user.user?.username
    })
  },
  created() {
    // URL 쿼리 파라미터에서 검색 조건 가져오기
    const query = this.$route.query;
    if (query.sidoCode) this.searchParams.sidoCode = query.sidoCode;
    if (query.gugunCode) this.searchParams.gugunCode = query.gugunCode;
    if (query.keyword) this.searchParams.keyword = query.keyword;
    if (query.sortBy) this.searchParams.sortBy = query.sortBy;
    if (query.minPrice) this.searchParams.minPrice = query.minPrice;
    if (query.maxPrice) this.searchParams.maxPrice = query.maxPrice;
    if (query.page) this.searchParams.page = parseInt(query.page);
    if (query.category) this.selectedCategory = query.category;

    // 시도 목록 로드
    this.loadSidos();

    // 숙소 검색
    this.searchAccommodations();
  },
  methods: {
    ...mapActions('user', ['logoutUser']),
    ...mapActions('accommodation', ['fetchAccommodations']),

    /**
     * 로그아웃 처리
     */
    async logout() {
      try {
        await this.logoutUser();
        this.$router.push('/user/login');
      } catch (error) {
        console.error('로그아웃 중 오류가 발생했습니다:', error);
      }
    },

    /**
     * 시도 목록 로드
     */
    async loadSidos() {
      try {
        const response = await fetch('/api/accommodations/sidos');
        this.sidos = await response.json();

        // 시도가 선택되어 있으면 구군 목록 로드
        if (this.searchParams.sidoCode) {
          this.loadGuguns();
        }
      } catch (error) {
        console.error('시도 목록을 불러오는 중 오류가 발생했습니다:', error);
      }
    },

    /**
     * 구군 목록 로드
     */
    async loadGuguns() {
      if (!this.searchParams.sidoCode) {
        this.guguns = [];
        this.searchParams.gugunCode = '';
        return;
      }

      try {
        const response = await fetch(`/api/accommodations/guguns?sido=${this.searchParams.sidoCode}`);
        this.guguns = await response.json();
      } catch (error) {
        console.error('구군 목록을 불러오는 중 오류가 발생했습니다:', error);
      }
    },

    /**
     * 숙소 검색
     */
    async searchAccommodations() {
      this.loading = true;

      try {
        // URL 쿼리 파라미터 업데이트
        this.updateQueryParams();

        // 검색 파라미터 구성
        const params = { ...this.searchParams };
        if (this.selectedCategory) {
          params.category = this.selectedCategory;
        }

        // Vuex 액션 호출
        const result = await this.fetchAccommodations(params);
        this.accommodations = result.content;
        this.totalPages = result.totalPages;
        this.currentPage = result.number + 1;
      } catch (error) {
        console.error('숙소 검색 중 오류가 발생했습니다:', error);
      } finally {
        this.loading = false;
      }
    },

    /**
     * 카테고리 선택
     * @param {string} categoryId - 선택한 카테고리 ID
     */
    selectCategory(categoryId) {
      if (this.selectedCategory === categoryId) {
        this.selectedCategory = null;
      } else {
        this.selectedCategory = categoryId;
      }
      this.searchParams.page = 1;
      this.searchAccommodations();
    },

    /**
     * 페이지 이동
     * @param {number} page - 이동할 페이지 번호
     */
    goToPage(page) {
      this.searchParams.page = page;
      this.searchAccommodations();
    },

    /**
     * URL 쿼리 파라미터 업데이트
     */
    updateQueryParams() {
      const query = {};
      if (this.searchParams.sidoCode) query.sidoCode = this.searchParams.sidoCode;
      if (this.searchParams.gugunCode) query.gugunCode = this.searchParams.gugunCode;
      if (this.searchParams.keyword) query.keyword = this.searchParams.keyword;
      if (this.searchParams.sortBy !== 'createdAt') query.sortBy = this.searchParams.sortBy;
      if (this.searchParams.minPrice) query.minPrice = this.searchParams.minPrice;
      if (this.searchParams.maxPrice) query.maxPrice = this.searchParams.maxPrice;
      if (this.searchParams.page > 1) query.page = this.searchParams.page;
      if (this.selectedCategory) query.category = this.selectedCategory;

      this.$router.replace({ query });
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

.navbar-yanolja {
  background-color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.navbar-brand {
  font-weight: bold;
  color: var(--yanolja-red) !important;
  font-size: 1.5rem;
}

.search-bar {
  background-color: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
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

.category-icon {
  width: 60px;
  height: 60px;
  background-color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 10px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  font-size: 1.5rem;
  color: var(--yanolja-red);
  cursor: pointer;
  transition: background-color 0.3s, color 0.3s;
}

.category-icon.active {
  background-color: var(--yanolja-red);
  color: white;
}

.category-item {
  text-align: center;
  margin-bottom: 15px;
}

.category-name {
  font-size: 0.9rem;
  color: #333;
}

.section-title {
  font-weight: bold;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-title a {
  font-size: 0.9rem;
  color: var(--yanolja-dark-gray);
  text-decoration: none;
}

.accommodation-card {
  border-radius: 10px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
  background-color: white;
  transition: transform 0.3s;
}

.accommodation-card:hover {
  transform: translateY(-5px);
}

.card-img-top {
  height: 180px;
  object-fit: cover;
}

.card-body {
  padding: 15px;
}

.accommodation-type {
  font-size: 0.8rem;
  color: var(--yanolja-dark-gray);
  margin-bottom: 5px;
}

.accommodation-title {
  font-weight: bold;
  margin-bottom: 5px;
  font-size: 1.1rem;
}

.accommodation-location {
  font-size: 0.9rem;
  color: var(--yanolja-dark-gray);
  margin-bottom: 10px;
}

.accommodation-price {
  font-weight: bold;
  color: var(--yanolja-red);
  font-size: 1.2rem;
}

.price-unit {
  font-size: 0.8rem;
  font-weight: normal;
  color: var(--yanolja-dark-gray);
}

.promotion-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background-color: var(--yanolja-red);
  color: white;
  padding: 3px 8px;
  border-radius: 5px;
  font-size: 0.8rem;
  font-weight: bold;
}

.rating {
  color: #ffb700;
  font-weight: bold;
  margin-right: 5px;
}

.review-count {
  font-size: 0.8rem;
  color: var(--yanolja-dark-gray);
}

.banner {
  border-radius: 10px;
  overflow: hidden;
  margin-bottom: 20px;
}

.banner img {
  width: 100%;
  height: auto;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: white;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.1);
  padding: 10px 0;
  z-index: 1000;
}

.bottom-nav-item {
  text-align: center;
  font-size: 0.8rem;
  cursor: pointer;
}

.bottom-nav-icon {
  font-size: 1.5rem;
  margin-bottom: 5px;
  color: var(--yanolja-dark-gray);
}

.bottom-nav-item.active .bottom-nav-icon,
.bottom-nav-item.active .bottom-nav-text {
  color: var(--yanolja-red);
}

.main-content {
  margin-bottom: 70px; /* 하단 네비게이션을 위한 공간 */
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