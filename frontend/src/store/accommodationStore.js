import { defineStore } from "pinia";
import apiUtils from "@/api"; // apiUtils import

// 숙소 스토어 정의
export const useAccommodationStore = defineStore("accommodation", {
  // 상태(state)
  state: () => ({
    accommodations: [],
    filteredAccommodations: [],
    currentAccommodation: null,
    favorites: [],
    searchQuery: "",
    filters: {
      location: "",
      priceMin: null,
      priceMax: null,
      sortBy: "recommended",
    },
    loading: false,
    error: null,
  }),

  // 게터(getters)
  getters: {
    // 추천 숙소 (최대 3개)
    featuredAccommodations: (state) => {
      return state.accommodations.filter((acc) => acc.rating >= 4).slice(0, 3);
    },

    // 특정 숙소 정보 가져오기
    getAccommodationById: (state) => (id) => {
      return state.accommodations.find((acc) => acc.id === parseInt(id));
    },

    // 찜한 숙소 목록
    favoriteAccommodations: (state) => {
      return state.accommodations.filter((acc) => state.favorites.includes(acc.id));
    },

    // 숙소가 찜 목록에 있는지 확인
    isFavorite: (state) => (id) => {
      return state.favorites.includes(parseInt(id));
    },
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
          Object.keys(params).forEach((key) => {
            if (params[key] !== null && params[key] !== undefined && params[key] !== "") {
              queryParams.append(key, params[key]);
            }
          });
        }

        // API 호출
        const response = await fetch(`/api/accommodations?${queryParams.toString()}`);

        // 응답이 JSON이 아닌 경우 처리
        const contentType = response.headers.get("content-type");
        if (!contentType || !contentType.includes("application/json")) {
          throw new Error("서버에서 JSON 형식의 응답이 오지 않았습니다.");
        }

        if (!response.ok) {
          throw new Error(`HTTP 오류! 상태 코드: ${response.status}`);
        }

        const data = await response.json();

        // 응답 데이터 유효성 검사
        if (!data) {
          throw new Error("서버에서 유효하지 않은 응답이 반환되었습니다.");
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
          size: data.size || 10,
        };
      } catch (error) {
        this.error = "숙소 정보를 불러오는 중 오류가 발생했습니다.";
        console.error("숙소 정보 로드 중 오류:", error);
        // 오류 발생 시 빈 결과 반환
        return {
          content: [],
          totalPages: 0,
          number: 0,
          totalElements: 0,
          size: 10,
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
        const contentType = response.headers.get("content-type");
        if (!contentType || !contentType.includes("application/json")) {
          throw new Error("서버에서 JSON 형식의 응답이 오지 않았습니다.");
        }

        if (!response.ok) {
          throw new Error(`HTTP 오류! 상태 코드: ${response.status}`);
        }

        const accommodation = await response.json();

        // 응답 데이터 유효성 검사
        if (!accommodation) {
          throw new Error("서버에서 유효하지 않은 응답이 반환되었습니다.");
        }

        this.currentAccommodation = accommodation;
      } catch (error) {
        this.error = error.message || "숙소 상세 정보를 불러오는 중 오류가 발생했습니다.";
        console.error("숙소 상세 정보 로드 중 오류:", error);
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
        filtered = filtered.filter(
          (item) =>
            item.name.toLowerCase().includes(query) ||
            item.location.toLowerCase().includes(query) ||
            item.description.toLowerCase().includes(query)
        );
      }

      // 위치 필터링
      if (this.filters.location) {
        filtered = filtered.filter((item) => item.location.includes(this.filters.location));
      }

      // 가격 범위 필터링
      if (this.filters.priceMin !== null) {
        filtered = filtered.filter((item) => item.price >= this.filters.priceMin);
      }

      if (this.filters.priceMax !== null) {
        filtered = filtered.filter((item) => item.price <= this.filters.priceMax);
      }

      // 정렬
      switch (this.filters.sortBy) {
        case "price-low":
          filtered.sort((a, b) => a.price - b.price);
          break;
        case "price-high":
          filtered.sort((a, b) => b.price - a.price);
          break;
        case "rating":
          filtered.sort((a, b) => b.rating - a.rating);
          break;
        case "recommended":
        default:
          // 기본 정렬은 추천순 (평점 * 리뷰 수)
          filtered.sort((a, b) => b.rating * b.reviewCount - a.rating * a.reviewCount);
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

    // 찜하기 토글 (로컬 상태 및 localStorage만 업데이트)
    toggleFavoriteLocal(accommodationId) {
      if (!accommodationId) {
        console.error("toggleFavoriteLocal: accommodationId is missing");
        this.error = "찜 처리 중 로컬 오류가 발생했습니다 (숙소 ID 누락)."; // 사용자에게 보이지 않을 수 있음
        return;
      }
      const id = parseInt(String(accommodationId));

      const index = this.favorites.indexOf(id);
      if (index === -1) {
        this.favorites.push(id);
        console.log(`[Store] Accommodation ${id} added to local favorites.`);
      } else {
        this.favorites.splice(index, 1);
        console.log(`[Store] Accommodation ${id} removed from local favorites.`);
      }
      localStorage.setItem("favorites", JSON.stringify(this.favorites));
    },

    // 로컬 스토리지에서 찜 목록 로드 (및 초기 API 동기화)
    async loadFavoritesFromStorage() {
      try {
        const storedFavorites = localStorage.getItem("favorites");
        if (storedFavorites) {
          this.favorites = JSON.parse(storedFavorites);
        }
        // 앱 시작 시 실제 DB와 동기화 (선택적)
        // await this.fetchUserFavorites();
      } catch (error) {
        console.error("찜 목록 로드 중 오류 발생:", error);
        localStorage.removeItem("favorites");
        this.favorites = [];
      }
    },

    // (선택적) 사용자의 실제 찜 목록을 DB에서 가져와 스토어와 로컬 스토리지를 업데이트하는 함수
    async fetchUserFavorites() {
      // 이 함수는 로그인한 사용자의 찜 목록을 /api/favorites (GET)으로 가져와서
      // this.favorites를 업데이트하고 localStorage에도 저장하는 로직을 구현할 수 있습니다.
      // 이렇게 하면 여러 기기에서의 동기화나 로컬 스토리지 불일치 문제를 해결할 수 있습니다.
      this.loading = true;
      this.error = null;
      try {
        const response = await apiUtils.api.get("/api/favorites"); // 사용자의 모든 찜 목록을 가져오는 API (백엔드 응답 확인 필요)
        // 응답이 [ { accommodation: { accommodationId: 123, ... } }, ... ] 형태라고 가정
        this.favorites = response.data.map((fav) => fav.accommodation.accommodationId).filter((id) => id != null);
        localStorage.setItem("favorites", JSON.stringify(this.favorites));
        console.log("[Store] User favorites loaded from DB and updated in store/localStorage.");
      } catch (err) {
        console.error("Failed to fetch user favorites from DB", err);
        this.error =
          err.response?.data?.message || err.message || "데이터베이스에서 찜 목록을 가져오는데 실패했습니다.";
        // DB 로드 실패 시 로컬 스토리지 값을 그대로 사용하거나, 로컬 스토리지를 우선시 할 수 있음
        // 현재는 로컬 스토리지에서 이미 로드된 값을 유지
      } finally {
        this.loading = false;
      }
    },
  },
});
