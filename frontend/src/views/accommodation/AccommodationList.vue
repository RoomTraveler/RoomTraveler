<template>
  <!-- Navbar -->
  <nav class="navbar navbar-expand-lg navbar-light navbar-yanolja">
    <div class="container">
      <a class="navbar-brand" href="#">방구석 여행자</a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarNav">
        <ul class="navbar-nav ms-auto">
          <li class="nav-item" v-if="!userStore.isLoggedIn">
            <router-link class="nav-link" to="/user/login">로그인</router-link>
          </li>
          <li class="nav-item" v-if="!userStore.isLoggedIn">
            <router-link class="nav-link" to="/user/register">회원가입</router-link>
          </li>
          <li class="nav-item" v-if="userStore.isLoggedIn">
            <router-link class="nav-link" to="/user/profile">{{ userStore.username }}님</router-link>
          </li>
          <li class="nav-item" v-if="userStore.isLoggedIn">
            <a class="nav-link" href="#" @click.prevent="logout">로그아웃</a>
          </li>
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
      <!-- 외부 이미지 URL 대신 로컬 이미지 또는 신뢰할 수 있는 CDN 사용 -->
      <img src="@/assets/promotion-banner.jpg" alt="프로모션 배너" @error="handleImageError">
    </div>

    <!-- 숙소 목록 -->
    <div class="row">
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
        <div class="card accommodation-card" @click="openAccommodationModal(accommodation)">
          <div v-if="accommodation.promotion" class="promotion-badge">특가</div>
          <img 
            :src="accommodation.mainImageUrl || require('@/assets/no-image.jpg')" 
            class="card-img-top" 
            :alt="accommodation.title"
          >
          <div class="card-body">
            <div class="accommodation-type">{{ accommodation.type || '숙소' }}</div>
            <h5 class="accommodation-title">{{ accommodation.title }}</h5>
            <p class="accommodation-location">{{ accommodation.sidoName }} {{ accommodation.gugunName }}</p>
            <div class="d-flex justify-content-between align-items-center">
              <div class="accommodation-price">
                {{ formatPrice(accommodation.price) }}
                <span class="price-unit">원</span>
              </div>
              <div v-if="accommodation.rating" class="d-flex align-items-center">
                <span class="rating">{{ accommodation.rating.toFixed(1) }}</span>
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

  <!-- 숙소 상세 모달 -->
  <div class="modal" :class="{ 'show': showModal }" v-if="showModal" @click="closeModalOnOutsideClick">
    <div class="modal-content">
      <span class="close" @click="closeModal">&times;</span>
      <div id="modalContent" v-if="selectedAccommodation">
        <h2 id="modalTitle">{{ selectedAccommodation.title }}</h2>
        <div id="modalBasicInfo">
          <p>{{ selectedAccommodation.address }}</p>
          <p v-if="selectedAccommodation.rating">
            평점: {{ selectedAccommodation.rating.toFixed(1) }} 
            ({{ selectedAccommodation.reviewCount || 0 }}개의 리뷰)
          </p>
        </div>

        <div class="tab-container">
          <div class="tab">
            <button 
              class="tablinks" 
              :class="{ 'active': activeTab === 'roomInfo' }" 
              @click="setActiveTab('roomInfo')"
            >
              객실 정보
            </button>
            <button 
              class="tablinks" 
              :class="{ 'active': activeTab === 'facilityInfo' }" 
              @click="setActiveTab('facilityInfo')"
            >
              시설 정보
            </button>
            <button 
              class="tablinks" 
              :class="{ 'active': activeTab === 'imageInfo' }" 
              @click="setActiveTab('imageInfo')"
            >
              이미지
            </button>
          </div>

          <div id="roomInfo" class="tabcontent" v-show="activeTab === 'roomInfo'">
            <div class="room-list">
              <div class="room-card" v-for="(room, index) in selectedAccommodation.rooms || []" :key="index">
                <h4>{{ room.name }}</h4>
                <img :src="room.imageUrl || require('@/assets/no-image.jpg')" :alt="room.name">
                <div class="room-details">
                  <p>가격: {{ formatPrice(room.price) }}원</p>
                  <p>최대 인원: {{ room.maxOccupancy }}명</p>
                  <p>객실 크기: {{ room.size }}㎡</p>
                  <div class="room-facilities">
                    <span class="facility" v-for="(facility, i) in room.facilities || []" :key="i">
                      {{ facility }}
                    </span>
                  </div>
                </div>
              </div>
              <div v-if="!selectedAccommodation.rooms || selectedAccommodation.rooms.length === 0">
                <p>객실 정보가 없습니다.</p>
              </div>
            </div>
          </div>

          <div id="facilityInfo" class="tabcontent" v-show="activeTab === 'facilityInfo'">
            <div id="facilityDetails">
              <div v-if="selectedAccommodation.facilities && selectedAccommodation.facilities.length > 0">
                <ul>
                  <li v-for="(facility, index) in selectedAccommodation.facilities" :key="index">
                    {{ facility }}
                  </li>
                </ul>
              </div>
              <div v-else>
                <p>시설 정보가 없습니다.</p>
              </div>
            </div>
          </div>

          <div id="imageInfo" class="tabcontent" v-show="activeTab === 'imageInfo'">
            <div class="image-gallery">
              <img 
                v-for="(image, index) in selectedAccommodation.images || []" 
                :key="index" 
                :src="image.url || require('@/assets/no-image.jpg')" 
                :alt="`${selectedAccommodation.title} 이미지 ${index + 1}`"
                @click="openFullImage(image.url)"
              >
              <div v-if="!selectedAccommodation.images || selectedAccommodation.images.length === 0">
                <p>이미지가 없습니다.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 하단 네비게이션 -->
  <div class="bottom-nav">
    <div class="container">
      <div class="row">
        <div class="col-3">
          <router-link to="/" class="bottom-nav-item" :class="{ 'active': $route.path === '/' }">
            <div class="bottom-nav-icon"><i class="bi bi-house"></i></div>
            <div class="bottom-nav-text">홈</div>
          </router-link>
        </div>
        <div class="col-3">
          <router-link to="/search" class="bottom-nav-item" :class="{ 'active': $route.path.includes('/search') }">
            <div class="bottom-nav-icon"><i class="bi bi-search"></i></div>
            <div class="bottom-nav-text">검색</div>
          </router-link>
        </div>
        <div class="col-3">
          <router-link to="/favorites" class="bottom-nav-item" :class="{ 'active': $route.path.includes('/favorites') }">
            <div class="bottom-nav-icon"><i class="bi bi-heart"></i></div>
            <div class="bottom-nav-text">찜</div>
          </router-link>
        </div>
        <div class="col-3">
          <router-link to="/user/profile" class="bottom-nav-item" :class="{ 'active': $route.path.includes('/user/profile') }">
            <div class="bottom-nav-icon"><i class="bi bi-person"></i></div>
            <div class="bottom-nav-text">마이</div>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useAccommodationStore } from '@/store/accommodationStore';
import { useUserStore } from '@/store/userStore';
import Layout from '@/components/layout/Layout.vue';

export default {
  name: 'AccommodationList',
  components: {
    Layout
  },
  data() {
    return {
      accommodationStore: useAccommodationStore(),
      userStore: useUserStore(),
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
      ],
      // 모달 관련 데이터
      showModal: false,
      selectedAccommodation: null,
      activeTab: 'roomInfo'
    };
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
    /**
     * 시도 목록 로드
     * 
     * 문제 해결:
     * 1. 서버에서 HTML 응답이 오는 경우 JSON 파싱 오류 발생 -> content-type 확인 로직 추가
     * 2. 오류 메시지를 한글로 표시하여 디버깅 용이성 향상
     * 3. 샘플 데이터 제거 - 항상 DB에서 데이터를 가져오도록 수정
     */
    async loadSidos() {
      try {
        // 백엔드 API에서 시도 목록 가져오기
        const response = await fetch('/accommodation/api/sidos');

        // 응답이 JSON이 아닌 경우(HTML 등) 처리
        // 서버가 HTML 에러 페이지를 반환하는 경우 JSON 파싱 오류 방지
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
          throw new Error('서버에서 JSON 형식의 응답이 오지 않았습니다. HTML이 반환되었을 수 있습니다.');
        }

        if (!response.ok) {
          throw new Error(`HTTP 오류! 상태 코드: ${response.status}`);
        }

        this.sidos = await response.json();

        // 시도가 선택되어 있으면 구군 목록 로드
        if (this.searchParams.sidoCode) {
          this.loadGuguns();
        }
      } catch (error) {
        console.error('시도 목록을 불러오는 중 오류가 발생했습니다:', error);
        // 오류 발생 시 빈 배열 설정 (샘플 데이터 사용하지 않음)
        this.sidos = [];
        alert('시도 목록을 불러오는데 실패했습니다. 네트워크 연결을 확인하고 다시 시도해주세요.');
      }
    },

    /**
     * 구군 목록 로드
     * 
     * 문제 해결:
     * 1. 서버에서 HTML 응답이 오는 경우 JSON 파싱 오류 발생 -> content-type 확인 로직 추가
     * 2. 오류 메시지를 한글로 표시하여 디버깅 용이성 향상
     * 3. 샘플 데이터 제거 - 항상 DB에서 데이터를 가져오도록 수정
     */
    async loadGuguns() {
      if (!this.searchParams.sidoCode) {
        this.guguns = [];
        this.searchParams.gugunCode = '';
        return;
      }

      try {
        // 백엔드 API에서 구군 목록 가져오기
        const response = await fetch(`/accommodation/api/guguns?sido=${this.searchParams.sidoCode}`);

        // 응답이 JSON이 아닌 경우(HTML 등) 처리
        // 서버가 HTML 에러 페이지를 반환하는 경우 JSON 파싱 오류 방지
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
          throw new Error('서버에서 JSON 형식의 응답이 오지 않았습니다. HTML이 반환되었을 수 있습니다.');
        }

        if (!response.ok) {
          throw new Error(`HTTP 오류! 상태 코드: ${response.status}`);
        }

        this.guguns = await response.json();
      } catch (error) {
        console.error('구군 목록을 불러오는 중 오류가 발생했습니다:', error);

        // 오류 발생 시 빈 배열 설정 (샘플 데이터 사용하지 않음)
        this.guguns = [];
        alert('구군 목록을 불러오는데 실패했습니다. 네트워크 연결을 확인하고 다시 시도해주세요.');
      }
    },

    /**
     * 숙소 검색
     * 
     * 문제 해결:
     * 1. API 응답이 예상과 다른 형식일 때 발생하는 오류 처리
     * 2. 'Cannot read properties of undefined (reading 'content')' 오류 방지
     * 3. 오류 발생 시에도 UI가 깨지지 않도록 빈 결과 제공
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

        // Pinia 스토어 액션 호출
        const result = await this.accommodationStore.fetchAccommodations(params);

        // 응답 데이터 유효성 검사
        if (!result || typeof result !== 'object') {
          throw new Error('서버에서 유효하지 않은 응답 형식이 반환되었습니다.');
        }

        this.accommodations = result.content || [];
        this.totalPages = result.totalPages || 0;
        this.currentPage = (result.number || 0) + 1;
      } catch (error) {
        console.error('숙소 검색 중 오류가 발생했습니다:', error);
        // 오류 발생 시 빈 결과 설정
        this.accommodations = [];
        this.totalPages = 0;
        this.currentPage = 1;
      } finally {
        this.loading = false;
      }
    },

    // 카테고리 선택
    selectCategory(categoryId) {
      if (this.selectedCategory === categoryId) {
        this.selectedCategory = null;
      } else {
        this.selectedCategory = categoryId;
      }
      this.searchParams.page = 1;
      this.searchAccommodations();
    },

    // 페이지 이동
    goToPage(page) {
      this.searchParams.page = page;
      this.searchAccommodations();
    },

    // URL 쿼리 파라미터 업데이트
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
    },

    // 가격 포맷팅 (1000 -> 1,000)
    formatPrice(price) {
      if (!price) return '0';
      return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    },

    // 숙소 모달 열기
    openAccommodationModal(accommodation) {
      this.selectedAccommodation = accommodation;
      this.activeTab = 'roomInfo';
      this.showModal = true;

      // 객실 정보 로드 (실제 구현 시 API 호출 필요)
      if (!this.selectedAccommodation.rooms) {
        // 예시 데이터
        this.selectedAccommodation.rooms = [
          {
            name: '스탠다드 더블',
            imageUrl: 'https://via.placeholder.com/300x200?text=스탠다드+더블',
            price: 120000,
            maxOccupancy: 2,
            size: 24,
            facilities: ['무료 Wi-Fi', '에어컨', '냉장고', 'TV', '욕실용품']
          },
          {
            name: '디럭스 트윈',
            imageUrl: 'https://via.placeholder.com/300x200?text=디럭스+트윈',
            price: 150000,
            maxOccupancy: 3,
            size: 32,
            facilities: ['무료 Wi-Fi', '에어컨', '냉장고', 'TV', '욕실용품', '미니바']
          }
        ];
      }

      // 시설 정보 로드
      if (!this.selectedAccommodation.facilities) {
        this.selectedAccommodation.facilities = [
          '무료 Wi-Fi', '주차장', '수영장', '피트니스 센터', '레스토랑', '바/라운지', '컨시어지 서비스'
        ];
      }

      // 이미지 로드
      if (!this.selectedAccommodation.images) {
        this.selectedAccommodation.images = [
          { url: this.selectedAccommodation.mainImageUrl || 'https://via.placeholder.com/300x200?text=이미지1' },
          { url: 'https://via.placeholder.com/300x200?text=이미지2' },
          { url: 'https://via.placeholder.com/300x200?text=이미지3' }
        ];
      }
    },

    // 모달 닫기
    closeModal() {
      this.showModal = false;
      this.selectedAccommodation = null;
    },

    // 모달 외부 클릭 시 닫기
    closeModalOnOutsideClick(event) {
      if (event.target.className === 'modal show') {
        this.closeModal();
      }
    },

    // 탭 전환
    setActiveTab(tabName) {
      this.activeTab = tabName;
    },

    // 이미지 전체 화면으로 보기
    openFullImage(imageUrl) {
      if (!imageUrl) return;
      window.open(imageUrl, '_blank');
    },

    // 로그아웃
    logout() {
      this.userStore.logout();
      this.$router.push('/');
    },

    /**
     * 이미지 로드 오류 처리
     * 
     * 문제 해결:
     * 1. 외부 이미지 URL(via.placeholder.com)이 로드되지 않는 문제 해결
     * 2. 네트워크 오류(ERR_NAME_NOT_RESOLVED)가 발생해도 UI가 깨지지 않도록 함
     * 3. 인라인 데이터 URL로 대체하여 안정적인 사용자 경험 제공
     * 
     * @param {Event} event - 이미지 오류 이벤트
     */
    handleImageError(event) {
      // 이미지 로드 실패 시 인라인 데이터 URL로 대체
      // 간단한 빨간색 배경의 배너 이미지 (데이터 URL 형식)
      event.target.src = 'data:image/svg+xml;charset=UTF-8,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22800%22%20height%3D%22200%22%20viewBox%3D%220%200%20800%20200%22%20preserveAspectRatio%3D%22none%22%3E%3Crect%20width%3D%22800%22%20height%3D%22200%22%20fill%3D%22%23f0213b%22%3E%3C%2Frect%3E%3Ctext%20x%3D%22400%22%20y%3D%22100%22%20font-family%3D%22Arial%2C%20sans-serif%22%20font-size%3D%2236%22%20font-weight%3D%22bold%22%20text-anchor%3D%22middle%22%20alignment-baseline%3D%22middle%22%20fill%3D%22white%22%3E%ED%8A%B9%EA%B0%80%20%ED%94%84%EB%A1%9C%EB%AA%A8%EC%85%98%3C%2Ftext%3E%3C%2Fsvg%3E';
      console.warn('이미지 로드에 실패하여 기본 이미지로 대체되었습니다.');
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
  transition: all 0.3s ease;
  cursor: pointer;
}

.category-icon.active {
  background-color: var(--yanolja-red);
  color: white;
}

.category-item {
  text-align: center;
  margin-bottom: 15px;
  cursor: pointer;
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
  cursor: pointer;
  position: relative;
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
  color: var(--yanolja-dark-gray);
  text-decoration: none;
  display: block;
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
  margin-bottom: 70px; /* Space for bottom nav */
}

.modal {
  display: none;
  position: fixed;
  z-index: 1000;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  overflow: auto;
  background-color: rgba(0,0,0,0.4);
}

.modal.show {
  display: block;
}

.modal-content {
  background-color: #fefefe;
  margin: 5% auto;
  padding: 20px;
  border: 1px solid #888;
  width: 80%;
  max-width: 1000px;
  border-radius: 5px;
}

.close {
  color: #aaa;
  float: right;
  font-size: 28px;
  font-weight: bold;
  cursor: pointer;
}

.close:hover,
.close:focus {
  color: black;
  text-decoration: none;
  cursor: pointer;
}

.room-list {
  margin-top: 20px;
}

.room-card {
  border: 1px solid #ddd;
  border-radius: 5px;
  padding: 15px;
  margin-bottom: 15px;
}

.room-card h4 {
  margin-top: 0;
  margin-bottom: 10px;
}

.room-card img {
  max-width: 300px;
  max-height: 200px;
  margin-right: 15px;
  float: left;
}

.room-details {
  overflow: hidden;
}

.room-details p {
  margin: 5px 0;
}

.room-facilities {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.facility {
  background-color: #f0f0f0;
  padding: 3px 8px;
  border-radius: 3px;
  font-size: 0.9em;
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

.loading {
  text-align: center;
  padding: 20px;
  display: none;
}

.tab-container {
  margin-top: 20px;
}

.tab {
  overflow: hidden;
  border: 1px solid #ccc;
  background-color: #f1f1f1;
  border-radius: 10px 10px 0 0;
}

.tab button {
  background-color: inherit;
  float: left;
  border: none;
  outline: none;
  cursor: pointer;
  padding: 10px 16px;
  transition: 0.3s;
}

.tab button:hover {
  background-color: #ddd;
}

.tab button.active {
  background-color: var(--yanolja-red);
  color: white;
}

.tabcontent {
  display: none;
  padding: 20px;
  border: 1px solid #ccc;
  border-top: none;
  border-radius: 0 0 10px 10px;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.image-gallery img {
  width: 200px;
  height: 150px;
  object-fit: cover;
  cursor: pointer;
  border-radius: 5px;
}
</style>
