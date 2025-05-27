<template>
  <div class="bg-light min-vh-100">
    <div class="container py-4 d-flex flex-column align-items-center">
      <!-- 항상 768px 제한 -->
      <div class="w-100" style="max-width: 768px">
        <!-- 헤더 -->
        <AccommodationHeader :accommodationTitle="accommodation?.title || '숙소 상세정보'" />

        <!-- 이미지 스와이퍼 -->
        <div class="my-3" v-if="allAccommodationImages.length > 0 && allAccommodationImages[0]?.imageUrl !== noImage">
          <swiper
            :modules="swiperModules"
            :slides-per-view="1"
            :space-between="10"
            navigation
            :pagination="{ clickable: true }"
            :autoplay="{ delay: 4000, disableOnInteraction: false }"
            loop
            class="rounded shadow-sm accommodation-swiper"
          >
            <swiper-slide v-for="(img, index) in allAccommodationImages" :key="img.imageUrl + '-' + index">
              <img :src="img.imageUrl" alt="숙소 이미지" class="w-100 h-100" />
            </swiper-slide>
          </swiper>
        </div>
        <div
          class="my-3"
          v-else-if="!loading || (allAccommodationImages.length > 0 && allAccommodationImages[0]?.imageUrl === noImage)"
        >
          <img
            :src="noImage"
            alt="이미지 없음"
            class="w-100 rounded shadow-sm"
            style="max-height: 320px; object-fit: contain"
          />
        </div>

        <!-- 숙소 정보 -->
        <div v-if="accommodation" class="card mb-4">
          <div class="card-body">
            <div class="d-flex justify-content-between align-items-start mb-3">
              <h1 class="h4 fw-bold flex-grow-1 me-3 mb-0">{{ accommodation.title || "숙소명 없음" }}</h1>
              <div class="d-flex gap-2">
                <button @click="toggleWishlist" class="btn btn-light border" :disabled="wishlistApiLoading">
                  <i :class="isFavorite ? 'bi-heart-fill text-danger' : 'bi-heart'"></i>
                </button>
                <button @click="shareAccommodation" class="btn btn-light border">
                  <i class="bi bi-share-fill"></i>
                </button>
              </div>
            </div>
            <div class="text-secondary mb-1">
              <i class="bi bi-geo-alt-fill me-1"></i>
              <span>
                {{ accommodation.address || accommodation.sidoName || "위치 정보 없음" }}
                {{ accommodation.gugunName || "" }}
              </span>
            </div>
            <div v-if="reviewCount > 0 && !loadingReviews && !fetchReviewsError" class="mb-1">
              <i class="bi bi-star-fill text-warning"></i>
              <span class="fw-bold">{{ averageRating.toFixed(1) }}</span>
              <span class="ms-1 text-muted">({{ reviewCount }}개의 리뷰)</span>
            </div>
            <div v-else-if="!loadingReviews && !fetchReviewsError && reviewCount === 0" class="text-muted mb-1">
              첫 리뷰를 작성해주세요!
            </div>
            <div v-else-if="loadingReviews" class="text-muted mb-1">리뷰를 불러오는 중...</div>
            <div v-else-if="fetchReviewsError" class="text-danger mb-1">리뷰를 불러오는데 실패했습니다.</div>
            <hr />
          </div>
        </div>
        <div v-else-if="!loading" class="alert alert-warning">숙소 정보를 불러오지 못했습니다.</div>

        <!-- 방문자 리뷰 섹션 -->
        <div class="card mb-4" id="reviews-section">
          <div class="card-body">
            <h2 class="h5 fw-bold mb-3">방문자 리뷰 (총 {{ reviewCount }}개)</h2>
            <!-- 리뷰 캐러셀 (슬라이드) -->
            <template v-if="reviewCarouselItems.length > 0">
              <div class="position-relative mb-4" style="height: 180px">
                <div class="overflow-hidden h-100 rounded border">
                  <div
                    class="d-flex transition"
                    :style="{
                      transform: `translateX(-${currentReviewSlideIndex * 100}%)`,
                      width: `${reviewCarouselItems.length * 100}%`,
                    }"
                  >
                    <div
                      v-for="item in reviewCarouselItems"
                      :key="
                        item.type === 'review' && item.review
                          ? item.review.reviewId
                          : 'see-all-' + currentReviewSlideIndex
                      "
                      class="flex-shrink-0 p-3 bg-white"
                      style="
                        width: 100%;
                        height: 100%;
                        display: flex;
                        flex-direction: column;
                        justify-content: space-between;
                      "
                    >
                      <template v-if="item.type === 'review' && item.review">
                        <div>
                          <div class="d-flex justify-content-between align-items-center mb-2">
                            <span class="fw-semibold small">{{ item.review.userNickname || "익명" }}</span>
                            <span>
                              <i
                                v-for="n in 5"
                                :key="n + 'crs_star'"
                                class="bi"
                                :class="
                                  n <= item.review.rating ? 'bi-star-fill text-warning' : 'bi-star text-secondary'
                                "
                                style="font-size: 0.95rem"
                              ></i>
                            </span>
                          </div>
                          <p
                            class="small text-muted mb-1"
                            style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 90%"
                          >
                            {{ item.review.content }}
                          </p>
                        </div>
                        <span class="text-xxs text-secondary mt-auto">{{ formatDate(item.review.createdAt) }}</span>
                      </template>
                      <template v-else-if="item.type === 'see-all'">
                        <div class="d-flex flex-column align-items-center justify-content-center h-100 text-center">
                          <i class="bi bi-card-text fs-3 text-danger mb-2"></i>
                          <div class="fw-semibold text-danger mb-1">모든 리뷰 보기</div>
                          <div class="small text-muted">총 {{ reviewCount }}개의 리뷰를 확인하세요.</div>
                          <button @click="scrollToReviews" class="btn btn-danger btn-sm mt-3">전체 리뷰 보기</button>
                        </div>
                      </template>
                    </div>
                  </div>
                </div>
                <button
                  v-if="canPrevReviewSlide"
                  @click="prevReviewSlide"
                  class="btn btn-light position-absolute top-50 start-0 translate-middle-y"
                  style="z-index: 2"
                >
                  <i class="bi bi-chevron-left"></i>
                </button>
                <button
                  v-if="canNextReviewSlide"
                  @click="nextReviewSlide"
                  class="btn btn-light position-absolute top-50 end-0 translate-middle-y"
                  style="z-index: 2"
                >
                  <i class="bi bi-chevron-right"></i>
                </button>
              </div>
            </template>
            <!-- 리뷰 작성/리스트 등은 위 코드 계속 -->
            <slot name="review-forms-and-list"></slot>
          </div>
        </div>

        <!-- 객실 선택 UI -->
        <div class="card mb-4" id="booking-options-section">
          <div class="card-body">
            <h2 class="h5 fw-bold mb-4">객실 선택</h2>
            <div class="row g-3 justify-content-center mb-3">
              <div class="col-auto">
                <button
                  @click="openDateSelectionModal"
                  class="btn btn-outline-secondary w-100 text-start"
                  style="min-width: 140px; max-width: 230px"
                  aria-label="날짜 선택하기"
                >
                  <div class="small text-muted mb-1">체크인 - 체크아웃</div>
                  <div class="fw-semibold">{{ selectedDateRangeDisplay }}</div>
                </button>
              </div>
              <div class="col-auto">
                <button
                  @click="openGuestSelectionModal"
                  class="btn btn-outline-secondary w-100 text-start"
                  style="min-width: 90px; max-width: 150px"
                  aria-label="인원 선택하기"
                >
                  <div class="small text-muted mb-1">인원</div>
                  <div class="fw-semibold">{{ selectedGuestCountDisplay }}</div>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 객실 리스트 -->
        <div class="mb-4" id="room-list-section">
          <div v-if="rooms && rooms.length > 0">
            <div v-if="areAllRoomsUnbookable" class="alert alert-warning text-center">
              <i class="bi bi-exclamation-circle me-2"></i>
              선택하신 날짜와 인원으로는 현재 예약 가능한 객실이 없습니다. 다른 조건으로 검색해보세요.
            </div>
            <div>
              <RoomListItem
                v-for="room_item in rooms"
                :key="room_item.roomId"
                :room="room_item"
                :is-bookable="isRoomBookable(room_item)"
                @view-detail="goToRoomDetail(room_item)"
                @book-room="handleBookRoom(room_item)"
                @add-to-cart="handleAddToCart(room_item)"
                class="mb-3"
              />
            </div>
          </div>
          <div v-else-if="!loading" class="text-center bg-white p-4 rounded shadow-sm border">
            <i class="bi bi-door-closed fs-2 text-secondary mb-2"></i>
            <p class="text-muted">이 숙소에는 현재 등록된 객실 정보가 없습니다.</p>
          </div>
        </div>

        <!-- 숙소 소개 -->
        <div class="card mb-5" id="accommodation-description-section">
          <div class="card-body">
            <h2 class="h5 fw-bold mb-3">숙소 소개</h2>
            <p class="text-muted mb-4">{{ accommodation?.description || "등록된 숙소 설명이 없습니다." }}</p>
            <h3 class="h6 fw-bold mb-2">편의시설</h3>
            <ul v-if="amenitiesArray.length > 0" class="row row-cols-2 row-cols-sm-3 g-2 text-secondary mb-0">
              <li v-for="(amenity, idx) in amenitiesArray" :key="idx" class="col d-flex align-items-center">
                <i class="bi bi-check-circle-fill text-success me-2"></i>
                {{ amenity }}
              </li>
            </ul>
            <p v-else class="text-muted mb-0">등록된 편의시설 정보가 없습니다.</p>
          </div>
        </div>
      </div>
      <!-- ========== 모달 (Element Plus/직접 구현은 기존 코드 그대로) ========== -->

      <!-- 날짜 선택 모달 (새로운 컴포넌트 사용) -->
      <DateSelectModal
        :show="showDateModal"
        :initialDateRange="[selectedCheckInDate, selectedCheckOutDate]"
        :maxBookingDays="9"
        @update:show="showDateModal = $event"
        @apply="confirmDateSelectionAndCloseModal"
        @close="showDateModal = false"
      />

      <!-- 인원 선택 모달 (새로운 컴포넌트 사용) -->
      <GuestSelectModal
        :show="showGuestModal"
        :initial-guests="selectedGuests"
        :max-guests="maxCapacityOfAllRooms"
        @update:show="showGuestModal = $event"
        @apply="confirmGuestSelectionAndCloseModal"
        @close="showGuestModal = false"
      />

      <!-- 리뷰 수정 모달 (직접 구현 부분도 기존 구조 유지!) -->
      <div
        v-if="showEditReviewModal"
        class="modal fade show d-block"
        tabindex="-1"
        style="background: rgba(0, 0, 0, 0.4); z-index: 2000"
        @click.self="showEditReviewModal = false"
      >
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header">
              <h5 class="modal-title">리뷰 수정</h5>
              <button @click="showEditReviewModal = false" class="btn-close"></button>
            </div>
            <div class="modal-body">
              <div class="mb-3">
                <label class="form-label small">별점</label>
                <select v-model="editableReview.rating" class="form-select">
                  <option value="5">★★★★★ (5점)</option>
                  <option value="4">★★★★☆ (4점)</option>
                  <option value="3">★★★☆☆ (3점)</option>
                  <option value="2">★★☆☆☆ (2점)</option>
                  <option value="1">★☆☆☆☆ (1점)</option>
                </select>
              </div>
              <div class="mb-3">
                <label class="form-label small">내용</label>
                <textarea v-model="editableReview.content" class="form-control" rows="4"></textarea>
              </div>
              <div v-if="editReviewError" class="text-danger small">{{ editReviewError }}</div>
            </div>
            <div class="modal-footer">
              <button class="btn btn-secondary" @click="showEditReviewModal = false">취소</button>
              <button class="btn btn-pink" @click="submitEditReview" :disabled="isSubmittingEditReview">
                <span v-if="isSubmittingEditReview" class="spinner-border spinner-border-sm me-1"></span>
                수정 완료
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Toast Messages -->
      <div v-if="toasts.length > 0" class="toast-container position-fixed bottom-0 end-0 p-3" style="z-index: 2100">
        <div
          v-for="toast_item in toasts"
          :key="toast_item.id"
          class="toast show align-items-center text-bg-dark border-0 mb-2"
          role="alert"
        >
          <div class="d-flex">
            <div class="toast-body">{{ toast_item.message }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { useUserStore } from "@/store/userStore";
import { useAccommodationStore } from "@/store/accommodationStore";
import AccommodationHeader from "../../components/accommodation/AccommodationHeader.vue";
import RoomListItem from "../../components/accommodation/RoomListItem.vue";
import GuestSelectModal from "../../components/modals/GuestSelectModal.vue";
import DateSelectModal from "../../components/modals/DateSelectModal.vue";
import noImage from "@/assets/no-image.jpg";
import axios from "axios";
import { useCartStore } from "@/store/cartStore";
import { Swiper, SwiperSlide } from "swiper/vue";
import { Navigation, Pagination, Autoplay } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";
import apiUtils from "@/api";

const KOREAN_DAYS = ["일", "월", "화", "수", "목", "금", "토"];
const MAX_REVIEWS_IN_CAROUSEL = 10;

export default {
  name: "AccommodationDetail",
  components: {
    AccommodationHeader,
    RoomListItem,
    GuestSelectModal,
    DateSelectModal,
    Swiper,
    SwiperSlide,
  },
  props: { id: { type: [String, Number], required: true } },
  setup() {
    const userStore = useUserStore();
    const cartStore = useCartStore();
    const accommodationStore = useAccommodationStore();
    if (!userStore.isAuthenticated) {
      userStore.loadUserFromStorage();
    }
    return {
      userStore,
      cartStore,
      accommodationStore,
      swiperModules: [Navigation, Pagination, Autoplay],
    };
  },
  data() {
    const today = new Date();
    const initialCheckIn = new Date(today);
    const initialCheckOut = new Date(today);
    initialCheckOut.setDate(today.getDate() + 1);

    const queryParams = this.$route.query;
    let urlCheckInDate = initialCheckIn;
    let urlCheckOutDate = initialCheckOut;
    let urlGuests = 2;

    if (queryParams.checkInDate && typeof queryParams.checkInDate === "string") {
      const parsedCheckIn = new Date(queryParams.checkInDate);
      if (!isNaN(parsedCheckIn.getTime())) urlCheckInDate = parsedCheckIn;
    }
    if (queryParams.checkOutDate && typeof queryParams.checkOutDate === "string") {
      const parsedCheckOut = new Date(queryParams.checkOutDate);
      if (!isNaN(parsedCheckOut.getTime())) urlCheckOutDate = parsedCheckOut;
    }
    const adultsFromUrl = queryParams.adults ? parseInt(String(queryParams.adults), 10) : 0;
    const childrenFromUrl = queryParams.children ? parseInt(String(queryParams.children), 10) : 0;
    if (adultsFromUrl > 0 || childrenFromUrl > 0) {
      urlGuests = Math.max(1, adultsFromUrl + childrenFromUrl);
    }

    return {
      accommodation: null,
      rooms: [],
      reviews: [],
      loading: true,
      loadingReviews: false,
      fetchError: null,
      fetchReviewsError: null,
      isFavorite: false,
      wishlistApiLoading: false,
      noImage,
      newReview: { rating: 5, content: "" },
      isSubmittingReview: false,
      reviewError: "",
      userHasReviewed: false,
      canUserReview: false,
      fetchReviewEligibilityAttempted: false,
      showEditReviewModal: false,
      editableReview: { reviewId: null, rating: 5, content: "" },
      isSubmittingEditReview: false,
      editReviewError: "",
      isDeletingReview: null,
      averageRating: 0,
      reviewCount: 0,
      toasts: [],
      selectedCheckInDate: urlCheckInDate,
      selectedCheckOutDate: urlCheckOutDate,
      selectedGuests: urlGuests,
      currentReviewSlideIndex: 0,
      showDateModal: false,
      showGuestModal: false,
    };
  },
  computed: {
    headerTitle() {
      return this.accommodation?.title || "숙소 상세정보";
    },
    isLoggedIn() {
      return this.userStore.isAuthenticated;
    },
    userId() {
      return this.userStore.user?.id;
    },
    amenitiesArray() {
      if (this.accommodation && this.accommodation.amenities) {
        if (Array.isArray(this.accommodation.amenities)) {
          return this.accommodation.amenities.filter(Boolean);
        }
        return this.accommodation.amenities
          .split(",")
          .map((amenity) => amenity.trim())
          .filter(Boolean);
      }
      return [];
    },
    selectedDateRangeDisplay() {
      if (!this.selectedCheckInDate || !this.selectedCheckOutDate) return "날짜를 선택하세요";
      const checkInStr = this.formatDateWithDay(this.selectedCheckInDate);
      const checkOutStr = this.formatDateWithDay(this.selectedCheckOutDate);
      const nights = this.calculateNights(this.selectedCheckInDate, this.selectedCheckOutDate);
      return `${checkInStr} ~ ${checkOutStr} • ${nights}박`;
    },
    selectedGuestCountDisplay() {
      return `인원 ${this.selectedGuests}명`;
    },
    reviewCarouselItems() {
      if (this.loadingReviews || this.fetchReviewsError || this.reviewCount === 0) {
        return [];
      }
      const carouselReviews = this.reviews
        .slice(0, MAX_REVIEWS_IN_CAROUSEL)
        .map((review) => ({ type: "review", review }));
      if (this.reviewCount > 0) {
        carouselReviews.push({ type: "see-all" });
      }
      return carouselReviews;
    },
    canPrevReviewSlide() {
      return this.currentReviewSlideIndex > 0;
    },
    canNextReviewSlide() {
      return this.currentReviewSlideIndex < this.reviewCarouselItems.length - 1;
    },
    areAllRoomsUnbookable() {
      if (!this.rooms || this.rooms.length === 0) {
        return false;
      }
      if (!this.selectedCheckInDate || !this.selectedCheckOutDate || this.selectedGuests === 0) {
        return false;
      }
      return this.rooms.every((room) => !this.isRoomBookable(room));
    },
    maxCapacityOfAllRooms() {
      if (!this.rooms || this.rooms.length === 0) {
        return 10;
      }
      return (
        this.rooms.reduce((max, room) => {
          const capacity = parseInt(String(room.capacity || 0), 10);
          return capacity > max ? capacity : max;
        }, 0) || 10
      );
    },
    allAccommodationImages() {
      if (this.accommodation) {
        const images = [];
        if (this.accommodation.mainImageUrl) {
          images.push({ imageUrl: this.accommodation.mainImageUrl, isMain: true });
        } else if (this.accommodation.thumbnailImageUrl) {
          images.push({ imageUrl: this.accommodation.thumbnailImageUrl, isMain: true });
        }

        if (images.length > 0) {
          return images.filter((img) => img.imageUrl && img.imageUrl !== this.noImage);
        }
      }
      return this.loading ? [] : [{ imageUrl: this.noImage, isMain: true }];
    },
  },
  watch: {
    reviews(newReviews, oldReviews) {
      if (Array.isArray(newReviews)) {
        this.currentReviewSlideIndex = 0;
        if (this.isLoggedIn) {
          this.checkUserReviewStatus();
          this.checkReviewEligibility();
        }
      } else if (newReviews === null && oldReviews === undefined) {
      } else {
        this.userHasReviewed = false;
        this.canUserReview = false;
      }
    },
    isLoggedIn(newVal) {
      if (newVal && this.accommodation) {
        this.fetchFavoriteStatus(this.accommodation.accommodationId);
        this.checkUserReviewStatus();
        this.checkReviewEligibility();
      } else if (!newVal) {
        this.isFavorite = false;
        this.userHasReviewed = false;
        this.canUserReview = false;
      }
    },
    id: {
      immediate: true,
      handler(newId, oldId) {
        if (newId && newId !== oldId) {
          this.fetchData();
        } else if (newId && !this.accommodation) {
          this.fetchData();
        }
      },
    },
  },
  created() {},
  methods: {
    async fetchData() {
      if (!this.id) {
        this.fetchError = "숙소 ID가 유효하지 않습니다.";
        this.loading = false;
        return;
      }
      this.loading = true;
      this.fetchError = null;
      this.fetchReviewsError = null;
      this.currentReviewSlideIndex = 0;

      try {
        const accommodationId = Number(this.id);

        // 1. 숙소 및 객실 정보 로드
        try {
          await this.fetchAccommodationAndRooms(accommodationId);
        } catch (accError) {
          console.error("숙소/객실 정보 로드 실패:", accError);
          this.fetchError = accError.message || "숙소 및 객실 정보를 불러오는 중 오류가 발생했습니다.";
        }

        // 2. 리뷰 정보 로드 (숙소/객실 로드 성공 여부와 관계없이 시도)
        try {
          await this.fetchReviewsAndSummary(accommodationId);
        } catch (revError) {
          console.error("리뷰 정보 로드 실패:", revError);
          // fetchReviewsAndSummary 내부에서 이미 fetchReviewsError를 설정하므로 여기서는 추가 처리 불필요
        }

        // 3. 사용자 관련 정보 로드 (숙소 정보가 있어야 의미 있음)
        if (this.isLoggedIn && this.accommodation) {
          try {
            await this.fetchFavoriteStatus(this.accommodation.accommodationId);
            this.checkUserReviewStatus(); // API 호출이 아님, await 제거
            this.checkReviewEligibility(); // API 호출이 아님, await 제거
          } catch (userSpecificError) {
            console.error("사용자 특정 정보(찜, 리뷰 상태) 로드 실패:", userSpecificError);
            this.showToast("사용자 관련 정보를 가져오는데 일부 실패했습니다.", 3000, "warning");
          }
        }
      } catch (e) {
        // 이 최상위 catch는 예상치 못한 전반적인 오류 처리용
        console.error("fetchData에서 예상치 못한 오류:", e);
        if (!this.fetchError && !this.fetchReviewsError) {
          // 개별 에러가 이미 설정되지 않은 경우
          this.fetchError = e.message || "정보를 불러오는 중 전반적인 오류가 발생했습니다.";
        }
      } finally {
        this.loading = false;
      }
    },
    async fetchAccommodationAndRooms(accommodationId) {
      try {
        const params = {};
        if (this.selectedGuests > 0) params.guests = this.selectedGuests;
        if (this.selectedCheckInDate && this.selectedCheckOutDate) {
          params.checkInDate = this.formatDateForApi(this.selectedCheckInDate);
          params.checkOutDate = this.formatDateForApi(this.selectedCheckOutDate);
        }

        console.log("[AccommodationDetail] API Request Params for Rooms:", JSON.parse(JSON.stringify(params)));

        const res = await apiUtils.api.get(`/api/accommodations/${accommodationId}`, { params });
        if (res.data && res.data.accommodation) {
          if (res.data.accommodation.id && !res.data.accommodation.accommodationId) {
            res.data.accommodation.accommodationId = res.data.accommodation.id;
          }
          this.accommodation = res.data.accommodation;
          this.rooms = res.data.rooms || [];
        } else {
          throw new Error("숙소 정보를 찾을 수 없습니다.");
        }
      } catch (err) {
        console.error("Error fetching accommodation details:", err);
        this.fetchError = err.response?.data?.message || err.message || "숙소 정보를 가져오는데 실패했습니다.";
        if (!this.accommodation) {
          this.accommodation = null;
          this.rooms = [];
        }
      }
    },
    async fetchReviewsAndSummary(accommodationId) {
      this.loadingReviews = true;
      this.fetchReviewsError = null;
      try {
        const [reviewsRes, summaryRes] = await Promise.all([
          apiUtils.api.get(`/api/reviews/accommodation/${accommodationId}`),
          apiUtils.api.get(`/api/reviews/summary/accommodation/${accommodationId}`),
        ]);
        this.reviews = Array.isArray(reviewsRes.data) ? reviewsRes.data : [];
        if (summaryRes.data) {
          this.averageRating = summaryRes.data.averageRating || 0;
          this.reviewCount = summaryRes.data.totalReviews || 0;
        } else {
          this.averageRating = 0;
          this.reviewCount = 0;
        }
      } catch (error) {
        console.error("Error fetching reviews or summary:", error);
        this.fetchReviewsError = error.message || "리뷰 정보를 가져오는데 실패했습니다.";
        this.reviews = [];
        this.averageRating = 0;
        this.reviewCount = 0;
      } finally {
        this.loadingReviews = false;
      }
    },
    async fetchFavoriteStatus(accommodationId) {
      if (!this.isLoggedIn || !accommodationId) {
        this.isFavorite = false;
        return;
      }
      this.wishlistApiLoading = true;
      try {
        const response = await apiUtils.api.get(`/api/favorites/status?accommodationId=${accommodationId}`);
        this.isFavorite = response.data.isFavorite;
      } catch (error) {
        console.error("Error fetching favorite status:", error);
        this.isFavorite = false;
        if (error.response && error.response.status !== 404) {
          this.showToast("찜 상태를 불러오는데 실패했습니다.", 2000, "error");
        }
      } finally {
        this.wishlistApiLoading = false;
      }
    },
    checkUserReviewStatus() {
      if (!this.isLoggedIn || !this.userId) {
        this.userHasReviewed = false;
        return;
      }
      this.userHasReviewed =
        Array.isArray(this.reviews) && this.reviews.some((review) => review.userId === this.userId);
    },
    checkReviewEligibility() {
      this.fetchReviewEligibilityAttempted = true;
      this.canUserReview = !this.userHasReviewed;
    },
    async submitReview() {
      if (!this.newReview.content.trim() || !this.newReview.rating) {
        this.reviewError = "별점과 리뷰 내용을 모두 입력해주세요.";
        return;
      }
      if (!this.accommodation) {
        this.reviewError = "숙소 정보가 없어 리뷰를 등록할 수 없습니다.";
        return;
      }
      this.isSubmittingReview = true;
      this.reviewError = "";
      try {
        const payload = {
          accommodationId: this.accommodation.accommodationId,
          rating: parseInt(String(this.newReview.rating), 10),
          content: this.newReview.content,
        };
        await apiUtils.api.post(`/api/reviews`, payload);
        this.showToast("리뷰가 성공적으로 등록되었습니다.");
        this.newReview.rating = 5;
        this.newReview.content = "";
        await this.fetchReviewsAndSummary(this.accommodation.accommodationId);
      } catch (error) {
        console.error("Error submitting review:", error);
        this.reviewError = error.response?.data?.message || "리뷰 등록 중 오류가 발생했습니다.";
      } finally {
        this.isSubmittingReview = false;
      }
    },
    openEditReviewModal(review) {
      this.editableReview = { ...review, rating: review.rating.toString() };
      this.editReviewError = "";
      this.showEditReviewModal = true;
    },
    async submitEditReview() {
      if (!this.editableReview.content.trim() || !this.editableReview.rating || this.editableReview.reviewId === null) {
        this.editReviewError = "별점, 리뷰 내용, 리뷰 ID를 확인해주세요.";
        return;
      }
      this.isSubmittingEditReview = true;
      this.editReviewError = "";
      try {
        const payload = {
          rating: parseInt(String(this.editableReview.rating), 10),
          content: this.editableReview.content,
        };
        await apiUtils.api.put(`/api/reviews/${this.editableReview.reviewId}`, payload);
        this.showToast("리뷰가 성공적으로 수정되었습니다.");
        this.showEditReviewModal = false;
        if (this.accommodation) {
          await this.fetchReviewsAndSummary(this.accommodation.accommodationId);
        }
      } catch (error) {
        console.error("Error editing review:", error);
        this.editReviewError = error.response?.data?.message || "리뷰 수정 중 오류가 발생했습니다.";
      } finally {
        this.isSubmittingEditReview = false;
      }
    },
    async deleteReview(reviewId) {
      if (!confirm("정말로 이 리뷰를 삭제하시겠습니까?")) return;
      this.isDeletingReview = reviewId;
      try {
        await apiUtils.api.delete(`/api/reviews/${reviewId}`);
        this.showToast("리뷰가 성공적으로 삭제되었습니다.");
        if (this.accommodation) {
          await this.fetchReviewsAndSummary(this.accommodation.accommodationId);
        }
      } catch (error) {
        console.error("Error deleting review:", error);
        this.showToast(error.response?.data?.message || "리뷰 삭제 중 오류가 발생했습니다.");
      } finally {
        this.isDeletingReview = null;
      }
    },
    async toggleWishlist() {
      if (!this.isLoggedIn) {
        this.showToast("찜하기는 로그인 후 이용 가능합니다.", 3000, "info");
        return;
      }
      if (!this.accommodation) {
        this.showToast("숙소 정보가 유효하지 않아 찜할 수 없습니다.", 3000, "error");
        return;
      }

      this.wishlistApiLoading = true;
      const accommodationId = this.accommodation.accommodationId;

      try {
        if (this.isFavorite) {
          await apiUtils.api.delete(`/api/favorites?accommodationId=${accommodationId}`);
          this.isFavorite = false;
          this.showToast("찜 목록에서 삭제되었습니다.");
        } else {
          await apiUtils.api.post(`/api/favorites`, { accommodationId });
          this.isFavorite = true;
          this.showToast("찜 목록에 추가되었습니다.");
        }
        if (this.accommodationStore) {
          this.accommodationStore.toggleFavoriteLocal(accommodationId);
        }
      } catch (error) {
        console.error("Error toggling wishlist in component:", error);
        this.showToast(error.response?.data?.message || "찜 처리 중 오류가 발생했습니다.", 3000, "error");
      } finally {
        this.wishlistApiLoading = false;
      }
    },
    shareAccommodation() {
      if (!this.accommodation) return;
      if (navigator.share) {
        navigator
          .share({
            title: this.accommodation.title,
            text: `${this.accommodation.title} 숙소 정보를 확인해보세요!`,
            url: window.location.href,
          })
          .catch((error) => {
            console.log("Error sharing", error);
            this.showToast("공유에 실패했습니다. 링크를 복사해주세요.", 3000, "error");
          });
      } else {
        navigator.clipboard
          .writeText(window.location.href)
          .then(() => this.showToast("링크가 클립보드에 복사되었습니다."))
          .catch(() => this.showToast("링크 복사에 실패했습니다.", 3000, "error"));
      }
    },
    formatDate(dateInput) {
      if (!dateInput) return "";
      const date = typeof dateInput === "string" ? new Date(dateInput) : dateInput;
      if (!(date instanceof Date) || isNaN(date.getTime())) return "날짜 오류";

      const options = { year: "numeric", month: "short", day: "numeric" };
      return date.toLocaleDateString("ko-KR", options);
    },
    formatPrice(price) {
      if (price === undefined || price === null || isNaN(Number(price))) return "가격 문의";
      return new Intl.NumberFormat("ko-KR").format(Number(price));
    },
    goToRoomDetail(room) {
      if (!this.isRoomBookable(room)) {
        this.showToast("선택하신 조건으로 현재 예약이 불가능한 객실입니다.", 3000);
        return;
      }

      if (!this.selectedCheckInDate || !this.selectedCheckOutDate) {
        this.showToast("날짜를 선택해야 객실 상세 정보를 볼 수 있습니다.", 3000);
        const bookingOptionsSection = document.getElementById("booking-options-section");
        if (bookingOptionsSection) {
          bookingOptionsSection.scrollIntoView({ behavior: "smooth", block: "center" });
        }
        return;
      }
      if (this.selectedGuests === 0) {
        this.showToast("인원을 선택해야 객실 상세 정보를 볼 수 있습니다.", 3000);
        const bookingOptionsSection = document.getElementById("booking-options-section");
        if (bookingOptionsSection) {
          bookingOptionsSection.scrollIntoView({ behavior: "smooth", block: "center" });
        }
        return;
      }

      const queryParams = {
        checkIn: this.formatDateForApi(this.selectedCheckInDate),
        checkOut: this.formatDateForApi(this.selectedCheckOutDate),
        guests: this.selectedGuests.toString(),
      };

      this.$router.push({
        name: "RoomDetail",
        params: { roomId: room.roomId.toString() },
        query: queryParams,
      });
    },
    scrollToReviews() {
      const reviewsSection = document.getElementById("reviews-section");
      if (reviewsSection) {
        reviewsSection.scrollIntoView({ behavior: "smooth", block: "start" });
      }
    },
    showToast(msg, duration = 3000, type = "info") {
      const toastId = Date.now();
      this.toasts.push({ id: toastId, message: msg, type });
      setTimeout(() => {
        this.toasts = this.toasts.filter((t) => t.id !== toastId);
      }, duration);
    },
    formatDateForApi(date) {
      if (!date) return undefined;
      const year = date.getFullYear();
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      return `${year}-${month}-${day}`;
    },
    formatDateWithDay(date) {
      if (!date) return "";
      const month = (date.getMonth() + 1).toString().padStart(2, "0");
      const day = date.getDate().toString().padStart(2, "0");
      const dayOfWeek = KOREAN_DAYS[date.getDay()];
      return `${month}.${day}(${dayOfWeek})`;
    },
    calculateNights(startDate, endDate) {
      if (!startDate || !endDate) return 0;
      const diffTime = Math.abs(endDate.getTime() - startDate.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return diffDays > 0 ? diffDays : 0;
    },
    openDateSelectionModal() {
      this.showDateModal = true;
    },
    async confirmDateSelectionAndCloseModal(dates) {
      if (dates && dates[0] && dates[1]) {
        this.selectedCheckInDate = new Date(dates[0]);
        this.selectedCheckOutDate = new Date(dates[1]);
        this.showToast("날짜가 선택되어 객실 정보를 업데이트합니다.");

        this.loading = true;
        try {
          if (this.accommodation) {
            await this.fetchAccommodationAndRooms(this.accommodation.accommodationId);
          } else if (this.id) {
            await this.fetchAccommodationAndRooms(Number(this.id));
          }
        } catch (error) {
          console.error("날짜 변경 후 데이터 다시 로드 실패:", error);
          this.showToast("객실 정보 업데이트 중 오류가 발생했습니다.", 3000, "error");
        } finally {
          this.loading = false;
        }
      }
      this.showDateModal = false;
    },
    openGuestSelectionModal() {
      this.showGuestModal = true;
    },
    async confirmGuestSelectionAndCloseModal(payload) {
      if (payload && typeof payload.guests === "number") {
        this.selectedGuests = payload.guests;
        this.showToast("인원이 선택되어 객실 정보를 업데이트합니다.");

        if (this.selectedCheckInDate && this.selectedCheckOutDate) {
          this.loading = true;
          try {
            if (this.accommodation) {
              await this.fetchAccommodationAndRooms(this.accommodation.accommodationId);
            } else if (this.id) {
              await this.fetchAccommodationAndRooms(Number(this.id));
            }
          } catch (error) {
            console.error("인원 변경 후 데이터 다시 로드 실패:", error);
            this.showToast("객실 정보 업데이트 중 오류가 발생했습니다.", 3000, "error");
          } finally {
            this.loading = false;
          }
        }
      }
      this.showGuestModal = false;
    },
    nextReviewSlide() {
      if (this.canNextReviewSlide) {
        this.currentReviewSlideIndex++;
      }
    },
    prevReviewSlide() {
      if (this.canPrevReviewSlide) {
        this.currentReviewSlideIndex--;
      }
    },
    async handleBookRoom(room) {
      if (!this.isRoomBookable(room)) {
        this.showToast("선택하신 조건으로 현재 예약이 불가능하여 바로 예약할 수 없습니다.", 3000);
        return;
      }
      if (!this.isLoggedIn) {
        this.showToast("로그인이 필요한 서비스입니다.", 3000, "info");
        this.$router.push({ name: "Login", query: { redirect: this.$route.fullPath } });
        return;
      }
      if (!this.selectedCheckInDate || !this.selectedCheckOutDate || this.selectedGuests <= 0) {
        this.showToast("날짜와 인원을 모두 선택해야 바로 예약할 수 있습니다.", 3000, "info");
        const bookingOptionsSection = document.getElementById("booking-options-section");
        if (bookingOptionsSection) {
          bookingOptionsSection.scrollIntoView({ behavior: "smooth", block: "center" });
        }
        return;
      }

      const reservationDetails = {
        roomId: room.roomId,
        checkInDate: this.formatDateForApi(this.selectedCheckInDate),
        checkOutDate: this.formatDateForApi(this.selectedCheckOutDate),
        guestCount: this.selectedGuests,
        price: room.price, // 또는 room.pricePerNight 등 실제 가격 필드
        accommodationId: this.accommodation.accommodationId,
        roomName: room.name,
        accommodationTitle: this.accommodation.title,
        roomMainImageUrl: room.mainImageUrl || room.thumbnailUrl || this.noImage, // 객실 대표 이미지
        accommodationCheckInTime: this.accommodation.checkInTime,
        accommodationCheckOutTime: this.accommodation.checkOutTime,
      };

      this.$router.push({
        name: "ReservationForm", 
        query: reservationDetails,
      });
    },
    async handleAddToCart(room) {
      if (!this.isRoomBookable(room)) {
        this.showToast(
          room.minAvailableCount !== undefined && room.minAvailableCount <= 0
            ? "해당 객실은 현재 예약이 마감되었습니다. 다른 객실을 선택해주세요."
            : "선택하신 조건으로 현재 예약이 불가능하여 장바구니에 담을 수 없습니다.",
          3500,
          "warning"
        );
        return;
      }
      if (!this.isLoggedIn) {
        this.showToast("로그인이 필요한 서비스입니다.", 3000, "info");
        this.$router.push({ name: "Login", query: { redirect: this.$route.fullPath } });
        return;
      }
      if (!this.selectedCheckInDate || !this.selectedCheckOutDate || this.selectedGuests <= 0) {
        this.showToast("날짜와 인원을 모두 선택해야 장바구니에 담을 수 있습니다.", 3000, "info");
        const bookingOptionsSection = document.getElementById("booking-options-section");
        if (bookingOptionsSection) {
          bookingOptionsSection.scrollIntoView({ behavior: "smooth", block: "center" });
        }
        return;
      }

      const itemDetails = {
        roomId: room.roomId,
        checkInDate: this.formatDateForApi(this.selectedCheckInDate),
        checkOutDate: this.formatDateForApi(this.selectedCheckOutDate),
        guestCount: this.selectedGuests,
        price: room.price,
      };

      try {
        const response = await this.cartStore.addToCart(itemDetails);
        this.showToast(response.message || "객실이 장바구니에 추가되었습니다.");
      } catch (error) {
        console.error("AccommodationDetail - Error adding to cart:", error);
        let errorMessage = "장바구니 추가 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.";
        if (error.response?.data?.message) {
          if (error.response.data.message.includes("재고") || error.response.data.message.includes("마감")) {
            errorMessage = "선택하신 객실은 현재 예약이 마감되었거나 재고가 부족합니다. 다른 객실을 선택해주세요.";
          } else {
            errorMessage = error.response.data.message;
          }
        } else if (error.message) {
          if (error.message.includes("재고") || error.message.includes("마감")) {
            errorMessage = "선택하신 객실은 현재 예약이 마감되었거나 재고가 부족합니다. 다른 객실을 선택해주세요.";
          } else {
            errorMessage = error.message;
          }
        }
        this.showToast(errorMessage, 3500, "error");
      }
    },
    isRoomBookable(room) {
      if (!room) return false;

      if (!this.selectedCheckInDate || !this.selectedCheckOutDate || this.selectedGuests === 0) {
        return false;
      }

      const totalGuests = this.selectedGuests;
      const roomCapacity = parseInt(String(room.capacity || 0), 10);
      if (totalGuests > roomCapacity) {
        return false;
      }

      if (room.minAvailableCount !== undefined && room.minAvailableCount <= 0) {
        return false;
      }
      return true;
    },
  },
};
</script>

<style scoped>
.line-clamp-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.text-xxs {
  font-size: 0.65rem;
  line-height: 0.9rem;
}

.animate-toast-in-out {
  animation:
    toast-in 0.5s ease-out forwards,
    toast-out 0.5s ease-in 2.5s forwards;
}

@keyframes toast-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes toast-out {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(20px);
  }
}

.date-selection-dialog .el-dialog__header {
  display: none;
}
.date-selection-dialog .el-dialog__body {
  padding: 0;
}
.date-cell-content {
  cursor: pointer;
  border-radius: 4px;
}
.date-cell-content.is-selected {
  background-color: #fde2ec;
  color: #c12860;
  font-weight: bold;
}
.date-cell-content.is-disabled {
  color: #c0c4cc;
  cursor: not-allowed;
}
.date-cell-content:not(.is-disabled):hover {
  background-color: #fef7f9;
}
.guest-selection-dialog .el-dialog__header {
  display: none;
}
.guest-selection-dialog .el-dialog__body {
  padding: 0 !important;
}
.guest-selection-dialog .el-dialog__footer {
  padding: 0 1rem 1rem 1rem;
}
.btn-pink {
  background-color: #f0213b;
  color: #fff;
  border: none;
}
.btn-pink:hover,
.btn-outline-pink:hover {
  background-color: #d01c33;
  color: #fff;
}
.btn-outline-pink {
  border: 1px solid #f0213b;
  color: #f0213b;
  background: #fff;
}
.text-pink {
  color: #f0213b !important;
}

.text-truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.transition {
  transition: transform 0.4s cubic-bezier(0.77, 0, 0.18, 1);
}

.bg-pink {
  background-color: #f0213b !important;
}

.swiper-slide img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.swiper-pagination-bullet-active {
  background-color: #f0213b !important;
}

.swiper-button-next,
.swiper-button-prev {
  color: #f0213b !important;
  background-color: rgba(255, 255, 255, 0.7);
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.swiper-button-next::after,
.swiper-button-prev::after {
  font-size: 16px !important;
}

.accommodation-swiper {
  height: 320px;
  width: 100%;
}

.accommodation-swiper .swiper-slide img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.swiper-pagination-bullet-active {
  background-color: #f0213b !important;
}

.swiper-button-next::after,
.swiper-button-prev::after {
  font-size: 16px !important;
}
</style>
