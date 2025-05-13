import { defineStore } from 'pinia';

// 숙소 스토어 정의
export const useAccommodationStore = defineStore('accommodation', {
  // 상태(state)
  state: () => ({
    accommodations: [],
    filteredAccommodations: [],
    currentAccommodation: null,
    favorites: [],
    searchQuery: '',
    filters: {
      location: '',
      priceMin: null,
      priceMax: null,
      sortBy: 'recommended'
    },
    loading: false,
    error: null
  }),

  // 게터(getters)
  getters: {
    // 추천 숙소 (최대 3개)
    featuredAccommodations: (state) => {
      return state.accommodations
        .filter(acc => acc.rating >= 4)
        .slice(0, 3);
    },
    
    // 특정 숙소 정보 가져오기
    getAccommodationById: (state) => (id) => {
      return state.accommodations.find(acc => acc.id === parseInt(id));
    },
    
    // 찜한 숙소 목록
    favoriteAccommodations: (state) => {
      return state.accommodations.filter(acc => 
        state.favorites.includes(acc.id)
      );
    },
    
    // 숙소가 찜 목록에 있는지 확인
    isFavorite: (state) => (id) => {
      return state.favorites.includes(parseInt(id));
    }
  },

  // 액션(actions)
  actions: {
    // 숙소 목록 가져오기
    async fetchAccommodations() {
      this.loading = true;
      this.error = null;
      
      try {
        // 실제 API 호출 대신 임시 데이터 사용
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // 샘플 데이터
        const data = [
          {
            id: 1,
            name: '서울 시티 호텔',
            location: '서울 강남구',
            price: 120000,
            rating: 4.5,
            reviewCount: 32,
            imageUrl: 'https://via.placeholder.com/300x200?text=서울+시티+호텔',
            description: '강남역 인근에 위치한 현대적인 호텔입니다. 비즈니스 및 관광에 최적의 위치를 자랑합니다.',
            amenities: ['무료 Wi-Fi', '에어컨', '헤어 드라이어', '냉장고', '전자레인지', '욕조', '주차장']
          },
          {
            id: 2,
            name: '부산 오션 리조트',
            location: '부산 해운대구',
            price: 150000,
            rating: 4.7,
            reviewCount: 48,
            imageUrl: 'https://via.placeholder.com/300x200?text=부산+오션+리조트',
            description: '해운대 해변이 보이는 아름다운 리조트입니다. 바다 전망과 함께 편안한 휴식을 즐기세요.',
            amenities: ['무료 Wi-Fi', '에어컨', '헤어 드라이어', '냉장고', '수영장', '헬스장', '주차장']
          },
          {
            id: 3,
            name: '제주 풀빌라',
            location: '제주 서귀포시',
            price: 200000,
            rating: 4.9,
            reviewCount: 56,
            imageUrl: 'https://via.placeholder.com/300x200?text=제주+풀빌라',
            description: '제주의 아름다운 자연 속에 위치한 프라이빗 풀빌라입니다. 완벽한 휴식을 위한 최고의 선택입니다.',
            amenities: ['무료 Wi-Fi', '에어컨', '헤어 드라이어', '냉장고', '개인 수영장', '바베큐', '주차장']
          },
          {
            id: 4,
            name: '서울 강남 레지던스',
            location: '서울 강남구',
            price: 130000,
            rating: 4.2,
            reviewCount: 28,
            imageUrl: 'https://via.placeholder.com/300x200?text=서울+강남+레지던스',
            description: '강남 중심부에 위치한 현대적인 레지던스입니다. 장기 투숙에 적합한 시설을 갖추고 있습니다.',
            amenities: ['무료 Wi-Fi', '에어컨', '헤어 드라이어', '냉장고', '세탁기', '주방', '주차장']
          },
          {
            id: 5,
            name: '부산 해변 호텔',
            location: '부산 해운대구',
            price: 140000,
            rating: 4.3,
            reviewCount: 35,
            imageUrl: 'https://via.placeholder.com/300x200?text=부산+해변+호텔',
            description: '해운대 해변에서 도보 5분 거리에 위치한 편안한 호텔입니다. 해변 산책을 즐기기에 최적의 위치입니다.',
            amenities: ['무료 Wi-Fi', '에어컨', '헤어 드라이어', '냉장고', '카페', '주차장']
          },
          {
            id: 6,
            name: '제주 바다뷰 펜션',
            location: '제주 서귀포시',
            price: 180000,
            rating: 4.6,
            reviewCount: 42,
            imageUrl: 'https://via.placeholder.com/300x200?text=제주+바다뷰+펜션',
            description: '서귀포 바다가 한눈에 보이는 전망 좋은 펜션입니다. 가족 여행에 이상적인 공간을 제공합니다.',
            amenities: ['무료 Wi-Fi', '에어컨', '헤어 드라이어', '냉장고', '바베큐', '주차장']
          }
        ];
        
        this.accommodations = data;
        this.filteredAccommodations = [...data];
        
        // 로컬 스토리지에서 찜 목록 로드
        this.loadFavoritesFromStorage();
      } catch (error) {
        this.error = '숙소 정보를 불러오는 중 오류가 발생했습니다.';
        console.error('숙소 정보 로드 중 오류:', error);
      } finally {
        this.loading = false;
      }
    },
    
    // 특정 숙소 상세 정보 가져오기
    async fetchAccommodationDetails(id) {
      this.loading = true;
      this.error = null;
      
      try {
        // 실제 API 호출 대신 임시 로직 사용
        await new Promise(resolve => setTimeout(resolve, 500));
        
        // 이미 로드된 숙소 목록에서 찾기
        const accommodation = this.getAccommodationById(id);
        
        if (accommodation) {
          this.currentAccommodation = accommodation;
        } else {
          throw new Error('숙소를 찾을 수 없습니다.');
        }
      } catch (error) {
        this.error = error.message;
        console.error('숙소 상세 정보 로드 중 오류:', error);
      } finally {
        this.loading = false;
      }
    },
    
    // 숙소 검색 및 필터링
    filterAccommodations() {
      let filtered = [...this.accommodations];
      
      // 검색어 필터링
      if (this.searchQuery) {
        const query = this.searchQuery.toLowerCase();
        filtered = filtered.filter(item => 
          item.name.toLowerCase().includes(query) || 
          item.location.toLowerCase().includes(query) ||
          item.description.toLowerCase().includes(query)
        );
      }
      
      // 위치 필터링
      if (this.filters.location) {
        filtered = filtered.filter(item => 
          item.location.includes(this.filters.location)
        );
      }
      
      // 가격 범위 필터링
      if (this.filters.priceMin !== null) {
        filtered = filtered.filter(item => item.price >= this.filters.priceMin);
      }
      
      if (this.filters.priceMax !== null) {
        filtered = filtered.filter(item => item.price <= this.filters.priceMax);
      }
      
      // 정렬
      switch (this.filters.sortBy) {
        case 'price-low':
          filtered.sort((a, b) => a.price - b.price);
          break;
        case 'price-high':
          filtered.sort((a, b) => b.price - a.price);
          break;
        case 'rating':
          filtered.sort((a, b) => b.rating - a.rating);
          break;
        case 'recommended':
        default:
          // 기본 정렬은 추천순 (평점 * 리뷰 수)
          filtered.sort((a, b) => (b.rating * b.reviewCount) - (a.rating * a.reviewCount));
          break;
      }
      
      this.filteredAccommodations = filtered;
    },
    
    // 검색어 설정
    setSearchQuery(query) {
      this.searchQuery = query;
      this.filterAccommodations();
    },
    
    // 필터 설정
    setFilter(filterName, value) {
      this.filters[filterName] = value;
      this.filterAccommodations();
    },
    
    // 찜하기 토글
    toggleFavorite(id) {
      id = parseInt(id);
      const index = this.favorites.indexOf(id);
      
      if (index === -1) {
        // 찜 목록에 추가
        this.favorites.push(id);
      } else {
        // 찜 목록에서 제거
        this.favorites.splice(index, 1);
      }
      
      // 로컬 스토리지에 저장
      localStorage.setItem('favorites', JSON.stringify(this.favorites));
    },
    
    // 로컬 스토리지에서 찜 목록 로드
    loadFavoritesFromStorage() {
      try {
        const storedFavorites = localStorage.getItem('favorites');
        if (storedFavorites) {
          this.favorites = JSON.parse(storedFavorites);
        }
      } catch (error) {
        console.error('찜 목록 로드 중 오류 발생:', error);
        localStorage.removeItem('favorites');
        this.favorites = [];
      }
    }
  }
});