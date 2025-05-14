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
    async fetchAccommodations(params) {
      this.loading = true;
      this.error = null;

      try {
        // URL 쿼리 파라미터 구성
        const queryParams = new URLSearchParams();

        // params 객체의 모든 속성을 쿼리 파라미터로 추가
        if (params) {
          Object.keys(params).forEach(key => {
            if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
              queryParams.append(key, params[key]);
            }
          });
        }

        // API 호출
        const response = await fetch(`/api/accommodations?${queryParams.toString()}`);

        // 응답이 JSON이 아닌 경우 처리
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
          throw new Error('서버에서 JSON 형식의 응답이 오지 않았습니다.');
        }

        if (!response.ok) {
          throw new Error(`HTTP 오류! 상태 코드: ${response.status}`);
        }

        const data = await response.json();

        // 응답 데이터 유효성 검사
        if (!data) {
          throw new Error('서버에서 유효하지 않은 응답이 반환되었습니다.');
        }

        // 데이터 저장
        this.accommodations = data.content || [];
        this.filteredAccommodations = [...this.accommodations];

        // 로컬 스토리지에서 찜 목록 로드
        this.loadFavoritesFromStorage();

        // 페이지네이션 정보가 포함된 응답 반환
        return {
          content: data.content || [],
          totalPages: data.totalPages || 1,
          number: data.number || 0,
          totalElements: data.totalElements || 0,
          size: data.size || 10
        };
      } catch (error) {
        this.error = '숙소 정보를 불러오는 중 오류가 발생했습니다.';
        console.error('숙소 정보 로드 중 오류:', error);
        // 오류 발생 시 빈 결과 반환
        return {
          content: [],
          totalPages: 0,
          number: 0,
          totalElements: 0,
          size: 10
        };
      } finally {
        this.loading = false;
      }
    },

    // 특정 숙소 상세 정보 가져오기
    async fetchAccommodationDetails(id) {
      this.loading = true;
      this.error = null;

      try {
        // API 호출로 숙소 상세 정보 가져오기
        const response = await fetch(`/api/accommodations/${id}`);

        // 응답이 JSON이 아닌 경우 처리
        const contentType = response.headers.get('content-type');
        if (!contentType || !contentType.includes('application/json')) {
          throw new Error('서버에서 JSON 형식의 응답이 오지 않았습니다.');
        }

        if (!response.ok) {
          throw new Error(`HTTP 오류! 상태 코드: ${response.status}`);
        }

        const accommodation = await response.json();

        // 응답 데이터 유효성 검사
        if (!accommodation) {
          throw new Error('서버에서 유효하지 않은 응답이 반환되었습니다.');
        }

        this.currentAccommodation = accommodation;
      } catch (error) {
        this.error = error.message || '숙소 상세 정보를 불러오는 중 오류가 발생했습니다.';
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
